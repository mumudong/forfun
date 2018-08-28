package hbase.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;

/**
 * MapReduce with HBase case
 * Created by hadoop on 10/19/16.
 */
public class HBaseMapReduce   {
    /**
     * mapper read from hbase table,一个region对应一个mapper
     *                                              <outputkey,outputvalue>
     */
    @SuppressWarnings("all")
    public static class ReadMap extends TableMapper<ImmutableBytesWritable, Put> {
        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context)
                throws IOException, InterruptedException {
            Put put = new Put(key.get());
            //choose the info:name and info:age to put
            for (Cell cell : value.rawCells()) {
                System.out.println("family:" + Bytes.toString(cell.getFamily())+"---->"+Bytes.toString(cell.getQualifier()));

                if ("info".equals(Bytes.toString(CellUtil.cloneFamily(cell)))) {
                        put.add(cell);
                        context.write(key, put);
                }
            }


        }
    }

    /**
     * reducer write to hbase table
     * <keyin,value,keyout>
     */
    public static class WriteReduce extends TableReducer<ImmutableBytesWritable, Put, ImmutableBytesWritable> {
        @Override
        protected void reduce(ImmutableBytesWritable key, Iterable<Put> values, Context context)
                throws IOException, InterruptedException {
            for (Put put : values) {
                context.write(key, put);
            }
        }
    }



    public static void main(String[] args) throws Exception {
        //get the conf
        System.setProperty("HADOOP_USER_NAME","hdfs");
        Configuration conf = HBaseConfiguration.create();
        conf.set("mapreduce.job.jar", "D:\\ksdler\\git_repository\\test\\target\\test-1.0-SNAPSHOT.jar");

        //run job
        Job job = Job.getInstance(conf);
        job.setJobName("hbase_to_hbase");
        job.setJarByClass(HBaseMapReduce.class);
        /**
         * hadoop在分配完map reduce task后，会预测性的判断某个map 或reduce task所在的
         * 节点资源有限，执行会比较慢，因此他在资源更多的节点上会启动一个完全一样的
         * map或 reduce task，同时执行，哪个先完成，就将未完成的那个task kill掉，提高
         * 整体job效率。
         */
        job.setSpeculativeExecution(false);
        job.setReduceSpeculativeExecution(false);
//        conf.setBoolean("mapreduce.map.speculative",false);
//        conf.setBoolean("mapreduce.reduce.speculative",false);
        Scan scan = new Scan();
        scan.setCaching(1000);
        /**
         * regionserver cache的大小，默认是0.2，是整个堆内存的多少比例作为regionserver的cache
         * 避免由于mapred使用regionserver的cache都被替换，造成hbase的查询性能明显下降。
         */
        scan.setCacheBlocks(false);
        TableMapReduceUtil.initTableMapperJob("student",
                scan,
                ReadMap.class,
                ImmutableBytesWritable.class,
                Put.class,
                job,false);
        TableMapReduceUtil.initTableReducerJob("t5",
                WriteReduce.class,
                job,null,null,null,null,false);
        boolean result = job.waitForCompletion(true);
        if(result){
            System.out.println("执行完毕!");
        }else{
            System.out.println("错误!!!!");
            System.exit(1);
        }
    }
}
