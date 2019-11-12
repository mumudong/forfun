package test;

import java.util.HashMap;

public class Test {
    public static void main(String[] args) {
        HashMap<String,String> map = new HashMap<>();
        map.put("a","1");
        map.put("b","2");
//        map.compute("a",(k,v) -> "11");
        map.computeIfAbsent("c",k -> null);
        System.out.println(map);
    }
}
