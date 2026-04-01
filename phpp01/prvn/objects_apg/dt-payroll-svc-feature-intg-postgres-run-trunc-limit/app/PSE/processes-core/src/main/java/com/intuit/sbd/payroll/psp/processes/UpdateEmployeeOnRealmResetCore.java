package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.workflows.processflag.ProcessFlagWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.processflag.employee.EmployeeProcessFlagWorkflows;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateEmployeeOnRealmResetCore extends Process implements IProcess {

    private static final Logger logger = LoggerFactory.getLogger(UpdateEmployeeOnRealmResetCore.class);
    private Company company;
    private CompanyDTO mDtoCompany;

    public UpdateEmployeeOnRealmResetCore(Company company, CompanyDTO mDtoCompany) {
        this.company = company;
        this.mDtoCompany = mDtoCompany;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if(StringUtil.isNullOrEmpty(company.getSourceCompanyId())) {
            validationResult.getMessages()
                    .BadProcessArgument("PSID for company null or empty");
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        DomainEntitySet<Employee> employees = company.getEmployees();

        //Invalidating Employee Publish Status bit 3 for personaCheck on realm reset
        //Persona will not be present in new companyRealm so invalidating the persona check for these employees
        invalidateEmployeesInCompanyForPersonaCheck(employees);

        return processResult;
    }

    private void invalidateEmployeesInCompanyForPersonaCheck(DomainEntitySet<Employee> employees) {
        for( Employee employee : employees) {
            employee.setProcessFlagWorkflowState(EmployeeProcessFlagWorkflows.PERSONA_CHECK, ProcessFlagWorkflowState.INITIAL);
            Application.save(employee);
        }
    }
}
