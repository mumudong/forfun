package kafka.kafka_demo;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

public class TopicPartitionThread extends Thread{
    private static Logger logger = Logger.getLogger(TopicPartitionThread.class);
    private ExecutorService workerExecutorService;
    private Semaphore semaphore;
    private Map<TopicPartition, OffsetAndMetadata> offsetsMap = new HashMap<>();
    private List<Future<String>> taskList = new ArrayList<>();

    public TopicPartitionThread(ExecutorService workerExecutorService, Semaphore semaphore){
        this.workerExecutorService = workerExecutorService;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        //启动kafka消费
        Properties props = new Properties();
        props.put("bootstrap.servers", "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667");
        props.put("group.id", "test0001");
        props.put("enable.auto.commit", "false");
        props.put("session.timeout.ms", "30000");
        props.put("max.poll.records", 100); //每次poll最多获取100条数据
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,String>(props);
        System.out.println("========================");
        consumer.subscribe(Arrays.asList("stormtx" )
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

        //接收kafka消息
        while (Cache.getInstance().isKafkaThreadStatus()) {
            try {
                //使用100ms作为获取超时时间
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (final ConsumerRecord<String, String> record : records) {
                    semaphore.acquire();
                    //记录当前 TopicPartition和OffsetAndMetadata
                    TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                    OffsetAndMetadata offset = new OffsetAndMetadata(record.offset());
                    offsetsMap.put(topicPartition, offset);

                    //提交任务到线程池处理
                    taskList.add(workerExecutorService.submit(new WorkThread(record.topic(), record.value(), semaphore)));
                }

                //判断kafka消息是否处理完成
                for(Future<String> task : taskList){
                    //阻塞，直到消息处理完成
                    task.get();
                }

                //同步向kafka集群中提交offset
                consumer.commitSync();
            } catch (Exception e) {
                logger.error("TopicPartitionThread run error.", e);
            } finally{
                //清空taskList列表
                taskList.clear();
            }
        }

        //关闭comsumer连接
        consumer.close();
    }
}
