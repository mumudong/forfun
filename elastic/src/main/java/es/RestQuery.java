package es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2018/4/18.
 */
public class RestQuery {
    public static void main(String[] args) throws Exception {
        HttpURLConnection conn = null;
        DataOutputStream wr = null;
        BufferedReader br = null;
        String line = null;
        try {
            System.out.println("start---->");
            // 1. Prepare url
            String queryAddress = "http://hadoop-5:9200/tx/baidu_news/_search?pretty";

            // 2. Prepare query param
            String queryParamJson = buildQueryParamByStr();
//            String queryParamJson = buildQueryParamByAPI();
            System.out.println("queryJson---->" + queryParamJson);
            // 3. Inject url
            URL url = new URL(queryAddress);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Content-Length",
                    Integer.toString(queryParamJson.getBytes().length));
            conn.setRequestProperty("Content-Language", "en-US");
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            // 4. Inject query param
            wr = new DataOutputStream (
                    conn.getOutputStream());
            wr.writeBytes(queryParamJson);

            // Connection failure handling
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            StringBuffer sb = new StringBuffer();
            // 5. Get Response
            br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("result---->" + sb.toString());

            JSONObject jsonObject = JSON.parseObject(sb.toString());
            JSONObject hits = jsonObject.getJSONObject("hits");
            JSONArray array = hits.getJSONArray("hits");
            for(Object obj:array){
                if(obj instanceof JSONObject)
                    System.out.println(((JSONObject) obj).toJSONString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(wr != null) {
                wr.close();
            }
            if(br != null) {
                br.close();
            }
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String buildQueryParamByAPI() {
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("empname", "emp2");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String queryParamJson = searchSourceBuilder.query(queryBuilder).toString();
        System.out.println("json~~~" + queryParamJson);
        return queryParamJson;
    }
    public static String buildQueryParamByStr() {
        /*curl 'centos1:9200/dept/employee/_search?pretty' -d
         * '{"query" : {"match" : {"empname" : "emp2"}}}'*/
        String search =
                "{\n" +
                "  \"_source\": \"{id}\", \n" +
                " \"size\":1\n" +
                "}";
        return search;
    }
}
