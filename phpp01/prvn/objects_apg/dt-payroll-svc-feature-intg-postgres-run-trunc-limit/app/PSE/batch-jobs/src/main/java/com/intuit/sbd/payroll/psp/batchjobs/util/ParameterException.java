package com.intuit.sbd.payroll.psp.batchjobs.util;

/*
 * Copyright (c) 2011 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

/**
 * Exception thrown when a required parameter is missing or has invalid data
 * @author janderson
 * Date: Mar 25, 2011
 * Time: 10:34:21 AM
 */
public class ParameterException extends RuntimeException {
    public ParameterException(String message) {
        super(message);
    }
}
