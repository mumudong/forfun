package multi_thread;

public class LambdaThread {
    public static void main(String[] args) {
        new Thread(()->{
            System.out.println(Thread.currentThread() + "正在运行...");
        }).start();
    }
}
