package feature

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.sql.{Row, SparkSession}
import org.junit.Test

/**
  * Created by Administrator on 2018/6/8.
  */
class StatsExample {
    @Test
    def stats()={
        val data = Array( (85 ,45),
                          (15 ,55))
        val spark = SparkSession.builder().
                                appName("stats").
                                master("local").
                                getOrCreate()
        import spark.implicits._
//        implicit val mapEncoder = org.apache.spark.sql.Encoders.kryo[Double]
//        implicit val mapEncoder2 = org.apache.spark.sql.Encoders.kryo[Int]
        val df = spark.createDataFrame(data).toDF("women","man")
        val dfDouble = df.map(x=>Array(x.getInt(0).toDouble,x.getInt(1).toDouble)).rdd
        val dat2 = dfDouble.map{case x:Array[Double]  => Vectors.dense(x)}
        val dat = dat2

        val stat = Statistics.colStats(dat)
        println("max ----> " + stat.max +
            "\nmin ----> " + stat.min +
            "\nmean ----> " + stat.mean +
            "\nvariance ----> " + stat.variance +
            "\nnormL1 ----> " + stat.normL1 +
            "\nnormL2 ----> " + stat.normL2  )
        println("pearson ----> " + Statistics.corr(dat,"pearson") +
                "\nspearman ----> " + Statistics.corr(dat,"spearman")  +
                "\nchitest ----> " + Statistics.chiSqTest(Vectors.dense(dat.collect().flatMap(x=>Array(x(0)))),
                                                          Vectors.dense(dat.collect().flatMap(x=>Array(x(1))))))
//        val a = dat2.select("women").rdd.flatMap(x=>Array(x.getDouble(0))).collect()
//        println(a)
        df.printSchema()



    }

}
