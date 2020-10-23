package hive;

import org.apache.hadoop.hive.ql.parse.ParseUtils;

public class TestSql {
    public static void main(String[] args)throws Exception {
        String sql = "insert overwrite table avt_dev.avtdev_zyh1022_baitiao_py2_a_d PARTITION (dt = '{TX_DATE}') select id from avt_dev.avtdev_zyh1022_baitiao_pyinsert_i_d";
        ParseUtils.parse(sql);
    }
}
