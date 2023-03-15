include etc/environment.sh

run:
	./mvnw spring-boot:run
package:
	mvn package

ddb: ddb.package ddb.deploy
ddb.package:
	sam package --profile ${PROFILE} -t ${DDB_TEMPLATE} --output-template-file ${DDB_OUTPUT} --s3-bucket ${S3BUCKET} --s3-prefix ${DDB_STACK}
ddb.deploy:
	sam deploy --profile ${PROFILE} -t ${DDB_OUTPUT} --stack-name ${DDB_STACK} --parameter-overrides ${DDB_PARAMS} --capabilities CAPABILITY_NAMED_IAM

apigw: apigw.package apigw.deploy
apigw.package:
	sam package --profile ${PROFILE} -t ${APIGW_TEMPLATE} --output-template-file ${APIGW_OUTPUT} --s3-bucket ${S3BUCKET} --s3-prefix ${APIGW_STACK}
apigw.deploy:
	sam deploy --profile ${PROFILE} -t ${APIGW_OUTPUT} --region ${REGION} --stack-name ${APIGW_STACK} --parameter-overrides ${APIGW_PARAMS} --capabilities CAPABILITY_NAMED_IAM

lambda.local:
	sam local invoke -t ${APIGW_TEMPLATE} --parameter-overrides ${APIGW_PARAMS} --env-vars etc/envvars.json -e etc/event.json Fn | jq -r '.body' | jq
lambda.invoke.sync:
	aws lambda invoke --profile ${PROFILE} --function-name ${O_FN} --invocation-type RequestResponse --payload file://etc/event.json --cli-binary-format raw-in-base64-out --log-type Tail tmp/fn.json | jq "." > tmp/response.json
	cat tmp/response.json | jq -r ".LogResult" | base64 --decode
	cat tmp/fn.json | jq
lambda.invoke.async:
	aws lambda invoke --profile ${PROFILE} --function-name ${O_FN} --invocation-type Event --payload file://etc/event.json --cli-binary-format raw-in-base64-out --log-type Tail tmp/fn.json | jq "."

echo:
	echo ${P_FN_VERSION}