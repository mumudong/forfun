package javalearn.jmxdemo.base0;

import java.io.Serializable;

public class Hello implements HelloMBean, Serializable {

    private String name;

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void sayHello() {
        System.out.println("Hello," + name);
    }

}