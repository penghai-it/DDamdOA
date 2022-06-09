package com.seeyon.apps.itmer.manager;

import com.seeyon.ctp.util.annotation.AjaxAccess;

/**
 * OA集成钉钉接口
 */
public interface DingTalkOAIntegration {
    //OA获取钉钉中的审批数据
    @AjaxAccess
    public void dingTalkOAApproval();
}
