package dataset

import org.apache.flink.api.common.functions.{FlatJoinFunction, JoinFunction}
import org.apache.flink.api.java.aggregation.Aggregations
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.util.Collector

import scala.util.Random

object DeltaIterDemo {
  def main(args: Array[String]): Unit = {
    val env:ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    iter(env)

      deltaIter(env)


//    datastream需要执行,dataset不需要执行
//    env.execute("iter pi example")

  }

  /**
    * 全量迭代计算pi
    * @param env 运行环境
    */
  def iter(env:ExecutionEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val iterNum = 100000
    val random = Random
    val iterData = env.fromElements(0).iterate(iterNum) {
      data: DataSet[Int] =>   data.map(value => {
          val x = random.nextDouble()
          val y = random.nextDouble()
          value + (if (x * x + y * y <= 1) 1 else 0)
        })
      }

    val pi = iterData map (x => x * 1.0 / iterNum * 4)
    pi.print()
  }

  /**
    * 增量迭代
    * 计算连接图中一个顶点可以连接到的最小的顶点
    * 1 - 2
    *    / \
    *   3 - 4
    *
    *      5
    *     / \
    *    6 - 7
    * @param env 运行环境
    */
  def deltaIter(env:ExecutionEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val iterNum = 1
    val vertix = env.fromElements(1L,2L,3L,4L,5L,6L,7L)
    val edgs = env.fromElements(
      Tuple2(1L, 2L),
      Tuple2(2L, 3L),
      Tuple2(2L, 4L),
      Tuple2(3L, 4L),
      Tuple2(5L, 6L),
      Tuple2(5L, 7L),
      Tuple2(6L, 7L)
    )
    //单向边转双向边
    edgs.flatMap( x => {
      Array(x,(x._2,x._1))
    })
    val initialWorkset = vertix.map(x => (x,x))
    val initialSolutionSet = vertix.map(x => (x,x))

    var runtime = iterNum
    val result = initialSolutionSet.iterateDelta(initialWorkset,iterNum,Array(0)){
      (solution,workset) => {
        val changes = workset.join(edgs).where(0).equalTo(0).apply((x,y) => (y._2,x._2))
          .groupBy(0).aggregate(Aggregations.MIN,1)
          .join(solution).where(0).equalTo(0).apply(new FlatJoinFunction[(Long,Long),(Long,Long),(Long,Long)] {
          override def join(first: (Long, Long), second: (Long, Long), out: Collector[(Long, Long)]): Unit = {
            if(first._2 < second._2)
              out.collect(first)
          }
        })
        println(s"执行次数 ${runtime}")
        (changes,changes)
      }

    }
    result.print()
  }


}
