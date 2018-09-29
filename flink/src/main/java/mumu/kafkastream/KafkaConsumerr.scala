package mumu.kafkastream

import java.text.SimpleDateFormat
import java.util.{Date, Properties}

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaConsumer08}
import org.apache.flink.util.Collector
import org.slf4j.LoggerFactory


/**
  * ./bin/flink run -m yarn-cluster -yn 2 -c mumu.kafkastream.KafkaConsumer /opt/test-1.0-SNAPSHOT.jar flink /opt/result.txt
  *
  *  默认watermark = eventtime
  *    使用AssignerWithPeriodicWatermarks 之后，watermark延迟相应时间
  *
  *  water mark window触发时间
  *   1、watermark时间 >= window_end_time
  *   2、在[window_start_time,window_end_time)中有数据存在
  *   或者
  *   watermark > eventtime
  *
  */
object KafkaConsumerr {
    def main(args: Array[String]): Unit = {
        val logger = LoggerFactory.getLogger(getClass)
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.enableCheckpointing(35000) // 设置检查点
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

        val props = new Properties()
        props.setProperty("bootstrap.servers", "hadoop-5:6667")
        props.setProperty("zookeeper.connect","hadoop-7:2181")
        props.setProperty("group.id", "flink-group")
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

        import org.apache.flink.api.scala._
        val kafka = new FlinkKafkaConsumer08[String]("flink",new SimpleStringSchema(),props)
        kafka.assignTimestampsAndWatermarks(new MessageWaterEmitter())
        val keyedStream = env.addSource(kafka)
                            .flatMap(new MessageSpliter())
                            .keyBy(0)
                            .timeWindow(Time.seconds(10))
                            .reduce((x1,x2) => (x1._1,x1._2 + x2._2,x1._3 + "|" + x2._3))

        keyedStream.writeAsText("/output.txt",FileSystem.WriteMode.OVERWRITE)
        keyedStream.print()
        env.execute("flink-kafka")
    }

    class MessageWaterEmitter extends AssignerWithPunctuatedWatermarks[String]{
        val dateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss")
        val logger = LoggerFactory.getLogger(getClass)
        override def checkAndGetNextWatermark(t: String, l: Long): Watermark = {
            if(t != null && t.contains(",")){
                val elements = t.split(",")
                if(elements.length == 3){
                    logger.info("checkAndGetNextWatermark={},and time={},and l = {}", t, dateFormat.parse(elements(2)),dateFormat.format(new Date(l)))
                    return new Watermark(dateFormat.parse(elements(2)).getTime)
                }
            }
            return null
        }

        override def extractTimestamp(t: String, l: Long): Long = {
            if( t != null && t.contains(",")){
                val elements = t.split(",")
                if(elements.length == 3){
                    logger.info("extractTimestamp={},privious={},curr={}", t, dateFormat.format(new Date(l)), dateFormat.parse(elements(2)))
                    return dateFormat.parse(elements(2)).getTime
                }
            }
            0
        }
    }
    class MessageSpliter extends FlatMapFunction[String,Tuple3[String,Integer,String]]{
        override def flatMap(t: String, collector: Collector[(String, Integer, String)]): Unit = {
            if( t != null && t.contains(",")){
                val elements = t.split(",")
                if(elements.length == 3){
                    collector.collect(new Tuple3[String,Integer,String](elements(0),Integer.parseInt(elements(1)),elements(2)))
                }
            }
        }
    }
}
