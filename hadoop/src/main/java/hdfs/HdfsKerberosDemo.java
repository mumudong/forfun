package hdfs;


import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;

public class HdfsKerberosDemo {

    FileSystem fs;

    Configuration conf;

    @Before
    public void getset() throws Exception {
//		System.setProperty("HADOOP_USER_NAME", "hdfs");
        //默认加载/src配置文件 core-site.xml  hdfs-site.xml
        String basePath = HdfsKerberosDemo.class.getResource("/").toString();
        String user = "hdfs/hadoop-9@EXAMPLE.COM";
        conf = new Configuration();
        conf.set("dfs.nameservices", "tianxi-ha");
        conf.set("dfs.ha.namenodes.tianxi-ha", "nn1,nn2");
        conf.set("dfs.namenode.rpc-address.tianxi-ha.nn1", "hadoop-1:8020");
        conf.set("dfs.namenode.rpc-address.tianxi-ha.nn2", "hadoop-2:8020");
        conf.set("dfs.client.failover.proxy.provider.ns1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        conf.set("hadoop.security.authentication","kerberos");
        System.setProperty("java.security.krb5.conf", basePath.substring(6) + "krb5.conf");
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab(user, basePath.substring(6) + "hdfs-hadoop9.keytab");
        fs = FileSystem.get(conf);
    }

    @After
    public void end() throws Exception  {
        fs.close();
    }


    //查看ls、创建目录mkdir、上传put、下载、删除del
    @Test
    public void ls() throws Exception {
        Path path = new Path("/");
        FileStatus[] status = fs.listStatus(path);
        for(FileStatus s : status) {
            System.out.println(s.getPath());
        }
    }

    @Test
    public void mkdir() throws Exception {
        Path path = new Path("/test/dir");
        Boolean flag = fs.mkdirs(path);
        if(flag) {
            System.out.println("mkdir /test/dir success~~");
        }
    }

    @Test
    public void put() throws Exception {
        long start = System.currentTimeMillis();
        Path path = new Path("/test/架构设计与分层.mp4");
        FSDataOutputStream out = fs.create(path);
        IOUtils.copyBytes(new FileInputStream(new File("C:\\Users\\Administrator\\Desktop\\hdfs\\架构设计与分层.mp4")), out, conf);
        long stop = System.currentTimeMillis();
        System.out.println("上传文件耗时：" + (stop-start)/1000);
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

