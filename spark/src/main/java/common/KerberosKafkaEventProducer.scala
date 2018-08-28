package common

import java.util.Properties

import com.alibaba.fastjson.JSONObject
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.PartitionInfo
import org.junit.Test

import scala.util.Random

/**
  * Created by Administrator on 2018/4/13.
  */
class KerberosKafkaEventProducer{
    private val users = Array(
        "4A4D769EB9679C054DE81B973ED5D768", "8dfeb5aaafc027d89349ac9a20b3930f",
        "011BBF43B89BFBF266C865DF0397AA71", "f2a8474bf7bd94f0aabbd4cdd2c06dcf",
        "068b746ed4620d25e26055a9f804385f", "97edfc08311c70143401745a03a50706",
        "d7f141563005d1b5d0d3dd30138f3f62", "c8ee90aade1671a21336c721512b817a",
        "6b67c8c700427dee7552f81f3228c927", "a95f22eabc4fd4b580c011a3161a9d9d")

    private val random = new Random()

    private var pointer = -1

    def getUserID() : String = {
        pointer = pointer + 1
        if(pointer >= users.length) {
            pointer = 0
            users(pointer)
        } else {
            users(pointer)
        }
    }
    def click() : Double = {
        random.nextInt(10)
    }
    @Test
    def produce(): Unit ={
        val fsPath = "C:\\Users\\Administrator\\Desktop\\hadoop-conf\\"
        System.setProperty("java.security.krb5.conf", fsPath + "krb5.conf")
        System.setProperty("java.security.auth.login.config", fsPath + "kafka_client_jaas.conf")

        val topic = "test_user_events"
        val brokers = "hadoop-5:6667"
        val props = new Properties()
//        props.put("metadata.broker.list", brokers)
        props.put("bootstrap.servers", brokers)
        props.put("serializer.class", "kafka.serializer.StringEncoder")
        props.put("security.protocol", "SASL_PLAINTEXT")
        props.put("sasl.mechanism", "GSSAPI")

        val kafkaConfig = new ProducerConfig(props)
        val producer = new Producer[String, String](kafkaConfig)
        while(true) {
            // prepare event data
            val event = new JSONObject()
            event.put("uid", getUserID)
            event.put("event_time", System.currentTimeMillis.toString)
            event.put("os_type", "Android")
            event.put("click_count", click)

            // produce event message
            producer.send(new KeyedMessage[String, String](topic, event.toString))
            println("Message sent: " + event)
            Thread.sleep(200)
        }
    }
    @Test
    def kafkaProduce():Unit={
        val props = new Properties()
        props.put("bootstrap.servers", "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667")
        props.put("acks", "all")
        props.put("retries", "1")
        props.put("batch.size", "2000")
        props.put("linger.ms", "1")
        props.put("buffer.memory", "33554432")
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        //生产者发送消息
        val topic = "test_user_events"
        val procuder = new KafkaProducer[String, String](props)
        for( i <- 1 to 10){
            val event = new JSONObject()
            event.put("uid", getUserID)
            event.put("event_time", System.currentTimeMillis.toString)
            event.put("os_type", "Android")
            event.put("click_count", click)
            val msg = new ProducerRecord[String,String](topic,event.toString)
            procuder.send(msg)
        }

        //列出topic信息
        import scala.collection.JavaConverters._
        val partitions:List[PartitionInfo] = procuder.partitionsFor(topic).asScala.toList
        for(p <- partitions){
            println(p)
        }
        println("---发送结束")
        Thread.sleep(10000)
        procuder.close()
    }
    @Test
    def kafkaConsumer(): Unit ={
        val props = new Properties()
        props.put("bootstrap.servers", "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667");
        props.put("group.id", "test003")
        props.put("enable.auto.commit", "true")//自动提交offset  false则手动提交  consumer.commitSync
        props.put("auto.commit.interval.ms", "1000")
        props.put("session.timeout.ms", "30000")
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.put("auto.offset.reset", "earliest")
        props.put("offsets.storage","kafka")
        val consumer = new KafkaConsumer[String,String](props)
        import scala.collection.JavaConversions._
        consumer.subscribe(List[String]("test_user_events"))
        while (true) {
            var i = 0
            val records = consumer.poll(100)
            for (record:ConsumerRecord[String, String] <- records) {
                i = i + 1
                println(i + " " + record.key() + "---->" + record.value())
//                consumer.seekToEnd(consumer.assignment())
            }
        }
        println("消费组---->" + consumer.assignment().size())
        consumer.seekToEnd(consumer.assignment())
    }
}

