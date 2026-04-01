/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/SourcePayrollParameterDTO.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameterCode;

/**
 * @author Ken Paul
 */
public class SourcePayrollParameterDTO {
    private SourceSystemCode sourceSystemCd;
    private String name;
    private String description;
    private SourcePayrollParameterCode parameterCd;
    private String parameterValue;

    /**
     * The update process does not use any parameters beyond these 3.
     * @param sourceSystemCd
     * @param parameterCd
     * @param parameterValue
     */
    public SourcePayrollParameterDTO(SourceSystemCode sourceSystemCd, SourcePayrollParameterCode parameterCd, String parameterValue) {
        this.sourceSystemCd = sourceSystemCd;
        this.parameterCd = parameterCd;
        this.parameterValue = parameterValue;
    }

    public SourcePayrollParameterDTO(SourceSystemCode pSourceSystemCd,
                                     String pName,
                                     String pDescription,
                                     SourcePayrollParameterCode pParameterCd,
                                     String pParameterValue) {
        this.sourceSystemCd = pSourceSystemCd;
        this.name = pName;
        this.description = pDescription;
        this.parameterCd = pParameterCd;
        this.parameterValue = pParameterValue;
    }

    public SourceSystemCode getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(SourceSystemCode pSourceSystemCd) {
        this.sourceSystemCd = pSourceSystemCd;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        this.name = pName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        this.description = pDescription;
    }

    public SourcePayrollParameterCode getParameterCd() {
        return parameterCd;
    }

    public void setParameterCd(SourcePayrollParameterCode pParameterCd) {
        this.parameterCd = pParameterCd;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String pParameterValue) {
        this.parameterValue = pParameterValue;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (parameterValue == null || !Validator.isValidLength(parameterValue, 1, 99)) {
            // Note: setting "blank" is required for backward compatibility with existing QBOE error message
            validationResult.getMessages().InvalidArgument(
                    EntityName.SourcePayrollParameter, sourceSystemCd.toString(), "blank");
        }

        if (parameterCd == null) {
            validationResult.getMessages().SourcePayrollParameterDoesNotExist(
                    EntityName.SourcePayrollParameter, sourceSystemCd.toString(), sourceSystemCd.toString(), null);
        }

        return validationResult;
    }
}
