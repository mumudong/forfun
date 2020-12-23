package com.frame;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
/**
 * Created by Administrator on 2018/8/31.
 *
 *
 *当向通道中注册SelectionKey.OP_READ事件后，如果客户端有向缓存中write数据，下次轮询时，则会 isReadable()=true；
 *当向通道中注册SelectionKey.OP_WRITE事件后，这时你会发现当前轮询线程中isWritable()一直为ture，如果不设置为其他事件
 *
 * 注意：写事件很容易满足，就是操作系统给对应socket分配的缓冲区还有空闲,一般情况都满足
 * key.interestOps(SelectionKey.OP_READ)相当于给通道注册读事件,等价取消key关联通道对写事件感兴趣,一个key目前
 * 只能对一个事件感兴趣,如果不取消对写事件感兴趣，写事件会不断被触发
 *
 * 要点一：不推荐直接写channel，而是通过缓存和attachment传入要写的数据，改变interestOps()来写数据；
 * 要点二：每个channel只对应一个SelectionKey，所以，只能改变interestOps()，不能register()和cancel()。
 *
 *buffer ： capacity、position、limit
 *         position：当前读写指针位置
 *         limit：写模式下标识最多能写多少数据=capacity
 *                读模式下能赌多少=缓存中实际数据大小
 *                写模式下调用flip：limit设为position，即当前写了多少数据
 *                                position设为0，以表示读操作从缓存头开始
 *
 *BossEventLoopGroup 和 WorkerEventLoopGroup 包含一个或者多个 NioEventLoop。BossEventLoopGroup 负责监听客户端的 Accept 事件，
 * 当事件触发时，将事件注册至 WorkerEventLoopGroup 中的一个 NioEventLoop 上。每新建一个 Channel， 只选择一个 NioEventLoop 与其绑定。
 * 所以说 Channel 生命周期的所有事件处理都是线程独立的，不同的 NioEventLoop 线程之间不会发生任何交集。
 *
 *NioEventLoop 完成数据读取后，会调用绑定的 ChannelPipeline 进行事件传播，ChannelPipeline 也是线程安全的，数据会被传递到 ChannelPipeline 的第一个 ChannelHandler 中。
 *
 *
 *
 */

// NioEventLoop 无锁串行化的设计不仅使系统吞吐量达到最大化，而且降低了用户开发业务逻辑的难度，不需要花太多精力关心线程安全问题。
// 虽然单线程执行避免了线程切换，但是它的缺陷就是不能执行时间过长的 I/O 操作，一旦某个 I/O 事件发生阻塞，
// 那么后续的所有 I/O 事件都无法执行，甚至造成事件积压。在使用 Netty 进行程序开发时，我们一定要对 ChannelHandler 的实现逻辑有充分的风险意识。

public class ChatServer implements Runnable{
    //选择器
    private Selector selector;
    //注册ServerSocketChannel后的选择键
    private SelectionKey serverKey;
    //标识是否运行
    private boolean isRun;
    //当前聊天室中的用户名称列表
    private Vector<String> unames;
    //时间格式化器
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 构造函数
     * @param port 服务端监控的端口号
     */
    public ChatServer(int port) {
        isRun = true;
        unames = new Vector<String>();
        init(port);
    }

