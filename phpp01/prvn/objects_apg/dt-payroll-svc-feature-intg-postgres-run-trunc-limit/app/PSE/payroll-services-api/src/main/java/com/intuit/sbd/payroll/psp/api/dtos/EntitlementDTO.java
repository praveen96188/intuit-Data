package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 7, 2010
 * Time: 10:42:47 AM
 */
public class EntitlementDTO {
    private String mOrderNumber;
    private String mAssetItemNumber;
    private EntitlementStateCode mEntitlementState;
    private EditionType mEditionType;
    private NumberOfEmployeesType mNumberOfEmployeesType;
    private String mEntitlementOfferingCode;
    private String mLicenseNumber;
    private String mCustomerId;
    private SpcfCalendar mNextChargeDate;
    private Integer mBillingDayOfMonth;
    private SpcfCalendar mSubscriptionStartDate;
    private SpcfCalendar mSubscriptionEndDate;
    private EntitlementPaymentMethodType mPaymentMethodType;
    private String mCreditCardType;
    private String mCreditCardNumber;
    private String mCreditCardExpiration;
    private String mContactName;
    private String mContactEmail;
    private String mBillingZipCode;
    private String mCancellationReason;
    private SpcfCalendar mLastMessageTimestamp;
    private Map<String, EntitlementUnitStatusCode> mEntitlementUnitStatuses;
    private OrderSourceCode mOrderSourceCd;
    private String mSubscriptionNumber;
    private String mBillingProfileId;
    private boolean mTrialAssociated;
    private boolean mRetail;

    public String getOrderNumber() {
        return mOrderNumber;
    }

    public void setOrderNumber(String pOrderNumber) {
        mOrderNumber = pOrderNumber;
    }

    public String getAssetItemNumber() {
        return mAssetItemNumber;
    }

    public void setAssetItemNumber(String pAssetItemNumber) {
        mAssetItemNumber = pAssetItemNumber;
    }

    public EntitlementStateCode getEntitlementState() {
        return mEntitlementState;
    }

    public void setEntitlementState(EntitlementStateCode pEntitlementState) {
        mEntitlementState = pEntitlementState;
    }

    public EditionType getEditionType() {
        return mEditionType;
    }

    public void setEditionType(EditionType pEditionType) {
        mEditionType = pEditionType;
    }

    public NumberOfEmployeesType getNumberOfEmployeesType() {
        return mNumberOfEmployeesType;
    }

