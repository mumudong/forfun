package trident.state;

import java.util.Date;

/**
 * Created by Administrator on 2018/6/29.
 */
public class Bean {
    private String key;
    private int value;
    private long txid;
    private Date inserttime;
    private int preValue;

    public Bean() {
    }

    public Bean(String key, int value, long txid, Date inserttime, int preValue) {
        this.key = key;
        this.value = value;
        this.txid = txid;
        this.inserttime = inserttime;
        this.preValue = preValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public long getTxid() {
        return txid;
    }

    public void setTxid(long txid) {
        this.txid = txid;
    }

    public Date getInserttime() {
        return inserttime;
    }

    public void setInserttime(Date inserttime) {
        this.inserttime = inserttime;
    }

    public int getPreValue() {
        return preValue;
    }

    public void setPreValue(int preValue) {
        this.preValue = preValue;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "key='" + key + '\'' +
                ", value=" + value +
                ", txid=" + txid +
                ", inserttime=" + inserttime +
                ", preValue=" + preValue +
                '}';
    }
}
