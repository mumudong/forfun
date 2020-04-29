package datastream;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.IterativeStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class app080 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Long> someIntegers = env.addSource(new SourceFunction<Long>() {
            @Override
            public void run(SourceContext<Long> ctx) throws Exception {
                long i = 0;
                while (true) {
                    System.out.println("初始值==》" + i);
                    ctx.collect(i);
                    i = i + 1;
                    Thread.sleep(2000);
                }
            }

            @Override
            public void cancel() {

            }
        });
        // 创建迭代流
        IterativeStream<Long> iteration = someIntegers.iterate();
        // 增加处理逻辑，对元素执行减一操作。
        DataStream<Long> minusOne = iteration.map(new MapFunction<Long, Long>() {
            @Override
            public Long map(Long value) throws Exception {
                return value - 1;
            }
        });

        // 获取要进行迭代的流，
        DataStream<Long> stillGreaterThanZero = minusOne.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long value) throws Exception {
                return (value > 0);
            }
        });
        // 对需要迭代的流形成一个闭环
        iteration.closeWith(stillGreaterThanZero);
        minusOne.print("iter ---");

        // 小于等于0的数据继续向前传输
        DataStream<Long> lessThanZero = minusOne.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long value) throws Exception {
                return (value <= 0);
            }
        });
        lessThanZero.print();


        env.execute();

    }
}