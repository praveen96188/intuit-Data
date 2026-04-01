/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddServiceCheckDistribution.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.dtos.CheckDistributionServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.CDCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

public class AddServiceCheckDistribution extends Process implements IProcess {

    private Company company;
    private CheckDistributionServiceInfoDTO dtoService;

    public AddServiceCheckDistribution(Company pCompany, CheckDistributionServiceInfoDTO pDTOService) {
        company = pCompany;
        dtoService = pDTOService;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

         boolean bIsOnTax = company.getSourceSystemCd() == SourceSystemCode.QBDT && company.isCompanyOnService(ServiceCode.Tax);

        if (!bIsOnTax) {
            validationResult.getMessages().NotAnAS400Company(EntityName.Company, company.getSourceCompanyId());
        }
        return validationResult;
    }

    public ProcessResult process() {
        CDCompanyServiceInfo companyService = new CDCompanyServiceInfo();
        companyService.setLastPaycheckId(dtoService.getLastPaycheckId());

        ProcessResult processResult = new ProcessResult();
        processResult.setResult(companyService);
        processResult.setSuccess(true);
        return processResult;
    }
}
