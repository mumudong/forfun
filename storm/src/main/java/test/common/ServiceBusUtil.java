package test.common;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;

import java.util.LinkedList;

/**
 * Created by Administrator on 2018/5/28.
 */
public class ServiceBusUtil {
    private static LinkedList<ServiceBusContract> services = null;
//    private ServiceBusContract service = null;
     public static void init() {
        System.out.println("实例化ServiceBusUtil----->");
         services = new LinkedList<ServiceBusContract>();
         for (int i = 0;i<2;i++) {
             Configuration config =
                     ServiceBusConfiguration.configureWithSASAuthentication(
                             "tianxibus",
                             "RootManageSharedAccessKey",
                             "OdajKieVKO7QgqS/IMBwtwdmkbU6lCagLQ5LeCZVq2o=",
                             ".servicebus.chinacloudapi.cn"
                     );

             services.add(ServiceBusService.create(config));
         }
    }

//    public void sendQuene(String mess){
//        QueueInfo queueInfo = new QueueInfo("test");
//        try {
//            BrokeredMessage message = new BrokeredMessage("MyMessage");
//            service.sendQueueMessage("TestQueue", message);
//        } catch (Exception e) {
//            System.out.print("ServiceException encountered: ");
//            System.out.println(e.getMessage());
//        }
//    }
    private  static ServiceBusContract getService(){
         if(services.size() > 0){
             synchronized (ServiceBusUtil.class){
                 if(services.size() > 0){
                     return services.removeFirst();
                 }
             }
         }
         return null;
    }
    public static void sendTopic(String topic,String mess){
        ServiceBusContract service = null;
        try {
            while((service = getService()) == null){

            }
            BrokeredMessage message = new BrokeredMessage(mess);
            service.sendTopicMessage(topic, message);
            System.out.println("发送成功--------------》");
        } catch (Exception e) {
            System.out.print("自定义 ServiceException encountered: ");
            System.out.println(e.getMessage());
        }finally {
            services.add(service);
        }
    }
//    public  void receiveQuene(){
//        try
//        {
//            ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
//            opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
//
//            while(true)  {
//                ReceiveQueueMessageResult resultQM =
//                        service.receiveQueueMessage("TestQueue", opts);
//                BrokeredMessage message = resultQM.getValue();
//                if (message != null && message.getMessageId() != null)
//                {
//                    System.out.println("MessageID: " + message.getMessageId());
//                    // Display the queue message.
//                    System.out.print("From queue: ");
//                    byte[] b = new byte[200];
//                    String s = null;
//                    int numRead = message.getBody().read(b);
//                    while (-1 != numRead)
//                    {
//                        s = new String(b);
//                        s = s.trim();
//                        System.out.print(s);
//                        numRead = message.getBody().read(b);
//                    }
//                    System.out.println();
//                    System.out.println("Custom Property: " +
//                            message.getProperty("MyProperty"));
//                    // Remove message from queue.
//                    System.out.println("Deleting this message.");
//                    //service.deleteMessage(message);
//                }
//                else
//                {
//                    System.out.println("Finishing up - no more messages.");
//                    break;
//                    // Added to handle no more messages.
//                    // Could instead wait for more messages to be added.
//                }
//            }
//        }
//        catch (ServiceException e) {
//            System.out.print("ServiceException encountered: ");
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//        catch (Exception e) {
//            System.out.print("Generic exception encountered: ");
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }
//    public static  void receiveTopic(){
//        try
//        {
//            ServiceBusContract service ;
//            while((service = getService()) == null){
//
//            }
//            ReceiveMessageOptions opts = ReceiveMessageOptions.DEFAULT;
//            opts.setReceiveMode(ReceiveMode.PEEK_LOCK);
//
//            while(true)  {
//                ReceiveSubscriptionMessageResult  resultSubMsg =
//                        service.receiveSubscriptionMessage("antifraud", "AllMessages", opts);
//                BrokeredMessage message = resultSubMsg.getValue();
//                if (message != null && message.getMessageId() != null)
//                {
//                    System.out.println("MessageID: " + message.getMessageId());
//                    // Display the topic message.
//                    System.out.print("From topic: ");
//                    byte[] b = new byte[200];
//                    String s = null;
//                    int numRead = message.getBody().read(b);
//                    while (-1 != numRead)
//                    {
//                        s = new String(b);
//                        s = s.trim();
//                        System.out.print(s);
//                        numRead = message.getBody().read(b);
//                    }
//                    System.out.println();
//                    System.out.println("Custom Property: " +
//                            message.getProperty("MessageNumber"));
//                    // Delete message.
////                    System.out.println("Deleting this message.");
////                    service.deleteMessage(message);
//                }
//                else
//                {
//                    System.out.println("Finishing up - no more messages.");
//                    break;
//                    // Added to handle no more messages.
//                    // Could instead wait for more messages to be added.
//                }
//            }
//        }
//        catch (ServiceException e) {
//            System.out.print("ServiceException encountered: ");
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//        catch (Exception e) {
//            System.out.print("Generic exception encountered: ");
//            System.out.println(e.getMessage());
//            System.exit(-1);
//        }
//    }


}
