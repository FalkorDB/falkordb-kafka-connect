#!/usr/bin/env bash

curl -o kafka.tgz -L https://downloads.apache.org/kafka/3.9.0/kafka_2.13-3.9.0.tgz
tar -xf kafka.tgz
mv kafka_2.13-* kafka_home
rm kafka.tgz

echo "KAFKA_HOME=$(pwd)/kafka_home"