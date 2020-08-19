package com.changgou.search.service;

import java.util.Map;

/**
 * TODO
 *索引库的SKU业务层接口
 * @author L5781
 * @version 1.0
 * @date 2020/8/16 10:12
 */
public interface SkuService {

    /**
     * 条件搜索
     * @param searchMap
     * @return
     */
    Map<String,Object> search(Map<String,String> searchMap);

    /**
     * 导入Sku数据到ES索引库接口
     */
    void importData();
}
