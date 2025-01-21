#!/usr/bin/env bash

if [ -z "$KAFKA_HOME" ]; then
    KAFKA_HOME="$(pwd)/kafka_home"
fi
echo "KAFKA_HOME is set to $KAFKA_HOME"

# List all topics and delete them one by one
for topic in $("$KAFKA_HOME"/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list); do
    if [ "$topic" != "__consumer_offsets" ]; then  # Skip default Kafka topic
        echo "Deleting topic: $topic"
        "$KAFKA_HOME"/bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic "$topic"
    fi
done
