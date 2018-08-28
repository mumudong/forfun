package graphx

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Administrator on 2018/3/20.
  */
class Test{}
case class Ve(id:Int,count:Int)
object Test {
    def main(args: Array[String]): Unit = {

        val list = List((1,1),(2,2),(3,3),(4,4),(5,5),(6,6))
        val conf = new SparkConf().setAppName("test").setMaster("local[2]")
        val sc = new SparkContext(conf)
        val sqlContext = new SQLContext(sc)
        val li = sc.parallelize(list,1)
        def max(a:(Int,Int),b:(Int,Int)):(Int,Int) = {
            println(a,b)
            if(a._2 > b._2) a else b
        }

        import sqlContext.implicits._
        val df = li.map(x => Ve(x._1,x._2)).toDF()
        df.printSchema()



    }
}
