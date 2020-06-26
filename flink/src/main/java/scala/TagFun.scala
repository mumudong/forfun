package scala


/**
  * Created by Administrator on 2018/6/20.
  */
class TagFun{class TagF}
object TagFun {
    def testClassEraser[T](x:List[T]): Unit ={
        println("=================================================testClassEraser")
        // List[Int]和List[String]是不同的，但jvm运行时会擦出类，都成为了class<Lint>?
        println("TagFun.getClass --> " + TagFun.getClass) //得到子类
        println("classOf[Tagfun] --> " + classOf[TagFun]) //得到正确类
        println("equal --> " + TagFun.getClass == classOf[TagFun])
        println("classOf[List[Int]] --> " + classOf[List[Int] ]) //得到List类
        println("manifest[List[Int]] --> " + manifest[List[Int] ] ) //得到正确类型
    }
    def testTypeTag(): Unit ={
        println("=================================================testTypeTag")
        def m(f:TagFun)(b:f.TagF)(implicit ev:Manifest[f.TagF]) = ev
        val f1 = new TagFun ;val b1 = new f1.TagF
        val f2 = new TagFun; val b2 = new f2.TagF
        println("ev1 --> " + m(f1)(b1))
        println("ev2 --> " + m(f2)(b2))
        println("equal --> " + m(f1)(b1) == m(f2)(b2))
    }

    def testManifest[T](x:List[T])(implicit m:Manifest[T]): Unit ={
        println("=================================================testManifest")
        if(m <:< manifest[String]){
            println("this is List of String")
        }else{
            println("None String list")
        }
    }
    def testManifest2[t:Manifest](x:List[t]): Unit ={
        println("=================================================testManifest2")
        println("this is testManifest2 --> " + manifest[t])
        val a = List(1,2)
        println("a.getClass --> " + a.getClass) //getclass 可以获取单一类型，不能获取复合类型
        println(a.getClass.getClasses)
    }


    def main(args: Array[String]): Unit = {
        testClassEraser(List(1,2))
        testManifest(List("1",2))
        testManifest2(List(1,2))
        testTypeTag
    }
}
