package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.CDCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Core process for adding the cloud service to an existing company.
 *
 * @author Jeff Jones
 */
public class AddServiceCloud extends Process implements IProcess {

    private Company company;

    public AddServiceCloud(Company pCompany) {
        company = pCompany;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();        

        return validationResult;
    }

    public ProcessResult process() {
        CompanyService companyService = new CompanyService();

        ProcessResult processResult = new ProcessResult();
        processResult.setResult(companyService);
        processResult.setSuccess(true);
        return processResult;
    }

}
