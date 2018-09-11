package trident.state;

import org.apache.storm.task.IMetricsContext;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.state.BaseStateUpdater;
import org.apache.storm.trident.state.State;
import org.apache.storm.trident.state.StateFactory;
import org.apache.storm.trident.state.map.OpaqueMap;
import org.apache.storm.trident.tuple.TridentTuple;
import trident.ibackingmapMysql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/7/4.
 */
public class MysqlState implements State {
    private JDBCStateConfig config;
    private Long _txid = 0l;
    @Override
    public void beginCommit(Long txid) {
        System.err.println("begin txid ------------------> " + txid);
        this._txid = txid;
    }

    @Override
    // sql异常仍然commit 不合常理....
    public void commit(Long txid) {
        this._txid = txid;
        System.err.println("commit txid ------------------> " + txid);
    }

    public void updateBulk(List<String> keys,List<Integer> values){
        JDBCUtil jdbcUtil = new JDBCUtil(config.getDriver(),config.getUrl(),config.getUsername(),config.getPassword());
        Bean bean = null;
        int value = 0;
        int preValue = 0;
        long txid = 0l;
        for(int i = 0;i < keys.size();i++){// 一个批次里有两个a,第一次正常走，第二次会以为是opaque触发了
            bean = jdbcUtil.queryForMap("select * from stormtxx where key = ? ",keys.get((i)));
//            if(keys.get(i).equals("z"))
//                System.out.println(1/0);
            if(bean == null)
                jdbcUtil.insert("insert into stormtxx(key,value,inserttime,txid,\"preValue\") values(?,?,NOW(),?,?)",keys.get(i),values.get(0),this._txid,0);
            else{ // 根据spout类型修改，此处使用opaque spout
                System.out.println(bean + "  ,this._txid = " + this._txid);
                if(bean.getTxid() != this._txid){
                    value = values.get(i) + bean.getValue();
                    preValue = bean.getValue();
                    txid = this._txid;
                    jdbcUtil.insert("update stormtxx set value = ?,\"preValue\" = ?,inserttime = NOW(),txid = ? where key = ?",value,preValue,this._txid,keys.get(i));
                }else{ //失败重试的批次
                    System.err.println("------------------失败重试--------------------- bean --> " + bean);
                    value = values.get(i) + bean.getPreValue();
                    preValue = bean.getPreValue();
                    txid = this._txid;
                    jdbcUtil.insert("update stormtxx set value = ?,\"preValue\" = ?,inserttime = NOW() where key = ?",value,preValue,keys.get(i));
                }
            }
        }
    }
    public MysqlState(JDBCStateConfig config) {
        this.config = config;
    }

}
class MysqlStateFactory implements StateFactory{
    private JDBCStateConfig config;
    public MysqlStateFactory(JDBCStateConfig config) {
        this.config = config;
    }
    @Override
    public State makeState(Map conf, IMetricsContext metrics, int partitionIndex, int numPartitions) {
        System.out.println("---------------- make state ----------------");
        return new MysqlState(config);
    }
}
class MysqlUpdater extends BaseStateUpdater<MysqlState>{
    @Override
    public void updateState(MysqlState state, List<TridentTuple> tuples, TridentCollector collector) {
        List<String> keys = new ArrayList<String>();
        List<Integer> values = new ArrayList<Integer>();
        for (TridentTuple t:tuples){
            System.out.println("tuple ---> " + t);
            keys.add(t.getString(0));
            values.add(t.getLong(1).intValue());
        }
        state.updateBulk(keys,values);
    }
}
