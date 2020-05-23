package driver.test;
import com.alibaba.fastjson.JSONObject;
import java.sql.*;
public class Test {
    public static void main(String[] args) throws Exception {
        Class.forName("driver.test.Driver");
        String confPath = Test.class.getClassLoader().getResource("").getPath().substring(1);
        System.out.println(confPath);
        Connection connection = DriverManager.getConnection("jdbc:json:_123_"+confPath);
        Statement statement = connection.createStatement();
        ResultSet resultSet = resultSet = statement.executeQuery(
                "select \"user\".\"uid\" from \"resources\".\"user\" ");
        printResultSet(resultSet);
        resultSet = statement.executeQuery(
                "select * from \"resources\".\"order\" ");
        printResultSet(resultSet);
        resultSet = statement.executeQuery(
                "select * from \"resources\".\"user\" inner join \"resources\".\"order\"  on \"user\".\"uid\" = \"order\".\"uid\"");
        printResultSet(resultSet);
    }
    public static void printResultSet(ResultSet resultSet) throws SQLException {
        while(resultSet.next()){
            JSONObject jo = new JSONObject();
            int n = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= n; i++) {
                jo.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
            }
            System.out.println(jo.toJSONString());
        }
    }
}
