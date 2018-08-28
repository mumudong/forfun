package trident.ibackingmapMysql;

import org.apache.storm.task.IMetricsContext;
import org.apache.storm.trident.state.*;
import org.apache.storm.trident.state.map.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于IbackingMap的自定义state
 */
public class JDBCState<T> implements IBackingMap<T> {
    private JDBCStateConfig config;

    public JDBCState(JDBCStateConfig config) {
        this.config = config;
    }

    @Override
    public List<T> multiGet(List<List<Object>> keys) {
        JDBCUtil jdbcUtil = new JDBCUtil(config.getDriver(),config.getUrl(),config.getUsername(),config.getPassword());
        List<Object> result = new ArrayList<Object>(); // 存储结果值
        for(List<Object> list:keys){
            Object key = list.get(0);// 电话 次数中的电话
            System.out.println("上游获取key ----> " + key);
            Map<String,Object> mapBean= jdbcUtil.queryForMap("select * from stormtx where tel = ?",key);
            Bean itemBean = (Bean)mapBean.get(key);
            System.out.println("itemBean --> " + itemBean);
            long txid = 0L;
            long val = 0L;
            if (itemBean!=null) {
                val=itemBean.getSum();
                txid=itemBean.getTxid();
            }
            if(config.getType() == StateType.OPAQUE){// 模糊
                result.add(new OpaqueValue(txid,val));
            }else if(config.getType() == StateType.TRANSACTIONAL){
                System.out.println("================================");
                result.add(new TransactionalValue(txid,val));
            }else{ // 一般storm
                result.add(val);
            }
        }
        return (List<T>)result;
    }

    @Override
    public void multiPut(List<List<Object>> keys, List<T> vals)  {
        JDBCUtil jdbcUtil = new JDBCUtil(config.getDriver(),config.getUrl(),config.getUsername(),config.getPassword());
        for(int i = 0;i < keys.size(); i++){
            List<Object> key = keys.get(i);
            System.out.println("输入state key ----> " + key);
            if(config.getType() == StateType.TRANSACTIONAL){
                Map<String,Object> map = null;
                TransactionalValue val = (TransactionalValue)vals.get(i);
                map = jdbcUtil.queryForMap("select * from stormTx where tel = ?",key);
                Bean itemBean = (Bean)map.get(key);
                long sum = (Long)val.getVal();
                if(itemBean.getTxid() != val.getTxid()){
                    jdbcUtil.insert("insert into stormtx(tel,sum,txid,time) values (?,?,?,NOW())",key.get(0),val.getVal(),val.getTxid());
                }
            }else if(config.getType() == StateType.OPAQUE){
                OpaqueValue val = (OpaqueValue)vals.get(i);
                long txid = jdbcUtil.queryTxid("select txid from stormtx where tel = ?",key.get(0));
                System.out.println("收取 opaque txid ----> " + val.getCurrTxid() + " 收取 sum --> " + val.getCurr());
                if(val.getCurrTxid() == txid){
                    System.out.println("put的时候txid相同,旧txid -- 》"  + txid);
                    jdbcUtil.insert("update stormtx sum =?,time = NOW() where key = ? " , (Long)val.getPrev() + (Long)val.getCurr(), key.get(0));
                }else {
                    System.out.println("put的时候txid不相同,旧txid -- 》"  + txid);
//                    System.out.println(1/0);
                    jdbcUtil.insert("insert into stormtx(tel,presum,sum,txid,time) values (?,?,?,?,NOW()) " +
                                    " on conflict(tel) do update set presum  = ?,sum = ?,txid = ?,time = now()", key.get(0), val.getPrev(), val.getCurr(), val.getCurrTxid(),
                            val.getPrev(), val.getCurr(), val.getCurrTxid());
                }
            }
        }
    }
    public static Factory getFactory(JDBCStateConfig config){
        return new Factory(config);
    }
    static class Factory implements StateFactory {
        private static JDBCStateConfig config;
        public Factory(JDBCStateConfig config){
            this.config = config;
        }
        @Override
        public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
//            final CachedMap stat = new CachedMap(new JDBCState(config),config.getCacheSize());
            JDBCState stat = new JDBCState(config);
            System.out.println("-------make state---------");
            if(config.getType() == StateType.OPAQUE)
                return OpaqueMap.build(stat);
            else if(config.getType() == StateType.TRANSACTIONAL)
                return TransactionalMap.build(stat);
            else
                return NonTransactionalMap.build(stat);
        }
    }
}
