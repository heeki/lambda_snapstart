package cloud.heeki.snapstart.lib;

import cloud.heeki.snapstart.lib.Customer;
import cloud.heeki.snapstart.lib.DynamoAdapter;
import cloud.heeki.snapstart.lib.PropertiesLoader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.paginators.ScanIterable;

@RestController
public class SnapstartController {
    private Properties props = PropertiesLoader.loadProperties("application.properties");
    private ArrayList<Customer> customers = new ArrayList<Customer>();
    private Gson g = new GsonBuilder().setPrettyPrinting().create();
    private DynamoAdapter da;

    SnapstartController() {
        // initialization: client
        System.out.println(g.toJson(this.props));
        this.da = new DynamoAdapter(props.getProperty("table.name"));

        // initialization: read from dynamodb
        for (ScanResponse page : da.scan()) {
            for (Map<String, AttributeValue> item : page.items()) {
                Customer c = new Customer(item);
                customers.add(c);
            }
        }
    }

    @GetMapping("/")
    String getBase() {
        return g.toJson(this.props);
    }

    @GetMapping("/customer")
    String getCustomers() {
        return this.customers.toString();
    }

    @PostMapping("/customer")
    String createCustomer(@RequestBody Customer c) {
        this.da.put(c);
        customers.add(c);
        return c.toString();
    }

    @DeleteMapping("/customer/{id}")
    void deleteCustomer(@PathVariable String id) {
        this.da.delete(id);
        customers.removeIf(c -> c.uuid.toString().equals(id));
    }
}