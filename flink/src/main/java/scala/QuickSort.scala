package scala

object QuickSort {

  def main(args: Array[String]): Unit = {
    testListMatch()

  }

  /**
    * == (同equals ,同时避免null异常)
    * eq(引用是否相同,ne反义)
    *
    * <% 视界:   ordered接口有<函数,int也有<函数, 此处使用 T <% Ordered[T]标识 Int类型可以视作Ordered接口
    * 协变 +T: 类型放大至祖先
    * 逆变 -T: 类型缩小至子孙
    * A <: B  a是b的子类
    * A >: B  a是b的超类
    *
    * A =:= B A必须是B类型
    * A <:< B 表示 A 必须是B的子类型 (类似于简单类型约束 <:)
    * A <%< B 表示 A 必须是可视化为 B类型, 可能通过隐式转换 (类似与简单类型约束 <%)
    *
    */
  def qsort[T <% Ordered[T]] (list:List[T]):List[T] = {
    list match {
      case Nil => Nil
      case ::(head, tl) => {
        val (before, after) = tl.partition(x => x.< {head})
        qsort(before).++(qsort(after).::(head))
      }
    }
  }

  def testListMatch(): Unit ={
    val list = List(1,2,3,4,5)
    list match {
      case Nil => Nil
//      case ::(head,tail) => {
//        println(head) // 1
//        println(tail) // List(2,3,4,5)
//      }
      case x::y => {
        println(x) // 1
        println(y) // List(2,3,4,5)
      }
    }
  }


}
