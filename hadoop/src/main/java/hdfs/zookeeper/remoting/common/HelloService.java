package hdfs.zookeeper.remoting.common;
 
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
 
public interface HelloService extends Remote {
 
    String sayHello(String name) throws RemoteException;
}