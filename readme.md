./gradlew  shadowJar && cp ./build/libs/falkordb-kafka-connect-uber.jar ./kafka-docker/connectors

```bash
download-kafka.sh
run-kafka.sh
docker run -p 6379:6379 -p 3000:3000 -it --rm falkordb/falkordb:latest
run-connector.sh
```
