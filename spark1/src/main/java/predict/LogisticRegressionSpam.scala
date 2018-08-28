package predict

import java.io.File

import common.{AnsyUtil, BaiduNewsResult, JDBCHelper}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.classification.{ClassificationModel, LogisticRegressionModel, LogisticRegressionWithSGD}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.mllib.optimization.{SimpleUpdater, SquaredL2Updater, Updater}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._


/**
  * 新闻分类
  * 提交参数中加入  --files /usr/local/spark1.3/conf/c3p0.properties --jars /usr/local/spark1.3/lib/mysql-connector-java-5.1.38-bin.jar,
  */
object LogisticRegressionSpam {

    def main(args: Array[String]): Unit = {
        //   train("newsanalys/newsModel","test2",1.0,10,1.0)//训练新闻模型
        //   train("newsanalys/tiebaModel","test_tieba3",0.1,50,0.5)//训练贴吧模型
        System.setProperty("HADOOP_USER_NAME","hdfs")
        //写个定时器，每天跑一次,好像不可以
        //试用crontab定时提交
        val logger = Logger.getLogger("org.apache.spark")
        val isLocal = false
//        val isLocal =  if("true".equals(args(0))){
//                            true
//                        }else{
//                            false
//                        }
        logger.error("训练模型-->" + isLocal)
         train("/test/model","baidu_news_predict",isLocal)
    }
    def train(modelPath:String,table:String,isLocal:Boolean)={
        val fileConf = new Configuration()
        val fileSystem = FileSystem.get(fileConf)
        val path = new Path(modelPath)


        //屏蔽不必要的日志显示在终端上
//        Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
//        Logger.getLogger("org.apache.eclipse.jetty.server").setLevel(Level.OFF)
        val conf = new SparkConf().setAppName("LogisticRegression").setMaster("local[2]")
        val sc = new SparkContext(conf)
        //获取新闻数据
        var negativeNews:Array[BaiduNewsResult] = null
        var positiveNews:Array[BaiduNewsResult] = null
        //是否为本地模式
        val jdbcHelper = JDBCHelper.getJDBCHelper( isLocal)

        if(table.equals("baidu_news_predict")){
            negativeNews = jdbcHelper.query("select * from \"public\"." + table + " where label='0.0'" ).toArray[BaiduNewsResult](Array())
            positiveNews = jdbcHelper.query("select * from \"public\"." + table + " where label='1.0'" ).toArray[BaiduNewsResult](Array())
        }else if(table.equals("test_tieba3")){
            negativeNews = jdbcHelper.query("select * from \"public\"." + table + " where label='0'" ).toArray[BaiduNewsResult](Array())
            positiveNews = jdbcHelper.query("select * from \"public\"." + table + " where label='1' or label='2'" ).toArray[BaiduNewsResult](Array())
        }
        println(negativeNews.size+"======"+positiveNews.size)
        val nega=sc.parallelize(negativeNews)
        val posit=sc.parallelize(positiveNews)
        //创建一个HashingTF实例来把邮件文本映射为包含25000特征的向量
        val tf = new HashingTF(numFeatures = 25000)
        //各邮件都被切分为单词，每个单词被映射为一个特征
        val negaFeatures_  = nega.map(news => tf.transform(AnsyUtil.ansjFenci(news)))
        val posiFeatures_ = posit.map(news => tf.transform(AnsyUtil.ansjFenci(news)))

        val idfModel = new IDF().fit(negaFeatures_.union(posiFeatures_))
        val negaFeatures = idfModel.transform(negaFeatures_)
        val posiFeatures = idfModel.transform(posiFeatures_)



        val negativeData = negaFeatures.map(features => LabeledPoint(0, features))
        val positiveData = posiFeatures.map(features => LabeledPoint(1, features))

        val data=positiveData.union(negativeData).randomSplit(Array(0.6,0.4),111)

        val trainingData = data(0)//训练数据
        val testData=data(1)//测试数据

        trainingData.cache() // 逻辑回归是迭代算法，所以缓存训练数据的RDD
        testData.cache()

        println("\n迭代测试==========>\n")
        val iterSeq =Seq(1, 5, 10, 30,50,80,100,130) //iteratTest(trainingData,testData)  迭代10次效果最好？？？ Seq(1, 5, 10, 30,50,80,100,130)
        println("\n步长测试==========>\n")
        val stepSeq =Seq(0.001, 0.01, 0.05,0.1,0.5, 1.0,3.0,7.0, 10.0) //stepTest(trainingData,testData) 步长1效果最好？？？  Seq(0.001, 0.01, 0.05,0.1,0.5, 1.0,3.0,7.0, 10.0)
        println("\n正则测试==========>\n")
        val regSeq =Seq(0.001, 0.005,0.01,0.05, 0.1,0.5,1.0,3.0,7.0, 10.0) //regTest(trainingData,testData) 正则取1效果最好？？？  Seq(0.001, 0.005,0.01,0.05, 0.1,0.5,1.0,3.0,7.0, 10.0)
        var bestIter = 0
        var bestStep = 0.0
        var bestReg = 0.0
        var bestAUC = 0.0
        var bestPR = 0.0
        iterSeq.map{ paramIter =>
            stepSeq.map{ paramStep =>
                regSeq.map{ paramReg =>
                    val model = trainWithParams(trainingData, paramReg, paramIter, new SimpleUpdater, paramStep)
                    //准确度评估
                    val correctCount=testData.map{ labelp=>
                        if(labelp.label==model.predict(labelp.features)) 1 else 0
                    }
                    val accuracy = correctCount.sum / correctCount.count

                    val tr = createMetrics(s"选择最优参数", trainingData, model)
                    val te = createMetrics(s"选择最优参数", testData, model)
                    println(f"accuracy=${accuracy * 100}%2.2f%%")
                    println(f"训练集---->选择最优参数 ter = ${paramIter},step = ${paramStep},reg = ${paramReg}, AUC = ${tr._2 * 100}%2.2f%%, PR = ${tr._1 * 100}%2.2f%%" )
                    println(f"测试集---->选择最优参数 ter = ${paramIter},step = ${paramStep},reg = ${paramReg}, AUC = ${te._2 * 100}%2.2f%%, PR = ${te._1 * 100}%2.2f%%" )
                    println("\nte._2 * 100="+te._2 * 100 + "\nbestAUC=" + bestAUC)
                    if(te._2 * 100 > bestAUC){
                        println("第一步发生替换---->\n"+
                                bestIter + "-bestIter->" + paramIter +
                            bestStep + "-bestStep->" + paramStep +
                            bestReg + "-bestReg->" + paramReg +
                            bestAUC + "-bestAUC->" + te._2 * 100
                        )
                        bestIter = paramIter
                        bestStep = paramStep
                        bestReg = paramReg
                        bestAUC = te._2 * 100
                        bestPR = te._1 * 100
                    }else if(bestAUC == te._2 * 100 && te._1 * 100 > bestPR){
                        println("第二步发生替换---->\n"+
                            bestIter + "-bestIter->" + paramIter +
                            bestStep + "-bestStep->" + paramStep +
                            bestReg + "-bestReg->" + paramReg +
                            bestAUC + "-bestAUC->" + te._2 * 100
                        )
                        bestIter = paramIter
                        bestStep = paramStep
                        bestReg = paramReg
                        bestAUC = te._2 * 100
                        bestPR = te._1 * 100
                    }
                }
            }
        }
        println(s"\n╮（￣▽￣）╭ --  ╮（￣▽￣）╭ --  ╮（￣▽￣）╭ --  ╮（￣▽￣）╭\n\n最终获胜选手 \n\n iter = ${bestIter},step = ${bestStep},reg = ${bestReg}")
        //使用SGD算法运行逻辑回归
        val model=trainWithParams(trainingData,bestReg,bestIter,new SquaredL2Updater,bestStep)

        //准确度评估
        val correctCount=testData.map{ labelp=>
            if(labelp.label==model.predict(labelp.features)) 1 else 0
        }
        val accuracy = correctCount.sum / correctCount.count

        //PR曲线，准确率及召回率，查全率（负类判为正类的比率）
        val falseVsTrue = testData.map{labelp=>
            (model.predict(labelp.features),labelp.label)
        }
        val predictScale = new BinaryClassificationMetrics(falseVsTrue)
        val prArea = predictScale.areaUnderPR()
        val rocArea = predictScale.areaUnderROC()

        println(f"最终训练模型\nAccuracy: ${accuracy * 100}%2.2f%%\nArea under PR: ${prArea * 100.0}%2.2f%%\nArea under ROC: ${rocArea * 100.0}%2.2f%%")

        //删除旧模型，保存新模型
        try {
//            deleteDir(new File(path))
            fileSystem.delete(path,true)
            println("就模型已删除！")
        } catch  {
            case e:Exception=>println("无模型")
        }

        model.save(sc,"hdfs://tianxi-ha" + path)
        sc.stop()

    }

