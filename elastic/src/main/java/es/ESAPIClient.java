package es;

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
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Administrator on 2018/4/18.
 */
public class ESAPIClient {

    // ES client
    private static TransportClient  client;

    /**
     * init ES client
     */
    public static void initEsClient(){

        Settings settings = Settings.builder()
                                    .put("client.transport.sniff", ConfReader.getConfig("client.transport.sniff"))
//                                    .put("xpack.security.user", "elasticsearch:elasticsearch")
                                    .put("cluster.name", ConfReader.getConfig("cluster.name"))
                                    .put("transport.type",ConfReader.getConfig("transport.type"))
                                    .put("http.type", ConfReader.getConfig("http.type"))
                                    .build();
        try {
            client = new PreBuiltTransportClient(settings);
            for(String node:ConfReader.getConfig("nodeHost").split(",")){
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
    public static void main(String[] args) {
        initEsClient();
//        get();
//        testPrepareSearch();
        testScroll();
    }
}
