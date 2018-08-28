package hdfs.mr;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.StringUtils;

public class FofMapperOne extends Mapper<LongWritable, Text, Fof, IntWritable> {

	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		
		String[] strs = StringUtils.split(value.toString(), ' ');
		
		for (int i = 0; i < strs.length; i++) {
			String f1 = strs[i];
			context.write(new Fof(strs[0], f1), new IntWritable(0));
			for (int j = i+1; j < strs.length; j++) {
				String f2 = strs[j];
				context.write(new Fof(f1, f2), new IntWritable(1));
			}
		}
	}
}
