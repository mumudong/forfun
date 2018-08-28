package trident.transasction;

import org.apache.storm.transactional.ITransactionalSpout;
import org.apache.storm.utils.Utils;

import java.math.BigInteger;

/**
 * Created by Administrator on 2018/5/30.
 */
public class TestCoordinator implements ITransactionalSpout.Coordinator<TestMetaData>{

    public TestCoordinator(){
        System.err.println("TestCoordinator start");
    }

    @Override
    public TestMetaData initializeTransaction(BigInteger txid,
                                              TestMetaData prevMetadata) {
        long index = 0L;
        if (null == prevMetadata){
            index = 0L;
        }
        else {
            index = prevMetadata.get_index()+prevMetadata.get_size();
        }
        TestMetaData metaDate = new TestMetaData();
        metaDate.set_index(index);
        metaDate.set_size(10);
        System.err.println("开始事务："+metaDate.toString());
        return metaDate;
    }

    @Override
    public boolean isReady() {
        Utils.sleep(1000);
        return true;
    }

    @Override
    public void close() {
    }

}
