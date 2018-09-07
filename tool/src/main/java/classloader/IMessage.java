package classloader;

/**
 * Created by Administrator on 2018/9/6.
 */
public interface IMessage {
    String message();
    int plus();
    int counter();
    IMessage copy(IMessage message);

}
