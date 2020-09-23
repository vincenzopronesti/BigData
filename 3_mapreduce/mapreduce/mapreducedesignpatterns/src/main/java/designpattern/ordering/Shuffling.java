package designpattern.ordering;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Random;

public class Shuffling {

    public static void main(String[] args) throws Exception {

        /* Create and configure a new MapReduce Job */
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Shuffling");
        job.setJarByClass(Shuffling.class);

        /* Map function */
        job.setMapperClass(ShufflingMapper.class);
        // if equal to the reduce output, can be omitted
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);

        /* Reduce function */
        job.setReducerClass(ShufflingReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        /* Set input and output files/directories using command line arguments */
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        // if these files are different from text files, we can specify the format
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        /* Wait for job termination */
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }

    public static class ShufflingMapper
            extends Mapper<Object, Text, IntWritable, Text> {

        private static final int MAX_INT = 10;
        private Random rnd = new Random();
        private IntWritable rndKey = new IntWritable();

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            rndKey.set(rnd.nextInt(MAX_INT));
            context.write(rndKey, value);

        }

    }

    public static class ShufflingReducer
            extends Reducer<IntWritable, Text, Text, NullWritable> {

        public void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            for (Text value : values){
                // Write the word with a null value
                context.write(value, NullWritable.get());
            }

        }

    }

}