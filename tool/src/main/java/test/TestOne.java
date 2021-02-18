package test;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import scala.Char;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestOne {
    public static void main(String[] args) {
//        URL aaaa = TestOne.class.getClass().getClassLoader().getResource("log4j.properties");
//        System.out.println(aaaa.getPath());
//        System.out.println("abc         def".split("\t",-1).length);
//        System.out.println("\u0009");
//        System.out.println(StringUtils.join("abc,,c".split(",",3),"_"));
//        List<String> list = new ArrayList<>();
//        System.out.println(list instanceof List);

        int[] dateRanges = getDateRanges(5, 4);
        System.out.println(JSON.toJSONString(dateRanges));
        testUri();
    }

    private static void testUri(){
        try {
            URI uri = new URI("http://www.baidu.com/test?param=value");
            String fragment = uri.getFragment();
            System.out.println(fragment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[] getDateRanges(int dateDiff,int parallelNum){
        int baseRange = (dateDiff + 1) / parallelNum;
        int[] ranges = new int[parallelNum + 1];
        ranges[0] = 0;
        int left = (dateDiff + 1) % parallelNum;
        for(int i = 1;i <= parallelNum;i++){
            if(i <= left) {
                ranges[i] = baseRange + 1 + ranges[i-1];
            }else{
                ranges[i] = baseRange + ranges[i-1];
            }
        }
        return ranges;
    }
}
