package trident.ibackingmapMysql;

/**
 * Created by Administrator on 2018/6/29.
 */
public class Bean {
    private String tel;
    private long sum;
    private long txid;
    private String time;
    private long presum;
    public Bean() {
    }
    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public long getSum() {
        return sum;
    }

    public void setSum(long sum) {
        this.sum = sum;
    }

    public long getTxid() {
        return txid;
    }

    public void setTxid(long txid) {
        this.txid = txid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getPresum() {
        return presum;
    }

    public void setPresum(long presum) {
        this.presum = presum;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "tel='" + tel + '\'' +
                ", sum=" + sum +
                ", txid=" + txid +
                ", time='" + time + '\'' +
                ", presum=" + presum +
                '}';
    }
}
