package javalearn.classloader;

/**
 * Created by Administrator on 2018/9/6.
 */
public class Message implements IMessage {
    private int counter;
    @Override
    public String message() {
        return "version 1";
    }

    @Override
    public int plus() {
        return counter++;
    }

    @Override
    public int counter() {
        return counter;
    }

    @Override
    public IMessage copy(IMessage message) {
        if(message != null)
            counter = message.counter();
        return this;
    }
}
