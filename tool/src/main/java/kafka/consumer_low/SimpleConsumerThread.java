package kafka.consumer_low;

import java.util.List;

/**
 * Created by Administrator on 2018/8/31.
 */
public class SimpleConsumerThread implements Runnable{
    long maxReads = 300;
    KafkaTopicPartitionInfo topicPartitionInfo;
    List<KafkaBrokerInfo> brokers;
    JavaKafkaSimpleConsumerAPI example;

    public SimpleConsumerThread(long maxReads, KafkaTopicPartitionInfo topicPartitionInfo, List<KafkaBrokerInfo> brokers, JavaKafkaSimpleConsumerAPI example) {
        this.maxReads = maxReads;
        this.topicPartitionInfo = topicPartitionInfo;
        this.brokers = brokers;
        this.example = example;
    }

    @Override
    public void run() {
        try {
            example.run(maxReads, topicPartitionInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
