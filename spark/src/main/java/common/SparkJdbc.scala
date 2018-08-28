package common

import org.apache.spark.sql.SparkSession
import org.junit.Test

/**
  * Created by Administrator on 2018/6/8.
  */
class SparkJdbc {
    @Test
    def read(): Unit ={
        val spark = SparkSession.builder().appName("jdbc").master("local").getOrCreate()
        val url = "jdbc:mysql://mysqlHost:3306/database"
        val tableName = "table"
        // 设置连接用户&密码
        val prop = new java.util.Properties
        prop.setProperty("user","username")
        prop.setProperty("password","pwd")
        /**
          * 将9月16-12月15三个月的数据取出，按时间分为6个partition
          * 为了减少事例代码，这里的时间都是写死的
          * modified_time 为时间字段
          */
        val predicates =
            Array(
                "2015-09-16" -> "2015-09-30",
                "2015-10-01" -> "2015-10-15",
                "2015-10-16" -> "2015-10-31",
                "2015-11-01" -> "2015-11-14",
                "2015-11-15" -> "2015-11-30",
                "2015-12-01" -> "2015-12-15"
            ).map {
                case (start, end) =>
                    s"cast(modified_time as date) >= date '$start' " + s"AND cast(modified_time as date) <= date '$end'"
            }

        // 取得该表数据
        val jdbcDF = spark.read.jdbc(url,tableName,predicates,prop)
    }
}
