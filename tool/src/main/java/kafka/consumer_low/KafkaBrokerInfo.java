package kafka.consumer_low;

public class KafkaBrokerInfo {
    public final String brokerHost;  //主机名
    public final int brokerPort;     //端口

    public KafkaBrokerInfo(String brokerHost, int brokerPort) {
        this.brokerHost = brokerHost;
        this.brokerPort = brokerPort;
    }

    public KafkaBrokerInfo(String brokerHost) {
        this(brokerHost,9092);
    }
}
