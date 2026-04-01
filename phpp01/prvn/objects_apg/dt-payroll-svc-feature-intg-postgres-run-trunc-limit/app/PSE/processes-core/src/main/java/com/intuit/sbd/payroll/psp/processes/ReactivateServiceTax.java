/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/ReactivateServiceTax.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;


import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;

public class ReactivateServiceTax extends Process implements IProcess {
    private CompanyService taxCompanyService;

    public ReactivateServiceTax(CompanyService pDomainCompanyService, ServiceCode pServiceCode) {
        taxCompanyService = pDomainCompanyService;
    }

    public ProcessResult validate() {
        ProcessResult results = new ProcessResult();
        return results;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        RAFEnrollment rafEnrollment = taxCompanyService.getCompany().getCurrentRAFEnrollment();
        if (rafEnrollment!=null) {
            if (rafEnrollment.getStatus() == RAFEnrollmentStatus.Deleted ||  rafEnrollment.getStatus() == RAFEnrollmentStatus.Cancelled) {

                if (rafEnrollment.getStatus() == RAFEnrollmentStatus.Deleted) {
                    ProcessResult updateRAFEnrollmentResult =
                        PayrollServices.companyManager.updateRAFEnrollmentStatus(taxCompanyService.getCompany().getSourceSystemCd(),
                                taxCompanyService.getCompany().getSourceCompanyId(), rafEnrollment,
                                RAFEnrollmentStatus.Cancelled);
                    processResult.merge(updateRAFEnrollmentResult);
                }

                ProcessResult updateRAFEnrollmentResult =
                    PayrollServices.companyManager.updateRAFEnrollmentStatus(taxCompanyService.getCompany().getSourceSystemCd(),
                            taxCompanyService.getCompany().getSourceCompanyId(), null,
                            RAFEnrollmentStatus.PendingEnrollment);
                processResult.merge(updateRAFEnrollmentResult);
            }

            if (rafEnrollment.getStatus() == RAFEnrollmentStatus.PendingDeleteTape) {
                ProcessResult updateRAFEnrollmentResult =
                        PayrollServices.companyManager.updateRAFEnrollmentStatus(taxCompanyService.getCompany().getSourceSystemCd(),
                                taxCompanyService.getCompany().getSourceCompanyId(), rafEnrollment,
                                RAFEnrollmentStatus.Enrolled);
                    processResult.merge(updateRAFEnrollmentResult);
            }
        }


        return processResult;
    }
}