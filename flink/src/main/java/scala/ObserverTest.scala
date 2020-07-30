package scala

import java.io.File

import scala.tools.nsc.interpreter.InputStream

trait Observable{
  type Handler
  var callbacks = Map[Handler,this.type => Unit ]()
  def observe(callback :this.type => Unit) :Handler = {
    val handler = createHandler(callback)
    callbacks += (handler -> callback)
    handler
  }

  def unobserve(handler: Handler):Unit = {
    callbacks -= handler
  }

  protected def createHandler(callback:this.type =>Unit):Handler

  protected def notifyListeners():Unit =
    for(callback <- callbacks.values)
      callback(this)
}

trait DefaultHandlers extends Observable{
  type Handler = (this.type => Unit)

  protected def createHandler(callback:this.type => Unit):Handler = callback
}

class IntStore(private var value:Int) extends Observable with DefaultHandlers{
  def get:Int = value
  def set(newValue:Int):Unit = {
    value = newValue
    notifyListeners()
  }
  override def toString: String = s"IntStore($value)"
}

object ObserverTest {
  def main(args: Array[String]): Unit = {
    val x = new IntStore(1)
    val handler = x.observe(println)
    x.set(2)
    x.unobserve(handler)

    test()
  }

  def test2():Unit = {
    val y:List[_] = List()// _表示存在类型,存在一种类型定义,但并不关心这个具体类型
    val x :List[X forSome {type X}] = y

    val yy:List[_ <: AnyRef] = List()
//    val xx:List[X forSome {type X <: AnyRef}] = y
  }

  /**
    * 使用方式test3(List("string"))(_.isEmpty)
    * scala类型推断器从左到右自动判断方法类型,前一个参数类型可以影响后一个参数的推断结果
    * 如果将方法改为test3[A](col:List[A],f:A=>Boolean)将无法用上面的方式调用,因为无法推断函数类型
    */
  def test3[A](col:List[A])(f:A=>Boolean):Unit = {

  }

  def test():Unit = {
    class A{
      type B >: List[Int]
      def foo(a:B) = a
    }
    val x = new A{type B = Traversable[Int]}
    x.foo(Set(1)) //不报错,因为set是traversable子类
//    val y = new A{type B = Set[Int]} //会报错
  }

  /*类型lambda*/
  def test1():Unit = {
    // M[_]中_ 表示一个未知的存在类型
    def foo[M[_]](f:M[Int]) = f
    type CallBack[T] = Function1[T,Unit]
    val x0 :CallBack[Int] = y=>println(y+2)
    val xx = foo[CallBack](x0)
    val x = foo[({type X[Y] = Function1[Y,Unit]})#X]((x:Int) => println(x))
    x(1)
  }

  def test4():Unit = {
    trait FileLike[T]{
      def children(directory:T):Seq[T]
      def child(parent:T,name:String):T
      def writeContent(file:T,otherContent:InputStream):Unit
      def content(file:T):InputStream
    }
    def synct[F:FileLike,T:FileLike](from:F,to:T):Unit = {
      val fromHelper = implicitly[FileLike[F]]
      val toHelper = implicitly[FileLike[T]]
      def syncFile(file1:F,file2:T):Unit = {
        toHelper.writeContent(file2,fromHelper.content(file1))
      }
    }

    //调用会失败,因为没有隐式参数
//    synct(new File("tmp1"),new File("tmp2"))
  }

}







