package javalearn.jmxdemo.client;

import javalearn.jmxdemo.base.HelloMBean;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Client {
    public static void main(String[] args) throws Exception {
        JMXServiceURL url = new JMXServiceURL(
                "service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

        // 把所有Domain都打印出来
        printAllDomains(mbsc);

        // MBean的总数
        System.out.println("MBean count = " + mbsc.getMBeanCount());
        ObjectName mbeanName = new ObjectName("allen:name=HelloWorld");

        // 对name属性的操作（属性名的第一个字母要大写）
        mbsc.setAttribute(mbeanName, new Attribute("Name", "Allen"));// 设值
        System.out.println("Name = " + mbsc.getAttribute(mbeanName, "Name"));// 取值

        // 得到proxy代理后直接调用的方式
        proxyInvokeHelloMBean(mbsc, mbeanName);

        // 远程调用的方式
        remoteInvokeHelloMBean(mbsc, mbeanName);

        // 打印Hello mbean的信息
        printMBeanInfo(mbsc, mbeanName);

        // 得到所有的MBean的ObjectName
        printAllMBeanObject(mbsc);

        // 注销 //mbsc.unregisterMBean(mbeanName);
        // 关闭MBeanServer连接
        jmxc.close();

    }


    private static void proxyInvokeHelloMBean(MBeanServerConnection mbsc,
                                              ObjectName mbeanName) {
        HelloMBean proxy = MBeanServerInvocationHandler.newProxyInstance(mbsc,
                mbeanName, HelloMBean.class, false);

        // 调用Hello MBean的pprintHello( )
        proxy.sayHello();

        // 调用Hello MBean的pprintHello(String whoName)
        proxy.sayHello("Allen");

    }


    private static void remoteInvokeHelloMBean(MBeanServerConnection mbsc,
                                               ObjectName mbeanName) throws InstanceNotFoundException,
            MBeanException, ReflectionException, IOException {

        mbsc.invoke(mbeanName, "printHello", null, null);
        mbsc.invoke(mbeanName, "printHello", new Object[]
                {"kimmy"}, new String[]
                {String.class.getName()});
    }


    private static void printAllDomains(MBeanServerConnection mbsc)
            throws IOException {
        System.out.println("Domains:---------------");
        String domains[] = mbsc.getDomains();
        for (int i = 0; i < domains.length; i++) {
            System.out.println("\tDomain[" + i + "] = " + domains[i]);
        }
    }


    private static void printMBeanInfo(MBeanServerConnection mbsc,
                                       ObjectName mbeanName) throws InstanceNotFoundException,
            IntrospectionException, ReflectionException, IOException {
        System.out.println("Hello Mbean:---------------");
        MBeanInfo info = mbsc.getMBeanInfo(mbeanName);
        System.out.println("Hello Class: " + info.getClassName());
        System.out.println("Hello Attriber："
                + info.getAttributes()[0].getName());
        System.out.println("Hello Operation："
                + info.getOperations()[0].getName());
    }


    private static void printAllMBeanObject(MBeanServerConnection mbsc)
            throws IOException {
        System.out.println("all ObjectName：---------------");
        Set<ObjectInstance> set = mbsc.queryMBeans(null, null);
        for (Iterator<ObjectInstance> it = set.iterator(); it.hasNext(); ) {
            ObjectInstance oi = it.next();
            System.out.println("\t" + oi.getObjectName());
        }
    }
}
