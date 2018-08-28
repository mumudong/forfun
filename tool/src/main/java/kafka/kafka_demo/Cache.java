package kafka.kafka_demo;

public class Cache {
    private static Cache instance = null;
    /**
     * kafka 拉取数据线程的状态， false：停止获取数据
     */
    private boolean kafkaThreadStatus = true;

    public static Cache getInstance(){
        if(instance == null){
            init();
        }
        return instance;
    }

    public synchronized static void init(){
        if(instance == null){
            instance = new Cache();
        }
    }

    public boolean isKafkaThreadStatus() {
        System.out.println("---------------------");
        return kafkaThreadStatus;
    }
    public void setKafkaThreadStatus(boolean kafkaThreadStatus) {
        this.kafkaThreadStatus = kafkaThreadStatus;
    }

}