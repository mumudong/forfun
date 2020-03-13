package test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BitTest {

    public static void main(String[] args) {
        test();
    }
    public static void test() {
        TesT t = new TesT();
        String testMsg = t.msg;
        t.msg = "test...";
        System.out.println(testMsg);
    }
}
class TesT{
    String msg;
}
