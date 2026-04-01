package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Jun 24, 2010
 * Time: 3:41:08 PM
 */

@XmlAccessorType(XmlAccessType.FIELD)
public class SAPAddCompany {

    @XmlElement(name = "LicenseNumber", required = true)
    protected String licenseNumber;

    @XmlElement(name = "EOC", required = true)
    protected String eoc;

    @XmlElement(name = "ItemNumber", required = true)
    protected String itemNumber;

    @XmlElement(name = "ServiceAccountId", required = true)
    protected String serviceAccountId;

    @XmlElement(name = "PriceType", required = true)
    protected String priceType;

    @XmlElement(name = "Contacts", required = true)
    protected ArrayList<SAPContact> contacts;

    @XmlElement(name = "LegalInfo", required = true)
    protected SAPCompanyLegalInfo legalInfo;

    @XmlElement(name = "OfferCode", required = true)
    protected String offerCode;


    @XmlElement(name = "oldEIN", required = true)
    protected String oldEIN;

    @XmlElement(name = "einEffectiveDate", required = true)
    protected Date einEffectiveDate;

    @XmlElement(name = "isSuccessorEntityChange", required = true)
    protected Boolean isSuccessorEntityChange = false;

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getEoc() {
        return eoc;
    }

    public void setEoc(String eoc) {
        this.eoc = eoc;
    }

    public SAPCompanyLegalInfo getLegalInfo() {
        return legalInfo;
    }

    public void setLegalInfo(SAPCompanyLegalInfo legalInfo) {
        this.legalInfo = legalInfo;
    }

    public ArrayList<SAPContact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<SAPContact> contacts) {
        this.contacts = contacts;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getServiceAccountId() {
        return serviceAccountId;
    }

    public void setServiceAccountId(String serviceAccountId) {
        this.serviceAccountId = serviceAccountId;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public String getOldEIN() {
        return oldEIN;
    }

    public void setOldEIN(String pOldEIN) {
        oldEIN = pOldEIN;
    }

    public Date getEinEffectiveDate() {
        return einEffectiveDate;
    }

    public void setEinEffectiveDate(Date pEinEffectiveDate) {
        einEffectiveDate = pEinEffectiveDate;
    }

    public Boolean getIsSuccessorEntityChange() {
        return isSuccessorEntityChange;
    }

    public void setIsSuccessorEntityChange(Boolean pSuccessorEntityChange) {
        isSuccessorEntityChange = pSuccessorEntityChange;
    }
}
