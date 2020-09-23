#!/bin/bash
$KAFKA_HOME/bin/kafka-console-consumer.sh --topic sum-of-odd-numbers-topic --from-beginning --bootstrap-server localhost:9092 --property print.key=true --property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
