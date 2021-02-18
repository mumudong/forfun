package javalearn.jmxdemo.mxbean;

public interface QueueSamplerMXBean {
    public QueueSample getQueueSample();
    public void clearQueue();
}
