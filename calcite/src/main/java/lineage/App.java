package lineage;

import org.apache.calcite.adapter.java.ReflectiveSchema;
import org.apache.calcite.config.Lex;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.RelBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App {
    public static class Employee {
        public final int empid;
        public final String name;
        public final int deptno;

        public Employee(int emp_id, String name, int dept_no) {
            this.empid = emp_id;
            this.name = name;
            this.deptno = dept_no;
        }
    }

    public static class Department {
        public final String name;
        public final int deptno;

        public Department(int dept_no, String name) {
            this.deptno = dept_no;
            this.name = name;
        }
    }

    public static class HrSchema {
        public final Employee[] emps = {
                new Employee(100, "joe", 1),
                new Employee(200, "oliver", 2),
                new Employee(300, "twist", 2),
                new Employee(301, "king", 3),
                new Employee(305, "kelly", 1)
        };

        public final Department[] depts = {
                new Department(1, "dev"),
                new Department(2, "market"),
                new Department(3, "test")
        };
    }

    static void testInMemoryTable() throws Exception {
        Class.forName("org.apache.calcite.jdbc.Driver");
        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        Connection connection =
                DriverManager.getConnection("jdbc:calcite:", info);
        CalciteConnection calciteConnection =
                connection.unwrap(CalciteConnection.class);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Schema schema = new ReflectiveSchema(new HrSchema());
        rootSchema.add("hr", schema);



        Statement statement = calciteConnection.createStatement();

        String sql = "select d.deptno, min(e.empid)\n"
                + "from hr.emps as e\n"
                + "join hr.depts as d\n"
                + "  on e.deptno = d.deptno\n"
                + "group by d.deptno\n"
                + "having count(*) > 1";

//        relTest(rootSchema);
        planTest(rootSchema,sql);

        ResultSet resultSet = statement.executeQuery(sql);

        while(resultSet.next()){
            System.out.println(resultSet.getInt("deptno") + " ," + resultSet.getString(2));
        }
        resultSet.close();
        statement.close();
        connection.close();
    }

    static void relTest(SchemaPlus rootSchema){

        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.configBuilder().setCaseSensitive(false).setLex(Lex.MYSQL).build())
                .defaultSchema(rootSchema)
                .build();
        RelBuilder relBuilder = RelBuilder.create(config);
        RelNode table = relBuilder
                .scan("hr","emps")//schema与table的关系不能使用"schema.table"字符串表示
                .scan("hr","depts")
                .join(JoinRelType.INNER, "deptno")
                .project(relBuilder.field("name"), relBuilder.field("deptno"))
                .build();

        System.out.println(RelOptUtil.toString(table));
    }

    static void planTest(SchemaPlus rootSchema,String sql) throws Exception{

        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .parserConfig(SqlParser.configBuilder().setCaseSensitive(false).setLex(Lex.MYSQL).build())
                .defaultSchema(rootSchema)
                .build();

        Planner planner = Frameworks.getPlanner(config);
        SqlNode sqlNode = planner.parse(sql);
        SqlNode validate = planner.validate(sqlNode);
        RelRoot plan = planner.rel(validate);
        System.out.println(RelOptUtil.toString(plan.rel));
    }

    static void sqlParser() throws Exception{
        String sql = "select d.deptno, min(e.empid)\n"
                + "from hr.emps as e\n"
                + "join hr.depts as d\n"
                + "  on e.deptno = d.deptno\n"
                + "group by d.deptno\n"
                + "having count(*) > 1";
        SqlParser.Config mysqlConfig = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        SqlParser mysqlParser = SqlParser.create(sql,mysqlConfig);
        SqlNode sqlNode = mysqlParser.parseQuery();
        System.out.println(sqlNode.toString());




    }

    public static void main( String[] args ) throws Exception{
        testInMemoryTable();
        sqlParser();
    }
}
