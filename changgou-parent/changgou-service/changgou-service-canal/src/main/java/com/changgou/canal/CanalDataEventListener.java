package com.changgou.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.xpand.starter.canal.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * TODO
 *canal微服务监听类
 * @author L5781
 * @version 1.0
 * @date 2020/8/10 15:56
 */
@CanalEventListener
public class CanalDataEventListener {

    private final ContentFeign contentFeign;
    private final StringRedisTemplate stringRedisTemplate;

    public CanalDataEventListener(ContentFeign contentFeign, StringRedisTemplate stringRedisTemplate) {
        this.contentFeign = contentFeign;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 自定义数据监听
     * @param eventType
     * @param rowData
     */
    @ListenPoint(
            eventType = {
                    CanalEntry.EventType.DELETE,
                    CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.INSERT},
            schema = {"changgou_content"},
            table = {"tb_content","tb_content_category"},
            destination = "example"
    )
    public void onEventListener(CanalEntry.EventType eventType,CanalEntry.RowData rowData) {

        String categoryId = getColumnValue(eventType, rowData);
        List<Content> contents = contentFeign.findByCategoryId(Long.parseLong(categoryId)).getData();
        stringRedisTemplate.boundValueOps("content_"+categoryId).set(JSON.toJSONString(contents));
    }
    private String getColumnValue (CanalEntry.EventType eventType,CanalEntry.RowData rowData) {
        if (eventType == CanalEntry.EventType.UPDATE || eventType == CanalEntry.EventType.INSERT){
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if ("category_id".equalsIgnoreCase(column.getName())){
                    System.out.println("(自定义监听后列名   " + column.getName() + "变更的数据" + column.getValue());
                    return column.getValue();
                }
            }
        }
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                if ("category_id".equalsIgnoreCase(column.getName())) {
                    System.out.println("自定义前列名： " + column.getName() + "变更的数据：" + column.getValue());
                    return column.getValue();
                }
            }
        }
        return "";
    }

}
