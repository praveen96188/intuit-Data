package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/14/12
 * Time: 5:29 PM
 */
@XmlRootElement()
@XmlType(name = "EmployeeInfoRequest")
public class EmployeeInfoRequest extends PSPCompanyRequest {

    private String ssn;
    private String employeeName;

    @XmlElement(required = true)
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String pSsn) {
        ssn = pSsn;
    }

    @XmlElement(required = true)
    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String pEmployeeName) {
        employeeName = pEmployeeName;
    }
}
