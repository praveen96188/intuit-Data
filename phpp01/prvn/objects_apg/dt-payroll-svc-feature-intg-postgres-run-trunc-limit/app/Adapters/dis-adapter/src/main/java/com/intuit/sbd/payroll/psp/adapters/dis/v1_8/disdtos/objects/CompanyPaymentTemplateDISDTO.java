package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)

@XmlType(name = "CompanyPaymentTemplateDISDTO", propOrder = {
        "paymentTemplateCd","paymentTemplateName","agencyName","agencyID","agencyTaxpayerId","companyLawItems","companyLawRatesDetails","registeredForACH",
        "currentDepositFrequency","futureDepositFrequency",
        "is944Filer","companyPaymentMethods"})
public class CompanyPaymentTemplateDISDTO {

    @XmlElement(name = "PaymentTemplateCd")
    private String paymentTemplateCd;

    @XmlElement(name = "PaymentTemplateName")
    private String paymentTemplateName;

    @XmlElement(name = "AgencyName")
    private String agencyName;

    @XmlElement(name = "AgencyID")
    private String agencyID;

    @XmlElement(name = "AgencyTaxpayerId")
    private String agencyTaxpayerId;

    @XmlElement(name = "CompanyLawItem")
    private List<CompanyLawItemDISDTO> companyLawItems;

    @XmlElement(name = "CompanyLawRatesDetail")
    private List<CompanyLawRateDetailDISDTO> companyLawRatesDetails;

    @XmlElement(name = "RegisteredForACH")
    private Boolean registeredForACH;

    @XmlElement(name = "CurrentDepositFrequency")
    CompanyDepositFrequencyDISDTO currentDepositFrequency;

    @XmlElement(name = "FutureDepositFrequency")
    CompanyDepositFrequencyDISDTO futureDepositFrequency;

    @XmlElement(name = "Is944Filer")
    private Boolean is944Filer;

    @XmlElement(name = "CompanyPaymentMethod")
    private List<CompanyPaymentMethodDISDTO> companyPaymentMethods;

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }

    public String getPaymentTemplateName() {
        return paymentTemplateName;
    }

    public void setPaymentTemplateName(String paymentTemplateName) {
        this.paymentTemplateName = paymentTemplateName;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getAgencyID() {
        return agencyID;
    }

    public void setAgencyID(String agencyID) {
        this.agencyID = agencyID;
    }

    public String getAgencyTaxpayerId() {
        return agencyTaxpayerId;
    }

    public void setAgencyTaxpayerId(String agencyTaxpayerId) {
        this.agencyTaxpayerId = agencyTaxpayerId;
    }

    public List<CompanyLawItemDISDTO> getCompanyLawItems() {
        return companyLawItems;
    }

    public void setCompanyLawItems(List<CompanyLawItemDISDTO> companyLawItems) {
        this.companyLawItems = companyLawItems;
    }

    public List<CompanyLawRateDetailDISDTO> getCompanyLawRatesDetails() {
        return companyLawRatesDetails;
    }

    public void setCompanyLawRatesDetails(List<CompanyLawRateDetailDISDTO> companyLawRatesDetails) {
        this.companyLawRatesDetails = companyLawRatesDetails;
    }

    public Boolean getRegisteredForACH() {
        return registeredForACH;
    }

    public void setRegisteredForACH(Boolean registeredForACH) {
        this.registeredForACH = registeredForACH;
    }

    public CompanyDepositFrequencyDISDTO getCurrentDepositFrequency() {
        return currentDepositFrequency;
    }

    public void setCurrentDepositFrequency(CompanyDepositFrequencyDISDTO currentDepositFrequency) {
        this.currentDepositFrequency = currentDepositFrequency;
    }

    public CompanyDepositFrequencyDISDTO getFutureDepositFrequency() {
        return futureDepositFrequency;
    }

    public void setFutureDepositFrequency(CompanyDepositFrequencyDISDTO futureDepositFrequency) {
        this.futureDepositFrequency = futureDepositFrequency;
    }

    public Boolean getIs944Filer() {
        return is944Filer;
    }

    public void setIs944Filer(Boolean is944Filer) {
        this.is944Filer = is944Filer;
    }

    public List<CompanyPaymentMethodDISDTO> getCompanyPaymentMethods() {
        return companyPaymentMethods;
    }

    public void setCompanyPaymentMethods(List<CompanyPaymentMethodDISDTO> companyPaymentMethods) {
        this.companyPaymentMethods = companyPaymentMethods;
    }
}
