package datastream

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.api.scala._

case class Order(user:Long,product:String,amount:Int)

object SqlDemo {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

//    sqlDemo(env,tableEnv)
    tableDemo(env,tableEnv)

  }

  def sqlDemo(env:StreamExecutionEnvironment,tableEnv:StreamTableEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val orderA = env.fromElements(Array(
      Order(1L,"beer",3),
      Order(1L,"diaper",4),
      Order(3L,"rubber",2)):_*)
    val orderB = env.fromCollection(Seq(
      Order(2L, "pen", 3),
      Order(2L, "rubber", 3),
      Order(4L, "beer", 1)))
    val tableA = tableEnv.fromDataStream(orderA,'user,'product,'amount)
    tableEnv.registerDataStream("orderB",orderB)
    val result = tableEnv.sqlQuery(s"select * from $tableA where amount > 2 union all select * from orderB where amount < 2")

    result.toAppendStream[(Long,String,Int)].print()
    env.execute("sql demo")
  }

  def tableDemo(env:StreamExecutionEnvironment,tableEnv:StreamTableEnvironment):Unit = {
    import org.apache.flink.api.scala._
    val orderA = env.fromElements(Array(
      Order(1L,"beer",3),
      Order(1L,"diaper",4),
      Order(3L,"rubber",2)):_*).toTable(tableEnv)
    val orderB = env.fromCollection(Seq(
      Order(2L, "pen", 3),
      Order(2L, "rubber", 3),
      Order(4L, "beer", 1))).toTable(tableEnv)
    //    val orderCA = env.fromElements(Array(
    //      Order(1L,"beer",3),
    //      Order(1L,"diaper",4),
    //      Order(3L,"rubber",2)):_*).toTable(tableEnv,'_1)
    val result = orderA.unionAll(orderB)
      .groupBy('user)
      .select('user,'amount.sum as 'amount)
//      .select('user,'product,'amount)
//       .toAppendStream[Order]
      .toRetractStream[(Long,Int)]
    result.print()
    env.execute("sql test")
  }
}
