package hdfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.security.UserGroupInformation;

/**
 * hadoop jar statistic-mr.jar com.statistic.script.Main -libjars /path/cascading-core-2.5.jar,/path/cascading-hadoop-2.5.jar
 */
public class JobRunner {
	
	static enum My{
		MyCounter;
	}

	public static void main(String[] args)throws Exception {
		Configuration config =new Configuration();
        config.set("mapred.jar", "D:\\ksdler\\git_repository\\forfun\\hadoop\\target\\hadoop-1.0-SNAPSHOT.jar");

//        config.set("mapreduce.application.classpath", System.getProperty("user.dir"));
		System.setProperty("HADOOP_USER_NAME","hdfs");

        String basePath = HdfsKerberosDemo.class.getResource("/").toString();
        String user = "hdfs";
        config.set("dfs.nameservices", "tianxi-ha");
        config.set("dfs.ha.namenodes.tianxi-ha", "nn1,nn2");
        config.set("dfs.namenode.rpc-address.tianxi-ha.nn1", "hadoop-1:8020");
        config.set("dfs.namenode.rpc-address.tianxi-ha.nn2", "hadoop-2:8020");
        config.set("dfs.client.failover.proxy.provider.ns1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        config.set("hadoop.security.authentication","kerberos");
        config.set("hadoop.registry.client.auth","kerberos");
        System.setProperty("java.security.krb5.conf", basePath.substring(6) + "krb5.conf");
        UserGroupInformation.setConfiguration(config);
        UserGroupInformation.loginUserFromKeytab(user, basePath.substring(6) + "hdfs.keytab");


		try {
			FileSystem fs =FileSystem.get(config);
			double d =0.001;
			int i=1;//计数器
			Job job =null;
			while(true){//定义一个收敛标准
				config.setInt("my.count", i);
				job =Job.getInstance(config,"pr"+i);
				
				job.setJarByClass(JobRunner.class);
				job.setMapperClass(PageRankMapper.class);
				job.setReducerClass(PageRankReducer.class);
				
				job.setMapOutputKeyClass(Text.class);
				job.setMapOutputValueClass(Text.class);
                /**
                 * TextInputFormat 默认的
                 * DBinputformat读取数据库
                 * KeyValueInputFormat
                 如果行中有分隔符，那么分隔符前面的作为key，后面的作为value
                 如果行中没有分隔符，那么整行作为key，value为空
                 默认分隔符为 \t
                 5CombineTextInputFormat:
                 将输入源目录下多个小文件 合并成一个文件(split)来交给mapreduce处理 这样只会生成一个map任务
                 比如用户给的文件全都是10K那种的文件， 其内部也是用的TextInputFormat 当合并大小大于(64M)128M的时候，
                 也会产生对应个数的split
                 MultipleInputs： 对应于 多个文件处理类型下 比如又要处理数据库的文件 同时又要处理小文件
                 */
				job.setInputFormatClass(KeyValueTextInputFormat.class);
				if(i==1){
					FileInputFormat.addInputPath(job, new Path("/test/pagerank/input/pagerank.txt"));
				}else{
					FileInputFormat.addInputPath(job, new Path("/test/pagerank/pr"+(i-1)));
				}
				
				Path output =new Path("/test/pagerank/pr"+i);
				if(fs.exists(output)){
					fs.delete(output, true);
				}
				FileOutputFormat.setOutputPath(job, output);
				job.waitForCompletion(true);
				long sum = job.getCounters().findCounter(My.MyCounter).getValue();
				
				double avg_d =sum/4000.0;
				if(avg_d<d){
					break;
				}
				i++;
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static class  PageRankMapper extends Mapper<Text, Text, Text, Text>{
		
		protected void map(Text key, Text value,
				Context context) throws IOException,
				InterruptedException {
			int i =context.getConfiguration().getInt("my.count", 1);
			double pr =1.0;
			String[] strs =value.toString().split("\t");
			if(i !=1){
				pr =Double.parseDouble(strs[0]);
			}
			if(i!= 1&& strs.length>1){//非第一次计算 
				int outputCount =strs.length-1;
				for(int j=1;j<strs.length;j++){
					String outputNode=strs[j];
					double outputValue= pr/outputCount;//投票的票值
					context.write(new Text(outputNode), new Text(""+outputValue));
				}
				context.write(key,value);//key:A    Value: 1.22	B	D
			}else if (i==1&& strs.length>0){//第一次计算
				int outputCount =strs.length;
				for(int j=0;j<strs.length;j++){
					String outputNode=strs[j];
					double outputValue= pr/outputCount;//投票的票值
					context.write(new Text(outputNode), new Text(""+outputValue));
				}
				context.write(key, new Text(pr+"\t"+value.toString()));//key:A    Value: 1.0	B	D
			}
			
		}
	}
	
	static class PageRankReducer extends Reducer<Text, Text, Text, Text>{
		protected void reduce(Text k, Iterable<Text> arg1,
				Context arg2)
				throws IOException, InterruptedException {
			double oldpr =0;
			double sourcePr =0;
			double sum =0;
			String newValue =null;
			for(Text v:arg1){
				String[] strs =v.toString().split("\t");
				double ouputValue =Double.parseDouble(strs[0]);
				if(strs.length>1){
					oldpr=Double.parseDouble(strs[0]);
					newValue =v.toString().substring(v.toString().indexOf("\t"), v.toString().length());
					sourcePr =ouputValue;
				}else{
					sum =sum+ouputValue;
				}
			}
			
			//新的pageRank
			double newpr =(0.15/4) + (0.85*sum);
			int d = (int) (Math.abs(newpr-oldpr) * 1000);
			
			arg2.getCounter(My.MyCounter).increment(d);
			
			newValue=newpr+newValue;
			arg2.write(k, new Text(newValue));
		}
		
		
	}
	
}

