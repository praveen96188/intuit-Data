/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import javax.xml.bind.JAXBContext;

/**
    @author Jeff Jones
 */
public class JaxBFactory {
    public static IJaxBManager getManagerInstance(Class pClass) throws Exception {
        return new JaxBManagerImpl(JAXBContext.newInstance(pClass));
    }
}