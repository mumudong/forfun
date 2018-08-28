package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

public class HBaseClient {
    static Configuration conf;
    static {
        System. setProperty("java.security.krb5.conf", "C:\\Users\\Administrator\\Downloads\\krb5.conf" );
        conf = HBaseConfiguration.create();
        UserGroupInformation.setConfiguration(conf);
        try {
            UserGroupInformation.loginUserFromKeytab("hbase", "C:\\Users\\Administrator\\Downloads\\hbase.keytab");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(conf.get("hbase.zookeeper.quorum"));
    }
    public static HTableInterface getTable(String name) throws Exception {

        HConnection connection = HConnectionManager.createConnection(conf);
        HTableInterface result = connection.getTable(name);
        return result;
    }

    public static void createTable(String tablename) throws Exception{

        HBaseAdmin admin = new HBaseAdmin(conf);
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
    }
    /**
     * get data from hbase table
     * @param table
     * @throws IOException
     */
    public static void getData(HTableInterface table) throws IOException {
        Get get = new Get(Bytes.toBytes("10001"));
        get.addFamily(Bytes.toBytes("info"));
        Result result = table.get(get);

        for (Cell cell : result.rawCells()) {
            System.out.println(
                    Bytes.toString(CellUtil.cloneFamily(cell)) + "->" +
                            Bytes.toString(CellUtil.cloneQualifier(cell)) + "->" +
                            Bytes.toString(CellUtil.cloneValue(cell)) + "->" +
                            cell.getTimestamp()
            );
            System.out.println("-----------------------------------------");
        }
    }

    /**
     * insert data into hbase table
     * @param table
     * @throws Exception
     */
    public static void putData(HTableInterface table) throws Exception {
        Put put = new Put(Bytes.toBytes("10001"));
        put.add(Bytes.toBytes("info"),
                Bytes.toBytes("sex"),
                Bytes.toBytes("female"));
        put.add(Bytes.toBytes("info"),
                Bytes.toBytes("addr"),
                Bytes.toBytes("Shanghai"));
        table.put(put);
        getData(table);
    }

    /**
     * delete data from hbase table
     *
     * @param table
     * @throws Exception
     */
    public static void delData(HTableInterface table) throws Exception {
        Delete del = new Delete(Bytes.toBytes("10001"));
        del.deleteColumn(Bytes.toBytes("info"),
                Bytes.toBytes("sex"));

        del.deleteColumns(
                Bytes.toBytes("info"),
                Bytes.toBytes("addr")
        );

        table.delete(del);
        getData(table);
    }

    /**
     * full scan data from hbase table
     *
     * @param table
     * @throws Exception
     */
    public static void scanData(HTableInterface table) throws Exception {
        Scan scan = new Scan();

        ResultScanner rsscan = table.getScanner(scan);

        printResultScanner(rsscan);
    }

    /**
     * range scan data from hbase table
     *
     * @param table
     * @throws Exception
     */
    public static void rangeScan(HTableInterface table) throws Exception {
        //get the scan instance
        Scan scan = new Scan();
        //conf the scan1
        //scan.addColumn(Bytes.toBytes("info"), Bytes.toBytes("name"));
        scan.addFamily(Bytes.toBytes("info"));
        //conf the scan2
        //scan.setStartRow(Bytes.toBytes("10001"));
        //scan.setStopRow(Bytes.toBytes("10002"));
        //conf the scan3
        //String prefix = "10003";
        //Filter filter = new PrefixFilter(Bytes.toBytes(prefix));
        //scan.setFilter(filter);
        //conf the scan4
        scan.setCacheBlocks(true);
        //两个共同决定了rpc的请求的次数
        //scan操作做操作的时候，hbase客户端是通过rpc去hbase server端取数据的
        // 一次rpc中能够获取的行数就是cache的值，
        // server 端一次能够传送的column的数量就是batch,最后再有一次rpc确认scan结束
        scan.setCaching(100);
        scan.setBatch(1);
        ResultScanner rsscan = table.getScanner(scan);
        //print the data
        printResultScanner(rsscan);
    }

    /**
     * print cell info.
     *
     * @param cell
     */
    public static void printCell(Cell cell) {
        System.out.println(
                Bytes.toString(CellUtil.cloneFamily(cell)) + "->" +
                Bytes.toString(CellUtil.cloneQualifier(cell))  + "->" +
                Bytes.toString(CellUtil.cloneValue(cell))  + "->" +
                cell.getTimestamp());
    }

    /**
     * print result(cells) info.
     *
     * @param rs
     */
    public static void printResult(Result rs) {
        System.out.println(Bytes.toString(rs.getRow()));
        for (Cell cell : rs.rawCells()) {
            printCell(cell);
        }
        System.out.println("------------------------------------");
    }

    /**
     * print result scanner(results) info.
     *
     * @param rsc
     */
    public static void printResultScanner(ResultScanner rsc) {
        for (Result rs : rsc) {
            printResult(rs);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("createTable case...");
        createTable("student");
        HTableInterface tbStudent = HBaseClient.getTable("student");
        System.out.println("getData case...");
        getData(tbStudent);
        System.out.println("putData case...");
        putData(tbStudent);
//        System.out.println("delData case...");
//        delData(tbStudent);
//        System.out.println("scanData case...");
//        scanData(tbStudent);
//        System.out.println("rangeScan case...");
//        rangeScan(tbStudent);
    }
}
