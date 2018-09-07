package hbase.file;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *  向队列添加待上传文件路径
 */
//@Component
public class HbaseProducer{
    private volatile boolean isRunning = true;
    private BlockingQueue<File> queue;
    Logger logger = LoggerFactory.getLogger(getClass());
    public HbaseProducer() throws Exception{
        this.queue = new LinkedBlockingQueue<>(10);
        HbaseConsumer consumer1 = new HbaseConsumer(queue);
        HbaseConsumer consumer2 = new HbaseConsumer(queue);
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(consumer1);
        service.execute(consumer2);
    }

    public void produce(String path)throws Exception{
        File dir = new File(path);
        if (dir.isFile()){
            logger.info("生产文件 --> " + dir.getAbsolutePath() + " 文件大小(kb)：" + dir.length()/1024);
            queue.put(dir);
        }else if(dir.isDirectory()){
            for(File file:dir.listFiles()){
                if(file.isFile()) {
                    logger.info("生产文件 --> " + dir.getAbsolutePath() + " 文件大小(kb)：" + file.length()/1024);
                    queue.put(file);
                }
                else if(file.isDirectory()) {
                    logger.info("目录文件 ----> " + dir.getAbsolutePath());
                    produce(file.getAbsolutePath());
                }
            }
        }
    }
}
