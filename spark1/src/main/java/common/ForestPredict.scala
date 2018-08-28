package common

import java.io.File

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.{LogisticRegression, RandomForestClassifier}
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature.{HashingTF, IDF, StringtoVector}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.{SparkConf, SparkContext}


/**
  * 新闻分类
  * 提交参数中加入  --files /usr/local/spark1.3/conf/c3p0.properties --jars /usr/local/spark1.3/lib/mysql-connector-java-5.1.38-bin.jar,
  */
object ForestPredict {

    def main(args: Array[String]): Unit = {
        //   train("newsanalys/newsModel","test2",1.0,10,1.0)//训练新闻模型
        //   train("newsanalys/tiebaModel","test_tieba3",0.1,50,0.5)//训练贴吧模型
        System.setProperty("HADOOP_USER_NAME","hdfs")
        //写个定时器，每天跑一次,好像不可以
        //试用crontab定时提交

         train()
    }
    def train()={
        val fileConf = new Configuration()
        val fileSystem = FileSystem.get(fileConf)

//        Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
//        Logger.getLogger("org.apache.eclipse.jetty.server").setLevel(Level.OFF)
        val conf = new SparkConf().setAppName("LogisticRegression").setMaster("local[2]")
        val sc = new SparkContext(conf)
        val sqlSc = new SQLContext(sc)

        val train = sqlSc.read.format("jdbc").options(
            Map("url"->"jdbc:postgresql://10.167.202.177:5432/crawler-hx",
                "dbtable"->"baidu_news_predict",
                "driver"->"org.postgresql.Driver",
                "user"->"TXDB",
                "password"->"123456"
            )
        ).load()
//        jdbcDF.collect().take(1).foreach(println(_))
        val training = train.withColumn("label2", train.col("label").cast(DoubleType))
                                .drop("label").withColumnRenamed("label2","label")

        val fenci = new StringtoVector().setInputCol("content").setOutputCol("fenci")
        val hashingTf = new HashingTF().setInputCol(fenci.getOutputCol)
                                        .setOutputCol("feat")
        val idf = new IDF().setInputCol(hashingTf.getOutputCol).setOutputCol("features")
        val forest = new RandomForestClassifier().setLabelCol("label")
                                                    .setFeaturesCol("features")

        val pipeLine = new Pipeline().setStages(Array(fenci,hashingTf,idf,forest))

        val paramGrid = new ParamGridBuilder().addGrid(hashingTf.numFeatures,Array(2000,4000,8000,12000,16000,20000,24000,28000))
                                                .addGrid(forest.numTrees,Array(10,15,20))
                                                .addGrid(forest.impurity,Array("gini","entropy"))
                                                    .build()

        val cv = new CrossValidator().setEstimator(pipeLine)
                                        .setEvaluator(new BinaryClassificationEvaluator())
                                        .setEstimatorParamMaps(paramGrid)
                                        .setNumFolds(10)
        val cvModel = cv.fit(training)
        val path = new Path("/cvmodel")
        if(fileSystem.exists(path)){fileSystem.delete(path,true)}
        cvModel.save("/cvmodel")


//        val cvModel = CrossValidatorModel.load("/cvmodel")
        println("cvModel.avgMetrics --> " )
        cvModel.avgMetrics.foreach(println(_))
        println("params --> ")
//        cvModel.params.foreach(x => println(x.name))
        val bestPipeline = cvModel.bestModel.parent.asInstanceOf[Pipeline]
            println("length -- > " + bestPipeline.getStages.length)
        val stage = bestPipeline.getStages(0)
//        println(stage.extractParamMap().get(stage.getParam("numFeatrues")))
//        println(stage.extractParamMap().get(stage.getParam("regParam")))

        println("map --> " + bestPipeline.getStages(3).extractParamMap())


        val testing  = sqlSc.read.format("jdbc").options(
            Map("url"->"jdbc:postgresql://10.167.202.177:5432/crawler-hx",
                "dbtable"->"model_news",
                "driver"->"org.postgresql.Driver",
                "user"->"TXDB",
                "password"->"123456"
            )
        ).load()
        val testing2 = testing.withColumn("label2",testing.col("label").cast(DoubleType)).drop("label").withColumnRenamed("label2","label")
        val test = cvModel.transform(testing2)
        val result = test.selectExpr("id","label","prediction","title").filter("label != prediction")
        result.show()
        println("sum --> " + result.count())
        test.printSchema()

        val scoreAndLabels = test.select("label","prediction").rdd.map(x => (x.getAs[Double](0),x.getAs[Double](1)))
        val metrics = new BinaryClassificationMetrics(scoreAndLabels)
        println("auc ----> " + metrics.areaUnderROC())
        println("pr ----> " + metrics.areaUnderPR())
        val accuracy = test.filter("label = prediction").count() * 1.0/test.count()
        println(f"accuracy ---->  ${accuracy * 100}%2.2f%%")
//        test.select("id","content","probability","prediction")
//                                .collect().take(40).foreach{
//                                                case Row(id:Long,content:String,prob:Vector,prediction:Double)=>
//                                                    println(s"($id,$content) \n--> prob=$prob   \nprediction=$prediction")
//                                                }
        sc.stop()

    }
    def deleteDir(dir: File): Unit = {
        val files = dir.listFiles()
        files.foreach(f => {
            if (f.isDirectory) {
                deleteDir(f)
            } else {
                f.delete()
                //        println("delete file " + f.getAbsolutePath)
            }
        })
        dir.delete()
        //    println("delete dir " + dir.getAbsolutePath)
    }

}

