package org.apache.spark.ml.feature

import common.Agefunc
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.types._
import org.apache.spark.ml.param._
import org.apache.spark.ml.util.Identifiable
//import com.hs.xlzf.Utils.SparkUtil
import org.apache.spark.ml.param.shared._
import org.apache.spark.ml.util._
import org.apache.spark.ml.Estimator
import org.apache.spark.ml.attribute._
/**
  * Created by Administrator on 2018/6/19.
  */
object DiscretizerTest {
    def discretizer(): Unit = {
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

        val discretizer = new QuantileDiscretizer().setInputCol("id")
                                                        .setOutputCol("ids")
                                                        .setNumBuckets(3)



        df.show()
        spark.stop()
    }

    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder.appName("bucketizer ho ho")
            .master("local").getOrCreate()
        val inputCol1 = "f1"
        val inputCol2 = "f2"
        val labelCol = "label"
        val outputCol1 = "discretizer1"
        val outputCol2 = "discretizer2"

        val train = spark.createDataFrame(
            List(
                (1, 2.3, 0),
                (2, 8.1, 0),
                (3, 1.1, 1),
                (4, 2.2, 1),
                (5, 3.3, 0),
                (6, 7.0, 1))).toDF(inputCol1, inputCol2, labelCol)

        val test = spark.createDataFrame(
            List(
                (1, 7.1),
                (7, 8.1))).toDF(inputCol1, inputCol2)

        val discretizer = new Discretizer().
            setInputCol(inputCol2).
            setOutputCol(outputCol2).
            setNumBuckets(2).
            setLabelCol(labelCol).
            setMinInstancesPerBucket(1)

        val model = discretizer.fit(train)

        model.transform(test).show()
        model.getSplits.foreach {
            arr => println(arr )
        }
    }
}
private[feature] trait DiscretizerBase extends Params
    with HasHandleInvalid with HasInputCol with HasOutputCol
    with HasInputCols with HasOutputCols with HasLabelCol {

    final val impurity: Param[String] = new Param[String](this, "impurity", "Criterion used for information gain calculation (case-insensitive). " +
        " Supported: \"entropy\" and \"gini\". (default = gini)",
        ParamValidators.inArray(Array("gini", "entropy")))
    final val minInfoGain: DoubleParam = new DoubleParam(this, "minInfoGain", "分组的最小信息增益（不包含），需非负数，默认0.0", ParamValidators.gtEq(0.0))
    final val numBuckets: IntParam = new IntParam(this, "numBuckets", "离散分桶数量，正整数", ParamValidators.gtEq(2))
    val numBucketsArray = new IntArrayParam(this, "numBucketsArray", "Array of number of buckets " +
        "(quantiles, or categories) into which data points are grouped. This is for multiple " +
        "columns input. If transforming multiple columns and numBucketsArray is not set, but " +
        "numBuckets is set, then numBuckets will be applied across all columns.",
        (arrayOfNumBuckets: Array[Int]) => arrayOfNumBuckets.forall(ParamValidators.gtEq(2)))

    final val minInstancesPerBucket: IntParam = new IntParam(this, "minInstancesPerBucket", "每个桶最少记录数量（包含），默认1")
    def getImpurity() = $(minInfoGain)
    def getMinInfoGain() = $(minInfoGain)
    def getNumBuckets() = $(numBuckets)
    def getNumBucketsArray: Array[Int] = $(numBucketsArray)
    def getMinInstancesPerBucket() = $(minInstancesPerBucket)

//    override val handleInvalid: Param[String] = new Param[String](
//        this,
//        "handleInvalid", "how to handle invalid entries. Options are skip (which will filter out rows with bad values), " +
//            "or error (which will throw an error) or keep (keep invalid values in a special additional bucket).",
//        ParamValidators.inArray(Array("skip", "error", "keep")))

    def setImpurity(value: String): this.type = set(impurity, value)
    def setHandleInvalid(value: String): this.type = set(handleInvalid, value)
    def setLabelCol(value: String): this.type = set(labelCol, value)
    def setInputCol(value: String): this.type = set(inputCol, value)
    def setOutputCol(value: String): this.type = set(outputCol, value)
    def setInputCols(value: Array[String]): this.type = set(inputCols, value)
    def setOutputCols(value: Array[String]): this.type = set(outputCols, value)
    def setMinInfoGain(value: Double): this.type = set(minInfoGain, value)
    def setNumBuckets(value: Int): this.type = set(numBuckets, value)
    def setNumBucketsArray(value: Array[Int]): this.type = set(numBucketsArray, value)
    def setMinInstancesPerBucket(value: Int): this.type = set(minInstancesPerBucket, value)

    setDefault(minInfoGain -> 0.0, labelCol -> "label", minInstancesPerBucket -> 1, handleInvalid -> "error", impurity -> "gini")

    protected def getInOutCols: (Array[String], Array[String]) = {
//        require(
//            (isSet(inputCol) && isSet(outputCol) && !isSet(inputCols) && !isSet(outputCols)) ||
//                (!isSet(inputCol) && !isSet(outputCol) && isSet(inputCols) && isSet(outputCols)),
//            "Discretizer only supports setting either inputCol/outputCol or " +
//                "inputCols/outputCols.")

        if (isSet(inputCol)) {
            (Array($(inputCol)), Array($(outputCol)))
        } else {
            require(
                $(inputCols).length == $(outputCols).length,
                "inputCols number do not match outputCols")
            ($(inputCols), $(outputCols))
        }
    }

}

