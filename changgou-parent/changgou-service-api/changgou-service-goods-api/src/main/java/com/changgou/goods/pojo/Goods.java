package com.changgou.goods.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * TODO
 *商品信息集合类：
 * @author L5781
 * @version 1.0
 * @date 2020/8/7 12:55
 * 1、spu：属性值、特性相同的货品就可以成为一个spu
 * 2、sku：即库存进出计量的单位，是物理上不可分割的最小存货单位
 */
@Data
public class Goods implements Serializable {

    //Spu信息
    private Spu spu;

    //sku集合信息
    private List<Sku> skuList;
}