    // 根据给定数据输入模型
    def trainWithParams(input: RDD[LabeledPoint], regParam: Double, numIterations: Int, updater: Updater, stepSize: Double) = {
        val lr = new LogisticRegressionWithSGD

        lr.optimizer.setNumIterations(numIterations).setUpdater(updater).setRegParam(regParam).setStepSize(stepSize)
        lr.run(input)
    }
    // 根据输入数据和分类模型，计算AUC
    def createMetrics(label: String, data: RDD[LabeledPoint], model: ClassificationModel) = {

        val scoreAndLabels = data.map { point =>
            (model.predict(point.features), point.label)
        }
        val metrics = new BinaryClassificationMetrics(scoreAndLabels)
        (metrics.areaUnderPR, metrics.areaUnderROC)
    }
    //测试不同迭代次数
    def iteratTest(train: RDD[LabeledPoint],test: RDD[LabeledPoint]):Seq[Int]= {
        var bestIt = 0
        var bestAUC = 0.0
        var bestPR = 0.0
        var bestIter = Seq[Int]()
        var testIter = Seq(1, 5, 10, 30,50,80,100,130)
        val iterResults = testIter.map { param =>
            val model = trainWithParams(train, 0.0, param, new SimpleUpdater, 1.0)
            //准确度评估
            val correctCount=test.map{ labelp=>
                if(labelp.label==model.predict(labelp.features)) 1 else 0
            }
            val accuracy = correctCount.sum / correctCount.count

            val tr = createMetrics(s"$param iterations", train, model)
            val te = createMetrics(s"$param iterations", test, model)
            println(f"accuracy=${accuracy * 100}%2.2f%%")
            println(f"训练集---->${param}, AUC = ${tr._2 * 100}%2.2f%%, PR = ${tr._1 * 100}%2.2f%%" )
            println(f"测试集---->${param}, AUC = ${te._2 * 100}%2.2f%%, PR = ${te._1 * 100}%2.2f%%" )
            if(te._2 * 100 > bestAUC){
                bestIt = testIter.indexOf(param)
                bestAUC = te._2 * 100
                bestPR = te._1 * 100
            }else if(bestAUC == te._2 * 100 && te._1 * 100 > bestPR){
                bestIt = testIter.indexOf(param)
                bestAUC = te._2 * 100
                bestPR = te._1 * 100
            }
        }
        if(bestIt == 0)
            bestIter = Seq(testIter(bestIt),testIter(bestIt+1))
        else if(bestIt == testIter.indexOf(130))
            bestIter = Seq(testIter(bestIt),testIter(bestIt-1))
        else
            bestIter = Seq(testIter(bestIt+1),testIter(bestIt),testIter(bestIt-1))
        bestIter
    }
    //测试不同步长
    def stepTest(train: RDD[LabeledPoint],test: RDD[LabeledPoint]):Seq[Double]= {
        var bestSt = 0
        var bestAUC = 0.0
        var bestPR = 0.0
        var bestStep = Seq[Double]()
        var testStep = Seq(0.001, 0.01, 0.05,0.1,0.5, 1.0,3.0,7.0, 10.0)


        val stepResults = testStep.map { param =>
            val model = trainWithParams(train, 0.0, 100, new SimpleUpdater, param)

            //准确度评估
            val correctCount=test.map{ labelp=>
                if(labelp.label==model.predict(labelp.features)) 1 else 0
            }
            val accuracy = correctCount.sum / correctCount.count

            println(f"accuracy=${accuracy * 100}%2.2f%%")
            val tr = createMetrics(s"$param step size", train, model)
            val te = createMetrics(s"$param step size", test, model)
            println(f"训练集---->$param, AUC = ${tr._2 * 100}%2.2f%%, PR = ${tr._1 * 100}%2.2f%%")
            println(f"测试集---->$param, AUC = ${te._2 * 100}%2.2f%%, PR = ${te._1 * 100}%2.2f%%")
            if(te._2 * 100 > bestAUC){
                bestSt = testStep.indexOf(param)
                bestAUC = te._2 * 100
                bestPR = te._1 * 100
            }else if(bestAUC == te._2 * 100 && te._1 * 100 > bestPR){
                bestSt = testStep.indexOf(param)
                bestAUC = te._2 * 100
                bestPR = te._1 * 100
            }

        }
        if(bestSt == 0)
            bestStep = Seq(testStep(bestSt),testStep(bestSt+1))
        else if(bestSt == testStep.indexOf(10.0))
            bestStep = Seq(testStep(bestSt),testStep(bestSt-1))
        else
            bestStep = Seq(testStep(bestSt+1),testStep(bestSt),testStep(bestSt-1))
        bestStep
    }
    //
    def regTest(train: RDD[LabeledPoint],test: RDD[LabeledPoint]):Seq[Double]= {
        var bestR = 0
        var bestAUC = 0.0
        var bestPR = 0.0
        var bestReg = Seq[Double]()
        var testReg = Seq(0.001, 0.005,0.01,0.05, 0.1,0.5,1.0,3.0,7.0, 10.0)

        val regResults = testReg.map { param =>
            val model = trainWithParams(train, param, 100, new SquaredL2Updater, 1.0)
            val tr = createMetrics(s"$param L2 regularization parameter", train, model)
            val te = createMetrics(s"$param L2 regularization parameter", test, model)

            //准确度评估
            val correctCount=test.map{ labelp=>
                if(labelp.label==model.predict(labelp.features)) 1 else 0
            }
            val accuracy = correctCount.sum / correctCount.count

            println(f"accuracy=${accuracy * 100}%2.2f%%")
            println(f"训练集---->$param, AUC = ${tr._2 * 100}%2.2f%%, PR = ${tr._1 * 100}%2.2f%%")
            println(f"测试集---->$param, AUC = ${te._2 * 100}%2.2f%%, PR = ${te._1 * 100}%2.2f%%")
            if(te._2 * 100 > bestAUC){
                bestR = testReg.indexOf(param)
                bestAUC = te._2 * 100
                bestPR = te._1 * 100
            }else if(bestAUC == te._2 * 100 && te._1 * 100 > bestPR){
                bestR = testReg.indexOf(param)
                bestAUC = te._2 * 100
                bestPR = te._1 * 100
            }
        }
        if(bestR == 0)
            bestReg = Seq(testReg(bestR),testReg(bestR+1))
        else if(bestR == testReg.indexOf(10.0))
            bestReg = Seq(testReg(bestR),testReg(bestR-1))
        else
            bestReg = Seq(testReg(bestR+1),testReg(bestR),testReg(bestR-1))
        bestReg
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
    def predict( path:String,table:String)={
        //屏蔽不必要的日志显示在终端上
        Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
        Logger.getLogger("org.apache.eclipse.jetty.server").setLevel(Level.OFF)
        val conf = new SparkConf().setAppName("LogisticRegression").setMaster("local[2]")
        val sc = new SparkContext(conf)
        //加载模型
        val model=LogisticRegressionModel.load(sc,path)
        val jdbcHelper = JDBCHelper.getJDBCHelper( true);
        //获取待测试数据
        var news1:Array[BaiduNewsResult] = null
        if(table.equals("test_tieba")){
            news1 = jdbcHelper.query("select * from \"public\"."+table+" where label is null and content!='无数据，或数据已被删除' and content is not null and trim(content) !=''" ).toArray[BaiduNewsResult](Array[BaiduNewsResult]())
        }else if(table.equals("test_result")){
            news1=jdbcHelper.query("select * from \"public\"."+table+" where label is null and content!='无数据，或数据已被删除' and title is not null" ).toArray[BaiduNewsResult](Array[BaiduNewsResult]())
        }
        val xinwen=sc.parallelize(news1)

        val tf = new HashingTF(numFeatures = 25000)
        val xinwenVector = xinwen.map(xin=>tf.transform(AnsyUtil.ansjFenci(xin)))
        //预测分类结果
        val preResult=model.predict(xinwenVector)
        val result=xinwen.zip(preResult).mapPartitions({(ite:Iterator[(BaiduNewsResult,Double)])=>
            var list=List[BaiduNewsResult]()
            while(ite.hasNext){
                val it=ite.next()
                val bdn=it._1
                bdn.setLabel(it._2.toInt.toString)
                list=list.::(bdn)
            }
            list.reverse.iterator
        }).collect()
        result.foreach(println(_))
        jdbcHelper.save("update \"public\"."+table+" set label=? where \"id\"=? ", result.toBuffer.asJava)
        sc.stop()
    }
}

