package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 *
 * User: mvillani
 * Date: Aug 30, 2007
 * Time: 4:53:57 PM

 */
public class DeactivateEmployeeBankAccountCore extends Process implements IProcess {

    /**
     * Core process for deactivating a employee bank account.
     *
     * @author Marcela Villani
     */

    private EmployeeBankAccount mEmployeeBankAccount;
    private EmployeeBankAccountDTO mEmployeeBankAccountDTO;
    private Employee mEmployee;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mEmployeeId;


    public EmployeeBankAccount getEmployeeBankAccount() {
        return mEmployeeBankAccount;
    }

    public DeactivateEmployeeBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                             String pEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccountDTO) {
        mEmployeeBankAccountDTO = pEmployeeBankAccountDTO;
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEmployeeId = pEmployeeId;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        mEmployeeBankAccount.deactivate();
        if (processResult.isSuccess()) {
            processResult.setResult(Application.save(mEmployeeBankAccount));
        }
        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate DTO
        validationResult.merge(mEmployeeBankAccountDTO.validateEmployeeBankAccount());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists

        Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (! company.isAllowedCapability(SystemCapabilityCode.ChangeEmployeeBankAccount)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(), SystemCapabilityCode.ChangeEmployeeBankAccount.toString());
        }

        // Check if Employee Exists
        mEmployee = Employee.findEmployee(company, mEmployeeId);
        if (mEmployee == null) {
            validationResult.getMessages().EmployeeDoesNotExist(EntityName.Employee,
                    mEmployeeId, company.getSourceSystemCd().toString(), mSourceCompanyId, mEmployeeId);
        } else {
            // Checks if the bank account  exists for the employee and is active
            String sourceBankAccountId = mEmployeeBankAccountDTO.getEmployeeBankAccountId();
            mEmployeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(mEmployee, sourceBankAccountId);
            if (mEmployeeBankAccount == null) {
                validationResult.getMessages().EmployeeBankAccountNotFound(EntityName.EmployeeBankAccount, sourceBankAccountId, sourceBankAccountId, mEmployeeId);
            } else if (mEmployeeBankAccount.getStatusCd() != BankAccountStatus.Active) {
                validationResult.getMessages().EmployeeBankAccountNotActive(EntityName.EmployeeBankAccount, sourceBankAccountId, sourceBankAccountId, mEmployeeId);
            }
        }
        return validationResult;
    }
}
