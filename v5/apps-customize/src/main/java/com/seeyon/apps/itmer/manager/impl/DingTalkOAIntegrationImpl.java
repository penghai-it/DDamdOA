package com.seeyon.apps.itmer.manager.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.itmer.manager.DingTalkOAIntegration;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.SystemEnvironment;
import com.seeyon.ctp.util.JDBCAgent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DingTalkOAIntegrationImpl implements DingTalkOAIntegration {
    private static final Log log = LogFactory.getLog(DingTalkOAIntegrationImpl.class);

    /**
     * 第一张表单
     */
    @Override
    public void dingTalkOAApproval() {
        JDBCAgent jdbcAgent = new JDBCAgent(true, true);
        try {
            //钉钉接口Bean
            DingTaikInterfaceImpl dingTaikInterfaceImpl = (DingTaikInterfaceImpl) AppContext.getBean("dingTaikInterfaceImpl");
            //OA接口Bean对象
            OAInterfaceImpl oAInterfaceImpl = (OAInterfaceImpl) AppContext.getBean("oAInterfaceImpl");
            //获取token
            String accessToken = dingTaikInterfaceImpl.getAccessToken();
            //表名通过表单编号获取表名
            CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
            String recordFormCode = AppContext.getSystemProperty("approvalProcess.recordFormCode");
            FormBean formBean = cap4FormManager.getFormByFormCode(recordFormCode);
            //记录表名
            String formName = formBean.getMasterTableBean().getTableName();
            //获取钉钉审批的实例列表ID
            // List<String> exampleIdList = dingTaikInterfaceImpl.getExampleIdList(accessToken);
            String SQL = "SELECT * FROM " + formName + " WHERE 判断的字段=0";//需要完善sql TODO
            jdbcAgent.execute(SQL);
            List listData = jdbcAgent.resultSetToList();
            if (listData.size() != 0) {
                Object[] listDataArray = listData.toArray();
                for (Object dataArray : listDataArray) {
                    String StringData = JSONObject.toJSONString(dataArray);
                    JSONObject jsonData = JSONObject.parseObject(StringData);
                    //实例id TODO
                    String exampleId = String.valueOf(jsonData.get("审批id字段"));
                    //是属于那张表  TODO
                    String fromId = String.valueOf(jsonData.get("判断是那张表字段"));
            /*    }

            }

            for (String exampleId : exampleIdList) {
                //判断当前实例id是否存在Start  TODO

                int execute = jdbcAgent.execute(SQL);
                if (true) { //添加为实例id时候存在 TODO*/
                    //获取表实例数据
                    JSONObject exampleData = dingTaikInterfaceImpl.getExampleData(exampleId, accessToken);
                    String exampleDataString = JSONObject.toJSONString(exampleData);
                    JSONObject exampleDataJson = JSONObject.parseObject(exampleDataString);
                    Object process_instance = exampleDataJson.get("process_instance");
                    String processinstanceString = JSONObject.toJSONString(process_instance);
                    JSONObject processinstanceJson = JSONObject.parseObject(processinstanceString);
                    //查询流程的状态
                    String status = String.valueOf(processinstanceJson.get("status"));
                    //判断流程是否结束了
                    if ("COMPLETED".equals(status)) {
                        //获取表单数据
                        List formComponentValues = (List) processinstanceJson.get("form_component_values");
                        Map<String, Object> map = new HashMap<>();
                        //数据MAP
                        Map<Object, Object> dataMap = new HashMap<>();
                        for (Object formComponentValue : formComponentValues) {
                            String formComponentValueString = JSONObject.toJSONString(formComponentValue);
                            JSONObject formComponentValueJson = JSONObject.parseObject(formComponentValueString);
                            //判断是否是附件
                            if ("DDAttachment".equals(formComponentValueJson.get("component_type"))) {
                                String fileValueString = String.valueOf(formComponentValueJson.get("value"));
                                List fileValueList = (List) JSON.parse(fileValueString);
                                if (fileValueList != null && !fileValueList.isEmpty()) {
                                    List<Long> formcontentatt = new ArrayList();
                                    for (Object fileValue : fileValueList) {
                                        JSONObject fileValueJson = JSONObject.parseObject(JSONObject.toJSONString(fileValue));
                                        //文件id
                                        String fileId = String.valueOf(fileValueJson.get("fileId"));
                                        //文件名
                                        String fileName = String.valueOf(fileValueJson.get("fileName"));
                                        //获取钉钉附件
                                        JSONObject attachmentData = dingTaikInterfaceImpl.getAttachment(exampleId, accessToken, fileId);
                                        JSONObject result = JSONObject.parseObject(JSONObject.toJSONString(attachmentData.get("result")));
                                        //附件下载地址
                                        String downloadUri = String.valueOf(result.get("download_uri"));
                                        AttachmentOperationImpl attachmentOperationImpl = (AttachmentOperationImpl) AppContext.getBean("attachmentOperationImpl");
                                        //环境安装目录
                                        String applicationFolder = SystemEnvironment.getApplicationFolder();
                                        //文件保存目录
                                        String filePath = applicationFolder.split("ApacheJetspeed")[0] + "base/data";
                                        //附件下载（返回的文件路径）
                                        String download = attachmentOperationImpl.download(downloadUri, fileName, filePath);
                                        //OA的token
                                        String oaToken = oAInterfaceImpl.getOAToken();
                                        //返回的附件fileUrl
                                        long fileUrl = oAInterfaceImpl.fileUpload(download, oaToken);
                                        formcontentatt.add(fileUrl);
                                    }
                                    //附件信息
                                    map.put("formContentAtt", formcontentatt);
                                }
                            } else {
                                //除了附件其他数据都以name为key值，value为数据value
                                dataMap.put(formComponentValueJson.get("name"), formComponentValueJson.get("value"));
                            }
                        }
                        //最终数据据转Json用于调OA的流程发起
                        map.put("senderLoginName", AppContext.getSystemProperty("approvalProcess.loginName"));
                        map.put("data", dataMap);
                        String data = JSONUtils.toJSONString(map);
                        if ("1".equals(fromId)) {
                            String result = oAInterfaceImpl.processInitiation(accessToken, data);
                            log.info("流程发起后返回的信息！__接口1" + result);
                        }
                        String result = oAInterfaceImpl.processInitiation2(accessToken, data);
                        log.info("流程发起后返回的信息！__接口2" + result);
                        //更新语句需要完善 TODO
                        String SQL1 = "UPDATE " + formName + " SET 判断的字段=1 WHERE 流程id= " + exampleId;
                        jdbcAgent.execute(SQL1);
                    }
                }
            }

        } catch (Exception e) {
            log.info("OA获取数据失败！", e);
        } finally {
            //关闭数据库链接
            jdbcAgent.close();
        }
    }

    /**
     * 查询钉钉实例ID写入OA表中
     */
    @Override
    public void ddProcessIdQuery() {
        JDBCAgent jdbcAgent = new JDBCAgent(true, true);
        try {
            //钉钉接口Bean
            DingTaikInterfaceImpl dingTaikInterfaceImpl = (DingTaikInterfaceImpl) AppContext.getBean("dingTaikInterfaceImpl");
            //OA接口Bean对象
            //OAInterfaceImpl oAInterfaceImpl = (OAInterfaceImpl) AppContext.getBean("oAInterfaceImpl");
            //获取token
            String accessToken = dingTaikInterfaceImpl.getAccessToken();
            //获取钉钉审批的实例列表ID__接口1
            List<String> exampleIdList = dingTaikInterfaceImpl.getExampleIdList(accessToken);
            //表名通过表单编号获取表名
            CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
            String recordFormCode = AppContext.getSystemProperty("approvalProcess.recordFormCode");
            FormBean formBean = cap4FormManager.getFormByFormCode(recordFormCode);
            //记录表名
            String formName = formBean.getMasterTableBean().getTableName();
            StringBuilder SQL = new StringBuilder("INSERT INTO " + formName + " (?,?,?) VALUES ");
            for (String exampleId : exampleIdList) {
                //判断当前实例id是否存在Start  TODO
                String SQL1 = "SELECT ? FROM " + formName + " WHERE ? = " + exampleId;
                jdbcAgent.execute(SQL1);
                List resultList = jdbcAgent.resultSetToList();
                if (resultList.size() == 0) { // 判断条件是否正确 TODO
                    //判断是否是最后一个元素
                    if (exampleId.equals(exampleIdList.get(exampleIdList.size() - 1))) {
                        SQL.append("(").append(exampleId).append(" ,1,0)");
                    } else {
                        SQL.append("(").append(exampleId).append(" ,1,0),");
                    }
         /*           //获取表实例数据
                    JSONObject exampleData = dingTaikInterfaceImpl.getExampleData(exampleId, accessToken);
                    String exampleDataString = JSONObject.toJSONString(exampleData);
                    JSONObject exampleDataJson = JSONObject.parseObject(exampleDataString);
                    Object process_instance = exampleDataJson.get("process_instance");
                    String processinstanceString = JSONObject.toJSONString(process_instance);
                    JSONObject processinstanceJson = JSONObject.parseObject(processinstanceString);
                    //查询流程的状态
                    String status = String.valueOf(processinstanceJson.get("status"));
                    //判断流程是否结束了
                    if ("COMPLETED".equals(status)) {
                        //记录当前实例的id（及 exampleId）TODO

                        //获取表单数据
                        List formComponentValues = (List) processinstanceJson.get("form_component_values");
                        Map<String, Object> map = new HashMap<>();
                        //数据MAP
                        Map<Object, Object> dataMap = new HashMap<>();
                        for (Object formComponentValue : formComponentValues) {
                            String formComponentValueString = JSONObject.toJSONString(formComponentValue);
                            JSONObject formComponentValueJson = JSONObject.parseObject(formComponentValueString);
                            //判断是否是附件
                            if ("DDAttachment".equals(formComponentValueJson.get("component_type"))) {
                                //附件数据处理 TODO
                                String fileValueString = String.valueOf(formComponentValueJson.get("value"));
                                List fileValueList = (List) JSON.parse(fileValueString);
                                if (fileValueList != null && !fileValueList.isEmpty()) {
                                    List<Long> formcontentatt = new ArrayList();
                                    for (Object fileValue : fileValueList) {
                                        JSONObject fileValueJson = JSONObject.parseObject(JSONObject.toJSONString(fileValue));
                                        //文件id
                                        String fileId = String.valueOf(fileValueJson.get("fileId"));
                                        //文件名
                                        String fileName = String.valueOf(fileValueJson.get("fileName"));
                                        //获取钉钉附件
                                        JSONObject attachmentData = dingTaikInterfaceImpl.getAttachment(exampleId, accessToken, fileId);
                                        JSONObject result = JSONObject.parseObject(JSONObject.toJSONString(attachmentData.get("result")));
                                        //附件下载地址
                                        String downloadUri = String.valueOf(result.get("download_uri"));
                                        AttachmentOperationImpl attachmentOperationImpl = (AttachmentOperationImpl) AppContext.getBean("attachmentOperationImpl");
                                        //环境安装目录
                                        String applicationFolder = SystemEnvironment.getApplicationFolder();
                                        //文件保存目录
                                        String filePath = applicationFolder.split("ApacheJetspeed")[0] + "base/data";
                                        //附件下载（返回的文件路径）
                                        String download = attachmentOperationImpl.download(downloadUri, fileName, filePath);

                                        //OA的token
                                        String oaToken = oAInterfaceImpl.getOAToken();
                                        //返回的附件fileUrl
                                        long fileUrl = oAInterfaceImpl.fileUpload(download, oaToken);
                                        formcontentatt.add(fileUrl);
                                    }
                                    //附件信息
                                    map.put("formContentAtt", formcontentatt);
                                }
                            } else {
                                //除了附件其他数据都以name为key值，value为数据value
                                dataMap.put(formComponentValueJson.get("name"), formComponentValueJson.get("value"));
                            }
                        }
                        //最终数据据转Json用于调OA的流程发起
                        map.put("senderLoginName", AppContext.getSystemProperty("approvalProcess.loginName"));
                        map.put("data", dataMap);
                        String data = JSONUtils.toJSONString(map);
                        String result = oAInterfaceImpl.processInitiation2(accessToken, data);
                        log.info("流程发起后返回的信息！" + result);
                    }*/
                }
            }
            //批量插入接口1的数据
            int execute = jdbcAgent.execute(SQL.toString());
            //获取钉钉审批的实例列表ID__接口2
            List<String> exampleIdList2 = dingTaikInterfaceImpl.getExampleIdList2(accessToken);
            for (String exampleId2 : exampleIdList2) {
                //判断当前实例id是否存在Start
                String SQL1 = "SELECT ? FROM " + formName + " WHERE ? = " + exampleId2;
                jdbcAgent.execute(SQL1);
                List resultList2 = jdbcAgent.resultSetToList();
                if (resultList2.size() == 0) { //添加为实例id时候存在 TODO
                    //判断是否是最后一个元素
                    if (exampleId2.equals(exampleIdList2.get(exampleIdList2.size() - 1))) {
                        SQL.append("(").append(exampleId2).append(" ,2,0)");
                    } else {
                        SQL.append("(").append(exampleId2).append(" ,2,0),");
                    }
                }
            }
            //批量插入接口2的数据
            int execute1 = jdbcAgent.execute(SQL.toString());
        } catch (Exception e) {
            log.info("查询钉钉流程id写入OA表中失败！", e);
        } finally {
            //关闭数据库链接
            jdbcAgent.close();
        }
    }
}
