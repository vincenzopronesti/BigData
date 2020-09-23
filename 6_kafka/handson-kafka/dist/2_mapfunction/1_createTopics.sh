#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --create --topic TextLinesTopic --zookeeper localhost:2181 --partitions 1 --replication-factor 1
$KAFKA_HOME/bin/kafka-topics.sh  --create --topic UppercasedTextLinesTopic --zookeeper localhost:2181 --partitions 1 --replication-factor 1

