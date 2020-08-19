package com.changgou.goods;

import com.changgou.common.controller.BaseExceptionHandler;
import com.changgou.common.entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * TODO
 *Goods SpringBoot服务启动类
 * @author L5781
 * @version 1.0
 * @date 2020/8/1 19:46
 */
@SpringBootApplication
@EnableEurekaClient//开启Eureke客户端
@MapperScan(basePackages = {"com.changgou.goods.dao"})//开启包扫描
public class GoodsApplication {

    @Bean
    public BaseExceptionHandler baseExceptionHandler(){
        return new BaseExceptionHandler();
    }

    /***
     * IdWorker
     * @return
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,0);
    }

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class,args);
    }
}
