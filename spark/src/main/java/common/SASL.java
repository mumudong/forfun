package common;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class SASL {
    String[] users = new String[]{
            "4A4D769EB9679C054DE81B973ED5D768", "8dfeb5aaafc027d89349ac9a20b3930f",
            "011BBF43B89BFBF266C865DF0397AA71", "f2a8474bf7bd94f0aabbd4cdd2c06dcf",
            "068b746ed4620d25e26055a9f804385f", "97edfc08311c70143401745a03a50706",
            "d7f141563005d1b5d0d3dd30138f3f62", "c8ee90aade1671a21336c721512b817a",
            "6b67c8c700427dee7552f81f3228c927", "a95f22eabc4fd4b580c011a3161a9d9d"};

    Random random = new Random();

    int pointer = -1;

    String  getUserID()  {
        pointer = pointer + 1;
        if(pointer >= users.length) {
            pointer = 0;
            return users[pointer];
        } else {
            return users[pointer];
        }
    }
    Double click() {
       return random.nextInt(10)+0.0;
    }
    @Test
    public void saslProducer(){
        String fsPath="C:\\Users\\Administrator\\Desktop\\hadoop-conf\\";
        System.setProperty("java.security.auth.login.config", fsPath+"kafka_client_jaas.conf");
        System.setProperty("java.security.krb5.conf", fsPath+"krb5.conf");
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
        try{
            producer = new KafkaProducer<>(props);
            while(true){
                JSONObject event = new JSONObject();
                event.put("uid", getUserID());
                event.put("event_time", System.currentTimeMillis());
                event.put("os_type", "Android");
                event.put("click_count", click());
                producer.send(new ProducerRecord("test_user_events", event.toString()));
                System.out.println("Sent: " + event.toString());
                Thread.sleep(666);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
    @Test
    public void saslConsumer(){
        String fsPath="C:\\Users\\Administrator\\Desktop\\hadoop-conf\\";
        System.setProperty("java.security.auth.login.config", fsPath+"kafka_client_jaas.conf");
        System.setProperty("java.security.krb5.conf", fsPath+"krb5.conf");
        // 环境变量添加，需要输入配置文件的路径System.out.println("===================配置文件地址"+fsPath+"\\conf\\cons_client_jaas.conf");
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop-5:6667");
        props.put("group.id", "sasl-group");
        props.put("enable.auto.commit", "true");//自动提交offset  false则手动提交  consumer.commitSync
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        props.put("offsets.storage","kafka");
        props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "GSSAPI");
        KafkaConsumer<String,String> kafkaConsumer = new KafkaConsumer<String,String>(props);
        kafkaConsumer.subscribe(Arrays.asList("test_user_events"));
        while (true) {
            ConsumerRecords<String,String> records = kafkaConsumer.poll(100);
            for (ConsumerRecord record : records) {
                System.out.println("Partition: " + record.partition() + " Offset: " + record.offset() + " Value: " + record.value() + " ThreadID: " + Thread.currentThread().getId());
            }
        }
    }
}
