package mumudong.core

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, State, StateSpec, StreamingContext}
import org.junit.Test

/**
  *        ┏┓　　　┏┓+ +
  *　　　┏┛┻━━━┛┻┓ + +
  *　　　┃　　　　　　　┃ 　
  *　　　┃　　　━　　　┃ ++ + + +
  *　　 ████━████ ┃+
  *　　　┃　　　　　　　┃ +
  *　　　┃　　　┻　　　┃
  *　　　┃　　　　　　　┃ + +
  *　　　┗━┓　　　┏━┛
  *　　　　　┃　　　┃　　　　　　　　　　　
  *　　　　　┃　　　┃ + + + +
  *　　　　　┃　　　┃　　　　Codes are far away from bugs with the animal protecting　　　
  *　　　　　┃　　　┃ + 　　　　          神兽保佑,代码无bug　　
  *　　　　　┃　　　┃
  *　　　　　┃　　　┃　　+　　　　　　　　　
  *　　　　　┃　 　　┗━━━┓ + +
  *　　　　　┃ 　　　　　　　┣┓
  *　　　　　┃ 　　　　　　　┏┛
  *　　　　　┗┓┓┏━┳┓┏┛ + + + +
  *　　　　　　┃┫┫　┃┫┫
  *　　　　　　┗┻┛　┗┻┛+ + + +
  */
object StateTest {
    def main(args: Array[String]): Unit = {
        System.setProperty("HADOOP_USER_NAME","hdfs")
        val ssc = StreamingContext.getOrCreate("/test/mapstate3",()=>mapstate)
        ssc.start()
        ssc.awaitTermination()
    }

    def mapstate:StreamingContext = {
        val sparkConf = new SparkConf().setMaster("local[2]").setAppName("stateful mapwithstate test")
        val ssc = new StreamingContext(sparkConf,Seconds(5))
        ssc.checkpoint("/test/mapstate3")
        val initialRDD = ssc.sparkContext.parallelize(List(("hello",1),("word",1)))
        val lines = ssc.socketTextStream("hadoop-7",19999)
        val words = lines.flatMap(_.split(" "))
        val wordDstream = words.map(x => (x,1))
        val mappingFunc = (word:String,one:Option[Int],state:State[Int]) => {
            val sum = one.getOrElse(0) + state.getOption().getOrElse(0)
            val output = (word,sum)
            state.update(sum)
            output
        }
        val stateDstream = wordDstream.mapWithState(
            StateSpec.function(mappingFunc).initialState(initialRDD) )
        stateDstream.print()//每次打印只显示本次更新的数据，可使用foreachrdd存入mysql等
//        ssc.start()
//        ssc.awaitTermination()
        ssc
    }
}
