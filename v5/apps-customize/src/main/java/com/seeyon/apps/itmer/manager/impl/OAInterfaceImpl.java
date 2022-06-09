package com.seeyon.apps.itmer.manager.impl;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.seeyon.apps.itmer.manager.OAInterface;
import com.seeyon.ctp.common.AppContext;
import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class OAInterfaceImpl implements OAInterface {
    private static final Log log = LogFactory.getLog(OAInterfaceImpl.class);
    public static final String FILE_BOUNDARY = "-----";

    /**
     * 获取OA的token
     *
     * @return
     */
    @Override
    public String getOAToken() {
        //OA的ip和端口
        String ipAndPort = AppContext.getSystemProperty("approvalProcess.ipAndPort");
        //OA的rest用户名和密码
        String restUser = AppContext.getSystemProperty("approvalProcess.restUser");
        //用于发流程的专用用户名
        String loginName = AppContext.getSystemProperty("approvalProcess.loginName");
        //OAtoken获取的接地址
        String url = "http://" + ipAndPort + "/seeyon/rest/token/" + restUser + "?loginName=" + loginName;
        log.info("获取token的url: " + url);
        String result = "";
        //调用rest接口
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            // 4 判断是否请求成功
            if (response.isSuccessful()) {
                // 得到响应体中的身体,将其转成  string
                result = response.body().string();
            }
        } catch (IOException e) {
            log.error("token获取失败！:" + "返回的数据：" + result, e);
        } finally {
            client = null;
        }
        String token = "";
        if (result.contains(":")) {
            JSONObject jsonObject = JSONObject.parseObject(result);
            token = String.valueOf(jsonObject.get("id"));
        } else {
            token = result;
        }
        log.error("token返回的数据:" + token);
        return token;
    }

    /**
     * 文件上传接口
     *
     * @param filePath 附件的绝对路径
     * @param token    OA的token
     * @return
     */
    @Override
    public long fileUpload(String filePath, String token) {
        String ipAndPort = AppContext.getSystemProperty("approvalProcess.ipAndPort");
        String url = "http://" + ipAndPort + "/seeyon/rest/attachment?token=" + token + "&applicationCategory=0&extensions=&firstSave=true";
        log.info("附件上传的url：" + url);
        File uploadFile = new File(filePath);
        StringBuffer sb = null;
        try {
            URL preUrl = new URL(url);
            // 设置请求头
            HttpURLConnection hc = (HttpURLConnection) preUrl.openConnection();
            hc.setDoOutput(true);
            hc.setUseCaches(false);
            hc.setRequestProperty("contentType", "charset=utf-8");
            hc.setRequestMethod("POST");
            hc.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + FILE_BOUNDARY);
            DataOutputStream dos = new DataOutputStream(hc.getOutputStream());
            dos.write(getStartData(uploadFile));
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(uploadFile));
            int data = 0;
            while ((data = input.read()) != -1) {
                dos.write(data);
            }
            dos.write(("\r\n--" + FILE_BOUNDARY + "--\r\n").getBytes());
            dos.flush();
            dos.close();
            sb = new StringBuffer();

            InputStream is = hc.getInputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                sb.append((char) ch);
            }
            if (is != null) is.close();
            if (input != null) input.close();
            if (sb.toString() != null && !"".equals(sb.toString())) {
                log.info("附件上传成功！！ID:" + sb.toString());
            } else {
                log.info("附件上传失败！！");
            }
        } catch (Exception e) {
            log.info("附件上传失败！！错误信息：" + e.getMessage());
        }
        JSONObject dataObject = JSONObject.parseObject(sb.toString());
        Object atts = dataObject.get("atts");
        String jsonString = JSONObject.toJSONString(atts);
        String substring = jsonString.substring(1, jsonString.length() - 1);
        JSONObject object2 = JSONObject.parseObject(substring);
        String fileUrl = String.valueOf(object2.get("fileUrl"));
        long fileId = Long.parseLong(fileUrl);
        //删除已经上传了的附件
        File myDelFile = new File(filePath);
        myDelFile.delete();
        return fileId;


    }

    @Override
    public byte[] getStartData(File file) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        sb.append("--");
        sb.append(FILE_BOUNDARY);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data; \r\n name=\"1\"; filename=\"" + file.getName() + "\"\r\n");
        sb.append("Content-Type: msoffice\r\n\r\n");
        return sb.toString().getBytes("UTF-8");
    }


    /**
     * OA流程发起接口
     *
     * @param token
     * @param data  数据
     * @return
     */
    @Override
    public String processInitiation(String token, String data) {
        String ipAndPort = AppContext.getSystemProperty("approvalProcess.ipAndPort");
        String templateCode = AppContext.getSystemProperty("approvalProcess.templateCode");
        String url = "http://" + ipAndPort + "/seeyon/rest/flow/" + templateCode + "?token=" + token;
        log.info("流程发起传的数据" + data);
        String result = "";
        //调用rest接口
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        try {
            RequestBody body = RequestBody.create(mediaType, data);
            Request request = new Request.Builder().url(url).method("POST", body).addHeader("Content-Type", "application/json").build();
            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (Exception e) {
            log.error("流程发起接口调用失败,返回值:" + result, e);
            return "流程发起接口调用失败！";
        } finally {
            client = null;
            mediaType = null;
        }
        log.error("流程发起接口返回的数据:" + result);
        return result;

    }
}
