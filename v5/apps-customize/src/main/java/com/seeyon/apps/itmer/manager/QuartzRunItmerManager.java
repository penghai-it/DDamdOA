package com.seeyon.apps.itmer.manager;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.quartz.QuartzJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author penghai
 * @type Class
 * @date 2022年01月10日 10:01
 */
public class QuartzRunItmerManager implements QuartzJob {
    private static final Log log = LogFactory.getLog(QuartzRunItmerManager.class);

    /**
     * 定时发起流程
     *
     * @author penghai
     * @type method
     * @date 2022/1/10 15:39
     */
    public static void StartTask() {
        Calendar cal = Calendar.getInstance();
        Date start = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();
        // parameters里放要在任务中取出的参数
        Map<String, String> parameters = new HashMap<>();
        parameters.put("name", "DDOA");
        try {
            // 凌晨01:00
            if (QuartzHolder.hasQuartzJob("DDOAShinai", "dingTalkOAIntegrationImpl")) {
                QuartzHolder.deleteQuartzJobByGroupAndJobName("DDOAShinai", "dingTalkOAIntegrationImpl");
            }
            String startItmer = AppContext.getSystemProperty("approvalProcess.startItmer");
            QuartzHolder.newCronQuartzJob("DDOAShinai", "dingTalkOAIntegrationImpl", startItmer, start, end, "QuartzRunItmerManager", parameters);
            log.info("定时发起流程注入成功");
        } catch (Exception ex) {
            log.info("定时器启动失败", ex);
        }
    }


    @Override
    public void execute(Map<String, String> map) {
        String _name = map.get("name");
        switch (_name) {
            case "DDOA":
                DingTalkOAIntegration dingTalkOAIntegration = (DingTalkOAIntegration) AppContext.getBean("dingTalkOAIntegrationImpl");
                if (dingTalkOAIntegration != null) {
                    log.info("start processInit success");
                    dingTalkOAIntegration.dingTalkOAApproval();
                }
        }
    }
}
