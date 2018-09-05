package mumudong.sql

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

/***
 *                     /88888888888888888888888888\
 *                     |88888888888888888888888888/
 *                      |~~____~~~~~~~~~"""""""""|
 *                     / \_________/"""""""""""""\
 *                    /  |              \         \
 *                   /   |  88    88     \         \
 *                  /    |  88    88      \         \
 *                 /    /                  \        |
 *                /     |   ________        \       |
 *                \     |   \______/        /       |
 *     /"\         \     \____________     /        |
 *     | |__________\_        |  |        /        /
 *   /""""\           \_------'  '-------/       --
 *   \____/,___________\                 -------/
 *   ------*            |                    \
 *     ||               |                     \
 *     ||               |                 ^    \
 *     ||               |                | \    \
 *     ||               |                |  \    \
 *     ||               |                |   \    \
 *     \|              /                /"""\/    /
 *        -------------                |    |    /
 *        |\--_                        \____/___/
 *        |   |\-_                       |
 *        |   |   \_                     |
 *        |   |     \                    |
 *        |   |      \_                  |
 *        |   |        ----___           |
 *        |   |               \----------|
 *        /   |                     |     ----------""\
 *   /"\--"--_|                     |               |  \
 *   |_______/                      \______________/    )
 *                                                 \___/
  * */
object SqlStreamingTest {
    def main(args: Array[String]): Unit = {
        val sparkConf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
        val ssc = new StreamingContext(sparkConf,Seconds(5))
        val lines = ssc.socketTextStream("hadoop-7",19999,StorageLevel.MEMORY_AND_DISK_SER)
        val words = lines.flatMap(_.split(" "))
        words.foreachRDD{
            (rdd:RDD[String],time:Time) =>
                val spark = SparkSessionSingleton.getInstance(sparkConf)
                import spark.implicits._
                val wordsDataFrame =  rdd.map( x => Rec(x)).toDF()
                wordsDataFrame.createOrReplaceTempView("words")
                val wordCountsDataFrame = spark.sql("select word,count(*) as total from words group by word")
                println(s"=======$time=======")
                wordCountsDataFrame.show() //每次只有本批数据，历史数据不累计
        }
        ssc.start()
        ssc.awaitTermination()
    }
}
case class Rec(word:String)
object SparkSessionSingleton{
    @transient private var instance:SparkSession = _
    def getInstance(sparkConf:SparkConf):SparkSession = {
        if(instance == null){
            instance = SparkSession.builder().config(sparkConf).getOrCreate()
        }
        instance
    }
}
