package test.common;

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.PartitionReceiver;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;


public class EventhubReceiver{

    public static void main(String[] args)
            throws ServiceBusException, ExecutionException, InterruptedException, IOException {
        final String namespaceName = "tianxieventhubdemo.servicebus.chinacloudapi.cn/";
        final String eventHubName = "antifraud2";
        final String sasKeyName = "RootManageSharedAccessKey";
        final String sasKey = "DONU7hNTLBRnySuqQ0fJNz1jtnUAcNbMaMZ3BC9OKw4=";

        ConnectionStringBuilder connStr = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
        System.out.println("connStr" + connStr.getEndpoint());
        EventHubClient ehClient = EventHubClient.createFromConnectionString(connStr.toString()).get();
        // receiver
        String partitionId = "3"; // API to get PartitionIds will be released as part of V0.2
        PartitionReceiver receiver = ehClient.createEpochReceiver(
                EventHubClient.DEFAULT_CONSUMER_GROUP_NAME,
                partitionId,
                PartitionReceiver.START_OF_STREAM,
                false,
                1).get();
        try {
            Iterable<EventData> receivedEvents = receiver.receive(100).get();
            while (true) {
                int batchSize = 0;
                if (receivedEvents != null) {
                    for(EventData receivedEvent: receivedEvents) {
                        System.out.println(String.format("Message Payload----------> %s", new String(receivedEvent.getBody(), Charset.defaultCharset())));
                        /*System.out.println(String.format("Offset: %s, SeqNo: %s, EnqueueTime: %s",
                                receivedEvent.getSystemProperties().getOffset(),
                                receivedEvent.getSystemProperties().getSequenceNumber(),
                                receivedEvent.getSystemProperties().getEnqueuedTime()));*/
                        batchSize++;
                    }
                }
                System.out.println(String.format("ReceivedBatch Size: %s", batchSize));
                receivedEvents = receiver.receive(100).get();
            }
        }
        finally {
            // this is paramount; max number of concurrent receiver per consumergroup per partition is 5
            receiver.close().get();
        }
    }



    /**
     * actual application-payload, ex: a telemetry event
     */

    static final class PayloadEvent {

        PayloadEvent()	{}
        public String strProperty;
        public long longProperty;
        public int intProperty;
    }



}
