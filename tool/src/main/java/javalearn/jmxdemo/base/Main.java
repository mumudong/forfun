package javalearn.jmxdemo.base;


import java.lang.management.*;
import java.rmi.registry.LocateRegistry;
import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

public class Main {
    /* For simplicity, we declare "throws Exception".  Real programs
       will usually want finer-grained exception handling.  */
    public static void main(String[] args) throws Exception {
        LocateRegistry.createRegistry(9999);
        // Get the Platform MBean Server
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        // Construct the ObjectName for the MBean we will register
        ObjectName name = new ObjectName("javalearn.jmxdemo.base:type=Hello");

        // Create the Hello World MBean
        Hello mbean = new Hello();



        JMXConnectorServer cserver = JMXConnectorServerFactory
                .newJMXConnectorServer(new JMXServiceURL(
                                "service:jmx:rmi:///jndi/rmi://localhost:9999/server"),
                        null, mbs);
        cserver.start();
        // Register the Hello World MBean
        mbs.registerMBean(mbean, name);
        // Wait forever
        System.out.println("Waiting forever...");
        Thread.sleep(Long.MAX_VALUE);
    }
}
