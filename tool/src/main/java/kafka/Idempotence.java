package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

/**
 *  0.11.0之后支持幂等性和事务，此处测试幂等性
 */
public class Idempotence {
    public static void main(String[] args) {

    }
    private Producer buildIdempotProducer(){
        // create instance for properties to access producer configs
        Properties props = new Properties();
        // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
        props.put("bootstrap.servers", "localhost:9092");
        // 支持幂等，此时ack自动为all
        props.put("enable.idempotence",true);
        //If the request fails, the producer can automatically retry,
        props.put("retries", 3);
        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);
        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("buffer.memory", 33554432);

        // Kafka消息是以键值对的形式发送,需要设置key和value类型序列化器
        props.put("key.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        props.put("value.serializer",
                "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        //发送消息和正常一样
//        producer.send(new ProducerRecord<String, String>("topic", "message"));
//        producer.flush();
        return producer;
    }
}
