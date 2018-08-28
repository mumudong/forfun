package kafka.consumer_low;

public class KafkaTopicPartitionInfo {
    public final String topic;
    public final int partitionID;

    public KafkaTopicPartitionInfo(String topic, int partitionID) {
        this.topic = topic;
        this.partitionID = partitionID;
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + partitionID;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KafkaTopicPartitionInfo that = (KafkaTopicPartitionInfo) o;

        if (partitionID != that.partitionID) return false;
        return topic != null ? topic.equals(that.topic) : that.topic == null;
    }
}
