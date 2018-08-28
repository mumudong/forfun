package predict

import java.text.SimpleDateFormat
import java.util
import java.util.concurrent.{Executors, TimeUnit}
import java.util.{Date, Locale, TimerTask}

import common._
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.streaming.kafka.KafkaManager
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by MuDong on 16-8-4.
  */
object YuqingPredict222 {
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
        //加载模型
        var newsModel:LogisticRegressionModel = null
        //  var newsModel = LogisticRegressionModel.load(ssc.sparkContext,"newsanalys/newsModel")
        //  val tiebaModel = LogisticRegressionModel.load(ssc.sparkContext,"tiebaModel")
        newsModel  = LogisticRegressionModel.load(ssc.sparkContext,"hdfs://tianxi-ha/test/model")

        new Thread(new Runnable() {
            override def run(): Unit = {
                val task = new TimerTask() {
                    def run(): Unit = {
                        newsModel  = LogisticRegressionModel.load(ssc.sparkContext,"hdfs://tianxi-ha/test/model")
                        println("----------加载模型" + new Date().toLocaleString)
                    }
                }
                val  pool = Executors.newScheduledThreadPool(1)
                pool.scheduleAtFixedRate(task, 0, 1, TimeUnit.DAYS)
            }
        }).start()

        val tf = new HashingTF(25000)
        // Create direct kafka stream with brokers and topics

        val kafkaParams = Map[String, String](
            "metadata.broker.list" -> "10.167.222.105:6667,10.167.222.106:6667,10.167.222.107:6667",
            "zookeeper.connect"->"10.167.222.105:2181,10.167.222.106:2181,10.167.222.107:2181",
            "group.id" -> "test-consumer-group" ,
            "auto.offset.reset" -> "largest"
        )

        val km = new KafkaManager(kafkaParams)
        newsModel = LogisticRegressionModel.load(ssc.sparkContext,"hdfs://tianxi-ha/test/model")
        val messages = km.createDirectStream[String, String, StringDecoder, StringDecoder](
            ssc, kafkaParams, Set("tx"))
        //        val messages = messages.checkpoint(Durations.seconds(10L))

        messages.foreachRDD(rdd => {

            rdd.foreachPartition{partitionRecords=>
                val jdbcHelper = JDBCHelper.getJDBCHelper(isLocal)
                // 先处理消息
                if(!partitionRecords.isEmpty)
                    processNews(partitionRecords,tf,newsModel,jdbcHelper)
            }
            km.updateZKOffsets(rdd)
        })

        ssc.start()
        ssc.awaitTermination()
    }

    def processNews(rdd: Iterator[(String, String)],tf:HashingTF,model:LogisticRegressionModel,jdbcHelper:JDBCHelper ): Unit = {
        val lines = rdd.map(_._2)
        val format =  new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US)
        val list = new util.ArrayList[BaiduNewsResult]()
        while(lines.hasNext) {
            val line_ = lines.next()
            val line = line_.split("@@") //id content creattime
            try {
                println("line --> " + line_)
                val label = model.predict(tf.transform(AnsyUtil.stringFenci(line(2))))
                val bdNews = new BaiduNewsResult
                bdNews.setId(line(0).toLong)
                bdNews.setTitle(line(1))
                bdNews.setContent(line(2))
                bdNews.setLabel(label.toString)
                bdNews.setIntro(line(3))
                bdNews.setCreatetime(format.parse(line(4)))
                bdNews.setTime(line(5))
                bdNews.setSource(line(6))
                bdNews.setKeyword(line(7))
                bdNews.setWebsite(line(8))
                bdNews.setUrl(line(9))
                list.add(bdNews)
            } catch {
                case  e => {
                    e.printStackTrace()
                    println("kafka数据解析失败 ---> \n" + lines)
                    jdbcHelper.saveOne("INSERT INTO \"public\".baidu_news_error(content, label) VALUES(?,?)",line_ )
                }
            }
        }

        println("list.size--->" + list.size)
        //保存数据库
        if(list.size > 0)
            try {
                jdbcHelper.save("INSERT INTO \"public\".baidu_news_predict(id,title,content,intro, label , createtime , time,source,keyword,website,url) VALUES(?,?,?,?,?,to_timestamp(?,'YYYY-MM-DD HH24:MI:SS'),?,?,?,?,?) ON CONFLICT (id) do UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, intro = EXCLUDED.intro,label = EXCLUDED.label,createtime = excluded.createtime,time = excluded.time,source = excluded.source,keyword = excluded.keyword,website = excluded.website,url = excluded.url;", list)
            } catch {
                case e => {
                    e.printStackTrace()
                    println("kafka至数据库存储失败")
                    //存储处理失败的新闻id
                    jdbcHelper.save("INSERT INTO \"public\".baidu_news_error(id,title,content,intro, label , createtime , time,source,keyword,website,url) VALUES(?,?,?,?,?,to_timestamp(?,'YYYY-MM-DD HH24:MI:SS'),?,?,?,?,?) ON CONFLICT (id) do UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, intro = EXCLUDED.intro,label = EXCLUDED.label,createtime = excluded.createtime,time = excluded.time,source = excluded.source,keyword = excluded.keyword,website = excluded.website,url = excluded.url;", list)
                }
            }




    }


}
