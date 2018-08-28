package hdfs.zookeeper.remoting.client;

import hdfs.zookeeper.remoting.common.HelloService;

import java.rmi.Naming;
 
public class RmiClient {
 
    public static void main(String[] args) throws Exception {
        String url = "rmi://localhost:1099/demo.zookeeper.remoting.server.HelloServiceImpl";
        //  返回与指定 name 关联的远程对象的引用
        HelloService helloService = (HelloService) Naming.lookup(url);
        String result = helloService.sayHello("Jack");
        System.out.println(result);
    }
}