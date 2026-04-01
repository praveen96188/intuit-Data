/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

public class AddServiceDD implements IProcess {
    private DDCompanyServiceInfo ddCompanyService;
    private DDServiceInfoDTO dtoDDServiceInfo;

    public AddServiceDD(DDCompanyServiceInfo pDomainCompanyService, DDServiceInfoDTO pDTODDServiceInfo) {
        ddCompanyService = pDomainCompanyService;
        dtoDDServiceInfo = pDTODDServiceInfo;
    }

    public ProcessResult execute() {
        ProcessResult processResult = new ProcessResult();

        Service ddService = Application.findById(Service.class, ServiceCode.DirectDeposit);

        if (dtoDDServiceInfo.getAveragePayrollAmount() != null) {
            SpcfMoney avgPayrollRunAmount = new SpcfMoney(
                    SpcfDecimal.createInstance(dtoDDServiceInfo.getAveragePayrollAmount().doubleValue()));
            ddCompanyService.setAveragePayRunAmount(avgPayrollRunAmount);
        } else {
            ddCompanyService.setAveragePayRunAmount(null);
        }

        if (dtoDDServiceInfo.getHighAnnualPayrollAmount() != null) {
            SpcfMoney highPayrollRunAmount = new SpcfMoney(
                    SpcfDecimal.createInstance(dtoDDServiceInfo.getHighAnnualPayrollAmount().doubleValue()));
            ddCompanyService.setHighAnnualPayAmount(highPayrollRunAmount);
        } else {
            ddCompanyService.setHighAnnualPayAmount(null);
        }

        ddCompanyService.setOverrideCompanyLimitAmount(null);
        ddCompanyService.setOverrideEmployeeLimitAmount(null);

        // todo kp: create CustomerSignedUp event

        return processResult;
    }
}
