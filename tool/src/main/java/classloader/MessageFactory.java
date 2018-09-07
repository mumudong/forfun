package classloader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by Administrator on 2018/9/6.
 */
public class MessageFactory {
    public static IMessage newInstance() throws Exception{
        URLClassLoader loader = new MyClassLoader(new URL[]{getClassPath()});
        return (IMessage)loader.loadClass("classloader.Message").newInstance();
    }


    public static URL getClassPath(){
        String resName = MessageFactory.class.getName().replace('.','/') + ".class";
        System.out.println("resName ----> " + resName);
        String loc = MessageFactory.class.getClassLoader().getResource(resName).toExternalForm();
        System.out.println("loc ----> " + loc);
        URL url = null;
        try {
            url = new URL(loc.substring(0,loc.length() - resName.length()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }
}
