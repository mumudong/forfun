import com.alibaba.fastjson.JSONObject;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/6/26.
 */
public class Test {
    public static void main(String[] args) {
        JSONObject obj = new JSONObject();
        obj.put("key1",0.1);
//        obj.put("key2",Integer.valueOf(""));
        System.out.println(obj.getFloatValue("key1"));
//        System.out.println(obj.getIntValue("key2"));
        boolean bl = Pattern.matches("\\d*","");
        System.out.println(bl);

    }
}
