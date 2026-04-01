/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPCompany.java#5 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.api.dtos.PayrollFrequencyDTO;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * SAPCompany -- DTO to represent a company for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
public class SAPCompany {
    private String sourceSystemCd;
    private String companyId;
    private String fein;
    private String legalName;
    private String DBA;
    private String notificationEmail;
    private PayrollFrequencyDTO payrollFrequencyCd;
    private SAPTaxCompanyServiceInfo taxService;
    private boolean isEditable;
    private String gseq;
    private boolean isAssisted;
    private boolean isDIY; // DIY (DD or non DD)
    private SAPCompanyServiceState companyServiceStateCd;
    private boolean canChangePriceType;
    private boolean hasCompanyAgencies;
    private boolean isAssistedServiceCancelled;
    private String customerId = null;
    private boolean isVmp;
    private String iamRealmId;
    private boolean isMoneyMovementOnboardingEnabled;

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String pSourceSystemCd) {
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

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String pNotificationEmail) {
        this.notificationEmail = pNotificationEmail;
    }

    public PayrollFrequencyDTO getPayrollFrequencyCd() {
        return payrollFrequencyCd;
    }

    public void setPayrollFrequencyCd(PayrollFrequencyDTO pPayrollFrequencyCd) {
        this.payrollFrequencyCd = pPayrollFrequencyCd;
    }

    public String getGseq() {
        return gseq;
    }

    public void setGseq(String gseq) {
        this.gseq = gseq;
    }

    public SAPTaxCompanyServiceInfo getTaxService() {
        return taxService;
    }

    public void setTaxService(SAPTaxCompanyServiceInfo taxService) {
        this.taxService = taxService;
    }

    public boolean getIsEditable() {
        return isEditable;
    }

    public void setIsEditable(boolean editable) {
        isEditable = editable;
    }

    public boolean getIsAssisted() {
        return isAssisted;
    }

    public void setIsAssisted(boolean pIsCompanyOnAssisted) {
        isAssisted = pIsCompanyOnAssisted;
    }

    public boolean getHasCompanyAgencies() {
        return hasCompanyAgencies;
    }

    public void setHasCompanyAgencies(boolean hasCompanyAgencies) {
        this.hasCompanyAgencies = hasCompanyAgencies;
    }

    public boolean getCanChangePriceType() {
        return canChangePriceType;
    }

    public void setCanChangePriceType(boolean canChangePriceType) {
        this.canChangePriceType = canChangePriceType;
    }

    public SAPCompanyServiceState getCompanyServiceStateCd() {
        return companyServiceStateCd;
    }

    public void setCompanyServiceStateCd(SAPCompanyServiceState companyServiceStateCd) {
        this.companyServiceStateCd = companyServiceStateCd;
    }

    public boolean getIsAssistedServiceCancelled() {
        return isAssistedServiceCancelled;
    }

    public void setIsAssistedServiceCancelled(boolean assistedServiceStatus) {
        this.isAssistedServiceCancelled = assistedServiceStatus;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String pCustomerId) {
        customerId = pCustomerId;
    }

    public boolean getIsVmp() {
        return isVmp;
    }

    public void setIsVmp(boolean pVmp) {
        isVmp = pVmp;
    }

    public String getIamRealmId() {
        return iamRealmId;
    }

    public void setIamRealmId(String pIamRealmId) {
        iamRealmId = pIamRealmId;
    }

    public boolean getIsDIY() {
        return isDIY;
    }

    public void setIsDIY(boolean pIsDIY) {
        isDIY = pIsDIY;
    }

    public void setIsMoneyMovementOnboardingEnabled(boolean moneyMovementOnboardingEnabled) {
        isMoneyMovementOnboardingEnabled = moneyMovementOnboardingEnabled;
    }

    public boolean getIsMoneyMovementOnboardingEnabled() {
        return isMoneyMovementOnboardingEnabled;
    }

}
