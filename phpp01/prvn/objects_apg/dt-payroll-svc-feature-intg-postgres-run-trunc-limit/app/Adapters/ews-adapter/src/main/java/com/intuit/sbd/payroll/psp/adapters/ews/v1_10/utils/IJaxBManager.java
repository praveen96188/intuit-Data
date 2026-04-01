/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

/**
    @author Jeff Jones
 */
public interface IJaxBManager {
    /**
     *
     * @param pObject
     * @return
     * @throws Exception
     */
    public String marshall(Object pObject) throws Exception;

    /**
     *
     * @param pXML
     * @return
     * @throws Exception
     */
    public Object Unmarshall(String pXML) throws Exception;

}