    public void setNumberOfEmployeesType(NumberOfEmployeesType pNumberOfEmployeesType) {
        mNumberOfEmployeesType = pNumberOfEmployeesType;
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

    public SpcfCalendar getNextChargeDate() {
        return mNextChargeDate;
    }

    public void setNextChargeDate(SpcfCalendar pNextChargeDate) {
        mNextChargeDate = pNextChargeDate;
    }

    public Integer getBillingDayOfMonth() {
        return mBillingDayOfMonth;
    }

    public void setBillingDayOfMonth(Integer pBillingDayOfMonth) {
        mBillingDayOfMonth = pBillingDayOfMonth;
    }

    public SpcfCalendar getSubscriptionStartDate() {
        return mSubscriptionStartDate;
    }

    public void setSubscriptionStartDate(SpcfCalendar pSubscriptionStartDate) {
        mSubscriptionStartDate = pSubscriptionStartDate;
    }

    public SpcfCalendar getSubscriptionEndDate() {
        return mSubscriptionEndDate;
    }

    public void setSubscriptionEndDate(SpcfCalendar pSubscriptionEndDate) {
        mSubscriptionEndDate = pSubscriptionEndDate;
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

    public String getBillingZipCode() {
        return mBillingZipCode;
    }

    public void setBillingZipCode(String pBillingZipCode) {
        if (pBillingZipCode != null && pBillingZipCode.length() > 0){
            mBillingZipCode = pBillingZipCode;
        }
    }

    public String getCancellationReason() {
        return mCancellationReason;
    }

    public void setCancellationReason(String pCancellationReason) {
        mCancellationReason = pCancellationReason;
    }

    public SpcfCalendar getLastMessageTimestamp() {
        return mLastMessageTimestamp;
    }

    public void setLastMessageTimestamp(SpcfCalendar pLastMessageTimestamp) {
        mLastMessageTimestamp = pLastMessageTimestamp;
    }

    public boolean getTrialAssociated() {
        return mTrialAssociated;
    }

    public void setTrialAssociated(boolean pTrialAssociated) {
        mTrialAssociated = pTrialAssociated;
    }

    public Map<String, EntitlementUnitStatusCode> getEntitlementUnitStatuses() {
        if(mEntitlementUnitStatuses == null) {
            mEntitlementUnitStatuses = new HashMap<String, EntitlementUnitStatusCode>();
        }
        return mEntitlementUnitStatuses;
    }

    public OrderSourceCode getOrderSourceCd() {
        return mOrderSourceCd;
    }

    public void setOrderSourceCd(OrderSourceCode pOrderSourceCd) {
        this.mOrderSourceCd = pOrderSourceCd;
    }

    public String getSubscriptionNumber() {
        return mSubscriptionNumber;
    }

    public void setSubscriptionNumber(String pSubscriptionNumber) {
        this.mSubscriptionNumber = pSubscriptionNumber;
    }

    public String getBillingProfileId() {
        return mBillingProfileId;
    }

    public void setBillingProfileId(String pBillingProfileId) {
        mBillingProfileId = pBillingProfileId;
    }

    public ProcessResult validateAdd() {
        return validateCommon();
    }

    public ProcessResult validateUpdate() {        
        return validateCommon();
    }

    protected ProcessResult validateCommon() {
        ProcessResult validationResult = new ProcessResult();

        if (getLicenseNumber() == null || !Validator.isValidLength(getLicenseNumber(), 1, 20)) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getLicenseNumber() == null ? "null" : getLicenseNumber(), "LicenseNumber");
        }

        if (getEntitlementOfferingCode() == null || !Validator.isValidLength(getEntitlementOfferingCode(), 1, 20)) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getEntitlementOfferingCode() == null ? "null" : getEntitlementOfferingCode(), "EntitlementOfferingCode");
        }

        if(getOrderNumber() != null && !Validator.isValidLength(getOrderNumber(), 1, 20)){
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getOrderNumber(), "OrderNumber");
        }

        if(getCustomerId() != null && !Validator.isValidLength(getCustomerId(), 1, 50)){
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getCustomerId(), "CustomerId");
        }

        if(getCreditCardType() != null && !Validator.isValidLength(getCreditCardType(), 1, 20)){
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getCreditCardType(), "CreditCardType");
        }

        if(getCreditCardNumber() != null && !Validator.isValidLength(getCreditCardNumber(), 1, 4)){
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getCreditCardNumber(), "CreditCardNumber");
        }

        if(getCreditCardExpiration() != null && !Validator.isValidLength(getCreditCardExpiration(), 1, 7)){
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getCreditCardExpiration(), "CreditCardExpiration");
        }

        if(getContactEmail() != null && !Validator.isValidLength(getContactEmail(), 1, 100)){
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getContactEmail(), "ContactEmail");
        }

        if(getContactName() != null && !Validator.isValidLength(getContactName(), 1, 255)) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getContactName(), "ContactName");
        }

        if(getCancellationReason() != null && !Validator.isValidLength(getCancellationReason(), 1, 255)) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getCancellationReason(), "CancellationReason");
        }

        if(getBillingZipCode() != null && !Validator.isValidLength(getBillingZipCode(), 1, 10)) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getBillingZipCode(), "BillingZipCode");
        }

        if(getBillingProfileId() != null && !Validator.isValidLength(getBillingProfileId(), 1, 40)) {
            validationResult.getMessages().InvalidValue(EntityName.Entitlement, getBillingProfileId(), "BillingProfileId");
        }

        return validationResult;
    }

    public ProcessResult copyDTOToDomain(Entitlement pEntitlement, EntitlementCode pEntitlementCode) {
        ProcessResult processResult = new ProcessResult();
        pEntitlement.setCreditCardExpiration(getCreditCardExpiration());
        pEntitlement.setCreditCardNumber(getCreditCardNumber());
        pEntitlement.setCreditCardType(getCreditCardType());
        if(getCustomerId() != null) {
            // don't overwrite with null
            pEntitlement.setCustomerId(getCustomerId());
        }
        pEntitlement.setLicenseNumber(getLicenseNumber());
        pEntitlement.setEntitlementOfferingCode(getEntitlementOfferingCode());
        if (getOrderNumber() != null && getOrderNumber().length() > 0) {
            pEntitlement.setOrderNumber(getOrderNumber());
        }
        pEntitlement.setPaymentMethodType(getPaymentMethodType());
        pEntitlement.setContactEmail(getContactEmail());
        pEntitlement.setContactName(getContactName());
        pEntitlement.setCancellationReason(getCancellationReason());
        pEntitlement.setLastMessageTimestamp(getLastMessageTimestamp());
        pEntitlement.setOrderSourceCd(getOrderSourceCd());
        pEntitlement.setBillingProfileId(getBillingProfileId());
        pEntitlement.setTrialAssociated(getTrialAssociated());
        pEntitlement.setRetail(isRetail());

        if (getBillingZipCode() != null && getBillingZipCode().length() > 0){
            pEntitlement.setBillingZipCode(getBillingZipCode());
        }

        if (getSubscriptionNumber() != null) {
            pEntitlement.setSubscriptionNumber(getSubscriptionNumber());
        }

        if (getSubscriptionStartDate() != null) {
            pEntitlement.setSubscriptionStartDate(getSubscriptionStartDate());
        }

        if (getNextChargeDate() != null) {
            pEntitlement.setNextChargeDate(getNextChargeDate());
        }

        if (getBillingDayOfMonth() != null) {
            pEntitlement.setBillingDayOfMonth(getBillingDayOfMonth());
        }

        //Assisted entitlements should not have a next charge date.
        if (pEntitlement.getEntitlementCode().isAssisted()) {
            pEntitlement.setNextChargeDate(null);
        }

        if (pEntitlement.hasSubscriptionEndDateChanged(getSubscriptionEndDate())) {
            List<Company> companies = new ArrayList<Company>();
            for (EntitlementUnit entitlementUnit : pEntitlement.getEntitlementUnitCollection()) {
                Company company = entitlementUnit.getCompany();
                if(!companies.contains(company)) {
                    companies.add(company);
                    CompanyEvent.createSubscriptionEndDateChangedEvent(company, pEntitlement.getSubscriptionEndDate(), getSubscriptionEndDate());
                }
            }
        }
        pEntitlement.setSubscriptionEndDate(getSubscriptionEndDate());

        if(getEntitlementUnitStatuses() != null) {
            for (EntitlementUnit entitlementUnit : pEntitlement.getEntitlementUnitCollection()) {
                EntitlementUnitStatusCode entitlementUnitStatusCode = getEntitlementUnitStatuses().get(entitlementUnit.getFedTaxId());
                if(entitlementUnitStatusCode != null) {
                    switch (entitlementUnitStatusCode) {
                        case Activated:
                            if(entitlementUnit.getEntitlementUnitStatus().in(EntitlementUnitStatusCode.PendingActivation,
                                                                             EntitlementUnitStatusCode.PendingReactivation,
                                                                             EntitlementUnitStatusCode.ErrorActivating)) {
                                entitlementUnit.setEntitlementUnitStatus(entitlementUnitStatusCode);
                            }
                            break;
                        case Deactivated:
                            if(entitlementUnit.getEntitlementUnitStatus().in(EntitlementUnitStatusCode.PendingDeactivation,
                                                                             EntitlementUnitStatusCode.ErrorDeactivating)) {
                                entitlementUnit.setEntitlementUnitStatus(entitlementUnitStatusCode);
                            }
                            break;
                    }
                }
            }
        }

        if(getEntitlementState() != null) {
            if (!getEntitlementState().equals(pEntitlement.getEntitlementState())) {
                List<Company> companies = new ArrayList<Company>();
                for (EntitlementUnit entitlementUnit : pEntitlement.getEntitlementUnitCollection()) {
                    Company company = entitlementUnit.getCompany();
                    try {
                        PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
                        if(!companies.contains(company)) {
                            companies.add(company);
                            CompanyEvent.createEntitlementStateChangedEvent(company, pEntitlement.getEntitlementState(), getEntitlementState(), pEntitlement.getId().toString());
                            if (getEntitlementState() == EntitlementStateCode.Disabled && pEntitlement.getEntitlementCode().isAssisted() && company.isCompanyOnService(ServiceCode.Tax)) {
                                DomainEntitySet<ServiceSubStatus> cancelledSubStatusAsList = Application.find(ServiceSubStatus.class, ServiceSubStatus.ServiceSubStatusCd().equalTo(ServiceSubStatusCode.Cancelled));
                                processResult.merge(PayrollServices.companyManager.updateSubStatuses(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, cancelledSubStatusAsList));
                            }else if (getEntitlementState() == EntitlementStateCode.Disabled && company.isCompanyOnService(ServiceCode.ViewMyPaycheck)){
                                processResult.merge(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                        ServiceCode.ViewMyPaycheck,
                                        ServiceSubStatusCode.Cancelled));
                            }
                        }
                    } finally {
                        PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
                    }
                }
            }
            pEntitlement.setEntitlementState(getEntitlementState());
        }
        
        if(pEntitlementCode != null) {
            if (! pEntitlement.getEntitlementCode().equals(pEntitlementCode)) {
                for (EntitlementUnit eu : pEntitlement.getEntitlementUnitCollection()) {
                    CompanyEvent.createEntitlementCodeChangedEvent(eu.getCompany(), pEntitlement.getEntitlementCode(), pEntitlementCode);    
                }
            }
            pEntitlement.setEntitlementCode(pEntitlementCode);
        }

        return processResult;
    }

    public static Map<String, EditionType> getEditionValues() {
        Map<String, EditionType> responseMap = new HashMap<String, EditionType>();

        List<String> list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_EDITION_VALUE_BASIC, "Basic").split(","));
        loadListInToMap(list, responseMap,  EditionType.Basic);

        list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_EDITION_VALUE_ENHANCED, "Enhanced").split(","));
        loadListInToMap(list, responseMap,  EditionType.Enhanced);

        list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_EDITION_VALUE_ENHANCED_ACCOUNTANT, "Enhanced Accountant").split(","));
        loadListInToMap(list, responseMap,  EditionType.EnhancedAccountant);

        list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_EDITION_VALUE_ENHANCED_ACCOUNTANT_PRO_ADVISOR, "Enhanced Accountant Pro Advisor").split(","));
        loadListInToMap(list, responseMap,  EditionType.EnhancedAccountantProAdvisor);

        list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_EDITION_VALUE_STANDARD, "Standard").split(","));
        loadListInToMap(list, responseMap,  EditionType.Standard);

        return responseMap;
    }

    public static Map<String, NumberOfEmployeesType> getNumberOfEmployeesValues() {
        Map<String, NumberOfEmployeesType> responseMap = new HashMap<String, NumberOfEmployeesType>();

        List<String> list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_NUMBER_OF_EMPLOYEES_VALUE_ONE, "One").split(","));
        loadListInToMap(list, responseMap,  NumberOfEmployeesType.ONE);

        list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_NUMBER_OF_EMPLOYEES_VALUE_UPTO3, "Up to 3").split(","));
        loadListInToMap(list, responseMap,  NumberOfEmployeesType.UPTO3);

        list = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.AMO_NUMBER_OF_EMPLOYEES_VALUE_UNLIMITED, "Unlimited").split(","));
        loadListInToMap(list, responseMap,  NumberOfEmployeesType.UNLIMITED);

        return responseMap;
    }

    private static <T, E> void loadListInToMap(List<T> list, Map<T, E> map, E value) {
        for (T listItem : list) {
            map.put(listItem, value);
        }
    }

    private <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isRetail() {
        return mRetail;
    }

    public void setRetail(boolean pRetail) {
        mRetail = pRetail;
    }
}
