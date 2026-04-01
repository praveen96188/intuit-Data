package com.intuit.sbd.payroll.psp.adapters.brm.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 9/18/12
 * Time: 2:35 PM
 * To change this template use File | Settings | File Templates.
 */
@XmlType(name = "EmployeeDetailInfo")
public class UsageBillingEmployeeDetail {
    private Date paycheckDate = null;
    private String checkNumber = null;
    private String employeeName = null;
    private String companyName = null;
    private String ein = null;

    @XmlElement(name = "PaycheckDate")
    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date pPaycheckDate) {
        paycheckDate = pPaycheckDate;
    }

    @XmlElement(name = "CheckNumber")
    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String pCheckNumber) {
        checkNumber = pCheckNumber;
    }

    @XmlElement(name = "EmployeeName")
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String pEmployeeName) {
        employeeName = pEmployeeName;
    }

    @XmlElement(name = "CompanyName")
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String pCompanyName) {
        companyName = pCompanyName;
    }

    @XmlElement(name = "EIN")
    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }
}
