package javalearn.multi_thread.exception;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MyUncaughtExeceptionHandler implements Thread.UncaughtExceptionHandler {
    /**
     * 在线程因未捕获的异常而临近死亡时被调用
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("get Exception --> " + e);
    }
}

/**
 * 定义线程工厂，将任务赋给线程，并绑定异常处理
 */
class HandlerThreadFactory implements ThreadFactory{
    @Override
    public Thread newThread(Runnable r) {
        System.out.println(this + " creating new Thread ...");
        Thread thread = new Thread(r);
        System.out.println("created " + thread);
        thread.setUncaughtExceptionHandler(new MyUncaughtExeceptionHandler());
        System.out.println("exceptionHandler --> " + thread.getUncaughtExceptionHandler());
        return thread;
    }
}

/**
 * 模拟任务抛出异常
 */
class ExceptionRunnable implements Runnable{
    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        System.out.println("run() by " + thread);
        System.out.println("exceptionHandler --> " + thread.getUncaughtExceptionHandler());
        throw new RuntimeException("这是ExceptionThread模拟异常");
    }
}

class MultiThreadExceptionUncaughtExceptionHandler{
    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool(new HandlerThreadFactory());
        service.execute(new ExceptionRunnable());
        service.shutdownNow();
    }
}