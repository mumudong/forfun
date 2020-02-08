package classloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by Administrator on 2018/9/6.
 */
public class MyClassLoader extends URLClassLoader {
    public MyClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if ("classloader.Message".equals(name)){
            return findClass(name);
        }
        return super.loadClass(name);
    }


}
