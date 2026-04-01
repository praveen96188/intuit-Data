/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/CompanyDTO.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TaxExemptStatusCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Marcela Villani
 */
public class CompanyDTO {
    private SourceSystemCode sourceSystemCd;
    private String companyId;
    private String fein;
    private String legalName;
    private String DBA;
    private AddressDTO legalAddress;
    private AddressDTO mailingAddress;
    private AddressDTO complianceAddress;
    private String notificationEmail;
    private String phone;
    private Collection<ContactDTO> contacts = new ArrayList<ContactDTO>();
    private PayrollFrequencyDTO payrollFrequencyCd;
    private String nextEmployeeId;
    private String nextPaycheckId;
    private String nextPayrollTransactionId;
    private String nextPayrollItemId;
    private DateDTO taxExemptExpirationDate = null;
    private TaxExemptStatusCode taxExemptStatus = null;
    private QuickbooksInfoDTO quickBooksInfo;
    private DateDTO signUpDate = null;
    private Long currentToken = null;
    private boolean debugLogging = false;
    private String psId;
    private Long cloudCurrentToken;
    private String priceType;
    private String iAMRealmId = null;
    private EntityChangeDTO entityChange = null;
    private String NameControl;
    private CompanyAdditionalInfoDTO companyAdditionalInfo = null;

    public String getNameControl() {
        return NameControl;
    }

    public void setNameControl(String pNameControl) {
        NameControl = pNameControl;
    }

    public SourceSystemCode getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(SourceSystemCode pSourceSystemCd) {
        this.sourceSystemCd = pSourceSystemCd;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String pCompanyId) {
        this.companyId = pCompanyId;
    }

    public String getFein() {
        return fein;
    }

    public void setFein(String pFein) {
        this.fein = pFein;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String pLegalName) {
        this.legalName = pLegalName;
    }

    public String getDBA() {
        return DBA;
    }

    public void setDBA(String pDBA) {
        this.DBA = pDBA;
    }

    public AddressDTO getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(AddressDTO pLegalAddress) {
        this.legalAddress = pLegalAddress;
    }

    public AddressDTO getMailingAddress() {
        return mailingAddress;
    }

    public AddressDTO getComplianceAddress() {
        return complianceAddress;
    }

    public void setComplianceAddress(AddressDTO complianceAddress) {
        this.complianceAddress = complianceAddress;
    }

    public void setMailingAddress(AddressDTO pMailingAddress) {
        this.mailingAddress = pMailingAddress;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String pNotificationEmail) {
        this.notificationEmail = pNotificationEmail;
    }

    public Collection<ContactDTO> getContacts() {
        return contacts;
    }

    public void setContacts(Collection<ContactDTO> pContacts) {
        this.contacts = pContacts;
    }

    public PayrollFrequencyDTO getPayrollFrequencyCd() {
        return payrollFrequencyCd;
    }

    public void setPayrollFrequencyCd(PayrollFrequencyDTO pPayrollFrequencyCd) {
        this.payrollFrequencyCd = pPayrollFrequencyCd;
    }

    public String getNextEmployeeId() {
        return nextEmployeeId;
    }

    public void setNextEmployeeId(String pNextEmployeeId) {
        this.nextEmployeeId = pNextEmployeeId;
    }

    public String getNextPaycheckId() {
        return nextPaycheckId;
    }

    public void setNextPaycheckId(String pNextPaycheckId) {
        this.nextPaycheckId = pNextPaycheckId;
    }

    public String getNextPayrollTransactionId() {
        return nextPayrollTransactionId;
    }

    public void setNextPayrollTransactionId(String pNextPayrollTransactionId) {
        this.nextPayrollTransactionId = pNextPayrollTransactionId;
    }

    public String getNextPayrollItemId() {
        return nextPayrollItemId;
    }

    public void setNextPayrollItemId(String pNextPayrollItemId) {
        this.nextPayrollItemId = pNextPayrollItemId;
    }

    public DateDTO getTaxExemptExpirationDate() {
        return taxExemptExpirationDate;
    }

    public void setTaxExemptExpirationDate(DateDTO taxExemptExpirationDate) {
        this.taxExemptExpirationDate = taxExemptExpirationDate;
    }

    public TaxExemptStatusCode getTaxExemptStatus() {
        return taxExemptStatus;
    }

    public void setTaxExemptStatus(TaxExemptStatusCode pTaxExemptStatus) {
        taxExemptStatus = pTaxExemptStatus;
    }

    public QuickbooksInfoDTO getQuickBooksInfo() {
        return quickBooksInfo;
    }

