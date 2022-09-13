package cloud.heeki.snapstart;

// Reference: https://github.com/awslabs/aws-serverless-java-container/blob/main/samples/springboot2/pet-store/src/main/java/com/amazonaws/serverless/sample/springboot2/StreamLambdaHandler.java
import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.IllegalStateException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SnapstartHandler implements RequestStreamHandler {
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;
    private static Gson g = new Gson();

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(SnapstartApplication.class);
            handler.onStartup(servletContext -> {
                ArrayList<String> scope = new ArrayList<String>();
                scope.add("_HANDLER");
                scope.add("AWS_EXECUTION_ENV");
                scope.add("AWS_LAMBDA_FUNCTION_HANDLER");
                scope.add("AWS_LAMBDA_FUNCTION_MEMORY_SIZE");
                scope.add("AWS_LAMBDA_FUNCTION_NAME");
                scope.add("AWS_LAMBDA_FUNCTION_TIMEOUT");
                scope.add("AWS_LAMBDA_FUNCTION_VERSION");
                scope.add("AWS_LAMBDA_RUNTIME_API");
                scope.add("AWS_REGION");
                scope.add("AWS_SAM_LOCAL");
                scope.add("LD_LIBRARY_PATH");
                scope.add("PATH");
                HashMap<String, String> debug = new HashMap();
                for (String var : scope) {
                    debug.put(var, System.getenv(var));
                }
                System.out.println(g.toJson(debug));
            });
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot application", e);
        }
    }

    public SnapstartHandler() {
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        handler.proxyStream(inputStream, outputStream, context);
    }
}
