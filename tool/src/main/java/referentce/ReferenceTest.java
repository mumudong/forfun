package referentce;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class ReferenceTest {
    public static void main(String[] args) {
        testSoftReference();
    }

    static void testSoftReference() {
        MyObject obj = new MyObject();
        ReferenceQueue<MyObject> softQueue = new ReferenceQueue<>();
        SoftReference<MyObject> softReference = new SoftReference<MyObject>(obj, softQueue);
        new Thread(() -> {
            Reference<MyObject> o = null;
            try {
                //如果对象被回收,则进入引用队列
                o = (Reference<MyObject>) softQueue.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (o != null) {
                System.out.println("o for softReference is " + o.get());
            }
        }).start();
        obj = null;
        System.gc();
        System.out.println("after gc:soft get = " + softReference.get());
        System.out.println("分配大块内存");
        byte[] b = new byte[4 * 1024 * 700];
        System.out.println("after new byte[]:soft get = " + softReference.get());

    }
}

class MyObject {
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("myObject finalize call");
    }

    @Override
    public String toString() {
        return " this is MyObject ";
    }
}