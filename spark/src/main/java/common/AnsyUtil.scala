package common

import java.io.{BufferedReader, File, FileReader}

import org.ansj.library.UserDefineLibrary
import org.ansj.recognition.impl.FilterRecognition
import org.ansj.splitWord.analysis.{NlpAnalysis, ToAnalysis}
import org.apache.spark.sql.Row
import org.nlpcn.commons.lang.tire.domain.Forest
import org.nlpcn.commons.lang.tire.library.Library
/**
  * Created by MuDong on 2017/8/24.
  */
object AnsyUtil{
    def main(args: Array[String]): Unit = {
//        val negativeNews = JDBCHelper.query("select id,content from \"public\"." + "test_tieba3").toArray[BaiduNewsResult](Array())
//         negativeNews.sortWith((n1,n2)=>n1.getId<n2.getId).foreach(AnsyUtil.ansjFenci(_))
//        println(removeEnglish("<noscript><img src=\"https://pic2.zhimg.com/a01ff1d1425b6b1bbbf8a3e3540dd095_b.png\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb\" width=\"450\" data-original=\"https://pic2.zhimg.com/a01ff1d1425b6b1bbbf8a3e3540dd095_r.png\"></noscript><img src=\"data:image/svg+xml;utf8,&lt;svg%20xmlns='http://www.w3.org/2000/svg'%20width='450'%20height='800'&gt;&lt;/svg&gt;\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb lazy\" width=\"450\" data-original=\"https://pic2.zhimg.com/a01ff1d1425b6b1bbbf8a3e3540dd095_r.png\" data-actualsrc=\"https://pic2.zhimg.com/a01ff1d1425b6b1bbbf8a3e3540dd095_b.png\"><noscript><img src=\"https://pic2.zhimg.com/06d97aa385a828787a6c15cb09730b8d_b.png\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb\" width=\"450\" data-original=\"https://pic2.zhimg.com/06d97aa385a828787a6c15cb09730b8d_r.png\"></noscript><img src=\"data:image/svg+xml;utf8,&lt;svg%20xmlns='http://www.w3.org/2000/svg'%20width='450'%20height='800'&gt;&lt;/svg&gt;\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb lazy\" width=\"450\" data-original=\"https://pic2.zhimg.com/06d97aa385a828787a6c15cb09730b8d_r.png\" data-actualsrc=\"https://pic2.zhimg.com/06d97aa385a828787a6c15cb09730b8d_b.png\"><noscript><img src=\"https://pic2.zhimg.com/d86d49a9669aa0b60c19a47dd4080589_b.png\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb\" width=\"450\" data-original=\"https://pic2.zhimg.com/d86d49a9669aa0b60c19a47dd4080589_r.png\"></noscript><img src=\"data:image/svg+xml;utf8,&lt;svg%20xmlns='http://www.w3.org/2000/svg'%20width='450'%20height='800'&gt;&lt;/svg&gt;\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb lazy\" width=\"450\" data-original=\"https://pic2.zhimg.com/d86d49a9669aa0b60c19a47dd4080589_r.png\" data-actualsrc=\"https://pic2.zhimg.com/d86d49a9669aa0b60c19a47dd4080589_b.png\"><noscript><img src=\"https://pic2.zhimg.com/60a0dae145cca3c9d486a50a46d2ca19_b.png\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb\" width=\"450\" data-original=\"https://pic2.zhimg.com/60a0dae145cca3c9d486a50a46d2ca19_r.png\"></noscript><img src=\"data:image/svg+xml;utf8,&lt;svg%20xmlns='http://www.w3.org/2000/svg'%20width='450'%20height='800'&gt;&lt;/svg&gt;\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb lazy\" width=\"450\" data-original=\"https://pic2.zhimg.com/60a0dae145cca3c9d486a50a46d2ca19_r.png\" data-actualsrc=\"https://pic2.zhimg.com/60a0dae145cca3c9d486a50a46d2ca19_b.png\"><noscript><img src=\"https://pic3.zhimg.com/aca639311e85aa6b82f37b38c39e291e_b.png\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb\" width=\"450\" data-original=\"https://pic3.zhimg.com/aca639311e85aa6b82f37b38c39e291e_r.png\"></noscript><img src=\"data:image/svg+xml;utf8,&lt;svg%20xmlns='http://www.w3.org/2000/svg'%20width='450'%20height='800'&gt;&lt;/svg&gt;\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb lazy\" width=\"450\" data-original=\"https://pic3.zhimg.com/aca639311e85aa6b82f37b38c39e291e_r.png\" data-actualsrc=\"https://pic3.zhimg.com/aca639311e85aa6b82f37b38c39e291e_b.png\"><noscript><img src=\"https://pic2.zhimg.com/d294e7a435b6daffde5ed68e96f7ef0d_b.png\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb\" width=\"450\" data-original=\"https://pic2.zhimg.com/d294e7a435b6daffde5ed68e96f7ef0d_r.png\"></noscript><img src=\"data:image/svg+xml;utf8,&lt;svg%20xmlns='http://www.w3.org/2000/svg'%20width='450'%20height='800'&gt;&lt;/svg&gt;\" data-rawheight=\"800\" data-rawwidth=\"450\" class=\"origin_image zh-lightbox-thumb lazy\" width=\"450\" data-original=\"https://pic2.zhimg.com/d294e7a435b6daffde5ed68e96f7ef0d_r.png\" data-actualsrc=\"https://pic2.zhimg.com/d294e7a435b6daffde5ed68e96f7ef0d_b.png\"><br>    谢谢楼主的提问，我心里其实也有疑问，希望抛砖引玉得到更多高人的指点。以上截屏是我满心疑惑向自己校友的提问，涉及隐私所以打了马赛克。<br><br><br>今天事情又有了进展，不过这个帖子关注度不高，等到有空我再慢慢"))
        testDic()
    }

