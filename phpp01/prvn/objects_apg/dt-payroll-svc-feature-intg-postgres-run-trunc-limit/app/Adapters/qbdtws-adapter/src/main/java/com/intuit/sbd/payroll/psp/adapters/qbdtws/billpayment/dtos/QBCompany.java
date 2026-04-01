package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "QBCompany")
public class QBCompany {

    private String clientApplicationName;
    private String clientApplicationVersion;
    private String companyLegalName;    
    private String vendorGatewayVersion;
    private QBAddress legalAddress;

    @XmlElement(name = "ClientApplicationName", required = true, nillable = false)
    public String getClientApplicationName() {
        return clientApplicationName;
    }

    public void setClientApplicationName(String pClientApplicationName) {
        clientApplicationName = pClientApplicationName;
    }

    @XmlElement(name = "ClientApplicationVersion", required = true, nillable = false)
    public String getClientApplicationVersion() {
        return clientApplicationVersion;
    }

    public void setClientApplicationVersion(String pClientApplicationVersion) {
        clientApplicationVersion = pClientApplicationVersion;
    }

    @XmlElement(name = "CompanyLegalName")
    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String pCompanyLegalName) {
        companyLegalName = pCompanyLegalName;
    }

    @XmlElement(name = "VendorGatewayVersion")
    public String getVendorGatewayVersion() {
        return vendorGatewayVersion;
    }

    public void setVendorGatewayVersion(String pVendorGatewayVersion) {
        vendorGatewayVersion = pVendorGatewayVersion;
    }

    @XmlElement(name = "LegalAddress")
    public QBAddress getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(QBAddress pLegalAddress) {
        legalAddress = pLegalAddress;
    }
}
