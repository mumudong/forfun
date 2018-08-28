package hbase.coprocessor.secondindex;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/18.
 */
public class ESClient {
    // ES集群名称
    public static String clusterName;
    // ES节点
    public static String[] nodeHost;
    // ES端口   java API使用Transport端口，即TCP
    public static int nodePort;
    // ES索引名称
    public static String indexName ;
    // ES type名称
    public static String typeName;
    // ES client
    public static TransportClient  client;

    /**
     * init ES client
     */
    public static void initEsClient(){
        Settings settings = Settings.builder()
                                    .put("client.transport.sniff", true)
//                                    .put("xpack.security.user", "elasticsearch:elasticsearch")
                                    .put("cluster.name", clusterName)
                                    .put("transport.type","netty3")
                                    .put("http.type", "netty3")
                                    .build();
        try {
            client = new PreBuiltTransportClient(settings);
            for(String node:nodeHost){
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node), nodePort));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    /**
     * close ES client
     */
    public static void closeEsClient(){
        client.close();
    }

    public static String getInfo() {
        List<String> fields = new ArrayList<String>();
        try {
            for (Field f : ESClient.class.getDeclaredFields()) {
                fields.add(f.getName() + "=" + f.get(null));
                System.out.println(f.getName() + "-->" + f.get(null));
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return StringUtils.join(fields, ", ");
    }
    /**
     *  ES get
     */
    public static void get(){
        GetResponse response = client.prepareGet("tx","baidu_news","4346").execute().actionGet();
        System.out.println(response.getSourceAsString());
    }
    public static void hbaseGet(String word){
        SearchResponse response = client.prepareSearch("tx")
                                        .setTypes("hbase")
                                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                                        .setPostFilter(QueryBuilders.termQuery("title","测试"))
                                        .execute().actionGet();
        SearchHits hits = response.getHits();
        List<Get> gets = new ArrayList<Get>();
        for(SearchHit hit:hits){
            String hbaseKey = (String)hit.getSource().get("ROWKEY");
            Get get = new Get(Bytes.toBytes(hbaseKey));
            gets.add(get);
        }
        if(gets.size() == 0)
            return;
        HTable table = null;
        try {
            table = new HTable(HBaseConfiguration.create(),"es");
            Result[] results = table.get(gets);
            for (Result result:results){
                System.out.println("result---->" + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(table != null){
                try {
                    table.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            client.close();
        }

    }
    public static void main(String[] args) {
//        initEsClient();
//        get();
//        getInfo();

    }
}
