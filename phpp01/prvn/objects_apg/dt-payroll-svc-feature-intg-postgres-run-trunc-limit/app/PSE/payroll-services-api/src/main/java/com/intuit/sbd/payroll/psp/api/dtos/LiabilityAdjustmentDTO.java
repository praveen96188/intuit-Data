/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/LiabilityAdjustmentDTO.java#1 $
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
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;

public class LiabilityAdjustmentDTO {
    private String lawId;
    private String payrollItemId;
    private String sourceEmployeeId;
    private SpcfMoney amount;
    private DateDTO effectiveDate;
    private SpcfMoney taxableWages;
    private SpcfMoney totalWages;
    private boolean isReconcilingAdjustment;
    private QBDTTransactionInfoDTO QBDTTransactionInfoDTO;
    private Collection<PayItemDTO> mPayItemDTOs;

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public String getPayrollItemId() {
        return payrollItemId;
    }

    public void setPayrollItemId(String pPayrollItemId) {
        payrollItemId = pPayrollItemId;
    }

    public SpcfMoney getAmount() {
        return amount;
    }

    public void setAmount(SpcfMoney amount) {
        this.amount = amount;
    }

    public DateDTO getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(DateDTO effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public SpcfMoney getTaxableWages() {
        return taxableWages;
    }

    public void setTaxableWages(SpcfMoney pTaxableWages) {
        taxableWages = pTaxableWages;
    }

    public SpcfMoney getTotalWages() {
        return totalWages;
    }

    public void setTotalWages(SpcfMoney pTotalWages) {
        totalWages = pTotalWages;
    }

    public String getSourceEmployeeId() {
        return sourceEmployeeId;
    }

    public void setSourceEmployeeId(String pSourceEmployeeId) {
        sourceEmployeeId = pSourceEmployeeId;
    }

    public boolean isReconcilingAdjustment() {
        return isReconcilingAdjustment;
    }

    public void setReconcilingAdjustment(boolean reconcilingAdjustment) {
        isReconcilingAdjustment = reconcilingAdjustment;
    }

    public QBDTTransactionInfoDTO getQBDTTransactionInfoDTO() {
        return QBDTTransactionInfoDTO;
    }

    public void setQBDTTransactionInfoDTO(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        QBDTTransactionInfoDTO = pQBDTTransactionInfoDTO;
    }

    public Collection<PayItemDTO> getPayItemDTOs() {
        if (mPayItemDTOs == null) {
            mPayItemDTOs = new ArrayList<PayItemDTO>();
        }
        return mPayItemDTOs;
    }

    public void setPayItemDTOs(Collection<PayItemDTO> pPayItemDTOs) {
        mPayItemDTOs = pPayItemDTOs;
    }

    public void addPayItemDTO(PayItemDTO pPayItemDTO) {
        getPayItemDTOs().add(pPayItemDTO);
    }


    /**
     * Validates a LiabilityAdjustment DTO
     *
     * @return
     */
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (effectiveDate != null) {
            validationResult.merge(effectiveDate.validate());
        }

        if (lawId == null || !Validator.isValidLength(lawId, 1, 50)) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.PayrollTax, "LawId", "LawId");
        }

        return validationResult;
    }
}