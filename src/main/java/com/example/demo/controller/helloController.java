/*
* @Author: Zhang Guohua
* @Date:   2018-11-21 13:12:27
* @Last Modified by:   zgh
* @Last Modified time: 2018-11-21 13:12:55
* @Description: create by zgh
* @GitHub: Savour Humor
*/
package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class helloController {

    @RequestMapping("/test")
    public String test(){

        return "hello";
    }
}
