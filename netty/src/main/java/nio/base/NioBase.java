package nio.base;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class NioBase {
    public static void main(String[] args) throws Exception{
        Selector selector = Selector.open();
        //创建一个 TCP 套接字通道
        SocketChannel channel = SocketChannel.open();
        //调整通道为非阻塞模式
        channel.configureBlocking(false);
        //向选择器注册一个通道
        //key维护selector和channel的关系
        //key.
        SelectionKey key = channel.register(selector, SelectionKey.OP_READ);

        //
        //假如 readySet 的值为 13，二进制 「0000 1101」，
        // 从后向前数，第一位为 1，第三位为 1，第四位为 1，那么说明选择器关联的通道，读就绪、写就绪，连接就绪。
        int readySet = key.readyOps();

        /**
         * selectedKeys 方法会返回选择器中注册成功的所有通道的 SelectionKey 实例集合。
         * 我们通过这个集合的 SelectionKey 实例，可以得到所有通道的事件就绪情况并进行相应的处理操作。
         */
        Set<SelectionKey> keys = selector.selectedKeys();



    }
}
