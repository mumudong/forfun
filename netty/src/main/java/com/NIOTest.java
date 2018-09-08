package com;

import org.junit.Test;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Administrator on 2018/8/31.
 */
public class NIOTest {
    /**
     * FileChannel,DatagramChannel,SocketChannel,ServerSocketChannel
     *
     * 下面这种方式成为scattering read分散读，必须前一个buffer满了才会写第二个buffer
     * ByteBuffer header = ByteBuffer.allocate(128);
     * ByteBuffer body   = ByteBuffer.allocate(1024);
     * ByteBuffer[] bufferArray = { header, body };
     * channel.read(bufferArray);
     *
     * 下面这种方式称为gathering write,可以较好处理动态消息，只会写入包含的消息，不会按buffer容器的大小写
     * ByteBuffer header = ByteBuffer.allocate(128);
     * ByteBuffer body   = ByteBuffer.allocate(1024);
     * ByteBuffer[] bufferArray = { header, body };
     * channel.write(bufferArray);
     */
    @Test
    public void channel()throws Exception{
        RandomAccessFile aFile = new RandomAccessFile("C:\\Users\\Administrator\\Desktop\\ansj-solr\\library.properties","rw");
        FileChannel channel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(48);
        int bytesRead = channel.read(buf);
        while (bytesRead != -1){
            System.out.println("read " + bytesRead);
            buf.flip();
            while(buf.hasRemaining()){
                System.out.print((char)buf.get());
            }
            buf.clear();//或者 buf.compact() 只清除已经读过的数据，未读的数据都被移至缓冲区的起始处
            bytesRead = channel.read(buf);
        }
        aFile.close();
    }

    @Test
    public void buffer(){

    }
}
