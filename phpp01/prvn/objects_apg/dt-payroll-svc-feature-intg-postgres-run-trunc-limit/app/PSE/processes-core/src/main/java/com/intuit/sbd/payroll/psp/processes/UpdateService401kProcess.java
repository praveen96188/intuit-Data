/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdateService401kProcess.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.ThirdParty401kCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;

public class UpdateService401kProcess implements IProcess {
    private ThirdParty401kCompanyServiceInfo thirdParty401kCompanyService;
    private ThirdParty401kServiceInfoDTO dto401kServiceInfo;

    public UpdateService401kProcess(ThirdParty401kCompanyServiceInfo pDomainCompanyService, ThirdParty401kServiceInfoDTO pDTO401kServiceInfo) {
        thirdParty401kCompanyService = pDomainCompanyService;
        dto401kServiceInfo = pDTO401kServiceInfo;
    }


    public ProcessResult execute() {
        ProcessResult processResult = new ProcessResult();

        thirdParty401kCompanyService.setCustodialId(dto401kServiceInfo.getCustodialId());
        thirdParty401kCompanyService.setServiceStartDate(dto401kServiceInfo.getServiceStartDate());
        thirdParty401kCompanyService.setHasSafeHarbor(dto401kServiceInfo.hasSafeHarbor());

        return processResult;
    }
}