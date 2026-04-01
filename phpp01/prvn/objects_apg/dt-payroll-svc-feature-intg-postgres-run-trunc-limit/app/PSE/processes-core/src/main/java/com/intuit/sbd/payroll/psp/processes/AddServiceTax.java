/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddServiceTax.java#4 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

public class AddServiceTax extends Process {
    private TaxCompanyServiceInfo mTaxCompanyService;
    private TaxServiceInfoDTO mTaxServiceInfoDTO;

    public AddServiceTax(TaxCompanyServiceInfo pDomainCompanyService, TaxServiceInfoDTO pTaxServiceInfoDTO) {
        mTaxCompanyService = pDomainCompanyService;
        mTaxServiceInfoDTO = pTaxServiceInfoDTO;
    }

    public ProcessResult<TaxCompanyServiceInfo> process() {
        ProcessResult result = new ProcessResult();
        Company company = mTaxCompanyService.getCompany();

        if (!company.hasService(ServiceCode.Tax)) {
            company.addCompanyService(mTaxCompanyService);
        }

        // Add IRS and Create EFTPS enrollment
        CompanyAgency.addCompanyAgency(company, Agency.IRS, mTaxCompanyService.getStatusEffectiveDate());

        ProcessResult updateEnrollmentResult =
                PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        EftpsEnrollmentStatus.PendingEnrollment);
        result.merge(updateEnrollmentResult);

        ProcessResult updateRAFEnrollmentResult =
                PayrollServices.companyManager.updateRAFEnrollmentStatus(company.getSourceSystemCd(),
                        company.getSourceCompanyId(), null,
                        RAFEnrollmentStatus.PendingEnrollment);
        result.merge(updateRAFEnrollmentResult);

        mTaxCompanyService.setLastQuarterToFile(mTaxServiceInfoDTO.getLastQuarterToFile());
        mTaxCompanyService.setFinalAnnualReturns(false);
        mTaxCompanyService.setFileAnnualReturns(false);
        mTaxCompanyService.setLastPayrollDate(null);
        mTaxCompanyService.setW2DeliveryPreferenceCd(mTaxServiceInfoDTO.getW2DeliveryPreferenceCd());
        mTaxCompanyService.setClientPacketDeliveryPreferenceCd(mTaxServiceInfoDTO.getClientPacketDeliveryPreferenceCd());
        mTaxCompanyService.setLastTaxYear(mTaxServiceInfoDTO.getLastTaxYear());

        result.setSuccess(true);

        result.setResult(mTaxCompanyService);
        return result;

    }

    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();
        if (mTaxCompanyService == null) {
            result.getMessages().CompanyServiceNotSpecified(EntityName.CompanyService, null);
        }
        return result;
    }
}
