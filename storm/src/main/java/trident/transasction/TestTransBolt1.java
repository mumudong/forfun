package trident.transasction;

import org.apache.storm.coordination.BatchOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseTransactionalBolt;
import org.apache.storm.transactional.TransactionAttempt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * Created by Administrator on 2018/5/30.
 */
public class TestTransBolt1 extends BaseTransactionalBolt {

    private static final long serialVersionUID = 1L;
    private BatchOutputCollector _outputCollector;
    private TransactionAttempt _tx;
    private int count = 0;
    private TopologyContext _context;

    public TestTransBolt1() {
        System.err.println("start TestTransBolt1 ");
    }

    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map conf,
                        TopologyContext context, BatchOutputCollector collector,
                        TransactionAttempt id) {
        this._context = context;
        this._outputCollector = collector;
        System.err.println("1 prepare TestTransBolt1 TransactionId:"
                + id.getTransactionId() + ",AttemptId:" + id.getAttemptId());

    }

    @Override
    public void execute(Tuple tuple) {
        _tx = (TransactionAttempt) tuple.getValueByField("tx");
        String content = tuple.getStringByField("count");
        System.err.println("bolt1 execute TaskId:"+_context.getThisTaskId()+",TestTransBolt1 TransactionAttempt "
                + _tx.getTransactionId() + "  attemptid" + _tx.getAttemptId());
        if (null != content && !content.isEmpty()) {
            count++;
        }
    }

    @Override
    public void finishBatch() {
        System.out.println("bolt1 finishbatch TaskId:"+_context.getThisTaskId() + ",TestTransBolt1 TransactionAttempt "
                + _tx.getTransactionId() + ",finishBatch count:"+count);
        _outputCollector.emit(new Values(_tx, count));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("tx", "count"));
    }

}
