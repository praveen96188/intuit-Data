package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheck;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * User: sriramn394
 */
public class ReactivateServiceWorkersComp extends Process implements IProcess {

    private CompanyService companyService;

    public ReactivateServiceWorkersComp(CompanyService companyService) {
        this.companyService = companyService;
    }

    public ProcessResult validate() {
        ProcessResult processResult = new ProcessResult();
        return processResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = AddServiceWorkersComp.createWorkersCompPaychecks(companyService);
        return processResult;
    }
}
