package hdfs.mr;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

public class FofMapperTwo extends Mapper<LongWritable, Text, Friend, IntWritable>{

	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		
		String[] strs = StringUtils.split(value.toString(), ' ');
		System.out.println(strs[0]);
		System.out.println(strs[1]);
		
		context.write(new Friend(strs[0], strs[1], Integer.parseInt(strs[2])), new IntWritable(Integer.parseInt(strs[2])));
		context.write(new Friend(strs[1], strs[0], Integer.parseInt(strs[2])), new IntWritable(Integer.parseInt(strs[2])));
	}
}