class Discretizer(override val uid: String) extends Estimator[Bucketizer]
    with DiscretizerBase with DefaultParamsWritable {
    def this() = this(Identifiable.randomUID("Discretizer"))
    override def copy(extra: ParamMap): this.type = defaultCopy(extra)

    override def fit(dataset: Dataset[_]): Bucketizer = {
        transformSchema(dataset.schema, true)
        val bucketizer = new Bucketizer(uid).setHandleInvalid($(handleInvalid))
        val (inputColNames, outputColNames) = getInOutCols

        val numBucketsArray_t = if (isSet(numBucketsArray) && isSet(inputCols)) {
            $(numBucketsArray)
        } else {
            Array.fill[Int](inputColNames.size)($(numBuckets))
        }

        val splitsArrayBuffer = new ArrayBuffer[Array[Double]]()
        inputColNames.zip(numBucketsArray_t).foreach {
            case (inputColName, numBuckets_t) =>
                val splits = deiscretizeCol(dataset, inputColName, numBuckets_t)
                splitsArrayBuffer += splits.sorted
        }

        if (splitsArrayBuffer.size == 1) {
            val splits = splitsArrayBuffer.head
            bucketizer.setSplits(splits)
        } else {
//            var splitsArray = splitsArrayBuffer.toArray
//            splitsArray.foreach(f => f.foreach(println))
//            bucketizer.setSplitsArray(splitsArray)
        }
        copyValues(bucketizer.setParent(this))
    }

    override def transformSchema(schema: StructType): StructType = {
        val (inputColNames, outputColNames) = getInOutCols
        val existingFields = schema.fields
        var outputFields = existingFields
        inputColNames.zip(outputColNames).foreach {
            case (inputColName, outputColName) =>
                require(
                    existingFields.exists(_.name == inputColName),
                    s"Iutput column ${inputColName} not exists.")
                require(
                    existingFields.forall(_.name != outputColName),
                    s"Output column ${outputColName} already exists.")
                val inputColType = schema(inputColName).dataType
                require(
                    inputColType.isInstanceOf[NumericType],
                    s"The input column $inputColName must be numeric type, " +
                        s"but got $inputColType.")

                val attr = NominalAttribute.defaultAttr.withName(outputColName)
                outputFields :+= attr.toStructField()
        }
        StructType(outputFields)
    }

    def deiscretizeCol(dataset: Dataset[_], inputColName: String, numBuckets_t: Int) = {
        val input_arr = dataset.select(col(inputColName).cast(DoubleType)).distinct().orderBy(inputColName).rdd.map(_.getDouble(0)).collect()
        val splits = new ArrayBuffer[Double]()
        splits.append(Double.MinValue)
        splits.append(Double.MaxValue)

        var split_map_arr = new ArrayBuffer[scala.collection.mutable.Map[String, Any]]()
        split_map_arr.append(scala.collection.mutable.Map(
            "arr" -> input_arr,
            "closure" -> true,
            "node" -> null))

        var flag = true
        while (flag) {
            for (split_map <- split_map_arr) {
                if (split_map("node") == null) {
                    getBestPoint(split_map, dataset, inputColName)
                }
            }

            split_map_arr = split_map_arr.filter {
                split_map => split_map("node").asInstanceOf[Map[String, Double]]("value") > $(minInfoGain)
            }

            if (split_map_arr.length > 0) {
                val entropy_idxs = (Map[Double, Array[Int]]() /: split_map_arr.zipWithIndex) { (r, split_map_idx) =>
                    val (split_map, idx) = split_map_idx
                    val value = split_map("node").asInstanceOf[Map[String, Double]]("value")

                    r + (value -> (r.get(value) match {
                        case Some(arr: Array[Int]) => arr :+ idx
                        case None => Array[Int](idx)
                    }))
                }

                val split_map_arr_break = new ArrayBuffer[scala.collection.mutable.Map[String, Any]]()

                entropy_idxs(entropy_idxs.keys.max).zipWithIndex.foreach {
                    case (idx, i) => {
                        split_map_arr_break.append(split_map_arr.remove(idx - i))
                    }
                }

                split_map_arr_break.foreach(
                    split_map => {
                        val point = split_map("node").asInstanceOf[Map[String, Double]]("point")
                        splits.append(point)
                        val arr = split_map("arr").asInstanceOf[Array[Double]]
                        val closure = split_map("closure").asInstanceOf[Boolean]

                        var left_arr = Array[Double]()
                        var right_arr = Array[Double]()
                        for (e <- arr) {
                            if (e < point) {
                                left_arr :+= e
                            } else {
                                right_arr :+= e
                            }
                        }
                        left_arr :+= point

                        val left_split_map = scala.collection.mutable.Map(
                            "arr" -> left_arr,
                            "closure" -> false,
                            "node" -> null)

                        val right_split_map = scala.collection.mutable.Map(
                            "arr" -> right_arr,
                            "closure" -> closure,
                            "node" -> null)

                        split_map_arr.append(left_split_map)
                        split_map_arr.append(right_split_map)
                    })

                if (splits.length - 1 >= numBuckets_t) {
                    flag = false
                }
            } else {
                flag = false
            }
        }

        splits.toArray
    }

    /*
       * 获取最大信息增益分割点
       */
    def getBestPoint(split_map: scala.collection.mutable.Map[String, Any], dataset: Dataset[_], inputColName: String) {
        val arr: Array[Double] = split_map("arr").asInstanceOf[Array[Double]]
        if (arr.length <= 1) {
            split_map("node") = Map("point" -> arr(0), "value" -> 0.0)
            return
        }

        val start = arr(0)
        val end = arr(arr.length - 1)

        val closure = split_map("closure").asInstanceOf[Boolean]
        var ds = dataset.filter(col(inputColName) >= start and col(inputColName) < end)
        if (closure) {
            ds = dataset.filter(col(inputColName) >= start and col(inputColName) <= end)
        }

        val point_set = ds.select(col(inputColName).cast(DoubleType), col($(labelCol))).
            groupBy(col(inputColName)).
            pivot($(labelCol)).count.
            orderBy(col(inputColName)).collect()

        val classNums = point_set(0).size - 1
        val all_seq = (new Array[Long](classNums) /: point_set) { (arr, p) =>
            val idx = (1 to (p.size - 1)).foreach {
                i =>
                    arr(i - 1) += p(i).asInstanceOf[Long]
            }
            arr
        }

        val info_entropy = entropy(all_seq)
        val all_count = all_seq.sum

        var left_count = 0L
        var left_seq = new Array[Long](classNums)
        val gains = arr.zipWithIndex.map {
            case (point, idx) =>
                if (idx == 0) {
                    (point, 0.0)
                } else {
                    left_seq.zipWithIndex.foreach {
                        case (e, i) =>
                            left_seq(i) = e + point_set(idx - 1)(i + 1).asInstanceOf[Long]
                    }

                    val right_seq = all_seq.zip(left_seq).map {
                        case (all_e, left_e) =>
                            all_e - left_e
                    }

                    val left_count = left_seq.sum
                    val right_count = right_seq.sum

                    if (left_count < $(minInstancesPerBucket) || right_count < $(minInstancesPerBucket)) {
                        (point, -1.0)
                    } else {
                        val conditional_entropy = left_count * 1.0 / all_count * entropy(left_seq) + right_count * 1.0 / all_count * entropy(right_seq)
                        val gain = info_entropy - conditional_entropy
                        (point, gain)
                    }
                }
        }

        val gain_max = gains.map(_._2).max
        val point = gains.filter(_._2 >= gain_max).map(_._1).apply(0)
        val node = Map("point" -> point, "value" -> gain_max)
        split_map("node") = node
    }

    /*
       * 计算信息熵， 单位nat
       */
    def entropy(groupCounts: Seq[Long]) = {
        val count = groupCounts.sum
        if ($(impurity) == "gini") {
            (0.0 /: groupCounts) { (sum, groupCount) =>
                if (groupCount == 0) {
                    sum
                } else {
                    val p = groupCount * 1.0 / count
                    sum + (p * (1 - p))
                }
            }
        } else if ($(impurity) == "entropy") {
            (0.0 /: groupCounts) { (sum, groupCount) =>
                if (groupCount == 0) {
                    sum
                } else {
                    val p = groupCount * 1.0 / count
                    sum + (-p * math.log(p))
                }
            }
        } else { //增加算法
            Double.MaxValue
        }
    }

}


