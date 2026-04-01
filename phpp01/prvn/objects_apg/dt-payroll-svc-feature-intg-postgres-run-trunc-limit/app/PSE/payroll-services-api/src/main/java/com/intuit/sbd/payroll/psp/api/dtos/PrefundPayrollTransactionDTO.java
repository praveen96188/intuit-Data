package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.domain.OfferingServiceChargeType;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 31, 2009
 * Time: 2:14:24 PM
 */
public class PrefundPayrollTransactionDTO {
    private static final SpcfMoney ZERO = new SpcfMoney("0.00");

    private String originalTransactionId; 
    private SpcfMoney transactionAmount;
    private String originalTaxTransactionId;
    private SpcfMoney taxTransactionAmount;

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public SpcfMoney getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(SpcfMoney transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getOriginalTaxTransactionId() {
        return originalTaxTransactionId;
    }

    public void setOriginalTaxTransactionId(String originalTaxTransactionId) {
        this.originalTaxTransactionId = originalTaxTransactionId;
    }

    public SpcfMoney getTaxTransactionAmount() {
        return taxTransactionAmount;
    }

    public void setTaxTransactionAmount(SpcfMoney taxTransactionAmount) {
        this.taxTransactionAmount = taxTransactionAmount;
    }

    public boolean validateTransaction(ProcessResult validationResult) {
        // check the transaction amount
        if(this.getTransactionAmount() == null){
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "amount");
            return false;
        }
        else if(this.getTransactionAmount().compareTo(ZERO) < 0) {
            validationResult.getMessages().AmountPositiveForNonACHTransactions(EntityName.FinancialTransaction, this.getTransactionAmount().toString());
            return false;
        }        
        return true;
    }

    public boolean validateTaxTransction(ProcessResult validationResult) {
        // check related tax transaction amount if there is one
        if(this.getOriginalTaxTransactionId() != null){
            if(this.getTaxTransactionAmount() == null){
                validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "amount");
                return false;
            }
            else if(this.getTaxTransactionAmount().compareTo(ZERO) < 0) {
                validationResult.getMessages().AmountPositiveForNonACHTransactions(EntityName.FinancialTransaction, this.getTransactionAmount().toString());
                return false;
            }
        }
        return true;
    }
}
