package com.seeyon.apps.itmer.manager.impl;

import com.seeyon.apps.itmer.manager.AttachmentOperation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AttachmentOperationImpl implements AttachmentOperation {
    /**
     * 附件下载
     *
     * @param url      下载地址
     * @param name     文件名
     * @param filePath 本地保存位置
     * @return 文件的路径
     */
    @Override
    public String download(String url, String name, String filePath) {
        //System.out.println("fileName---->"+filePath);
        //创建不同的⽂件夹⽬录
        File file = new File(filePath);
        //判断⽂件夹是否存在
        if (!file.exists()) {
            //如果⽂件夹不存在，则创建新的的⽂件夹
            file.mkdirs();
        }
        FileOutputStream fileOut = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        try {
            // 建⽴链接
            URL httpUrl = new URL(url);
            conn = (HttpURLConnection) httpUrl.openConnection();
            //以Post⽅式提交表单，默认get⽅式
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // post⽅式不能使⽤缓存
            conn.setUseCaches(false);
            //连接指定的资源
            conn.connect();
            //获取⽹络输⼊流
            inputStream = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            //判断⽂件的保存路径后⾯是否以/结尾
            if (!filePath.endsWith("/")) {
                filePath += "/";
            }
            //写⼊到⽂件（注意⽂件保存路径的后⾯⼀定要加上⽂件的名称）
            fileOut = new FileOutputStream(filePath + name);
            BufferedOutputStream bos = new BufferedOutputStream(fileOut);
            byte[] buf = new byte[4096];
            int length = bis.read(buf);
            //保存⽂件
            while (length != -1) {
                bos.write(buf, 0, length);
                length = bis.read(buf);
            }
            bos.close();
            bis.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();

        }
        return filePath + name;
    }
    
}

