/*
* @Author: Zhang Guohua
* @Date:   2018-11-21 13:12:27
* @Last Modified by:   zgh
* @Last Modified time: 2018-11-21 13:18:55
* @Description: create by zgh
* @GitHub: Savour Humor
*/
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
// 打 war 包需要继承 SpringBootServletInitializer
// 并在 pom.xml 中放开 <scope>provided</scope>
public class DdApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DdApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(DdApplication.class, args);
	}
}
