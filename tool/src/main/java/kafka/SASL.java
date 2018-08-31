package kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

public class SASL {
    public static void main(String[] args) {
        SASL sasl = new SASL();
        sasl.saslProducer();
        System.out.println("================================");
        sasl.saslConsumer();
    }
    @Test
    public void saslProducer(){
        String fsPath="C:\\Users\\Administrator\\Desktop\\hadoop-conf\\";
        System.setProperty("java.security.krb5.conf", fsPath+"krb5.conf");
        System.setProperty("java.security.auth.login.config", fsPath+"kafka_client_jaas.conf");
        // 环境变量添加，需要输入配置文件的路径 System.out.println("===================配置文件地址"+fsPath+"\\conf\\prod_client_jaas.conf");
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop-5:6667,hadoop-6:6667");
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty ("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.kerberos.service.name","kafka");
        props.setProperty ("sasl.mechanism", "GSSAPI");/**不启用kerberos时的验证方式PLAIN,启用kerberos的验证方式 GSSAPI*/
        Producer producer = null;
        try {
            producer = new KafkaProducer<>(props);
            for (int i = 0; i < 2; i++) {
                String msg = "Message " + i;
                producer.send(new ProducerRecord("test", msg));
                System.out.println("Sent:" + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
    @Test
    public void saslConsumer(){
//        String fsPath="C:\\Users\\Administrator\\Desktop\\hadoop-conf\\";
//        System.setProperty("java.security.krb5.conf", fsPath+"krb5.conf");
//        System.setProperty("java.security.auth.login.config", fsPath+"kafka_client_jaas.conf");
        // 环境变量添加，需要输入配置文件的路径System.out.println("===================配置文件地址"+fsPath+"\\conf\\cons_client_jaas.conf");
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop-5:6667");
        props.put("group.id", "sasl-group2");
        props.put("enable.auto.commit", "true");//自动提交offset  false则手动提交  consumer.commitSync
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("offsets.storage","kafka");
//        props.setProperty ("security.protocol", "SASL_PLAINTEXT");
//        props.setProperty ("sasl.mechanism", "GSSAPI");
        KafkaConsumer<String,String> kafkaConsumer = new KafkaConsumer<String,String>(props);
        kafkaConsumer.subscribe(Arrays.asList("test"));
        while (true) {
            ConsumerRecords<String,String> records = kafkaConsumer.poll(100);
            for (ConsumerRecord record : records) {
                System.out.println("Partition: " + record.partition() + " Offset: " + record.offset() + " Value: " + record.value() + " ThreadID: " + Thread.currentThread().getId());
            }
        }
    }
}
