package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: kpaul
 * Date: Jun 23, 2009
 * Time: 2:02:40 AM
 */
public class SAPCompanyEventEmailParam {
    private String mParamType;
    private String mParamValue;

    public String getParamType() {
        return mParamType;
    }

    public void setParamType(String pParamType) {
        mParamType = pParamType;
    }

    public String getParamValue() {
        return mParamValue;
    }

    public void setParamValue(String pParamValue) {
        mParamValue = pParamValue;
    }
}
