package com.seeyon.apps.event;


import com.seeyon.apps.xc.enums.CtripEum;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.modules.event.FormBeanAfterSaveEvent;
import com.seeyon.cap4.form.modules.event.FormDataAfterSubmitEvent;
import com.seeyon.ctp.form.bean.FormBean;
import com.seeyon.ctp.form.bean.FormDataBean;
import com.seeyon.ctp.form.modules.event.FormUpdateEvent;
import com.seeyon.ctp.form.service.FormManager;
import com.seeyon.ctp.util.annotation.ListenEvent;
import net.joinwork.bpm.engine.wapi.FormData;

import java.util.Map;


public class FormSaveListener {
    @ListenEvent(event = FormDataAfterSubmitEvent.class)
    public void test2(FormDataAfterSubmitEvent event) {
        FormDataMasterBean source = (FormDataMasterBean) event.getSource();
        Map<String, Object> allDataMap = source.getAllDataMap();

        System.out.println("表单更行触发了2222");
    }
}
