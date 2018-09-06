package hdfs;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.ToolRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.net.ResourceManager;

public class HdfsDemo {

    FileSystem fs;

    Configuration conf;

    @Before
    public void getset() throws Exception {
		System.setProperty("HADOOP_USER_NAME", "hdfs");
        //默认加载/src配置文件 core-site.xml  hdfs-site.xml
        conf = new Configuration();
        conf.set("fs.defaultFS","hdfs://tianxi-ha");
        conf.set("dfs.nameservices", "tianxi-ha");
        conf.set("dfs.ha.namenodes.tianxi-ha", "nn1,nn2");
        conf.set("dfs.namenode.rpc-address.tianxi-ha.nn1", "hdp-1:8020");
        conf.set("dfs.namenode.rpc-address.tianxi-ha.nn2", "hdp-2:8020");
        conf.set("dfs.client.failover.proxy.provider.ns1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
//        fs = FileSystem.get(conf);
        fs = FileSystem.get(new URI("hdfs://tianxi-ha"), conf, "hdfs");
    }

    @After
    public void end() throws Exception  {
        fs.close();
    }
    @Test
    public void shell() throws Exception{
        FsShell shell = new FsShell();
        ToolRunner.run(shell,new String[]{"-chown","-R","tianzt:tianzt","/microservice"});
        ToolRunner.run(shell,new String[]{"-chown","-R","tianzt:tianzt","/TxFile"});

    }

    @Test
    public void test() throws Exception{
        System.out.println("absolutePath --> " + HdfsDemo.class.getResource(""));
        System.out.println("absolutePath --> " + HdfsDemo.class.getResource("/"));
    }

    //查看ls、创建目录mkdir、上传put、下载、删除del
    @Test
    public void ls() throws Exception {
//        Path path = new Path("/");
//        FileStatus[] status = fs.listStatus(path);
//        for(FileStatus s : status) {
//            System.out.println(s.getPath());
//            System.out.println(s.getAccessTime());
//            System.out.println(s.getLen());
//            System.out.println(s.getBlockSize());
//        }
        String [] dirs = new String[]{"/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180817",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180818",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180820",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180821",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180822",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180823",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180824",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180825",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180827",
                "/TxFile/remote/prod/zhzc/UrgePayment/jiaxin/20180828"};
        File file = new File("D:\\ksdler\\git_repository\\forfun\\hadoop\\src\\main\\out");

        for(String dir:dirs){
            System.out.println("--------------"+dir);
            FileOutputStream out = new FileOutputStream(dir.substring(dir.lastIndexOf("/")+1));
            BufferedOutputStream buffer = new BufferedOutputStream(out);
            Path path = new Path(dir);
            FileStatus[] status = fs.listStatus(path);
            for(FileStatus s : status) {
                String lujing =  s.getPath().toString();
                lujing = lujing.substring(lujing.lastIndexOf("/")+1);
                System.out.println(lujing);
                out.write((lujing+"\n").getBytes());
            }
            buffer.close();
            out.close();
        }
    }

    @Test
    public void mkdir() throws Exception {
        Path path = new Path("/microservice/test");

//        FsPermission permission = new FsPermission(FsAction.ALL,FsAction.ALL,FsAction.ALL);
//        fs.setPermission(path,permission);

        Boolean flag = fs.mkdirs(path);
        if(flag) {
            System.out.println("mkdir /microservice/test~~");
        }
    }

    @Test
    public void put() throws Exception {
        Path path = new Path("/test/aaa.txt");
        FSDataOutputStream out = fs.create(path);
        IOUtils.copyBytes(new FileInputStream(new File("D:/HDFS/aaa.txt")), out, conf);
    }

    @Test
    public void putsmall() throws Exception {
        //小文件上传
        Path path = new Path("/test/bigfile");
        SequenceFile.Writer write = new SequenceFile.Writer(fs, conf, path, Text.class, Text.class);

        File[] files = new File("D:/HDFS/").listFiles();
        for(File f : files) {
            write.append(new Text(f.getName()), new Text(FileUtils.readFileToString(f)));
        }
        write.close();
    }

    @Test
    public void getsmall() throws Exception {
        Path path = new Path("/test/bigfile");
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, path, conf);

        Text key = new Text();
        Text val = new Text();

        while(reader.next(key, val)) {
            System.out.println("111");
            System.out.println(key.toString());
            System.out.println(val.toString());
        }
    }

    @Test
    public void del() throws Exception {
        Path path = new Path("/test/aaa.txt");
        fs.delete(path, true);
    }
}

