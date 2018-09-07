package hbase.file;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2018/8/28.
 */
public class HbaseTest {
    public static void main(String[] args)throws Exception {

//        HbaseProducer producer1 = new HbaseProducer();
        //上传文件及目录
//        producer1.produce("C:\\Users\\Administrator\\Desktop\\hdfs");
        //单独下载、批量下载
        HbaseLoader loader = new HbaseLoader();
        loader.getFile("f44ea377a10f11e8a8580050568c03b7_1535106302520.mp3","C:\\Users\\Administrator\\Desktop\\hdfs\\a\\b");
    }
}
