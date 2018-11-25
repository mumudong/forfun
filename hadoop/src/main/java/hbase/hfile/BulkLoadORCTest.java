package hbase.hfile;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
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
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.orc.mapred.OrcStruct;
import org.apache.orc.mapreduce.OrcInputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Administrator on 2018/3/29.
 */
@SuppressWarnings("all")
public class BulkLoadORCTest {
   static class BulkLoadMap extends Mapper<NullWritable, OrcStruct, ImmutableBytesWritable,Put> {
       Logger logger = LoggerFactory.getLogger(getClass());
       private   String columnFamily ;
       private    String tableName;
       private   String columnKey;
       private String[] columns;
       private String partitionValue;
       private String partitionColumn;
       @Override
       protected void setup(Context context) throws IOException, InterruptedException {
           super.setup(context);
           columnFamily = context.getConfiguration().get("columnFamily");
           tableName = context.getConfiguration().get("tableName");
           columnKey = context.getConfiguration().get("columnKey");
           columns = context.getConfiguration().get("columns").split(",");
           partitionColumn = context.getConfiguration().get("partitionColumn");
           FileSplit fileSplit = (FileSplit)context.getInputSplit();
           partitionValue = fileSplit.getPath().getParent().getName();
           partitionValue = partitionValue.substring(partitionValue.indexOf("=") + 1);
           /** birth=2011-11-11 */
           logger.error("partitionValue --> " + partitionValue);
           logger.error("partitionValue1 --> " + fileSplit.getPath().getName());
       }

       @Override
       protected void map(NullWritable key, OrcStruct value, Context context) throws IOException, InterruptedException {
           /** 获取rowkey */
           System.out.println("orcFileds --> " + value.getSchema().getFieldNames());
           IntWritable tkey = (IntWritable) value.getFieldValue(0);
           /**  struct<_col0:int,_col1:string,_col2:int,_col3:date>  */
           System.out.println("tkey --> " + tkey);
           ImmutableBytesWritable rowKey = new ImmutableBytesWritable(Bytes.toBytes(tkey.toString()));
           int columnCount = value.getNumFields();
           for (int i = 0; i < columnCount; i++) {
               value.getSchema().getFieldNames();
               Put put = new Put(Bytes.toBytes(tkey.toString()));
               put.add(Bytes.toBytes(columnFamily),
                       Bytes.toBytes(columns[i]),
                       Bytes.toBytes((value.getFieldValue(i)).toString()));
               context.write(rowKey, put);
           }
           Put put = new Put(Bytes.toBytes(tkey.toString()));
           put.add(Bytes.toBytes(columnFamily),
                   Bytes.toBytes(partitionColumn),
                   Bytes.toBytes(partitionValue));
           context.write(rowKey, put);
       }
   }

    public static void main(String[] args) throws Exception{
       System.setProperty("HADOOP_USER_NAME","root");
        Configuration conf = HBaseConfiguration.create();
        Properties prop = new Properties();
        prop.load(BulkLoadORCTest.class.getClassLoader().getResourceAsStream("my.properties"));

        for(String propName:prop.stringPropertyNames()){
            conf.set(propName,prop.getProperty(propName));
        }

        Job job = Job.getInstance(conf,"bulktest");
        job.setJarByClass(BulkLoadORCTest.class);
        job.setMapperClass(BulkLoadMap.class);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(Put.class);

        job.setSpeculativeExecution(false);
        job.setReduceSpeculativeExecution(false);
        //设置文件输入输出格式
        job.setInputFormatClass(OrcInputFormat.class);
        job.setOutputFormatClass(HFileOutputFormat2.class);
        //输入输出路径 args[0]  args[1]
//        FileInputFormat.setInputPaths(job,args[0]);
        OrcInputFormat.addInputPath(job,new Path(conf.get("inputPath")));
        Path output = new Path(conf.get("outputPath"));
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(output)) {
            fs.delete(output, true);
        }
        FileOutputFormat.setOutputPath(job,output);
        //设置Hfile导入
        HTable table = new HTable(conf, TableName.valueOf(conf.get("tableName")));
        HFileOutputFormat2.configureIncrementalLoad(job,table);

        if(job.waitForCompletion(true)){
            FsShell shell = new FsShell(conf);
            shell.run(new String[]{"-chmod","-R","777",conf.get("outputPath")});
            //载入到hbase
            LoadIncrementalHFiles loader = new LoadIncrementalHFiles(conf);
            loader.doBulkLoad(output,table);
        }
    }
}
