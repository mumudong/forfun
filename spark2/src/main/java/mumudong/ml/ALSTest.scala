package mumudong.ml

import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.SparkSession

/**
  * 协同过滤矩阵分解算法
  *     奇异值分解(不允许矩阵有null值)
  *     正则化矩阵分解:
  *             为解决稀疏矩阵可能过学习的问题，评价矩阵分解采用RMSE
  *     带偏置的矩阵分解：用户偏好侧重不同
  *
  *  Spark采用的是带正则化矩阵分解，优化函数选择交叉最小二乘法ALS
  */
object ALSTest {
    case class Rating(userId:Int,movieId:Int,rating:Float,timestamp:Long)
    def parseRating(str:String):Rating = {
        val fields = str.split("::")
        assert(fields.length == 4)
        Rating(fields(0).toInt,fields(1).toInt,fields(2).toFloat,fields(3).toLong)
    }
    def main(args: Array[String]): Unit = {
        val session = SparkSession.builder()
                                .appName(getClass.getSimpleName)
                                .master("local[2]")
                                .getOrCreate()
        import session.implicits._
        val ratings = session.read.textFile("file:///D:\\ksdler\\git_repository\\forfun\\spark2\\data\\sample_movielens_ratings.txt")
                                        .map(parseRating)
                                        .toDF()
        val Array(traing,test) = ratings.randomSplit(Array(0.8,0.2))
        val als = new ALS().setMaxIter(5)
                            .setRegParam(0.01)
                            .setRank(10)//矩阵的秩
                            .setUserCol("userId")
                            .setItemCol("movieId")
                            .setRatingCol("rating")
                            .setNumUserBlocks(2)//数据量大时设置
        val model = als.fit(traing)
        model.setColdStartStrategy("drop")
        val predictions = model.transform(test)
        val evaluator = new RegressionEvaluator()
                                .setMetricName("rmse")
                                .setLabelCol("rating")
                                .setPredictionCol("prediction")
        val rmse = evaluator.evaluate(predictions)
        println(s"root-mean-square error --> $rmse")
        val userRecs = model.recommendForAllUsers(10)//每个用户推荐十个物品
        val movieRecs = model.recommendForAllItems(10)//每部电影推荐是个人
        val users = ratings.select(als.getUserCol).distinct().limit(3)
        val userSubsetRecs = model.recommendForUserSubset(users,10)//这些人推荐10个物品
        println("userRecs")
        userRecs.show(false)
        println("movieRecs")
        movieRecs.show(false)
        println("userSubsetRecs")
        userSubsetRecs.show(false)
        session.stop()
    }
}
