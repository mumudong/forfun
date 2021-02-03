package metrics;

import java.util.Map;

public class HadoopMetricConfigDemo {
    private static String prefix = "datanode";
    private static String SINK_KEY = "sink";
    public static void main(String[] args) {
        MyMetricsConfig metricsConfig = MyMetricsConfig.create(prefix);
        Map<String, MyMetricsConfig>  sinkConfigs = metricsConfig.getInstanceConfigs(SINK_KEY);
        for (Map.Entry<String, MyMetricsConfig> entry : sinkConfigs.entrySet()) {
            MyMetricsConfig conf = entry.getValue();
            String clsName = conf.getClassName("");
            if (clsName == null) continue;  // sink can be registered later on
            String sinkName = entry.getKey();
        }
    }
}
