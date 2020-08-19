package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.common.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.api.pojo.SkuInfo;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.service.SkuService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;



import java.util.*;

/**
 * TODO
 *
 * @author L5781
 * @version 1.0
 * @date 2020/8/16 10:14
 */
@Service
public class SkuEsServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /**
     * elasticsearchTemplate::相当于模板技术，封装了用于索引库的增、删、改、查操作
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 条件搜索
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //搜索条件封装
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);

        //集合搜索（调用抽取方法searchList）
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);

       /* //调用searchCategoryList方法实现
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {//该搜索查询只适用于页面分类展示，用户选择某个分类则不需要对分
            List<String> categoryList = searchCategoryList(nativeSearchQueryBuilder);
            resultMap.put("categoryList", categoryList);
        }

        //查询品牌集合（根据搜索条件）
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            List<String> brandList = searchBrandList(nativeSearchQueryBuilder);
            resultMap.put("brandList", brandList);
        }

        //规格查询
        Map<String, Set<String>> specList = searchSpecList(nativeSearchQueryBuilder);
        resultMap.put("specList",specList);
        return resultMap;*/
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);

        resultMap.putAll(groupMap);

        return resultMap;
    }

    /**
     *根据品牌、分类、规格分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String,Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder,Map<String,String> searchMap) {

        /**
         * 分组查询分类集合
         * addAggregation:添加聚合操作
         * 参数说明：
         * 1）别名
         * 2）根据域名进行分组查询
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> skuInfoAggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /**
         * 获取分组数据：
         * skuInfoAggregatedPage.getAggregations()：获取集合,可以进行多个域进行分组
         * get("skuCategory"):获取指定域的数据
         */

        //定义一个Map，存储所有分组结果
        Map<String,Object> groupMapResult = new HashMap<String, Object>();

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = skuInfoAggregatedPage.getAggregations().get("skuCategory");
            //获取分类分组集合数据
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList",categoryList);
        }

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = skuInfoAggregatedPage.getAggregations().get("skuBrand");
            //获取品牌分组集合数据
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList",brandList);
        }

        StringTerms specTerms = skuInfoAggregatedPage.getAggregations().get("skuSpec");
        //获取规格分组集合数据
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupMapResult.put("specList",specMap);
        return groupMapResult;

    }

    /**
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public List<String> getGroupList(StringTerms stringTerms){
        List<String> groupList = new ArrayList<String>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String feildName = bucket.getKeyAsString();//分组的值
            groupList.add(feildName);
        }
        return groupList;
    }

    /**
     * 搜索条件封装
     * @param searchMap
     * @return
     */
    public NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //创建构建对象nativeSearchQueryBuilder,用于封装各种搜索条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //创建分类、品牌筛选查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //关键字非空判断并获取
        if (searchMap != null && searchMap.size()>0){
            //获取关键字
            String keywords = searchMap.get("keywords");
            //当关键字不为空则执行搜索指定域-name
            if (StringUtils.isNotBlank(keywords)){
               // nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            //分类过滤
            if (!StringUtils.isEmpty(searchMap.get("category"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",searchMap.get("category")));
            }

            //品牌过滤
            if (!StringUtils.isEmpty(searchMap.get("brand"))){
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }

            //规格筛选过滤
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                //如果key以spec_开始，则表示规格筛选查询
                if (key.startsWith("spec_")){
                    //规格条件的值
                    String value = entry.getValue();
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+ key.substring(5) +".keyword",value));
                }
            }

            //获取前端页面传来的价格
            String price = searchMap.get("price");
            //价格筛选
            if (StringUtils.isNotBlank(price)){
                //去掉价格之间存在的中文“元”和“以上”
                price = price.replace("元", "").replace("以上", "");
                //价格根据“-”分割
                String[] prices = price.split("-");
                if (prices != null && prices.length>0){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    if (prices.length ==2){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }

            //排序实现
            String sortField = searchMap.get("sortField");      //指定排序的域
            String sortRule = searchMap.get("sortRule");       //指定排序的规则
            if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortRule)){
                /**
                 * FieldSortBuilder(sortField)：指定排序的域
                 * order(SortOrder.valueOf(sortRule))：指定排序的规则
                 */
                nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }
        //分页，默认页数为第一页
        Integer pageNum = coverterPage(searchMap);
        Integer size = 10;          //默认查询数据条数
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum-1,size));

        //将boolQueryBuilder对象填充到nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        return nativeSearchQueryBuilder;
    }

    /**
     * 获取前端传来的分页参数
     * @param searchMap
     * @return
     */
    public Integer coverterPage(Map<String,String> searchMap){
        if (searchMap != null){
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    /**
     * 集合搜索
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {

        //高亮配置
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");//指定商品名称为高亮域
        //1、前缀和后缀  <em style="color:red;">      </em>
        field.preTags("<em style=\"color:red;\">");
        field.postTags("</em>");
        //碎片长度(关键词数据长度)
        field.fragmentSize(100);
        //添加高亮
        nativeSearchQueryBuilder.withHighlightFields(field);

        /**
         * 执行搜索并响应结果:
         * 参数说明
         * 1)搜索条件封装
         * 2）搜索结果集（集合数据）需要转换的类型
         */
        //AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class,
                        new SearchResultMapper() {
                            @Override
                            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                                //创建存储所有转换后的高亮数据对象
                                List<T> list =  new ArrayList<T>();
                                //执行查询，并获取所有结果
                                for (SearchHit hit : response.getHits()) {
                                    //获取非高亮数据
                                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                                    //从结果集数据当中的获取高亮数据（仅仅只有某一个域的高亮）
                                    HighlightField highlightField = hit.getHighlightFields().get("name");
                                    //高亮数据非空判断
                                    if (highlightField != null && highlightField.getFragments() !=null){
                                        //读取高亮数据
                                        Text[] fragments = highlightField.getFragments();
                                        //将遍历的高亮结果赋值给buffer
                                        StringBuffer buffer = new StringBuffer();
                                        //遍历高亮数据
                                        for (Text fragment : fragments) {
                                            buffer.append(fragment.toString());
                                        }
                                        //替换非高亮中指定的域
                                        skuInfo.setName(buffer.toString());
                                    }
                                    //将高亮数据添加到集合中
                                    list.add((T) skuInfo);
                                }
                                /**
                                 * AggregatedPageImpl参数说明：
                                 * 1）搜索的集合数据
                                 * 2）分页对象
                                 * 3）搜索记录总条数
                                 */
                                return new AggregatedPageImpl<T>(list,pageable,response.getHits().getTotalHits());
                            }
                        });

        //创建封装对象用于存储所有数据结果
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("rows",page.getContent());
        resultMap.put("total",page.getTotalElements());
        resultMap.put("totalPages",page.getTotalPages());
        return resultMap;
    }

    /**
     * 规格分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {

        /**
         * 分组查询规格集合
         * addAggregation:添加聚合操作
         * 参数说明：
         * 1）别名
         * 2）根据域名进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> skuInfoAggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /**
         * 获取分组数据：
         * skuInfoAggregatedPage.getAggregations()：获取集合,可以进行多个域进行分组
         * get("skuCategory"):获取指定域的数据
         */
        StringTerms stringTerms = skuInfoAggregatedPage.getAggregations().get("skuSpec");

        List<String> specList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String specName = bucket.getKeyAsString();//获取品牌名字
            specList.add(specName);
        }

        //创建数据格式合并后的结果集
        Map<String, Set<String>> allSpec = new HashMap<String,Set<String>>();

        //循环遍历specList
        for (String spec : specList) {
            //将规格参数数据（JSON）全数转换为Map格式
            Map <String,String> specMap = JSON.parseObject(spec, Map.class);
            //遍历specMap，并获取Key、Value
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String key = entry.getKey();        //规格名称  例如：电视屏幕尺寸
                String value = entry.getValue(); //各个规格下的值  例如：60英寸
                //从allSpec中获取当前规格对应的Set集合数据
                Set<String> specSet = allSpec.get(key);
                if (specSet == null){//表示之前allSpec中没有该规格
                    specSet = new HashSet<String>();
                }
                specSet.add(value);
                allSpec.put(key,specSet);
            }
        }
        return allSpec;
    }

    /**
     * 合并规格
     * @param specList
     * @return
     */
    public Map<String,Set<String>> putAllSpec(List<String> specList){
        //1、定义合并后的Map对象：将每个Map对象合成成为一个Map<String,Set<String>>
        Map<String, Set<String>> allSpec = new HashMap<String, Set<String>>();
        //2、循环遍历specList
        for (String spec : specList) {
            //3、将每个JSON字符串转成Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);
            //4、合并流程
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                //4.1取出当前Map中的key和对应的value
                String key = entry.getKey();           //规格名字
                String value = entry.getValue();   //规格值

                //4.2将当前循环的数据合并到一个Map<String,Set<String>>去
                Set<String> specSet = allSpec.get(key);//从allSpec中获取当前规格对应的Set集合数据
                if (specSet == null){//如果之前specSet没有值则进行以下代码块操作
                    //将specSet的值填充进HashSet去重复
                    specSet = new HashSet<String>();
                }
                specSet.add(value);
                allSpec.put(key,specSet);
            }
        }
        return allSpec;
    }

    /**
     * 品牌分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /**
         * 分组查询品牌集合
         * addAggregation:添加聚合操作
         * 参数说明：
         * 1）别名
         * 2）根据域名进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> skuInfoAggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /**
         * 获取分组数据：
         * skuInfoAggregatedPage.getAggregations()：获取集合,可以进行多个域进行分组
         * get("skuCategory"):获取指定域的数据
         */
        StringTerms stringTerms = skuInfoAggregatedPage.getAggregations().get("skuBrand");

        List<String> brandList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();//获取品牌名字
            brandList.add(brandName);
        }
        return brandList;
    }

    /**
     * 分类分组查询
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {

        /**
         * 分组查询分类集合
         * addAggregation:添加聚合操作
         * 参数说明：
         * 1）别名
         * 2）根据域名进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> skuInfoAggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /**
         * 获取分组数据：
         * skuInfoAggregatedPage.getAggregations()：获取集合,可以进行多个域进行分组
         * get("skuCategory"):获取指定域的数据
         */
        StringTerms stringTerms = skuInfoAggregatedPage.getAggregations().get("skuCategory");

        List<String> categoryList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categoryName = bucket.getKeyAsString();
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /**
     * 导入Sku数据到ES索引库接口
     */
    @Override
    public void importData() {
        //1、调用Feign，获得查询结果集List<Sku>
        Result<List<Sku>> skuListResult = skuFeign.findByStatus("1");
        //将数据转成search.Sku
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuListResult.getData()), SkuInfo.class);
        //遍历当前skuInfoList
        for (SkuInfo skuInfo : skuInfoList) {
            //获取Spec并转换map格式数据
            Map<String,Object> specMap = JSON.parseObject(skuInfo.getSpec());
            //将转换数据格式成功后的specMap填充进skuInfo对象中，即生成动态的域
            skuInfo.setSpecMap(specMap);//注：域的名称就是该Map的key,而value则会成为对应的值
        }
        //调用Dao实现数据批量导入
        skuEsMapper.saveAll(skuInfoList);
    }
}