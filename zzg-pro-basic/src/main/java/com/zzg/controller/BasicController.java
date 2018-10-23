package com.zzg.controller;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.zzg.service.BasicService;

/**
 * 启动spring IOC容器
 */
@Configuration
@ComponentScan("com.zzg")
public class BasicController {

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext context = new AnnotationConfigApplicationContext(BasicController.class);
		BasicService basicService = context.getBean(BasicService.class);
		basicService.testSaveUser();
	}

}
