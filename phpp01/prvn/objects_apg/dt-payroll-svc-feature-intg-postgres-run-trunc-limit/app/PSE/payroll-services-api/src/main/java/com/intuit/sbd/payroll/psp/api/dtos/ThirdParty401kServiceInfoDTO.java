/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/ThirdParty401kServiceInfoDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;

import java.math.BigDecimal;

public class ThirdParty401kServiceInfoDTO extends ServiceInfoDTO {
    private String custodialId;
    private Boolean hasSafeHarbor = false;

    public ThirdParty401kServiceInfoDTO() {
        super.setServiceCode(ServiceCode.ThirdParty401k);
    }

    public String getCustodialId() {
        return custodialId;
    }

    public void setCustodialId(String custodialId) {
        this.custodialId = custodialId;
    }

    public Boolean hasSafeHarbor() {
        return hasSafeHarbor;
    }

    public void setHasSafeHarbor(Boolean hasSafeHarbor) {
        this.hasSafeHarbor = hasSafeHarbor;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        //todo length checking
        if (custodialId == null) {
            validationResult.getMessages().InvalidValue(EntityName.ThirdParty401k, null,
                    "CustodialId");
        }

        return validationResult;
    }
}