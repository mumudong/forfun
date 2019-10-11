package multi_thread;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiThreadAndDequeTest {
    public static void main(String[] args) throws Exception {

        String s = "1||36016020000000010156|0|J50080000|0";
        System.out.println(s.split("\\|").length);
        /*Map<String,Map<String,Set<String>>> result = new ConcurrentHashMap<>();
        String dt = "2019";
        ExecutorService service = Executors.newFixedThreadPool(4);
        String jobName = "aaa";
        Lock lock = new ReentrantLock();
        for(int i = 0;i < 4;i++) {
            final int j = i;
            service.execute(() -> {
                try {
                    lock.lock();
                    result.putIfAbsent(dt, new ConcurrentHashMap<>());
                    result.get(dt).putIfAbsent(jobName, Collections.synchronizedSet(new HashSet<>()));
                    result.get(dt).get(jobName).add(j+"");
                    System.out.println(result.get(dt).get(jobName).size());
                } finally {
                    lock.unlock();
                }
            });
*/
        }
    }

    class Test {
        public synchronized void test(Boolean isTrue) {
            System.out.println("测试方法");
        }
    }
