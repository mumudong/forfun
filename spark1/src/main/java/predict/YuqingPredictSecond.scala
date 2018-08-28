package predict

import java.io.{File, FileFilter, FilenameFilter}
import java.util.Date

import common._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.LogisticRegressionModel
import org.apache.spark.mllib.feature.HashingTF

/**
  * Created by MuDong on 2017/10/16.
  */
object YuqingPredictSecond {
    def main(args: Array[String]): Unit = {
        predict("baidu_news_error")
    }
    def predict( table:String)={
        //屏蔽不必要的日志显示在终端上
        Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
        Logger.getLogger("org.apache.eclipse.jetty.server").setLevel(Level.OFF)
        val conf = new SparkConf().setAppName("LogisticRegression").setMaster("local[2]")
        val sc = new SparkContext(conf)
        //加载模型

        val jdbcHelper = JDBCHelper.getJDBCHelper( true);


        //获取待测试数据
        var news1:Array[BaiduNewsResult] = null
        var model:LogisticRegressionModel = LogisticRegressionModel.load(sc,"newsanalys/newsModel")

        val file = new File("newsanalys")
        new Thread(new Runnable() {
            override def run(): Unit = {
                try
                    new DirDog(file, false, new FileActionCallback() {
                        override def create(file: File): Unit = {
                            val date = new Date()
                            System.out.println(s"模型创建\t${date}\t" + file.getAbsolutePath)
                            file.listFiles(new FileFilter(){
                                override def accept(pathname: File): Boolean = {
                                    val s: String = pathname.getName.toLowerCase
                                    if (s.endsWith(".parquet")) {
                                        Thread.sleep(4000)
                                        model  = LogisticRegressionModel.load(sc,"newsanalys/newsModel")
                                    }
                                    return false
                                }
                            })

                        }

                        override def delete(file: File): Unit = {
                            val date = new Date()
                            System.out.println(s"模型删除\t${date.toLocaleString}\t" + file.getAbsolutePath)
                        }

                        override def modify(file: File): Unit = {
                        }
                    })
                catch {
                    case e: Exception =>
                        e.printStackTrace()
                }
            }
        }).start()



        while(true) {
            if(table.equals("baidu_news_error")){
                news1 = jdbcHelper.query("select * from \"public\"."+table+" where label is null and content!='无数据，或数据已被删除' and content is not null and trim(content) !=''" ).toArray[BaiduNewsResult](Array[BaiduNewsResult]())
            }
            val xinwen = sc.parallelize(news1)
            val tf = new HashingTF(numFeatures = 25000)
            val xinwenVector = xinwen.map(xin => tf.transform(AnsyUtil.ansjFenci(xin)))
            //预测分类结果
            val preResult = model.predict(xinwenVector)
            val result = xinwen.zip(preResult).mapPartitions({ (ite: Iterator[(BaiduNewsResult, Double)]) =>
                var list = List[BaiduNewsResult]()
                while (ite.hasNext) {
                    val it = ite.next()
                    val bdn = it._1
                    bdn.setLabel(it._2.toInt.toString)
                    list = list.::(bdn)
                }
                list.reverse.iterator
            }).collect()
            import scala.collection.JavaConversions._
            if (result.size > 0)
                jdbcHelper.save("INSERT INTO \"public\".baidu_news_error(id,title,content,intro, label , createtime , time,source,keyword) VALUES(?,?,?,?,?,to_timestamp(?,'YYYY-MM-DD HH24:MI:SS'),?,?,?) ON CONFLICT (id) do UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, intro = EXCLUDED.intro,label = EXCLUDED.label,createtime = excluded.createtime,time = excluded.time,source = excluded.source,keyword = excluded.keyword;", result.toList)
            else
                println(new Date().toLocaleString + "    无数据")
            Thread.sleep(60000)
            result.foreach(println(_))
        }

        sc.stop()
    }
}
