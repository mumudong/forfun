package javalearn.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamTwo {
    public static void main(String[] args) {
        //中间操作符，有状态、无状态
        //终结操作符，短路、非短路
        // referencePipeline/sink等链式操作

    }

    /**
     * 并行使用的forkJoin框架，该示例是错误的使用方式,因为多线程操作list不安全
     */
    static void testWrongParallel(){
        List<Integer> list = new ArrayList<>();
        IntStream.range(0,1000).boxed().parallel().filter(x -> x % 2 == 1).forEach(list::add);
        //很可能会小于500，因为线程不安全
        System.out.println(list.size());
    }
    static void testRightParallel(){
        List<Integer> collect = IntStream.range(0, 1000).boxed().parallel().filter(x -> x % 2 == 1).collect(Collectors.toList());
        //线程安全
        System.out.println(collect.size());
    }
}
