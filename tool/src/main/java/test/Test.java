package test;

import com.alibaba.fastjson.JSON;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws Exception{
        String str = "".intern();
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        long nanos = TimeUnit.SECONDS.toNanos(2);
        System.out.println(nanos);
        lock.lock();
        int i = 10;
        try {
            while (i-- > 0) {
                if (nanos <= 0L)
                    System.out.println("等待超时");
                nanos = condition.awaitNanos(nanos);
                System.out.println("剩余时间:" + nanos);
            }
            // ...
        } finally {
            lock.unlock();
        }

    }

    class Test2 extends Test {

    }
}