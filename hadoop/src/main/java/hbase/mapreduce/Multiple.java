package hbase.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.mapreduce.TableSplit;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/28.
 */
@SuppressWarnings("all")
public class Multiple {
    static class TBMapperOne extends TableMapper<ImmutableBytesWritable,Put>{
        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {

            Put put = new Put(key.get());
            byte[] bytes = null ;
            for(Cell cell:value.rawCells()){
                put.add(cell.getFamilyArray(),
                        cell.getQualifierArray(),
                        cell.getValueArray());
                bytes = cell.getFamilyArray();
            }
            InputSplit splitt = context.getInputSplit();
            if(bytes != null && splitt instanceof TableSplit) {
                TableSplit split = (TableSplit)splitt;
                put.add(bytes, split.getTableName(),split.getTableName());
            }
            context.write(key,put);
        }
    }

    static class TBMapperTwo extends TableMapper<ImmutableBytesWritable,Put>{
        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context) throws IOException, InterruptedException {
            Put put = new Put(key.get());
            byte[] bytes = null ;
            for(Cell cell:value.rawCells()){
                put.add(cell.getFamilyArray(),
                        cell.getQualifierArray(),
                        cell.getValueArray());
                bytes = cell.getFamilyArray();
            }
            if(bytes != null)
                put.add(bytes, Bytes.toBytes("student"),Bytes.toBytes("我来自student表"));
            context.write(key,put);
        }
        static class HBReducer extends TableReducer<ImmutableBytesWritable,Put,ImmutableBytesWritable>{
            @Override
            protected void reduce(ImmutableBytesWritable key, Iterable<Put> values, Context context) throws IOException, InterruptedException {
                for(Put put:values)
                    context.write(key,put);
            }
        }
        public static void main(String[] args) throws Exception{
            Configuration conf = HBaseConfiguration.create();
            System.setProperty("HADOOP_USER_NAME","hdfs");
            conf.set("mapreduce.job.jar","D:\\ksdler\\git_repository\\test\\target\\test-1.0-SNAPSHOT.jar");
            Job job = Job.getInstance(conf);
            job.setJarByClass(Multiple.class);
            job.setJobName("multipleHbase");
            Scan scan = new Scan();
            scan.setCacheBlocks(false);
            scan.setCaching(100);
            scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, "student".getBytes());
            Scan scan2 = new Scan();
            scan2.setCacheBlocks(false);
            scan2.setCaching(100);
            scan2.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, "t5".getBytes());
            List<Scan> scans = new ArrayList<Scan>();
            scans.add(scan);
            scans.add(scan2);
            TableMapReduceUtil.initTableMapperJob(scans,TBMapperOne.class,ImmutableBytesWritable.class,Put.class,job,false);
            TableMapReduceUtil.initTableReducerJob("t6",HBReducer.class,job);
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }
    }
}