    /**
     * 初始化选择器和服务器套接字
     *
     * @param port 服务端监控的端口号
     */
    private void init(int port) {
        try {
            //获得选择器实例
            selector = Selector.open();
            //获得服务器套接字实例
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            //绑定端口号
            serverChannel.socket().bind(new InetSocketAddress(port));
            //设置为非阻塞
            serverChannel.configureBlocking(false);
            //将ServerSocketChannel注册到选择器，指定其行为为"等待接受连接"
            serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            printInfo("server starting...");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        try {
            //轮询选择器选择键
            while (isRun) {
                //选择一组已准备进行IO操作的通道的key，等于1时表示有这样的key
                //select会将就绪的事件放入selectedKeys中,不会自己删除,所以下面处理数据时需要清理处理过的事件
                int n = selector.select();
                if (n > 0) {
                    //从选择器上获取已选择的key的集合并进行迭代
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        //若此key的通道是等待接受新的套接字连接
                        if (key.isAcceptable()) {
                            //记住一定要remove这个key，否则之后的新连接将被阻塞无法连接服务器
                            iter.remove();
                            //获取key对应的通道
                            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                            //接受新的连接返回和客户端对等的套接字通道
                            SocketChannel channel = serverChannel.accept();
                            if (channel == null) {
                                continue;
                            }
                            //设置为非阻塞
                            channel.configureBlocking(false);
                            //将这个套接字通道注册到选择器，指定其行为为"读"
                            channel.register(selector, SelectionKey.OP_READ);
                        }
                        //若此key的通道的行为是"读"
                        if (key.isReadable()) {
                            readMsg(key);
                        }
                        //若次key的通道的行为是"写"
                        if (key.isWritable()) {
                            writeMsg(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从key对应的套接字通道上读数据
     * @param key 选择键
     * @throws IOException
     */
    private void readMsg(SelectionKey key) throws IOException {
        //获取此key对应的套接字通道
        SocketChannel channel = (SocketChannel) key.channel();
        //创建一个大小为1024k的缓存区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        StringBuffer sb = new StringBuffer();
        //将通道的数据读到缓存区
        int count = channel.read(buffer);
        if (count > 0) {
            //翻转缓存区(将缓存区由写进数据模式变成读出数据模式)
            buffer.flip();
            //将缓存区的数据转成String
            sb.append(new String(buffer.array(), 0, count));
        }
        String str = sb.toString();
        //若消息中有"open_"，表示客户端准备进入聊天界面
        //客户端传过来的数据格式是"open_zing"，表示名称为zing的用户请求打开聊天窗体
        //用户名称列表有更新，则应将用户名称数据写给每一个已连接的客户端
        if (str.indexOf("open_") != -1) {//客户端连接服务器
            String name = str.substring(5);
            printInfo(name + " online");
            unames.add(name);
            //获取选择器已选择的key并迭代
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selKey = iter.next();
                //若不是服务器套接字通道的key，则将数据设置到此key中
                //并更新此key感兴趣的动作
                if (selKey != serverKey) {
                    selKey.attach(unames);
                    selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        } else if (str.indexOf("exit_") != -1) {// 客户端发送退出命令
            String uname = str.substring(5);
            //删除此用户名称
            unames.remove(uname);
            //将"close"字符串附加到key
            key.attach("close");
            //更新此key感兴趣的动作
            key.interestOps(SelectionKey.OP_WRITE);
            //获取选择器上的已选择的key并迭代
            //将更新后的名称列表数据附加到每个套接字通道key上,并重设key感兴趣的操作
            Iterator<SelectionKey> iter = key.selector().selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selKey = iter.next();
                if (selKey != serverKey && selKey != key) {
                    selKey.attach(unames);
                    selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
            printInfo(uname + " offline");
        } else {// 读取客户端聊天消息
            String uname = str.substring(0, str.indexOf("^"));
            String msg = str.substring(str.indexOf("^") + 1);
            printInfo("("+uname+")说：" + msg);
            String dateTime = sdf.format(new Date());
            String smsg = uname + " " + dateTime + "\n  " + msg + "\n";
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey selKey = iter.next();
                if (selKey != serverKey) {
                    selKey.attach(smsg);
                    selKey.interestOps(selKey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        }
    }

    /**
     * 写数据到key对应的套接字通道
     * @param key
     * @throws IOException
     */
    private void writeMsg(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Object obj = key.attachment();
        //这里必要要将key的附加数据设置为空，否则会有问题
        key.attach("");
        //附加值为"close"，则取消此key，并关闭对应通道
        if (obj.toString().equals("close")) {
            key.cancel();
            channel.socket().close();
            channel.close();
            return;
        }else {
            //将数据写到通道
            channel.write(ByteBuffer.wrap(obj.toString().getBytes()));
        }
        //重设此key兴趣
        key.interestOps(SelectionKey.OP_READ);
    }

    private void printInfo(String str) {
        System.out.println("[" + sdf.format(new Date()) + "] -> " + str);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer(19999);
        new Thread(server).start();
    }
}
