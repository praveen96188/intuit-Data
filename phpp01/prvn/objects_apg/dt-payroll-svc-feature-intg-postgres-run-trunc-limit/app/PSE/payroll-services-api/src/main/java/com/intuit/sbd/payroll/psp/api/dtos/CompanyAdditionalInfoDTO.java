package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.IndustryType;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

/**
 * Created by suganyas315 on 4/23/15.
 */
public class CompanyAdditionalInfoDTO {
    private String industry;
    private String ownership;

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String pIndustry) {
        industry = pIndustry;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }
}
