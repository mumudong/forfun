package cc.mrbird.sso.client.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author MrBird
 */
@RestController("test")
public class InfoController {

    @RequestMapping("testinfo")
    public Map<String,String> testinfo() {
        Map map = new HashMap();
        map.put("key","info");
        return map;
    }
}
