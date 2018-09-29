package mumu.kafkastream

import java.lang
import java.util.{Properties, Scanner}

import org.apache.commons.lang3.StringUtils
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.junit.Test

/**
  * eee,1,20180504-113411
    eee,2,20180504-113415
    eee,2,20180504-113412
    eee,2,20180504-113419
    eee,1,20180504-113421
  */
object KafkaProducerr {

    def main(args: Array[String]): Unit ={
        val props = new Properties()
        props.put("bootstrap.servers", "hadoop-5:6667")
        props.put("acks", "all")
        props.put("retries", Integer.valueOf(0))
        props.put("batch.size", Integer.valueOf(16384))
        props.put("linger.ms", lang.Long.valueOf(1))
        props.put("buffer.memory", lang.Long.valueOf(33554432))
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

        val producer = new KafkaProducer[String, String](props)
        val scanner = new Scanner(System.in)
        var str:String = ""

        while (scanner.hasNext()) {
            str = scanner.nextLine()
            if(!StringUtils.isBlank(str)) {
                producer.send(new ProducerRecord[String,String]("flink", str), new Callback() {
                    override def onCompletion(metadata:RecordMetadata , exception:Exception ):Unit = {
                        if (exception != null) {
                            println("Failed to send message with exception " + exception)
                        }else{
                            println("record -> " + str)
                        }
                    }
                })
            }
        }
        producer.close()
    }

}
