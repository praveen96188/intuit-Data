package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 21, 2008
 * Time: 4:10:28 PM
 */
public class SAPChaseReport {
    private Date connectionDate;
    private Date postingDate;
    private String companyName;
    private String sourceSystem;
    private String bankName;
    private ArrayList<SAPChaseReportTransaction> transactions;

    public Date getConnectionDate() {
        return connectionDate;
    }

    public void setConnectionDate(Date connectionDate) {
        this.connectionDate = connectionDate;
    }

    public Date getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(Date postingDate) {
        this.postingDate = postingDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public ArrayList<SAPChaseReportTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<SAPChaseReportTransaction> transactions) {
        this.transactions = transactions;
    }
}
