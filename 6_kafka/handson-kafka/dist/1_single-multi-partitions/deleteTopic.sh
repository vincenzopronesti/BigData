#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic t-multi-part
$KAFKA_HOME/bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic t-singl-part
