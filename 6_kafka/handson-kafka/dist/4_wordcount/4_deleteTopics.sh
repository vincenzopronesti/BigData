#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --delete --topic streams-plaintext-input --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-topics.sh  --delete --topic streams-wordcount-output --zookeeper localhost:2181
