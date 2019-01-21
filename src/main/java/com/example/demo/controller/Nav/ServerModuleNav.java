package com.example.demo.controller.Nav;/*
 * Created by zgh on 2019-01-21.
 * @author zgh
 * @date 2019-01-21
 * @Description: 
 * @Version: V1.0
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/server")
public class ServerModuleNav {

    @RequestMapping(value = "/websokcet")
    public String websocket() {
        return "/javaPowerTest/testWebsocket";
    }

}
