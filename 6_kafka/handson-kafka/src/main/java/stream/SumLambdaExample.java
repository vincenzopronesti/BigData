package stream;

import config.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

/**
 * 1) Create the input and output topics used by this example.
 * $ bin/kafka-topics --create --topic numbers-topic \
 * --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic sum-of-odd-numbers-topic \
 * --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 *
 * 2) Start this example application
 *
 * 3) Write some input data to the source topic
 *
 * 4) Inspect the resulting data in the output topics, e.g. via {@code kafka-console-consumer}.
 * $ bin/kafka-console-consumer --topic sum-of-odd-numbers-topic --from-beginning \
 * --new-consumer --bootstrap-server localhost:9092 \
 * --property value.deserializer=org.apache.kafka.common.serialization.IntegerDeserializer
 */
public class SumLambdaExample {

    private static final String SUM_OF_ODD_NUMBERS_TOPIC = "sum-of-odd-numbers-topic";
    static final String NUMBERS_TOPIC = "numbers-topic";

    private static Properties createStreamProperties() {

        final Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "sum-lambda-example");
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "sum-lambda-example-client");

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.Integer().getClass().getName());

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");

        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);

        return props;

    }


    public static void main(final String[] args) {

        final Properties props = createStreamProperties();
        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, Integer> input = builder.stream(NUMBERS_TOPIC);

        final KTable<String, Integer> sumOfOddNumbers = input
                // Creates a new stream with elements that satisfy the given predicate
                .filter((k, v) -> v % 2 != 0)

                // We want to compute the total sum across ALL numbers, so we must re-key all records to the
                // same key.  This re-keying is required because in Kafka Streams a data record is always a
                // key-value pair, and KStream aggregations such as `reduce` operate on a per-key basis.
                // The actual new key (here: `overallSum`) we pick here doesn't matter as long it is the same across
                // all records.
                .selectKey((k, v) -> "overallSum") // Set a new key for each input record

                // Count element by key
                .groupByKey()
                .reduce((v1, v2) -> v1 + v2); //reduce() leads the creation of a KTable

        sumOfOddNumbers.toStream().to(SUM_OF_ODD_NUMBERS_TOPIC, Produced.with(Serdes.String(), Serdes.Integer()));

        final KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.cleanUp();
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}