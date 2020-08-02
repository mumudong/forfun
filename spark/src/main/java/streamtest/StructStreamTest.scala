package streamtest

import org.apache.spark.sql.SparkSession

object StructStreamTest {
  def main(args: Array[String]): Unit = {
    test1()
  }

  def test1():Unit ={
    val spark = SparkSession.builder().master("local[*]").appName("struct-test").getOrCreate()
    val lines = spark.readStream.format("socket")
      .option("host","mu2")
      .option("port",9999)
      .load()
    import spark.implicits._
    val words = lines.as[String].flatMap(_.split(" ")).groupBy("value").count()
    val query = words.writeStream.outputMode("append").format("console").start()
    query.awaitTermination()
  }



}
