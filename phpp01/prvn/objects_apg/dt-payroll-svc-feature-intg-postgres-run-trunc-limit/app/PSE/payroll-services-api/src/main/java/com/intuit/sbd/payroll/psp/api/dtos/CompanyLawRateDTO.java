package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 12, 2010
 * Time: 2:45:28 PM
 */
public class CompanyLawRateDTO {
    private double mRate;
    private DateDTO mEffectiveDate;
    private QbdtNumericType mRateType;

    /**
     * Initializes the CompanyLawRateDTO object.
     * Default rate type is "Percentage"
     */
    public CompanyLawRateDTO(){
        //Fixed Rates were introduced with PSP-3156 but the default rate type is always percentage
        mRateType = QbdtNumericType.Percentage;
    }

    public double getRate() {
        return mRate;
    }

    public void setRate(double pRate) {
        mRate = pRate;
    }

    public DateDTO getEffectiveDate() {
        return mEffectiveDate;
    }

    public void setEffectiveDate(DateDTO pEffectiveDate) {
        mEffectiveDate = pEffectiveDate;
    }

    public QbdtNumericType getRateType() {
        return mRateType;
    }

    public void setRateType(QbdtNumericType pRateType) {
        mRateType = pRateType;
    }

    public String toString() {
        return "Rate: " + getRate() + " Effective Date: " + getEffectiveDate();
    }
}
