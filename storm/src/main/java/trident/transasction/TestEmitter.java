package trident.transasction;

import org.apache.storm.coordination.BatchOutputCollector;
import org.apache.storm.transactional.ITransactionalSpout;
import org.apache.storm.transactional.TransactionAttempt;
import org.apache.storm.tuple.Values;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2018/5/30.
 */
public class TestEmitter implements ITransactionalSpout.Emitter<TestMetaData> {

    private Map<Long, String> _dbMap = null;

    public TestEmitter(Map<Long, String> dbMap) {
        System.err.println("start TestEmitter");
        this._dbMap = dbMap;
    }

    @Override
    public void emitBatch(TransactionAttempt tx, TestMetaData coordinatorMeta,
                          BatchOutputCollector collector) {
        Random random = new Random();
        long index = coordinatorMeta.get_index();
        long size = index + coordinatorMeta.get_size();
        System.err.println("TestEmitter emitBatch size:" + size
                + ",_dbMap size:" + _dbMap.size());
        if (size > _dbMap.size()) {
            return;
        }
        for (; index < size; index++) {
            if (null == _dbMap.get(index)) {
                System.err.println("TestEmitter continue");
                continue;
            }
            try {
                Thread.sleep(Long.valueOf(random.nextInt(1000)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            collector.emit(new Values(tx, _dbMap.get(index)));
            System.err.println("TestEmitter emitBatch index:"+index);
        }
    }

    @Override
    public void cleanupBefore(BigInteger txid) {
    }

    @Override
    public void close() {
    }

}
