package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 1/10/13
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddServiceViewMyPaycheck extends Process implements IProcess {
    public AddServiceViewMyPaycheck() {
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