    public void setQuickBooksInfo(QuickbooksInfoDTO quickBooksInfo) {
        this.quickBooksInfo = quickBooksInfo;
    }

    public DateDTO getSignUpDate() {
        return signUpDate;
    }

    public void setSignUpDate(DateDTO signUpDate) {
        this.signUpDate = signUpDate;
    }

    public Long getCurrentToken() {
        return currentToken;
    }

    public void setCurrentToken(Long currentToken) {
        this.currentToken = currentToken;
    }

    public boolean isDebugLogging() {
        return debugLogging;
    }

    public void setDebugLogging(boolean debugLogging) {
        this.debugLogging = debugLogging;
    }

    public String getPsId() {
        return psId;
    }

    public void setPsId(String psId) {
        this.psId = psId;
    }

    public ProcessResult validateCompanyDTO() {
        ProcessResult validationResult = new ProcessResult();

        if (fein == null ||
                !(Validator.isMatchingPattern(fein, "^[0-9]{9}$"))) {
            validationResult.getMessages().InvalidValue(EntityName.Company, companyId, "FedTaxId");
        }

        if (!Validator.isValidLength(DBA, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.Company, companyId, "DBAName");
        }

        if ((legalName == null) || !(Validator.isValidLength(legalName, 1, 100))) {
            validationResult.getMessages().InvalidValue(EntityName.Company, companyId, "LegalName");
        }

        if ((companyId == null) ||
                !(Validator.isValidLength(companyId, 1, 50))) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.Company, companyId, "SourceCompanyId");
        }

        if ((sourceSystemCd == null) ||
                !(Validator.isValidLength(sourceSystemCd.toString(), 1, 10))) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.Company, companyId, "SourceSystemCD");
        }

        if ((notificationEmail == null) ||
                !(Validator.isValidEmail(notificationEmail))) {
            validationResult.getMessages()
                    .InvalidValue(EntityName.Company, companyId, "NotificationEmail");
        }

        if (mailingAddress == null) {
            validationResult.getMessages().CompanyAddressNull(EntityName.Company, companyId);
        } else {
            validationResult.merge(mailingAddress.validateAddressDTO());
        }

        if (legalAddress == null) {
            validationResult.getMessages().CompanyAddressNull(EntityName.Company, companyId);
        } else {
            validationResult.merge(legalAddress.validateAddressDTO());
        }

        if(complianceAddress != null) {
            validationResult.merge(complianceAddress.validateAddressDTO());
        }

        if (contacts == null || contacts.size() == 0) {
            validationResult.getMessages().CompanyContactNotSpecified(EntityName.Company, companyId);
        }

        for (ContactDTO currContact : contacts) {
            validationResult.merge(currContact.validateContactDTO());
        }

        if (taxExemptExpirationDate != null) {
            validationResult.merge(taxExemptExpirationDate.validate());
        }

        if (quickBooksInfo != null) {
            validationResult.merge(quickBooksInfo.validate());
        }

        if (signUpDate != null) {
            validationResult.merge(signUpDate.validate());
        }

        if (cloudCurrentToken != null && cloudCurrentToken.longValue() < 0) {
            validationResult.getMessages().RangeValidationFailure(EntityName.Company,  companyId, "cloudCurrentToken", 0L);
        }

        if (!Validator.isValidLength(phone, 0, 100)) {
            validationResult.getMessages().InvalidValue(EntityName.Company, companyId, "Phone");
        }

        return validationResult;
    }

    public boolean hasAccountSignatoryContact() {
        for (ContactDTO currContact : contacts) {
            if (currContact != null && currContact.getAccountSignatory()) {
                return true;
            }
        }

        return false;
    }

    public Long getCloudCurrentToken() {
        return cloudCurrentToken;
    }

    public void setCloudCurrentToken(Long cloudCurrentToken) {
        this.cloudCurrentToken = cloudCurrentToken;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getIAMRealmId() {
        return iAMRealmId;
    }

    public void setIAMRealmId(String iAMRealmId) {
        this.iAMRealmId = iAMRealmId;
	}

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String pPhone) {
        phone = pPhone;
    }

    public EntityChangeDTO getEntityChange() {
        return entityChange;
    }

    public void setEntityChange(EntityChangeDTO pEntityChange) {
        entityChange = pEntityChange;
    }

    public CompanyAdditionalInfoDTO getCompanyAdditionalInfo() {
        return companyAdditionalInfo;
    }

    public void setCompanyAdditionalInfo(CompanyAdditionalInfoDTO pCompanyAdditionalInfo) {
        companyAdditionalInfo = pCompanyAdditionalInfo;
    }
}
