package javalearn.multi_thread.future;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by Mu on 2017/10/12.
 */
@SuppressWarnings("all")
public class FutureCompletionService {
    //运行结果为最先结束的线程结果先被处理：
    public static void main(String[] args) {
        try {
            completionServiceCount();
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 使用completionService收集callable结果
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void completionServiceCount() throws  Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(
                executorService);
        int threadNum = 5;
        for (int i = 0; i < threadNum; i++) {
            completionService.submit(getTask(i));
        }
        getResult(executorService,completionService,threadNum);
        doOtherThings();
    }

    public static Callable<Integer> getTask(final int no) {
        final Random rand = new Random();
        Callable<Integer> task = new Callable<Integer>() {
            public Integer call() throws Exception {
                int time = rand.nextInt(100)*100;
                Thread.sleep(time);
                System.out.println("sleep thead:"+ no +" time is : "+time);
                return no;
            }
        };
        return task;
    }
    /**
     * other things
     */
    public static void doOtherThings() {
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
     * 从CompletionService中获取线程结果，打印结果
     *
     * @param  executorService completionService threadNum
     */
    public static void getResult(final ExecutorService executorService,final CompletionService<Integer> completionService, final int threadNum) throws Exception{
        executorService.execute(new Runnable() {
            public void run() {
                int sum = 0;
                int temp = 0;
                for(int i=0;i<threadNum;i++){
                    try {
                        temp = completionService.take().get();
                    } catch ( Exception e) {
                        e.printStackTrace();
                    }
                    sum += temp;
                    System.out.print(temp + "\t");
                }
                System.out.println("CompletionService all is : " + sum);
                executorService.shutdown();
            }
        });
    }
}
