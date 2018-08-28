package crawler

/**
  * Created by MuDong on 2017/10/17.
  */
object ListTest {
    def main(args: Array[String]): Unit = {
        // 模式匹配
        val shuffledData = List(6, 3, 5, 6, 2, 9, 1)
        println(sortList(shuffledData))
        // 排序
        def sortList(dataSet: List[Int]): List[Int] = dataSet match {
            case List()       => List()
            case head :: tail => compute(head, sortList(tail))

        }

        def compute(data: Int, dataSet: List[Int]): List[Int] = dataSet match {
            case List() => List(data)
            // 如果集合第一个元素值小于data值，data放在第一个位置
            case head :: tail => if (data <= head) data :: dataSet
            // 如果不小于data值，进行下次比较
            else head :: compute(data, tail)
        }
    }
}
