package lock;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/3/22.
 */
public class Cyclicbarrier_Example {
    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        final CyclicBarrier cb = new CyclicBarrier(3);//创建CyclicBarrier对象并设置3个公共屏障点
        for(int i=0;i<3;i++){
            Runnable runnable = new Runnable(){
                public void run(){
                    try {
                        Thread.sleep((long)(Math.random()*10000));
                        System.out.println("线程" + Thread.currentThread().getName() +
                                "即将到达集合地点1，当前已有" + cb.getNumberWaiting() + "个已经到达，正在等候");
                        cb.await();//到此如果没有达到公共屏障点，则该线程处于等待状态，如果达到公共屏障点则所有处于等待的线程都继续往下运行

                        Thread.sleep((long)(Math.random()*10000));
                        System.out.println("线程" + Thread.currentThread().getName() +
                                "即将到达集合地点2，当前已有" + cb.getNumberWaiting() + "个已经到达，正在等候");
                        cb.await();
                        Thread.sleep((long)(Math.random()*10000));
                        System.out.println("线程" + Thread.currentThread().getName() +
                                "即将到达集合地点3，当前已有" + cb.getNumberWaiting() + "个已经到达，正在等候");
                        cb.await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            service.execute(runnable);
        }
        service.shutdown();
    }
}
