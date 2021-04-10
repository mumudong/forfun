package javalearn.rp;

/**
 * 响应式编程（reactive programming）是一种基于数据流（data stream）和变化传递（propagation of change）
 * 的声明式（declarative）的编程范式。
 *
 * 典型例子：电子表格,定义工时c1=a1+b1,c1会随a1和b1的值而变化
 *
 * 命令式编程：c=a+b,先计算a+b,然后赋给c,之后c不会随a、b的值而变化
 * 响应式编程：购物车物品的加减抽象为一个个事件,事件包括商品对应的价格数量等属性
 *         添加购物车的操作抽象为数据流,流中的数据为购物车的变化
 *         声明式：lamda表达式声明购物车价格的计算逻辑
 *           sum = 0
 *           stream.map(x -> sum+= x.price)
 *
 *
 *
 */
public class RPDemo {
}
