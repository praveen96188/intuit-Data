package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.managers.IEmployeeManager;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.*;
import com.intuit.sbd.payroll.psp.processes.Workforce.EmployeeChunkWorkforceCore;
import com.intuit.sbd.payroll.psp.processes.datamanager.DGAuthBasedDeleteEmployeeProcessCore;
import com.intuit.sbd.payroll.psp.processes.guideline401k.employee.AddOrUpdate401kEmployeeDeductionCore;
import com.intuit.sbd.payroll.psp.processes.guideline401k.employee.AddOrUpdate401kEmployeePensionCore;
import com.intuit.sbd.payroll.psp.processes.wallet.WalletCreateCore;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.v4.payroll.employee.EmployeeDeduction;
import com.intuit.v4.payroll.employee.EmployeePension;

import java.util.List;

/**
 * @author achaves
 *         Date: Nov 7, 2007
 *         Time: 10:19:33 PM
 */
class EmployeeManager implements IEmployeeManager {
    public ProcessResult<Employee> addEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee) {
        return addEmployee(pSourceSystemCode, pSourceCompanyId, pEmployee, null, false);
    }

    public ProcessResult<Employee> addEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee, String pTransmissionId) {
        return addEmployee(pSourceSystemCode, pSourceCompanyId, pEmployee, pTransmissionId, false);
    }

    public ProcessResult<Employee> addEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee, String pTransmissionId, boolean ignoreDuplicates) {
        IProcess processCore = new AddEmployeeCore(pSourceSystemCode, pSourceCompanyId, pEmployee, pTransmissionId, ignoreDuplicates);
        return processCore.execute();
    }

    public ProcessResult<Employee> updateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee) {
        return updateEmployee(pSourceSystemCode, pSourceCompanyId, pEmployee, null);
    }

    public ProcessResult<Employee> updateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee, String pTransmissionId) {
        IProcess processCore = new UpdateEmployeeCore(pSourceSystemCode, pSourceCompanyId, pEmployee, pTransmissionId);
        return processCore.execute();
    }

    public ProcessResult<Employee> deactivateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId, DateDTO pTerminationDate) {
        IProcess processCore = new DeactivateEmployeeCore(pSourceSystemCode, pSourceCompanyId, pSourceEmployeeId, pTerminationDate);
        return processCore.execute();
    }

    // EmployeeBankAccount
    public ProcessResult<EmployeeBankAccount> addEmployeeBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccount) {
        IProcess processCore = new AddEmployeeBankAccountCore(pSourceSystemCode, pSourceCompanyId, pEmployeeId, pEmployeeBankAccount);
        return processCore.execute();
    }

    public ProcessResult<EmployeeBankAccount> updateEmployeeBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccount) {
        IProcess processCore = new UpdateEmployeeBankAccountCore(pSourceSystemCode, pSourceCompanyId, pEmployeeId, pEmployeeBankAccount);
        return processCore.execute();
    }

    public ProcessResult<EmployeeBankAccount> deactivateEmployeeBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccount) {
        IProcess processCore = new DeactivateEmployeeBankAccountCore(pSourceSystemCode, pSourceCompanyId, pEmployeeId, pEmployeeBankAccount);
        return processCore.execute();
    }
    
    public ProcessResult createWallet(EmployeeBankAccount employeeBankAccount) {
        return new WalletCreateCore(employeeBankAccount).execute();
    }

    public ProcessResult<Employee> reactivateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId, DateDTO pReHireDate) {
        IProcess processCore = new ReactivateEmployeeCore(pSourceSystemCode, pSourceCompanyId, pSourceEmployeeId, pReHireDate);
        return processCore.execute();
    }

    public ProcessResult<Employee> deleteEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId) {
        IProcess processCore = new DeleteEmployeeCore(pSourceSystemCode, pSourceCompanyId, pSourceEmployeeId);
        return processCore.execute();
    }

    public ProcessResult<Employee> updateEmployeeAccrual(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee) {
        IProcess processCore = new UpdateEmployeeAccrual(pSourceSystemCode, pSourceCompanyId, pEmployee);
        return processCore.execute();
    }

    public ProcessResult<Employee> removeConsumerRealmId(String pEmpId) {
        return new RemoveConsumerRealmId(pEmpId).execute();
    }

    @Override
    public ProcessResult<String> disassociateEmployeeConsumerRealm(String mEmployeeId, String workOrderId, String workOrderCreatedTime) {
        return new DGAuthBasedDeleteEmployeeProcessCore(mEmployeeId, workOrderId, workOrderCreatedTime).execute();
    }

    public ProcessResult<Integer> employeeChunkInvite(List<SpcfUniqueId> partitionedEmployeeIdSubList, Company company, IAMTicket iamTicket, boolean isResend, String emailTemplateName, String invitationSource) {
        return new EmployeeChunkWorkforceCore(partitionedEmployeeIdSubList, company, iamTicket, isResend, emailTemplateName, invitationSource).execute();
    }

    @Override
    public ProcessResult addOrUpdate401kEmployeeDeduction(EmployeeDeduction employeeDeduction) {
        return new AddOrUpdate401kEmployeeDeductionCore(employeeDeduction).execute();
    }

    @Override
    public ProcessResult addOrUpdate401kEmployeePension(EmployeePension employeePension) {
        return new AddOrUpdate401kEmployeePensionCore(employeePension).execute();
    }
}