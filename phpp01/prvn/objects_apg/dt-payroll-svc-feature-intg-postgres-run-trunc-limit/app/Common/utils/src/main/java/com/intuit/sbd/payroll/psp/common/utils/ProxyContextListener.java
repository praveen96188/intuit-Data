package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbg.psp.proxyInjector.service.ProxyServerSetup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application context listener implementation initializes the Logging system for batch jobs web application
 *
 * @author kmuthurangam
 *
 */
public class ProxyContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContext) {

        if(ProxyServerSetup.isProxyServerRequired()) {
            ProxyServerSetup.initialize();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContext) {

    }
}