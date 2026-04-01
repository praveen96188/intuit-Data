package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: mwaqarbaig
 * Date: Jan 19, 2011
 * Time: 4:48:54 PM
 */
public class SAPCompanyLawRatesHistory {
    private ArrayList<SAPCompanyLawRateDetail> mCompanyLawRateDetails;
    private ArrayList<String> mCompanyLawNames;

    public ArrayList<SAPCompanyLawRateDetail> getCompanyLawRateDetails() {
        if (mCompanyLawRateDetails == null) {
            mCompanyLawRateDetails = new ArrayList<SAPCompanyLawRateDetail>();
        }
        return mCompanyLawRateDetails;
    }

    public void setCompanyLawRateDetails(ArrayList<SAPCompanyLawRateDetail> pCompanyLawRateDetails) {
        mCompanyLawRateDetails = pCompanyLawRateDetails;
    }

    public ArrayList<String> getCompanyLawNames() {
        if (mCompanyLawNames == null) {
            mCompanyLawNames = new ArrayList<String>();
        }
        return mCompanyLawNames;
    }

    public void setCompanyLawNames(ArrayList<String> pCompanyLawNames) {
        mCompanyLawNames = pCompanyLawNames;
    }

}
