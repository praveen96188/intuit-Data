package com.intuit.sbd.payroll.psp.adapters.brm.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 9/17/12
 * Time: 3:29 PM
 * To change this template use File | Settings | File Templates.
 */

@XmlRootElement()
@XmlType(name = "GetUsageBillingDetailResponse")
public class GetUsageBillingDetailResponse {
    private Date usagePeriodEndDate = null;
    private Date usagePeriodStartDate = null;
    private ArrayList<UsageBillingEmployeeDetail> employeeDetails = null;
    private Integer numEmployeesBilled = 0;
    private Integer numCompaniesBilled = 0;
    private Boolean isMultiEin = null;
    private ResponseStatus status = null;

   @XmlElement(name = "Status", nillable = false, required = true)
    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus pStatus) {
        status = pStatus;
    }

    @XmlElement(name = "UsagePeriodEndDate")
    public Date getUsagePeriodEndDate() {
        return usagePeriodEndDate;
    }

    public void setUsagePeriodEndDate(Date pUsagePeriodEndDate) {
        usagePeriodEndDate = pUsagePeriodEndDate;
    }

    @XmlElementWrapper(name = "EmployeeDetails")
    @XmlElement(name = "EmployeeDetailInfo")
    public ArrayList<UsageBillingEmployeeDetail> getEmployeeDetails() {
        return employeeDetails;
    }

    public void setEmployeeDetails(ArrayList<UsageBillingEmployeeDetail> pEmployeeDetails) {
        employeeDetails = pEmployeeDetails;
    }

    @XmlElement(name = "NumEmployeesBilled")
    public Integer getNumEmployeesBilled() {
        return numEmployeesBilled;
    }

    public void setNumEmployeesBilled(Integer pNumEmployeesBilled) {
        numEmployeesBilled = pNumEmployeesBilled;
    }

    @XmlElement(name = "NumCompaniesBilled")
    public Integer getNumCompaniesBilledBilled() {
        return numCompaniesBilled;
    }

    public void setNumCompaniesBilled(Integer pNumCompaniesBilled) {
        numCompaniesBilled = pNumCompaniesBilled;
    }

    @XmlElement(name = "IsMultiEin")
    public Boolean getIsMultiEin() {
        return isMultiEin;
    }

    public void setIsMultiEin(Boolean pMultiEin) {
        isMultiEin = pMultiEin;
    }

    @XmlElement(name = "UsagePeriodStartDate")
    public Date getUsagePeriodStartDate() {
        return usagePeriodStartDate;
    }

    public void setUsagePeriodStartDate(Date pUsagePeriodStartDate) {
        usagePeriodStartDate = pUsagePeriodStartDate;
    }
}
