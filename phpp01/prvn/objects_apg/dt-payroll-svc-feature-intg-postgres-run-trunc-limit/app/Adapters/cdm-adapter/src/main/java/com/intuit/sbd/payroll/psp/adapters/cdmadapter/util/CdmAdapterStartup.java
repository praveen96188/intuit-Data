/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intuit.sbd.payroll.psp.adapters.cdmadapter.util;

import com.intuit.sbd.payroll.psp.Application;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author ppednekar
 */
public class CdmAdapterStartup implements ServletContextListener {
    private static SpcfLogger logger = null;
    static {
        logger = Application.getLogger(CdmAdapterStartup.class);
    }


    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        logger.info("Initializing TePS properties");
        try {
            String url = ConfigurationManager.getSettingValue(ConfigurationModule.ViewMyPaycheck, "vmp.teps.url");
            String secret = ConfigurationManager.getSettingValue(ConfigurationModule.ViewMyPaycheck, "vmp.teps.secret");
            String proxyHost = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_launchdarkly_proxyHost");
            System.setProperty("teps.url", url);
            System.setProperty("teps.secret", secret);
            if(!StringUtils.isEmpty(proxyHost)) {
                String proxyPort = ConfigurationManager.getSettingValue(ConfigurationModule.Common, "psp_launchdarkly_proxyPort");
                System.setProperty("teps.proxyHost", proxyHost);
                System.setProperty("teps.proxyPort", proxyPort);
            }
        } catch (Exception ex) {
            logger.error("Error initializing TePS properties", ex);
        }
    }
}
