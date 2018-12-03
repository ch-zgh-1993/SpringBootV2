package com.example.demo.controller.Nav;/*
 * Created by zgh on 2018-12-03.
 * @author zgh
 * @date 2018-12-03
 * @Description: 
 * @Version: V1.0
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wild")
public class TestWildController {

//    匹配 /a /b /c 不能匹配 /a/b
//    @RequestMapping(value = "/*")
//    public String test1(){
//        return "javaPowerTest/navWildcard";
//    }

//    匹配 /a /b ， /a/b/c 等嵌套路径。
    @RequestMapping(value = "/**")
    public String test2(){
        return "javaPowerTest/navWildcard";
    }

}
