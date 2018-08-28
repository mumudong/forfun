package test.bolt;


import org.apache.storm.tuple.Values;
import test.common.BinarySearch;
import test.common.C3POUtil;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.sql.Timestamp;
import java.util.Map;

@SuppressWarnings("all")
public class EventhubBoltOne extends BaseRichBolt {
    private OutputCollector _collector;

    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }

    public void execute(Tuple input) {
    	String msg = input.getString(0);
        if(msg.contains("\n"))
            msg = msg.substring(0,msg.indexOf("\n"));
        System.out.println("接收数据----------->" + msg);
        String[] strs = msg.split(",");
        if(strs.length == 7){
//            Long Id ;
            String Gender = strs[6].equals("1")?"男":"女";
            String CustomNo = strs[1];
            String Age = strs[2];
            String AgeScore = BinarySearch.binarySearchByItem("age",Age);
            String Income = strs[3];
            String IncomeScore = BinarySearch.binarySearchByItem("income",Income);
            String OverdueNo90 = strs[4];
            String OverdueNo90Score = BinarySearch.binarySearchByItem("yuqi",OverdueNo90);
            String AuthorizedAmounts = strs[5];
            String AuthorizedAmountsScore = BinarySearch.binarySearchByItem("sx",AuthorizedAmounts);
//            String CreditRatings;
            String IsFraud = "6".equals(OverdueNo90)?"是":"否";
            String EnterTime = strs[0];
            Timestamp CreateTime;
            String CreditRatings = "";
            if("否".equals(IsFraud))
                    CreditRatings = BinarySearch.calcX(Integer.valueOf(Age),
                    Integer.valueOf(Income),
                    Integer.valueOf(OverdueNo90),
                    Integer.valueOf(AuthorizedAmounts)).getStratege();

            String sql = "insert into CreditRating(CustomNo,Gender,Age,AgeScore,Income,IncomeScore," +
                    "OverdueNo90,OverdueNo90Score,AuthorizedAmounts,AuthorizedAmountsScore,CreditRatings,IsFraud,EnterTime) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            try {
                boolean result  = C3POUtil.insertUpdateDelete(sql,CustomNo,Gender,Age,AgeScore,Income,IncomeScore,
                        OverdueNo90,OverdueNo90Score,AuthorizedAmounts,AuthorizedAmountsScore,CreditRatings,IsFraud,EnterTime);
                if(result == false)
                    System.out.println("数据插入失败------------->" + CustomNo);
            } catch (Exception e) {
                System.out.println("sql执行出错了------------->" + CustomNo);
                e.printStackTrace();
            }
        }


//        _collector.emit(input,new Values(msg));
        _collector.ack(input);
    }

    public void cleanup() {
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("JsonMsg"));
    }
}