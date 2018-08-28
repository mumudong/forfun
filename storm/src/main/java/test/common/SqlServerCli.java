package test.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2018/5/23.
 */
public class SqlServerCli {
    private static LinkedList<Connection> listConnections = new LinkedList<Connection>();
    static{
        try {
            String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            String url = "jdbc:sqlserver://tianxibigdata.database.chinacloudapi.cn:1433;DatabaseName=TianxiBigData";
            String username = "tianxi";
            String password = "1qa2ws!QA";
            //数据库连接池的初始化连接数大小
            int jdbcPoolInitSize =1;
            //加载数据库驱动
            Class.forName(driver);
            for (int i = 0; i < jdbcPoolInitSize; i++) {
                Connection conn = DriverManager.getConnection(url, username, password);

                System.out.println("获取到了链接" + conn);
                //将获取到的数据库连接加入到listConnections集合中，listConnections集合此时就是一个存放了数据库连接的连接池
                listConnections.add(conn);
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws Exception {
        if (listConnections.size()>0) {
            //从listConnections集合中获取一个数据库连接
            final Connection conn = listConnections.removeFirst();
            System.out.println("listConnections数据库连接池剩余可连接数---->" + listConnections.size());
            //返回Connection对象的代理对象
            return (Connection) Proxy.newProxyInstance(SqlServerCli.class.getClassLoader(), conn.getClass().getInterfaces(), new InvocationHandler(){
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if(!method.getName().equals("close")){
                        return method.invoke(conn, args);
                    }else{
                        //如果调用的是Connection对象的close方法，就把conn还给数据库连接池
                        listConnections.add(conn);
                        System.out.println(conn + "被还给listConnections数据库连接池了！！");
                        System.out.println("listConnections数据库连接池大小为" + listConnections.size());
                        return null;
                    }
                }
            });
        }else {
            System.out.println("对不起，数据库忙,等待1s");
            Thread.sleep(1000l);
            return getConnection();
        }
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
            close(connection, preparedStatement);
        }
        return resultCode == 1 ? true : false;
    }

    public static <T> List<T> select(String sql, Class<T> classname, Object... params) throws Exception {
        // 获取数据库连接
        Connection connection = getConnection();
        System.out.println(classname);
        // 查询结果集
        List<T> objectList = new ArrayList<T>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            // 执行
            preparedStatement = (PreparedStatement) connection
                    .prepareStatement(sql);
            // 如果有查询条件
            if (params != null) {
                for (int i = 1; i < params.length + 1; i++) {
                    preparedStatement.setObject(i, params[i - 1]);
                }
            }
            resultSet = preparedStatement.executeQuery();
            // 要查的列表数量
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                // 构建一个对象实例

                T beanObject = classname.newInstance();

                for (int i = 1; i <= columnCount; i++) {
                    // 获取查询的字段名称
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    // 获取查询的字段的值
                    String value = resultSet.getString(i);
                    // 给对象属性赋值
                    Field field = classname.getDeclaredField(columnName);
                    field.setAccessible(true);
                    field.set(beanObject,value);
                }
                objectList.add(beanObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            resultSet.close();
            close(connection,preparedStatement);
        }

        return objectList;

    }

    public static <T> T selectOne(String sql, Class<T> classname, Object... params) throws Exception {
        // 获取数据库连接
        System.out.println(classname.getSimpleName());
        Connection connection = getConnection();
        // 查询结果集
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            // 执行
            preparedStatement = (PreparedStatement) connection
                    .prepareStatement(sql);
            // 如果有查询条件
            if (params != null) {
                for (int i = 1; i < params.length + 1; i++) {
                    preparedStatement.setObject(i, params[i - 1]);
                }
            }
            resultSet = preparedStatement.executeQuery();
            // 要查的列表数量
            int columnCount = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                // 构建一个对象实例
                T beanObject = null;
                if(classname.getSimpleName().equals("Integer"))
                    beanObject = classname.getDeclaredConstructor(String.class).newInstance("0");
                else
                    beanObject = classname.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    // 获取查询的字段名称
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    // 获取查询的字段的值
                    String value = resultSet.getString(i);
                    // 给对象属性赋值
                    Field field = null;
                    if(classname.getSimpleName().equals("Integer")){
                        field = classname.getDeclaredField("value");
                        field.setAccessible(true);
                        field.set(beanObject,Integer.valueOf(value));
                    } else{
                        field = classname.getDeclaredField(columnName);
                        field.setAccessible(true);
                        field.set(beanObject,value);
                    }
                }
                return beanObject;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            resultSet.close();
            close(connection,preparedStatement);
        }
        return null;
    }

    public static void close(Connection connection,
                             PreparedStatement preparedStatement) {
        try {
            preparedStatement.close();
            connection.close();
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
             close(conn,pst);
        }

    }
}
