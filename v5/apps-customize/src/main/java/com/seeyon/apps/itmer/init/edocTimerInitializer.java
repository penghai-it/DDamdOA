package com.seeyon.apps.itmer.init;

import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.plugin.PluginInitializer;
import org.apache.log4j.Logger;

public class edocTimerInitializer implements PluginInitializer{
    @Override
    public boolean isAllowStartup(PluginDefinition pluginDefinition, Logger logger) {
        logger.info("testTimer will run automaticely");
//        return "1".equals(AppContext.getSystemProperty("testTimer.runAtStart"));
        return true;
    }
}
