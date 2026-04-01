package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * User: michaelp696
 */
public class AddServiceWorkersComp extends Process implements IProcess {
    private CompanyService companyService;
    private ServiceInfoDTO serviceInfoDto;

    public AddServiceWorkersComp(CompanyService companyService, ServiceInfoDTO serviceInfoDto) {
        this.companyService = companyService;
        this.serviceInfoDto = serviceInfoDto;
    }

    public ProcessResult validate() {
        ProcessResult processResult = new ProcessResult();
        return processResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        this.companyService.setServiceStartDate(serviceInfoDto.getServiceStartDate());
        return processResult;
    }

    public static ProcessResult createWorkersCompPaychecks(CompanyService companyService) {
        ProcessResult processResult = new ProcessResult();
        Expression<Paycheck> query = new Query<Paycheck>()
                .Where(Paycheck.PayrollRun().Company().equalTo(companyService.getCompany())
                               .And(Paycheck.SourceEmployee().isNotNull())
                               .And(Paycheck.Status().equalTo(PaycheckStatusCode.Active))
                               .And(Paycheck.PayrollRun().PaycheckDate().greaterOrEqualThan(companyService.getServiceStartDate())))
                .EagerLoad(Paycheck.PayrollRun());
        DomainEntitySet<Paycheck> paychecks = PayrollServices.entityFinder.find(Paycheck.class, query);
        for (Paycheck paycheck : paychecks) {
            WorkersCompPaycheck.createWorkersCompPaycheck(paycheck);
        }
        return processResult;
    }
}
