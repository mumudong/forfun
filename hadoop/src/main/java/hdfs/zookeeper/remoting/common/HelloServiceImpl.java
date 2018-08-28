package hdfs.zookeeper.remoting.common;

import hdfs.zookeeper.remoting.common.HelloService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
 
public class HelloServiceImpl extends UnicastRemoteObject implements HelloService {
    //UnicastRemoteObject用于导出带 JRMP 的远程对象和获得与该远程对象通信的 stub
    public HelloServiceImpl() throws RemoteException {
    }
 
    @Override
    public String sayHello(String name) throws RemoteException {
        return String.format("Hello %s", name);
    }
}