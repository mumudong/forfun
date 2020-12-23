package encode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * write 系列方法会改变 writerIndex 位置，当 writerIndex 等于 capacity 的时候，Buffer 置为不可写状态；
 *
 * 向不可写 Buffer 写入数据时，Buffer 会尝试扩容，但是扩容后 capacity 最大不能超过 maxCapacity，如果写入的数据超过 maxCapacity，程序会直接抛出异常；
 *
 * read 系列方法会改变 readerIndex 位置，get/set 系列方法不会改变 readerIndex/writerIndex 位置。
 */
public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(6,10);
        printByteBufInfo("ByteBufAllocator.buffer(5,10)",buf);
        buf.writeBytes(new byte[]{1,2});
        printByteBufInfo("write 2 bytes",buf);
        buf.writeInt(100);
        printByteBufInfo("write int 100",buf);
        buf.writeBytes(new byte[]{3,4,5});
        printByteBufInfo("write 3 bytes",buf);
        byte[] read = new byte[buf.readableBytes()];
        buf.readBytes(read);
        printByteBufInfo("readerBytes(" + buf.readableBytes() + ")",buf);
        printByteBufInfo("beforeGetAndSet",buf);
        System.out.println("getInt(2):" + buf.getInt(2));
        buf.setByte(1,0);
        System.out.println("getByte(1):" + buf.getByte(1));
        printByteBufInfo("afterGetAndSet",buf);


    }

    static void printByteBufInfo(String step,ByteBuf buffer){
        System.out.println("--------------" + step + "--------------");
        System.out.println("readerIndex():" + buffer.readerIndex());
        System.out.println("writerIndex():" + buffer.writerIndex());
        System.out.println("readableBytes():" + buffer.readableBytes());
        System.out.println("maxWritableBytes():" + buffer.maxWritableBytes());
        System.out.println("capacity():" + buffer.capacity());
        System.out.println("maxCapacity():" + buffer.maxCapacity());
    }
}
