package com.changgou.search.dao;

import com.changgou.search.api.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO
 *
 * @author L5781
 * @version 1.0
 * @date 2020/8/16 10:10
 */
@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {

}
