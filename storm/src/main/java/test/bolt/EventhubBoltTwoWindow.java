package test.bolt;


import test.common.BinarySearch;
import test.common.C3POUtil;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.sql.Timestamp;
import java.util.Map;

@SuppressWarnings("all")
public class EventhubBoltTwoWindow extends BaseRichBolt {
    private OutputCollector _collector;
    int messageSize = 0;
    @SuppressWarnings("rawtypes")
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        _collector = collector;
    }



    public String[][] getS(String s){
        String[] a = s.split("\n");
        String[][] result = new String[a.length][];
        for(int i = 0;i<a.length;i++){
            result[i] = a[i].split(",");
        }
        return result;
    }
    public void execute(Tuple input) {
        messageSize = 0;
    	String msg = input.getString(0);
        System.out.println("接收数据----------->" + msg);
        if(msg.contains("\n")){
            String[][] strlist = getS(msg);
            for(String[] strs:strlist){
                messageSize ++;
                if(strs.length == 7){
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
            }
        }else{
            String[] strs = msg.split(",");
            messageSize =  1;
            if(strs.length == 7){
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
        }
        _collector.emit(input,new Values(messageSize));
        _collector.ack(input);
    }

    public void cleanup() {
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("count"));
    }
}