package com.intuit.sbd.payroll.psp.processes.guideline401k.employer;

import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyPolicy;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyQbdtPitem;
import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kDeductionContributor;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

public class Add401kCompanyQBDTPItemCore extends Process {

    private final Hcm401kCompanyPolicy hcm401kCompanyPolicy;
    private final String employerPItemId;
    private final String employeePItemId;

    public Add401kCompanyQBDTPItemCore(Hcm401kCompanyPolicy hcm401kCompanyPolicy,
                                       String employerPItemId,
                                       String employeePItemId){
        this.hcm401kCompanyPolicy = hcm401kCompanyPolicy;
        this.employerPItemId = employerPItemId;
        this.employeePItemId = employeePItemId;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if(Objects.isNull(hcm401kCompanyPolicy)){
            validationResult.getMessages()
                    .BadProcessArgument("Hcm401kCompanyPolicy");
            return validationResult;
        }

        if(StringUtils.isEmpty(employeePItemId)){
            validationResult.getMessages()
                    .BadProcessArgument("employeePItemId");
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        Hcm401kCompanyQbdtPitem hcm401kCompanyQbdtPitemER = new Hcm401kCompanyQbdtPitem();
        Hcm401kCompanyQbdtPitem hcm401kCompanyQbdtPitemEE = new Hcm401kCompanyQbdtPitem();

        if(!employerPItemId.isEmpty()){
            hcm401kCompanyQbdtPitemER.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);
            hcm401kCompanyQbdtPitemER.setHcm401kContributor(Hcm401kDeductionContributor.Employer);
            hcm401kCompanyQbdtPitemER.setQbdtPitemId(employerPItemId);
            ApplicationSecondary.save(hcm401kCompanyQbdtPitemER);
        }

        hcm401kCompanyQbdtPitemEE.setHcm401kCompanyPolicy(hcm401kCompanyPolicy);
        hcm401kCompanyQbdtPitemEE.setHcm401kContributor(Hcm401kDeductionContributor.Employee);
        hcm401kCompanyQbdtPitemEE.setQbdtPitemId(employeePItemId);
        ApplicationSecondary.save(hcm401kCompanyQbdtPitemEE);

        processResult.setResult(hcm401kCompanyQbdtPitemEE);

        return processResult;
    }
}
