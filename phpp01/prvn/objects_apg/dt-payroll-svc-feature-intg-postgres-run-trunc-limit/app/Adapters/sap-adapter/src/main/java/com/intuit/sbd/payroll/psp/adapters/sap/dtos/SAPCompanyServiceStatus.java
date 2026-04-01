package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rnorian
 * Date: Jun 4, 2009
 * Time: 1:30:20 PM
 */
public class SAPCompanyServiceStatus {

    private String serviceCd;
    private SAPDisplayStatus displayStatus;
    private SAPServiceStatus status;
    private boolean canUpdateStatus;
    private Boolean hasSignatureFile;
    private String custodialId;
    private boolean isSafeHarbor;
    private ArrayList<SAPServiceStatus> allowedTransitions;
    private SAPCompanyDdLimits ddLimits;
    private String fundingModelCd;
    private Date serviceStartDate;
    private String offering;
    private SAPOffering offeringDetails;
    private boolean canEditOffering;
    private String offer;
    private Date offerExpirationDate;
    private boolean canEditOffer;
    private boolean isAssistedActive;

    private Date firstTaxQuarter;
    private String w2PrintingPreference;
 // specific information for each service displays in company banner
    private SAPDirectDepositServiceInformation directDepositAdditionalInfo;
    private SAPBillPaymentServiceInformation billPaymentAdditionalInfo;

    public String getServiceCd() {
        return serviceCd;
    }

    public void setServiceCd(String serviceCd) {
        this.serviceCd = serviceCd;
    }
    

    public SAPDisplayStatus getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(SAPDisplayStatus displayStatus) {
        this.displayStatus = displayStatus;
    }


    public SAPServiceStatus getStatus() {
        return status;
    }

    public void setStatus(SAPServiceStatus status) {
        this.status = status;
    }

    public boolean getCanUpdateStatus() {
        return canUpdateStatus;
    }

    public void setCanUpdateStatus(boolean canUpdateStatus) {
        this.canUpdateStatus = canUpdateStatus;
    }

    public Boolean getHasSignatureFile() {
        return hasSignatureFile;
    }

    public void setHasSignatureFile(Boolean pHasSignatureFile) {
        hasSignatureFile = pHasSignatureFile;
    }

    public ArrayList<SAPServiceStatus> getAllowedTransitions() {
        return allowedTransitions;
    }

    public void setAllowedTransitions(ArrayList<SAPServiceStatus> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public SAPCompanyDdLimits getDdLimits() {
        return ddLimits;
    }

    public void setDdLimits(SAPCompanyDdLimits pDdLimits) {
        ddLimits = pDdLimits;
    }

    public String getFundingModelCd() {
        return fundingModelCd;
    }

    public void setFundingModelCd(String pFundingModelCd) {
        fundingModelCd = pFundingModelCd;
    }

    public SAPDirectDepositServiceInformation getDirectDepositAdditionalInfo() {
        return directDepositAdditionalInfo;
    }

    public void setDirectDepositAdditionalInfo(SAPDirectDepositServiceInformation pDirectDepositAdditionalInfo) {
        directDepositAdditionalInfo = pDirectDepositAdditionalInfo;
    }

    public SAPBillPaymentServiceInformation getBillPaymentAdditionalInfo() {
        return billPaymentAdditionalInfo;
    }

    public void setBillPaymentAdditionalInfo(SAPBillPaymentServiceInformation pBillPaymentAdditionalInfo) {
        billPaymentAdditionalInfo = pBillPaymentAdditionalInfo;
    }

 	public String getCustodialId() {
        return custodialId;
    }

    public void setCustodialId(String custodialId) {
        this.custodialId = custodialId;
    }

    public boolean getIsSafeHarbor() {
        return isSafeHarbor;
    }

    public void setSafeHarbor(boolean safeHarbor) {
        isSafeHarbor = safeHarbor;
    }

    public Date getServiceStartDate() {
        return serviceStartDate;
    }

    public void setServiceStartDate(Date serviceStartDate) {
        this.serviceStartDate = serviceStartDate;
    }

    public String getOffering() {
        return offering;
    }

    public void setOffering(String pOffering) {
        offering = pOffering;
    }

    public SAPOffering getOfferingDetails() {
        return offeringDetails;
    }

    public void setOfferingDetails(SAPOffering offeringDetails) {
        this.offeringDetails = offeringDetails;
    }

    public boolean getCanEditOffering() {
        return canEditOffering;
    }

    public void setCanEditOffering(boolean pCanEditOffering) {
        canEditOffering = pCanEditOffering;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String pOffer) {
        offer = pOffer;
    }

    public boolean getCanEditOffer() {
        return canEditOffer;
    }

    public void setCanEditOffer(boolean pCanEditOffer) {
        canEditOffer = pCanEditOffer;
    }

    public Date getFirstTaxQuarter() {
           return firstTaxQuarter;
       }

    public void setFirstTaxQuarter(Date firstTaxQuarter) {
        this.firstTaxQuarter = firstTaxQuarter;
    }

    public boolean getIsAssistedActive() {
        return isAssistedActive;
    }

    public void setIsAssistedActive(boolean pAssistedActive) {
        isAssistedActive = pAssistedActive;
    }

    public Date getOfferExpirationDate() {
        return offerExpirationDate;
    }

    public void setOfferExpirationDate(Date pOfferExpirationDate) {
        offerExpirationDate = pOfferExpirationDate;
    }

    public String getW2PrintingPreference() {
        return w2PrintingPreference;
    }

    public void setW2PrintingPreference(String pW2PrintingPreference) {
        w2PrintingPreference = pW2PrintingPreference;
    }
}
