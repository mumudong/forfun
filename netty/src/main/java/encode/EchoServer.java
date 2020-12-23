package encode;

import com.netty.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;

public class EchoServer {
    public void startEchoServer(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
//                            //固定长度解码器
//                            ch.pipeline().addLast(new FixedLengthFrameDecoder(10));
                            //stripDelimiter去掉分隔符,failFast为true则字节数达到maxFrameLength报错,为false则解析一个完整句子且字节数超过maxFrameLength报错
//                            ByteBuf delimiter = Unpooled.copiedBuffer("&".getBytes());
//                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10,true,true,delimiter));
//                            ch.pipeline().addLast(new EchoServerHandler());
                              ch.pipeline().addLast(new FixedLengthFrameDecoder(10));//in
                              ch.pipeline().addLast(new ResponseSampleEncoder());//out
                              ch.pipeline().addLast(new RequestSampleHandler());//in
                        }
                    });
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    public static void main(String[] args) throws Exception {
        new EchoServer().startEchoServer(8088);
    }
}
