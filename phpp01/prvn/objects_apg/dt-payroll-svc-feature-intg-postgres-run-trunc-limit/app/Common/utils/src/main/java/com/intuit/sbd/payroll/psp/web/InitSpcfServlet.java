/*
 * $Id: //psp/dev/Common/Utils/src/com/intuit/sbd/payroll/psp/web/InitSpcfServlet.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.web;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.ShutdownHookManager;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author Wiktor Kozlik
 */
public class InitSpcfServlet extends HttpServlet {

    private static final SpcfLogger mLogger = SpcfLogManager.getLogger(InitSpcfServlet.class);

    public void destroy(){
        //if shutdown is triggered,
        //wait for shutdown hooks to complete before destroying the beans
        try{
        waitForShutdownHooksToComplete();
        } catch (Exception ex) {
            mLogger.error("Error in waitForShutdownHooksToComplete proceeding with destroy method", ex);
        }

        Application.uninitialize();
        ApplicationSecondary.uninitialize();
        super.destroy();
        System.out.println("PSP Application undeployed");
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Application.initialize();
        ApplicationSecondary.initialize();
        System.out.println("PSP Application initialized");
    }

    /*
    Checks if a shutdown is in progress.
    If yes, it first waits for the shutdown to complete
     */
    private void waitForShutdownHooksToComplete() {
        //wait for shutdown hooks to complete before destroying the beans
        ShutdownHookManager shutdownHookManager = PayrollApplicationBeanFactory.getBean(ShutdownHookManager.class);
        if(shutdownHookManager.isShutdownInProgress()) {
            try {
                shutdownHookManager.waitForShutdownHooksToComplete();
            } catch (InterruptedException ex) {
                mLogger.error("Exception while waiting for shutdown hooks to complete", ex);
            } catch (ShutdownHookManager.ShutdownNotTriggeredException ex) {
                mLogger.error("This should not be thrown as we have already checked that shutdown is triggered",ex);
            }

        }
    }
}
