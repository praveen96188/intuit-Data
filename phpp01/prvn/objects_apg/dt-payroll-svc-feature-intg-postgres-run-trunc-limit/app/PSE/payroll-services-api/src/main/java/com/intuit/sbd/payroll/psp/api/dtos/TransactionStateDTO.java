/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/TransactionStateDTO.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * @author Wiktor Kozlik
 */
public enum TransactionStateDTO {
    Pending,
    Executed,
    Canceled,
    Returned,
    Completed,
    Voided
}
