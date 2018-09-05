package mumudong.core

import org.apache.spark.sql.SparkSession
import org.apache.spark.util.AccumulatorV2

import scala.collection.mutable

/**
  * scala默认使用不可变的容器
  */
object CustomAccumulator {
    def main(args: Array[String]): Unit = {
        val session = SparkSession.builder.master("local[2]").appName("accumulator").getOrCreate()
        val accum = new MyAccumulator
        session.sparkContext.register(accum,"myAccum")
        val sum = session.sparkContext.parallelize(Array("1","aa","2","bb","3","cc","4","dd"),2)
                        .filter{ x => //过滤只会留下为true的值
                            if(x.length == 2)
                                accum.add(x)
                            x.length == 1
                        }.map(_.toInt).reduce(_ + _)
        println("sum: " + sum)
        println("accum: " + accum.value)
        session.stop()
    }

}
class MyAccumulator extends AccumulatorV2[String,mutable.Set[String]]{
    val _mySet:mutable.Set[String] = mutable.Set[String]()
    override def isZero: Boolean = {_mySet.isEmpty}

    override def copy(): AccumulatorV2[String, mutable.Set[String]] = {
        val newSet = new MyAccumulator()
        _mySet.synchronized{
            newSet._mySet ++= _mySet
        }
        newSet
    }

    override def reset(): Unit = {_mySet.clear()}

    override def add(v: String): Unit = {_mySet.add(v)}

    override def value: mutable.Set[String] = {
        _mySet
    }

    override def merge(other: AccumulatorV2[String, mutable.Set[String]]): Unit = {
        other match{
            case oth:MyAccumulator => _mySet ++= (oth.value)
        }
    }
}
