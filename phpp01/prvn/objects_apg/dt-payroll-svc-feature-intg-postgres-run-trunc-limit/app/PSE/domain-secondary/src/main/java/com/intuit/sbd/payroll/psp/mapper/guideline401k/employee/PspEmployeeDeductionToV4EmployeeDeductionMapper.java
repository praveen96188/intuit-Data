package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.domainsecondary.exception.PolicyDescriptionNotFoundException;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.GlobalId;
import com.intuit.v4.common.Amount;
import com.intuit.v4.payroll.employee.EmployeeDeduction;
import com.intuit.v4.payroll.employer.EmployerDeduction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PspEmployeeDeductionToV4EmployeeDeductionMapper extends
        BeanMapper<Hcm401kEmployeeDeduction, EmployeeDeduction> {

    private final EmployeeDeductionAmountHelper employeeDeductionAmountHelper;

    @Autowired
    public PspEmployeeDeductionToV4EmployeeDeductionMapper(EmployeeDeductionAmountHelper employeeDeductionAmountHelper) {
        this.employeeDeductionAmountHelper = employeeDeductionAmountHelper;
    }

    @Override
    public EmployeeDeduction mapToTarget(Hcm401kEmployeeDeduction hcm401kEmployeeDeduction,
            Class<EmployeeDeduction> t) throws PolicyDescriptionNotFoundException{

        // since we are mapping only a specific type of deduction
        // we only want to serve Loan repayment as a v4 employee deduction
        EmployeeDeduction v4EmployeeDeduction = new EmployeeDeduction();

        v4EmployeeDeduction.setId(GlobalId.create("", hcm401kEmployeeDeduction.getId().toString()));

        v4EmployeeDeduction.setActive(hcm401kEmployeeDeduction.getActive());

        v4EmployeeDeduction.setAmount(employeeDeductionAmountHelper.createEmployeeDeductionAmount(
                hcm401kEmployeeDeduction));

        v4EmployeeDeduction.setAnnualMaximum(Amount.valueOf(hcm401kEmployeeDeduction.getMaxAmount()));

        v4EmployeeDeduction.setEmployerDeduction(createEmployerDeductionObject(
                hcm401kEmployeeDeduction.getHcm401kCompanyPolicy()));
        return v4EmployeeDeduction;
    }

    private EmployerDeduction createEmployerDeductionObject(Hcm401kCompanyPolicy hcm401kCompanyPolicy) {
        EmployerDeduction employer401kDeduction = new EmployerDeduction();
        employer401kDeduction.setId(GlobalId.create("",hcm401kCompanyPolicy.getId().toString()));
        DeductionItemPolicy deductionItemPolicy = hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy();
        employer401kDeduction.setStatutoryDeductionPolicy(
                DeductionItemPolicy.getStatutoryPolicyName(deductionItemPolicy.name()));
        employer401kDeduction.setName(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(deductionItemPolicy, DeductionItemProvider.Guideline).getDescription());
        return employer401kDeduction;
    }
}
