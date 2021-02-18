package es;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by Administrator on 2018/4/18.
 */
public class ConfReader {
    public static Properties prop ;
    public static String getConfig(String key){
        if(prop == null){   //锁class对其所有对象均起作用
            synchronized (ConfReader.class) {
                if(prop == null) {
                    prop = new Properties();
                    try {
                        // javalearn.classloader.getresouceasstream是从根目录找/
                        // class.getresourceasstream（""）是从当前class目录找
                        //                          （"/"）是从根目录找
                        prop.load(ConfReader.class.getClassLoader().getResourceAsStream("es-config.properties"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return prop.getProperty(key);
    }

    public static void main(String[] args) {
        System.out.println(getConfig("a"));
    }
}
