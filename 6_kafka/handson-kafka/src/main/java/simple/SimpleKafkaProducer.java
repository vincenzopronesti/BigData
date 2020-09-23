package simple;

import config.Configuration;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class SimpleKafkaProducer {

    private final static String PRODUCER_ID = "simple-producer";

    private String topic;

    private Producer<String, String> producer;

    public SimpleKafkaProducer(String topic) {

        this.topic = topic;
        producer = createProducer();

    }

    private static Producer<String, String> createProducer() {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.BOOTSTRAP_SERVERS);

        props.put(ProducerConfig.CLIENT_ID_CONFIG, PRODUCER_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<String, String>(props);
    }

    public void produce(String key, String value) {


        try {

            final ProducerRecord<String, String> record =
                    new ProducerRecord<String, String>(topic, key, value);

            RecordMetadata metadata = producer.send(record).get();

            // DEBUG
            System.out.printf("sent record(key=%s value=%s) meta(partition=%d, offset=%d)\n",
                    record.key(), record.value(), metadata.partition(), metadata.offset());


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        producer.flush();
        producer.close();
    }
}
