package trident.window;

import org.apache.storm.trident.operation.BaseAggregator;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.FlatMapFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 窗口函数测试
 */
public class WordAggregator extends BaseAggregator<HashMap<String,Long>>{
    @Override
    public HashMap<String, Long> init(Object batchId, TridentCollector collector) {
        return new HashMap<String,Long>();
    }

    @Override
    public void aggregate(HashMap<String, Long> val, TridentTuple tuple, TridentCollector collector) {
        String word = tuple.getStringByField("word");
        long count = 1;
        if(val.containsKey(word))
            count += val.get(word);
        val.put(word,count);
    }

    @Override
    public void complete(HashMap<String, Long> val, TridentCollector collector) {
        collector.emit(new Values(val));
    }
}
class SplitFunction implements FlatMapFunction{
    @Override
    public Iterable<Values> execute(TridentTuple input) {
        ArrayList<Values> values = new ArrayList<Values>();
        String sentence = input.getStringByField("str");
        String[] split = sentence.split(" ");
        for (String s:split)
            values.add(new Values(s));
        return values;
    }
}
class TopNFunction extends BaseFunction{
    private int topN;
    public TopNFunction(int n){
        this.topN = n;
    }

    /**
     * 没有数据的时候此方法不会执行
     * @param tuple
     * @param collector
     */
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        HashMap<String,Long> hashMap = (HashMap<String,Long>) tuple.get(0);
        List<Map.Entry<String,Long>> list = new ArrayList<Map.Entry<String, Long>>(hashMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            @Override
            public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        int i = 1;
        for(int j = list.size() - 1;j >= 0;j--){
            if(i > topN)
                break;
            collector.emit(new Values(String.valueOf(i),list.get(j).getKey(),String.valueOf(list.get(j).getValue())));
            System.out.println("Sending: " + i + " " + list.get(j).getKey() + ":" + list.get(j).getValue());
            i++;
        }
        System.out.println("----------------done-------------------" + LocalDateTime.now());
    }
}