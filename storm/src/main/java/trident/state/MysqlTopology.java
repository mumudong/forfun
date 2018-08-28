package trident.state;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.kafka.trident.OpaqueTridentKafkaSpout;
import org.apache.storm.kafka.trident.TridentKafkaConfig;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.CombinerAggregator;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

/**
 * Created by Administrator on 2018/6/29.
 */
public class MysqlTopology {
    public static void main(String[] args) {
        // zookeeper管理状态需要修改配置文件 transactional.zookeeper.servers
        TridentKafkaConfig tconfig = new TridentKafkaConfig(new ZkHosts("hadoop-5:2181"),"stormtx","stormtx-spout");
        tconfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(tconfig);
        // state持久化配置
        JDBCStateConfig config = new JDBCStateConfig();
        config.setDriver("org.postgresql.Driver");
        config.setUrl("jdbc:postgresql://10.167.222.124:5432/test2");
        config.setUsername("txbi");
        config.setPassword("txbipg");
        config.setBatchSize(10);
        config.setCacheSize(10);
//        config.setType(StateType.TRANSACTIONAL);
        config.setCols("tel");
        config.setTable("stormtxx");

        TridentTopology topology = new TridentTopology();
        topology.newStream("MysqlStateTest",spout)
                .each(new Fields("str"),new KeyValueFun(),new Fields("tel","times"))
                .partitionPersist(new MysqlStateFactory(config),new Fields("tel","times"),new MysqlUpdater());

        LocalCluster cluster = new LocalCluster();
        Config conf = new Config();
        cluster.submitTopology("Test",conf,topology.build());
    }
}

class KeyValueFun extends BaseFunction{
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        String record = tuple.getString(0);
        System.out.println(record);
        for(String s: record.split(" "))
            collector.emit(new Values(s,1));
    }
}