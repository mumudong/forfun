package org.apache.spark.ml.feature

import org.ansj.library.UserDefineLibrary
import org.ansj.recognition.impl.FilterRecognition
import org.ansj.splitWord.analysis.ToAnalysis
import org.apache.spark.annotation.{Experimental, Since}
import org.apache.spark.ml.UnaryTransformer
import org.apache.spark.ml.param._
import org.apache.spark.ml.util._
import org.apache.spark.sql.types.{ArrayType, DataType, StringType}
import org.nlpcn.commons.lang.tire.domain.Forest
import org.nlpcn.commons.lang.tire.library.Library

/**
  * Created by Mu on 2018/1/16.
  */
@Experimental
class StringtoVector(override val uid: String)
    extends UnaryTransformer[String, Seq[String], StringtoVector] with DefaultParamsWritable {
//    UserDefineLibrary.FOREST=AnsyUtil.forest
    def this() = this(Identifiable.randomUID("tok"))

    override protected def createTransformFunc: String => Seq[String] = {
        ansjRowFenci(_)
    }

    override protected def validateInputType(inputType: DataType): Unit = {
        require(inputType == StringType, s"Input type must be string type but got $inputType.")
    }

    override protected def outputDataType: DataType = new ArrayType(StringType, true)

    override def copy(extra: ParamMap): StringtoVector = defaultCopy(extra)

    def  ansjRowFenci(news:String):Seq[String]={


        val filter = new FilterRecognition()
        filter.insertStopNatures("w","u","ud","uz","uj","uv","ul","ug");//去掉中文标点符号,去掉助词
        filter.insertStopWord(" ","　","!",",",">","<",")","(","|","-",":")//去掉空格和英文标点
        //精准分词
        var terms = ToAnalysis.parse(news.replaceAll("[a-zA-Z\\pP]"," ")).recognition(filter)
        if(terms.size()==0){
            return Array[String]("").toSeq
        }
        //        println("分词---->\tid="+news.getId+"\t"+terms.toStringWithOutNature())
        return terms.toStringWithOutNature().split(",").toSeq
    }
}

@Since("1.6.0")
object StringtoVector extends DefaultParamsReadable[StringtoVector] {

    @Since("1.6.0")
    override def load(path: String): StringtoVector = super.load(path)
}
