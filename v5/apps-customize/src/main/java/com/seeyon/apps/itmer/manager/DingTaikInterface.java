package com.seeyon.apps.itmer.manager;

import com.taobao.api.ApiException;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 钉钉的相关接口
 */
public interface DingTaikInterface {
    //获取钉钉access_token
    public String getAccessToken() throws ApiException;

    /**
     * 获取钉钉审批的实例列表ID
     *
     * @param token
     * @return
     * @throws ApiException
     */
    public List<String> getExampleIdList(String token) throws ApiException;

    /**
     * 获取表实例数据
     *
     * @param exampleId 实例id
     * @param token
     * @return
     * @throws ApiException
     */
    public JSONObject getExampleData(String exampleId, String token) throws ApiException;

    /**
     * 获取审批流程的附件
     *
     * @param exampleId 实例id
     * @param token
     * @param fileid    文件id
     * @return 返回的附件数据
     */
    public JSONObject getAttachment(String exampleId, String token, String fileid) throws ApiException;
}
