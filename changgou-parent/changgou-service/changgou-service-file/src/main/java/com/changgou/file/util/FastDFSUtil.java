package com.changgou.file.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * TODO
 *实现FastDFS信息获取以及文件的相关操作
 * @author L5781
 * @version 1.0
 * @date 2020/8/3 19:07
 * 实现FastDFS文件管理：
 *                              文件上传
 *                              文件删除
 *                              文件下载
 *                              文件信息获取
 *                              Storage、Tracker信息获取
 */
public class FastDFSUtil {

    /**
     * 加载Tracker连接信息
     */
    static {
        try {
            //查找文件为fdfs_client.conf的路径地址（）
            String filename = new ClassPathResource("fdfs_client.conf").getPath();
            //加载配置文件
            ClientGlobal.init(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * trackerServer抽取
     * @return
     * @throws Exception
     */
    public static TrackerServer getTrackerServer() throws Exception{
        //创建Tracker客户端访问对象trackerClient,并通过其对象直接访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    /**
     * 抽取StorageClient
     * @param trackerServer
     * @return
     */
    public static StorageClient getStorageClieng(TrackerServer trackerServer){
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }

    /**
     * 文件上传
     * @param fastDFSFile
     * @throws Exception
     * @return
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws Exception{

        //附加参数
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author",fastDFSFile.getAuthor());
        //调用抽取方法
        TrackerServer trackerServer = getTrackerServer();

        StorageClient storageClient = getStorageClieng(trackerServer);

        /**
         * 通过storageClient访问Storage，实现文件的上传，并获取上传文件上传 后的存储信息
         * upload_file 参数说明（从左到右方向）：
         * 1、上传文件的字节数
         * 2、文件的拓展名 例如：jpg、png、gif
         * 3、附加参数  例如：图片拍摄地址、拍摄器材
         * uploadFile 参数说明:
         * 1、uploadFile[0]:文件上传所存储的Storage的组名字 例：group1
         * 2、uploadFile[1]:文件存储到Storage的文件名字    例：MOO/02/44/黄昏.jpg
         */
        String[] uploadFile = storageClient.upload_file(fastDFSFile.getContent(), fastDFSFile.getExt(), meta_list);
        return uploadFile;
    }

    /**
     * 获取文件信息
     * @param groupName http://192.168.1.4:8080/group1/M00/00/00/wKgBBF8oxtiAR8h2AABBUppopkY173.jpg
     * @param remoteFileName
     * @return
     * @throws Exception
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception {
        //调用抽取方法
        TrackerServer trackerServer = getTrackerServer();

        StorageClient storageClient = getStorageClieng(trackerServer);
        //获取文件信息
        return storageClient.get_file_info(groupName,remoteFileName);
    }

    /**
     * 文件下载
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws Exception
     */
    public static InputStream downloadFile(String groupName, String remoteFileName) throws Exception {
        //调用抽取方法
        TrackerServer trackerServer = getTrackerServer();

        StorageClient storageClient = getStorageClieng(trackerServer);

        //文件下载
        byte[] bytes = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 删除文件
     * @param groupName
     * @param remoteFileName
     * @throws Exception
     */
    public static void deleteFile(String groupName, String remoteFileName) throws Exception {
        //调用抽取方法
        TrackerServer trackerServer = getTrackerServer();

        StorageClient storageClient = getStorageClieng(trackerServer);

        //文件删除
        storageClient.delete_file(groupName,remoteFileName);
    }

    /**
     * 获取stroage信息
     * @return
     * @throws Exception
     */
    public static StorageServer getStorage() throws Exception {
        //创建Tracker客户端访问对象trackerClient,并通过其对象直接访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取storage信息
        return trackerClient.getStoreStorage(trackerServer);
    }

    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception {
        //创建Tracker客户端访问对象trackerClient,并通过其对象直接访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取Storage端口和IP信息
      return trackerClient.getFetchStorages(trackerServer,groupName,remoteFileName);

    }

    /**
     * 获取Tracker
     * @return
     * @throws Exception
     */
    public static String getTrackerInfo() throws Exception {
        //创建Tracker客户端访问对象trackerClient,并通过其对象直接访问TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //获取TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取IP、端口
        int tracker_http_port = ClientGlobal.getG_tracker_http_port();
        String hostString = trackerServer.getInetSocketAddress().getHostString();

        String url = "http://" + hostString + ":" + tracker_http_port;
        return url;
    }

    public static void main(String[] args) throws Exception {
    /*    FileInfo fileInfo = getFile("group1", "M00/00/00/wKgBBF8oxtiAR8h2AABBUppopkY173.jpg");

        System.out.println( fileInfo.getFileSize());
        System.out.println(fileInfo.getSourceIpAddr());*/

/*        //文件下载
        InputStream inputStream = downloadFile("group1", "M00/00/00/wKgBBF8oxtiAR8h2AABBUppopkY173.jpg");

        //将文件写入到本地磁盘
        FileOutputStream fileOutputStream = new FileOutputStream("E:/huanghun.jpg");

        //定义缓冲区
        byte[] bytes = new byte[1024];
        while (inputStream.read(bytes) !=-1){
            fileOutputStream.write(bytes);
        }
        fileOutputStream.flush();
        fileOutputStream.close();
        inputStream.close();*/

   /*     //文件删除
        deleteFile("group1","M00/00/00/wKgBBF8o4yyAPKEgAABuB7U5gRA767.jpg");*/

 /*       //获取storage
        StorageServer storageServer = getStorage();
        System.out.println(storageServer.getStorePathIndex());//获取下标信息
        System.out.println(storageServer.getInetSocketAddress().getHostString());//获取IP信息*/


/*
        //获取Storage信息
        ServerInfo[] groups = getServerInfo("group1", "M00/00/00/wKgBBF8o8FaAYolJAABuB7U5gRA957.jpg");
        for (ServerInfo group:groups){
            System.out.println(group.getIpAddr());
            System.out.println(group.getPort());
        }*/

/*        //获取TrackInfo信息
        System.out.println(getTrackerInfo());*/
    }
}
