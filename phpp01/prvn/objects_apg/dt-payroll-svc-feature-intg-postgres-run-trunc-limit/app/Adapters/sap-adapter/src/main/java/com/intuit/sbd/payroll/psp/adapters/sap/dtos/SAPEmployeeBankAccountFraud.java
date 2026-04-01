package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: dcrossley
 * Date: Jul 30, 2009
 * Time: 10:35:23 AM
 */
public class SAPEmployeeBankAccountFraud {

    private String bankName;
    private String bankAccountNumber;
    private ArrayList<SAPEmployeeInfo> employeeInfo;

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public ArrayList<SAPEmployeeInfo> getEmployeeInfo() {
        return employeeInfo;
    }

    public void setEmployeeInfo(ArrayList<SAPEmployeeInfo> employeeInfo) {
        this.employeeInfo = employeeInfo;
    }
}
