package hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HBaseClient {

    Logger logger = LoggerFactory.getLogger(getClass());
    Connection connection;
    @Before
    public void init() throws Exception{
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        conf.set("hbase.zookeeper.quorum", "hdp-5,hdp-6,hdp-7");
        connection = ConnectionFactory.createConnection(conf);
    }
    @After
    public void close() throws Exception{
        connection.close();
    }
    @Test
    public  void createTable() throws Exception{
        String tablename = "txfile_file";
        TableName table = TableName.valueOf(tablename);
        Admin admin = connection.getAdmin();
        if(admin.tableExists(table)){
            admin.disableTable(table);
            admin.deleteTable(table);
            System.out.println("删除旧表！");
        }
        System.out.println("新的表正在创建中！！！");
//        HTableDescriptor tableDescriptor = new HTableDescriptor(table);
//        tableDescriptor.addFamily(new HColumnDescriptor("info"));
//        admin.createTable(tableDescriptor);
    }
    /**
     * get data from hbase table
     * @param table
     * @throws IOException
     */
    public static void getData(Table table) throws IOException {
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
     * @throws Exception
     */
    @Test
    public   void putData() throws Exception {
        Table table = connection.getTable(TableName.valueOf("txfile_file"));
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
     * @throws Exception
     */
    @Test
    public   void delData() throws Exception {
        Table table = connection.getTable(TableName.valueOf("txfile_file"));
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

}
