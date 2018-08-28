package hbase.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * HBase
 * Created by hadoop on 10/19/16.
 */
public class HBaseUtils {
    //HBase Configuration
    private static final Configuration configuration = HBaseConfiguration.create();

    public static void initNamespace(String namespace) {
        HBaseAdmin admin = null;

        try {
            admin = new HBaseAdmin(configuration);

            NamespaceDescriptor descriptor =
                    NamespaceDescriptor.create(namespace)
                            .addConfiguration("creator", "elbert.malone")
                            .addConfiguration("createTime", System.currentTimeMillis() + "")
                            .build();

            admin.createNamespace(descriptor);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void deleteNamespace(String namespace) {
        HBaseAdmin admin = null;

        try {
            admin = new HBaseAdmin(configuration);

            NamespaceDescriptor[] namespaceDescriptors =
                    admin.listNamespaceDescriptors();

            boolean isExists = false;

            for (NamespaceDescriptor nd : namespaceDescriptors) {
                if (nd.getName().equals(namespace)) {
                    isExists = true;
                    break;
                }
            }

            if (isExists) {
                admin.deleteNamespace(namespace);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void initTable(String namespace, String tableName, String[] splitKeys, ColumnFamilyProperties[] cfps) {

        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(configuration);
            if (namespace == null && namespace.trim().equals("")) {
                namespace = "default";
            }

            TableName _tableName = TableName.valueOf(namespace + ":" + tableName);
            HTableDescriptor hTableDescriptor = new HTableDescriptor(_tableName);

            if (cfps == null || cfps.length <= 0) {
                throw new IllegalArgumentException("You should need at least one column family!");
            }

            for (ColumnFamilyProperties cfp : cfps) {
                String cfName = cfp.NAME;
                HColumnDescriptor _family = new HColumnDescriptor(cfName);
                _family.setDataBlockEncoding(cfp.DATA_BLOCK_ENCODING);
                _family.setBlocksize(cfp.BLOCKSIZE);
                _family.setBloomFilterType(cfp.BLOOMFILTER);
                _family.setInMemory(cfp.IN_MEMORY);
                _family.setKeepDeletedCells(cfp.KEEP_DELETED_CELLS);
                _family.setCompressionType(cfp.COMPRESSION);
                _family.setMaxVersions(cfp.VERSIONS);
                _family.setMinVersions(cfp.MIN_VERSIONS);
                _family.setTimeToLive(cfp.TTL);
                _family.setBlockCacheEnabled(cfp.BLOCKCACHE);
                _family.setScope(cfp.REPLICATION_SCOPE);
                hTableDescriptor.addFamily(_family);
            }
            if (splitKeys != null && splitKeys.length > 0) {
                byte[][] _splitKeys = new byte[splitKeys.length][];
                for (int i = 0; i < splitKeys.length; i++) {
                    _splitKeys[i] = Bytes.toBytes(splitKeys[i]);
                }
                admin.createTable(hTableDescriptor, _splitKeys);
            } else {

                admin.createTable(hTableDescriptor);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void deleteTable(String tableName) {
        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(configuration);

            TableName[] tableNames = admin.listTableNames();
            boolean isExists = false;
            for (TableName _tableName : tableNames) {
                if (tableName.equals(_tableName.getNameAsString())) {
                    isExists = true;
                    break;
                }
            }
            if (isExists) {
                admin.disableTable(tableName);
                admin.deleteTable(tableName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (admin != null) {
                try {
                    admin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
