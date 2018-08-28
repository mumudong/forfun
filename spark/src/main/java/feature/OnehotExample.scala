package feature

import breeze.linalg.Matrix
import common.Agefunc
import org.apache.spark.ml.feature._
import org.apache.spark.ml.linalg.{DenseMatrix, Matrix, Vector, Vectors}
import org.apache.spark.mllib.linalg.SingularValueDecomposition
import org.apache.spark.mllib.linalg.distributed.RowMatrix
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.junit.Test

/**
  * Created by Administrator on 2018/6/7.
  */
class OnehotExample {

    @Test
    def oneHot(): Unit = {
        val spark = SparkSession.builder.appName("oneHot ho ho")
            .master("local").getOrCreate()
        val df = spark.createDataFrame(Seq((0, "a"),
            (1, "b"),
            (2, "c"),
            (3, "a"),
            (4, "a"),
            (5, "c"))).toDF("id", "category")

        val indexer = new StringIndexer().setInputCol("category")
            .setOutputCol("categoryIndex")
            .fit(df).transform(df)
        indexer.show()
        val encoder = new OneHotEncoder().setDropLast(false)
            .setInputCol("categoryIndex")
            .setOutputCol("oneHot").transform(indexer)

        encoder.show()
        spark.stop()
    }

    @Test
    def maxMin(): Unit = {
        val spark = SparkSession.builder.appName("minMax ho ho")
            .master("local").getOrCreate()
        val df = spark.createDataFrame(Seq(
            (0, Vectors.dense(1.0, 0.1, -1.0)),
            (1, Vectors.dense(2.0, 3.1, 1.0)),
            (2, Vectors.dense(3.0, 10.1, 3.0))
        )).toDF("id", "features")

        val scaler = new MinMaxScaler().setInputCol("features")
            .setOutputCol("scaledFeatures")
        val scalerDf = scaler.fit(df)
            .transform(df)
        println(s"Features scaled to range:[${scaler.getMin},${scaler.getMax}]")
        scalerDf.show()
        spark.stop()
    }

    @Test
    def rFormulor(): Unit = {

        val spark = SparkSession.builder.appName("rFormula ho ho")
            .master("local").getOrCreate()
        val dataset = spark.createDataFrame(Seq(
            (7, "US", 18, 1.0),
            (8, "CA", 12, 0.0),
            (9, "NZ", 15, 0.0),
            (10, "cH", 22, 2.0),
            (11, "NZ", 15, 0.0)
        )).toDF("id", "country", "hour", "clicked")
        val formular = new RFormula().setFormula("clicked ~ country + hour")
            .setFeaturesCol("features")
            .setLabelCol("label")
        val output = formular.fit(dataset).transform(dataset)
        output.select("features", "label").show()
        val scaler = new MinMaxScaler().setInputCol("features")
            .setOutputCol("scaledFeatures")
        val scalerDf: DataFrame = scaler.fit(output)
            .transform(output)
        scalerDf.show()
        scalerDf.printSchema()
//        import spark.implicits._
//        implicit val mapEncoder = org.apache.spark.sql.Encoders.kryo[Vector]
//        val df_to_rdd: RDD[Array[Double]] = scalerDf.select("scaledFeatures").map { x => x.getAs[Vector](0).toArray }.rdd
//        val matrix = new RowMatrix(df_to_rdd.map(x => org.apache.spark.mllib.linalg.Vectors.dense(x)))
//        println(matrix.computePrincipalComponents(3))
//        val svd = matrix.computeSVD(k = 4)
//        println("svd.s ----> \n" + svd.s)
//        println("svd.U ----> \n" + svd.U)
//        println("svd.v ----> \n" + svd.V)
        val pcaModel = new PCA().setInputCol("scaledFeatures")
                                .setOutputCol("pcaFeatures")
                                .setK(4)
                                .fit(scalerDf)
        for(x <- 1 to pcaModel.explainedVariance.size){
            println(x + "\t" + pcaModel.explainedVariance(x-1))
        }
        spark.stop()

    }
    @Test
    def bucketizer(): Unit = {
        //df.withColumn("column22", sqlfunc(col("column1"), lit(1), lit(3))，函数里面传常量只有这样才可以实现
        val spark = SparkSession.builder.appName("bucketizer ho ho")
            .master("local").getOrCreate()
        spark.udf.register("fun",Agefunc,DataTypes.StringType)
        val df = spark.createDataFrame(Seq((0, "a"),
            (10, "b"),
            (20, "c"),
            (30, "a"),
            (40, "a"),
            (50, "c"))).toDF("id", "category")
        val cl = df.selectExpr("id","category","fun('age',Id)").toDF("id","category","score")
        cl.show()
        val df2 = df.withColumn("id2",df("id").cast(DataTypes.DoubleType))
        val bucketizer = new Bucketizer().setInputCol("id2")
                                        .setOutputCol("bucket")
                                        .setSplits(Array(0,30,60))
        val bucketData = bucketizer.transform(df2)
        bucketData.show()
        spark.stop()
    }

}

