package datastream

import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.util.Random

object IterDemo {
  private final val Bound = 100
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    fibonacci(env)
  }

  def fibonacci(env:StreamExecutionEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val inputStream = env.addSource(new SourceFunction[(Int,Int)]{
      val rand = Random
      var counter = 0
      @volatile var isRunning = true
      override def run(ctx: SourceFunction.SourceContext[(Int, Int)]): Unit = {
        while(isRunning && counter < Bound){
          val first = rand.nextInt(Bound / 2 - 1) + 1
          val second = rand.nextInt(Bound / 2 - 1) + 1
          ctx.collect((first,second))
          counter += 1
          Thread.sleep(100L)
        }
      }

      override def cancel(): Unit = isRunning = false
    })

    def withinBound(value:(Int,Int)) = value._1 < Bound && value._2 < Bound

    val numbers = inputStream.map( x => (x._1,x._2,x._1,x._2,0))
                             .iterate((data:DataStream[(Int,Int,Int,Int,Int)]) => {
                               //partial solution计算斐波那契
                               val step = data.map(x => (x._1,x._2,x._4,x._3 + x._4,x._5 + 1))
                               //feedback为空则该条数据迭代结束
                               step.map(x => println(x._1 + ":" + x._2 + "  -  " + x._3 + "---" + x._4))
                               val feedback = step.filter(x => withinBound(x._3,x._4))
                               val output = step.filter(x => !withinBound(x._3,x._4))
                                                .map(x => ((x._1,x._2),x._5))
                               (feedback,output)
                             },500L) //output超过500毫秒无数据,停止迭代
    numbers.print("-->")
    env.execute("fibonacci iter")
  }
}
