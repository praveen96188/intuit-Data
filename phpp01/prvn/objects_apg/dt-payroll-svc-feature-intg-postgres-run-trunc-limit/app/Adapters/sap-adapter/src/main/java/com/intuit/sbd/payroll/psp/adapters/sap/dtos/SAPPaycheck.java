package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 16, 2009
 * Time: 3:17:31 PM
 */
public class SAPPaycheck {
    private String paycheckGseq;
    private String sourcePaycheckId;
    private Date payPeriodBeginDate;
    private Date payPeriodEndDate;
    private Date paycheckDate;
    private Date voidedDate;
    private boolean voidedAfterOffload;

    //dd ee name
    private String employeeName;
    //source ee name
    private String sourceEmployeeName;
    private double netPaycheckAmount;
    private String status;

    private SAPPaycheck401k paycheck401k;



    public String getSourcePaycheckId() {
        return sourcePaycheckId;
    }

    public void setSourcePaycheckId(String sourcePaycheckId) {
        this.sourcePaycheckId = sourcePaycheckId;
    }

    public Date getPayPeriodBeginDate() {
        return payPeriodBeginDate;
    }

    public void setPayPeriodBeginDate(Date payPeriodBeginDate) {
        this.payPeriodBeginDate = payPeriodBeginDate;
    }

    public Date getPayPeriodEndDate() {
        return payPeriodEndDate;
    }

    public void setPayPeriodEndDate(Date payPeriodEndDate) {
        this.payPeriodEndDate = payPeriodEndDate;
    }

    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date paycheckDate) {
        this.paycheckDate = paycheckDate;
    }

    public Date getVoidedDate() {
        return voidedDate;
    }

    public void setVoidedDate(Date voidedDate) {
        this.voidedDate = voidedDate;
    }

    public boolean isVoidedAfterOffload() {
        return voidedAfterOffload;
    }

    public void setVoidedAfterOffload(boolean voidedAfterOffload) {
        this.voidedAfterOffload = voidedAfterOffload;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public double getNetPaycheckAmount() {
        return netPaycheckAmount;
    }

    public void setNetPaycheckAmount(double netPaycheckAmount) {
        this.netPaycheckAmount = netPaycheckAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaycheckGseq() {
        return paycheckGseq;
    }

    public void setPaycheckGseq(String paycheckGseq) {
        this.paycheckGseq = paycheckGseq;
    }

    public String getSourceEmployeeName() {
        return sourceEmployeeName;
    }

    public void setSourceEmployeeName(String sourceEmployeeName) {
        this.sourceEmployeeName = sourceEmployeeName;
    }

    public SAPPaycheck401k getPaycheck401k() {
        return paycheck401k;
    }

    public void setPaycheck401k(SAPPaycheck401k paycheck401k) {
        this.paycheck401k = paycheck401k;
    }
}
