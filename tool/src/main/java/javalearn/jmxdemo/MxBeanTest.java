package javalearn.jmxdemo;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * 在MXBean中，如果一个MXBean的接口定义了一个属性是一个自定义类型，如果MXBean定义了一种自定义的类型，
 * 当JMX使用这个MXBean时，这个自定义类型就会被转换成一种标准的类型，这些类型被称为开放类型，是定义在javax.management.openmbean包中的。
 *
 * 而这个转换的规则是，如果是原生类型，如int或者是String，则不会有变化，但如果是其他自定义类型，则被转换成CompositeDataSupport类,这样,
 * JMX调用这个MXBean提供的接口的时候,classpath下没有这个自定义类型也是可以调用成功的,但是换做MBean,则调用发的classpath下必须存在这个自定义类型的类定义
 */
public class MxBeanTest {
    public static void main(String[] args) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] allThreadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfo = threadMXBean.getThreadInfo(allThreadIds);
        for(ThreadInfo info:threadInfo){
            System.out.println(info.getThreadId() + ": " + info.getThreadName());
        }


    }
}
