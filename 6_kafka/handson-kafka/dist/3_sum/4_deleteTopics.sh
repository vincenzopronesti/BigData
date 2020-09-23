#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --delete --topic numbers-topic --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-topics.sh  --delete --topic sum-of-odd-numbers-topic --zookeeper localhost:2181
