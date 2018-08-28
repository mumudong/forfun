package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FsShell;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by Administrator on 2018/3/29.
 */
public class BulkLoadTest {
   static class BulkLoadMap extends Mapper<LongWritable,Text,ImmutableBytesWritable,Put>{
       @Override
       protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
           String[] valueStrSplit = value.toString().split("\t");
           String tkey = valueStrSplit[0];
           String family = valueStrSplit[1].split(":")[0];
           String column = valueStrSplit[1].split(":")[1];
           String tvalue = valueStrSplit[2];
           ImmutableBytesWritable rowKey = new ImmutableBytesWritable(Bytes.toBytes(tkey));
           Put put = new Put(Bytes.toBytes(tkey));
           put.add(Bytes.toBytes(family),
                   Bytes.toBytes(column),
                   Bytes.toBytes(tvalue));
           context.write(rowKey,put);
       }
   }

    public static void main(String[] args) throws Exception{
       System.setProperty("HADOOP_USER_NAME","hdfs");
        Configuration conf = HBaseConfiguration.create();
        conf.set("mapreduce.job.jar","D:\\ksdler\\git_repository\\test\\target\\test-1.0-SNAPSHOT.jar");
        Job job = Job.getInstance(conf,"bulktest");
        job.setJarByClass(BulkLoadTest.class);
        job.setMapperClass(BulkLoadMap.class);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(Put.class);

        job.setSpeculativeExecution(false);
        job.setReduceSpeculativeExecution(false);
        //设置文件输入输出格式
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(HFileOutputFormat2.class);
        //输入输出路径 args[0]  args[1]
        FileInputFormat.setInputPaths(job,args[0]);
        FileOutputFormat.setOutputPath(job,new Path(args[1]));
        //设置Hfile导入
        HTable table = new HTable(conf, TableName.valueOf(args[2]));
        HFileOutputFormat2.configureIncrementalLoad(job,table);

        if(job.waitForCompletion(true)){
            FsShell shell = new FsShell(conf);
            shell.run(new String[]{"-chmod","-R","777",args[1]});
            //载入到hbase
            LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
            loader.doBulkLoad(new Path(args[1]),table);
        }
    }
}
