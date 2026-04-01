package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 5, 2009
 * Time: 5:35:01 PM
 */
public class SAPPaymentTemplate {
    private String paymentTemplateCd;
    private String paymentTemplateName;
    private String agencyName;
    private Date mSupportStartDate;
    private Date processingStartDate;
    private ArrayList<String> mAgencyIDs;
    private boolean canBeFinalized;
    private boolean followsFedDepositFrequency;

    private ArrayList<SAPLawItem> lawItems;
    private ArrayList<String> possibleDepositFrequencies;

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }

    public String getPaymentTemplateName() {
        return paymentTemplateName;
    }

    public void setPaymentTemplateName(String paymentTemplateName) {
        this.paymentTemplateName = paymentTemplateName;
    }

    public ArrayList<SAPLawItem> getLawItems() {
        return lawItems;
    }

    public void setLawItems(ArrayList<SAPLawItem> lawItems) {
        this.lawItems = lawItems;
    }

    public ArrayList<String> getPossibleDepositFrequencies() {
        return possibleDepositFrequencies;
    }

    public void setPossibleDepositFrequencies(ArrayList<String> possibleDepositFrequencies) {
        this.possibleDepositFrequencies = possibleDepositFrequencies;
    }

    public Date getSupportStartDate() {
        return mSupportStartDate;
    }

    public void setSupportStartDate(Date pSupportStartDate) {
        mSupportStartDate = pSupportStartDate;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String pAgencyName) {
        agencyName = pAgencyName;
    }

    public ArrayList<String> getAgencyIDs() {
        if (mAgencyIDs == null) {
            mAgencyIDs = new ArrayList<String>();
        }
        return mAgencyIDs;
    }

    public void setAgencyIDs(ArrayList<String> pAgencyIDs) {
        mAgencyIDs = pAgencyIDs;
    }

    public boolean getCanBeFinalized() {
        return canBeFinalized;
    }

    public void setCanBeFinalized(boolean canBeFinalized) {
        this.canBeFinalized = canBeFinalized;
    }

    public Date getProcessingStartDate() {
        return processingStartDate;
    }

    public void setProcessingStartDate(Date pProcessingStartDate) {
        processingStartDate = pProcessingStartDate;
    }

    public boolean getFollowsFedDepositFrequency() {
        return followsFedDepositFrequency;
    }

    public void setFollowsFedDepositFrequency(boolean pFollowsFedDepositFrequency) {
        followsFedDepositFrequency = pFollowsFedDepositFrequency;
    }
}
