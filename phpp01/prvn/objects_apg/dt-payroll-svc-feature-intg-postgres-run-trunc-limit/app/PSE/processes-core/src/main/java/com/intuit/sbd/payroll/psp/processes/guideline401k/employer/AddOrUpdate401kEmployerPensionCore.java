package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.amazonaws.util.StringUtils;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domainsecondary.*;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.v4.GlobalId;
import com.intuit.v4.payroll.employer.EmployerPension;

import java.util.Objects;

public class AddOrUpdate401kEmployerPensionCore extends Process {

    private final EmployerPension employerPension;
    private final Company company;

    private Hcm401kCompanyPolicy hcm401kCompanyPolicy;

    public AddOrUpdate401kEmployerPensionCore(Company company, EmployerPension employerPension) {
        this.employerPension = employerPension;
        this.company = company;
    }

    @Override
    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        if(Objects.isNull(employerPension)){
            validationResult.getMessages()
                    .BadProcessArgument("EmployerPension");
            return validationResult;
        }

        if(StringUtils.isNullOrEmpty(employerPension.getStatutoryPensionPolicy())){
            validationResult.getMessages()
                    .BadProcessArgument("EmployerPension.StatutoryPensionPolicy");
            return validationResult;
        }

        //update entity
        if(employerPension.isIdSet()) {
                validationResult.getMessages()
                        .UpdateUnsupportedForEmployerEntity("employer Pension cannot be updated");
            return validationResult;
        }
        if(Objects.isNull(company)) {
            validationResult.getMessages().
                    BadProcessArgument("pspCompany");
            return validationResult;
        } else{
            hcm401kCompanyPolicy = new Hcm401kCompanyPolicy();
            hcm401kCompanyPolicy.setCompanyId(company.getId().toString());
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {

        DeductionItemPolicy deductionItemPolicy = DeductionItemPolicy.getDeductionItemPolicyByName(employerPension.getStatutoryPensionPolicy());
        hcm401kCompanyPolicy.setActive(true);
        hcm401kCompanyPolicy.setHcm401kPolicy(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(deductionItemPolicy, DeductionItemProvider.Guideline));
        ApplicationSecondary.save(hcm401kCompanyPolicy);

        ProcessResult processResult = new ProcessResult();
        processResult.setResult(getEmployerPensionFromDb());

        return processResult;
    }

    private EmployerPension getEmployerPensionFromDb() {
        EmployerPension employerPensionFromDb = new EmployerPension();
        employerPensionFromDb.setId(GlobalId.create("",hcm401kCompanyPolicy.getId().toString()));
        DeductionItemPolicy deductionItemPolicy = hcm401kCompanyPolicy.getHcm401kPolicy().getDeductionItemPolicy();
        employerPensionFromDb.setName(Hcm401kPolicy.getHcm401kPolicyByDeductionItemAndProvider(
                deductionItemPolicy, DeductionItemProvider.Guideline).getDescription());
        employerPensionFromDb.setStatutoryPensionPolicy(
                DeductionItemPolicy.getStatutoryPolicyName(deductionItemPolicy.name()));
        return employerPensionFromDb;
    }
}
