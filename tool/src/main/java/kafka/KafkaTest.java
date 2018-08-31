package kafka;

import java.util.*;
import java.util.Map.Entry;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.consumer.SimpleConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaTest {

    public static void main(String[] args) {
        Properties props = new Properties();
        KafkaProducer<String, String> producer = null;
        props.put("bootstrap.servers", "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);//如果数据量没有达到batch size，会linger一段时间等待更多消息
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(props);
        for(int i = 0; i< 20;i++){
            produce(producer,"test......" + i);
        }
        close(producer);
//        System.out.println(Long.MAX_VALUE + 1);
//        System.out.println(Long.MAX_VALUE );
    }
    public static void produce(KafkaProducer producer,String str){
        producer.send(new ProducerRecord<String, String>("test", str));
    }
    public static void close(KafkaProducer producer){
        producer.close();
    }
}
