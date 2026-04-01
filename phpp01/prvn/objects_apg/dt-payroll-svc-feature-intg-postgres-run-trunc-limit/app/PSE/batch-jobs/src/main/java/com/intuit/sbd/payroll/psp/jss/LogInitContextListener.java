package com.intuit.sbd.payroll.psp.jss;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.intuit.sbd.payroll.psp.jss.util.Log4jConfigurator;

/**
 * Application context listener implementation initializes the Logging system for batch jobs web application
 * 
 * @author kmuthurangam
 *
 */
public class LogInitContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent servletContext) {
		Log4jConfigurator.configure();
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContext) {

	}
}
