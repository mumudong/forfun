package scala


class Outer{
  trait Inner
  def y = new Inner {}
  def foo(x:this.Inner) = null //参数必须是outer对象的内部类
  def bar(x:Outer#Inner) = null //参数不限定是当前outer对象的内部类

}

object ClassTypeTest {
  def main(args: Array[String]): Unit = {
    val x = new Outer
    val y = new Outer
    println(s"x.y -> ${x.y}")
    println(s"y.y -> ${y.y}")

    x.foo(x.y)
//    x.foo(y.y)
    x.bar(y.y)


    ClassTypeTest.closeResource(System.in)
  }

  //结构化类型
  type Resource = {
    def close() : Unit
  }

  def closeResource(r:Resource) = r.close()

}
