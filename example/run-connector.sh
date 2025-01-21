#!/usr/bin/env bash

if [ -z "$KAFKA_HOME" ]; then
    KAFKA_HOME="$(pwd)/kafka_home"
fi
echo "KAFKA_HOME is set to $KAFKA_HOME"

(cd ../ && ./gradlew shadowJar && cp build/libs/falkordb-kafka-connect-uber.jar kafka/connectors/)

$KAFKA_HOME/bin/connect-standalone.sh connectors/connect.properties connectors/falkordb-connector-config.properties