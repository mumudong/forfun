package predict

import java.io.{File, FileFilter}
import java.sql.{Date, Timestamp}
import java.text.SimpleDateFormat
import java.util.concurrent.{Executors, TimeUnit}
import java.util.{Locale, Properties, Timer, TimerTask}

import common._
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.tuning.CrossValidatorModel
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.GenericMutableRow
import org.apache.spark.sql.types.{StructType, _}
import org.apache.spark.sql.{Row, SQLContext, SaveMode}
import org.apache.spark.streaming.kafka.KafkaManager
import org.apache.spark.streaming.{Durations, Seconds, StreamingContext}
import org.apache.spark.unsafe.types.UTF8String
import org.glassfish.grizzly.http.util.TimeStamp

//@SQLUserDefinedType(udt = classOf[BaiduNewsUDT])
case class BaiduNews(
 var id: Long
,var title: String
,var content: String
,var intro: String
,var createtime: Timestamp
,var time: String
,var source: String
,var keyword: String
,var website: String
,var url: String ) extends Serializable

/**
  * Created by MuDong on 16-8-4.
  */
object YuqingPredict {
    def main(args: Array[String]) {
        if (args.length < 1) {
            System.err.println( s"""
                                   |Usage: YuqingPredict <isLocal>
                                   |  <isLocal> where to  read jdbc file, cluster or local
                                   |
                 """.stripMargin)
//                   System.exit(1)
        }
        //关闭日志
        Logger.getLogger("org").setLevel(Level.ERROR)
        //接收命令行参数
        // val Array(brokers, topics, groupId) = args
        val isLocal = true
//        val isLocal =   if("true".equals(args(0))){
//                            true
//                        }else{
//                            false
//                        }
        System.setProperty("HADOOP_USER_NAME","hdfs")
        val sparkConf = new SparkConf().setAppName("YuqingPredict").setMaster("local[3]")
        //sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "5")  //数据量大时可设置拉取条数
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

        val ssc = new StreamingContext(sparkConf, Seconds(20))
        val sqlContext = new SQLContext(ssc.sparkContext)
        println("sqlcontext----> " + sqlContext.getAllConfs)
        //加载模型
        var newsModel:CrossValidatorModel = null
        //  var newsModel = LogisticRegressionModel.load(ssc.sparkContext,"newsanalys/newsModel")
        //  val tiebaModel = LogisticRegressionModel.load(ssc.sparkContext,"tiebaModel")
        newsModel  =  CrossValidatorModel.load("/cvmodel")

        new Thread(new Runnable() {
            override def run(): Unit = {
                val task = new TimerTask() {
                    def run(): Unit = {
                        newsModel  =  CrossValidatorModel.load("/cvmodel")
                        println("----------加载模型" + new java.util.Date().toLocaleString)
                    }
                }
                val  pool = Executors.newScheduledThreadPool(1)
                pool.scheduleAtFixedRate(task, 0, 1, TimeUnit.DAYS)
            }
        }).start()


        val kafkaParams = Map[String, String](
            "metadata.broker.list" -> "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667",
            "zookeeper.connect"->"10.167.222.105:2181,10.167.222.106:2181,10.167.222.107:2181",
            "group.id" -> "test-consumer-group" ,
            "auto.offset.reset" -> "largest"
        )

        val km = new KafkaManager(kafkaParams)

        val messages = km.createDirectStream[String, String, StringDecoder, StringDecoder](
            ssc, kafkaParams, Set("tx"))
        //        val messages = messages.checkpoint(Durations.seconds(10L))

        messages.foreachRDD(rdd => {

            val format =  new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US)
            val fm2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val structType = StructType(List(StructField("id",DataTypes.LongType,false),
                                            StructField("title",DataTypes.StringType,false),
                                            StructField("content",DataTypes.StringType,false),
                                            StructField("intro",DataTypes.StringType,false),
                                            StructField("createtime",DataTypes.TimestampType,false),
                                            StructField("time",DataTypes.StringType,false),
                                            StructField("source",DataTypes.StringType,false),
                                            StructField("keyword",DataTypes.StringType,false),
                                            StructField("website",DataTypes.StringType,false),
                                            StructField("url",DataTypes.StringType,false),
                                            StructField("modifiedtime",DataTypes.TimestampType,true)))
            val jdbcHelper = JDBCHelper.getJDBCHelper(isLocal)
            val dat = rdd.map( x => {
                val array = x._2.split("@@")
                var row:Row = null
                try{
                    row = Row(array(0).toLong,
                    array(1),
                    array(2),
                    array(3),
                    new Timestamp( format.parse(array(4)).getTime) ,
                    array(5),
                    array(6),
                    array(7),
                    array(8),
                    array(9),
                        new Timestamp(new java.util.Date().getTime))
                }catch {
                    case e => e.printStackTrace()
                        println("kafka数据解析失败 ---> \n" + x._2)
//                        jdbcHelper.saveOne("INSERT INTO \"public\".baidu_news_error(content, label) VALUES(?,?)",x._2 )
                }
                row
            })

                val url = "jdbc:postgresql://10.167.202.177:5432/crawler-hx"
                val prop = new Properties()
                prop.setProperty("user","TXDB")
                prop.setProperty("password","123456")
                prop.setProperty("classDriver","org.postgresql.Driver")
                val data = sqlContext.createDataFrame(dat.filter(x  => x!=null),structType)
                println("data.size----> " + data.count())
                var result = newsModel.transform(data)

                println("result.size----> " + result.count())
                if(result.count()>0){
                    val tb = result.selectExpr("id","createtime","content","intro","keyword","prediction","source","time","title","url","website","modifiedtime").withColumnRenamed("prediction","label")
                    tb.write.mode(SaveMode.Append).jdbc(url,"baidu_news_predict_test",prop)
                }
                km.updateZKOffsets(rdd)
        })

