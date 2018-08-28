package trident.transasction;

import org.apache.storm.coordination.BatchOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseTransactionalBolt;
import org.apache.storm.transactional.ICommitter;
import org.apache.storm.transactional.TransactionAttempt;
import org.apache.storm.tuple.Tuple;
import org.fusesource.jansi.Ansi;

import java.math.BigInteger;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;


/**
 * Created by Administrator on 2018/5/30.
 */
public class TestTransBolt2 extends BaseTransactionalBolt implements ICommitter {

    private static final long serialVersionUID = 1L;
    private int sum = 0;
    private TransactionAttempt _tx;
    private static int _result = 0;
    private static BigInteger _curtxid=null;
    BatchOutputCollector collector;
    public TestTransBolt2() {
        System.err.println("TestTransBolt2 start!");
    }

    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map conf,
                        TopologyContext context, BatchOutputCollector collector,
                        TransactionAttempt id) {
        this.collector = collector;
        this._tx = id;
        System.err.println("TestTransBolt2 prepare TransactionId:" + id);
    }

    @Override
    public void execute(Tuple tuple) {
        _tx = (TransactionAttempt) tuple.getValueByField("tx");
        sum += tuple.getIntegerByField("count");

        System.err.println("TestTransBolt2 execute TransactionAttempt:" + _tx);
    }

    @Override
    public void finishBatch() {
        System.out.println("bolt2 finishBatch _curtxid:" + _curtxid
                + ",getTransactionId:" + _tx.getTransactionId());
        if (null == _curtxid || !_curtxid.equals(_tx.getTransactionId())) {

            System.out.println("1 ****** bolt2 finishBatch _curtxid:" + _curtxid
                    + ",_tx.getTransactionId():" + _tx.getTransactionId());

            if (null == _curtxid) {
                _result = sum;
            } else {
                _result += sum;
            }
            _curtxid = _tx.getTransactionId();
            System.out.println(ansi().eraseScreen().fg(Ansi.Color.GREEN).a("2 ****** bolt2 finishBatch  _curtxid:" + _curtxid
                    + ",_tx.getTransactionId():" + _tx.getTransactionId()));
        }

        System.err.println("total==========================:" + _result);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
