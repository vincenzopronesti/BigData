package streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Counts words in UTF8 encoded, '\n' delimited text received from the network every second.
 *
 * Usage: WindowBasedWordCount2 <hostname> <port>
 * <hostname> and <port> describe the TCP server that Spark Streaming would connect to receive data.
 *
 * To run this on your local machine, you need to first run a Netcat server
 *    `$ nc -l -p 9999`
 * and then run the example
 *    `$ bin/spark-submit streaming.WindowBasedWordCount2`
 */
public final class WindowBasedWordCount2 {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9999;

    private static final Pattern SPACE = Pattern.compile(" ");
    private static final String  LOCAL_DIR = "output/streamingwordcount2/";
    private static final String  LOCAL_CHECKPOINT_DIR = "tmp/checkpoint/streamingwordcount2/";

    private static final int WINDOW_TIME_UNIT_SECS = 1;

    private static class SaveAsLocalFile implements VoidFunction2<JavaPairRDD<String, Integer>, Time>{
        @Override
        public void call(JavaPairRDD<String, Integer> v1, Time v2) throws Exception {
            if (!v1.isEmpty())
                v1.saveAsTextFile(LOCAL_DIR + v2.milliseconds());
        }
    }

    public static void main(String[] args) throws Exception {

        // Create the context with a 1 second batch size
        SparkConf sparkConf = new SparkConf()
                // Spark Streaming needs at least two working thread (local[2])
                .setMaster("local[2]")
                .setAppName("JavaNetworkWordCount");
        JavaStreamingContext ssc =
                new JavaStreamingContext(sparkConf, Durations.seconds(WINDOW_TIME_UNIT_SECS));
        ssc.sparkContext().setLogLevel("ERROR");

        ssc.checkpoint(LOCAL_CHECKPOINT_DIR);

        JavaPairDStream<String, Integer> wordCountPairs =
                ssc.socketTextStream(HOSTNAME, PORT, StorageLevels.MEMORY_ONLY)
                        .flatMap(x -> Arrays.asList(SPACE.split(x)).iterator())
                        .mapToPair(s -> new Tuple2<>(s, 1));

        JavaPairDStream<String, Integer> wordCounts =
                wordCountPairs.reduceByKeyAndWindow(
                        (i1, i2) -> i1 + i2,
                        (i1, i2) -> i1 - i2,
                        Durations.seconds(30 * WINDOW_TIME_UNIT_SECS),
                        Durations.seconds(2  * WINDOW_TIME_UNIT_SECS));

        // Operations
        wordCounts.print();
        wordCounts.foreachRDD(new SaveAsLocalFile());

        ssc.start();
        ssc.awaitTermination();
    }

}