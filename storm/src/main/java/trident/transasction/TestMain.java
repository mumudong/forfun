package trident.transasction;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.metric.LoggingMetricsConsumer;
import org.apache.storm.transactional.TransactionalTopologyBuilder;

/**
 * Created by Administrator on 2018/5/30.
 */
public class TestMain {

    public static void main(String[] args) {

        TransactionalTopologyBuilder builder = new TransactionalTopologyBuilder(
                "ttbId", "spoutid", new TestTransactionSpout(), 1);
        builder.setBolt("bolt1", new TestTransBolt1(), 3).shuffleGrouping(
                "spoutid");
        builder.setBolt("committer", new TestTransBolt2(), 1).shuffleGrouping(
                "bolt1");

        Config conf = new Config();
        conf.setDebug(false);
//        conf.registerMetricsConsumer(LoggingMetricsConsumer.class);


        if (args.length > 0) {
            try {
                StormSubmitter.submitTopology(args[0], conf,
                        builder.buildTopology());
            } catch (AlreadyAliveException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LocalCluster localCluster = new LocalCluster();
            localCluster.submitTopology("mytopology", conf,
                    builder.buildTopology());
        }
    }

}
