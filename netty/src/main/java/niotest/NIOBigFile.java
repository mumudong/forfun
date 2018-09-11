package niotest;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * NIO按行读取百万级别文件
 * @author Chillax
 *
 */
public class NIOBigFile {

    public static void main(String[] args) throws Exception {
        //指定读取文件所在位置
        File file = new File("d:/tmp/a.txt");
        FileChannel fileChannel = new RandomAccessFile(file,"r").getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        //使用temp字节数组用于存储不完整的行的内容
        byte[] temp = new byte[0];
        while(fileChannel.read(byteBuffer) != -1) {
            byte[] bs = new byte[byteBuffer.position()];
            byteBuffer.flip();
            byteBuffer.get(bs);
            byteBuffer.clear();
            int startNum=0;
            //判断是否出现了换行符，注意这要区分LF-\n,CR-\r,CRLF-\r\n,这里判断\n
            boolean isNewLine = false;
            for(int i=0;i < bs.length;i++) {
                if(bs[i] == 10) {
                    isNewLine = true;
                    startNum = i;//第i个字节是换行符
                }
            }

            if(isNewLine) {
                //如果出现了换行符，将temp中的内容与换行符之前的内容拼接
                byte[] toTemp = new byte[temp.length+startNum];
                System.arraycopy(temp,0,toTemp,0,temp.length);
                System.arraycopy(bs,0,toTemp,temp.length,startNum);
                System.out.println(new String(toTemp));
                //将换行符之后的内容(去除换行符)存到temp中
                temp = new byte[bs.length-startNum-1];
                System.arraycopy(bs,startNum+1,temp,0,bs.length-startNum-1);
                //使用return即为单行读取，不打开即为全部读取
//                return;
            } else {
                //如果没出现换行符，则将内容保存到temp中
                byte[] toTemp = new byte[temp.length + bs.length];
                System.arraycopy(temp, 0, toTemp, 0, temp.length);
                System.arraycopy(bs, 0, toTemp, temp.length, bs.length);
                temp = toTemp;
            }

        }
        if(temp.length>0) {
            System.out.println(new String(temp));
        }

    }
}
