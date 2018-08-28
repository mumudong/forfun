package test;


import test.bolt.EventhubBoltTwoWindow;
import test.bolt.EventhubCount;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.eventhubs.spout.EventHubSpout;
import org.apache.storm.eventhubs.spout.EventHubSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;

import java.util.concurrent.TimeUnit;


/**
 *
 * 调优点1：设置worker数量
 * Worker是运行在工作节点上面，被Supervisor守护进程创建的用来干活的进程。
 * 每个Worker对应于一个给定topology的全部执行任务的一个子集。
 * 反过来说，一个Worker里面不会运行属于不同的topology的执行任务。
 * 数目至少应该大于machines的数目
 *
 * 调优点2：给指定component创建的executor数量。通过setSpout/setBolt的参数来设置。
 * Executor可以理解成一个Worker进程中的工作线程。
 * 一个Executor中只能运行隶属于同一个component（spout/bolt）的task。
 * 一个Worker进程中可以有一个或多个Executor线程。在默认情况下，一个Executor运行一个task。
 *
 * 调优点3：给指定 component 创建的task数量。通过调用setNumTasks()方法来设置。
 * Task则是spout和bolt中具体要干的活了。
 * 一个Executor可以负责1个或多个task。
 * 每个component（spout/bolt）的并发度就是这个component对应的task数量。
 * 同时，task也是各个节点之间进行grouping（partition）的单位。
 * 默认和executor1:1
 */
@SuppressWarnings("all")
public class CountEventhubTopology {
    public static void main(String[] args) throws InterruptedException {
    	System.out.println("antifraud EventhubTopology main start!");

        EventHubSpoutConfig spoutConfig = new EventHubSpoutConfig("RootManageSharedAccessKey",
                "DONU7hNTLBRnySuqQ0fJNz1jtnUAcNbMaMZ3BC9OKw4=",
                "tianxieventhubdemo.servicebus.chinacloudapi.cn/",
                "antifraud2",
                4,
//                "hadoop-5:2181,hadoop-6:2181"
                "zk1-tianxi.vymdbanfbsrujcdydkzzsswlcg.ax.internal.chinacloudapp.cn:2181,zk4-tianxi.vymdbanfbsrujcdydkzzsswlcg.ax.internal.chinacloudapp.cn:2181"
                );
	    TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("RandomSentence", new EventHubSpout(spoutConfig), 1);
        builder.setBolt("antifraud", new EventhubBoltTwoWindow(), 2)
                            .shuffleGrouping("RandomSentence");//.setNumTasks(10);
        builder.setBolt("count",new EventhubCount().withWindow(new BaseWindowedBolt.Duration(2, TimeUnit.SECONDS),
                                                                   new BaseWindowedBolt.Duration(2,TimeUnit.SECONDS)),
                                                1).shuffleGrouping("antifraud");


        Config config = new Config();
        config.setDebug(false);
        config.setMessageTimeoutSecs(180);
        config.setNumAckers(2);
        if (args != null && args.length > 0) {
            config.setNumWorkers(14);
        	config.put(Config.NIMBUS_HOST, args[0]);
        	try {
	            StormSubmitter.submitTopology("antifraudTopology-1", config, builder.createTopology());
	        	System.out.println("submitTopology success!");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
        } else {
            config.setMaxTaskParallelism(1);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("ntifraudTopology-1", config, builder.createTopology());
        }

    	System.out.println("EventhubTopology main end!");
    }
}
//storm jar test-0.1-jar-with-dependencies.jar com.test.EventhubTopology hn1-tianxi.vymdbanfbsrujcdydkzzsswlcg.ax.internal.chinacloudapp.cn