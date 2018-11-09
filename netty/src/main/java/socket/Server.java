package socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    public Server(int port){
        try {
            this.serverSocket = new ServerSocket(port);
            
            System.out.println("服务端启动成功：" + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        new Thread(()->doStart()).start();
    }

    private void doStart() {
        while(true){
            try {
                Socket client = serverSocket.accept();
                new ServerHandler(client).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server(11111);
        server.start();
    }

}
