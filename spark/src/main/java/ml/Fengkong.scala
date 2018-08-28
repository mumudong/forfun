package ml

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.ml.feature._
import org.apache.spark.ml.tuning.{CrossValidator, CrossValidatorModel, ParamGridBuilder}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DataTypes
/**
  * Created by Administrator on 2018/6/8.
  */
object Fengkong {
    def main(args: Array[String]): Unit = {
        val fileConf = new Configuration()
        val fileSystem = FileSystem.get(fileConf)
        val spark = SparkSession.builder().appName("feng kong").master("local").getOrCreate()
        //获取数据
        var data = spark.read.format("jdbc")
                                .option("url","jdbc:sqlserver://tianxibigdata.database.chinacloudapi.cn:1433;DatabaseName=TianxiBigData")
                                .option("dbtable","(select top(1000) Gender , Age , Income , OverdueNo90 , AuthorizedAmounts,CreditRatings from creditrating) aa ")
                                .option("driver","com.microsoft.sqlserver.jdbc.SQLServerDriver")
                                .option("user","tianxi")
                                .option("password","1qa2ws!QA")
                                .load()
                                .cache()
        spark.udf.register("f1",(x:String) => x.length)
        import org.apache.spark.sql.functions._
        import spark.implicits._
        data = data.withColumn("label",when(data("CreditRatings")==="",0.0).otherwise(1.0))
                        .withColumn("Age2",data.col("Age").cast(DataTypes.DoubleType)).drop("Age").withColumnRenamed("Age2","Age")
                        .withColumn("Income2",data.col("Income").cast(DataTypes.DoubleType)).drop("Income").withColumnRenamed("Income2","Income")
                        .withColumn("OverdueNo902",data.col("OverdueNo90").cast(DataTypes.DoubleType)).drop("OverdueNo90").withColumnRenamed("OverdueNo902","OverdueNo90")
                        .withColumn("AuthorizedAmounts2",data.col("AuthorizedAmounts").cast(DataTypes.DoubleType)).drop("AuthorizedAmounts").withColumnRenamed("AuthorizedAmounts2","AuthorizedAmounts")
        /*
        * 此处需要根据实际正负例数据量，随机抽样值合适比例
        */
        val data0 = data.filter("CreditRatings = ''")
        val data1 = data.filter("CreditRatings != ''")
        println("负例----》" + data0.count() + " 正例----》" + data1.count())
        data.show(20)
        val formular = new RFormula().setFormula("label ~ Gender + Age + Income + OverdueNo90 + AuthorizedAmounts")
            .setFeaturesCol("features")
            .setLabelCol("Rlabel")
        val output = formular.fit(data).transform(data)
        output.select("features", "Rlabel").show(10,false)
        val scaler = new MinMaxScaler().setInputCol("features")
            .setOutputCol("scaledFeatures")
        val scalerDf: DataFrame = scaler.fit(output)
            .transform(output)
        scalerDf.show(10,false)
        scalerDf.printSchema()
        // PCA降维
//        val pcaModel = new PCA().setInputCol("scaledFeatures")
//            .setOutputCol("pcaFeatures")
//            .setK(5)
//            .fit(scalerDf)
//        for(x <- 1 to pcaModel.explainedVariance.size){
//            println(x + "\t" + pcaModel.explainedVariance(x-1))
//        }
        val selector = new ChiSqSelector()
            .setNumTopFeatures(1)
            .setFeaturesCol("scaledFeatures")
            .setLabelCol("Rlabel")
            .setOutputCol("selectedFeatures")
        /* 构建模型 */
        val lr = new LogisticRegression().setMaxIter(66).setFeaturesCol("selectedFeatures").setLabelCol("Rlabel")
        val pipeLine = new Pipeline().setStages(Array(formular,scaler,selector,lr))

        val paramGrid = new ParamGridBuilder().addGrid(selector.numTopFeatures,Array(3,4,5))
                                                .addGrid(lr.regParam,Array(0.01,0.05,0.1,0.3,0.7,1))
                                                .build()

        val cv = new CrossValidator().setEstimator(pipeLine)
            .setEvaluator(new BinaryClassificationEvaluator())
            .setEstimatorParamMaps(paramGrid)
            .setNumFolds(10)
        val cvModel = cv.fit(data)
        /* 模型存储 */
        val path = new Path("/test/fkmodel")
        if(fileSystem.exists(path)){fileSystem.delete(path,true)}
        cvModel.save("/test/fkmodel")
        /*  模型加载  */
        //val fkModel = CrossValidatorModel.load("/test/fkmodel")
        //获取测试数据
        var testData = spark.read.format("jdbc")
            .option("url","jdbc:sqlserver://tianxibigdata.database.chinacloudapi.cn:1433;DatabaseName=TianxiBigData")
            .option("dbtable","(select top(900)  Gender , Age , Income , OverdueNo90 , AuthorizedAmounts,CreditRatings from creditrating where id > 2960000) aa ")
            .option("driver","com.microsoft.sqlserver.jdbc.SQLServerDriver")
            .option("user","tianxi")
            .option("password","1qa2ws!QA")
            .load()
            .cache()

        testData = testData.withColumn("label",when(data("CreditRatings")==="",0.0).otherwise(1.0))
            .withColumn("Age2",data.col("Age").cast(DataTypes.DoubleType)).drop("Age").withColumnRenamed("Age2","Age")
            .withColumn("Income2",data.col("Income").cast(DataTypes.DoubleType)).drop("Income").withColumnRenamed("Income2","Income")
            .withColumn("OverdueNo902",data.col("OverdueNo90").cast(DataTypes.DoubleType)).drop("OverdueNo90").withColumnRenamed("OverdueNo902","OverdueNo90")
            .withColumn("AuthorizedAmounts2",data.col("AuthorizedAmounts").cast(DataTypes.DoubleType)).drop("AuthorizedAmounts").withColumnRenamed("AuthorizedAmounts2","AuthorizedAmounts")

        val preRs = cvModel.transform(testData)
        preRs.show(10,false)




        spark.stop()
    }
}
