package hdfs;

import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  shutDown 不在接受新的线程，并且等待之前提交的线程都执行完在关闭，
 *  shutDownNow 直接关闭活跃状态的所有的线程 ， 并返回等待中的线程
 */
public class ShutDownTest {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(50);
        ExecutorService executor= new ThreadPoolExecutor(
                5,  //core
                10, //max
                120L,   //2minutes
                TimeUnit.SECONDS,
                queue,
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        for(int i = 0; i < 20; i++){
            executor.execute(new UseThreadPoolExecutor2());
        }
//        Thread.sleep(1000);
        System.out.println("queue size:" + queue.size());
//        Thread.sleep(3000);
        executor.shutdown();
    }

}
class UseThreadPoolExecutor2 implements Runnable {
    private static AtomicInteger count = new AtomicInteger();
    @Override
    public void run() {
        try {
            int temp = count.incrementAndGet();
            System.out.println("任务:" + temp);
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
