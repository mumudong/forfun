package nio;

import java.nio.ByteBuffer;


/**
 * limit是读写能达到的最大位置
 *   读模式时为最后一个字节的下一位
 *   写模式为capacity
 * 写模式
 *
 *   a b c . . . .
 *         position=3
 *         limit = 7
 *         capacity=7
 *  flip反转到读模式
 *  a b c . . . .
 *  position = 0
 *  limit = 3
 *  capacity=7
 */
public class BufferTest {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("abc".getBytes());
        System.out.println("position --> " + buffer.position());
        System.out.println("limit --> " + buffer.limit());
        byte b = buffer.get();
        //下一个位置是3,没有数据
        System.out.println(b);
        //切换到读模式
        buffer.flip();
        //多线程读取时
        //线程1读取1-10个字节,position为10
        //线程2想从头读的话需要执行rewind,position变为0
        buffer.rewind();
    }
}
