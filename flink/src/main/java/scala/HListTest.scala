package scala

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.script.Message

object HListTest {
  def main(args: Array[String]): Unit = {
    type X[T <: TBool] = T#If[String,Int,Any]
    val x:X[TTrue] = "Hi" // val x:X[TTrue] = 5会报错,因为TTrue返回类型为第一个参数类型
    test()
    println("testCollection------------------>")
    testCollection()
  }

 //异构list
  def test(): Unit ={
    sealed trait HList

    final case class HCons[H,T <:HList](head:H,tail:T) extends HList{
      def ::[T](v:T) = HCons(v,this)
      override def toString: String = head + " :: " + tail
    }

    final class HNil extends HList{
      def ::[T](v:T) = HCons(v,this)
      override def toString: String = "Nil"
    }

    object HList{
      type ::[H,T <: HList] = HCons[H,T]
      val :: = HCons
      val HNil = new HNil
    }
    import HList._

    val xx:(String::Int::Boolean::HNil) = "Hi" :: 5 :: false :: HNil
    println(xx)
    val one :: two :: three :: HNil = xx
    println(one,two,three)
  }

  def testCollection():Unit = {
    //Stream的from方法构造一个从传入的数字开始的无限递增的流
    val x = List("a","b","c") zip (Stream from 1)
    println(s"x --> ${x}")
    //scala方法名以:结尾,采用右结合的方式
    val s = 1 #:: {println("hi")} #:: {println("bai")} #:: Stream.empty
    println(s"s --> ${s}")
    println(s(0),s(1),s(2))
    println(s"s --> ${s}")

    //斐波那契,如果内存放不下会出问题
    val fibs = {def f(a:Int,b:Int):Stream[Int]=a#::f(b,a+b);f(0,1)}
    {fibs drop 3 take 5 toList}
    println(s"fibs --> ${fibs}")

    //使用view延迟计算的斐波那契(traversable是内部foreach遍历,iterator是外部遍历)
    val fibs2 = new Traversable[Int]{
      override def foreach[U](f: Int => U): Unit = {
        def next(a:Int,b:Int):Unit = {
          f(a)
          next(b,a+b)
        }
        next(0,1)
      }
    } view
    val y = {fibs2 drop 3 take 5 toList}//view延迟计算反复执行开销大,因为没有保留计算过的值
    println(s"fibs2 --> ${fibs2}")

    //可变集合的三个特质,ObservalbeMap,ObservableBuffer,ObservableSet,监听稽核的变化
    object Tx extends ArrayBuffer[Int] with mutable.ObservableBuffer[Int] {
      subscribe(new Sub{
        override def notify(pub: Pub, event: Message[Int] with mutable.Undoable): Unit = {
          Console.println(s"Event:${event} from ${pub}")
        }
      })
    }

    Tx += 1
    Tx -= 1

    //集合默认是严格、串行的,通过view来延迟计算(force前置执行,与view相反)、par来并行计算
    val res1 = (1 to 1000) .par.foldLeft(0)(_+_)//看不出来有没有并行
    val res2 = (1 to 1000).par.foldLeft(Set[String]){
      (set,value) => set + Thread.currentThread().toString();set
    }
    val res3 = (1 to 1000).par map {
      x => Thread.currentThread().toString
    } toSet
    println(s"res1 -> ${res1},res2 -> ${res2},res3 -> ${res3}")
  }
}



//sealed模式匹配时漏掉某模式会有提示
sealed trait TBool{
  type If[TrueType <: Up,FalseType <: Up,Up] <: Up
}
class TTrue extends TBool{
  type If[TrueType <: Up,FalseType <: Up,Up] = TrueType
}
class TFalse extends TBool{
  type If[TrueType <:Up,FalseType <: Up,Up] = FalseType
}