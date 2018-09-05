package mumudong.core

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

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
object StateTestTwo {
    def main(args: Array[String]): Unit = {
        System.setProperty("HADOOP_USER_NAME","hdfs")
        val sparkConf = new SparkConf().setMaster("local[2]")
                                        .setAppName(getClass.getSimpleName)
        val ssc = new StreamingContext(sparkConf,Seconds(5))
        ssc.checkpoint("/test/mapstate")
        val lines = ssc.socketTextStream("hadoop-7",19999)
        lines.flatMap(_.split(" "))
                 .map(word => (word,1))
                .updateStateByKey { (curValues: Seq[Int], preValue: Option[Int]) =>
                    val curValueSum = curValues.sum
                    Some(curValueSum + preValue.getOrElse(0))
                }.print() //会保留所有数据的累计值
        ssc.start()
        ssc.awaitTermination()
        ssc.stop()

    }
}
