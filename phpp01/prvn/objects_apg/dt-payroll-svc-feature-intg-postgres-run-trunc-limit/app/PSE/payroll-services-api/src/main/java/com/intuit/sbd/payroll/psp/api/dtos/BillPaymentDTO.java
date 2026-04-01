/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/BillPaymentDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.BillPaymentTransactionType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;

public class BillPaymentDTO {
    private String billPaymentId;
    private PayeeDTO payeeDTO;
    private Collection<BillPaymentSplitDTO> paymentTransactions;
    private SpcfMoney amount;
    private DateDTO depositDate;
    private String memo;
    private BillPaymentTransactionType transactionType;
    private String sessionId;

    public String getBillPaymentId() {
        return billPaymentId;
    }

    public void setBillPaymentId(String pPaycheckId) {
        this.billPaymentId = pPaycheckId;
    }


    public Collection<BillPaymentSplitDTO> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(Collection<BillPaymentSplitDTO> pDDTransactions) {
        this.paymentTransactions = pDDTransactions;
    }


    public SpcfMoney getAmount() {
        return amount;
    }

    public void setAmount(SpcfMoney amount) {
        this.amount = amount;
    }

    public DateDTO getDepositDate() {
        return depositDate;
    }

    public void setDepositDate(DateDTO depositDate) {
        this.depositDate = depositDate;
    }

    public PayeeDTO getPayeeDTO() {
        return payeeDTO;
    }

    public void setPayeeDTO(PayeeDTO payeeDTO) {
        this.payeeDTO = payeeDTO;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public BillPaymentTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(BillPaymentTransactionType transactionType) {
        this.transactionType = transactionType;
    }
    

    public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
     * Validates a Paycheck DTO
     *
     * @return
     */
    public ProcessResult validateBillPaymentDTO() {
        ProcessResult validationResult = new ProcessResult();

        if (billPaymentId == null || !Validator.isValidLength(billPaymentId, 1, 50)) {
            validationResult.getMessages().InvalidValue(EntityName.BillPayment, null, "BillPaymentId");
            return validationResult;
        }

        if (payeeDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.BillPayment, billPaymentId, "PayeeDTO");
        } else {
            validationResult.merge(payeeDTO.validate());
        }

        if (amount == null || SpcfUtils.convertToBigDecimal(amount).scale() > 2) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.BillPayment, billPaymentId, "BillPaymentAmount");
        }


        if (depositDate == null) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.BillPayment, billPaymentId, "DepositDate");
        } else {
            validationResult.merge(depositDate.validate());
        }


        if (!(amount.compareTo(new SpcfMoney("0.00")) > 0)) {
            validationResult.getMessages()
                    .InvalidPaymentAmount(EntityName.DTO, billPaymentId, payeeDTO.getName(), "$" + amount.toString());
            return validationResult;
        }

        for (BillPaymentSplitDTO currPaymentSplitDTO : paymentTransactions) {
            validationResult.merge(currPaymentSplitDTO.validateBillPaymentSplitDTO());
        }

        // truncate to 4000 characters
        // QB allows up to 4096 chars.
        if (memo!= null && memo.length() > 4000) {
            memo = memo.substring(0, 4000);
        }

        return validationResult;
    }
}