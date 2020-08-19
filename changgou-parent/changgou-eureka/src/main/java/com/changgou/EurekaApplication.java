package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * TODO
 *
 * @author L5781
 * @version 1.0
 * @date 2020/8/1 19:09
 */
@SpringBootApplication
@EnableEurekaServer//开启Eureka服务
public class EurekaApplication {
    //注册服务启动类，相关配置以当前SpringBoot配置为标准
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class,args);
    }
}
