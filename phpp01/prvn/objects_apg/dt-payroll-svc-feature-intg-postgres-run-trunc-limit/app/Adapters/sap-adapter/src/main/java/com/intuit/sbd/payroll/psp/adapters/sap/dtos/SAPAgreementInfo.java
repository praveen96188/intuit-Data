package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2008
 * Time: 1:45:07 PM
 */
@Deprecated
public class SAPAgreementInfo {

	private String name;
    private String subscriptionNumber;
    private String serviceKey;
	private String serviceType;
	private String agreementSubType;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAgreementSubType() {
        return agreementSubType;
    }

    public void setAgreementSubType(String agreementSubType) {
        this.agreementSubType = agreementSubType;
    }
}
