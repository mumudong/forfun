package test.bolt;

import test.common.C3POUtil;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.windowing.TupleWindow;

import java.util.Map;

/**
 * Created by Administrator on 2018/5/25.
 */
public class EventhubCount extends BaseWindowedBolt{
    private static final long serialVersionUID = -2l;
    private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext context
                     , OutputCollector collector) {
        this.collector = collector;
     }
    @Override
    public void execute(TupleWindow inputWindow) {
        int sum = 0;
        for(Tuple tuple:inputWindow.get()){
            collector.ack(tuple);
            int i = tuple.getInteger(0);
            sum += i;

        }
        try {
            C3POUtil.insertUpdateDelete("update ConCount set count = ? where id = '1'",sum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("时间窗口内数据量---->" + sum);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        super.declareOutputFields(declarer);
    }
}
