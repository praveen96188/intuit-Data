/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/AddService401k.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kPaycheck;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

public class AddService401k implements IProcess {
    private ThirdParty401kCompanyServiceInfo thirdParty401kCompanyService;
    private ThirdParty401kServiceInfoDTO dto401kServiceInfo;

    public AddService401k(ThirdParty401kCompanyServiceInfo pDomainCompanyService, ThirdParty401kServiceInfoDTO pDTO401kServiceInfo) {
        thirdParty401kCompanyService = pDomainCompanyService;
        dto401kServiceInfo = pDTO401kServiceInfo;
    }

    public ProcessResult execute() {
        ProcessResult processResult = new ProcessResult();

        thirdParty401kCompanyService.setCustodialId(dto401kServiceInfo.getCustodialId());
        thirdParty401kCompanyService.setServiceStartDate(dto401kServiceInfo.getServiceStartDate());
        thirdParty401kCompanyService.setHasSafeHarbor(dto401kServiceInfo.hasSafeHarbor());

        if (!thirdParty401kCompanyService.getCompany().hasService(ServiceCode.ThirdParty401k)) {
            thirdParty401kCompanyService.getCompany().addCompanyService(thirdParty401kCompanyService);
        }

        Expression<Paycheck> query = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().Company().equalTo(thirdParty401kCompanyService.getCompany())
                        .And(Paycheck.SourceEmployee().isNotNull())
                        .And(Paycheck.PayrollRun().PaycheckDate().greaterOrEqualThan(thirdParty401kCompanyService.getServiceStartDate())))
                .EagerLoad(Paycheck.PayrollRun());
        DomainEntitySet<Paycheck> paychecks = PayrollServices.entityFinder.find(Paycheck.class, query);
        for (Paycheck paycheck : paychecks) {
            ThirdParty401kPaycheck.addTP401K(paycheck);
        }

        return processResult;
    }
}
