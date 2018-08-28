package test.common;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Random;

public class EventhubSender {

    public static void main(String[] args) throws Exception {
        final String namespaceName = "tianxieventhubdemo.servicebus.chinacloudapi.cn/";
        final String eventHubName = "antifraud2";
        final String sasKeyName = "RootManageSharedAccessKey";
        final String sasKey = "DONU7hNTLBRnySuqQ0fJNz1jtnUAcNbMaMZ3BC9OKw4=";

        ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);

//        int count =1;
//        while (true){
//            if(count==99) {
//                count = 1;
//            }
//            PayloadEvent payload = new PayloadEvent(count%100);
//            count++;
            String str = "2007-12-03,40974f8c-8b11-4d98-8eb4-d09e725e7506,63,27994,3,7194,1" ;
            String str2 = "2007-01-14,8368612a-48f2-4136-b493-3c13ed076be5,52,5532,2,28965,1";
            String str3 = "2007-10-19,96686392-b6e7-454b-aa74-5584d2b8f439,95,8623,3,875,0";
            String str4 = "2007-04-15,936e5870-929f-4e64-bf34-744e09c1f01c,42,27604,2,37848,0";
            EventData sendEvent = new EventData(str.getBytes(Charset.defaultCharset()));
            EventData sendEvent2 = new EventData(str2.getBytes(Charset.defaultCharset()));
            EventData sendEvent3 = new EventData(str3.getBytes(Charset.defaultCharset()));
            EventData sendEvent4 = new EventData(str4.getBytes(Charset.defaultCharset()));
            EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
            PartitionSender sender = ehClient.createPartitionSender("1").get();
            sender.send(sendEvent).get();
            sender.send(sendEvent2).get();
            sender.send(sendEvent3).get();
            sender.send(sendEvent4).get();
            System.out.println(Instant.now() + ": Send Complete...");
            sender.close();
            ehClient.close();

            /*System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s",
                    sendEvent.getSystemProperties().getOffset(),
                    sendEvent.getSystemProperties().getSequenceNumber(),
                    sendEvent.getSystemProperties().getEnqueuedTime()));*/
            //Thread.sleep(3000000);
//        }
    }



    /**
     * actual application-payload, ex: a telemetry event
     */

    static final class PayloadEvent {

        PayloadEvent(final int seed) {
            this.id = "telemetryEvent1-critical-eventid-" + seed;
            this.strProperty = "Sample payload"+
                    Long.toString(System.currentTimeMillis());
            this.longProperty = seed * new Random().nextInt(seed);
            this.intProperty = seed * new Random().nextInt(seed);
        }
        public String id;
        public String strProperty;
        public long longProperty;
        public int intProperty;

    }

}
