package com.seeyon.apps.itmer.manager.impl;


import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiProcessinstanceFileUrlGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiProcessinstanceFileUrlGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.seeyon.apps.itmer.manager.DingTaikInterface;
import com.seeyon.ctp.common.AppContext;
import com.taobao.api.ApiException;
import com.alibaba.fastjson.JSONObject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public class DingTaikInterfaceImpl implements DingTaikInterface {
    //获取钉钉access_token
    @Override
    public String getAccessToken() throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(AppContext.getSystemProperty("approvalProcess.tokenUrl"));
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(AppContext.getSystemProperty("approvalProcess.appKey"));
        request.setAppsecret(AppContext.getSystemProperty("approvalProcess.appSecret"));
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);
        String body = response.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);
        return String.valueOf(jsonObject.get("access_token"));
    }

    /**
     * 获取钉钉审批的实例列表ID
     *
     * @param token
     * @return exampleIdList 所有的实例id集合
     * @throws ApiException
     */
    @Override
    public List<String> getExampleIdList(String token) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(AppContext.getSystemProperty("approvalProcess.exampleId"));
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode(AppContext.getSystemProperty("approvalProcess.fromCode"));//表单编号的
        //当天零点（开始时间）
        LocalDateTime startData = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        //当天24点（结束时间）
        LocalDateTime endData = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long startTime = startData.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long endTime = endData.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        req.setStartTime(startTime);
        req.setEndTime(endTime);
        /*   req.setSize(10L);
        req.setCursor(0L);
        req.setUseridList("manager1,manager2");*/
        OapiProcessinstanceListidsResponse rsp = client.execute(req, token);
        JSONObject jsonObject = JSONObject.parseObject(rsp.getBody());
        Object result = jsonObject.get("result");
        String IdList = JSONObject.toJSONString(result);
        JSONObject IdListJson = JSONObject.parseObject(IdList);
        List<String> exampleIdList = (List<String>) IdListJson.get("list");
        return exampleIdList;
    }

    /**
     * 获取表实例数据
     *
     * @param exampleId 实例id
     * @param token
     * @return ExampleData 实例所有相关数据
     * @throws ApiException
     */
    @Override
    public JSONObject getExampleData(String exampleId, String token) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(AppContext.getSystemProperty("approvalProcess.exampleData"));
        OapiProcessinstanceGetRequest req = new OapiProcessinstanceGetRequest();
        req.setProcessInstanceId(exampleId);
        OapiProcessinstanceGetResponse rsp = client.execute(req, token);
        JSONObject ExampleData = JSONObject.parseObject(rsp.getBody());
        return ExampleData;
    }

    /**
     * 获取审批流程的附件
     *
     * @param exampleId 实例id
     * @param token
     * @param fileid    文件id
     * @return attachmentData 返回的附件数据
     */
    @Override
    public JSONObject getAttachment(String exampleId, String token, String fileid) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(AppContext.getSystemProperty("approvalProcess.attachmentUrl"));
        OapiProcessinstanceFileUrlGetRequest req = new OapiProcessinstanceFileUrlGetRequest();
        OapiProcessinstanceFileUrlGetRequest.GrantCspaceRequest obj1 = new OapiProcessinstanceFileUrlGetRequest.GrantCspaceRequest();
        obj1.setProcessInstanceId(exampleId);//就是bbbb方法中返回的实例id
        obj1.setFileId(fileid);
        req.setRequest(obj1);
        OapiProcessinstanceFileUrlGetResponse rsp = client.execute(req, token);//token
        JSONObject attachmentData = JSONObject.parseObject(rsp.getBody());
        return attachmentData;
    }
}
