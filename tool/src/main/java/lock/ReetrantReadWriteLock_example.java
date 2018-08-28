package lock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Administrator on 2018/3/22.
 */
public class ReetrantReadWriteLock_example {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    public static void main(String[] args){
        final ReetrantReadWriteLock_example lock = new ReetrantReadWriteLock_example();
        ExecutorService service = Executors.newCachedThreadPool();
        service.execute(new Runnable() {
            public void run() {
                lock.readFile(Thread.currentThread());
            }
        });
        service.execute(new Runnable() {
            public void run() {
                lock.readFile(Thread.currentThread());
            }
        });
        service.execute(new Runnable() {
            public void run() {
                lock.readFile(Thread.currentThread());
            }
        });
        service.execute(new Runnable() {
            public void run() {
                lock.writeFile(Thread.currentThread());
            }
        });
        service.execute(new Runnable() {
            public void run() {
                lock.writeFile(Thread.currentThread());
            }
        });

        service.shutdown();
    }
    public void readFile(Thread thread){
        lock.readLock().lock();
        boolean writeLock = lock.isWriteLocked();
        if(!writeLock){
            System.out.println("当前为读锁");
        }
        try{
            for(int i=0;i<5;i++){
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                System.out.println(thread.getName()+":正在进行读取操作！");
            }
            System.out.println(thread.getName()+":读取操作执行完毕！");
        }finally {
            System.out.println("释放读锁！");
            lock.readLock().unlock();
        }
    }
    public void writeFile(Thread thread){
        lock.writeLock().lock();
        boolean writeLock = lock.isWriteLocked();
        if(writeLock){
            System.out.println("当前为写锁!");
        }
        try{
            for(int i=0;i<5;i++){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                System.out.println(thread.getName()+":正在进行写操作！");
            }
            System.out.println(thread.getName()+":写操作执行完毕！");
        }finally {
            System.out.println("释放写锁!");
            lock.writeLock().unlock();
        }
    }
}
