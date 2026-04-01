package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: vidhyak689
 * Date: 8/23/12
 * Time: 12:03 PM
 */
public class SAPUsageBillingEmployeeDetail {
    private Date paycheckDate = null;
    private String checkNumber = null;
    private String employeeName = null;
    private String companyName = null;
    private String ein = null;


    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date paycheckDate) {
        this.paycheckDate = paycheckDate;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String pCompanyName) {
        companyName = pCompanyName;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }
}
