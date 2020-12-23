package httplearn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.InetSocketAddress;

/***
 *
 *
 * Reactor 线程模型运行机制的四个步骤，分别为
 *       连接注册: Channel 建立后，注册至 Reactor 线程中的 Selector 选择器
 *       事件轮询: 轮询 Selector 选择器中已注册的所有 Channel 的 I/O 事件
 *       事件分发: 为准备就绪的 I/O 事件分配相应的处理线程
 *       任务处理: Reactor 线程还负责任务队列中的非 I/O 任务，每个 Worker 线程从各自维护的任务队列中取出任务异步执行
 *
 * EventLoop是一种事件等待和处理的程序模型，可以解决多线程资源消耗高的问题。
 *   当事件发生时，应用程序都会将产生的事件放入事件队列当中，然后 EventLoop 会轮询从队列中取出事件执行或者将事件分发给相应的事件监听者执行。
 *   事件执行的方式通常分为立即执行、延后执行、定期执行几种。
 *
 *
 *
 *
 *
 */
public class HttpServer {
    public void start(int port) throws Exception{
        /**
         * reactor：单线程模型所有I/O操作由一个线程完成,new NioEventLoopGroup(1)
         *          多线程模式：new NioEventLoopGroup(),默认2倍cpu核数的线程,也可自己传参指定
         *          主从多线程模式：主reactor负责处理连接accept,然后把channel注册到从reactor,从reactor负责channel生命周期内所有I/O
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    /**
                     * 有多种类型channel,还有OioServerSocketChannel/EpollServerSocketChannel
                     */
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("codec",new HttpServerCodec())
                                                    .addLast("compressor",new HttpContentCompressor())
                                                     //http消息聚合
                                                     .addLast("aggregator",new HttpObjectAggregator(65536))
                                                     .addLast("handler",new HttpServerHandler());
                        }
                    })
                    /**
                     * 设置channel属性有option和childOption,option负责boss线程组,childOption负责worker线程组
                     */
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            /**
             * bind会启动,sync阻塞至启动完成
              */
            ChannelFuture f = b.bind().sync();
            System.out.println("http server started,listening on " + port);
            /**
             * 让线程进入wait状态,使服务端一直处于运行状态
             */
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
    public static void main(String[] args) throws Exception{
        // curl http://localhost:8088/abc?this_is_test
        new HttpServer().start(8088);
    }
}
