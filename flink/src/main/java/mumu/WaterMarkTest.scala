package mumu

import java.lang
import java.text.SimpleDateFormat

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

/**
  * 流式数据容许延迟处理
  *     AssignerWithPeriodicWatermarks
  *     000001,1461756862000
  */
object WaterMarkTest {
    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment

        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)//设置基于事件时间统计

        import org.apache.flink.api.scala._

        val text = env.socketTextStream("mu1",11999)
                        .map( x => (x.split("\\W+")(0), x.split("\\W+")(1).toLong))
                        .assignTimestampsAndWatermarks(new TimeStampExractor)

        import org.apache.flink.api.scala._
        val counts = text// 原始数据格式是  key,timestamp毫秒时间戳
                            .keyBy(_._1)
                            .timeWindow(Time.seconds(3)) // 每5s计算10s的窗口数据
                            .apply(new MyWindowFunction)
//                            .sum(1)
        counts.print()
        env.execute("ProcessingTime processing example")

    }

    class MyWindowFunction extends WindowFunction[(String,Long),(String,Int,String,String,String,String),String,TimeWindow]{
        override def apply(key: String, w: TimeWindow, iterable:  Iterable[(String, Long)], collector: Collector[(String, Int, String, String, String, String)]): Unit = {
            val list = iterable.toList.sortBy(_._2) // 按时间戳排序
            val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            collector.collect(key,iterable.size,format.format(list.head._2),format.format(list.last._2),"window-start:" + format.format(w.getStart),"window-end:" + format.format(w.getEnd))
        }
    }



    class TimeStampExractor extends AssignerWithPeriodicWatermarks[(String,Long)] with Serializable{
        var currentMaxTimestamp = 0L
        val maxOutOfOrderness = 10000L//最大允许的乱序时间是10s
        var water : Watermark = _

        val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

        override def getCurrentWatermark: Watermark = {
            water = new Watermark(currentMaxTimestamp - maxOutOfOrderness)
            water
        }

        override def extractTimestamp(t: (String,Long), l: Long): Long = {
            val timestamp = t._2
            currentMaxTimestamp = Math.max(timestamp,currentMaxTimestamp)
            println("extractTimestamp:" + t._1 + ", " + t._2 + "->" + format.format(t._2) + " || currentMax -> " + currentMaxTimestamp+"->"+format.format(currentMaxTimestamp) + " || water -> " + water  )
            timestamp
        }
    }
}

