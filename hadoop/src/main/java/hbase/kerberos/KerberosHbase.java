package hbase.kerberos;

import java.io.IOException;
import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KerberosHbase {
    Connection connection;
    Configuration conf ;
    Admin admin;
    Table table;
    @Before
    public void setUp() throws Exception{
        System. setProperty("java.security.krb5.conf", "C:\\Users\\Administrator\\Downloads\\krb5.conf" );
        conf = HBaseConfiguration.create();
        conf.set("dfs.nameservices", "tianxi-ha");
        conf.set("dfs.ha.namenodes.tianxi-ha", "nn1,nn2");
        conf.set("dfs.namenode.rpc-address.tianxi-ha.nn1", "hadoop-1:8020");
        conf.set("dfs.namenode.rpc-address.tianxi-ha.nn2", "hadoop-2:8020");
        conf.set("dfs.client.failover.proxy.provider.ns1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        conf.set("hbase.security.authentication","kerberos");
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab("hbase", "C:\\Users\\Administrator\\Downloads\\hbase.keytab");
        connection = ConnectionFactory.createConnection(conf);
        admin = connection.getAdmin();
    }
    @Test
    public void createTable() throws Exception{
        TableName tablename = TableName.valueOf("test_k");
        if(admin.tableExists(tablename)){
            admin.disableTable(tablename);
            admin.deleteTable(tablename);
            System.out.println("删除旧表！");
        }
        System.out.println("新的表正在创建中！！！");
        HTableDescriptor tableDescriptor = new HTableDescriptor(tablename);
        tableDescriptor.addFamily(new HColumnDescriptor("info"));
        admin.createTable(tableDescriptor);

        Put put = new Put("123".getBytes());
        put.add("info".getBytes(), "colum1".getBytes(), "value1".getBytes()) ;
        put.add("info".getBytes(), "colum2".getBytes(), "value2".getBytes()) ;
        put.add("info".getBytes(), "colum3".getBytes(), "value3".getBytes()) ;

        Put put2 = new Put("234".getBytes()) ;
        put2.add("info".getBytes(), "colum1".getBytes(), "value1".getBytes()) ;
        put2.add("info".getBytes(), "colum2".getBytes(), "value2".getBytes()) ;
        put2.add("info".getBytes(), "colum3".getBytes(), "value3".getBytes()) ;

        HTable table = new HTable(conf, tablename);
        table.put(put);
        table.put(put2);
        table.close();
    }
    @Test
    public void putData(){
        System.out.println("begin-----0");
        try {
            table = connection.getTable(TableName.valueOf("top4Count"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("begin-----1");
        Put put = new Put(Bytes.toBytes("rowKey1"));
        put.addColumn(Bytes.toBytes("result"),Bytes.toBytes("des"),Bytes.toBytes("this is test-1"));
        try {
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("begin-----2");
        System.out.println("数据插入完毕！");
        try {
            table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


















    @After
    public void end() throws Exception{
        admin.close();
        connection.close();
    }
}