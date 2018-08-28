package hbase.utils;

import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
import org.apache.hadoop.hbase.regionserver.BloomType;

/**
 * HBase表列簇属性
 * Created by hadoop on 10/19/16.
 */
public class ColumnFamilyProperties {
    /**
     * 列簇名称
     */
    String NAME;

    /**
     * 数据块编码方式
     */
    DataBlockEncoding DATA_BLOCK_ENCODING = DataBlockEncoding.NONE;

    /**
     * 布隆过滤器
     */
    BloomType BLOOMFILTER = BloomType.NONE;

    /**
     *
     */
    int REPLICATION_SCOPE = 0;

    /**
     * 最大保留版本数
     */
    int VERSIONS = 1;

    /**
     * 数据压缩方式
     */
    Compression.Algorithm COMPRESSION = Compression.Algorithm.NONE;

    /**
     * 最小版本数
     */
    int MIN_VERSIONS = 0;

    /**
     * 版本存活时间
     */
    int TTL = Integer.MAX_VALUE;

    /**
     * 是否保留删除的单元格
     */
    boolean KEEP_DELETED_CELLS = false;

    /**
     * HFile中数据块大小
     */
    int BLOCKSIZE = 65536;

    /**
     * 是否将列簇下的数据存在内存中
     */
    boolean IN_MEMORY = false;

    /**
     * 是否启用块缓存
     */
    boolean BLOCKCACHE = true;
}
