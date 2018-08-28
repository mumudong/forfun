package hdfs.mr;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.StringUtils;

public class FofReducerOne extends Reducer<Fof, IntWritable, Text, NullWritable> {

	protected void reduce(Fof fof, Iterable<IntWritable> iterable,
			Context context) throws IOException, InterruptedException {
		
		int sum = 0;
		Boolean flag = true;
		
		for(IntWritable i : iterable) {
			if(i.get() == 0) {
				flag = false;
				break;
			}
			sum += i.get();
		}
		
		if(flag) {
			String s = fof.toString();
			String msg = StringUtils.split(s, '\t')[0] + " " + StringUtils.split(s, '\t')[1] + " " + sum;
			context.write(new Text(msg), NullWritable.get());
		}
		
	}
}
