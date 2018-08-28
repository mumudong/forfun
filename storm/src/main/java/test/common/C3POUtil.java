package test.common;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by Administrator on 2018/5/24.
 */
@SuppressWarnings("all")
public class C3POUtil {
    private static ComboPooledDataSource ds;

    //静态初始化块进行初始化
    static{
        try {
            ds = new ComboPooledDataSource();//创建连接池实例
            ds.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");//设置连接池连接数据库所需的驱动
            ds.setJdbcUrl("jdbc:sqlserver://tianxibigdata.database.chinacloudapi.cn:1433;DatabaseName=TianxiBigData");//设置连接数据库的URL
            ds.setUser("tianxi");//设置连接数据库的用户名
            ds.setPassword("1qa2ws!QA");//设置连接数据库的密码
            ds.setMaxPoolSize(4);//设置连接池的最大连接数
            ds.setMinPoolSize(1);//设置连接池的最小连接数
            ds.setInitialPoolSize(2);//设置连接池的初始连接数

            ds.setMaxStatements(100);//设置连接池的缓存Statement的最大数
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //获取与指定数据库的连接
    public static ComboPooledDataSource getInstance(){
        return ds;
    }

    //从连接池返回一个连接
    public static Connection getConnection(){
        Connection conn = null;
        try {
            conn = ds.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    public static boolean insertUpdateDelete(String sql, Object... params) throws Exception{
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        // resultCode执行sql返回的结果值
        int resultCode = -1;
        try {
            preparedStatement = (PreparedStatement) connection
                    .prepareStatement(sql);
            for (int i = 1; i < params.length + 1; i++) {
                // 赋值的时候，索引是从1开始的
                preparedStatement.setObject(i, params[i - 1]);
            }
            resultCode = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            realeaseResource(preparedStatement,connection);
        }
        return resultCode == 1 ? true : false;
    }
    //释放资源
    public static void realeaseResource(PreparedStatement ps,Connection conn){
//        if(null != rs){
//            try {
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }

        if(null != ps){
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception{
        Connection conn = getConnection();
        PreparedStatement pst = null;
        ResultSetMetaData rsd = null;
        try{
            System.out.println("连接数据库成功");
            pst = conn.prepareStatement("select * from CreditRating where 1=2");
            rsd = pst.executeQuery().getMetaData();
            for(int i = 0; i < rsd.getColumnCount(); i++) {
                System.out.print("java类型："+rsd.getColumnClassName(i + 1));
                System.out.print("  数据库类型:"+rsd.getColumnTypeName(i + 1));
                System.out.print("  字段名称:"+rsd.getColumnName(i + 1));
                System.out.print("  字段长度:"+rsd.getColumnDisplaySize(i + 1));
                System.out.println();
            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.print("连接失败");
        }finally {
            realeaseResource(pst,conn);
        }

    }

}
