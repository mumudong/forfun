package trident.trident;

import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.LocalDRPC;
import org.apache.storm.trident.TridentState;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.operation.builtin.Count;
import org.apache.storm.trident.operation.builtin.FilterNull;
import org.apache.storm.trident.operation.builtin.MapGet;
import org.apache.storm.trident.operation.builtin.Sum;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.trident.testing.MemoryMapState;
import org.apache.storm.trident.testing.Split;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.DRPCClient;

/**
 * Created by Administrator on 2018/6/28.
 */
public class TridentHelloWorld {
    public static void main(String[] args) throws Exception{
        Logger logger = Logger.getLogger(TridentHelloWorld.class);
        FixedBatchSpout spout = new FixedBatchSpout(new Fields("sentence"), 3,
                new Values("the cow jumped over the moon hahaha"),
                new Values("the man went to the store and bought some candy hehehe"),
                new Values("four score and seven years ago hehehe"),
                new Values("how many apples can you eat"));
        spout.setCycle(true);



        TridentTopology topology = new TridentTopology(); // 1
        //普通stream
        TridentState wordCounts =
                topology.newStream("spout1", spout) // 2
                        .each(new Fields("sentence"), new Split(), new Fields("word")) // 3
                        .groupBy(new Fields("word")) // 4
                        .persistentAggregate(new MemoryMapState.Factory(), new Count(), new Fields("count")) // 5
                        .parallelismHint(6);
        //drpcstream
        LocalDRPC drpc = new LocalDRPC();
        topology.newDRPCStream("words", drpc)
                .each(new Fields("args"), new Split(), new Fields("word"))
                .groupBy(new Fields("word")).
                stateQuery(wordCounts, new Fields("word"), new MapGet(), new Fields("count"))
                .each(new Fields("count"),new FilterNull())
                .each(new Fields("count"),new Fun(),new Fields("count3"))
                .aggregate(new Fields("count3"), new Sum(), new Fields("sum"));
        Config conf = new Config();
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("drpc-demo",conf,topology.build());




//        DRPCClient client = new DRPCClient(conf,"drpc.server.location", 3772);
        for(int i = 0;i<10;i++) {
            Thread.sleep(1000);
            System.out.println("result---->" + drpc.execute("words", "hahaha hehehe"));
        }
    }

}
class Fun extends BaseFunction{
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        System.out.println("tuple---->" + tuple.getStringByField("") + tuple.getValues());
        collector.emit(tuple);
    }
}