/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdateServiceDDProcess.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.DDCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;

public class UpdateServiceDDProcess implements IProcess {
    private DDCompanyServiceInfo ddCompanyService;
    private DDServiceInfoDTO dtoDDServiceInfo;

    public UpdateServiceDDProcess(DDCompanyServiceInfo pDomainCompanyService, DDServiceInfoDTO pDTODDServiceInfo) {
        ddCompanyService = pDomainCompanyService;
        dtoDDServiceInfo = pDTODDServiceInfo;
    }

    public ProcessResult execute() {
        ProcessResult processResult = new ProcessResult();

        if (dtoDDServiceInfo.getAveragePayrollAmount() != null) {
            SpcfMoney avgPayrollRunAmount = new SpcfMoney(
                    SpcfDecimal.createInstance(dtoDDServiceInfo.getAveragePayrollAmount().doubleValue()));
            ddCompanyService.setAveragePayRunAmount(avgPayrollRunAmount);
        }

        if (dtoDDServiceInfo.getHighAnnualPayrollAmount() != null) {
            SpcfMoney highPayrollRunAmount = new SpcfMoney(
                    SpcfDecimal.createInstance(dtoDDServiceInfo.getHighAnnualPayrollAmount().doubleValue()));
            ddCompanyService.setHighAnnualPayAmount(highPayrollRunAmount);
        }

        return processResult;
    }
}
