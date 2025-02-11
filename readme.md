
![FalkorDB x Kafka Connect Banner](https://github.com/user-attachments/assets/a2c519ae-b817-4091-8ee6-1763a69b87c9)

[![Build](https://github.com/FalkorDB/falkordb-kafka-connect/actions/workflows/build.yml/badge.svg)](https://github.com/FalkorDB/falkordb-kafka-connect/actions/workflows/build.yml)
[![license](https://img.shields.io/github/license/falkordb/falkordb-kafka-connect.svg)](https://github.com/falkordb/falkordb-kafka-connect)
[![Forum](https://img.shields.io/badge/Forum-falkordb-blue)](https://github.com/orgs/FalkorDB/discussions)
[![Discord](https://img.shields.io/discord/1146782921294884966?style=flat-square)](https://discord.gg/ErBEqN9E)
[![Codecov](https://codecov.io/gh/falkordb/falkordb-kafka-connect/branch/main/graph/badge.svg)](https://codecov.io/gh/falkordb/falkordb-kafka-connect)
[![Release](https://img.shields.io/github/release/falkordb/falkordb-kafka-connect.svg)](https://github.com/falkordb/falkordb-kafka-connect/releases/latest)
[![Javadocs](https://www.javadoc.io/badge/com.falkordb/falkordb-kafka-connect.svg)](https://www.javadoc.io/doc/com.falkordb/falkordb-kafka-connect)
[![Maven Central Version](https://img.shields.io/maven-central/v/com.falkordb/falkordb-kafka-connect)](https://central.sonatype.com/artifact/com.falkordb/falkordb-kafka-connect)

# FalkorDB Kafka Connect

[![Try Free](https://img.shields.io/badge/Try%20Free-FalkorDB%20Cloud-FF8101?labelColor=FDE900&style=for-the-badge&link=https://app.falkordb.cloud)](https://app.falkordb.cloud)

##### What it does
This is an implementation of a Kafka Connect Sink Connector that writes data from kafka to a FalkorDB instance.

The connector reads the data from a Kafka topic and writes it to a FalkorDB instance.

The data is expected to be in JSON format, list of json object for a record, each json in the json array can execute command on different graph.

The format of the json is:
```json
[
  {
    "graphName": "falkordb",
    "command": "GRAPH_QUERY",
    "cypherCommand": "CREATE (p:Person {name: $name_param, age: $age_param, location: $location_param}) RETURN p",
    "parameters": {
      "location_param": "Location 0",
      "age_param": 20,
      "name_param": "Person 0"
    }
  },
  {
    "graphName": "falkordb",
    "command": "GRAPH_QUERY",
    "cypherCommand": "CREATE (p:Person {name: $name_param, age: $age_param, location: $location_param}) RETURN p",
    "parameters": {
      "location_param": "Location 1",
      "age_param": 21,
      "name_param": "Person 1"
    }
  }
]

```


##### how to get dependency from Maven/Gradle alternatively how to get the last version of the fat jar
You can build it locally (explain below) or download the fat jar from github  [release](https://github.com/FalkorDB/falkordb-kafka-connect/releases/download/v1.0.0/falkordb-kafka-connect-uber.jar) 

##### how to build locally  
This project use [sdkman](https://sdkman.io/) to manage multiple java versions, you can install it using the following command: `curl -s "https://get.sdkman.io" | bash`
Once installed you can use `sdk env` cmd in the terminal to select the right java sdk for this project.
After that you can build the project using the following command:
```bash
./gradlew build
```

##### How to run the example
There are a few scripts that can be used to run the example. The scripts are located in the `example` directory.

First run the `download-kafka.sh` script to download the kafka binaries. Then run the `run-kafka.sh` script to start the kafka server.
After kafka is up and running, run the `run-connector.sh` script to build the fat jar locally and  start the connector.

Then you can run falkordb locally using the docker image `docker run -p 6379:6379 -p 3000:3000 -it --rm falkordb/falkordb:latest`.
Lastly run the `send-to-topic.sh` script to send the queries to the kafka topic, if you wish to send the content of queries file to the topic use:
`cat queries.json | ./send-to-topic.sh`.

the example/connectors folder contains the connector configuration file that can be used to configure the connector.
