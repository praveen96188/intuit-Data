/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CompanyPayrollItemDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.PayrollItemCode;
import com.intuit.sbd.payroll.psp.domain.PayrollItemStatus;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.util.ArrayList;
import java.util.List;

public class CompanyPayrollItemDTO {
    private String sourcePayrollItemId;
    private String sourcePayrollItemDescription;
    private PayrollItemCode payrollItemCode;
    private PayrollItemStatus payrollItemStatus;
    private String mTaxFormLine;
    private Integer mW2Code;
    private QBDTPayrollItemInfoDTO QBDTPayrollItemInfoDTO;
    private boolean mArchived = false;

    private List<String> taxableToCompanyLawIds;

    public Integer getW2Code() {
        return mW2Code;
    }

    public void setW2Code(Integer pW2Code) {
        mW2Code = pW2Code;
    }

    public String getSourcePayrollItemId() {
        return sourcePayrollItemId;
    }

    public void setSourcePayrollItemId(String sourcePayrollItemId) {
        this.sourcePayrollItemId = sourcePayrollItemId;
    }

    public PayrollItemCode getPayrollItemCode() {
        return payrollItemCode;
    }

    public void setPayrollItemCode(PayrollItemCode payrollItemCode) {
        this.payrollItemCode = payrollItemCode;
    }

    public String getSourcePayrollItemDescription() {
        return sourcePayrollItemDescription;
    }

    public void setSourcePayrollItemDescription(String sourcePayrollItemDescription) {
        this.sourcePayrollItemDescription = sourcePayrollItemDescription;
    }

    public PayrollItemStatus getPayrollItemStatus() {
        return payrollItemStatus;
    }

    public void setPayrollItemStatus(PayrollItemStatus pPayrollItemStatus) {
        payrollItemStatus = pPayrollItemStatus;
    }

    public String getTaxFormLine() {
        return mTaxFormLine;
    }

    public void setTaxFormLine(String pTaxFormLine) {
        mTaxFormLine = pTaxFormLine;
    }

    public QBDTPayrollItemInfoDTO getQBDTPayrollItemInfoDTO() {
        return QBDTPayrollItemInfoDTO;
    }

    public void setQBDTPayrollItemInfoDTO(QBDTPayrollItemInfoDTO pQBDTPayrollItemInfoDTO) {
        QBDTPayrollItemInfoDTO = pQBDTPayrollItemInfoDTO;
    }

    public boolean isArchived() {
        return mArchived;
    }

    public void setArchived(boolean pArchived) {
        mArchived = pArchived;
    }

    public List<String> getTaxableToCompanyLawIds() {
        if(taxableToCompanyLawIds == null) {
            taxableToCompanyLawIds = new ArrayList<String>();
        }
        return taxableToCompanyLawIds;
    }

    public void setTaxableToCompanyLawIds(List<String> pTaxableToLaws) {
        taxableToCompanyLawIds = pTaxableToLaws;
    }

    /**
     * Validates a Paycheck DTO
     *
     * @return
     */
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (sourcePayrollItemId == null || !Validator.isValidLength(sourcePayrollItemId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollItem, sourcePayrollItemId, "sourcePayrollItemId");
            return validationResult;
        }

        if (payrollItemCode == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollItem, null, "payrollItemCode");
            return validationResult;
        }

        return validationResult;
    }
}
