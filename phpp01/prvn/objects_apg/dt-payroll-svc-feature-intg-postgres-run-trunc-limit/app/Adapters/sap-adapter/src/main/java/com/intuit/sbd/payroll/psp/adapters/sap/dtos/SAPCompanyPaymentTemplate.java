package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dweinberg
 * Date: May 16, 2009
 * Time: 7:32:35 PM
 */
public class SAPCompanyPaymentTemplate {

    private SAPPaymentTemplate paymentTemplate;

    private boolean canChangeDepositFrequency;
    private SAPDepositFrequency currentDepositFrequency;
    private SAPDepositFrequency futureDepositFrequency;
    private List<SAPPaymentMethod> mPaymentMethods;
    private List<SAPCompanyAgencyPaymentTemplateAgencyId> mAdditionalIds;

    private List<SAPCompanyLawRateDetail> mLawRates;
    private List<SAPCompanyFilingAmountHistory> activeFilingAmounts;

    private String agencyTaxpayerId;

    private boolean is944Filer;

    private String mFilerType;
    private SAPQuarter filerTypeFutureEffectiveQuarter;

    private boolean mRegisteredForACH = false;

    private boolean hasSUIERRates;

    public SAPPaymentTemplate getPaymentTemplate() {
        return paymentTemplate;
    }

    public void setPaymentTemplate(SAPPaymentTemplate paymentTemplate) {
        this.paymentTemplate = paymentTemplate;
    }

    public SAPDepositFrequency getCurrentDepositFrequency() {
        return currentDepositFrequency;
    }

    public void setCurrentDepositFrequency(SAPDepositFrequency currentDepositFrequency) {
        this.currentDepositFrequency = currentDepositFrequency;
    }

    @Deprecated
    /**
     * @deprecated this "single future deposit frequency" concept really only exists in Gemini.  Call getAllDepositFrequencies instead.
     */
    public SAPDepositFrequency getFutureDepositFrequency() {
        return futureDepositFrequency;
    }

    public void setFutureDepositFrequency(SAPDepositFrequency futureDepositFrequency) {
        this.futureDepositFrequency = futureDepositFrequency;
    }

    @Deprecated
    /**
     * @deprecated use getFilerType
     */
    public boolean isIs944Filer() {
        return is944Filer;
    }

    public void setIs944Filer(boolean is944Filer) {
        this.is944Filer = is944Filer;
    }

    public List<SAPPaymentMethod> getPaymentMethods() {
        if (mPaymentMethods == null) {
            mPaymentMethods = new ArrayList<SAPPaymentMethod>();
        }
        return mPaymentMethods;
    }

    public void setPaymentMethods(List<SAPPaymentMethod> pPaymentMethods) {
        if (pPaymentMethods == null) {
            pPaymentMethods = new ArrayList<SAPPaymentMethod>();
        }
        mPaymentMethods = pPaymentMethods;
    }

    public List<SAPCompanyLawRateDetail> getLawRates() {
        if (mLawRates == null) {
            mLawRates = new ArrayList<SAPCompanyLawRateDetail>();
        }
        return mLawRates;
    }

    public List<SAPCompanyAgencyPaymentTemplateAgencyId> getAdditionalIds() {
        if (mAdditionalIds == null) {
            mAdditionalIds=new ArrayList<SAPCompanyAgencyPaymentTemplateAgencyId>();
        }
        return mAdditionalIds;
    }

    public void setAdditionalIds(List<SAPCompanyAgencyPaymentTemplateAgencyId> pAdditionalIds) {
        if (pAdditionalIds == null) {
            pAdditionalIds=new ArrayList<SAPCompanyAgencyPaymentTemplateAgencyId>();
        }
        mAdditionalIds = pAdditionalIds;
    }

    public void setLawRates(List<SAPCompanyLawRateDetail> pLawRates) {
        mLawRates = pLawRates;
    }

    public String getFilerType() {
        return mFilerType;
    }

    public void setFilerType(String pFilerType) {
        mFilerType = pFilerType;
    }

    public boolean isRegisteredForACH() {
        return mRegisteredForACH;
    }

    public void setRegisteredForACH(boolean pRegisteredForACH) {
        mRegisteredForACH = pRegisteredForACH;
    }

    public String getAgencyTaxpayerId() {
        return agencyTaxpayerId;
    }

    public void setAgencyTaxpayerId(String agencyTaxpayerId) {
        this.agencyTaxpayerId = agencyTaxpayerId;
    }

    public List<SAPCompanyFilingAmountHistory> getActiveFilingAmounts() {
        if (activeFilingAmounts == null) {
            activeFilingAmounts = new ArrayList<SAPCompanyFilingAmountHistory>();
        }
        return activeFilingAmounts;
    }

    public void setActiveFilingAmounts(List<SAPCompanyFilingAmountHistory> pActiveFilingAmounts) {
        activeFilingAmounts = pActiveFilingAmounts;
    }

    public boolean getCanChangeDepositFrequency() {
        return canChangeDepositFrequency;
    }

    public void setCanChangeDepositFrequency(boolean pCanChangeDepositFrequency) {
        canChangeDepositFrequency = pCanChangeDepositFrequency;
    }

    public SAPQuarter getFilerTypeFutureEffectiveQuarter() {
        return filerTypeFutureEffectiveQuarter;
    }

    public void setFilerTypeFutureEffectiveQuarter(SAPQuarter pFilerTypeFutureEffectiveQuarter) {
        filerTypeFutureEffectiveQuarter = pFilerTypeFutureEffectiveQuarter;
    }

    public boolean getHasSUIERRates() {
        return hasSUIERRates;
    }

    public void setHasSUIERRates(boolean pHasSUIERRates) {
        hasSUIERRates = pHasSUIERRates;
    }
}
