package org.apache.calcite.adapter.csv;

import com.alibaba.fastjson.JSONObject;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.util.Sources;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;


/**
 * 调用流程：
 *    1、调用配置中csvSchemaFactory中create方法，生成csvSchema
 *    2、csvSchema根据上一步传入的文件夹和table类型(scantable/filtertable/translatabletable)
 *      构造csvSchema，且csvSchema扫描文件夹，构造tableMap[path,table]
 *    3、table中提供生成记录迭代器方法，使用迭代器遍历记录
 */
public class CsvTest {
    @Test
    public void test() throws Exception{
        Properties info = new Properties();
        //model配置中flavor决定表引擎是scantable，还是filtertable等
        info.put("model", jsonPath("bug"));

        try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
//        try (Connection connection = DriverManager.getConnection("jdbc:calcite:schemaFactory=org.apache.calcite.adapter.csv.CsvSchemaFactory; schema.directory=bug; schema.baseDirectory=E:/mydata/forfun/calcite/target/classes")) {


            CalciteConnection optiqConnection = (CalciteConnection) connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = optiqConnection.getRootSchema();

            final Statement statement = optiqConnection.createStatement();

            // date
             String sql1 = "select EMPNO from \"DATE\"\n"
                    + "where JOINEDAT > {d '2000-01-01'}";
             ResultSet joinedAt = statement.executeQuery(sql1);
            printResultSet(joinedAt);

            sql1 = "select * from ARCHERS";
            joinedAt = statement.executeQuery(sql1);
            printResultSet(joinedAt);
        }
    }

    private String jsonPath(String model) {
        return resourcePath(model + ".json");
    }

    private String resourcePath(String path) {
        return Sources.of(CsvTest.class.getResource("/" + path)).file().getAbsolutePath();
    }

    public static void printResultSet(ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            JSONObject jo = new JSONObject();
            int n = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= n; i++) {
                jo.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
            }
            System.out.println(jo.toJSONString());
        }
    }
}