[![Build](https://github.com/FalkorDB/falkordb-kafka-connect/actions/workflows/build.yml/badge.svg)](https://github.com/FalkorDB/falkordb-kafka-connect/actions/workflows/build.yml)

FalkorDB Kafka Connect
=======================

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


##### how to build locally  

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
