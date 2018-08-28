package trident.unstatetransaction;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.operation.builtin.Sum;
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
                .each(new Fields("field1"), function, new Fields())
                //这段代码将会对每个partition执行Count和Sum聚合器，并输出一个tuple（字段为 ["count", "sum"]）
                .chainedAgg()
                .partitionAggregate(new Count(),new Fields("count"))
                .partitionAggregate(new Sum(),new Fields("sum"))
                .chainEnd() ;




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