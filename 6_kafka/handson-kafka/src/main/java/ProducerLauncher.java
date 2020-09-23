import simple.SimpleKafkaProducer;

public class ProducerLauncher {

    private final static String TOPIC = "t-singl-part";
    //private final static String TOPIC = "t-multi-part";

    private final static int NUM_MESSAGES = 1000;
    private final static int SLEEP = 1000;


    public static void main(String[] args) {

        SimpleKafkaProducer producer = new SimpleKafkaProducer(TOPIC);

        try {

            for (int i = 0; i < NUM_MESSAGES; i++) {

                long timestamp = System.currentTimeMillis();
                String payload = "Hello world at " + timestamp;
                producer.produce(null, payload);
                Thread.sleep(SLEEP);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }

    }
}
