package streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Counts words in UTF8 encoded, '\n' delimited text received from the network every second.
 *
 * Usage: WindowBasedWordCount <hostname> <port>
 * <hostname> and <port> describe the TCP server that Spark Streaming would connect to receive data.
 *
 * To run this on your local machine, you need to first run a Netcat server
 *    `$ nc -l -p 9999`
 * and then run the example
 *    `$ bin/spark-submit streaming.WindowBasedWordCount`
 */
public class WindowBasedWordCount {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9999;
    private static final Pattern SPACE = Pattern.compile(" ");
    private static final String  LOCAL_DIR = "output/streamingwordcount/";
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
                new JavaStreamingContext(sparkConf,
                        Durations.seconds(WINDOW_TIME_UNIT_SECS));

        ssc.sparkContext().setLogLevel("ERROR");

        JavaReceiverInputDStream<String> lines =
                ssc.socketTextStream(HOSTNAME, PORT, StorageLevels.MEMORY_ONLY);

        /* Process incoming data using a sliding window (window length = 30, sliding interval = 2) */
        JavaDStream<String> linesInWindow =
                lines.window(Durations.seconds(30* WINDOW_TIME_UNIT_SECS),
                             Durations.seconds(2 * WINDOW_TIME_UNIT_SECS));

        JavaPairDStream<String, Integer> wordCounts =
                linesInWindow.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator())
                        .mapToPair(s -> new Tuple2<>(s, 1))
                        .reduceByKey((i1, i2) -> i1 + i2);

        // Operations
        wordCounts.print();
        wordCounts.foreachRDD(new SaveAsLocalFile());

        ssc.start();
        ssc.awaitTermination();
    }
}