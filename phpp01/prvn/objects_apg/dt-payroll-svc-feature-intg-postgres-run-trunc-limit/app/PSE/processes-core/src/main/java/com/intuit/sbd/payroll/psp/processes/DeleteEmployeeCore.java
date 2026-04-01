package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 1, 2009
 * Time: 12:01:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteEmployeeCore extends Process implements IProcess {
    private Company company;
    private String mSourceCompanyId;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceEmployeeId;
    private Employee mEmployee;

    public DeleteEmployeeCore(SourceSystemCode mSourceSystemCd, String mSourceCompanyId, String mSourceEmployeeId) {
        this.mSourceSystemCd = mSourceSystemCd;
        this.mSourceCompanyId = mSourceCompanyId;
        this.mSourceEmployeeId = mSourceEmployeeId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // validate company parameters
        ProcessResult result = Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId);
        if (!result.isSuccess()) {
            return result;
        }

        // Validate Source Employee Id
        if (mSourceEmployeeId == null) {
            validationResult.getMessages().EmployeeIdNotSpecified(
                    EntityName.Employee, mSourceEmployeeId);
            return validationResult;
        }

        // Validate Company Exists
        company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(
                    EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Validate Employee Exists
        mEmployee = Employee.findEmployee(company, mSourceEmployeeId);
        if (mEmployee == null) {
            validationResult.getMessages().EmployeeDoesNotExist(
                    EntityName.Employee,
                    mSourceEmployeeId,
                    mSourceSystemCd.toString(),
                    mSourceCompanyId,
                    mSourceEmployeeId);

            return validationResult;
        }

        //Validate if Employee has any paychecks
        DomainEntitySet<Paycheck> paychecks = Paycheck
                .findPaychecksBySourceEmployee(company, mEmployee);
        
        if (paychecks.size() > 0) {
            validationResult.getMessages().EmployeeHasPaychecks(EntityName.Employee,
                    mSourceEmployeeId, mSourceSystemCd.toString(), mSourceCompanyId, mSourceEmployeeId);
        }

        if (mEmployee.getQbdtEmployeeInfo() != null) {
            validationResult.getMessages().CannotDeleteDDEmployees(EntityName.Employee, mSourceEmployeeId,
                                                                   mSourceSystemCd.toString(), mSourceCompanyId, mSourceEmployeeId);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        for(EmployeeWagePlan wagePlan : mEmployee.getEmployeeWagePlanCollection()){
            Application.delete(wagePlan);
        }

        for (EmployeeAccrual employeeAccrual : mEmployee.getEmployeeAccrualCollection()) {
            Application.delete(employeeAccrual);
        }

        for (EmployeeBankAccount employeeBankAccount : mEmployee.getEmployeeBankAccountCollection()) {
            Application.delete(employeeBankAccount);
        }

        for (EmployeeTax employeeTax : mEmployee.getEmployeeTaxCollection()) {
            Application.delete(employeeTax);
        }

        for (EmployeeCustomField employeeCustomField : mEmployee.getEmployeeCustomFieldCollection()) {
            Application.delete(employeeCustomField);
        }

        for (EmployeePayrollItem employeePayrollItem : mEmployee.getEmployeePayrollItemCollection()) {
            Application.delete(employeePayrollItem);
        }

        if(mEmployee.getMailingAddress() != null) {
            Application.delete(mEmployee.getMailingAddress());
        }

        if(mEmployee.getQbdtEmployeeInfo() != null) {
            Application.delete(mEmployee.getQbdtEmployeeInfo());
        }
        if(company.isCompanyRequiredForOFACScreening()) {
            CompanyEvent.createChangeEmployeeCompanyEvent(company, EventTypeCode.EmployeeDeleted, mEmployee.getId().toString());
        }
        Application.delete(mEmployee);
        return processResult;
    }
}
