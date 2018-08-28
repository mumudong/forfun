package trident.unstatetransaction;

import com.google.common.collect.Lists;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.spout.ITridentSpout;
import org.apache.storm.trident.topology.TransactionAttempt;
import org.apache.storm.tuple.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/7/2.
 */
public class WordSpout implements ITridentSpout<MetaData>{
    private static final long serialVersionUID = -954626449213280061L;

    private String chars = "abcdefghijklmnopqrstuvwxyz";
    /**
     * 协调器
     * 负责保存重放batch元数据，当重放一个batch时，通过协调器中保存的元数据创建batch
     */
    @Override
    public ITridentSpout.BatchCoordinator<MetaData> getCoordinator(String txStateId, Map conf, TopologyContext context) {
        return new WordCoordinator();
    }

    @Override
    public ITridentSpout.Emitter<MetaData> getEmitter(String txStateId, Map conf, TopologyContext context) {
        return new WordEmitter();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    /**
     * 定义发送的所有字段
     */
    @Override
    public Fields getOutputFields() {
        return new Fields("field1","field2");
    }

    private class WordCoordinator implements ITridentSpout.BatchCoordinator<MetaData> {

        @Override
        public MetaData initializeTransaction(long txid, MetaData prevMetadata, MetaData currMetadata) {
            System.out.println("preMeta-->" + prevMetadata);
            System.out.println("curMeta-->" + currMetadata);
//            if(prevMetadata != null && currMetadata != null) {
//                currMetadata.set_index(prevMetadata.get_index() + prevMetadata.get_size());
//            }else
            if(currMetadata != null ){
//                currMetadata.set_index(currMetadata.get_index() + currMetadata.get_size());
                return currMetadata;
            }else if(currMetadata == null && prevMetadata != null){
                currMetadata = new MetaData();
                currMetadata.set_index(prevMetadata.get_index() + prevMetadata.get_size());
                currMetadata.set_size(2);
            }else{
                currMetadata = new MetaData();
                currMetadata.set_index(0);
                currMetadata.set_size(2);
            }
            return currMetadata;
        }

        @Override
        public void success(long txid) {
            System.out.println("success: " + txid);
        }

        @Override
        public boolean isReady(long txid) {
            System.out.println("begin  " + txid);
            return Boolean.TRUE;
        }

        @Override
        public void close() {

        }

    }

    /**
     * 发射器
     * 发送数据流
     *
     */
    private class WordEmitter implements ITridentSpout.Emitter<MetaData> {

        @Override
        public void success(TransactionAttempt tx) {
            System.out.println("emitter success " + tx.getId());
        }

        @Override
        public void close() {
        }

        /**
         * 每次调用本方法所发送的数据集合被称为batch
         * batch是Trident中发送数据流的最小单元
         */
        @Override
        public void emitBatch(TransactionAttempt tx, MetaData coordinatorMeta, TridentCollector collector) {

            System.out.printf("TransactionId : %d ,AttemptId : %d ,currMetadata : %s \n",tx.getTransactionId(),tx.getAttemptId(),coordinatorMeta);

            for(int i=coordinatorMeta.get_index();i < coordinatorMeta.get_size() + coordinatorMeta.get_index();i++){
                System.out.println("i--->" + i);
                List list = Lists.newArrayList();
                list.add("" + chars.charAt(i));
                list.add("event2");
                collector.emit(list);
            }
            try {
                Thread.sleep(2000l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Logger logger = LoggerFactory.getLogger("Trident Spout");
}
