package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.WagePlanDomainCode;
import com.intuit.sbd.payroll.psp.domain.WagePlanNameCode;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 10, 2009
 * Time: 3:17:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class WagePlanDTO {
    private String wagePlanValue;
    private WagePlanDomainCode domainCode;
    private String state;
    private WagePlanNameCode name;
    private String description;
    private String rulesVersion;

    public String getWagePlanValue() {
        return wagePlanValue;
    }

    public void setWagePlanValue(String wagePlanValue) {
        this.wagePlanValue = wagePlanValue;
    }

    public WagePlanDomainCode getDomainCode() {
        return domainCode;
    }

    public void setDomainCode(WagePlanDomainCode domainCode) {
        this.domainCode = domainCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public WagePlanNameCode getName() {
        return name;
    }

    public void setName(WagePlanNameCode name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String pDescription) {
        description = pDescription;
    }

    public String getRulesVersion() {
        return rulesVersion;
    }

    public void setRulesVersion(String pRulesVersion) {
        rulesVersion = pRulesVersion;
    }
}
