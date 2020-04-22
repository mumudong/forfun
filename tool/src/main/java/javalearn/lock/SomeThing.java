package javalearn.lock;

import java.util.ArrayList;
import java.util.List;

public class SomeThing {
    private Buffer mBuf = new Buffer();

    public void produce() {
        synchronized (this) {
            while (mBuf.isFull()) {
                try {
                    System.out.println("生产线程等待:" + Thread.currentThread().getName());
                    wait();
                    System.out.println("被唤醒");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("生产线程生产:" + Thread.currentThread().getName());
            mBuf.add();
            notifyAll();
        }
    }

    public void consume() {
        synchronized (this) {
            while (mBuf.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mBuf.remove();
            notifyAll();
        }
    }

    private class Buffer {
        private static final int MAX_CAPACITY = 1;
        private List innerList = new ArrayList<>(MAX_CAPACITY);

        void add() {
            if (isFull()) {
                throw new IndexOutOfBoundsException();
            } else {
                innerList.add(new Object());
            }
            System.out.println(Thread.currentThread().toString() + " add");

        }

        void remove() {
            if (isEmpty()) {
                throw new IndexOutOfBoundsException();
            } else {
                innerList.remove(MAX_CAPACITY - 1);
            }
            System.out.println(Thread.currentThread().toString() + " remove");
        }

        boolean isEmpty() {
            return innerList.isEmpty();
        }

        boolean isFull() {
            return innerList.size() == MAX_CAPACITY;
        }
    }

    public static void main(String[] args) {
        SomeThing sth = new SomeThing();
        Runnable runProduce = new Runnable() {
            int count = 40;

            @Override
            public void run() {
                while (count-- > 0) {
                    sth.produce();
                }
            }
        };
        Runnable runConsume = new Runnable() {
            int count = 40;

            @Override
            public void run() {
                while (count-- > 0) {
                    sth.consume();
                }
            }
        };
        for (int i = 0; i < 40; i++) {
            new Thread(runConsume).start();
        }
        for (int i = 0; i < 40; i++) {
            new Thread(runProduce).start();
        }
    }
}