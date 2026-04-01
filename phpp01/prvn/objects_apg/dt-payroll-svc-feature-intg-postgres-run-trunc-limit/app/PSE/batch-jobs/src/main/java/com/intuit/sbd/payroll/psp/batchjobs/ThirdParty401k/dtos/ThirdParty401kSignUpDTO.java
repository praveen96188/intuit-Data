package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.dtos;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
    @author Jeff Jones
 */
public class ThirdParty401kSignUpDTO {
    public enum RecordType {ADD, UPDATE, CANCEL}

    private RecordType mRecordType;
    private String mFEIN;
    private String mCustodialAccountId;
    private SpcfCalendar mEffectiveDate;
    private String mLegalName;
    private Boolean mHasSafeHarbor;

    public RecordType getRecordType() {
        return mRecordType;
    }

    public void setRecordType(RecordType mRecordType) {
        this.mRecordType = mRecordType;
    }

    public String getFEIN() {
        return mFEIN;
    }

    public void setFEIN(String mFEIN) {
        this.mFEIN = mFEIN;
    }

    public String getCustodialAccountId() {
        return mCustodialAccountId;
    }

    public void setCustodialAccountId(String mCustodialAccountId) {
        this.mCustodialAccountId = mCustodialAccountId;
    }

    public SpcfCalendar getEffectiveDate() {
        return mEffectiveDate;
    }

    public void setEffectiveDate(String mEffectiveDate) {
        SpcfCalendar date = SpcfCalendar.parse("MM/dd/yyyy", mEffectiveDate);
        date = SpcfCalendar.createInstance(date.getYear(), date.getMonth(), date.getDay(), SpcfTimeZone.getLocalTimeZone());
        this.mEffectiveDate = date;
    }

    public String getLegalName() {
        return mLegalName;
    }

    public void setLegalName(String mLegalName) {
        this.mLegalName = mLegalName;
    }

    public Boolean hasSafeHarbor() {
        return mHasSafeHarbor;
    }

    public void setHasSafeHarbor(Boolean mSafeHarbor) {
        this.mHasSafeHarbor = mSafeHarbor;
    }

}
