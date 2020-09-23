#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --create --topic numbers-topic --zookeeper localhost:2181 --partitions 1 --replication-factor 1
$KAFKA_HOME/bin/kafka-topics.sh  --create --topic sum-of-odd-numbers-topic --zookeeper localhost:2181 --partitions 1 --replication-factor 1
