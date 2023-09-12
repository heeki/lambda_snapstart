include etc/environment.sh

package:
	mvn package
run:
	./mvnw spring-boot:run

ddb: ddb.package ddb.deploy
ddb.package:
	sam package --profile ${PROFILE} -t ${DDB_TEMPLATE} --output-template-file ${DDB_OUTPUT} --s3-bucket ${S3BUCKET} --s3-prefix ${DDB_STACK}
ddb.deploy:
	sam deploy --profile ${PROFILE} -t ${DDB_OUTPUT} --stack-name ${DDB_STACK} --parameter-overrides ${DDB_PARAMS} --capabilities CAPABILITY_NAMED_IAM

lambda: lambda.package lambda.deploy
lambda.package:
	sam package --profile ${PROFILE} -t ${LAMBDA_TEMPLATE} --output-template-file ${LAMBDA_OUTPUT} --s3-bucket ${S3BUCKET} --s3-prefix ${LAMBDA_STACK}
lambda.deploy:
	sam deploy --profile ${PROFILE} -t ${LAMBDA_OUTPUT} --region ${REGION} --stack-name ${LAMBDA_STACK} --parameter-overrides ${LAMBDA_PARAMS} --capabilities CAPABILITY_NAMED_IAM

lambda.local:
	sam local invoke -t ${LAMBDA_TEMPLATE} --parameter-overrides ${LAMBDA_PARAMS} --env-vars etc/envvars.json -e etc/event.json Fn | jq -r '.body' | jq
lambda.invoke.sync:
	aws lambda invoke --profile ${PROFILE} --function-name ${O_FN} --invocation-type RequestResponse --payload file://etc/event.json --cli-binary-format raw-in-base64-out --log-type Tail tmp/fn.json | jq "." > tmp/response.json
	cat tmp/response.json | jq -r ".LogResult" | base64 --decode
	cat tmp/fn.json | jq
lambda.invoke.async:
	aws lambda invoke --profile ${PROFILE} --function-name ${O_FN} --invocation-type Event --payload file://etc/event.json --cli-binary-format raw-in-base64-out --log-type Tail tmp/fn.json | jq "."
