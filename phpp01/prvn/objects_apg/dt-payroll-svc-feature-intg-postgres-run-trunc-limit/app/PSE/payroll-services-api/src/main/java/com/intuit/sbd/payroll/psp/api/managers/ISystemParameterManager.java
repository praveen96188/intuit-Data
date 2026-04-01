/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/managers/ISystemParameterManager.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;

/**
 * System Parameter interface allows for saving of data into the PSP_SYSTEM_PARAMETER table
 */
public interface ISystemParameterManager {
    ProcessResult<SystemParameter> updateSystemParameterValue(SystemParameter.Code pSystemParameterCd, String pSystemParameterValue);   
    ProcessResult<SystemParameter> updateSystemParameterValue(String pSystemParameterCd, String pSystemParameterValue);
}
