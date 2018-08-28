package streamtest

import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{ForeachWriter, SparkSession}
import org.apache.spark.sql.streaming.ProcessingTime
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by Administrator on 2018/6/6.
  */
object StreamKafka010 {
    def main(args: Array[String]): Unit = {
        val conf = new SparkConf().setAppName("tt").setMaster("local[2]")
        val sc = new SparkContext(conf)
        val kafkaParams = Map[String, Object](
            "bootstrap.servers" -> "hadoop-5:6667,hadoop-6:6667",
            "key.deserializer" -> classOf[StringDeserializer],
            "value.deserializer" -> classOf[StringDeserializer],
            "group.id" -> "newStream",
            //      "auto.offset.reset" -> "latest",偏移量存至kafka如何配置
            "enable.auto.commit" -> (false: java.lang.Boolean)
        )
        val ssc = new StreamingContext(sc, Seconds(2))
        val fromOffsets = Map(new TopicPartition("test", 0) -> 1100449855L)
        val stream = KafkaUtils.createDirectStream[String, String](
            ssc,
            LocationStrategies.PreferConsistent,
            ConsumerStrategies.Assign[String, String](fromOffsets.keys.toList, kafkaParams, fromOffsets)
        )
        stream.print()
        stream.foreachRDD(rdd => {
            val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
            for (o <- offsetRanges) {
                println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}")
            }
            stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
        })

        //    stream.map(record => (record.key, record.value)).print(1)
        ssc.start()
        ssc.awaitTermination()
    }
    def structStream(): Unit = {
        System.setProperty("HADOOP_USER_NAME","hdfs")
        val spark = SparkSession.builder()
                                .appName("struct streaming")
                                .master("local[2]")
                                .getOrCreate()
        val inputStream = spark.readStream
                                .format("kafka")
                                .option("kafka.bootstrap.servers", "hadoop-5:6667,hadoop-6:6667")
                                .option("subscribe", "test_user_events")
                                .load()
        import spark.implicits._
        import java.sql._
        val writer = new JDBCSink()
        val query = inputStream.select($"key",$"value")
                                .as[(String,String)]
                                .map(kv => kv._1 + " " + kv._2)
                                .as[String]
                                .writeStream
                .foreach(writer)
                .outputMode("update")
//                                .outputMode("complete")
//                                .format("console")
//                                .outputMode("complete")
//                .format("parquet")
//                .option("path","/opt/mu_test/data")
//                .option("checkpointLocation","/opt/mu_test/check")
//                                .trigger(ProcessingTime("15 seconds"))
                                .start()
        query.awaitTermination()
    }
}

class JDBCSink  extends ForeachWriter[String]{

    override def open(partitionId: Long, version: Long): Boolean = {true}

    override def process(value:String): Unit = {
//        DbcpUtil.insertUpdateDelete("",value._1,value._2)
        println("---->" + value)
    }

    override def close(errorOrNull: Throwable): Unit = {}
}
