package mumudong.sql

import java.util.Properties
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, Encoders, SaveMode, SparkSession}
import org.junit.{After, Before, Test}

/**
  * ━━━━━━神兽出没━━━━━━
  * 　　　┏┓　　　┏┓
  * 　　┏┛┻━━━┛┻┓
  * 　　┃　　　　　　　┃
  * 　　┃　　　━　　　┃
  * 　　┃　┳┛　┗┳　┃
  * 　　┃　　　　　　　┃
  * 　　┃　　　┻　　　┃
  * 　　┃　　　　　　　┃
  * 　　┗━┓　　　┏━┛
  * 　　　　┃　　　┃             神兽保佑, 永无BUG!
  * 　　　　┃　　　┃           Code is far away from bug
  * 　　　　┃　　　┗━━━┓   with the animal protecting
  * 　　　　┃　　　　　　　┣┓
  * 　　　　┃　　　　　　　┏┛
  * 　　　　┗┓┓┏━┳┓┏┛
  * 　　　　　┃┫┫　┃┫┫
  * 　　　　　┗┻┛　┗┻┛
  * ━━━━━━感觉萌萌哒━━━━━━
  */
case class Record(key:Int,value:String)
class SQL {
    var spark:SparkSession = _
    @Before
    def init = {
//        spark = SparkSession
//            .builder()
//            .master("local[*]")
//            .appName(s"$this.getClass.getSimpleName")
//            //                    .config("","")
//            .getOrCreate()
    }
    @Test
    def jdbcSql: Unit = {
                spark = SparkSession
                    .builder()
                    .master("local[*]")
                    .appName(s"$this.getClass.getSimpleName")
                    //                    .config("","")
                    .getOrCreate()
        val jdbcDF = spark.read
            .format("jdbc")
            .option("url", "jdbc:mysql://hadoop-7:3306/test2?characterEncoding=UTF-8")
            .option("dbtable", "wan2")
            .option("user", "root")
            .option("password", "123456")
            .load()
        // global_temp.table
        // jdbcDF.createGlobalTempView("")
        val connectionPorperties = new Properties()
        connectionPorperties.put("user","root")
        connectionPorperties.put("password","123456")
        connectionPorperties.put("customSchema","name STRING")

        jdbcDF.printSchema()
        jdbcDF.write.mode((SaveMode.Append))
            .jdbc("jdbc:mysql://hadoop-7:3306/test2?characterEncoding=UTF-8","wan",connectionPorperties)
        spark.close()
    }
    @Test
    def rddRelation = {
        val session = SparkSession
            .builder()
            .master("local[*]")
            .appName(s"$this.getClass.getSimpleName")
            .getOrCreate()
        import session.implicits._
        val df = session.createDataFrame( (1 to 100).map( x => Record(x,s"val_$x")))
        df.createOrReplaceTempView("records")
        session.sql("select * from records").collect().foreach(println)

        val count = session.sql("select count(*) from records").collect().head.getLong(0)
        println(s"count(*):$count")

        df.where($"key" === 1).orderBy($"value".asc).select($"key",$"value").collect().foreach(println)
        df.write.mode(SaveMode.Overwrite).parquet("pair.parquet")

        val parquetFile = session.read.parquet("pair.parquet")
        parquetFile.where($"key" === 1).select($"value".as("a")).collect().foreach(println)
        parquetFile.createOrReplaceTempView("parquetFile")
        session.sql("select * from parquetFile").collect().foreach(println)
        session.close()
    }
    @Test
    def agg = {
        val session = SparkSession
            .builder()
            .master("local[*]")
            .appName(s"$this.getClass.getSimpleName")
            .getOrCreate()
        import session.implicits._
        val ds = session.read.json("data/employees.json").as[Employee]
        ds.show()

        val averageSalary = MyAverage.toColumn.name("average_salary")
        val result = ds.select(averageSalary)
        result.show()

        ds.createTempView("employees")
        session.sql("select sum(salary)/count(salary) from employees").collect().foreach(println)
        session.close()
    }
    @Test
    def hive = {
        System.setProperty("HADOOP_USER_NAME","hdfs")
        val session = SparkSession
            .builder()
            .master("local[2]")
            .appName(getClass.getSimpleName)
            .enableHiveSupport()
            .getOrCreate()
        import session.implicits._
        session.sql("show databases").show()
        session.sql("select * from tt").show(false)
        println("+++++++++++++++++++++++")
        session.sql("use tx_test")
        val df = session.table("test88")
        df.write.mode(SaveMode.Append).saveAsTable("test88_spark")
        session.sql("select * from test88_spark")
        //整合msql数据
        val jdbcDF = session.read
            .format("jdbc")
            .option("url", "jdbc:mysql://hadoop-7:3306/test2?characterEncoding=UTF-8")
            .option("dbtable", "wan2")
            .option("user", "root")
            .option("password", "123456")
            .load()

        jdbcDF.show(false)
        jdbcDF.createTempView("tmp1")
//        session.sql("select * from tmp1").show(false)

        session.sqlContext.setConf("hive.exec.dynamic.partition","true")
        session.sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")
//        df.write.mode(SaveMode.Append).insertInto("test88_spark3")
        session.sql("insert overwrite table tx_test.test88_spark3 partition(id) select sc,id from tmp1")
        session.sql("select * from test88_spark3").show(false)

        session.close()
    }

    @After
    def end = {
//        spark.close()
    }

}
case class Employee(name:String,salary:Long)
case class Average(var sum:Long,var count:Long)
object MyAverage extends Aggregator[Employee,Average,Double]{
    override def zero: Average = Average(0L,0L)

    override def reduce(b: Average, a: Employee): Average = {
        b.sum += a.salary
        b.count += 1
        b
    }

    override def merge(b1: Average, b2: Average): Average = {
        b1.sum += b2.sum
        b1.count += b2.count
        b1
    }

    override def finish(reduction: Average): Double = reduction.sum.toDouble / reduction.count.toDouble

    override def bufferEncoder: Encoder[Average] = Encoders.product


    override def outputEncoder: Encoder[Double] = Encoders.scalaDouble
}
