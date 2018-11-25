package hbase.hfile.rcbulk;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.HFileOutputFormat2;
import org.apache.hadoop.hbase.mapreduce.SimpleTotalOrderPartitioner;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hive.hcatalog.rcfile.RCFileMapReduceInputFormat;

import java.io.IOException;
import java.util.List;

public class Driver extends Configured implements Tool{
    private static Configuration conf ;
    private static Connection connection;
    private static HBaseAdmin hadmin = null;
    static {
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("hbase.zookeeper.quorum", "hdp-5,hdp-6,hdp-7");
        try {
            connection = ConnectionFactory.createConnection(conf);
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)throws Exception {
        Driver driver = new Driver();

        String[] otherArgs = new GenericOptionsParser(conf,args).getRemainingArgs();
        if(otherArgs.length != 4){
            System.err.println("Usage: <rcfile> <hfile> <schemafile> <hbasetable>");
            System.exit(1);
        }
        String path = System.getProperty("user.dir") + otherArgs[2];
        List<String> fieldNames = HiveTableUtils.getFieldName(path);
        StringBuilder sb = new StringBuilder(fieldNames.get(0));
        for(int i = 1;i< fieldNames.size();i++)
            sb.append(":").append(fieldNames.get(i));
        conf.set("schema",sb.toString());
        System.exit(ToolRunner.run(conf,driver,otherArgs));
    }

    @Override
    public int run(String[] args) throws Exception {
        Configuration config = getConf();
        Job job = new Job(config,"RCFile to Hfile");
        job.setJarByClass(Driver.class);
        job.setMapperClass(RCMapper.class);
        job.setMapOutputKeyClass(ImmutableBytesWritable.class);
        job.setMapOutputValueClass(KeyValue.class);
        job.setNumReduceTasks(0);
        job.setPartitionerClass(SimpleTotalOrderPartitioner.class);
        job.setInputFormatClass(RCFileMapReduceInputFormat.class);
        job.setOutputFormatClass(HFileOutputFormat2.class);

        HTable table = new HTable(config, args[3]);
        HFileOutputFormat2.configureIncrementalLoad(job,table.getTableDescriptor(),table.getRegionLocator());
        RCFileMapReduceInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        return job.waitForCompletion(true) ? 0 : 1;
    }
}
