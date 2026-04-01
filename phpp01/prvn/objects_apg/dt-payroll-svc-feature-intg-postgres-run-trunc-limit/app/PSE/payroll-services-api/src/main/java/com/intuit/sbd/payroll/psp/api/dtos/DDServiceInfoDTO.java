/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/DDServiceInfoDTO.java#1 $
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

public class DDServiceInfoDTO extends ServiceInfoDTO {
    private BigDecimal averagePayrollAmount;
    private BigDecimal highAnnualPayrollAmount;

    public DDServiceInfoDTO() {
        super.setServiceCode(ServiceCode.DirectDeposit);
    }

    public BigDecimal getAveragePayrollAmount() {
        return averagePayrollAmount;
    }

    public void setAveragePayrollAmount(BigDecimal pAveragePayrollAmount) {
        this.averagePayrollAmount = pAveragePayrollAmount;
    }

    public BigDecimal getHighAnnualPayrollAmount() {
        return highAnnualPayrollAmount;
    }

    public void setHighAnnualPayrollAmount(BigDecimal pHighAnnualPayrollAmount) {
        this.highAnnualPayrollAmount = pHighAnnualPayrollAmount;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        if (averagePayrollAmount != null && averagePayrollAmount.scale() >2) {
            validationResult.getMessages().InvalidValue(EntityName.DDServiceInfo, averagePayrollAmount.toString(),
                    "AveragePayrollAmount");
        }

        if (highAnnualPayrollAmount != null && highAnnualPayrollAmount.scale() >2) {
            validationResult.getMessages().InvalidValue(EntityName.DDServiceInfo, highAnnualPayrollAmount.toString(),
                    "HighAnnualPayrollAmount");
        }

        return validationResult;
    }
}
