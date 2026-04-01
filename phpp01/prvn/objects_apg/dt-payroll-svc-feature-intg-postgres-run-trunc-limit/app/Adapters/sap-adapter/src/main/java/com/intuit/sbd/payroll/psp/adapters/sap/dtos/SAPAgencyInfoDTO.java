package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mwaqarbaig
 * Date: Jan 17, 2011
 * Time: 5:33:09 PM
 */
public class SAPAgencyInfoDTO {
    private SAPAgency agency;
    private String mCurrentEFTPSStatus;
    private String mCurrentRAFStatus;
    private String currentACHEnrollmentStatus;
    private String nameControl;
    private Boolean erFicaDeferralEnabled;

    public String getNameControl() {
        return nameControl;
    }

    public void setNameControl(String pNameControl) {
        nameControl = pNameControl;
    }

    public SAPAgency getAgency() {
        return agency;
    }

    public void setAgency(SAPAgency pAgency) {
        agency = pAgency;
    }

    private List<SAPCompanyPaymentTemplate> mCompanyPaymentTemplates;

    public String getCurrentEFTPSStatus() {
        return mCurrentEFTPSStatus;
    }

    public void setCurrentEFTPSStatus(String pCurrentEFTPSStatus) {
        mCurrentEFTPSStatus = pCurrentEFTPSStatus;
    }

    public String getCurrentRAFStatus() {
        return mCurrentRAFStatus;
    }

    public void setCurrentRAFStatus(String pCurrentRAFStatus) {
        mCurrentRAFStatus = pCurrentRAFStatus;
    }

    public List<SAPCompanyPaymentTemplate> getCompanyPaymentTemplates() {
        if (mCompanyPaymentTemplates == null) {
            mCompanyPaymentTemplates = new ArrayList<SAPCompanyPaymentTemplate>();
        }
        return mCompanyPaymentTemplates;
    }

    public void setCompanyPaymentTemplates(List<SAPCompanyPaymentTemplate> pCompanyPaymentTemplates) {
        mCompanyPaymentTemplates = pCompanyPaymentTemplates;
    }

    public String getCurrentACHEnrollmentStatus() {
        return currentACHEnrollmentStatus;
    }

    public void setCurrentACHEnrollmentStatus(String pCurrentACHEnrollmentStatus) {
        currentACHEnrollmentStatus = pCurrentACHEnrollmentStatus;
    }

    public Boolean getErFicaDeferralEnabled() {
        return erFicaDeferralEnabled;
    }

    public void setErFicaDeferralEnabled(Boolean erFicaDeferralEnabled) {
        this.erFicaDeferralEnabled = erFicaDeferralEnabled;
    }
}
