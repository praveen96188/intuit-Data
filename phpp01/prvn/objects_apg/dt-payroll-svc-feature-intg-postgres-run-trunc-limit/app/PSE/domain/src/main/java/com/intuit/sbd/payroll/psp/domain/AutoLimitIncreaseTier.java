package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;


/**
 * This class is not mapped to the DB via Hibernate.  It is the representation of a set of records in the
 * PSP_SOURCE_PAYROLL_PARAMETER table that make up a logic 'tier' for determining how to apply an auto-increase to
 * a company's DD limits.
 */
public class AutoLimitIncreaseTier {
    private String mLevel;
    private SourceSystemCode mSourceSystemCd;
    private int mPayrollsRun;
    private int mDaysSinceFirstPayroll;
    private SpcfDecimal mIncreaseMultiplier;
    private SpcfMoney mCompanyCap;
    private SpcfMoney mPayeeCap;

    public String getLevel() {
        return mLevel;
    }

    public void setLevel(String level) {
        mLevel = level;
    }

    public SourceSystemCode getSourceSystemCd() {
        return mSourceSystemCd;
    }

    public void setSourceSystemCd(SourceSystemCode sourceSystemCd) {
        mSourceSystemCd = sourceSystemCd;
    }

    public int getPayrollsRun() {
        return mPayrollsRun;
    }

    public void setPayrollsRun(int payrollsRun) {
        mPayrollsRun = payrollsRun;
    }

    public int getDaysSinceFirstPayroll() {
        return mDaysSinceFirstPayroll;
    }

    public void setDaysSinceFirstPayroll(int daysSinceFirstPayroll) {
        mDaysSinceFirstPayroll = daysSinceFirstPayroll;
    }

    public SpcfDecimal getIncreaseMultiplier() {
        return mIncreaseMultiplier;
    }

    public void setIncreaseMultiplier(SpcfDecimal increaseMultiplier) {
        mIncreaseMultiplier = increaseMultiplier;
        if (mIncreaseMultiplier != null)
            mIncreaseMultiplier = mIncreaseMultiplier.setScale(2);
    }

    public SpcfMoney getCompanyCap() {
        return mCompanyCap;
    }

    public void setCompanyCap(SpcfMoney companyCap) {
        mCompanyCap = companyCap;
        if (mCompanyCap != null)
            mCompanyCap = new SpcfMoney(mCompanyCap.setScale(2));
    }

    public SpcfMoney getPayeeCap() {
        return mPayeeCap;
    }

    public void setPayeeCap(SpcfMoney payeeCap) {
        mPayeeCap = payeeCap;
        if (mPayeeCap != null)
            mPayeeCap = new SpcfMoney(mPayeeCap.setScale(2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutoLimitIncreaseTier that = (AutoLimitIncreaseTier) o;

        if (mDaysSinceFirstPayroll != that.mDaysSinceFirstPayroll) return false;
        if (mPayrollsRun != that.mPayrollsRun) return false;
        if (!mCompanyCap.equals(that.mCompanyCap)) return false;
        if (!mPayeeCap.equals(that.mPayeeCap)) return false;
        if (!mIncreaseMultiplier.equals(that.mIncreaseMultiplier)) return false;
        if (!mLevel.equals(that.mLevel)) return false;
        if (mSourceSystemCd != that.mSourceSystemCd) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mLevel.hashCode();
        result = 31 * result + mSourceSystemCd.hashCode();
        result = 31 * result + mPayrollsRun;
        result = 31 * result + mDaysSinceFirstPayroll;
        result = 31 * result + mIncreaseMultiplier.hashCode();
        result = 31 * result + mCompanyCap.hashCode();
        result = 31 * result + mPayeeCap.hashCode();
        return result;
    }
}