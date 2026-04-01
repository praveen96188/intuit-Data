package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 1, 2009
 * Time: 11:46:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class ReactivateEmployeeCore extends Process implements IProcess {
    private Company company;
    private Employee mEmployee;
    private String mSourceCompanyId;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceEmployeeId;
    private DateDTO mReHireDate;

    public ReactivateEmployeeCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pSourceEmployeeId,
                                  DateDTO pReHireDate) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceEmployeeId = pSourceEmployeeId;
        this.mReHireDate = pReHireDate;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        // Reactivate the employee
        mEmployee.setStatusCd(EmployeeStatus.Active);
        mEmployee.setStatusEffectiveDate(PSPDate.getPSPTime());
        mEmployee.setReHireDate(DateDTO.convertToSpcfCalendar(mReHireDate));

        mEmployee = Application.save(mEmployee);

        processResult.setResult(mEmployee);
        if(company.isCompanyRequiredForOFACScreening()) {
            CompanyEvent.createChangeEmployeeCompanyEvent(company, EventTypeCode.EmployeeAdded, mEmployee.getId().toString());
        }
        return processResult;
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

        if (mReHireDate == null) {
            validationResult.getMessages().InvalidValue(EntityName.Date, "", "ReHire Date");
        } else {
            validationResult.merge(mReHireDate.validate());
        }
        return validationResult;
    }
}
