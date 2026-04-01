package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kEmployeeDeduction;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.common.Amount;
import com.intuit.v4.payroll.definitions.BasePensionItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PspEmployeeDeductionToV4ContributionSetupMapper extends BeanMapper<Hcm401kEmployeeDeduction, BasePensionItem.ContributionSetup> {

    private final EmployeeDeductionAmountHelper employeeDeductionAmountHelper;

    @Autowired
    public PspEmployeeDeductionToV4ContributionSetupMapper(EmployeeDeductionAmountHelper employeeDeductionAmountHelper) {
        this.employeeDeductionAmountHelper = employeeDeductionAmountHelper;
    }

    @Override
    public BasePensionItem.ContributionSetup mapToTarget(Hcm401kEmployeeDeduction hcm401kEmployeeDeduction, Class<BasePensionItem.ContributionSetup> t) {

        BasePensionItem.ContributionSetup contributionSetup = new BasePensionItem.ContributionSetup();
        contributionSetup.setAmount(employeeDeductionAmountHelper.createEmployeeDeductionAmount(hcm401kEmployeeDeduction));
        contributionSetup.setCappings(createEmployeeDeductionCappings(hcm401kEmployeeDeduction, contributionSetup));
        return contributionSetup;
    }

    private List<BasePensionItem.CappingType> createEmployeeDeductionCappings(Hcm401kEmployeeDeduction hcm401kEmployeeDeduction, BasePensionItem.ContributionSetup contributionSetup) {
        List<BasePensionItem.CappingType> cappings = new ArrayList<>();
        cappings.add(createEmployeeDeductionCappingType(hcm401kEmployeeDeduction));
        return cappings;
    }

    private BasePensionItem.CappingType createEmployeeDeductionCappingType(Hcm401kEmployeeDeduction hcm401kEmployeeDeduction) {
        BasePensionItem.CappingType cappingType = new BasePensionItem.CappingType();
        cappingType.setAmount(Amount.valueOf(hcm401kEmployeeDeduction.getMaxAmount()));
        cappingType.setFrequency(BasePensionItem.CappingType.FrequencyEnum.BYFISCALYEAR);
        return cappingType;
    }
}
