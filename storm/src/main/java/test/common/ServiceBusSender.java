package test.common;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.*;
import org.junit.Test;

/**
 * Created by Administrator on 2018/5/28.
 */
@SuppressWarnings("all")
public class ServiceBusSender {
    private static ServiceBusContract service;
    static {
        Configuration config =
                ServiceBusConfiguration.configureWithSASAuthentication(
                        "tianxibus",
                        "RootManageSharedAccessKey",
                        "OdajKieVKO7QgqS/IMBwtwdmkbU6lCagLQ5LeCZVq2o=",
                        ".servicebus.chinacloudapi.cn"
                );
        service = ServiceBusService.create(config);
    }
    @Test
    public  void createQueue(){
        long maxSizeInMegabytes = 5120;
//        QueueInfo queueInfo = new QueueInfo("test");
//        queueInfo.setMaxSizeInMegabytes(maxSizeInMegabytes);
        TopicInfo info = new TopicInfo("Topic2");
        info.setMaxSizeInMegabytes(5120l);
        try
        {
            CreateTopicResult contract = service.createTopic(info);

        }
        catch (Exception e)
        {
            System.out.print("ServiceException encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
    public void send(){
        QueueInfo queueInfo = new QueueInfo("Topic2");
        try {
            BrokeredMessage message = new BrokeredMessage("MyMessage");
            service.sendQueueMessage("TestQueue", message);
        } catch (Exception e) {
            System.out.print("ServiceException encountered: ");
            System.out.println(e.getMessage());
        }
    }
    public void receive(){
        try
        {
            ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
            opts.setReceiveMode(ReceiveMode.PEEK_LOCK);

            while(true)  {
                ReceiveQueueMessageResult resultQM =
                        service.receiveQueueMessage("TestQueue", opts);
                BrokeredMessage message = resultQM.getValue();
                if (message != null && message.getMessageId() != null)
                {
                    System.out.println("MessageID: " + message.getMessageId());
                    // Display the queue message.
                    System.out.print("From queue: ");
                    byte[] b = new byte[200];
                    String s = null;
                    int numRead = message.getBody().read(b);
                    while (-1 != numRead)
                    {
                        s = new String(b);
                        s = s.trim();
                        System.out.print(s);
                        numRead = message.getBody().read(b);
                    }
                    System.out.println();
                    System.out.println("Custom Property: " +
                            message.getProperty("MyProperty"));
                    // Remove message from queue.
                    System.out.println("Deleting this message.");
                    //service.deleteMessage(message);
                }
                else
                {
                    System.out.println("Finishing up - no more messages.");
                    break;
                    // Added to handle no more messages.
                    // Could instead wait for more messages to be added.
                }
            }
        }
        catch (ServiceException e) {
            System.out.print("ServiceException encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        catch (Exception e) {
            System.out.print("Generic exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
    public static void main(String[] args) {
        ServiceBusSender sender = new ServiceBusSender();
        sender.sendTopic();
        System.out.println("发送成功！");
    }
    public void sendTopic(){

        try {
            BrokeredMessage message = new BrokeredMessage("test antifraud");
            service.sendTopicMessage("Topic2", message);
        } catch (Exception e) {
            System.out.print("ServiceException encountered: ");
            System.out.println(e.getMessage());
        }
    }
    @Test
    public void receiveTopic(){
        try
        {
            ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
            opts.setReceiveMode(ReceiveMode.PEEK_LOCK);

            while(true)  {
                ReceiveSubscriptionMessageResult  resultSubMsg =
                        service.receiveSubscriptionMessage("antifraud", "AllMessages", opts);
                BrokeredMessage message = resultSubMsg.getValue();
                if (message != null && message.getMessageId() != null)
                {
                    System.out.println("MessageID: " + message.getMessageId());
                    // Display the topic message.
                    System.out.print("From topic: ");
                    byte[] b = new byte[200];
                    String s = null;
                    int numRead = message.getBody().read(b);
                    while (-1 != numRead)
                    {
                        s = new String(b);
                        s = s.trim();
                        System.out.print(s);
                        numRead = message.getBody().read(b);
                    }
                    System.out.println();
                    System.out.println("Custom Property: " +
                            message.getProperty("MessageNumber"));
                    // Delete message.
                    System.out.println("Deleting this message.");
                    service.deleteMessage(message);
                }
                else
                {
                    System.out.println("Finishing up - no more messages.");
                    break;
                    // Added to handle no more messages.
                    // Could instead wait for more messages to be added.
                }
            }
        }
        catch (ServiceException e) {
            System.out.print("ServiceException encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        catch (Exception e) {
            System.out.print("Generic exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
}
