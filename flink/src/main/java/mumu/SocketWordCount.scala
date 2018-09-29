package mumu

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.time.Time

/**
  * Created by Administrator on 2018/9/26.
  */
case class WordWithCount(word:String,count:Long)
object SocketWordCount {
    def main(args: Array[String]): Unit = {
        val port : Int = ParameterTool.fromArgs(args).getInt("port")
        val env : StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
        val text : DataStream[String] = env.socketTextStream("hadoop-7",port,'\n')
        //解析数据，分组，窗口化，并且聚合求sum
        import org.apache.flink.api.scala._

        val windowCounts = text.flatMap{ w => w.split("\\s")}
                                .map{ w => WordWithCount(w,1)}
                                .keyBy("word")
                                .timeWindow(Time.seconds(5),Time.seconds(1))
                                .sum("count")

        windowCounts.print().setParallelism(1)
        env.execute("socket window wordcount")
    }
}
