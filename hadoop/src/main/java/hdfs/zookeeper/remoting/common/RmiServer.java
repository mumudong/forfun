package hdfs.zookeeper.remoting.common;
 
import hdfs.zookeeper.remoting.common.HelloServiceImpl;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
 
public class RmiServer {
 
    public static void main(String[] args) throws Exception {
        int port = 1099;
        String url = "rmi://localhost:1099/demo.zookeeper.remoting.server.HelloServiceImpl";
        // 创建并导出接受指定 port 请求的本地主机上的 Registry 实例。
        //---- 在本地启动注册服务,或者也可以在其他机器启动作为注册服务器
        LocateRegistry.createRegistry(port);
        //将指定名称重新绑定到一个新的远程对象。
        //----- 注册服务对象到注册服务器
        Naming.rebind(url, new HelloServiceImpl());
    }
}