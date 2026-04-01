package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/14/12
 * Time: 5:27 PM
 */
@XmlRootElement()
@XmlType(name = "EmployeeInfo")
public class EmployeeInfo {
    private String fullName;
    private String firstName;
    private String lastName;
    private String ssn;
    private AddressDTO address;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String pFullName) {
        fullName = pFullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String pFirstName) {
        firstName = pFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        lastName = pLastName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String pSsn) {
        ssn = pSsn;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO pAddress) {
        address = pAddress;
    }
}
