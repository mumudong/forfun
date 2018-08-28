package common;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class ProducerKafka {
    Properties props = new Properties();
    KafkaProducer<String, String> producer = null;
    {
        props.put("bootstrap.servers", "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<String, String>(props);
    }
    public void produce(String str){
        producer.send(new ProducerRecord<String, String>("tx", str));
    }
    public void close(){
        producer.close();
    }
}
