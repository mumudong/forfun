package trident.state;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/6/29.
 */
public class JDBCUtil {
    private String driver;
    private String url;
    private String username;
    private String password;
    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;
    public JDBCUtil(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        init();
    }
    void init(){
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public boolean insert(String sql,Object... params) {
        int state = 0;
        try {
            connection = DriverManager.getConnection(url, username, password);
            ps = connection.prepareStatement(sql);
            int i = 0;
            for(Object param:params){
                ps.setObject(++i,param);
            }
            state = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (state>0) {
            return true;
        }
        return false;
    }

    public Bean queryForMap(String sql,Object... param) {
        Bean bean = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            ps = connection.prepareStatement(sql);
            int i = 0;
            for(Object obj:param) {
                ps.setObject(++i,obj);
            }
            rs = ps.executeQuery();
            if(rs.next()){
                bean = new Bean();
                bean.setInserttime(rs.getTimestamp("inserttime"));
                bean.setKey(rs.getString("key"));
                bean.setValue(rs.getInt("value"));
                bean.setPreValue(rs.getInt("preValue"));
                bean.setTxid(rs.getLong("txid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                ps.close();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return bean;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
