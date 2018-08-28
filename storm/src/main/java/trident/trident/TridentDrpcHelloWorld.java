package trident.trident;

import org.apache.storm.Config;
import org.apache.storm.utils.DRPCClient;

/**
 * Created by Administrator on 2018/6/28.
 */
public class TridentDrpcHelloWorld {
    public static void main(String[] args)throws Exception {
        Config conf = new Config();
        DRPCClient client = new DRPCClient(conf,"drpc.server.location", 3772);
        System.out.println(client.execute("words", "cat dog the man"));
    }
}
