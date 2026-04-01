/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/DeactivateServiceTax.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

public class DeactivateServiceTax extends Process implements IProcess {

    private CompanyService taxCompanyService;


    public DeactivateServiceTax(CompanyService pDomainCompanyService) {
        taxCompanyService = pDomainCompanyService;
    }

    public ProcessResult validate() {
        return new ProcessResult();
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();


        //Update LastPayrollDate on TaxCompanyServiceInfo
        SpcfCalendar latestPaycheckDate = Application.executeObjectAggQuery(PayrollRun.class,
                                                                            new Query<PayrollRun>().Select(PayrollRun.PaycheckDate().Max())
                                                                                                   .Where(PayrollRun.Company().equalTo(taxCompanyService.getCompany())
                                                                                                                    .And(PayrollRun.CreatorId().equalTo(SystemPrincipal.QBDTAdapter.getId()))));
        if(latestPaycheckDate != null){
            TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) taxCompanyService;
            taxCompanyServiceInfo.setLastPayrollDate(latestPaycheckDate);
            Application.save(taxCompanyServiceInfo);
        }

        RAFEnrollment rafEnrollment = taxCompanyService.getCompany().getCurrentRAFEnrollment();
        if (rafEnrollment!=null && RAFEnrollment.shouldCancelOnServiceDeactivate(rafEnrollment.getStatus())) {
            ProcessResult updateRAFEnrollmentResult =
                    PayrollServices.companyManager.updateRAFEnrollmentStatus(taxCompanyService.getCompany().getSourceSystemCd(),
                            taxCompanyService.getCompany().getSourceCompanyId(), rafEnrollment,
                            RAFEnrollmentStatus.Cancelled);
            processResult.merge(updateRAFEnrollmentResult);
        }

        return processResult;
    }




}