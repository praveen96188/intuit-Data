package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEinSubscriptionStatus;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "subscriptionStatus",
        "subscriptionEndDate",
        "subscriptionBillingInfo",
        "psid",
        "fundingModel",
        "companyLegalInfo",
        "qbAccountName",
        "subType",
        "entitlementCreationDate"
})
public class EwsValidateSubscriptionResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "SubscriptionStatus", required = true)
    protected EwsEinSubscriptionStatus subscriptionStatus;

    @XmlElement(name = "SubscriptionEndDate", required = false)
    protected Calendar subscriptionEndDate;

    @XmlElement(name = "SubscriptionBillingInfo", required = false)
    protected EwsSubscriptionBillingInfo subscriptionBillingInfo;

    @XmlElement(name = "PSID", required = false)
    protected String psid;

    @XmlElement(name = "FundingModel", required = false)
    protected String fundingModel;

    @XmlElement(name = "CompanyLegalInfo", required = false)
    protected EwsLegalInfo companyLegalInfo;

    @XmlElement(name = "QBAccountName", required = false)
    protected String qbAccountName;

    @XmlElement(name = "SubType", required = false)
    protected String subType;

    @XmlElement(name = "EntitlementCreationDate", required = false)
    protected Calendar entitlementCreationDate;


    public EwsValidateSubscriptionResponse() {
        super();
    }

    public EwsValidateSubscriptionResponse clone() throws CloneNotSupportedException {
        EwsValidateSubscriptionResponse clone = (EwsValidateSubscriptionResponse) super.clone();

        if (subscriptionBillingInfo != null) {
            clone.setSubscriptionBillingInfo(subscriptionBillingInfo.clone());
        }

        if (companyLegalInfo != null) {
            clone.setCompanyLegalInfo(companyLegalInfo.clone());
        }

        return clone;
    }

    public EwsEinSubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(EwsEinSubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public Calendar getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public void setSubscriptionEndDate(Calendar subscriptionEndDate) {
        this.subscriptionEndDate = subscriptionEndDate;
    }

    public EwsSubscriptionBillingInfo getSubscriptionBillingInfo() {
        return subscriptionBillingInfo;
    }

    public void setSubscriptionBillingInfo(EwsSubscriptionBillingInfo subscriptionBillingInfo) {
        this.subscriptionBillingInfo = subscriptionBillingInfo;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    public String getFundingModel() {
        return fundingModel;
    }

    public void setFundingModel(String fundingModel) {
        this.fundingModel = fundingModel;
    }

    public EwsLegalInfo getCompanyLegalInfo() {
        return companyLegalInfo;
    }

    public void setCompanyLegalInfo(EwsLegalInfo companyLegalInfo) {
        this.companyLegalInfo = companyLegalInfo;
    }

    public String getQbAccountName() {
        return qbAccountName;
    }

    public void setQbAccountName(String qbAccountName) {
        this.qbAccountName = qbAccountName;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public Calendar getEntitlementCreationDate() {
        return entitlementCreationDate;
    }

    public void setEntitlementCreationDate(Calendar pEntitlementCreationDate) {
        entitlementCreationDate = pEntitlementCreationDate;
    }
}
