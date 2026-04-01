package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.PayItemCode;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: mwaqarbaig
 * Date: Nov 4, 2010
 * Time: 2:42:00 PM
 */
public class PayItemDTO {
    private PayItemCode mPayItemCode;
    private SpcfMoney mAmount;
    private String mEmployeeId;
    private DateDTO mEffectiveDate;

    public PayItemCode getPayItemCode() {
        return mPayItemCode;
    }

    public void setPayItemCode(PayItemCode pPayItemCode) {
        mPayItemCode = pPayItemCode;
    }

    public SpcfMoney getAmount() {
        return mAmount;
    }

    public void setAmount(SpcfMoney pAmount) {
        mAmount = pAmount;
    }

    public String getEmployeeId() {
        return mEmployeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        mEmployeeId = pEmployeeId;
    }

    public DateDTO getEffectiveDate() {
        return mEffectiveDate;
    }

    public void setEffectiveDate(DateDTO pEffectiveDate) {
        mEffectiveDate = pEffectiveDate;
    }
}
