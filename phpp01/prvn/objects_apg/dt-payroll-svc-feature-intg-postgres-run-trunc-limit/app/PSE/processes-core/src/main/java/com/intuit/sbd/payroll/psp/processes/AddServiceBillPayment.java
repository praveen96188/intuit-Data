/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddServiceBillPayment.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

public class AddServiceBillPayment extends Process implements IProcess {

    private Company domainCompany;

    private ServiceInfoDTO dtoService;
    private Service service;

    public AddServiceBillPayment(Company pCompany, ServiceInfoDTO pCompanyService) {
        domainCompany = pCompany;
        dtoService = pCompanyService;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        // BillPayment Service can only be activated if DD service is Active or PendingFirstPayroll
        service = Application.findById(Service.class, ServiceCode.DirectDeposit);
        CompanyService ddCompanyService = CompanyService.findCompanyService(domainCompany, service.getServiceCd());

        if (ddCompanyService != null) {
            if (ddCompanyService.getStatusCd().notIn(ServiceSubStatusCode.ActiveCurrent, ServiceSubStatusCode.PendingFirstPayroll)) {
                validationResult.getMessages()
                        .DDStatusNotValid(EntityName.CompanyService, service.getServiceCd().toString(), ddCompanyService.getStatusCd().toString());

            }
        } else {
            //Company does not have DD service
            validationResult.getMessages()
                    .CompanyNotSignedForDD(EntityName.CompanyService, service.getServiceCd().toString());
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        processResult.setSuccess(true);
        return processResult;
    }
}
