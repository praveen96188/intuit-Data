/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CheckDistributionServiceInfoDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.OfferingCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

public class CheckDistributionServiceInfoDTO extends ServiceInfoDTO {

    private long lastPaycheckId;

    public CheckDistributionServiceInfoDTO() {
        super.setServiceCode(ServiceCode.CheckDistribution);
        super.setOfferingCode(OfferingCode.CheckDistribution);
    }


    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        //todo length checking
        if (lastPaycheckId < 0) {
            validationResult.getMessages().InvalidValue(EntityName.CompanyService, String.valueOf(lastPaycheckId),
                    "LastPaycheckId");
        }

        return validationResult;
    }

    public long getLastPaycheckId() {
        return lastPaycheckId;
    }

    public void setLastPaycheckId(long lastPaycheckId) {
        this.lastPaycheckId = lastPaycheckId;
    }
}