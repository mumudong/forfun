package scala

import scala.collection.immutable.HashMap

class Point2(var x:Int,var y:Int,z:String) extends Equals{

  def getZ():String = z

  def move(mx : Int, my:Int) : Unit = {
    x += mx
    y += my
  }

  override def canEqual(that: Any): Boolean = that match {
    case p:Point2 => true
    case _ => false
  }

  override def hashCode(): Int = y + 31 * x

  override def equals(obj: Any): Boolean = {
    def strictEquals(other:Point2) = this.x == other.x && this.y == other.y

    obj match {
      case a: AnyRef if this eq a => true
      case b: Point2 => {
        println(s"case b:point2 b -> ${b.getZ()} , this -> ${this.getZ()}")
        (b canEqual (this)) && strictEquals(b)
      }
      case _ => false
    }
  }



}

object ImuteTest {
  def main(args: Array[String]): Unit = {
    val x = new Point2(1,1,"x")
    val y = new Point2(1,2,"y")
    val z = new Point2(1,1,"z")
    val refEq = x eq z//比较地址
    println(s"refEq x eq z  $refEq")
    val xz = x == z  //调用equals
    println(s"x == z  $xz")
    val xy = x == y
    println(s"x == y   $xy")
    val yx = y == x
    println(s"x == y   $yx")
    var mp = HashMap(x -> "value_x",y -> "value_y")
    mp+=(new Point2(2,3,"xx") -> "新增")
    println(s"mp.size --> ${mp.size}")
    println(s"mp(x) --> ${mp(x)}")
    println(s"mp(z) --> ${mp(z)}")
    println(s"mp.contains(z) --> ${mp.contains(z)}")
    mp.foreach(x => println(s"move之前 ${x._2} -> ${x._1.hashCode()}"))
    x.move(1,1)
    mp+=(new Point2(10,10,"xxx") -> "最后")
    mp.foreach(x => println(s"move之后 ${x._2} -> ${x._1.hashCode()}"))
    //因为采用triermap,数据结构为数组加hashmap, x move之后数组对应下标变化,不能找到正确的hashmap
    //使用z 的时候可以找到正确的数组下标,但是在hashmap中比较equals时和x不能匹配了
    println(mp.contains(x))
    mp.keys.foreach(x => println(x.hashCode()))

  }
}
