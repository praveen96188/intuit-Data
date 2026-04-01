package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.AccrualPeriod;
import com.intuit.sbd.payroll.psp.domain.AccrualType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 8, 2010
 * Time: 12:42:34 PM
 */
public class EmployeeAccrualDTO {
    private AccrualPeriod mAccrualPeriod;
    private double mHoursPerPeriod;
    private double mHours;
    private double mMaxHours;
    private boolean mNewYearReset;
    private AccrualType mAccrualType;

    public AccrualPeriod getAccrualPeriod() {
        return mAccrualPeriod;
    }

    public void setAccrualPeriod(AccrualPeriod pAccrualPeriod) {
        mAccrualPeriod = pAccrualPeriod;
    }

    public double getHoursPerPeriod() {
        return mHoursPerPeriod;
    }

    public void setHoursPerPeriod(double pHoursPerPeriod) {
        mHoursPerPeriod = pHoursPerPeriod;
    }

    public double getHours() {
        return mHours;
    }

    public void setHours(double pHours) {
        mHours = pHours;
    }

    public double getMaxHours() {
        return mMaxHours;
    }

    public void setMaxHours(double pMaxHours) {
        mMaxHours = pMaxHours;
    }

    public boolean isNewYearReset() {
        return mNewYearReset;
    }

    public void setNewYearReset(boolean pNewYearReset) {
        mNewYearReset = pNewYearReset;
    }

    public AccrualType getAccrualType() {
        return mAccrualType;
    }

    public void setAccrualType(AccrualType pAccrualType) {
        mAccrualType = pAccrualType;
    }
}
