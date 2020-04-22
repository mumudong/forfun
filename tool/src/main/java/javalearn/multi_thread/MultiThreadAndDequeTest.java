package javalearn.multi_thread;

public class MultiThreadAndDequeTest {
    public static void main(String[] args) throws Exception {

        String s = "1||36016020000000010156|0|J50080000|0";
        System.out.println(s.split("\\|").length);
        /*Map<String,Map<String,Set<String>>> result = new ConcurrentHashMap<>();
        String dt = "2019";
        ExecutorService service = Executors.newFixedThreadPool(4);
        String jobName = "aaa";
        Lock javalearn.lock = new ReentrantLock();
        for(int i = 0;i < 4;i++) {
            final int j = i;
            service.execute(() -> {
                try {
                    javalearn.lock.javalearn.lock();
                    result.putIfAbsent(dt, new ConcurrentHashMap<>());
                    result.get(dt).putIfAbsent(jobName, Collections.synchronizedSet(new HashSet<>()));
                    result.get(dt).get(jobName).add(j+"");
                    System.out.println(result.get(dt).get(jobName).size());
                } finally {
                    javalearn.lock.unlock();
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
