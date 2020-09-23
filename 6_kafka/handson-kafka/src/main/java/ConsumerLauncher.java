import simple.SimpleKafkaConsumer;

public class ConsumerLauncher {

    private final static String TOPIC = "t-singl-part";
    //private final static String TOPIC = "t-multi-part";
    private static final int NUM_CONSUMERS = 3;

    public static void main(String[] args) {


        for (int i = 0; i < NUM_CONSUMERS; i++){
            Thread c = new Thread(new SimpleKafkaConsumer(i, TOPIC));
            c.start();
        }

    }

}
