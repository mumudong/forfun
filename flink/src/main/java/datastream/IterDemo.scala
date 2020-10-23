package datastream

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.datastream.DataStreamSource
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.util.Random
import util.control.Breaks._
case class People(name:String,age:Int,sex:String)
object IterDemo {
  private final val Bound = 200
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    fibonacci(env)
  }

//  def iterTest(env:StreamExecutionEnvironment):Unit = {
//    import org.apache.flink.api.scala._
//    val inputStream:DataStreamSource[People] = env.addSource(new SourceFunction[People]{
//      val rand = Random
//      var counter = 0
//      @volatile var isRunning = true
//      override def run(ctx: SourceFunction.SourceContext[People]): Unit = {
//        while(isRunning && counter < Bound){
//          ctx.collect(People("张三",20,"male"))
//          Thread.sleep(2000L)
//        }
//      }
//      override def cancel(): Unit = isRunning = false
//    })
//    val ite = inputStream.iterate()
//    val feedback = ite.filter()
//  }

  def fibonacci(env:StreamExecutionEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val inputStream = env.addSource(new SourceFunction[(Int,Int)]{
      val rand = Random
      var counter = 0
      @volatile var isRunning = true
      override def run(ctx: SourceFunction.SourceContext[(Int, Int)]): Unit = {
        while(isRunning && counter < Bound){
          val first = 1 + counter * 10
          val second = 2 + counter * 10
          println(s"生成input  first:$first,second:$second ${System.currentTimeMillis()}")
          ctx.collect((first,second))
          counter += 1
          Thread.sleep(3000L)
        }
      }
      override def cancel(): Unit = isRunning = false
    })

    def withinBound(value:(Int,Int)) = value._1 < Bound && value._2 < Bound
    val numbers = inputStream.map( x => (x._1,x._2,x._1,x._2,0))
    val iterNum = numbers.iterate((data:DataStream[(Int,Int,Int,Int,Int)]) => {
                               //partial solution计算斐波那契
                               val step = data.map(x => (x._1,x._2,x._4,x._3 + x._4,x._5 + 1))
                               //feedback为空则该条数据迭代结束
                               step.map(x => println(x._1 + ":" + x._2 + "  -  " + x._3 + "---" + x._4+ "," + System.currentTimeMillis()))
                               val feedback = step.filter(x => withinBound(x._3,x._4))
                                feedback.map(x => println("feedback : " + x._1 + ":" + x._2 + "  -  " + x._3 + "---" + x._4 + "," + System.currentTimeMillis()))
                               val output = step.filter(x => !withinBound(x._3,x._4))
                                                .map(x => ((x._1,x._2),x._5))
                               (feedback,output)
                             }) //output超过500毫秒无数据,停止迭代
    iterNum.print("iterNum --> ")
    env.execute("fibonacci iter")
  }
}
