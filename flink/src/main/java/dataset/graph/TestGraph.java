package dataset.graph;

import org.apache.flink.runtime.io.network.partition.ResultPartitionType;
import org.apache.flink.runtime.jobgraph.DistributionPattern;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.JobVertex;
import org.apache.flink.runtime.jobmaster.JobResult;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.minicluster.TestingMiniClusterConfiguration;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestGraph {

    public static void testCoLocationConstraintJobExecution() throws Exception {
        final int numSlotsPerTaskExecutor = 1;
        final int numTaskExecutors = 1;
        final int parallelism = numTaskExecutors * numSlotsPerTaskExecutor;
        final JobGraph jobGraph = createNewJobGraph(parallelism);

        final TestingMiniClusterConfiguration miniClusterConfiguration = new TestingMiniClusterConfiguration.Builder()
                .setNumSlotsPerTaskManager(numSlotsPerTaskExecutor)
                .setNumTaskManagers(numTaskExecutors)
                .setLocalCommunication(true)
                .build();


        // 测试任务提交
        try (MiniCluster miniCluster = new MiniCluster(miniClusterConfiguration)) {
            miniCluster.start();
            miniCluster.submitJob(jobGraph).get();
            final CompletableFuture<JobResult> jobResultFuture = miniCluster.requestJobResult(jobGraph.getJobID());
            assertThat(jobResultFuture.get().isSuccess(), is(true));
        }
    }

    private static JobGraph createNewJobGraph(int parallelism) {
        //生成一个顶点
        final JobVertex sender = new JobVertex("Sender");
        sender.setParallelism(parallelism);
        //设置反射类
        sender.setInvokableClass(TestingInvokables.Sender.class);


        //生成一个顶点
        final JobVertex receiver = new JobVertex("Receiver");
        receiver.setParallelism(parallelism);
        receiver.setInvokableClass(TestingInvokables.Receiver.class);

        //生成一个边
        receiver.connectNewDataSetAsInput(sender, DistributionPattern.POINTWISE, ResultPartitionType.PIPELINED);

        //生成一个jobgraph图
        final JobGraph jobGraph = new JobGraph("my test graph", sender, receiver);

        return jobGraph;
    }

}
