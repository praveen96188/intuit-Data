package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * User: vidhyak689
 * Date: 8/23/12
 * Time: 11:53 AM
 */
public class SAPUsageBillingDetail {
    private Date usagePeriodStartDate = null;
    private Date usagePeriodEndDate = null;
    private ArrayList<SAPUsageBillingEmployeeDetail> employeeDetails = null;
    private Integer numEmployeesBilled = 0;
    private Integer numCompaniesBilled  = 0;
    private Boolean isMultiEin = null;

    public Date getUsagePeriodStartDate() {
        return usagePeriodStartDate;
    }

    public void setUsagePeriodStartDate(Date pUsagePeriodStartDate) {
        usagePeriodStartDate = pUsagePeriodStartDate;
    }

    public Date getUsagePeriodEndDate() {
        return usagePeriodEndDate;
    }

    public void setUsagePeriodEndDate(Date pUsagePeriodEndDate) {
        usagePeriodEndDate = pUsagePeriodEndDate;
    }

    public Integer getNumEmployeesBilled() {
        return numEmployeesBilled;
    }

    public void setNumEmployeesBilled(Integer pNumEmployeesBilled) {
        numEmployeesBilled = pNumEmployeesBilled;
    }

    public Integer getNumCompaniesBilled() {
        return numCompaniesBilled;
    }

    public void setNumCompaniesBilled(Integer pNumCompaniesBilled) {
        numCompaniesBilled = pNumCompaniesBilled;
    }

    public ArrayList<SAPUsageBillingEmployeeDetail> getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(ArrayList<SAPUsageBillingEmployeeDetail> employeeInfo) {
        this.employeeDetails = employeeInfo;
    }

    public Boolean getIsMultiEin() {
        return isMultiEin;
    }

    public void setIsMultiEin(Boolean pMultiEin) {
        isMultiEin = pMultiEin;
    }
}
