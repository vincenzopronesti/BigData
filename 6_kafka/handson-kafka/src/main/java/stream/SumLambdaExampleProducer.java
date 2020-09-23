package stream;

import config.Configuration;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.stream.IntStream;

public class SumLambdaExampleProducer {

    public static void main(final String[] args) {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);

        final KafkaProducer<String, Integer> producer = new KafkaProducer<>(props);

        System.out.println("Producing input ...");
        IntStream.range(0, 100)
                .mapToObj(val ->
                        new ProducerRecord<>(
                                SumLambdaExample.NUMBERS_TOPIC, "key_" + val, val))
                .forEach(producer::send);

        producer.flush();
    }

}