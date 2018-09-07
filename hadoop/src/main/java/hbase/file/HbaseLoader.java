package hbase.file;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载文件
 *
 */
public class HbaseLoader {
    Logger logger = LoggerFactory.getLogger(getClass());
    Connection connection;
    //hbase表名称
    private static String tableName = "test_k";

    public HbaseLoader() throws Exception{
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "hadoop-5,hadoop-6,hadoop-7");
        connection = ConnectionFactory.createConnection(configuration);
    }

    /**
     * 下载单个文件至指定文件夹
     * @param fileName 待下载文件名
     * @param path 待下载文件下载至...
     * @throws Exception
     */
    public void getFile(String fileName,String path)throws Exception{
        checkParam(fileName,path);
        File dir = new File(path);
        Table table = connection.getTable(TableName.valueOf(tableName));
        long start = System.currentTimeMillis();
        Get get = new Get(Bytes.toBytes(fileName));
        get.addFamily(Bytes.toBytes("info"));
        Result result = table.get(get);
        logger.info("下载文件 --> " + fileName);
        OutputStream outputStream = new FileOutputStream(dir.getAbsolutePath() + "/" + fileName);
        //获取录音内容
        byte[] res = result.getValue(Bytes.toBytes("info"),Bytes.toBytes("file"));
        ByteArrayInputStream bas = new ByteArrayInputStream(res);
        IOUtils.copy(bas,outputStream);
        outputStream.close();
        bas.close();
        table.close();
        long stop = System.currentTimeMillis();
        System.out.println("下载文件耗时:" + (stop-start)/1000);
        /** 关闭连接 */
        this.close();
    }

    /**
     * 下载多个文件至指定文件夹
     * @param fileNames 待下载文件名列表
     * @param path 待下载文件下载至...
     * @throws Exception
     */
    @SuppressWarnings("all")
    public void getFile(String[] fileNames,String path)throws Exception{
        for(String fileName:fileNames) {
            checkParam(fileName,path);
        }
        File dir = new File(path);
        Table table = connection.getTable(TableName.valueOf(tableName));
        long start = System.currentTimeMillis();
        List<Get> getList = new ArrayList<>();
        //每十个文件一个批次
        for(int i = 0;i < fileNames.length;i++){
            Get get = new Get(Bytes.toBytes(fileNames[i]));
            get.addFamily(Bytes.toBytes("info"));
            getList.add(get);
            if(i % 10 == 9){
                Result[] results = table.get(getList);
                for(Result result:results) {
                    String fileName = Bytes.toString(result.getValue("info".getBytes(),"fileName".getBytes()));
                    logger.info("下载文件 --> " + fileName);
                    OutputStream outputStream = new FileOutputStream(dir.getAbsolutePath() + "/" + fileName);
                    //获取录音内容
                    byte[] res = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("file"));
                    ByteArrayInputStream bas = new ByteArrayInputStream(res);
                    IOUtils.copy(bas, outputStream);
                    outputStream.close();
                    bas.close();
                }
                getList.clear();
            }
            Result[] results = table.get(getList);
            for(Result result:results) {
                String fileName = Bytes.toString(result.getValue("info".getBytes(),"fileName".getBytes()));
                logger.info("下载文件 --> " + fileName);
                OutputStream outputStream = new FileOutputStream(dir.getAbsolutePath() + "/" + fileName);
                //获取录音内容
                byte[] res = result.getValue(Bytes.toBytes("info"), Bytes.toBytes("file"));
                ByteArrayInputStream bas = new ByteArrayInputStream(res);
                IOUtils.copy(bas, outputStream);
                outputStream.close();
                bas.close();
            }
        }
        table.close();
        long stop = System.currentTimeMillis();
        System.out.println("下载文件耗时:" + (stop-start)/1000);
        /** 关闭连接 */
        this.close();
    }

    //退出时关闭连接
    public void close()throws Exception{
        this.connection.close();
    }

    public void checkParam(String fileName,String path) throws Exception{
        File dir = new File(path);
        if(StringUtils.isBlank(fileName) || StringUtils.isBlank(path)){
            throw new Exception("文件名错误，fileName = " + fileName);
        }else if(!dir.isDirectory()){
            logger.info("文件夹不存在，开始创建文件夹:" + path);
            dir.mkdirs();
        }
    }

    public static void setTableName(String tableName) {
        HbaseLoader.tableName = tableName;
    }
}
