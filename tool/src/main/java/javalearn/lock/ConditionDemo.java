package javalearn.lock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2018/3/22.
 */
public class ConditionDemo {
    volatile int key = 0;
    Lock lock = new ReentrantLock();
    //condition条件队列
    Condition condition = lock.newCondition();

    public static  void main(String[] args){
        ConditionDemo demo = new ConditionDemo();
        new Thread(demo.new A()).start();
        new Thread(demo.new B()).start();
    }

    class A implements Runnable{
        public void run() {
            int i = 10;
            while(i > 0){
                lock.lock();
                System.out.println("A获取锁 -- "+System.currentTimeMillis());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try{
                    if(key == 1){
                        System.out.println("A is Running");
                        i--;
                        key = 0;
                        condition.signal();//唤醒等待线程

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("A unlock -- "+System.currentTimeMillis());

                    }else{
                        System.out.println("A await -- "+System.currentTimeMillis());
                        condition.awaitUninterruptibly();//等待的时候会释放锁
                    }
                }
                finally{
                    System.out.println("A javalearn.lock unlock() ...");
                    lock.unlock();
                }
            }
        }

    }

    class B implements Runnable{
        public void run() {
            int i = 10;
            while(i > 0){
                lock.lock();
                System.out.println("B获取锁 -- "+System.currentTimeMillis());
                try {
                    Thread.sleep(2000l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try{
                    if(key == 0){
                        System.out.println("B is Running");
                        i--;
                        key = 1;
                        condition.signal();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("B unlock -- "+System.currentTimeMillis());
                    }else{
                        System.out.println("B await -- "+System.currentTimeMillis());
                        condition.awaitUninterruptibly();
                    }
                }
                finally{
                    System.out.println("B javalearn.lock unlock() ...");
                    lock.unlock();
                }
            }
        }
    }
}
