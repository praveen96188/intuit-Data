package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.PaylineType;
import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 8, 2010
 * Time: 12:37:18 PM
 */
public class EmployeePayrollItemDTO {
    private double mAmount;
    private QbdtNumericType mAmountType;
    private double mItemLimit;
    private QbdtNumericType mLimitType;
    private PaylineType mPaylineType;
    private String mPayrollItemId;
    private int mOrder;

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double pAmount) {
        mAmount = pAmount;
    }

    public QbdtNumericType getAmountType() {
        return mAmountType;
    }

    public void setAmountType(QbdtNumericType pAmountType) {
        mAmountType = pAmountType;
    }

    public double getItemLimit() {
        return mItemLimit;
    }

    public void setItemLimit(double pLimit) {
        mItemLimit = pLimit;
    }

    public QbdtNumericType getLimitType() {
        return mLimitType;
    }

    public void setLimitType(QbdtNumericType pLimitType) {
        mLimitType = pLimitType;
    }

    public PaylineType getPaylineType() {
        return mPaylineType;
    }

    public void setPaylineType(PaylineType pPaylineType) {
        mPaylineType = pPaylineType;
    }

    public String getPayrollItemId() {
        return mPayrollItemId;
    }

    public void setPayrollItemId(String pPayrollItemId) {
        mPayrollItemId = pPayrollItemId;
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int pOrder) {
        mOrder = pOrder;
    }
}
