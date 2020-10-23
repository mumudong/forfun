package mumudong.sql

import org.apache.spark.sql.SparkSession

/**
  * Spark 的 Join 策略是在 JoinSelection 类里面实现的
  *  首先判断broadcastHashJoin
  *  其次 shuffleHashJoin
  *  其次 sortMergeJoin
  *  其次 CartesianProduct
  *  其次 broadcastNestedLoopJoin
  */
object JoinStrategy {
  def main(args: Array[String]): Unit = {
//    println("testBroadcastJoin ------------------------------------------------ ")
//    testBroadcastJoin()
    println("testShuffleHashJoin ------------------------------------------------ ")
    testShuffleHashJoin()
  }

  /**
    * 小表broadcast join,避免shuffle
    *    小表大小阈值: spark.sql.autoBroadcastJoinThreshold  默认10m,设为-1表示关闭广播join
    *    只能用等值join,不需要join key可排序
    *    不支持full outer join
    *    Broadcast Hash Join 是在 BroadcastHashJoinExec 类里面实现的。
    */
  def testBroadcastJoin():Unit = {

    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(s"$this.getClass.getSimpleName")
      .getOrCreate()
    import session.implicits._

    val iteblogDF = Seq(
            (0, "https://www.iteblog.com"),
            (1, "iteblog_hadoop"),
            (2, "iteblog")).toDF("id", "info")

    val r = iteblogDF.join(iteblogDF, Seq("id"), "inner")
    r.explain(true)
    r.show(false)

  }

  /**
    * 把大表和小表按相同的join key分区算法和分区数分区，保证hash值一样的数据分在一个分区中,同一个executor中
    * 两张hash值一样的分区即可在本地进行hash join,join前会对小表构建hash map,利用了分治思想
    * 使用条件：1、等值join
    *         2、spark.sql.join.preferSortMergeJoin默认为true,需要设为false
    *         3、小表大小 < 广播大小 * shuffle分区数
    *               即（小表的大小（plan.stats.sizeInBytes）必须小于 spark.sql.autoBroadcastJoinThreshold * spark.sql.shuffle.partitions）
    *           小表大小的3倍必须小于大表的大小
    *         ShuffledHashJoin 的实现在 ShuffledHashJoinExec
    */
  def testShuffleHashJoin():Unit = {

    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(s"$this.getClass.getSimpleName")
      .getOrCreate()
    import session.implicits._

    session.conf.set("spark.sql.autoBroadcastJoinThreshold", 1)
    session.conf.set("spark.sql.join.preferSortMergeJoin", false)
    val iteblogDF1 = Seq( (2, "iteblog") )
                       .toDF("id", "info")
    val iteblogDF2 = Seq(
                        (0, "https://www.iteblog.com"),
                        (1, "iteblog_hadoop"),
                        (2, "iteblog")
                        ).toDF("id", "info")
    val r = iteblogDF1.join(iteblogDF2, Seq("id"), "inner")
    r.explain()
    r.show(false)
  }

  /**
    * 适合大表，对两张表按join的keys使用相同的算法和分区数进行分区，保证相同的key落到相同的分区，分区完成后按
    * join的key进行排序，reduce端获取两张表相同分区的数据进行merge join
    * 使用条件：
    *      等值join，且join key可排序
    *
    * 大表 Join 基本上都可以使用 SortMergeJoin 来实现，对应的类为 SortMergeJoinExec
    * 我们可以对参与 Join 的表按照 Keys 进行 Bucket 来避免 Shuffle Sort Merge Join 的 Shuffle 操作，因为 Bucket 的表事先已经按照 Keys 进行分区排序，
    * 所以做 Shuffle Sort Merge Join 的时候就无需再进行分区和排序了。
    */
  def testShuffleSortMergeJoin():Unit = {

    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(s"$this.getClass.getSimpleName")
      .getOrCreate()
    import session.implicits._

    session.conf.set("spark.sql.autoBroadcastJoinThreshold", 1)

    val iteblogDF1 = Seq(
          (0, "111"),
          (1, "222"),
          (2, "333")
          ).toDF("id", "info")

    val iteblogDF2 = Seq(
          (0, "https://www.iteblog.com"),
          (1, "iteblog_hadoop"),
          (2, "iteblog")
          ).toDF("id", "info")

    val r = iteblogDF1.join(iteblogDF2, Seq("id"), "inner")
    r.explain
    r.show(false)
  }

  /**
    * 不指定join条件，笛卡尔join
    */
  def testCartesianProductJoin():Unit = {
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(s"$this.getClass.getSimpleName")
      .getOrCreate()
    import session.implicits._

    // 因为我们下面测试数据都很小，所以我们先把 BroadcastJoin 关闭
    session.conf.set("spark.sql.autoBroadcastJoinThreshold", 1)
    val iteblogDF1 = Seq(
          (0, "111"),
          (1, "222"),
          (2, "333")
          ).toDF("id", "info")
    val iteblogDF2 = Seq(
          (0, "https://www.iteblog.com"),
          (1, "iteblog_hadoop"),
          (2, "iteblog")
          ).toDF("id", "info")
    // 这里也可以使用 val r = iteblogDF1.crossJoin(iteblogDF2)
    val r = iteblogDF1.join(iteblogDF2, Nil, "inner")
    r.explain
    r.show(false)
  }

  /**
    * 双层循环join
    * 会根据相关条件对小表进行广播，以减少表的扫描次数。触发广播的需要满足以下三个条件之一
    * right outer join 是会广播左表；
    * left outer, left semi, left anti 或者 existence join 时会广播右表；
    * inner join 的时候两张表都会广播。
    * Broadcast nested loop join 支持等值和不等值 Join，支持所有的 Join 类型。
    *
    */
  def testBroadcastNestedLoopJoin():Unit = {
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(s"$this.getClass.getSimpleName")
      .getOrCreate()
    import session.implicits._

    // 因为我们下面测试数据都很小，所以我们先把 BroadcastJoin 关闭
    session.conf.set("spark.sql.autoBroadcastJoinThreshold", 1)
    val iteblogDF1 = Seq(
      (0, "111"),
      (1, "222"),
      (2, "333")
    ).toDF("id", "info")
    val iteblogDF2 = Seq(
      (0, "https://www.iteblog.com"),
      (1, "iteblog_hadoop"),
      (2, "iteblog")
    ).toDF("id", "info")
    // 这里也可以使用 val r = iteblogDF1.crossJoin(iteblogDF2)
    val r = iteblogDF1.join(iteblogDF2, Nil, "leftouter")
    r.explain
    r.show(false)
  }
}
