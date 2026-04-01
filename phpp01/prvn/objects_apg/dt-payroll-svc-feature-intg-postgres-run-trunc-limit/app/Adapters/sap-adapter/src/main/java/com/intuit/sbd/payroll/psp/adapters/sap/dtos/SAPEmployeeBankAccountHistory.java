package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: cyoder
 * Date: Jun 17, 2008
 * Time: 12:25:46 PM
 */
public class SAPEmployeeBankAccountHistory {
    private String accountId;
    private ArrayList<SAPEmployeeBankAccountHistoryItem> employeeBankAccountHistoryItems = new ArrayList<SAPEmployeeBankAccountHistoryItem>();

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public ArrayList<SAPEmployeeBankAccountHistoryItem> getEmployeeBankAccountHistoryItems() {
        return employeeBankAccountHistoryItems;
    }

    public void setEmployeeBankAccountHistoryItems(ArrayList<SAPEmployeeBankAccountHistoryItem> employeeBankAccountHistoryItems) {
        this.employeeBankAccountHistoryItems = employeeBankAccountHistoryItems;
    }
}