        ssc.start()
        ssc.awaitTermination()
    }

    def processNews(rdd: RDD[(String, String)] ,   jdbcHelper:JDBCHelper  ): Unit = {
//        val lines = rdd.map(_._2)
//        val format =  new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US)
//        val fm2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//        var listError = List[BaiduNews]()
//        var li = List[BaiduNews]()
//        val list = new util.ArrayList[BaiduNewsResult]()
//        while(lines.hasNext) {
//            val line_ = lines.next()
//            val line = line_.split("@@") //id content creattime
//            try {
//
//               li = li.::(BaiduNews( line(0).toLong,
//                                     line(1),
//                                     line(2),
//                                     line(3),
//                                     Timestamp.valueOf(fm2.format(format.parse(line(4)))) ,
//                                     line(5),
//                                     line(6),
//                                     line(7),
//                                     line(8),
//                                     line(9)))
//            } catch {
//                case  e => {
//                    e.printStackTrace()
//                    println("kafka数据解析失败 ---> \n" + lines)
//                    jdbcHelper.saveOne("INSERT INTO \"public\".baidu_news_error(content, label) VALUES(?,?)",line_ )
//                }
//            }
//        }
//        return li
//        val data = sQLContext.createDataFrame(li)
//        val dt = sQLContext.createDataFrame(list,classOf[BaiduNewsResult])
//        println("list.size---->" + li.size)
//        val url = "jdbc:postgresql://10.167.202.177:5432/crawler-hx"
//        val prop = new Properties()
//        prop.setProperty("user","TXDB")
//        prop.setProperty("password","123456")
//        prop.setProperty("classDriver","org.postgresql.Driver")
//        val data = sQLContext.createDataFrame(li).drop("label")
//        println("data.size----> " + data.count())
//        val result = model.transform(data)
//        result.write.mode(SaveMode.Append).jdbc(url,"baidu_news_predict_test",prop)
    }
}
