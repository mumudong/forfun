package dataset

import java.io.{BufferedReader, File, FileReader}

import mumu.WordCountData
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.common.operators.Order
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration

import scala.util.Random

object DataSetDemo {
  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment


    /** ================================================================================ */
    //分布式缓存
//    cache(env)

    /** range-partition是批算子的优化,会对各分区采样,计算下游分区对应边界====================================== */
    rangeTest(env)





  }

  def rangeTest(env:ExecutionEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val data = env.fromElements(mumu.WordCountData.NUMS:_*).setParallelism(2)
//    val rangeData = data.partitionByRange(x => x._1)
//      .mapPartition(nums => {
//        val p = Random.nextInt(100)
//        nums.map(x => println(s"range$p --> $x"))
//        nums
//      })
//    rangeData.print()

//    val sortData = data.sortPartition(0,Order.DESCENDING)
//      .sortPartition(1,Order.ASCENDING)
//      .mapPartition(nums =>{
//        val p = Random.nextInt(100)
//        nums.map(x => println(s"sort$p -> $x"))
//        nums
//      })
//    sortData.print()
  }

  def cache(env : ExecutionEnvironment):Unit = {
    env.registerCachedFile("hdfs://ns/file","hdfsCacheFile_1")
    env.registerCachedFile("file:///path/file","localFile_1",true)
    import org.apache.flink.api.scala._

    val sentence:DataSet[String] = env.fromElements[String](WordCountData.WORDS:_*)
    val word = sentence.flatMap( x => x.split(" "))

    word.map(new RichMapFunction[String,String] {
      var myFile:File = _
      override def open(parameters: Configuration): Unit = {
        myFile = getRuntimeContext.getDistributedCache.getFile("hdfsCacheFile_1")
        val reader = new BufferedReader(new FileReader(myFile))
        var line: String = null
        while ((line = reader.readLine()) != null){
          println(s"myFile line -> $line")
        }
      }

      override def map(value: String): String = {
        println(s"value -> $value")
        value
      }
    })

    word.print()
  }
}
