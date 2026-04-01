/*
 * $Id: //psp/dev/PSE/PayrollServicesAPIImpl/src/com/intuit/sbd/payroll/psp/api/impl/managers/SystemParameterManager.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.api.managers.ISystemParameterManager;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.UpdateSystemParameterCore;

/**
 * Implementation Class for the System Parameter Manager.  This will allow the application to save values into
 * the system parameter table.
 */
class SystemParameterManager implements ISystemParameterManager {
   
    public ProcessResult<SystemParameter> updateSystemParameterValue(SystemParameter.Code pSystemParameterCd, String pSystemParameterValue) {
        UpdateSystemParameterCore sysParamCore = new UpdateSystemParameterCore(pSystemParameterCd, pSystemParameterValue);
        ProcessResult<SystemParameter> processResult = sysParamCore.execute();
        processResult.setResult(sysParamCore.getSystemParameter());

        return processResult;
    }

    public ProcessResult<SystemParameter> updateSystemParameterValue(String pSystemParameterCd, String pSystemParameterValue) {
        UpdateSystemParameterCore sysParamCore = new UpdateSystemParameterCore(pSystemParameterCd, pSystemParameterValue);
        ProcessResult<SystemParameter> processResult = sysParamCore.execute();
        processResult.setResult(sysParamCore.getSystemParameter());

        return processResult;
    }
}
