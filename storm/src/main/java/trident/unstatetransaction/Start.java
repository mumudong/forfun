package trident.unstatetransaction;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.operation.builtin.Sum;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;

/**
 * Created by Administrator on 2018/7/2.
 */
public class Start {

    public static StormTopology buildTopology() {
        TridentTopology topology = new TridentTopology();
        WordSpout spout = new WordSpout();
        WordFunction function = new WordFunction();

        topology.newStream("filter", spout)
                /**
                 * 将spout发送的数据流中哪些字段传入bolt中
                 */
                .each(new Fields("field1","field2"), function, new Fields("field3","field4"))
//                .project(new Fields("field4"))//只保留这些列
                .groupBy(new Fields("field3"))
                //这段代码将会对每个partition执行Count和Sum聚合器，并输出一个tuple（字段为 ["count", "sum"]）
                .chainedAgg()//对每个批次的数聚合，不累计
                .aggregate(new Fields("field4"),new Count(),new Fields("count"))
                .aggregate(new Fields("field4"),new Sum(),new Fields("sum"))
                .chainEnd()
                .each(new Fields("field3","count","sum"),new Fun(),new Fields( ));




        return topology.build();
    }

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("MyStorm", conf, buildTopology());

        Thread.sleep(1000 * 60);
        cluster.shutdown();
    }
}
class Fun extends BaseFunction {
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        System.out.println("tuple---->" + tuple);
        collector.emit(tuple);
    }
}