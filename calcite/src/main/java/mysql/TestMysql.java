package mysql;

import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static jdk.nashorn.internal.objects.Global.print;

public class TestMysql {
    public static void main(String[] args) throws Exception{
        //创建Calcite Connection对象
        Class.forName("org.apache.calcite.jdbc.Driver");
        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        Connection connection =
                DriverManager.getConnection("jdbc:calcite:", info);
        CalciteConnection calciteConnection =
                connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
//创建Mysql的数据源schema
        Class.forName("com.mysql.jdbc.Driver");
        DataSource dataSource = JdbcSchema.dataSource("jdbc:mysql://localhost","com.mysql.jdbc.Driver","username","password");
        Schema schema = JdbcSchema.create(rootSchema, "hr", dataSource,
                null, "name");
        rootSchema.add("hr", schema);
//执行查询
        Statement statement = calciteConnection.createStatement();
        ResultSet resultSet = statement.executeQuery(
                "select d.deptno, min(e.empid)\n"
                        + "from hr.emps as e\n"
                        + "join hr.depts as d\n"
                        + "  on e.deptno = d.deptno\n"
                        + "group by d.deptno\n"
                        + "having count(*) > 1");
        print(resultSet);
        resultSet.close();
        statement.close();
        connection.close();
    }
}
