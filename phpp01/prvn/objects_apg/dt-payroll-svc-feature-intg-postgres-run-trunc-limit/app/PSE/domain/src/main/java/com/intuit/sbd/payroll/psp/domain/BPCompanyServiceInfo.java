package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * Hand-written business logic
 */
public class BPCompanyServiceInfo extends BaseBPCompanyServiceInfo {

    /**
     * Default constructor.
     */
    public BPCompanyServiceInfo() {
        super();
    }

    public SpcfMoney getCompanyLimit() {
        if (this.getOverrideCompanyLimitAmount() != null) {
            return this.getOverrideCompanyLimitAmount();
        } else {
            return new SpcfMoney(LimitRule.findLimitRule(getCompany(), getService().getServiceCd()).findLimitValueByName(LimitValueType.DefaultCompanyLimit).getValue());

        }
    }

    public SpcfMoney getPayeeLimit() {
        if (this.getOverridePayeeLimitAmount() != null) {
            return this.getOverridePayeeLimitAmount();
        } else {
            return new SpcfMoney(LimitRule.findLimitRule(getCompany(), getService().getServiceCd()).findLimitValueByName(LimitValueType.DefaultEmployeeLimit).getValue());
        }
    }
}