package trident.transasction;

import org.apache.storm.metric.api.IMetric;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.transactional.ITransactionalSpout;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/5/30.
 */
public class TestTransactionSpout implements ITransactionalSpout<TestMetaData> {

    private static final long serialVersionUID = 1L;
    private Map<Long, String> DATA_BASE = null;
    int i = 0;
    public TestTransactionSpout(){
        DATA_BASE = new HashMap<Long, String>();

        for (long i=0;i<50;i++){
            DATA_BASE.put(i, "TestTransactionSpout:"+i);
        }

        System.err.println("TestTransactionSpout start");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tx","count"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public Coordinator<TestMetaData> getCoordinator(
            Map conf, TopologyContext context) {
//        context.registerMetric("testMetrics", new IMetric() {
//            int i = 0;
//            @Override
//            public Object getValueAndReset() {
//                System.out.println("testMetrics执行------>");
//                return "getValueAndReset --> " + i++;
//            }
//        },2);
        System.err.println("TestTransactionSpout getCoordinator start");
        return new TestCoordinator();
    }

    @Override
    public Emitter<TestMetaData> getEmitter(
            Map conf, TopologyContext context) {
        System.err.println("TestTransactionSpout getEmitter start");
        return new TestEmitter(DATA_BASE);
    }
}