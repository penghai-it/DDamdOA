package com.seeyon.apps.itmer.init;

import com.seeyon.apps.itmer.manager.QuartzRunItmerManager;
import com.seeyon.ctp.common.AbstractSystemInitializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author penghai
 * @type Class
 * @date 2022年01月10日 10:20
 */
public class edocQuartzInit extends AbstractSystemInitializer {
    private static final Log log = LogFactory.getLog(edocQuartzInit.class);

    @Override
    public void initialize() {
        log.info("定时任务开始");
        QuartzRunItmerManager.StartTask();
        QuartzRunItmerManager.StartTask2();
    }

    @Override
    public void destroy() {
    }
}
