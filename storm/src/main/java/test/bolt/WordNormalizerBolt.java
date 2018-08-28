package test.bolt;


import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

@SuppressWarnings("serial")
public class WordNormalizerBolt extends BaseRichBolt {
    private OutputCollector _collector;

    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    public void execute(Tuple input) {
    	String msg = input.getString(0);
        System.out.println("message----------->" + msg);



        _collector.emit(input,new Values(msg));
        _collector.ack(input);
    }

    public void cleanup() {
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("JsonMsg"));
    }
}