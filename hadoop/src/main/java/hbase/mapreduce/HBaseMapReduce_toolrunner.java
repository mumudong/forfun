package hbase.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
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
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * MapReduce with HBase case
 * Created by hadoop on 10/19/16.
 */
public class HBaseMapReduce_toolrunner extends Configured implements Tool {
    /**
     * mapper read from hbase table,一个region对应一个mapper
     *                                              <outputkey,outputvalue>
     */
    public static class ReadMap extends TableMapper<ImmutableBytesWritable, Put> {
        @Override
        protected void map(ImmutableBytesWritable key, Result value, Context context)
                throws IOException, InterruptedException {
            Put put = new Put(key.get());
            //choose the info:name and info:age to put
            for (Cell cell : value.rawCells()) {
                if ("info".equals(Bytes.toString(CellUtil.cloneFamily(cell)))) {
                    if ("name".equals(Bytes.toString(CellUtil.cloneQualifier(cell)))) {
                        put.add(cell);
                    } else if ("age".equals(Bytes.toString(CellUtil.cloneQualifier(cell)))) {
                        put.add(cell);
                    }
                }
            }

            context.write(key, put);
        }
    }

    /**
     * reducer write to hbase table
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

    /**
     * mapreduce driver class
     * @param args
     * @return
     * @throws Exception
     */
    public int run(String[] args) throws Exception {
        Configuration configuration = getConf();

        Job job = Job.getInstance(configuration, HBaseMapReduce_toolrunner.class.getSimpleName());
        job.setJarByClass(HBaseMapReduce_toolrunner.class);

        Scan scan = new Scan();
        scan.setCacheBlocks(false);
        TableMapReduceUtil.initTableMapperJob(
                "student", //input table
                scan, //scan instance to control column family and attribute selection
                ReadMap.class, //mapper class
                ImmutableBytesWritable.class, //mapper output key
                Put.class, //mapper output value
                job ,false);//addDependencyJars在本地需要为false,否则回去hdfs查找依赖包，因为是本地，所以查找不到会报错

        TableMapReduceUtil.initTableReducerJob(
                "t5", //output table
                WriteReduce.class, //reducer class
                job ,null,null,null,null,false);
        boolean isSuccess = job.waitForCompletion(true);
        return isSuccess ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        //get the conf
        System.setProperty("HADOOP_USER_NAME","hdfs");
        Configuration conf = HBaseConfiguration.create();
        //run job
        int status = ToolRunner.run(conf, new HBaseMapReduce_toolrunner(), args);

        //exit
        System.exit(status);
    }
}
