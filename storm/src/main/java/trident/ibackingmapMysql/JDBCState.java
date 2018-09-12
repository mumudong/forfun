package trident.ibackingmapMysql;

import org.apache.storm.task.IMetricsContext;
import org.apache.storm.topology.FailedException;
import org.apache.storm.trident.state.*;
import org.apache.storm.trident.state.map.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于IbackingMap的自定义state
 */
public class JDBCState<T> implements IBackingMap<T> {
    private JDBCStateConfig config;
    private Logger logger = LoggerFactory.getLogger(getClass());
    public JDBCState(JDBCStateConfig config) {
        this.config = config;
    }

    /**
     * 获取本批次数据
     * @param keys
     * @return
     */
    @Override
    public List<T> multiGet(List<List<Object>> keys) {
        JDBCUtil jdbcUtil = new JDBCUtil(config.getDriver(),config.getUrl(),config.getUsername(),config.getPassword());
        List<Object> result = new ArrayList<Object>(); // 存储结果值
        for(List<Object> list:keys){
            Object key = list.get(0);// 电话 次数中的电话
            System.out.println("上游获取key ----> " + key);
            Map<String,Object> mapBean= jdbcUtil.queryForMap("select * from stormtx where tel = ?",key);
            Bean itemBean = (Bean)mapBean.get(key);
            logger.error("itemBean --> " + itemBean);
            long txid = itemBean!=null ? itemBean.getTxid() : 0l;
            long val = itemBean!=null ? itemBean.getSum() : 0l;
            long prev = itemBean!=null ? itemBean.getPresum() : 0l;
            if(config.getType() == StateType.OPAQUE){// 模糊
                logger.error("get--> txid:" + txid + " ,val:" + val + " ,preval:" + prev);
                result.add(new OpaqueValue(txid,val,prev));
            }else if(config.getType() == StateType.TRANSACTIONAL){
                result.add(new TransactionalValue(txid,val));
            }else{ // 一般storm
                result.add(val);
            }
        }
        return (List<T>)result;
    }

    /**
     * 入库，vals是累计结果，由ibackingmap保存结果
     * @param keys
     * @param vals
     */
    @Override
    public void multiPut(List<List<Object>> keys, List<T> vals)  {
        JDBCUtil jdbcUtil = new JDBCUtil(config.getDriver(),config.getUrl(),config.getUsername(),config.getPassword());
        for(int i = 0;i < keys.size(); i++){
            List<Object> key = keys.get(i);
            /*if(key.get(0).equals("b")){
                throw new FailedException();//不停的发送
            }*/
            /*if(key.get(0).equals("f")){
                System.out.println(1/0);
            }*/
            Map<String,Object> map = null;
            map = jdbcUtil.queryForMap("select * from stormtx where tel = ?",key.get(0));
            Bean itemBean = (Bean)map.get(key.get(0));
            if(config.getType() == StateType.TRANSACTIONAL){
                TransactionalValue val = (TransactionalValue)vals.get(i);
                long sum = (Long)val.getVal();
                if(itemBean.getTxid() != val.getTxid()){
                    jdbcUtil.insert("insert into stormtx(tel,sum,txid,time) values (?,?,?,NOW())",key.get(0),val.getVal(),val.getTxid());
                }
            }else if(config.getType() == StateType.OPAQUE){
                OpaqueValue<Long> val = (OpaqueValue)vals.get(i);
                long txid = jdbcUtil.queryTxid("select txid from stormtx where tel = ?",key.get(0));
                if(val.getCurrTxid() == txid){
                    logger.error("put的时候txid相同,旧txid -- "  + txid + " ,val:" + val + " ,key:" + key);
                    jdbcUtil.insert("update stormtx set sum = ?,presum = ?,time = NOW() where tel = ? " , val.getCurr(),val.getPrev(), key.get(0));
                }else {
                    logger.error("put的时候txid不相同,旧txid -- 》"  + txid + ":" + val + ":" + key.get(0));
                    jdbcUtil.insert("insert into stormtx(tel,presum,sum,txid,time) values (?,?,?,?,NOW()) " +
                                    " on conflict(tel) do update set presum  = ?,sum = ?,txid = ?,time = now()", key.get(0), val.getPrev(), val.getCurr(), val.getCurrTxid(),
                            val.getPrev() , val.getCurr(), val.getCurrTxid());
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
