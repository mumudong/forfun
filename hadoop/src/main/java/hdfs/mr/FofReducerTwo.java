package hdfs.mr;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class FofReducerTwo extends Reducer<Friend, IntWritable, Text, NullWritable> {

	protected void reduce(Friend friend, Iterable<IntWritable> iterable,
			Context context) throws IOException, InterruptedException {
	
		int sum = 0;
		for(IntWritable i : iterable) {
            sum += i.get();
            System.out.println("reduce------->"+friend.getString() + "<------->" + sum);
		}
		
		String msg = friend.getFriend1() + " " + friend.getFriend2() + " " + sum;
		
		context.write(new Text(msg), NullWritable.get());
	}
}
