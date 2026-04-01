package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.v4.payroll.employee.EmployeeDeduction;
import com.intuit.v4.payroll.employee.EmployeePension;

import java.util.List;

/**
 * This is the PSP service API that deals with all employee related information
 * <p>The API includes:
 *   <p>Add/Update/Deactivate employees
 *   <p>Add/Update/Deactivate bank accounts for an employee
 */

public interface IEmployeeManager {
    ProcessResult<Employee> addEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee);
    ProcessResult<Employee> addEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee, String pTransmissionId);

    ProcessResult<Employee> addEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee, String pTransmissionId, boolean ignoreDuplicates);
    ProcessResult<Employee> updateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee);
    ProcessResult<Employee> updateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee, String pTransmissionId);

    ProcessResult<Employee> deactivateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId, DateDTO pTerminationDate);

    ProcessResult<EmployeeBankAccount> addEmployeeBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccount);

    ProcessResult<EmployeeBankAccount> updateEmployeeBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccount);

    ProcessResult<EmployeeBankAccount> deactivateEmployeeBankAccount(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccount);

    ProcessResult createWallet(EmployeeBankAccount employeeBankAccount);

    ProcessResult<Employee> reactivateEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId, DateDTO pReHireDate);

    ProcessResult<Employee> deleteEmployee(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pSourceEmployeeId);

    ProcessResult<Employee> updateEmployeeAccrual(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, EmployeeDTO pEmployee);

    ProcessResult<Employee> removeConsumerRealmId(String pEmpId);

    ProcessResult<String> disassociateEmployeeConsumerRealm(String mEmployeeId, String workOrderId, String workOrderCreatedTime);

    ProcessResult<Integer> employeeChunkInvite(List<SpcfUniqueId> partitionedEmployeeIdSubList, Company company, IAMTicket iamTicket, boolean isResend, String emailTemplateName, String invitationSource);

    ProcessResult addOrUpdate401kEmployeeDeduction(EmployeeDeduction employeeDeduction);

    ProcessResult addOrUpdate401kEmployeePension(EmployeePension employeePension);
}
