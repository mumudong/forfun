package hbase.coprocessor.secondindex;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CoprocessorEnvironment;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;


/**
 * Created by Administrator on 2018/4/18.
 */
public class HbaseSynEsObserver extends BaseRegionObserver{
    private static final Logger LOG = Logger.getLogger(HbaseSynEsObserver.class);
    private static void config(CoprocessorEnvironment env){
        Configuration conf = env.getConfiguration();
        ESClient.clusterName = conf.get("clusterName");
        ESClient.nodeHost = conf.get("nodeHost").split(",");
        ESClient.nodePort = conf.getInt("nodePort",-1);
        ESClient.indexName = conf.get("indexName");
        ESClient.typeName = conf.get("typeName");
    }

    @Override
    public void start(CoprocessorEnvironment e) throws IOException {
        config(e);
        ESClient.initEsClient();
        LOG.error("-----observer start-----" + ESClient.getInfo());
    }

    @Override
    public void stop(CoprocessorEnvironment e) throws IOException {
        ESClient.closeEsClient();
        ESBulkOperator.shutdownExcutorservice();
    }

    @Override
    public void postPut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, Durability durability) throws IOException {
        String indexId = new String(put.getRow());
        try{
            NavigableMap<byte[],List<Cell>> familyMap = put.getFamilyCellMap();
            Map<String,Object> infoJson = new HashMap<String,Object>();
            Map<String,Object> json = new HashMap<String,Object>();
            for(Map.Entry<byte[],List<Cell>> entry:familyMap.entrySet()){
                for(Cell cell:entry.getValue()){
                    String key = Bytes.toString(CellUtil.cloneQualifier(cell));
                    String value = Bytes.toString(CellUtil.cloneValue(cell));
                    json.put(key,value);
                }
                infoJson.put("info",json);
                ESBulkOperator.addUpdateBuilderToBulk(ESClient.client.prepareUpdate(
                        ESClient.indexName,ESClient.typeName,indexId).setDocAsUpsert(true).setDoc(infoJson)
                );
            }
        }catch (Exception excep){
            LOG.error("obsever put index -->" + ESClient.indexName + " indexId -->" + indexId + " error " + excep.getMessage());
        }
    }

    @Override
    public void postDelete(ObserverContext<RegionCoprocessorEnvironment> e, Delete delete, WALEdit edit, Durability durability) throws IOException {
        String indexId = new String(delete.getRow());
        try{
            ESBulkOperator.addDeleteBuilderToBulk(
                    ESClient.client.prepareDelete(ESClient.indexName,ESClient.typeName,indexId)
            );
        }catch (Exception excep){
            LOG.error("observer delete index -->" + ESClient.indexName + " indexId --> " + indexId + " error " + excep.getMessage());
        }
    }
}
