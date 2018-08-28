package common;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.spark.SparkFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by MuDong on 2017/8/24.
 */
@SuppressWarnings("all")
public class JDBCHelper {
    private   static JDBCHelper jdbcHelper;
    private  BasicDataSource ds;

    private JDBCHelper(){    }
    private JDBCHelper(Boolean isLocal){
        Properties properties=new Properties();
        InputStream in = null;
        try {
            if(isLocal)
                in = JDBCHelper.class.getClassLoader().getResourceAsStream("dbcp.properties");
            else
                in = null;
//                in = new FileInputStream(new File(SparkFiles.get("dbcp.properties")));
//            properties.load(in);
            properties.setProperty("url","jdbc:postgresql://10.167.202.177:5432/crawler-hx");
            properties.setProperty("username","TXDB");
            properties.setProperty("password","123456");
//            properties.setProperty("classDriver","org.postgresql.Driver");
            properties.setProperty("initialSize","5");
            properties.setProperty("maxActive","8");
            properties.setProperty("maxWait","3000");
            properties.setProperty("driverClassName","org.postgresql.Driver");
            System.out.println(properties.getProperty("username"));
            //创建连接池对象，并且用连接池工厂来加载配置对象的信息
            ds = (BasicDataSource) BasicDataSourceFactory.createDataSource(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }


    }
    //获取JDBCHelper对象
    public static JDBCHelper getJDBCHelper(Boolean isLocal){
        if (jdbcHelper == null) {
            synchronized (JDBCHelper.class) {
                if (jdbcHelper == null) {
                    jdbcHelper = new JDBCHelper(isLocal);
                }
            }
        }
        return jdbcHelper;
    }

    /**
     * 用于查询，返回结果集
     * @param sql sql语句
     * @return 结果集
     * @throws SQLException
     */
    public  List<BaiduNewsResult> query(String sql ) throws SQLException {
        List<BaiduNewsResult> list = new ArrayList<BaiduNewsResult>();
        Connection conn=null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            conn=ds.getConnection();
            preparedStatement=conn.prepareStatement(sql);
            rs = preparedStatement.executeQuery();

            while (rs.next()) {
                BaiduNewsResult bns=new BaiduNewsResult();
                bns.setContent(rs.getString("content"));
                bns.setId(rs.getLong("id"));
                bns.setCreatetime(rs.getTimestamp("createtime"));
                bns.setIntro(rs.getString("intro"));
                bns.setSource(rs.getString("source"));
                bns.setTitle(rs.getString("title"));
                bns.setKeyword(rs.getString("keyword"));
                bns.setTime(rs.getString("time"));
                bns.setUrl(rs.getString("url"));
                bns.setWebsite(rs.getString("website"));

                list.add(bns);
            }
            return list;
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            if(rs!=null)
                rs.close();
            if(preparedStatement!=null )
                preparedStatement.clearParameters();
            if(conn!=null)
                conn.close();
        }
    }
    /**
     * 用于存储结果集
     * @param sql sql语句
     * @return 结果集
     * @throws
     */
    public  <T> Integer save(String sql,List<T> lis ) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);
            Savepoint sp = conn.setSavepoint();
            if (lis.size() != 0 && lis.get(0) instanceof BaiduNewsResult) {
                List<BaiduNewsResult> list = (List<BaiduNewsResult>) lis;
                preparedStatement = conn.prepareStatement(sql);
                int i = 0;
                for (BaiduNewsResult news : list) {
                    i++;
                    preparedStatement.setLong(1, news.getId());
                    preparedStatement.setString(2, news.getTitle());
                    preparedStatement.setString(3, news.getContent());
                    preparedStatement.setString(4, news.getIntro());
                    preparedStatement.setString(5, news.getLabel());
                    preparedStatement.setString(6, format.format(news.getCreatetime()));
                    preparedStatement.setString(7, news.getTime());
                    preparedStatement.setString(8, news.getSource());
                    preparedStatement.setString(9, news.getKeyword());
                    preparedStatement.setString(10, news.getWebsite());
                    preparedStatement.setString(11, news.getUrl());
                    preparedStatement.addBatch();
                    if (i % 50 == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                    }
                }
            }
            preparedStatement.executeBatch();
            preparedStatement.clearBatch();
            conn.commit();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println("请注意---:更新数据库失败了！！！");
            return -1;
        } finally {

            if (preparedStatement != null)
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }

    }
    /**
     * 用于存储结果集
     * @param sql sql语句
     * @return 结果集
     * @throws
     */
    public  <T> Integer saveOne(String sql,String lis ) {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = ds.getConnection();
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, lis);
            preparedStatement.setString(2, "11"); //双11代表出错了
            preparedStatement.execute();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            System.out.println("请注意---:更新数据库失败了！！！");
            return -1;
        } finally {

            if (preparedStatement != null)
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            if (conn != null)
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }

    }
    public static void main(String[] args) throws SQLException {
        System.out.println(getJDBCHelper(false).query("select * from \"public\".baidu_news_predict  where label='0.0';"));
    }
}


