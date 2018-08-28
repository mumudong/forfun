package hbase.coprocessor.secondindex;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.ClearScrollRequestBuilder;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/18.
 */
@SuppressWarnings("all")
public class ESAPIClient {

    // ES client
    private static TransportClient  client;
    private static HConnection connection ;
    static {

        try {
            connection = HConnectionManager.createConnection(HBaseConfiguration.create());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * init ES client
     */
    public static void initEsClient(){

        Settings settings = Settings.builder()
                                    .put("client.transport.sniff", ConfReader.getConfig("client.transport.sniff"))
//                                    .put("xpack.security.user", "elasticsearch:elasticsearch")
                                    .put("cluster.name", ConfReader.getConfig("cluster.name"))
                                    .put("transport.type", ConfReader.getConfig("transport.type"))
                                    .put("http.type", ConfReader.getConfig("http.type"))
                                    .build();
        try {
            client = new PreBuiltTransportClient(settings);
            for(String node: ConfReader.getConfig("nodeHost").split(",")){
                client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node), Integer.valueOf(ConfReader.getConfig("nodePort"))));
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

    public static void testPrepareSearch(){
        SearchResponse response = client.prepareSearch("tx")//可以是多个index
                                        .setTypes("baidu_news")//可以多个type
                                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                                        .setQuery(QueryBuilders.rangeQuery("id").from(4400).to(4500))
                                        .setFrom(0).setSize(20).setExplain(true).get();
        System.out.println(response);

    }

    public static void testScroll(){
        SearchResponse response = client.prepareSearch(ConfReader.getConfig("indexName"))
                                        .setTypes(ConfReader.getConfig("typeName"))
//                                        .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                                        .setScroll(new TimeValue(60000))
                                        .setQuery(QueryBuilders.matchQuery("website","百度"))
                                        .setSize(4).get();
        String scrollId = response.getScrollId();
        System.out.println("count---->" + response.getHits().getHits().length);
        //scroll第一次查询只返回id和数据总量，第二次查询才有数据
        int i = 0;
        if(response.getHits().getHits().length != 0){
            while(true){
                SearchResponse resp = client.prepareSearchScroll(scrollId)
                                            .setScroll(TimeValue.timeValueMinutes(8))
                                            .execute().actionGet();
                System.out.print("第" + i++ + "次游标----->\n");
                scrollId = resp.getScrollId();
                for (SearchHit searchHitFields : resp.getHits().getHits()) {
                    System.out.println(searchHitFields.getSourceAsString());
                }
                int num = resp.getHits().getHits().length;
                if(num == 0)
                    break;
            }
        }

        ClearScrollRequestBuilder clearScrollRequestBuilder = client.prepareClearScroll();
        clearScrollRequestBuilder.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = clearScrollRequestBuilder.get();
        if(clearScrollResponse.isSucceeded())
            System.out.println("成功清除!");
    }
    /**
     *  ES get
     */
    public static void get(){
        GetResponse response = client.prepareGet("tx","baidu_news","4346").execute().actionGet();
        System.out.println(response.getSourceAsString());
    }
    public static void hbaseGet(String word){
        System.out.println("开始查询---->" + System.currentTimeMillis());
        SearchResponse response = client.prepareSearch("tx")
                .setTypes("hbase")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setPostFilter(QueryBuilders.matchQuery("info.title",word))
                .execute().actionGet();
        SearchHits hits = response.getHits();
        List<Get> gets = new ArrayList<Get>();
        for(SearchHit hit:hits){
            String hbaseKey = hit.getId();
            Get get = new Get(Bytes.toBytes(hbaseKey));
            gets.add(get);
        }
        if(gets.size() == 0)
            return;
        HTableInterface table = null;
        try {
            table = connection.getTable("es");
            Result[] results = connection.getTable("es").get(gets);
            for (Result result:results){
                System.out.println("result---->" + Bytes.toString(result.getRow())
                        + Bytes.toString(result.getValue(Bytes.toBytes("col"),Bytes.toBytes("title"))));
            }

            System.out.println("结束查询---->" + System.currentTimeMillis());
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
        initEsClient();
//        get();
//        testPrepareSearch();
//        testScroll();
        hbaseGet("测试");
    }

}
