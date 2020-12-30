package test;

import java.io.InputStream;
import java.net.URL;

public class TestOne {
    public static void main(String[] args) {
        URL aaaa = TestOne.class.getClass().getClassLoader().getResource("aaaa");
        System.out.println(aaaa.getPath());
    }
}
