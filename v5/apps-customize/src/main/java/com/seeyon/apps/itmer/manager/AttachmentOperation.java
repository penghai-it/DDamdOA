package com.seeyon.apps.itmer.manager;

/**
 * 附件操作
 */
public interface AttachmentOperation {
    /**
     * 附件下载
     *
     * @param url      下载地址
     * @param name     文件名
     * @param filePath 本地保存位置
     * @return
     */
    public String download(String url, String name, String filePath);


}
