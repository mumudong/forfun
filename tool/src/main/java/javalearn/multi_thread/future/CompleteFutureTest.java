package javalearn.multi_thread.future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sun.misc.Version.println;

public class CompleteFutureTest {
    public static void main(String[] args) throws Exception{
        CompletableFuture<String> one = new CompletableFuture<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.execute(() -> {
            try {
                Thread.sleep(3000);
                one.complete("one执行结果");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // WhenComplete 方法返回的 CompletableFuture 仍然是原来的 CompletableFuture 计算结果
        CompletableFuture<String> two = one.whenComplete((s,ex) -> {
            System.out.println("异步执行完毕时执行结果:" + s);
        });

        // ThenApply 方法返回的是一个新的 completeFuture.
        CompletableFuture<Integer> completableFutureThree = two.thenApply(s -> {
            System.out.println("当异步任务执行结束时, 根据上一次的异步任务结果, 继续开始一个新的异步任务!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return s.length();
        });

        completableFutureThree.thenCompose((x)->{
            return CompletableFuture.runAsync(()->{System.out.println(x);});
        });

        System.out.println("阻塞方式获取执行结果:" + completableFutureThree.get());

        pool.shutdown();






    }
}
