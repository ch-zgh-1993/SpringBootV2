package com.example.demo.controller;/*
 * Created by zgh on 2018-08-03.
 * @author zgh
 * @date 2018-08-03
 * @Description: 
 * @Version: V1.0
 */

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class helloController {

    @RequestMapping("/test")
    public String test(){

        return "hello";
    }
}
