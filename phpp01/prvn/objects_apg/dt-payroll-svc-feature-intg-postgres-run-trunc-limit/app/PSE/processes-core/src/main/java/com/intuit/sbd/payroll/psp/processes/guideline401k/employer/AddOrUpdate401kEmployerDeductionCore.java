package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.amazonaws.util.StringUtils;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.GlobalId;
import com.intuit.v4.payroll.employer.EmployerDeduction;

import java.util.Objects;

public class AddOrUpdate401kEmployerDeductionCore extends Process {

    private final Company company;
    private final EmployerDeduction employerDeduction;

    private Hcm401kCompanyPolicy hcm401kCompanyPolicy;

    public AddOrUpdate401kEmployerDeductionCore(Company company, EmployerDeduction employerDeduction) {

        this.employerDeduction = employerDeduction;
        this.company = company;
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        if(Objects.isNull(employerDeduction)){
            validationResult.getMessages()
                    .BadProcessArgument("EmployerDeduction");
            return validationResult;
        }

        if(StringUtils.isNullOrEmpty(employerDeduction.getStatutoryDeductionPolicy())){
            validationResult.getMessages()
                    .BadProcessArgument("EmployerDeduction.StatutoryDeductionPolicy");
            return validationResult;
        }
        if(employerDeduction.isIdSet()) {
                validationResult.getMessages()
                        .UpdateUnsupportedForEmployerEntity("Employer Deduction cannot be updated");
            return validationResult;
        }
        if(Objects.isNull(company)) {
            validationResult.getMessages()
                    .BadProcessArgument("pspCompany");
            return validationResult;
        } else{
            hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
            hcm401kCompanyPolicy.setCompanyId(company.getId().toString());
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {

        DeductionItemPolicy deductionItemPolicy = DeductionItemPolicy.getDeductionItemPolicyByName(employerDeduction.getStatutoryDeductionPolicy());
        hcm401kCompanyPolicy.setActive(true);
        hcm401kCompanyPolicy.setHcm401kPolicy(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(deductionItemPolicy, DeductionItemProvider.Guideline));
        ApplicationSecondary.save(hcm401kCompanyPolicy);

        ProcessResult processResult = new ProcessResult();
        processResult.setResult(getEmployerDeductionFromDb());
        return processResult;
    }

    private EmployerDeduction getEmployerDeductionFromDb() {
        EmployerDeduction employerDeductionFromDb = new EmployerDeduction();
        employerDeductionFromDb.setId(GlobalId.create("",hcm401kCompanyPolicy.getId().toString()));
        DeductionItemPolicy deductionItemPolicy = hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy();
        employerDeductionFromDb.setName(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(
                deductionItemPolicy, DeductionItemProvider.Guideline).getDescription());
        employerDeductionFromDb.setStatutoryDeductionPolicy(
                DeductionItemPolicy.getStatutoryPolicyName(deductionItemPolicy.name()));
        return employerDeductionFromDb;
    }
}
