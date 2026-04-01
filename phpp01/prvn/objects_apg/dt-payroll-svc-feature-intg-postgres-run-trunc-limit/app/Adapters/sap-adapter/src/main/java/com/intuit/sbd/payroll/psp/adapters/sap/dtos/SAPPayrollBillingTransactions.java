package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 31, 2008
 * Time: 4:07:06 PM
 */
public class SAPPayrollBillingTransactions {
    private Date initiationDate;
    private String payrollRunId;
    private Date checkDate;

    private ArrayList<SAPBillingTransaction> ddTransactions;
    private SAPBillingTransaction taxTransaction;
    private ArrayList<SAPBillingTransaction> feeTransactions;
    private SAPBillingTransaction handlingFeeTransaction;
    private boolean isCustomer;

    public Date getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(Date initiationDate) {
        this.initiationDate = initiationDate;
    }    

    public String getPayrollRunId() {
        return payrollRunId;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public void setPayrollRunId(String payrollRunId) {
        this.payrollRunId = payrollRunId;
    }

    public ArrayList<SAPBillingTransaction> getDdTransactions() {
        return ddTransactions;
    }

    public void setDdTransactions(ArrayList<SAPBillingTransaction> ddTransactions) {
        this.ddTransactions = ddTransactions;
    }

    public SAPBillingTransaction getTaxTransaction() {
        return taxTransaction;
    }

    public void setTaxTransaction(SAPBillingTransaction taxTransaction) {
        this.taxTransaction = taxTransaction;
    }

    public ArrayList<SAPBillingTransaction> getFeeTransactions() {
        return feeTransactions;
    }

    public void setFeeTransactions(ArrayList<SAPBillingTransaction> feeTransactions) {
        this.feeTransactions = feeTransactions;
    }

    public boolean getIsCustomer() {
        return isCustomer;
    }

    public void setIsCustomer(boolean customer) {
        isCustomer = customer;
    }

    public SAPBillingTransaction getHandlingFeeTransaction() {
        return handlingFeeTransaction;
    }

    public void setHandlingFeeTransaction(SAPBillingTransaction pHandlingFeeTransaction) {
        handlingFeeTransaction = pHandlingFeeTransaction;
    }
}
