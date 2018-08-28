package kafka;

import com.google.common.collect.Maps;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * 事务是0.11之后才有的，适用两种场景
 *      1：只有preoducer生产
 *      2：消费和生产并存，就是常说的 consumer-transform-producer 消费一个topic，结果生产到另一个topic
 */
public class TransactionKafka {
    public static void main(String[] args) {

    }
    /**
     * 在一个事务只有生产消息操作
     */
    public void onlyProduceInTransaction() {
        Producer producer = buildProducer();
        // 1.初始化事务
        producer.initTransactions();
        // 2.开启事务
        producer.beginTransaction();
        try {
            // 3.kafka写操作集合
            // 3.1 do业务逻辑
            // 3.2 发送消息
            producer.send(new ProducerRecord<String, String>("test", "transaction-data-1"));

            producer.send(new ProducerRecord<String, String>("test", "transaction-data-2"));
            // 3.3 do其他业务逻辑,还可以发送其他topic的消息。
            // 4.事务提交
            producer.commitTransaction();
        } catch (Exception e) {
            // 5.放弃事务
            producer.abortTransaction();
        }
    }
    /**
     * 在一个事务内,即有生产消息又有消费消息
     */
    public void consumeTransferProduce() {
        // 1.构建上产者
        Producer producer = buildProducer();
        // 2.初始化事务(生成productId),对于一个生产者,只能执行一次初始化事务操作
        producer.initTransactions();
        // 3.构建消费者和订阅主题
        Consumer consumer = buildConsumer();
        consumer.subscribe(Arrays.asList("test"));
        while (true) {
            // 4.开启事务
            producer.beginTransaction();
            // 5.1 接受消息
            ConsumerRecords<String, String> records = consumer.poll(500);
            try {
                // 5.2 do业务逻辑;
                System.out.println("customer Message---");
                Map<TopicPartition, OffsetAndMetadata> commits = Maps.newHashMap();
                for (ConsumerRecord<String, String> record : records) {
                    // 5.2.1 读取消息,并处理消息。print the offset,key and value for the consumer records.
                    System.out.printf("offset = %d, key = %s, value = %s\n",
                            record.offset(), record.key(), record.value());
                    // 5.2.2 记录提交的偏移量
                    commits.put(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset()));
                    // 6.生产新的消息。比如外卖订单状态的消息,如果订单成功,则需要发送跟商家结转消息或者派送员的提成消息
                    producer.send(new ProducerRecord<String, String>("test", "data2"));
                }
                // 7.提交偏移量
                producer.sendOffsetsToTransaction(commits, "group0323");
                // 8.事务提交
                producer.commitTransaction();
            } catch (Exception e) {
                // 7.放弃事务
                producer.abortTransaction();
            }
        }
    }
    private Producer buildProducer() {
        // create instance for properties to access producer configs
        Properties props = new Properties();
        // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
        props.put("bootstrap.servers", "localhost:9092");
        // 设置事务id
        props.put("transactional.id", "first-transactional");
        // 设置幂等性
        props.put("enable.idempotence",true);
        //Set acknowledgements for producer requests.
        props.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        props.put("retries", 1);
        //Specify buffer size in config,这里不进行设置这个属性,如果设置了,还需要执行producer.flush()来把缓存中消息发送出去
        //props.put("batch.size", 16384);
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

        return producer;
    }
    /**
     * 需要:
     * 1、关闭自动提交 enable.auto.commit
     * 2、isolation.level为
     * @return
     */
    public Consumer buildConsumer() {
        Properties props = new Properties();
        // bootstrap.servers是Kafka集群的IP地址。多个时,使用逗号隔开
        props.put("bootstrap.servers", "localhost:9092");
        // 消费者群组
        props.put("group.id", "group0323");
        // 设置隔离级别
        props.put("isolation.level","read_committed");
        // 关闭自动提交
        props.put("enable.auto.commit", "false");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", 100); //每次poll最多获取100条数据
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer
                <String, String>(props);
        return consumer;
    }
}
