package hbase.coprocessor.endpoint;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/3.
 */
public class TestEndPoint {
    public static void main(String[] args) throws Exception,Throwable{
        System.out.println("begin-------->");
        long begin_time = System.currentTimeMillis();
        Configuration conf = HBaseConfiguration.create();
        String master_ip = args[0];
        String zk_ip = args[1];
        String table_name = args[2];
//        conf.set("hbase.zookeeper.property.clientPort","2181");
//        conf.set("hbase.zookeeper.quorum",zk_ip);
//        conf.set("hbase.master",master_ip+":600000");
        final EndpointTestProtos.CountRequest request = EndpointTestProtos.CountRequest.getDefaultInstance();
        HTable table = new HTable(conf,table_name);
        Map<byte[],Long> results = table.coprocessorService(EndpointTestProtos.CountService.class,null,null,
                                    new Batch.Call<EndpointTestProtos.CountService,Long>(){
                                        @Override
                                        public Long call(EndpointTestProtos.CountService countService) throws IOException {
                                            ServerRpcController controller = new ServerRpcController();
                                            BlockingRpcCallback<EndpointTestProtos.CountResponse> rpcCallback = new BlockingRpcCallback<EndpointTestProtos.CountResponse>();
                                            countService.count(controller,request,rpcCallback);
                                            EndpointTestProtos.CountResponse response = rpcCallback.get();
                                            if(controller.failedOnException()){
                                                throw controller.getFailedOn();
                                            }
                                            return (response != null && response.hasCount()) ? response.getCount() : 0;
                                        }
                                    });
        table.close();
        if(results.size() > 0){
            System.out.println("result-->" + results.values());
        }else{
            System.out.println("无结果!");
        }
        long end_time = System.currentTimeMillis();
        System.out.println("time spent-->" + (end_time-begin_time)/1000);
    }
}
