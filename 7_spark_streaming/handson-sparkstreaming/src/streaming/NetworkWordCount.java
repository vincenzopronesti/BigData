package streaming;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.StorageLevels;
import org.apache.spark.streaming.Durations;
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
 * Usage: JavaNetworkWordCount <hostname> <port>
 * <hostname> and <port> describe the TCP server that Spark Streaming would connect to receive data.
 *
 * To run this on your local machine, you need to first run a Netcat server
 *    `$ nc -lk 9999`
 * and then run the example
 *    `$ bin/run-example org.apache.spark.examples.streaming.JavaNetworkWordCount`
 */
public final class NetworkWordCount {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 9999;

    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws Exception {

        // Create the context with a 1 second batch size
        SparkConf sparkConf = new SparkConf()
                // Spark Streaming needs at least two working thread (local[2])
                .setMaster("local[2]")
                .setAppName("JavaNetworkWordCount");

        JavaStreamingContext ssc =
                new JavaStreamingContext(sparkConf, Durations.seconds(1));

        ssc.sparkContext().setLogLevel("ERROR");

        // Create a JavaReceiverInputDStream on target ip:port and count the
        // words in input stream of \n delimited text (eg. generated by 'nc')
        // Note that no duplication in storage level only for running locally.
        // Replication necessary in distributed scenario for fault tolerance.
        JavaReceiverInputDStream<String> lines =
                ssc.socketTextStream(HOSTNAME, PORT,
                        StorageLevels.MEMORY_ONLY);

        JavaDStream<String> words =
                lines.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator());

        JavaPairDStream<String, Integer> wordCounts =
                words.mapToPair(s -> new Tuple2<>(s, 1))
                     .reduceByKey((i1, i2) -> i1 + i2);

        wordCounts.print();
        ssc.awaitTermination();
    }

}