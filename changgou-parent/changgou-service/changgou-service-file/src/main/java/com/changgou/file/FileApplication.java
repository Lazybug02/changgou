package com.changgou.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * TODO
 *启动类FileApplication
 * @author L5781
 * @version 1.0
 * @date 2020/8/3 18:47
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})//排除数据库自动加载
@EnableEurekaClient
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class,args);
    }
}
