package javalearn.jmxdemo.base;

public interface HelloMBean {
    // operations

    public void sayHello();
    public void sayHello(String userName);
    public int add(int x, int y);

    // attributes

    // a read-only attribute called Name of type String
    public String getName();

    // a read-write attribute called CacheSize of type int
    public int getCacheSize();
    public void setCacheSize(int size);
}
