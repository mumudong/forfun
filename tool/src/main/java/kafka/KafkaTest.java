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
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

public class KafkaTest {

    public static void main(String[] args) throws Exception{
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
        for(int i = 0; i< 10;i++){
            produce(producer,"a f y y z z a f f e e e h h h h e e y y f f f f");
            Thread.sleep(2000l);
        }
        close(producer);
//        System.out.println(Long.MAX_VALUE + 1);
//        System.out.println(Long.MAX_VALUE );
    }
    public static void produce(KafkaProducer producer,String str){
        producer.send(new ProducerRecord<String, String>("stormtx", str));
    }
    public static void close(KafkaProducer producer){
        producer.close();
    }
    public static void testConsumer(){
        //启动kafka消费
        Properties props = new Properties();
        props.put("bootstrap.servers", "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667");
        props.put("group.id", "monitor_real");
        props.put("enable.auto.commit", "false");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", 100); //每次poll最多获取100条数据
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,String>(props);
        System.out.println("========================");
        String monitorTopic = "xxxx";
        consumer.subscribe(Arrays.asList(monitorTopic)
//                ,new ConsumerRebalanceListener(){
//                    @Override
//                    public void onPartitionsRevoked(//rebalance之前
//                            Collection<TopicPartition> partitions) {
//                        logger.info(String.format("threadId = %d, onPartitionsRevoked.",Thread.currentThread().getId()));
//                        consumer.commitSync(offsetsMap);
//                    }
//                    @Override
//                    public void onPartitionsAssigned(//rebalance之后
//                            Collection<TopicPartition> partitions) {
//                        logger.info(String.format("threadId = %d, onPartitionsAssigned.", Thread.currentThread().getId() ));
//                        offsetsMap.clear();
//                        //清空taskList列表
//                        taskList.clear();
//                        for (TopicPartition partition : partitions) {
//                            System.out.println("*- partition:"+partition.partition());
//                            //获取消费偏移量，实现原理是向协调者发送获取请求
//                            OffsetAndMetadata offset = consumer.committed(partition);
//                            //设置本地拉取分量，下次拉取消息以这个偏移量为准
//                            consumer.seek(partition, offset.offset());
//                        }
//                    }}
        );
        List<PartitionInfo> partitionInfos = consumer.partitionsFor(monitorTopic);
        for(PartitionInfo partitionInfo:partitionInfos){

        }
//        consumer.endOffsets(partitionInfos);
    }
}
