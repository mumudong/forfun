package datastream

import java.util.concurrent.TimeUnit

import org.apache.flink.streaming.api.functions.source.{ParallelSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala.async.ResultFuture
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, StreamExecutionEnvironment}

import scala.concurrent.{ExecutionContext, Future}

/**
  * 有序输出模式下的 AsyncIO 会需要缓存数据，且这些数据会被写入 checkpoint，因此在内容资源方面的得分会低一点。
  * 另一方面，同步数据库查找关联的吞吐量问题得到解决
  * 仍不可避免地有数据库负载高和结果不确定两个问题(因为时间可能乱序了)
  *
  * 预加载维表数据:对数据库压力持续较短时间,但拷贝整个表压力较大
  *        优势: 运行期间不用访问数据库
  *        劣势: 但是维表数据不能更新,且对taskManager压力较大
  */
object AsyncDemo {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    import org.apache.flink.api.scala._
    val input:DataStream[Int] = env.addSource(new SimpleSource())

//    val asyncMapped = AsyncDataStream.orderedWait(input,10000,TimeUnit.MICROSECONDS,10){
//      (input,collector:ResultFuture[Int]) => {
//        Future{collector.complete(Seq(input))}(ExecutionContext.global)
//      }
//    }

     val asyncMapped = AsyncDataStream.unorderedWait(input,10000,TimeUnit.MICROSECONDS,10){
       (input,collector:ResultFuture[Int]) => {
         Future{collector.complete(Seq(input))}(ExecutionContext.global)
       }
     }


    asyncMapped.print()
    env.execute("Async I/O job")

  }
}

class SimpleSource extends ParallelSourceFunction[Int]{
  var running = true
  var counter = 0

  override def run(ctx: SourceFunction.SourceContext[Int]): Unit = {
    while(running){
      ctx.getCheckpointLock.synchronized{
        ctx.collect(counter)
      }
      counter += 1
    }
  }

  override def cancel(): Unit = {
    running = false
  }
}
