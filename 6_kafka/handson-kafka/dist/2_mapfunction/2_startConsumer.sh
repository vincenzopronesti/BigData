#!/bin/bash
$KAFKA_HOME/bin/kafka-console-consumer.sh --topic UppercasedTextLinesTopic --from-beginning --bootstrap-server localhost:9092
