package hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

public class TestwithMultipleOutputs extends Configured implements Tool {

    public static class MapClass extends Mapper<LongWritable, Text, Text, IntWritable> {

        private MultipleOutputs<Text, IntWritable> mos;

        protected void setup(Context context) throws IOException, InterruptedException {
            mos = new MultipleOutputs<Text, IntWritable>(context);
        }

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] tokens = line.split("-");

            mos.write("MOSInt", new Text(tokens[0]), new IntWritable(Integer.parseInt(tokens[1])));  //（第一处）输出mosin开头的文件
            mos.write("MOSText", new Text(tokens[0]), tokens[2]);         //（第二处）输出mosout开头的文件
            mos.write("MOSText", new Text(tokens[0]), line, tokens[0] + "/");    //（第三处）同时也可写到指定的文件或文件夹中
        }

        protected void cleanup(Context context) throws IOException, InterruptedException {
            mos.close();
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = getConf();
        Job job = new Job(conf, "word count with MultipleOutputs");
        job.setJarByClass(TestwithMultipleOutputs.class);
        Path in = new Path(args[0]);
        Path out = new Path(args[1]);
        FileInputFormat.setInputPaths(job, in);
        FileOutputFormat.setOutputPath(job, out);

        job.setMapperClass(MapClass.class);
        job.setNumReduceTasks(0);

        MultipleOutputs.addNamedOutput(job, "MOSInt", TextOutputFormat.class, Text.class, IntWritable.class);
        MultipleOutputs.addNamedOutput(job, "MOSText", TextOutputFormat.class, Text.class, Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TestwithMultipleOutputs(), args);
        System.exit(res);
    }

}
