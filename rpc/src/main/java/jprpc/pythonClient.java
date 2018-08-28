package jprpc;

import example.DataOuterClass;
import example.FormatDataGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;


public class pythonClient {

    private final ManagedChannel channel;
    private final FormatDataGrpc.FormatDataBlockingStub blockingStub;


    public pythonClient(String host, int port){
        channel = ManagedChannelBuilder.forAddress(host,port)
                .usePlaintext(true)
                .build();

        blockingStub = FormatDataGrpc.newBlockingStub(channel);
    }


    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public  void greet(String name){
        DataOuterClass.Data request = DataOuterClass.Data.newBuilder().setText(name).build();
        DataOuterClass.Data response = blockingStub.doFormat(request);
        System.out.println(response.getText());

    }

    public static void main(String[] args) throws InterruptedException {
        pythonClient client = new pythonClient("127.0.0.1",8080);
        for(int i=0;i<5;i++){
            client.greet("world:"+i);
        }


    }
}
