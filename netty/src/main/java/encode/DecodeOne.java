package encode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class DecodeOne extends ByteToMessageDecoder {
        /*
         +---------------------------------------------------------------+
         | 魔数 2byte | 协议版本号 1byte | 序列化算法 1byte | 报文类型 1byte  |
         +---------------------------------------------------------------+
         | 状态 1byte |        保留字段 4byte     |      数据长度 4byte     | 
         +---------------------------------------------------------------+
         |                   数据内容 （长度不定）                          |
         +---------------------------------------------------------------+
          */
        @Override
        public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
           // 判断 ByteBuf 可读取字节
           if (in.readableBytes() < 14) {
              return;
           }
           in.markReaderIndex(); // 标记 ByteBuf 读指针位置
           in.skipBytes(2); // 跳过魔数
           in.skipBytes(1); // 跳过协议版本号
           byte serializeType = in.readByte();
           in.skipBytes(1); // 跳过报文类型
           in.skipBytes(1); // 跳过状态字段
           in.skipBytes(4); // 跳过保留字段
           int dataLength = in.readInt();
           if (in.readableBytes() < dataLength) {
              in.resetReaderIndex(); // 重置 ByteBuf 读指针位置
              return;
           }
           byte[] data = new byte[dataLength];
           in.readBytes(data);
//           SerializeService serializeService = getSerializeServiceByType(serializeType);
//           Object obj = serializeService.deserialize(data);
//           if (obj != null) {
//               out.add(obj);
//           }
        }
}
