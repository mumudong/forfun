package trident.unstatetransaction;

import org.apache.storm.topology.FailedException;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2018/7/2.
 */
public class WordFunction extends BaseFunction {
    /**
     *
     */
    private static final long serialVersionUID = 735468688795780833L;

    /**
     * 接收数据流 每次接收batch中一条数据
     * FailedExcetion
     * TridentBoltExecutor捕获到FailedException后调用了failBatch方法，继续跟踪failBatch方法最终会
     * 在事务对象TransactionAttempt上transactionId不变, attempId +1并调用spout的emitBatch方法。
     */
    @Override
    public void execute(TridentTuple tuple, TridentCollector collector) {
        String value = tuple.getValueByField("field1").toString();
        System.out.println("funtion value : " + value);
        if (value.charAt(0) > 'h' && value.charAt(0) < 'n') {
            throw new FailedException();
        }
        collector.emit(new Values(tuple.getString(1),1));
    }
    private Logger logger = LoggerFactory.getLogger(getClass());
}