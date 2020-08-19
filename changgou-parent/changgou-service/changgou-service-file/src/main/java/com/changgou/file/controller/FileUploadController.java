package com.changgou.file.controller;

import com.changgou.common.entity.Result;
import com.changgou.common.entity.StatusCode;
import com.changgou.file.FastDFSFile;
import com.changgou.file.util.FastDFSUtil;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * TODO
 *fastDFS文件控制层
 * @author L5781
 * @version 1.0
 * @date 2020/8/3 19:46
 */
@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {

    /**
     * 文件上传
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping
    public Result upload(@RequestParam(value = "file")MultipartFile file) throws Exception {
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(),
                file.getBytes(),
                StringUtils.getFilenameExtension(file.getOriginalFilename())
        );
        String[] uploadFile = FastDFSUtil.upload(fastDFSFile);
        //拼接访问地址 url = http://192.168.211.132:8080/group1/M00/00/00/wKjThF0DBzaAP23MAAXz2mMp9oM26.jpeg
        //String url  = "http://192.168.1.4:8080/"+ uploadFile[0] + "/" +  uploadFile[1];
        String url  = FastDFSUtil.getTrackerInfo()+"/" + uploadFile[0] + "/" +  uploadFile[1];

        return new Result(true, StatusCode.OK,"文件上传成功",url);
    }

}
