package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;

/**
 * Created with IntelliJ IDEA.
 * User: srikanthm180
 * Date: 3/6/13
 * Time: 1:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddServiceCloudV2 extends Process implements IProcess {

    private Company company;

    public AddServiceCloudV2(Company pCompany) {
        company = pCompany;
    }

    /**
     * Validation step.
     */
    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        return validationResult;
    }

    /**
     * Processing step.
     */
    @Override
    public ProcessResult process() {
        CompanyService companyService = new CompanyService();
        ProcessResult processResult = new ProcessResult();
        processResult.setResult(companyService);
        processResult.setSuccess(true);
        return processResult;
    }
}
