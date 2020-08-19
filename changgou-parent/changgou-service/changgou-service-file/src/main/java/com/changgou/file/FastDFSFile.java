package com.changgou.file;

import lombok.Data;

import java.io.Serializable;

/**
 * TODO
 *文件信息封装
 * @author L5781
 * @version 1.0
 * @date 2020/8/3 18:56
 * 封装文件上传信息
 * 时间：
 * 上传人员：
 * type类型：
 * size大小：
 * 附加信息
 * 后缀：
 * 文件内容（文件的字节数组）
 */
@Data
public class FastDFSFile implements Serializable {

    //文件名字
    private String name;
    //文件内容
    private byte[] content;
    //文件扩展名 例：jpg、png、gif等......
    private String ext;//注：文件拓展名前面的”.“不属于拓展名范畴，只用于区别文件名与后缀的占位符
    //文件MD5摘要值
    private String md5;
    //文件创建作者
    private String author;

    public FastDFSFile(String name, byte[] content, String ext) {
        this.name = name;
        this.content = content;
        this.ext = ext;
    }

    public FastDFSFile(String name, byte[] content, String ext, String md5, String author) {
        this.name = name;
        this.content = content;
        this.ext = ext;
        this.md5 = md5;
        this.author = author;
    }
}
