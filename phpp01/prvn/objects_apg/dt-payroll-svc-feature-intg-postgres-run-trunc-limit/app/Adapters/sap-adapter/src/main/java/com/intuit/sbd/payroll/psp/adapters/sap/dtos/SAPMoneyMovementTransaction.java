package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 25, 2009
 * Time: 12:34:02 PM
 */
public class SAPMoneyMovementTransaction {
    private String spcfId;
    private String achReason;
    private Date checkDate;
    private Date settlementDate;
    private Date creationDate;
    private double achAmount;
    private SAPCompanyBankAccount bankAccount;
    private boolean showDetail;
    private boolean isHPDE;

    public String getAchReason() {
        return achReason;
    }

    public void setAchReason(String achReason) {
        this.achReason = achReason;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(Date settlementDate) {
        this.settlementDate = settlementDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public double getAchAmount() {
        return achAmount;
    }

    public void setAchAmount(double achAmount) {
        this.achAmount = achAmount;
    }

    public SAPCompanyBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(SAPCompanyBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getSpcfId() {
        return spcfId;
    }

    public void setSpcfId(String spcfId) {
        this.spcfId = spcfId;
    }

    public boolean getShowDetail() {
        return showDetail;
    }

    public void setShowDetail(boolean showDetail) {
        this.showDetail = showDetail;
    }

    public boolean getHPDE() {
        return isHPDE;
    }

    public void setHPDE(boolean HPDE) {
        isHPDE = HPDE;
    }
}
