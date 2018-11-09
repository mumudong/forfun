package socket;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 11111;
    private static final int SLEEP_TIME = 4000;
    private static volatile boolean flag = true;

    public static void main(String[] args) {
        final Map<String,Socket> map = new ConcurrentHashMap<>();
        try {
            System.out.println("客户端启动成功...");
            map.put("client",new Socket(HOST,PORT));
            new Thread(()->{
                while(flag) {
                    try {
                        System.out.println("发送心跳");
                        /**
                        * win7平台发送17次会报错，改用outstream.write方法,用reetrantlock控制并发
                        * */
                        map.get("client").sendUrgentData(0xFF);
                        Thread.sleep(1000);
                    } catch (Exception e) {//断线就设置标记位false
                        flag = false;
                        System.out.println("服务断开了.....");
                        e.printStackTrace();
                        while (!flag) {
                            try {
                                map.put("client", new Socket(HOST, PORT));
                                map.get("client").setKeepAlive(true);
                                sleep();
                                flag = true;
                            } catch (Exception w) {
                                w.printStackTrace();
                            }
                        }
                    }
                }
            }).start();

            new Thread(()->{
                while(true){
                    if(flag){
                        String message = "小小:" + System.currentTimeMillis();
                        System.out.println("客户端发送数据：" + message);
                        try {
                            map.get("client").getOutputStream().write(message.getBytes());
                        } catch (IOException e) {
                            System.out.println("写数据出错");
                            e.printStackTrace();
                        }
                    }
                    sleep();
                }
            }).start();

            /*new Thread(()->{
                while(flag){
                    String message = "";
                    System.out.println("客户端接收数据 --> " + message + "  flag:" + flag);
                    try {
                        InputStream inputStream = map.get("client").getInputStream();
                        byte[] data = new byte[1024];
                        int len;
                        while(!map.get("client").isClosed() && !map.get("client").isInputShutdown() && (len=inputStream.read(data))!=-1 ){
                            System.out.println(new String(data,0,len));
                        }
                    } catch (IOException e) {
                        System.out.println("接收据出错");
//                        e.printStackTrace();
                    }
                }
            }).start();*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
