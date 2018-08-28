package hdfs.mr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class FOFJob {

	public static void main(String[] args) {

//		Boolean flag = jobOne();
//		if(flag) {
			jobTwo();
//		}
	}
	
	static Boolean jobOne() {
        System.setProperty("HADOOP_USER_NAME","hdfs");
		Configuration conf = new Configuration();
        conf.set("mapred.jar", "D:\\ksdler\\git_repository\\test\\target\\test-1.0-SNAPSHOT.jar");
//		conf.set("fs.defaultFS", "hdfs://node1:8020");
//		conf.set("yarn.resourcemanager.hostname", "node3");
		Boolean flag = false;
		try {
			Job job = Job.getInstance(conf);
			
			job.setJarByClass(FOFJob.class);
			job.setJobName("fof one job");
			
			job.setMapperClass(FofMapperOne.class);
			job.setReducerClass(FofReducerOne.class);
			
			job.setMapOutputKeyClass(Fof.class);
			job.setMapOutputValueClass(IntWritable.class);
						
			FileInputFormat.addInputPath(job, new Path("/test/friend/input"));

			Path output = new Path("/test/friend/output1");
			
			FileSystem fs = FileSystem.get(conf);
			if (fs.exists(output)) {
				fs.delete(output, true);
			}
			
			FileOutputFormat.setOutputPath(job, output);
			
			flag = job.waitForCompletion(true);
			if (flag) {
				System.out.println("job 1 success~~");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		};
		return flag;
	}
	

	static Boolean jobTwo() {
        System.setProperty("HADOOP_USER_NAME","hdfs");
        Configuration conf = new Configuration();
        conf.set("mapred.jar", "D:\\ksdler\\git_repository\\test\\target\\test-1.0-SNAPSHOT.jar");
//		conf.set("fs.defaultFS", "hdfs://node1:8020");
//		conf.set("yarn.resourcemanager.hostname", "node3");
		
		Boolean flag = false;
		try {
			Job job = Job.getInstance(conf);
			
			job.setJarByClass(FOFJob.class);
			job.setJobName("fof two job");
			
			job.setMapperClass(FofMapperTwo.class);
			job.setReducerClass(FofReducerTwo.class);
			
			job.setMapOutputKeyClass(Friend.class);
			job.setMapOutputValueClass(IntWritable.class);
			
			job.setSortComparatorClass(FofSort.class);
			job.setGroupingComparatorClass(FofGroup.class);
			
			FileInputFormat.addInputPath(job, new Path("/test/friend/output1"));

			Path output = new Path("/test/friend/output2");
			
			FileSystem fs = FileSystem.get(conf);
			if (fs.exists(output)) {
				fs.delete(output, true);
			}
			
			FileOutputFormat.setOutputPath(job, output);
			
			flag = job.waitForCompletion(true);
			if (flag) {
				System.out.println("job 2 success~~");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		};
		return flag;
	}
}
