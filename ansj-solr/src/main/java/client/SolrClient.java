package client;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;

/**
 * Created by Administrator on 2018/8/17.
 */
public class SolrClient {
    private final static String SOLR_URL = "http://hadoop-7:8080/solr/";
    private final static String SOLR_EXP = "id:%s AND custom_ik:中国";
    public static HttpSolrClient getSolrClient(){
        HttpSolrClient client = null;
        client = new HttpSolrClient.Builder(SOLR_URL)
                                    .withConnectionTimeout(10000)
                                    .withSocketTimeout(60000)
                                    .build();
        return client;
    }

    public static String query(HttpSolrClient client,boolean needClose,String id){
//        HttpSolrClient client = getSolrClient();
        SolrQuery query = new SolrQuery();
        query.set("q",String.format(SOLR_EXP,id));
        //添加过滤条件
//        query.addFilterQuery("id:[10 TO 999]");
        //排序设置
        query.setSort("id",SolrQuery.ORDER.desc);
        //分页设置
        query.setStart(0);
        query.setRows(10);
        //设置高亮
        query.setHighlight(true);
        query.addHighlightField("custom_ik");
        query.setHighlightSimplePre("<font colr='red'>");
        query.setHighlightSimplePost("</font>");
        //获取结果
        QueryResponse response = null;
        try {
            response = client.query("test",query);
            SolrDocumentList docList = response.getResults();
            System.out.println("获取结果数据量: " + docList.getNumFound());
            //遍历
            for (SolrDocument doc : docList){
                System.out.println("id:" + doc.get("id") + " custom_ik: " + doc.get("custom_ik"));
                return "查到敏感词";
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(needClose)
                    client.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "无敏感词";
    }

    public static void add(String text,String info){
        SolrInputDocument inputDocument = new SolrInputDocument();
        //添加字段

        inputDocument.addField("custom_ik",text);
        inputDocument.addField("info",info);
        inputDocument.addField("input_time","2018-08-17");
        inputDocument.addField("id",12345);
        HttpSolrClient client = getSolrClient();
        try {
            UpdateResponse rsp = client.add("test",inputDocument);
            client.commit("test",false,false,true);
            String result = query(client,false,"12345");
            System.out.println("result ----> " + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteById(){
        HttpSolrClient client = getSolrClient();
        try {
            client.deleteById("test","12345");
            client.commit("test");
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("删除成功");
    }

    public static void main(String[] args) throws Exception{
//        add("这是一个发展中国家,第三遍","API测试");
        query(getSolrClient(),true,"2");
//        deleteById();
    }
}
