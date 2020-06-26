package scala

/**
  * Created by Administrator on 2018/6/20.
  */
object SwimingType{
    def learned(rb:String) = println("兔子会游泳了" + rb)
}
object Swiming{
    implicit def learningType(s:String) =  SwimingType
}

class Implic
object Implic{
    //隐式参数,不传参数时会查找上下文中的隐式变量
    def person(implicit name:String) = name
    def testPerson={
        implicit val p = "implicit会搜索作用域内的隐式值作为参数"
//        implicit  val p2 = "多个同类型参数的时候隐式参数会失败"
        println(person)
    }

    def foo(msg:String) = println(msg)
    implicit def intToString(x:Int) = x.toString
    def testFoo()={
        foo(10)
    }

    def testMethodNotExist(): Unit ={
        import Swiming._
        val rabbit = new String
        rabbit.learned(" bala bala 字符串也会游泳了")
    }



    def main(args: Array[String]): Unit = {
        testPerson
        testFoo
        testMethodNotExist

    }
}

