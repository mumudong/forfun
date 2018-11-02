package multi_thread.future;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Created by Mu on 2017/10/12.
 */
public class FutureTest {
    public static void main(String[] args) {
        playOne();
    }

    public static void playOne(){
        Callable<Integer> callable = new Callable<Integer>() {
            public Integer call() throws Exception {
                int i = 10;
                while(i > 0){
                    i--;
                    System.out.println("this is " + i);
                    Thread.sleep(1000);
                }
                return new Random().nextInt(100);
            }
        };
        FutureTask<Integer> future = new FutureTask<Integer>(callable);
        new Thread(future).start();
        try {
            Thread.sleep(5000);// 可能做一些事情
            System.out.println("谁先结束了----1");
            System.out.println(future.get());//get会阻塞
            System.out.println("谁先结束了----2");
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

}
