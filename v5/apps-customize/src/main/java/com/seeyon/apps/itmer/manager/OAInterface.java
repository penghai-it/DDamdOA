package com.seeyon.apps.itmer.manager;

import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * OA的相关操作接口
 */
public interface OAInterface {
    /**
     * 获取OA的token
     *
     * @return
     */
    public String getOAToken();

    /**
     * 文件上传接口
     *
     * @param filePath 附件的绝对路径
     * @param token    OA的token
     * @return
     */
    public long fileUpload(String filePath, String token);

    /**
     * OA流程发起接口
     *
     * @param token
     * @param data  数据
     * @return
     */
    public String processInitiation(String token, String data);

    /**
     * OA流程发起接口--2
     *
     * @param token
     * @param data  数据
     * @return
     */
    public String processInitiation2(String token, String data);

    public byte[] getStartData(File file) throws UnsupportedEncodingException;
}
