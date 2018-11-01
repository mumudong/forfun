package multi_thread.exception;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 多线程异常捕获，try catch捕获不到
 * 可以在runnable的run方法中捕获
 * 可以callable获取结果后的时候，future.take()  try catch捕获
 */
public class ThreadException implements Runnable{
    @Override
    public void run() {
        throw new RuntimeException();
    }

    public static void main(String[] args) {
        try{
            ExecutorService service = Executors.newCachedThreadPool();
            service.execute(new ThreadException());
        }catch (RuntimeException e){
            System.out.println("this is a runtime exception!");
        }
    }
}