    /**
      * ansj分词器
      * @param news
      * @return
      */
    def  ansjFenci(news:BaiduNewsResult):Array[String]={


        val filter = new FilterRecognition()
        filter.insertStopNatures("w","u","ud","uz","uj","uv","ul","ug") //去掉中文标点符号,去掉助词
        filter.insertStopWord(" ","　","!",",",">","<",")","(","|","-",":") //去掉空格和英文标点
        //精准分词
        var terms = ToAnalysis.parse(news.getContent.replaceAll("[a-zA-Z\\pP]"," ")).recognition(filter)
        if(terms.size()==0){
            return Array[String]("")
        }
//        println("分词---->\tid="+news.getId+"\t"+terms.toStringWithOutNature())
        return terms.toStringWithOutNature().split(",")
    }

    def  ansjRowFenci(news:Row):Array[String]={

//        UserDefineLibrary.insertWord("信和财富", "userDefine", 1000)
//        UserDefineLibrary.insertWord("不可靠", "userDefine", 1000)
//        UserDefineLibrary.insertWord("信和汇金", "userDefine", 1000)
//        UserDefineLibrary.insertWord("宜人贷", "userDefine", 1000)
//        UserDefineLibrary.insertWord("恒昌财富", "userDefine", 1000)
        val filter = new FilterRecognition()
        filter.insertStopNatures("w","u","ud","uz","uj","uv","ul","ug");//去掉中文标点符号,去掉助词
        filter.insertStopWord(" ","　","!",",",">","<",")","(","|","-",":")//去掉空格和英文标点
        //精准分词
        var terms = ToAnalysis.parse(news.getAs[String]("content").replaceAll("[a-zA-Z\\pP]"," ")).recognition(filter)
        if(terms.size()==0){
            return Array[String]("")
        }
        //        println("分词---->\tid="+news.getId+"\t"+terms.toStringWithOutNature())
        return terms.toStringWithOutNature().split(",")
    }
    /**
      * ansj分词器
      * @param news
      * @return
      */
    def  stringFenci(news:String):Array[String]={

        UserDefineLibrary.insertWord("信和财富", "userDefine", 1000)
        UserDefineLibrary.insertWord("不可靠", "userDefine", 1000)
        UserDefineLibrary.insertWord("信和汇金", "userDefine", 1000)
        UserDefineLibrary.insertWord("宜人贷", "userDefine", 1000)
        UserDefineLibrary.insertWord("恒昌财富", "userDefine", 1000)
        val filter = new FilterRecognition()
        filter.insertStopNatures("w","u","ud","uz","uj","uv","ul","ug");//去掉中文标点符号,去掉助词
        filter.insertStopWord(" ","　","!",",",">","<",")","(","|","-",":")//去掉空格和英文标点
        //精准分词
        var terms = ToAnalysis.parse(news.replaceAll("[a-zA-Z\\pP]"," ")).recognition(filter)
        if(terms.size()==0){
            return Array[String]("")
        }
        //        println("分词---->\tid="+news.getId+"\t"+terms.toStringWithOutNature())
        return terms.toStringWithOutNature().split(",")
    }

    /**
      * 计算所有新闻的分词数量
      */
    def calcCharact():Unit={
        var list=List[String]("a")
        val jdbcHelper = JDBCHelper.getJDBCHelper(true);
        val news = jdbcHelper.query("select * from \"public\".test " ).toArray[BaiduNewsResult](Array())
        for(n <- news){
            val li=ansjFenci(n).toList
            list=list.:::(li)
        }
        val sa=list.toSet
        println("新闻条数====="+news.size+"\n分词数====="+sa.size)
    }
    def testDic():Unit={
        val term = ToAnalysis.parse("罗毅虎和曹罗伟是高中同学，罗平是放高利贷的人，不久前跑路了")
        println("term-->" + term)

        val reader = new FileReader("D:\\ksdler\\wechat_webdriver_spider-master\\hx-crawler\\newsanalys\\src\\main\\resources\\library\\ntusd-negative.txt")
        val bufferReader = new BufferedReader(reader)
        var s:String = ""
        while((s = bufferReader.readLine()) != null && !s.trim().equals("")){
            UserDefineLibrary.insertWord(s,"negative.dic",1000)
        }
        bufferReader.close()
        reader.close()

        val terms = ToAnalysis.parse("罗毅虎和曹罗伟是高中同学，罗平是放高利贷的人，不久前跑路了")
        println("terms-->" + terms)
    }
    def removeEnglish(words:String):String={
        val str = words.replaceAll("[a-zA-Z\\pP]","")
        str
    }



}
