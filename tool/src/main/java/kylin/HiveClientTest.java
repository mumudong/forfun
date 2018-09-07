package kylin;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.sql.*;

public class HiveClientTest {
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static String url = "jdbc:hive2://10.167.222.103:10000/default";
    private static String user = "hdfs";
    private static String password = "hdfs";
    private static String sql = "";
    private static ResultSet res;
    private static String tableName="ods.t_gl_dict";
    private static final Logger log = Logger.getLogger(HiveClientTest.class);

    public static void main(String[] args) {
            try {
                    Class.forName(driverName);
                    Connection conn = DriverManager.getConnection(url, user, password);
                    Statement stmt = conn.createStatement();
                    sql = "select * from tx_test.test_k";
                    System.out.println("Executing:" + sql);
                    res = stmt.executeQuery(sql);
                    System.out.println("执行“select * query”运行结果:");
                    while (res.next()) {
                            System.out.println(res.getString(1) + "\t" + res.getString(2));
                    	
                    }
                    conn.close();
                    conn = null;
            } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    log.error(driverName + " not found!", e);
                    System.exit(1);
            } catch (SQLException e) {
                    e.printStackTrace();
                    log.error("Connection error!", e);
                    System.exit(1);
            }

    }
}
