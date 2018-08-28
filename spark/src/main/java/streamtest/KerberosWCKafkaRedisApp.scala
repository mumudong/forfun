package streamtest

import java.lang

import com.alibaba.fastjson.JSON
import common.{JedisOffset, RedisClient}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
//import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

/**
  *
  * /usr/ndp/current/spark2_client/bin/spark-submit \
  * --files ./kafka_client_jaas.conf,./kafka.service.keytab \
  * --conf "spark.executor.extraJavaOptions=-Djava.security.auth.login.config=./kafka_client_jaas.conf" \
  * --driver-java-options "-Djava.security.auth.login.config=./kafka_client_jaas.conf" \
  * --conf spark.yarn.keytab=/etc/security/keytabs/hbase.service.keytab \
  * --conf spark.yarn.principal=hbase/hzadg-mammut-platform1.server.163.org@BDMS.163.COM \
  * --class com.netease.spark.streaming.hbase.JavaKafkaToHBaseKerberos \
  * --master yarn  \
  * --deploy-mode client \
  * ./target/spark-demo-0.1.0-jar-with-dependencies.jar
  *
  * 》《 。。。
  * run configuration中VM configuration必须-Djava.security.krb5.conf=C:/Users/Administrator/Desktop/hadoop-conf/krb5.conf
  */
object KerberosWCKafkaRedisApp {

//      Logger.getLogger("org").setLevel(Level.WARN)
    def main(args: Array[String]): Unit = {
        val conf = new SparkConf().setMaster("local[*]").setAppName("xx")
            .set("spark.streaming.kafka.maxRatePerPartition", "100")
            .set("spark.serilizer", "org.apache.spark.serializer.KryoSerializer")
            // 建议开启rdd的压缩
//            .set("spark.rdd.compress", "true")
        val ssc = new StreamingContext(conf, Seconds(2))

        //启动一参数设置
        val groupId = "stream-kerberos"
        val kafkaParams = Map[String, Object](
            "bootstrap.servers" -> "hadoop-5:6667",
            "key.deserializer" -> classOf[StringDeserializer],
            "value.deserializer" -> classOf[StringDeserializer],
            "group.id" -> groupId,
            "auto.offset.reset" -> "earliest",
            "enable.auto.commit" -> (false: lang.Boolean),
            "security.protocol" -> "SASL_PLAINTEXT",
             "sasl.mechanism" -> "GSSAPI"
            ,"sasl.kerberos.service.name" -> "kafka"
        )
        val fsPath = "C:\\Users\\Administrator\\Desktop\\hadoop-conf\\"
        System.setProperty("java.security.krb5.conf", fsPath + "krb5.conf")
        System.setProperty("java.security.auth.login.config", fsPath + "kafka_client_jaas.conf")
        System.setProperty("java.security.krb5.kdc", "hadoop-1:21732")
        System.setProperty("java.security.krb5.realm", "EXAMPLE.COM")

        val topics = Array("test_user_events")

        //启动二参数设置
        var formdbOffset: Map[TopicPartition, Long] = JedisOffset(groupId)
        val dbIndex = 1
        val clickHashKey = "app::users::click"
        //拉取kafka数据
        val stream: InputDStream[ConsumerRecord[String, String]] = if (formdbOffset.size == 0) {
            KafkaUtils.createDirectStream[String, String](
                ssc,
                LocationStrategies.PreferConsistent,//位置策略（可用的Executor上均匀分配分区）
                ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
            )
        } else {
            KafkaUtils.createDirectStream(
                ssc,
                LocationStrategies.PreferConsistent,
                ConsumerStrategies.Assign[String, String](formdbOffset.keys, kafkaParams, formdbOffset))
        }

        stream.foreachRDD({
            rdd =>
                //获得偏移量对象数组  必须在Dstream.foreachrdd使用，Dstream进行其他操作之后使用会改变类型
                val offsetRange: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
                val events = rdd.flatMap(line => {
                    val data = JSON.parseObject(line.value())
                    Some(data)
                })
                //逻辑处理
                val userClicks = events.map(x => (x.getString("uid"), x.getIntValue("click_count"))).reduceByKey(_ + _)

                userClicks.foreachPartition(partitionOfRecords => {
                                    partitionOfRecords.foreach(pair => {
                                        val uid = pair._1
                                        val clickCount = pair._2
                                        println(uid,"----",clickCount)
                                        val jedis = RedisClient.pool.getResource
                                        jedis.select(dbIndex)
                                        jedis.hincrBy(clickHashKey, uid, clickCount)
                                        RedisClient.pool.returnResource(jedis)
                                    })
                                })

                //偏移量存入redis
                val jedis: Jedis = RedisClient.pool.getResource
                for (or <- offsetRange) {
                    jedis.hset(groupId, or.topic + "-" + or.partition, or.untilOffset.toString)
                    println(groupId, or.topic + "-" + or.partition, or.untilOffset.toString)
                }
                RedisClient.pool.returnResource(jedis)

        })


        ssc.start()
        ssc.awaitTermination()
    }
}
