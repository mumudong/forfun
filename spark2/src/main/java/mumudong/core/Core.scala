package mumudong.core

import org.junit.Test

/**
  * ----> count
  * val c = sc.parallelize(List("Gnu", "Cat", "Rat", "Dog"), 2)
    c.count
    res2: Long = 4
  *
  * ----> countByKey
  * val c = sc.parallelize(List((3, "Gnu"), (3, "Yak"), (5, "Mouse"), (3, "Dog")), 2)
  * c.countByKey
  * res1: scala.collection.Map[Int,Long] = Map(3 -> 3, 5 -> 1)
  * c.countByKey.size
  * res2: Int = 2
  *
  * ----> countByValue
  * scala> val b = sc.parallelize(List(1,2,3,4,5,6,7,8,2,4,2,1,1,1,1,1))
  * b: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[12] at parallelize at <console>:21
  *
  * scala> b.countByValue
  * res3: scala.collection.Map[Int,Long] = Map(5 -> 1, 1 -> 6, 6 -> 1, 2 -> 3, 7 -> 1, 3 -> 1, 8 -> 1, 4 -> 2)
  *
  * ----> groupByKey
  *  val scoreDetail = sc.parallelize(List(("xiaoming",75),("xiaoming",90),("lihua",95),("lihua",100),("xiaofeng",85)))
  *  scoreDetail.groupByKey().collect().foreach(println(_));
  *    (lihua,CompactBuffer(95, 100))
  *    (xiaoming,CompactBuffer(75, 90))
  *    (xiaofeng,CompactBuffer(85))
  *
  * ----> cogroup
  * scala> val scoreDetail = sc.parallelize(List(("xiaoming",95),("xiaoming",90),("lihua",95),("lihua",98),("xiaofeng",97)))
  * scala> val scoreDetai2 = sc.parallelize(List(("xiaoming",65),("lihua",63),("lihua",62),("xiaofeng",67)))
  * scala> val scoreDetai3 = sc.parallelize(List(("xiaoming",25),("xiaoming",15),("lihua",35),("lihua",28),("xiaofeng",36)))
  * scala> scoreDetail.cogroup(scoreDetai2,scoreDetai3)
  * res1: Array[(String, (Iterable[Int], Iterable[Int], Iterable[Int]))] = Array((xiaoming,(CompactBuffer(95, 90),CompactBuffer(65),CompactBuffer(25, 15))), (lihua,(CompactBuffer(95, 98),CompactBuffer(63, 62),CompactBuffer(35, 28))), (xiaofeng,(CompactBuffer(97),CompactBuffer(67),CompactBuffer(36))))
  */
class Core {
    @Test
    def test = {

    }
}

