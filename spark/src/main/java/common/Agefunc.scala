package common

import org.apache.spark.sql.api.java.UDF2

/**
  * Created by Administrator on 2018/6/19.
  */
object Agefunc extends UDF2[String,Int,String]{
    //年纪 年龄大于100分数减10000
    val age = Array(30,35,40,45,50,55,60,65,75,100)
    val ageScore = Array(30,20,17,11,5,0,-5,-11,-19,-26,-30 ).map(x => x.toString)
    //appOverdueCount 发生过逾期的贷款个数
    val appOverdueCount = Array(0,1,3,5)
    val appOverdueCountScore = Array(50,47,34,18,-10).map(x => x.toString)
    //monthAfterMoney	月税后工资
    val monthAfterMoney = Array(1000,5000,10000,15000,20000,25000,30000,35000,40000)
    val monthAfterMoneyScore = Array(-10,0,4,8,12,16,20,25,30,35).map(x => x.toString)
    //ccOverdueTimesL60S 	简版信用卡信息-信用卡单张5年内累计逾期次数 
    val ccOverdueTimesL60S = Array(0,1,3,5,10)
    val ccOverdueTimesL60SScore = Array(30,11,0,-5,-10,-33).map(x => x.toString)
    //ccDue90TimesL60S 	简版信用卡信息-信用卡5年内出现90天以上的逾期次数
    val ccDue90TimesL60S = Array(0,1,2,3,5)
    val ccDue90TimesL60SScore = Array(20,6,-6,-14,-22,-40).map(x => x.toString)
    //plOverdueTimesL60S 	简版贷款信息-贷款单笔5年内累计逾期次数
    val plOverdueTimesL60S = Array(0,1,3,5)
    val plOverdueTimesL60SScore = Array(27,10,-5,-10,-20).map(x => x.toString)
    //plDue90TimesL60S 	简版贷款信息-贷款5年内出现90天以上的逾期次数
    val plDue90TimesL60S = Array(0,1,2,3,5)
    val plDue90TimesL60SScore = Array(27,13,-2,-7,-11,-15).map(x => x.toString)
    /* overdue6Timesl6	近6个月逾期6天以上（不含）的次数
       此处规则有问题
     */
    val overdue6Timesl6 = Array(0,1,2,3,5)
    val overdue6Timesl6Score = Array(8,4,0,-3,-4,-8).map(x => x.toString)

    val edu = Array(1000,5000,10000,15000,20000,30000,40000)
    val eduScore = Array(-10,0,4,8,12,16,20,25).map(x => x.toString)

    override def call(func:String,input:Int):String= {
        binarySearchByItem(func,input)
    }
    def binaryIntSearch(num:Array[Int],  number:Int):Int = {
        if(number <= num(0))
             return 0
        if(number > num(num.length-1))
            return num.length
        var start,end,mid = 0
        end = num.length - 1
        while (start <= end ) {
            mid = (start + end) / 2
            if(mid == start)
                return start + 1
            if (num(mid) == number)
                return mid ;
            else if (num(mid) > number) {
                end = mid ;
            } else {
                start = mid
            }
        }
        return -1
    }
    def binarySearchByItem( item:String, value:Int):String = {
        if("age".equals(item))
            return ageScore(binaryIntSearch(age,value))
        else if("income".equals(item))
            return monthAfterMoneyScore(binaryIntSearch(monthAfterMoney,value))
        else if("yuqi".equals(item))
            return ccDue90TimesL60SScore(binaryIntSearch(ccDue90TimesL60S,value))
        else if("sx".equals(item))
            return eduScore(binaryIntSearch(edu,value))
        else if("label".equals(item)){
            if(value>3) "0" else "1"
        }
        else
        return "异常"
    }
}
