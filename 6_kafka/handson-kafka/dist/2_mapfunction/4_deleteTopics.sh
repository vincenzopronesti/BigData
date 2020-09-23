#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --delete --topic TextLinesTopic --zookeeper localhost:2181
$KAFKA_HOME/bin/kafka-topics.sh  --delete --topic UppercasedTextLinesTopic --zookeeper localhost:2181
