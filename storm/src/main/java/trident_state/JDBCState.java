package trident_state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.IMetricsContext;
import org.apache.storm.trident.state.OpaqueValue;
import org.apache.storm.trident.state.State;
import org.apache.storm.trident.state.StateFactory;
import org.apache.storm.trident.state.StateType;
import org.apache.storm.trident.state.TransactionalValue;
import org.apache.storm.trident.state.map.CachedMap;
import org.apache.storm.trident.state.map.IBackingMap;
import org.apache.storm.trident.state.map.NonTransactionalMap;
import org.apache.storm.trident.state.map.OpaqueMap;
import org.apache.storm.trident.state.map.TransactionalMap;

@SuppressWarnings("all")
public class JDBCState<T> implements IBackingMap<T> {

    private static JDBCStateConfig config;

    JDBCState(JDBCStateConfig config){
        this.config = config;
    }

    @Override
    public List<T> multiGet(List<List<Object>> keys) {

        JDBCUtil jdbcUtil = new JDBCUtil(config.getDriver(),config.getUrl(),config.getUsername(),config.getPassword());

        List<Object> result = new ArrayList<Object>();
        Map<String, Object> map = null;
        for (List<Object> list : keys) {
            Object key = list.get(0);
            map = jdbcUtil.queryForMap("select * from storm where tel = ? order by txid asc",key);
            Bean itemBean = (Bean)map.get(key);
            System.out.println("bean --> " + itemBean);
            long txid=0L;
            long val=0L;
            if (itemBean!=null) {
                val=itemBean.getSum();
                txid=itemBean.getTxid();
            }
            System.out.println("接收 key ----> " + key+" txid-->"+txid);
            if (config.getType()==StateType.OPAQUE) {
                result.add(new OpaqueValue(txid, val));
            } else if (config.getType()==StateType.TRANSACTIONAL) {
                result.add(new TransactionalValue(txid, val));
            } else {
                result.add(val);
            }
        }
        return (List<T>) result;
    }

    @Override
    public void multiPut(List<List<Object>> keys, List<T> vals) {
        //构建新增SQL
        JDBCUtil jdbcUtil = new JDBCUtil(config.getDriver(),config.getUrl(),config.getUsername(),config.getPassword());
        for (int i = 0; i < keys.size(); i++) {
            List<Object> key = keys.get(i);
            if (config.getType()==StateType.TRANSACTIONAL) {
                TransactionalValue val = (TransactionalValue)vals.get(i);
                System.out.println("put key:value --> " + key + ":" + val.getVal() + "\ttxid-" +val.getTxid());
                jdbcUtil.insert("insert into storm (txid,sum,prevalue,time,tel) values(?,?,?,now(),?)",val.getTxid(),val.getVal(),0,key.get(0));
            }else if (config.getType()==StateType.OPAQUE) {
                    TransactionalValue val = (TransactionalValue)vals.get(i);
                System.out.println("put key:value --> " + key + ":" + val.getVal() + "\ttxid-" +val.getTxid());
                jdbcUtil.insert("insert into storm (txid,sum,prevalue,time,tel) values(?,?,?,now(),?)",val.getTxid(),val.getVal(),0,key);
                }
            }

    }

    public static Factory getFactory(JDBCStateConfig config) {
        return new Factory(config);
    }

    static class Factory implements StateFactory {
        private static JDBCStateConfig config;
        public Factory(JDBCStateConfig config) {
            this.config = config;
        }
        @Override
        public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
            final CachedMap map = new CachedMap(new JDBCState(config), config.getCacheSize());
            System.out.println(config);
            if(config.getType()==StateType.OPAQUE) {
                return OpaqueMap.build(map);
            } else if(config.getType()==StateType.TRANSACTIONAL){
                return TransactionalMap.build(map);
            }else {
                return NonTransactionalMap.build(map);
            }
        }
    }

}
