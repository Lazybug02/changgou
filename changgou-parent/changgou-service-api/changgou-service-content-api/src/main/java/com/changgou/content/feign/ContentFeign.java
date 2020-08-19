package com.changgou.content.feign;

import com.changgou.common.entity.Result;
import com.changgou.content.pojo.Content;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * TODO
 *
 * @author L5781
 * @version 1.0
 * @date 2020/8/10 19:27
 */
@FeignClient(name="content")	//指定微服务的名字
@RequestMapping(value = "/content")
public interface ContentFeign {

    /**
     * 根据分类ID查询所有广告
     * @param id
     * @return
     */
    @GetMapping(value = "/list/category/{id}")
    Result<List<Content>> findByCategoryId(@PathVariable long id);
}
