package datastream

import mumu.WordCountData
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

class StreamDemo {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    /** 数据重新分布策略=================================================================== */
//    repartitionTest(env)



  }

  def repartitionTest(env:StreamExecutionEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val data = env.fromElements(WordCountData.WORDS:_*)
    //shuffle为random.nextInt(并行度)
    //rebalance为循环找下一个位移,保持数据分布平衡
    data.rebalance
    data.shuffle
    //发送给下游第一个算子
    data.global
    //如何上游并行度2,下游并行度4,则上游每个并行度对应2个下游且保持rebalance
    data.rescale
    //数据发送到所有下游
    data.broadcast
    //上下游保持同样的分区
    data.forward
  }
}
