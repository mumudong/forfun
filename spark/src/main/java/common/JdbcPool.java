package common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * author:Mudong
 */
@SuppressWarnings("all")
public class JdbcPool{
    private static LinkedList<Connection> listConnections = new LinkedList<Connection>();
    private static int count = 0;
    static{
        try {
            String driver = "org.postgresql.Driver";
            String url = "jdbc:postgresql://10.180.100.22:5432/postgres";
            String username = "txdb1";
            String password = "123456";
            //数据库连接池的初始化连接数大小
            int jdbcPoolInitSize =10;
            //加载数据库驱动
            Class.forName(driver);
            for (int i = 0; i < jdbcPoolInitSize; i++) {
                Connection conn = DriverManager.getConnection(url, username, password);

                System.out.println("获取到了链接" + conn);
                //将获取到的数据库连接加入到listConnections集合中，listConnections集合此时就是一个存放了数据库连接的连接池
                listConnections.add(conn);
                count++;
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        //如果数据库连接池中的连接对象的个数大于0
        if(listConnections.size()==0){
            if (count > 40)
                throw new RuntimeException("对不起，数据库忙");
            try {
                String driver = "org.postgresql.Driver";
                String url = "jdbc:postgresql://10.180.100.22:5432/postgres";
                String username = "txdb1";
                String password = "123456";
                //数据库连接池的初始化连接数大小
                int jdbcPoolInitSize =4;
                //加载数据库驱动
                Class.forName(driver);
                for (int i = 0; i < jdbcPoolInitSize; i++) {
                    Connection conn = DriverManager.getConnection(url, username, password);

                    System.out.println("获取到了链接" + conn);
                    //将获取到的数据库连接加入到listConnections集合中，listConnections集合此时就是一个存放了数据库连接的连接池
                    listConnections.add(conn);
                    count++;
                }
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
        if (listConnections.size()>0) {
            //从listConnections集合中获取一个数据库连接
            final Connection conn = listConnections.removeFirst();
            System.out.println("listConnections数据库连接池剩余可连接数---->" + listConnections.size());
            //返回Connection对象的代理对象
            return (Connection) Proxy.newProxyInstance(JdbcPool.class.getClassLoader(), conn.getClass().getInterfaces(), new InvocationHandler(){

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
            throw new RuntimeException("对不起，数据库忙");
        }
    }

    public static boolean insertUpdateDelete(String sql, Object... params) throws SQLException{
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


    }
}