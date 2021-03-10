package nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * BIO同步阻塞：并发少长任务
 * NIO同步非阻塞:适合高并发短任务
 * AIO异步非阻塞
 */
public class Server {
    public static void main(String[] args) throws IOException {
//        nonSelectorServer();
        selectorServer();
    }

    /**
     * selector作用于服务端
     *
     * client            selector                server
     *                      |
     * connect           ---|-->                 accept
     * read              <--|--                  write
     * write             ---|-->                 read
     *
     * @throws IOException
     */
    static void selectorServer()throws IOException{
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(9999));
        serverChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        while(true){
            //int select()：阻塞到至少有一个通道在你注册的事件上就绪了。
            //int select(long timeout)：和select()一样，但最长阻塞时间为timeout毫秒。
            //int selectNow()：非阻塞，只要有通道就绪就立刻返回。
            //select()方法返回的int值表示有多少通道已经就绪,是自上次调用select()方法后有多少通道变成就绪状态。之前在select（）调用时进入就绪的通道不会在本次调用中被记入，而在前一次select（）调用进入就绪但现在已经不在处于就绪的通道也不会被记入。例如：首次调用select()方法，如果有一个通道变成就绪状态，返回了1，若再次调用select()方法，如果另一个通道就绪了，它会再次返回1。如果对第一个就绪的channel没有做任何操作，现在就有两个就绪的通道，但在每次select()方法调用之间，只有一个通道就绪了。
            //一旦调用select()方法，并且返回值不为0时，则 可以通过调用Selector的selectedKeys()方法来访问已选择键集合
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                if(key.isAcceptable()){
                    ServerSocketChannel serverChann = (ServerSocketChannel) key.channel();
                    SocketChannel clientChanner = serverChann.accept();
                    //register连续多次调用时,最后一次调用会覆盖之前的
                    clientChanner.configureBlocking(false);
                    clientChanner.register(selector,SelectionKey.OP_READ|SelectionKey.OP_WRITE);
                }else if(key.isReadable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    channel.read(buffer);
                    System.out.println("receive from client: " + new String(buffer.array(),0,buffer.position()));
                    /**
                     * client     selector    server
                     * read        <---       write
                     * write       --->       read
                     * 处理完之后视情况而定是否取消事件
                     */
                    //取消读取事件
                    channel.register(selector,key.interestOps() ^ SelectionKey.OP_READ);

                }else if(key.isWritable()){
                    SocketChannel channel = (SocketChannel) key.channel();
                    channel.write(ByteBuffer.wrap("i am server,hello client".getBytes()));
                    //不注销可能重复写出？？？
                    channel.register(selector,key.interestOps() ^ SelectionKey.OP_WRITE);
                }
                iterator.remove();
            }
        }
    }

    static void nonSelectorServer() throws IOException{
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(9999));
        serverChannel.configureBlocking(false);
        SocketChannel clientChannel = serverChannel.accept();

        while(clientChannel == null){
            clientChannel = serverChannel.accept();
        }
        //建立连接之后程序会直接结束
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        clientChannel.read(allocate);
        allocate.flip();
        System.out.println(new String(allocate.array(),0,allocate.limit()));

        clientChannel.write(ByteBuffer.wrap("我是服务端,已收到数据".getBytes()));


        serverChannel.close();
    }
}
