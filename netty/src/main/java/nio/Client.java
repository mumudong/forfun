package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);

        channel.connect(new InetSocketAddress("localhost",9999));
        //非阻塞时连不上会直接往下走,所以这里要判断有没有连接上
        while(!channel.isConnected()){
            //底层会记数,超过一定次数不再尝试重新连接
            channel.finishConnect();
        }
       //建立连接之后程序会直接结束

        channel.write(ByteBuffer.wrap("hello server".getBytes()));
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        System.out.println("receive from server: " + new String(buffer.array(),0,buffer.position()));

        channel.write(ByteBuffer.wrap("hello server".getBytes()));
        ByteBuffer buffer2 = ByteBuffer.allocate(1024);
        channel.read(buffer);
        System.out.println("receive from server: " + new String(buffer.array(),0,buffer.position()));

        channel.close();
    }
}
