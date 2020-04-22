package javalearn.multi_thread.future;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
/**本代码遇到问题：
 *  list在for 循环遍历时不能remove list中的元素，需要通过iterator来移除元素
 *
 * 单线程是用iter移除没有问题，多线程时线程不安全，会出现错误，因iter与list未共用锁(vector也不可以)
 * 可以换用线程安全的容器来操作，CopyOnWriteArrayList，或ConcurrentHashMap
 *
 */


/**
 * Created by Mu on 2017/10/12.
 */
@SuppressWarnings("all")
public class FutureGetAndIsDone {
    //运行结果打印和future放入列表时的顺序一致，为0，1，2：
    public static void main(String[] args) {
        FutureGetAndIsDone t = new FutureGetAndIsDone();
        List<Future<String>> futureList = new ArrayList<Future<String>>();
        t.generate(3, futureList);
        t.getResult(futureList);
        t.doOtherThings();
    }

    /**
     * 生成指定数量的线程，都放入future数组
     *
     * @param threadNum
     * @param fList
     */
    public void generate(int threadNum, List<Future<String>> fList) {
        ExecutorService service = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < threadNum; i++) {
            Future<String> f = service.submit(getJob(i));
            fList.add(f);
        }
        service.shutdown();
    }

    /**
     * other things
     */
    public void doOtherThings() {
        try {
            for (int i = 0; i < 3; i++) {
                int time = 2;
                Thread.sleep(1000 * 2);
                System.out.println("do thing no:" + i + "大睡了" + time + "秒");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从future中获取线程结果，打印结果
     *
     * @param fList
     */
    public void getResult(List<Future<String>> fList) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(getCollectJob(fList));
        service.shutdown();
    }

    /**
     * 生成指定序号的线程对象
     *
     * @param i
     * @return
     */
    public Callable<String> getJob(final int i) {
        final int time = new Random().nextInt(10);
        return new Callable<String>() {
            public String call() throws Exception {
                Thread.sleep(1000 * time);
                return "thread-" + i + "睡了" + time + "秒";
            }
        };
    }

    /**
     * 生成结果收集线程对象
     *
     * @param fList
     * @return
     */
    public Runnable getCollectJob(final List<Future<String>> fList) {
        return new Runnable() {
            public void run() {
                int i = 0;
                boolean isTrue = true;
                Iterator<Future<String>> iterator = fList.iterator();
                while (isTrue) {
                    iterator = fList.iterator();
                    while (iterator.hasNext()) {
                        Future<String> future = iterator.next();
                        try {
                            if (future.isDone() && !future.isCancelled()) {
                                System.out.println("Future:" + future
                                        + ",Result:" + future.get());
                                iterator.remove();
                                i++;
                            }
                            if(i == 3)
                                isTrue = false;
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
    }

}

