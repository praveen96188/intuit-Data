package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils;

import java.util.ArrayList;

/**
 * @author Jeff Jones
 */
public class TransmissionsList {

    private String mCompanySeq;
    private ArrayList<AS400Transmission> mAS400Transmissions;
    private ArrayList<PSPTransmission> mPSPTransmissions;

    public TransmissionsList() {
        mAS400Transmissions = new ArrayList<AS400Transmission>();
        mPSPTransmissions = new ArrayList<PSPTransmission>();
    }

    public String getCompanySeq() {
        return mCompanySeq;
    }

    public void setCompanySeq(String pCompanySeq) {
        this.mCompanySeq = pCompanySeq;
    }

    public void add(AS400Transmission pAS400Transmission) {
        mAS400Transmissions.add(pAS400Transmission);
    }
    
    public void add(PSPTransmission pPSPTransmission) {
        mPSPTransmissions.add(pPSPTransmission);
    }

    public ArrayList<AS400Transmission> getmAS400Transmissions() {
        return mAS400Transmissions;
    }

    public ArrayList<PSPTransmission> getmPSPTransmissions() {
        return mPSPTransmissions;
    }
}
