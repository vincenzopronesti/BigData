#!/bin/bash
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic t-singl-part
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic t-multi-part
