/*
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.api.managers.ISubscriptionManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.*;

import java.util.HashMap;

class SubscriptionManager implements ISubscriptionManager {

    public boolean isEINInPsp(SourceSystemCode pSourceSystemCode, String pEIN) {
        return Company.findCompanies(pSourceSystemCode, pEIN).size() > 0;
    }

    public boolean shouldAddCompanyToPsp(SourceSystemCode pSourceSystemCode, ServiceCode pServiceCode) {
        // For v1, only direct deposit customer can be added to PSP
        // TODO:v2 add logic to handle Tax (Assisted) customers
        if (pServiceCode != ServiceCode.DirectDeposit) {
            return false;
        }

        // It is direct deposit, now need to check if system parameter allows adding a company to PSP
        SourcePayrollParameter sourcePayrollParameter = SourcePayrollParameter.findSourcePayrollParameter(pSourceSystemCode, SourcePayrollParameterCode.ShouldAddCompanyToPSP);
        return (sourcePayrollParameter.getParameterValue().equals("1"));
    }

    // EIN
    public ProcessResult<Company> verifyCompanyEIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pEIN, String pSubscriptionNum) {
        IProcess processCore = new VerifyCompanyEINCore(pSourceSystemCd, pSourceCompanyId, pEIN, pSubscriptionNum);
        ProcessResult<Company> processResult = processCore.execute();

        return processResult;
    }

    // PIN
    public ProcessResult<Company> verifyCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPIN) {
        IProcess processCore = new VerifyCompanyPINCore(pSourceSystemCd, pSourceCompanyId, pPIN);
        ProcessResult<Company> processResult = processCore.execute();

        return processResult;
    }

    public ProcessResult<HashMap<String,String>> createCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPIN) {
        IProcess processCore = new CreateCompanyPINCore(pSourceSystemCd, pSourceCompanyId, pPIN);
        ProcessResult<HashMap<String,String>> processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult<HashMap<String,String>> updateCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pPIN) {
        IProcess processCore = new UpdateCompanyPINCore(pSourceSystemCd, pSourceCompanyId, pPIN);
        ProcessResult<HashMap<String,String>> processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult<HashMap<String,String>> resetCompanyPIN(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pEncryptedData, String pUserName) {
        IProcess processCore = new ResetCompanyPINCore(pSourceSystemCd, pSourceCompanyId, pEncryptedData, pUserName);
        ProcessResult<HashMap<String,String>>processResult = processCore.execute();
        return processResult;
    }

    public ProcessResult unlockPINOnce(SourceSystemCode pSourceSystemCd, String pSourceCompanyId) {
        return new UnlockCompanyPINCore(pSourceSystemCd, pSourceCompanyId).execute();
    }

}
