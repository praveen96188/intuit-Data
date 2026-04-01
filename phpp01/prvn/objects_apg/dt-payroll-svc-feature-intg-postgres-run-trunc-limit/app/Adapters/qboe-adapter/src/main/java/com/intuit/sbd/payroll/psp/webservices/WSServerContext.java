package com.intuit.sbd.payroll.psp.webservices;

import intuit.osp.common.wsf.base.WSException;


/**
 * Created by IntelliJ IDEA.
 * User: rshenderovsky
 * Date: Apr 23, 2007
 * Time: 11:22:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class WSServerContext extends intuit.osp.common.wsf.server.WSServerContext {

	// preload
	static {
		com.sun.xml.bind.ContextFactory_1_0_1.class.getName();
	}

	public WSServerContext(String serviceName, String operationName) throws WSException {
		super(serviceName, operationName);
	}
}
