/*
 * $Id: //psp/dev/Gateways/AS400/src/com/intuit/sbd/payroll/psp/gateways/as400/SocketManagerFactory.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.qbdt.socket;

import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

/**
 * This object is intended to create handles to ISocketManager implementation classes to be returned to the user.  The
 * user then handles all the socket transactions through the ISocketManager interface.
 */
public class SocketManagerFactory {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(SocketManagerFactory.class);

    private static Class<? extends ISocketManager> instanceClass;

    public static void setInstanceClass(Class<? extends ISocketManager> instanceClass) {
        SocketManagerFactory.instanceClass = instanceClass;
    }

    public static ISocketManager createISocketManager() {
        try{
            if (instanceClass != null) {
                return instanceClass.newInstance();
            } else {
                return null;
            }
        }catch(Throwable t){
            logger.info("Socket Manager could not be constructed.", t);
            return null;
        }

    }
}
