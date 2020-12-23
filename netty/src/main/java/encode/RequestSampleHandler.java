package encode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

/**
 * 负责客户端数据处理
 */
public class RequestSampleHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String data = ((ByteBuf)msg).toString(CharsetUtil.UTF_8);
        System.out.println("this is requestSampleHandler,data:"+data);
        ResponseSample response = new ResponseSample("OK",data,System.currentTimeMillis());
        //flush  终会定位到 AbstractChannelHandlerContext 中的 write
        //flush 从tailContext至headContext,最终由head刷出
        ctx.channel().writeAndFlush(response);
    }
}
