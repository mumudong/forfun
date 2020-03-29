package hdfs.muti;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.LazyOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * @since 17-2-4
 */
class MultiOutput extends Configured implements Tool {

    public static class MultiMapper extends Mapper<LongWritable,Text,Text,Text> {

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.length() > 0) {
                String[] lines = line.split(",");
                context.write(new Text(lines[0]), value);
            }
        }
    }

    public static class MultiReducer extends Reducer<Text,Text,NullWritable,Text> { //设置多文件输出
        private MultipleOutputs mos;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            mos = new MultipleOutputs(context);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException { //注意要调用close方法，否则会没有输出
            mos.close();
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text value : values) {
                //指定写出不同文件的数据
                mos.write("BaseOnKey", NullWritable.get(), value, key.toString() + "/" + key.toString());
                //指定写出全部数据
                mos.write("All", NullWritable.get(), value);
            }
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf);
        job.setJarByClass(MultiOutput.class);
        job.setMapperClass(MultiMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setReducerClass(MultiReducer.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);
        //BaseOnKey的输出
        MultipleOutputs.addNamedOutput(job, "BaseOnKey", TextOutputFormat.class, NullWritable.class, Text.class);
        //没有区分的所有输出
        MultipleOutputs.addNamedOutput(job, "All", TextOutputFormat.class, NullWritable.class, Text.class);
        job.setInputFormatClass(TextInputFormat.class);
        //取消part-r-00000新式文件输出
        LazyOutputFormat.setOutputFormatClass(job, TextOutputFormat.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        Path outputPath = new Path(args[1]);
        FileSystem fs = FileSystem.get(job.getConfiguration());
        if (fs.exists(outputPath))
            fs.delete(outputPath, true);
        FileOutputFormat.setOutputPath(job, outputPath);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(new MultiOutput(), args));
    }
}




