package com.intuit.sbd.payroll.psp.gateways.amo;

import com.intuit.iep.abcsimpl.commoncomponents.v1.CCInfoType;
import com.intuit.iep.abcsimpl.commoncomponents.v1.EFTInfoType;
import com.intuit.iep.billingprofile.billingprofileabo.v1.BillingProfileType;
import com.intuit.iep.customeraccount.customeraccountbase.v1.*;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.*;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/16/13
 * Time: 11:28 AM
 */
public class GetCustomerAssetResponseTypeDTO {

    private static SpcfLogger logger = PayrollServices.getLogger(GetCustomerAssetResponseTypeDTO.class);

    private String mEntitlementOfferingCode;
    private String mLicenseNumber;
    private String mCustomerId;
    private String mBillingProfileId;
    private Integer mBillingDayOfMonth;
    private String mBillingZipCode;
    private String mContactName;
    private String mContactEmail;
    private EntitlementPaymentMethodType mPaymentMethodType;
    private String mCreditCardType;
    private String mCreditCardNumber;
    private String mCreditCardExpiration;
    private SpcfCalendar mNextChargeDate;
    private SpcfCalendar mSubscriptionEndDate;
    private SpcfCalendar mSubscriptionStartDate;
    private EntitlementStateCode mEntitlementState;
    private boolean isWalletEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.NGP_WALLET, false);

    public GetCustomerAssetResponseTypeDTO() {

    }

    public GetCustomerAssetResponseTypeDTO(IntuitCustomerAssetABOType pIntuitCustomerAssetABOType) throws Exception{

        if (pIntuitCustomerAssetABOType != null) {
            // find the corresponding profile ids for the asset
            String assetContactId = null;
            for (AssetType assetType : pIntuitCustomerAssetABOType.getAsset()) {
                if(assetType.getEntitlement() != null){
                    if(assetType.getStatus() == AssetStatusType.CANCELLED) {
                        setEntitlementState(EntitlementStateCode.Disabled);
                    } else if(assetType.getStatus() == AssetStatusType.ACTIVE) {
                        setEntitlementState(EntitlementStateCode.Enabled);
                    }

                    if(assetType.getBillingProfileId() != null && assetType.getBillingProfileId().getValue() != null) {
                        setBillingProfileId(assetType.getBillingProfileId().getValue());
                    }

                    if(assetType.getAssetContactId() != null && assetType.getAssetContactId().getValue() != null) {
                        //PSP-17076 Get valid asset contact id
                        assetContactId = getValidAssetContactId(assetType.getAssetContactId().getValue(),pIntuitCustomerAssetABOType.getCustomerAccount());
                    }

                    if(assetType.getEntitlement().getLicenseId() != null && assetType.getEntitlement().getLicenseId().getValue() != null) {
                        setLicenseNumber(assetType.getEntitlement().getLicenseId().getValue());
                    }

                    if(assetType.getEntitlement().getEntitlementId() != null && assetType.getEntitlement().getEntitlementId().getValue() != null) {
                        setEntitlementOfferingCode(assetType.getEntitlement().getEntitlementId().getValue());
                    }

                    //AMO will create the element set its value to nil when we need to set the next charge date to null
                    if(assetType.getNextChargeDate() != null && assetType.getNextChargeDate().getValue() != null) {
                        setNextChargeDate(CalendarUtils.createInstanceFromXMLGregorianCalendar(assetType.getNextChargeDate().getValue()));
                    }

                    if (assetType.getSubscriptionEndDate() != null) {
                        setSubscriptionEndDate(SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(assetType.getSubscriptionEndDate()));
                    }

                    if (assetType.getEntitlement().getEntitlementStartDate() != null) {
                        setSubscriptionStartDate(SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(assetType.getEntitlement().getEntitlementStartDate()));
                    }

                    // AMO sends the customer id as blank sometimes
                    if(assetType.getServiceAccountNumber() != null && assetType.getServiceAccountNumber().getValue() != null &&
                            !assetType.getServiceAccountNumber().getValue().trim().equals("")) {
                        setCustomerId(assetType.getServiceAccountNumber().getValue());
                    }
                }
            }

            String billingAddressId = null;
            for (BillingProfileType billingProfileType : pIntuitCustomerAssetABOType.getBillingProfile()) {
                if(billingProfileType.getBillingProfileId() != null && getBillingProfileId().equals(billingProfileType.getBillingProfileId().getValue())) {

                    if(!isWalletEnabled && billingProfileType.getPaymentMethod() != null) {
                        CCInfoType ccInfoType = billingProfileType.getPaymentMethod().getCCInfo();
                        EFTInfoType eftInfoType = billingProfileType.getPaymentMethod().getEFTInfo();

                        // process credit card information
                        if(ccInfoType != null) {
                            logger.info("settingPaymentMethType :"+EntitlementPaymentMethodType.CC);

                            setPaymentMethodType(EntitlementPaymentMethodType.CC);

                            if (ccInfoType.getExpirationMonth() != null && ccInfoType.getExpirationYear() != null) {
                                setCreditCardExpiration((ccInfoType.getExpirationMonth().intValue() >= 10 ? "" : "0") + ccInfoType.getExpirationMonth() + "/" + ccInfoType.getExpirationYear());
                            }

                            if(ccInfoType.getAccountType() != null) {
                                logger.info("settingCreditType :"+ccInfoType.getAccountType().toString());
                                setCreditCardType(ccInfoType.getAccountType().toString());
                            } else {
                                setCreditCardType(null);
                            }

                            String accountNumber = ccInfoType.getAccountNumber();
                            if(accountNumber != null) {
                                accountNumber = accountNumber.substring(accountNumber.length()-4, accountNumber.length());
                                setCreditCardNumber(accountNumber);
                            }
                        } else if(eftInfoType != null) {
                            setPaymentMethodType(EntitlementPaymentMethodType.EFT);

                            // clear out credit card information
                            setCreditCardExpiration(null);
                            setCreditCardNumber(null);
                            setCreditCardType(null);
                        }
                    }

                    if (billingProfileType.getBillingDayOfMonth() != null) {
                        setBillingDayOfMonth(billingProfileType.getBillingDayOfMonth().intValue());
                    }

                    if(billingProfileType.getBillingProfileAddressId() != null &&
                            billingProfileType.getBillingProfileAddressId().getValue() != null) {
                        billingAddressId = billingProfileType.getBillingProfileAddressId().getValue();
                    }
                }
            }

            for (CustomerAccountType customerAccountType : pIntuitCustomerAssetABOType.getCustomerAccount()) {
                // find asset contact
                for (CustomerAccountContactType customerAccountContactType : customerAccountType.getCustomerAccountContact()) {
                    if(assetContactId != null && customerAccountContactType.getCustomerAccountContactId() != null &&
                            assetContactId.equals(customerAccountContactType.getCustomerAccountContactId().getValue())) {

                        PersonType personType = customerAccountContactType.getContactPerson().getPerson();

                        //contact name
                        String contactName = "";
                        if(personType.getGivenName() != null){
                            contactName += personType.getGivenName() + " ";
                        }
                        if(personType.getMiddleName() != null){
                            contactName += personType.getMiddleName() + " ";
                        }
                        if(personType.getFamilyName() != null){
                            contactName += personType.getFamilyName();
                        }
                        contactName = contactName.trim();
                        setContactName(contactName.length() > 0 ? contactName : null);

                        // contact email
                        PersonType.EmailAddress emailAddress = personType.getEmailAddress();
                        if(emailAddress != null) {
                            if(emailAddress.getMainEmailAddress() != null) {
                                setContactEmail(emailAddress.getMainEmailAddress());
                            } else if(emailAddress.getAlternateEmailAddress() != null) {
                                setContactEmail(emailAddress.getAlternateEmailAddress());
                            }
                        }
                    }
                }

                if(billingAddressId != null) {
                    BusinessType.Address address = customerAccountType.getCustomerAccount().getBusinessCustomerAccount().getBusiness().getAddress();
                    if(address.getPrimaryBillToAddress() != null && address.getPrimaryBillToAddress().getAddressId() != null && billingAddressId.equals(address.getPrimaryBillToAddress().getAddressId().getValue())) {
                        setBillingZipCode(address.getPrimaryBillToAddress().getPostalAddress().getPostalCode());
                    } else if(address.getPrimaryAddress() != null && address.getPrimaryAddress().getAddressId() != null && billingAddressId.equals(address.getPrimaryAddress().getAddressId().getValue())) {
                        setBillingZipCode(address.getPrimaryAddress().getPostalAddress().getPostalCode());
                    } else if(address.getPrimaryShipToAddress() != null && address.getPrimaryShipToAddress().getAddressId() != null && billingAddressId.equals(address.getPrimaryShipToAddress().getAddressId().getValue())) {
                        setBillingZipCode(address.getPrimaryShipToAddress().getPostalAddress().getPostalCode());
                    } else if(address.getAddress() != null) {
                        for (AddressType addressType : address.getAddress()) {
                            if(addressType.getUniversalAddressId() != null && billingAddressId.equals(addressType.getUniversalAddressId().getValue())) {
                                setBillingZipCode(addressType.getPostalAddress().getPostalCode());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public String getEntitlementOfferingCode() {
        return mEntitlementOfferingCode;
    }

    public void setEntitlementOfferingCode(String pEntitlementOfferingCode) {
        mEntitlementOfferingCode = pEntitlementOfferingCode;
    }

    public String getLicenseNumber() {
        return mLicenseNumber;
    }

    public void setLicenseNumber(String pLicenseNumber) {
        mLicenseNumber = pLicenseNumber;
    }

    public String getCustomerId() {
        return mCustomerId;
    }

    public void setCustomerId(String pCustomerId) {
        mCustomerId = pCustomerId;
    }

    public String getBillingProfileId() {
        return mBillingProfileId;
    }

    public void setBillingProfileId(String pBillingProfileId) {
        mBillingProfileId = pBillingProfileId;
    }

    public Integer getBillingDayOfMonth() {
        return mBillingDayOfMonth;
    }

    public void setBillingDayOfMonth(Integer pBillingDayOfMonth) {
        mBillingDayOfMonth = pBillingDayOfMonth;
    }

    public String getBillingZipCode() {
        return mBillingZipCode;
    }

    public void setBillingZipCode(String pBillingZipCode) {
        mBillingZipCode = pBillingZipCode;
    }

    public String getContactName() {
        return mContactName;
    }

    public void setContactName(String pContactName) {
        mContactName = pContactName;
    }

    public String getContactEmail() {
        return mContactEmail;
    }

    public void setContactEmail(String pContactEmail) {
        mContactEmail = pContactEmail;
    }

    public EntitlementPaymentMethodType getPaymentMethodType() {
        return mPaymentMethodType;
    }

    public void setPaymentMethodType(EntitlementPaymentMethodType pPaymentMethodType) {
        mPaymentMethodType = pPaymentMethodType;
    }

    public String getCreditCardType() {
        return mCreditCardType;
    }

    public void setCreditCardType(String pCreditCardType) {
        mCreditCardType = pCreditCardType;
    }

    public String getCreditCardNumber() {
        return mCreditCardNumber;
    }

    public void setCreditCardNumber(String pCreditCardNumber) {
        mCreditCardNumber = pCreditCardNumber;
    }

    public String getCreditCardExpiration() {
        return mCreditCardExpiration;
    }

    public void setCreditCardExpiration(String pCreditCardExpiration) {
        mCreditCardExpiration = pCreditCardExpiration;
    }

    public SpcfCalendar getNextChargeDate() {
        return mNextChargeDate;
    }

    public void setNextChargeDate(SpcfCalendar pNextChargeDate) {
        mNextChargeDate = pNextChargeDate;
    }

    public SpcfCalendar getSubscriptionEndDate() {
        return mSubscriptionEndDate;
    }

    public void setSubscriptionEndDate(SpcfCalendar pSubscriptionEndDate) {
        mSubscriptionEndDate = pSubscriptionEndDate;
    }

    public SpcfCalendar getSubscriptionStartDate() {
        return mSubscriptionStartDate;
    }

    public void setSubscriptionStartDate(SpcfCalendar pSubscriptionStartDate) {
        mSubscriptionStartDate = pSubscriptionStartDate;
    }

    public EntitlementStateCode getEntitlementState() {
        return mEntitlementState;
    }

    public void setEntitlementState(EntitlementStateCode pEntitlementState) {
        mEntitlementState = pEntitlementState;
    }

    public EntitlementDTO copyAmoDtoToPspDto() {
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();

        copyAmoDtoToPspDto(entitlementUnitDTO);

        return entitlementUnitDTO;
    }

    public void copyAmoDtoToPspDto(EntitlementUnitDTO pEntitlementUnitDTO) {
        if (getBillingDayOfMonth() != null) {
            pEntitlementUnitDTO.setBillingDayOfMonth(getBillingDayOfMonth());
        }

        if (getBillingProfileId() != null) {
            pEntitlementUnitDTO.setBillingProfileId(getBillingProfileId());
        }

        if (getBillingZipCode() != null) {
            pEntitlementUnitDTO.setBillingZipCode(getBillingZipCode());
        }

        if (getContactEmail() != null) {
            pEntitlementUnitDTO.setContactEmail(getContactEmail());
        }

        if (getContactName() != null) {
            pEntitlementUnitDTO.setContactName(getContactName());
        }
        if (isWalletEnabled) {
            if (EntitlementPaymentMethodType.CC.equals(getPaymentMethodType())) {
                if (getPaymentMethodType() != null) {
                    pEntitlementUnitDTO.setPaymentMethodType(getPaymentMethodType());
                }
                if (getCreditCardExpiration() != null) {
                    pEntitlementUnitDTO.setCreditCardExpiration(getCreditCardExpiration());
                }
                if (getCreditCardNumber() != null) {
                    pEntitlementUnitDTO.setCreditCardNumber(getCreditCardNumber());
                }

                // Need to verify for AMO is using or not
                if (getCreditCardType() != null) {
                    pEntitlementUnitDTO.setCreditCardType(getCreditCardType());
                }
            } else {
                pEntitlementUnitDTO.setPaymentMethodType(getPaymentMethodType());
                pEntitlementUnitDTO.setCreditCardExpiration(getCreditCardExpiration());
                pEntitlementUnitDTO.setCreditCardNumber(getCreditCardNumber());
                pEntitlementUnitDTO.setCreditCardType(getCreditCardType());
            }
        }
        if (getNextChargeDate() != null) {
            pEntitlementUnitDTO.setNextChargeDate(getNextChargeDate());
        }

        //The sub end date from AMO is not stored in PSP anymore. We will create a sub end date based on entitlement status. See below
/*        if (getSubscriptionEndDate() != null) {
            pEntitlementUnitDTO.setSubscriptionEndDate(getSubscriptionEndDate());
        }*/

        if (getSubscriptionStartDate() != null) {
            pEntitlementUnitDTO.setSubscriptionStartDate(getSubscriptionStartDate());
        }

        if (EntitlementStateCode.Disabled.equals(pEntitlementUnitDTO.getEntitlementState()) &&
                pEntitlementUnitDTO.getSubscriptionEndDate() == null) {
            //Set subscription end date to PSPDate for disabled entitlements to trigger OSCAR.
            pEntitlementUnitDTO.setSubscriptionEndDate(PSPDate.getPSPTime());
        } else
        if (EntitlementStateCode.Enabled.equals(pEntitlementUnitDTO.getEntitlementState()) &&
                pEntitlementUnitDTO.getSubscriptionEndDate() != null) {
            //Set subscription end date to null for enabled entitlements to disable OSCAR.
            pEntitlementUnitDTO.setSubscriptionEndDate(null);
        }
    }

    /**
     * PSP-17076 Get valid asset contact id
     * @param pAssetContactId
     * @param pCustomerAccount
     * @return
     */
    public static String getValidAssetContactId(String pAssetContactId, List<com.intuit.iep.customeraccount.customeraccountbase.v1.CustomerAccountType> pCustomerAccount ){
        Boolean isOOPFixEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_OOP_FIX_ENABLED, false);
        if(isOOPFixEnabled){

            if(pAssetContactId==null || pAssetContactId.equals("")) return pAssetContactId;

            for (CustomerAccountType customerAccountType : pCustomerAccount) {
                // find asset contact
                for (CustomerAccountContactType customerAccountContactType : customerAccountType.getCustomerAccountContact()) {
                    if (pAssetContactId != null && customerAccountContactType.getCustomerAccountContactId() != null &&
                            pAssetContactId.equals(customerAccountContactType.getCustomerAccountContactId().getValue()) && hasValidContactRole(customerAccountContactType)) {

                        return pAssetContactId;
                    }
                }
                if (customerAccountType.getCustomerAccount() != null && customerAccountType.getCustomerAccount().getBusinessCustomerAccount() != null && customerAccountType.getCustomerAccount().getBusinessCustomerAccount().getPrimaryCustomerAccountContactId() != null){
                    return customerAccountType.getCustomerAccount().getBusinessCustomerAccount().getPrimaryCustomerAccountContactId().getValue();
                }
            }

        }

        return pAssetContactId;
    }

    /**
     * PSP-17076 Verify if the contact role
     * for the matching asset contact id is valid
     * @param customerAccountContactType
     * @return
     */
    public static boolean hasValidContactRole(CustomerAccountContactType customerAccountContactType){
        for(CustomerAccountContactRoleType customerAccountContactRoleType: customerAccountContactType.getContactRole()){
            if(customerAccountContactRoleType.getRoleName()!=null && customerAccountContactRoleType.getRoleName().equals("DELETED")){
                return false;
            }
        }
        return true;
    }
}
