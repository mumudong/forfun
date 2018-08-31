package kafka.consumer_low;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo {
    public static void main(String[] args) {
        long maxReads = 30000;
        String topic = "test";
        int partitionID = 0;

        KafkaTopicPartitionInfo topicPartitionInfo = new KafkaTopicPartitionInfo(topic, partitionID);
        List<KafkaBrokerInfo> brokers = new ArrayList<KafkaBrokerInfo>();
        brokers.add(new KafkaBrokerInfo("hadoop-5", 6667));
        JavaKafkaSimpleConsumerAPI example = new JavaKafkaSimpleConsumerAPI(brokers);

        // 获取该topic所属的所有分区ID列表
        List<Integer> ids = example.fetchTopicPartitionIDs(brokers, topic, 100000, 64 * 1024, "client-2");
        System.out.println(topic + "主题包含分区:" + ids);


//        ExecutorService executors = Executors.newFixedThreadPool(ids.size());
        try {
            example.run(maxReads, topicPartitionInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
