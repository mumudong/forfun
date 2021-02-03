package jmxdemo.mbeandesc;

@Author("Mr Bean")
@Version("1.0")
public interface QueueSamplerMXBean {
    @DisplayName("GETTER: QueueSample")
    public QueueSample getQueueSample();
    @DisplayName("OPERATION: clearQueue")
    public void clearQueue();
}