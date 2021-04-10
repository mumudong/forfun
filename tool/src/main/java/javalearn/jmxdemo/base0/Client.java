package javalearn.jmxdemo.base0;

import java.util.HashMap;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Client {

    public static void main(String[] args) throws Exception {

        HashMap<String, Object> prop = new HashMap<String, Object>();
        prop.put(JMXConnector.CREDENTIALS, "Hello");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
        JMXConnector conn = JMXConnectorFactory.connect(url, prop);
        MBeanServerConnection mbsc = conn.getMBeanServerConnection();
        ObjectName mbeanName = new ObjectName("com.jmx.demo9:name=Hello");
        HelloMBean hello = JMX.newMBeanProxy(mbsc, mbeanName,HelloMBean.class);
        hello.setName("World!");
        hello.sayHello();

    }
}
