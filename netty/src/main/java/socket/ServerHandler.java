package socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ServerHandler {
    public static final int MAX_DATA_LEN = 1024;
    private final Socket socket;

    public ServerHandler(Socket socket){
        this.socket=socket;
    }

    public void start(){
        System.out.println("新客户端接入");
        new Thread(()->doStart()).start();
    }

    private void doStart(){
        try {
            InputStream inputStream = socket.getInputStream();
            while(true){
                byte[] data = new byte[MAX_DATA_LEN];
                int len;
                while(inputStream.available()>0 && (len=inputStream.read(data))!=-1){
                    System.out.println("客户端传来消息：" + new String(data,0,len));
                    socket.getOutputStream().write(("服务端收到消息了：" + System.currentTimeMillis()).getBytes());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
