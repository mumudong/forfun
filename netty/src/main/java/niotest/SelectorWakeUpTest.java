package niotest;

import java.nio.channels.Selector;

/**
 *  执行 strace -f -e write java SelectorWakeUpTest
 *  查看管道输出，发现wakeup会触发管道输出，使select poll阻塞可以返回
 */
public class SelectorWakeUpTest {
    public static void main(String[] args) throws Exception{
        Selector selector = Selector.open();
        selector.wakeup();
        selector.selectNow();
        selector.wakeup();
        selector.selectNow();
        selector.wakeup();
    }
}
