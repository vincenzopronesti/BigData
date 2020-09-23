package stream;

import config.Configuration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

/*
 * 1) Create the input and output topics used by this example.
 * $ bin/kafka-topics.sh --create --topic streams-plaintext-input \
 *                       --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics.sh  --create --topic streams-wordcount-output \
 *                        --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 *
 * 2) Start this  application
 *
 * 3) Write some input data to the source topic "streams-plaintext-input"
 *      e.g.:
 * $ bin/kafka-console-producer --broker-list localhost:9092 --topic streams-plaintext-input
 *
 * 4) Inspect the resulting data in the output topic "streams-wordcount-output"
 *      e.g.:
 * $ bin/kafka-console-consumer --topic streams-wordcount-output --from-beginning \
 *                              --new-consumer --bootstrap-server localhost:9092 \
 *                              --property print.key=true \
 *                              --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
 */
public class WordCount {


    private static Properties createStreamProperties(){
        final Properties props = new Properties();

        // Give the Streams application a unique name.
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-lambda-example");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "wordcount-lambda-example-client");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.BOOTSTRAP_SERVERS);

        // Specify default (de)serializers for record keys and for record values.
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());

        // Records should be flushed every 10 seconds.
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

        return props;
    }


    public static void main(final String[] args) throws Exception {

        final Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

        final Properties props = createStreamProperties();
        final StreamsBuilder builder = new StreamsBuilder();


        // Construct a `KStream` from the input topic "streams-plaintext-input",
        final KStream<String, String> textLines =
                builder.stream("streams-plaintext-input");

        final KTable<String, Long> wordCounts = textLines
                // Applies a flatMap operation on values of the key-value pairs
                .flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
                // Re-groups the records using the provided KeyValueMapper
                .groupBy((key, word) -> word)
                // Count the number of records in this stream by the grouped key
                .count();

        // Observe that the count() operation results in KTable

        // Write the `KTable<String, Long>` to the output topic.
        wordCounts.toStream().to("streams-wordcount-output",
                Produced.with(Serdes.String(), Serdes.Long()));

        // Now that we have finished the definition of the processing topology we can actually run
        // it via `start()`.  The Streams application as a whole can be launched just like any
        // normal Java application that has a `main()` method.
        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
