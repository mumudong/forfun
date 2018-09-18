package mumudong.hbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Put, Result, Scan}
import org.apache.hadoop.hbase.filter.{CompareFilter, SingleColumnValueFilter}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapred.TableOutputFormat
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}
import org.apache.hadoop.mapred.JobConf
import org.apache.spark.sql.SparkSession

/**
  * spark操作hbase有三种方式
  *    hbase api
  *    tableinputformat,tableoutputformat基于region
  *    第三方jar包sparkonhbase hbasecontext并行rdd
  */
object HbaseTest {
    def convertScanToString(scan:Scan) = {
        val proto = ProtobufUtil.toScan(scan)
        Base64.encodeBytes(proto.toByteArray)
    }
    def main(args: Array[String]): Unit = {
        val session = SparkSession.builder().appName("hbase")
                                            .master("local[2]")
                                            .getOrCreate()
        val conf = HBaseConfiguration.create()
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
        conf.set("hbase.zookeeper.property.clientPort", "2181")
        conf.set("hbase.zookeeper.quorum", "hadoop-5,hadoop-6,hadoop-7")
        conf.set("zookeeper.znode.parent","/hbase-unsecure")
        val jobConf = new JobConf(conf)
        jobConf.set(TableOutputFormat.OUTPUT_TABLE,"test_k")
        jobConf.setOutputFormat(classOf[TableOutputFormat])
        val rdd = session.sparkContext.makeRDD(Array(1)).flatMap(_ => 0 to 1000)
        println("==========")
        rdd.map(x => {
            var put = new Put(Bytes.toBytes(x.toString + "_key"))
            put.addColumn(Bytes.toBytes("info"), Bytes.toBytes("c1"), Bytes.toBytes(x % 10))
            (new ImmutableBytesWritable, put)
        }).saveAsHadoopDataset(jobConf)
        println("-----------")
        /**读取数据*/
        conf.set(TableInputFormat.INPUT_TABLE,"test_k")
        val scan = new Scan()
        scan.setFilter(new SingleColumnValueFilter("info".getBytes(),"c1".getBytes(),CompareFilter.CompareOp.GREATER_OR_EQUAL,Bytes.toBytes(5)))
        conf.set(TableInputFormat.SCAN,convertScanToString(scan))
        val readRdd = session.sparkContext.newAPIHadoopRDD(conf,classOf[TableInputFormat],
                                                                classOf[ImmutableBytesWritable],
                                                                classOf[Result])
        val count = readRdd.count()
        println("readCount : " + count)
        readRdd.cache()

        readRdd.foreach{case (_,result) =>
            val key = Bytes.toString(result.getRow)
            val value = Bytes.toInt(result.getValue("info".getBytes(),"c1".getBytes()))
                println("rowkey : " + key + " ,value : " + value)
        }



    }
}
