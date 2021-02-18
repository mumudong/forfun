package javalearn.uri;

import java.net.URI;
import java.net.URL;

public class UriDemo {
    public static void main(String[] args)throws Exception {
        testURI();
        testURL();
    }

    static void testURI() throws Exception{
        URI uri = new URI("https://www.test.com:8080/goods/index.html?username=dgh&passwd=123#j2se");
        //scheme             : https
        System.out.println("scheme             : " + uri.getScheme());
        //SchemeSpecificPart : //www.test.com:8080/goods/index.html?username=dgh&passwd=123
        System.out.println("SchemeSpecificPart : " + uri.getSchemeSpecificPart());
        //Authority          : www.test.com:8080
        System.out.println("Authority          : " + uri.getAuthority());
        System.out.println("host               : " + uri.getHost());
        System.out.println("port               : " + uri.getPort());
        //path               : /goods/index.html
        System.out.println("path               : " + uri.getPath());
        //query              : username=dgh&passwd=123
        System.out.println("query              : "  + uri.getQuery());
        //fragment页面内资源定位   : j2se
        System.out.println("fragment           : " + uri.getFragment());

    }

    static void testURL() throws Exception{
        URL url = new URL("https://www.test.com:8080/goods/index.html?username=dgh&passwd=123#j2se");
        System.out.println("URL：                  " + url.toString());
        System.out.println("protocol：        " + url.getProtocol());
        System.out.println("authority：      " + url.getAuthority());
        System.out.println("file name：      " + url.getFile());
        System.out.println("host：                " + url.getHost());
        System.out.println("path：                " + url.getPath());
        System.out.println("port：                " + url.getPort());
        System.out.println("default port：" + url.getDefaultPort());
        System.out.println("query：              " + url.getQuery());
        System.out.println("ref：                  " + url.getRef());
    }
}
