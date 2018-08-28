package kylin;

import org.apache.kylin.jdbc.Driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 2018/7/26.
 */
public class KylinJdbc {
    public static void main(String[] args) throws Exception {
        Driver driver = (Driver) Class.forName("org.apache.kylin.jdbc.Driver").newInstance();

        Properties info = new Properties();
        info.put("user", "ADMIN");
        info.put("password", "KYLIN");
        Connection conn = driver.connect("jdbc:kylin://hadoop-7:7070/tx_demo", info);
        Statement state = conn.createStatement();
        ResultSet resultSet = state.executeQuery("select count(*) from tpcds_view.web_sales_view;");

        while (resultSet.next()) {
            System.out.println(resultSet.getInt(1));
        }
    }
}
