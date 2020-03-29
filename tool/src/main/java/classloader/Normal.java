package classloader;

import java.net.URLClassLoader;

public class Normal extends ClassLoader {

    public static void main(String[] args) {
        System.out.println(getSystemClassLoader());
    }
}
