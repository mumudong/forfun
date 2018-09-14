package lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2018/6/15.
 */
public class DistributeLock {
    static int n = 100;
    public  void curator() throws Exception {
        //创建zookeeper的客户端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("hadoop-5:2181", retryPolicy);
        client.start();
        //创建分布式锁, 锁空间的根节点路径为/curator/lock
        InterProcessMutex mutex = new InterProcessMutex(client, "/curator/lock");
        mutex.acquire();
        //获得了锁, 进行业务流程
        System.out.println("Enter mutex");
        //完成业务流程, 释放锁
        mutex.release();
        //关闭客户端
        client.close();
    }
    public static void main(String[] args){
        Runnable runnable = new Runnable() {
            public void run() {
                DistributeLockZK lock = null;
                Logger logger = LoggerFactory.getLogger(getClass());
                try {
                    lock = new DistributeLockZK("hadoop-5:2181,hadoop-6:2181,hadoop-7:2181", "test1");
                    try {
                        Thread.sleep(2000l);//zookeeper建立连接比较慢，线程睡一会儿等等它
                    } catch (InterruptedException e) {
                    }
                    lock.lock();
                    logger.error(--n + "");
                    logger.error(Thread.currentThread().getName() + "正在运行");
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        };
        // 10个线程使用同一把锁
        for (int i = 0; i < 4; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}
