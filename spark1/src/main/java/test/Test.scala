package test

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext

object Test{
    def main(args: Array[String]): Unit = {
        val conf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[2]")
        val sc = new SparkContext(conf)
        val hiveContext = new HiveContext(sc)
//        hiveContext.setConf("yarn.timeline-service.enabled","false")
        hiveContext.sql("show databases").groupBy("dt").count().orderBy().take(1)
        sc.stop()
    }

}