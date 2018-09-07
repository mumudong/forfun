package hbase.file;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.concurrent.BlockingQueue;

/**
 *  消费队列中文件名，上传文件
 */
public class HbaseConsumer implements Runnable{
    Logger logger = LoggerFactory.getLogger(getClass());
    private volatile boolean isRunning = true;
    private BlockingQueue<File> queue;
    Connection connection;
    //上传至表名称
    private String tableName = "test_k";
    //本条记录对应的是tianzt组的数据
    private String type = "tianzt";

    public HbaseConsumer(BlockingQueue queue) throws Exception{
        this.queue = queue;
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "hadoop-5,hadoop-6,hadoop-7");
        connection = ConnectionFactory.createConnection(configuration);
    }

    @Override
    public void run() {
        logger.info("启动hbase发送线程......");
        boolean isRunning = true;
        try {
            while (isRunning) {
                logger.info("获取文件中...........");
                File file = queue.take();
                if (null != file) {
                    logger.info("拿到文件：" + file.getName());
                    long start = System.currentTimeMillis();
                    Table table = connection.getTable(TableName.valueOf(tableName));
                    Put put = new Put(Bytes.toBytes(file.getName()));
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    InputStream inputStream = new FileInputStream(file);
                    IOUtils.copy(inputStream,outputStream);
                    inputStream.close();
                    put.addColumn(Bytes.toBytes("info"),"file".getBytes(),outputStream.toByteArray());
                    put.addColumn(Bytes.toBytes("info"),"fileName".getBytes(),file.getName().getBytes());
                    put.addColumn(Bytes.toBytes("info"),"date".getBytes(), LocalDate.now().toString().getBytes());
                    put.addColumn(Bytes.toBytes("info"),"type".getBytes(), type.getBytes());
                    table.put(put);
                    outputStream.close();
                    table.close();
                    long end  = System.currentTimeMillis();
                    logger.info("文件已上传：" + file.getName() + " 耗时：" + (end-start)/1000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            logger.info("退出hbase发送者线程！");
        }
    }
    //退出时关闭连接
    public void close()throws Exception{
        this.connection.close();
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setType(String type) {
        this.type = type;
    }
}
