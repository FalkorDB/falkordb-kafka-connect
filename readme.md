[![Build](https://github.com/FalkorDB/falkordb-kafka-connect/actions/workflows/build.yml/badge.svg)](https://github.com/FalkorDB/falkordb-kafka-connect/actions/workflows/build.yml)

#### build:

```bash
./gradlew  shadowJar && cp ./build/libs/falkordb-kafka-connect-uber.jar ./kafka-docker/connectors
```bash


##### Run kafka, falkor and feed the connector:

```bash
./download-kafka.sh
./run-kafka.sh
docker run -p 6379:6379 -p 3000:3000 -it --rm falkordb/falkordb:latest
./run-connector.sh
```

##### Send the queries to the topic:

```bash
cat queries.json | ./send-to-topic.sh
```
