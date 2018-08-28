package hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

/**
 * Created by Administrator on 2018/7/26.
 */
public class KerberosHdfs {
    public static void test1(String user, String keytab, String dir) throws Exception {
//        System.setProperty("java.security.krb5.conf", "D:/cdh_10/krb5.conf");
        Configuration conf = new Configuration();
        conf.addResource(new Path("D:/cdh_10/hdfs-site.xml"));
        conf.addResource(new Path("D:/cdh_10/core-site.xml"));
        System.setProperty("java.security.krb5.conf", "D:/cdh_10/krb5.conf");
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab(user, keytab);
        listDir(conf, dir);
    }

    public static void listDir(Configuration conf, String dir) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        FileStatus files[] = fs.listStatus(new Path(dir));
        for (FileStatus file : files) {
            System.out.println(file.getPath());
        }
    }

    public static void main(String[] args) {
        String user = "xxx";
        String keytab = "D:/cdh_10/xxx.keytab";
        String dir = "hdfs://ns1/data";
        try {
            test1(user, keytab, dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}