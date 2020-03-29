package classloader;

/**
 * Created by Administrator on 2018/9/6.
 */
public class Main {
    private static IMessage message1;

    public static void main(String[] args) throws Exception{
        while (true){
            message1 = MessageFactory.newInstance().copy(message1);
            System.out.println(message1.message() + " ---- " + message1.plus());
            System.out.println(message1.message() + " ---- " + message1.getClass());

            Thread.sleep(4000l);
        }
    }
}
