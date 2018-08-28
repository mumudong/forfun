package hdfs.zookeeper.remoting.client;


import hdfs.zookeeper.remoting.common.HelloService;

public class Client {
 
    public static void main(String[] args) throws Exception {
        ServiceConsumer consumer = new ServiceConsumer();
// zookeeper测试
        while (true) {
            HelloService helloService = consumer.lookup();
            String result = helloService.sayHello("Jack");
            System.out.println(result);
            Thread.sleep(3000);
        }
    }
}