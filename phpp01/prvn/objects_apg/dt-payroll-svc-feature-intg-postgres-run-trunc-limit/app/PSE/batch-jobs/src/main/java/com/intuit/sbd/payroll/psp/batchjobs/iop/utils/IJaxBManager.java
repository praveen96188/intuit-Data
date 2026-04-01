/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.iop.utils;

/**
    @author Jeff Jones
 */
public interface IJaxBManager {

    /**
     *
     * @param pObject
     * @return String
     * @throws Exception
     */
    public String marshall(Object pObject) throws Exception;

}
