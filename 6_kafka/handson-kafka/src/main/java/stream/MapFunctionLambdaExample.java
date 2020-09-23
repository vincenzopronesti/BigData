/*
 * Copyright Confluent Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package stream;

import config.Configuration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

/**
 * Demonstrates how to perform simple, state-less transformations via map functions.

 * 1) Create the input and output topics used by this example.
 * $ bin/kafka-topics --create --topic TextLinesTopic \
 * --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic UppercasedTextLinesTopic \
 * --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 *
 * 2) Start this application
 *
 * 3) Write some input data to the source topic
 * $ bin/kafka-console-producer --broker-list localhost:9092 --topic TextLinesTopic
 *
 * 4) Inspect the resulting data in the output topics, e.g. via {@code kafka-console-consumer}.
 *
 * $ bin/kafka-console-consumer --topic UppercasedTextLinesTopic --from-beginning \
 * --bootstrap-server localhost:9092
 * $ bin/kafka-console-consumer --topic OriginalAndUppercasedTopic --from-beginning \
 * --bootstrap-server localhost:9092 --property print.key=true
 */
public class MapFunctionLambdaExample {

    private static Properties createStreamProperties() {

        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,
                "map-function-lambda-example");
        props.put(StreamsConfig.CLIENT_ID_CONFIG,
                "map-function-lambda-example-client");

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                Configuration.BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.ByteArray().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        return props;
    }


    public static void main(final String[] args) {

        final Properties props = createStreamProperties();

        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<byte[], String> textLines =
                builder.stream("TextLinesTopic",
                Consumed.with(Serdes.ByteArray(),
                        Serdes.String()));

        // Variant 1: using `mapValues`
        final KStream<byte[], String> uppercaseWithMapValues =
                textLines.mapValues(v -> v.toUpperCase());

        // Write (i.e. persist) the results to a new Kafka topic called "UppercasedTextLinesTopic".
        //
        // In this case we can rely on the default serializers for keys and values because their data
        // types did not change, i.e. we only need to provide the name of the output topic.
        uppercaseWithMapValues.to("UppercasedTextLinesTopic");

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.cleanUp(); //to perform a clean up of the local StateStore
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}
