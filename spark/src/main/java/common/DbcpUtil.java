package common;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by Administrator on 2018/5/24.
 */
@SuppressWarnings("all")
public class DbcpUtil {
    private static DataSource ds;

    //静态初始化块进行初始化
    static{
        try{
            InputStream in=DbcpUtil.class.getClassLoader().getResourceAsStream("dbcp.properties");
            Properties prop=new Properties();
            prop.load(in);//以上与1同
            ds = BasicDataSourceFactory.createDataSource(prop);
            System.out.println(prop.getProperty("url"));
//工厂，创建Source
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //获取与指定数据库的连接
    public static DataSource getInstance(){
        return ds;
    }

    //从连接池返回一个连接
    public static Connection getConnection(){
        Connection conn = null;
        try {
            conn = ds.getConnection();
//            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
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
        ResultSet  rsd = null;
        try{
            System.out.println("连接数据库成功");
            pst =   conn.prepareStatement("insert into te  values(11111111111111111111,22222222222222222222)");
             pst.executeUpdate();
//            pst = conn.prepareStatement("select * from CreditRating where 1=2");
//            rsd = pst.executeQuery().getMetaData();
//            for(int i = 0; i < rsd.getColumnCount(); i++) {
//                System.out.print("java类型："+rsd.getColumnClassName(i + 1));
//                System.out.print("  数据库类型:"+rsd.getColumnTypeName(i + 1));
//                System.out.print("  字段名称:"+rsd.getColumnName(i + 1));
//                System.out.print("  字段长度:"+rsd.getColumnDisplaySize(i + 1));
//                System.out.println();
//            }
        }catch(Exception e){
            e.printStackTrace();
            System.out.print("连接失败");
        }finally {
            realeaseResource(pst,conn);
            if(rsd != null)
                rsd.close();
        }

    }

}
