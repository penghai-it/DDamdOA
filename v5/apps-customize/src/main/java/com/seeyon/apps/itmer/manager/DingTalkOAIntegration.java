package com.seeyon.apps.itmer.manager;

import com.seeyon.ctp.util.annotation.AjaxAccess;

/**
 * OA集成钉钉接口
 */
public interface DingTalkOAIntegration {
    //OA获取钉钉中的审批数据
    @AjaxAccess
    public void dingTalkOAApproval();

    //查询钉钉实例ID写入OA表中
    @AjaxAccess
    public void ddProcessIdQuery();
}
