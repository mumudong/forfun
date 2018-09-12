package trident.window;

import org.apache.hadoop.hbase.client.Durability;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.hbase.trident.mapper.SimpleTridentHBaseMapper;
import org.apache.storm.hbase.trident.mapper.TridentHBaseMapper;
import org.apache.storm.hbase.trident.state.HBaseState;
import org.apache.storm.hbase.trident.state.HBaseStateFactory;
import org.apache.storm.hbase.trident.state.HBaseUpdater;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.trident.OpaqueTridentKafkaSpout;
import org.apache.storm.kafka.trident.TridentKafkaConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.state.StateFactory;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.trident.windowing.config.SlidingCountWindow;
import org.apache.storm.trident.windowing.config.SlidingDurationWindow;
import org.apache.storm.trident.windowing.config.TumblingDurationWindow;
import org.apache.storm.trident.windowing.config.WindowConfig;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.HashMap;

/**
 *  HbaseState
 */
public class Main {
    public static void main(String[] args) throws Exception{
        TridentKafkaConfig tconfig = new TridentKafkaConfig(new ZkHosts("hadoop-5:2181"),"stormtx","stormtx-spout");
        tconfig.scheme = new SchemeAsMultiScheme(new StringScheme());

        OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(tconfig);

        TridentHBaseMapper hBaseMapper = new SimpleTridentHBaseMapper()
                .withColumnFamily("result")
                .withColumnFields(new Fields("word","count"))
                .withRowKeyField("rank");

        HBaseState.Options options = new HBaseState.Options()
                .withConfigKey("hbase")
                .withDurability(Durability.SYNC_WAL)
                .withMapper(hBaseMapper)
                .withTableName("top4Count");
        StateFactory stateFactory = new HBaseStateFactory(options);

        //SlidingWindow滑动窗口
        //TumblingWindow一个tuple只能在一个window
        //SlidingCountWindow按个数滑动窗口
        WindowConfig durationWindow = SlidingDurationWindow.of(BaseWindowedBolt.Duration.seconds(10),BaseWindowedBolt.Duration.seconds(5));

        TridentTopology topology = new TridentTopology();
        topology.newStream("fixedSpout",spout)
                .flatMap(new SplitFunction(),new Fields("word"))
                .parallelismHint(16)
                .window(durationWindow,new Fields("word"),new WordAggregator(),new Fields("wordcount"))
                .each(new Fields("wordcount"),new TopNFunction(4),new Fields("rank","word","count"))
                .partitionPersist(stateFactory,new Fields("rank","word","count"),new HBaseUpdater(),new Fields());

        Config conf = new Config();
        conf.put("hbase",new HashMap<String,Object>());
        if(args.length == 0){
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("Top4Topology",conf,topology.build());
        }else{
            conf.setNumWorkers(2);
            StormSubmitter.submitTopologyWithProgressBar(args[0],conf,topology.build());
        }
    }
}
