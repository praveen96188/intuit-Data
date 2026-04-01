package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author: Sriram Nutakki
 * Date created: 8/19/13
 */
@XmlRootElement(name = "Company")
public class CompanyDTO {

    private String psid;
    private String ein;
    private String companyLegalName;
    private String companyName;
    private String phone;
    private String email;

    private ContactDTO contact;
    private AddressDTO address;
    private String shipAddress;
    private List<EntitlementDTO> entitlements;
    private java.util.Date mLastPayrollRunDate;
    private java.util.Date mLastPaycheckDate;

    public CompanyDTO() {
        setEntitlements(new ArrayList<EntitlementDTO>());
    }

    @XmlElement(name="Contact")
    public ContactDTO getContact() {
        return contact;
    }

    public void setContact(ContactDTO pContact) {
        contact = pContact;
    }

    @XmlElement(name="Entitlement")
    public List<EntitlementDTO> getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(List<EntitlementDTO> pEntitlements) {
        this.entitlements = pEntitlements;
    }

    public void addEntitlement(EntitlementDTO pEntitlement) {
        entitlements.add(pEntitlement);
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String pCompanyLegalName) {
        companyLegalName = pCompanyLegalName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String pCompanyName) {
        companyName = pCompanyName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String pPhone) {
        phone = pPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }

    @XmlElement(name="Address")
    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO pAddress) {
        address = pAddress;
    }

    public String getShipAddress() {
        return shipAddress;
    }

    public void setShipAddress(String pShipAddress) {
        shipAddress = pShipAddress;
    }


    public String getPsid() {
        return psid;
    }

    public void setPsid(String pPsid) {
        psid = pPsid;
    }

    @XmlElement(name="lastPayrollRunDate")
    public Date getLastPayrollRunDate() {
       return mLastPayrollRunDate;
    }

    public void setLastPayrollRunDate(Date pLastPayrollDate) {
        mLastPayrollRunDate = pLastPayrollDate;
    }
    @XmlElement(name="lastPaycheckDate")
    public Date getLastPaycheckDate() {
        return mLastPaycheckDate;
    }

    public void setLastPaycheckDate(Date pLastPaycheckDate) {
        mLastPaycheckDate = pLastPaycheckDate;
    }
}
