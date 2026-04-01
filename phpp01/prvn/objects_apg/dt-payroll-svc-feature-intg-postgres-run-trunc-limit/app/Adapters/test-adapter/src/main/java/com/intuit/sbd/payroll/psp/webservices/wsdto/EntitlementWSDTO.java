package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 21, 2010
 * Time: 12:42:20 PM
 */
@XmlType(name = "Entitlement")
public class EntitlementWSDTO {
    private String mOrderNumber;
    private String mLicenseNumber;
    private String mCustomerId;
    private String mNextChargeDate;
    private String mPaymentMethodType;
    private String mCreditCardType;
    private String mCreditCardNumber;
    private String mCreditCardExpiration;
    private String mEntitlementOfferingCode;
    private String mContactEmail;
    private String mEntitlementState;
    private String mContactName;
    private String mSubscriptionNumber;
    private String mBillingZipCode;
    private String mCancellationReason;
    private String mAsset;
    private String mAssetItemNumber;
    private String mEdition;
    private String mNumberOfEmployees;
    private List<EntitlementUnitWSDTO> mEntitlementUnits;

    @XmlElement(name = "OrderNumber", required = false)
    public String getOrderNumber() {
        return mOrderNumber;
    }

    public void setOrderNumber(String pOrderNumber) {
        mOrderNumber = pOrderNumber;
    }

    @XmlElement(name = "LicenseNumber", required = false)
    public String getLicenseNumber() {
        return mLicenseNumber;
    }

    public void setLicenseNumber(String pLicenseNumber) {
        mLicenseNumber = pLicenseNumber;
    }

    @XmlElement(name = "CustomerId", required = false)
    public String getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(String pCustomerId) {
        mCustomerId = pCustomerId;
    }

    @XmlElement(name = "NextChargeDate", required = false)
    public String getNextChargeDate() {
        return mNextChargeDate;
    }

    public void setNextChargeDate(String pNextChargeDate) {
        mNextChargeDate = pNextChargeDate;
    }

    @XmlElement(name = "PaymentMethodType", required = false)
    public String getPaymentMethodType() {
        return mPaymentMethodType;
    }

    public void setPaymentMethodType(String pPaymentMethodType) {
        mPaymentMethodType = pPaymentMethodType;
    }

    @XmlElement(name = "CreditCardType", required = false)
    public String getCreditCardType() {
        return mCreditCardType;
    }

    public void setCreditCardType(String pCreditCardType) {
        mCreditCardType = pCreditCardType;
    }

    @XmlElement(name = "CreditCardNumber", required = false)
    public String getCreditCardNumber() {
        return mCreditCardNumber;
    }

    public void setCreditCardNumber(String pCreditCardNumber) {
        mCreditCardNumber = pCreditCardNumber;
    }

    @XmlElement(name = "CreditCardExpiration", required = false)
    public String getCreditCardExpiration() {
        return mCreditCardExpiration;
    }

    public void setCreditCardExpiration(String pCreditCardExpiration) {
        mCreditCardExpiration = pCreditCardExpiration;
    }

    @XmlElement(name = "EntitlementOfferingCode", required = false)
    public String getEntitlementOfferingCode() {
        return mEntitlementOfferingCode;
    }

    public void setEntitlementOfferingCode(String pEntitlementOfferingCode) {
        mEntitlementOfferingCode = pEntitlementOfferingCode;
    }

    @XmlElement(name = "ContactEmail", required = false)
    public String getContactEmail() {
        return mContactEmail;
    }

    public void setContactEmail(String pContactEmail) {
        mContactEmail = pContactEmail;
    }

    @XmlElement(name = "EntitlementState", required = false)
    public String getEntitlementState() {
        return mEntitlementState;
    }

    public void setEntitlementState(String pEntitlementState) {
        mEntitlementState = pEntitlementState;
    }

    @XmlElement(name = "ContactName", required = false)
    public String getContactName() {
        return mContactName;
    }

    public void setContactName(String pContactName) {
        mContactName = pContactName;
    }

    @XmlElement(name = "SubscriptionNumber", required = false)
    public String getSubscriptionNumber() {
        return mSubscriptionNumber;
    }

    public void setSubscriptionNumber(String pSubscriptionNumber) {
        mSubscriptionNumber = pSubscriptionNumber;
    }

    @XmlElement(name = "BillingZipCode", required = false)
    public String getBillingZipCode() {
        return mBillingZipCode;
    }

    public void setBillingZipCode(String pBillingZipCode) {
        mBillingZipCode = pBillingZipCode;
    }

    @XmlElement(name = "CancellationReason", required = false)
    public String getCancellationReason() {
        return mCancellationReason;
    }

    public void setCancellationReason(String pCancellationReason) {
        mCancellationReason = pCancellationReason;
    }

    @XmlElement(name = "Asset", required = false)
    public String getAsset() {
        return mAsset;
    }

    public void setAsset(String pAsset) {
        mAsset = pAsset;
    }

    @XmlElement(name = "Edition", required = false)
    public String getEdition() {
        return mEdition;
    }

    public void setEdition(String pEdition) {
        mEdition = pEdition;
    }

    @XmlElement(name = "NumberOfEmployees", required = false)
    public String getNumberOfEmployees() {
        return mNumberOfEmployees;
    }

    public void setNumberOfEmployees(String pNumberOfEmployees) {
        mNumberOfEmployees = pNumberOfEmployees;
    }

    @XmlElement(name = "EntitlementUnits", required = false)
    public List<EntitlementUnitWSDTO> getEntitlementUnits() {
        if(mEntitlementUnits == null) {
            mEntitlementUnits = new ArrayList<EntitlementUnitWSDTO>();
        }
        return mEntitlementUnits;
    }

    @XmlElement(name = "AssetItemNumber", required = false)
    public String getAssetItemNumber() {
        return mAssetItemNumber;
    }

    public void setAssetItemNumber(String pAssetItemNumber) {
        mAssetItemNumber = pAssetItemNumber;
    }
}
