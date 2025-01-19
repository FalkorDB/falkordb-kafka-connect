#!/usr/bin/env bash

if [ -z "$KAFKA_HOME" ]; then
    KAFKA_HOME="$(pwd)/kafka_home"
fi
echo "KAFKA_HOME is set to $KAFKA_HOME"

cat messages.txt

echo ""

echo "from file: cat messages.txt | $0"

"$KAFKA_HOME"/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 \
 --topic falkordb-topic \
 --property "parse.key=true" --property "key.separator=:"