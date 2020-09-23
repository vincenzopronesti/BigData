#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --create --topic streams-plaintext-input --zookeeper localhost:2181 --partitions 1 --replication-factor 1
$KAFKA_HOME/bin/kafka-topics.sh  --create --topic streams-wordcount-output --zookeeper localhost:2181 --partitions 1 --replication-factor 1
