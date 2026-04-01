/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kEmployeeInfoDTO;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company401kDataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contains the unit tests for the <CODE>UpdateEmployee401kCore</CODE> process.
 *
 * @author: Dawn Martens
 * @version: December 21, 2009
 */
public class UpdateEmployee401kCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void update401kEmployeeCoreNullDTO() {
        PayrollServices.beginUnitOfWork();
        Company401kDataloader c1401kDL = new Company401kDataloader();
        c1401kDL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        UpdateEmployee401k proc = new UpdateEmployee401k(SourceSystemCode.QBDT, "1234567", null);
        ProcessResult procResult = proc.execute();
        assertFalse(procResult.isSuccess());
        assertEquals("Number of messages",1,procResult.getMessages().size());
        Message message = procResult.getMessages().get(0);
        assertEquals("Message text","Employee401kDTO has invalid value",message.getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void update401kEmployeeCoreNullCompanyParams() {
        PayrollServices.beginUnitOfWork();
        Company401kDataloader c1401kDL = new Company401kDataloader();
        c1401kDL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        UpdateEmployee401k proc = new UpdateEmployee401k(SourceSystemCode.QBDT, null, null);
        ProcessResult procResult = proc.execute();
        assertFalse(procResult.isSuccess());
        assertEquals("Number of messages",1,procResult.getMessages().size());
        Message message = procResult.getMessages().get(0);
        assertEquals("Message text","Source Company ID is not specified.",message.getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void update401kEmployeeCoreNullCompanyDNE() {
        PayrollServices.beginUnitOfWork();
        Company401kDataloader c1401kDL = new Company401kDataloader();
        c1401kDL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EmployeeDTO updatedEE = c1401kDL.getEmployeeDTO();
        ThirdParty401kEmployeeInfoDTO tp401kEEInfo = new ThirdParty401kEmployeeInfoDTO();
        tp401kEEInfo.setEmail("updatedEmail@dawn.com");
        tp401kEEInfo.setPhoneNumber("888-999-9987");
        updatedEE.setBirthDate(new DateDTO("1988-08-08"));
        updatedEE.setTerminationDate(new DateDTO("2009-08-08"));
        tp401kEEInfo.setFamilyMember(true);
        tp401kEEInfo.setHighlyCompensatedEmployee(true);
        tp401kEEInfo.setOwnershipPercent(new BigDecimal("59"));
        updatedEE.setEmployee401kInfo(tp401kEEInfo);
        updatedEE.setFirstName("Lamb");
        ProcessResult procResult =PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, "12345", updatedEE);
        assertFalse(procResult.isSuccess());
        assertEquals("Number of messages",1,procResult.getMessages().size());
        Message message = procResult.getMessages().get(0);
        assertEquals("Message text","Company QBDT:12345 does not exist.",message.getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void update401kEmployeeCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company401kDataloader c1401kDL = new Company401kDataloader();
        c1401kDL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany("1234567", SourceSystemCode.QBDT);
        Employee existingEE = Employee.findEmployee(foundCompany, "EE1");
        EmployeeDTO updatedEE = c1401kDL.getEmployeeDTO();
        ThirdParty401kEmployeeInfoDTO tp401kEEInfo = new ThirdParty401kEmployeeInfoDTO();
        tp401kEEInfo.setEmail("updatedEmail@dawn.com");
        tp401kEEInfo.setPhoneNumber("888-999-9987");
        updatedEE.setBirthDate(new DateDTO("1988-08-08"));
        updatedEE.setTerminationDate(new DateDTO("2009-08-08"));
        tp401kEEInfo.setFamilyMember(true);
        tp401kEEInfo.setHighlyCompensatedEmployee(true);
        tp401kEEInfo.setOwnershipPercent(new BigDecimal("59"));
        updatedEE.setEmployee401kInfo(tp401kEEInfo);
        updatedEE.setFirstName("Lamb");
        updatedEE.setLastName("Lamberson");
        updatedEE.setSocialSecurityNumber("389756434");
        updatedEE.setExistingEmployeeGuid(existingEE.getId().toString());
        updatedEE.setEmployeeId("NewSourceEmployeeId");
        ProcessResult pr =PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, "1234567", updatedEE);
        assertSuccess(pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany("1234567", SourceSystemCode.QBDT);
        Employee foundEmployee = Employee.findEmployee(foundCompany, "NewSourceEmployeeId");

        assertNotNull(foundEmployee);
        assertNotNull(foundEmployee.getThirdParty401kInfo());
        DataLoadServices.assertEmployeesEqual(updatedEE, foundEmployee);
        PayrollServices.commitUnitOfWork();
    }



    @Test
    public void testUpdate401kEE_CloudDDBP401k() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));        
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment, ServiceCode.ThirdParty401k);        

        DataLoadServices.runOffload(PSPDate.getPSPTime());
        DataLoadServices.runACHTransactionProcessor();

        List<Employee> employees = DataLoadServices.addEEs(company, 1, false, true);
        List<CompanyPayrollItem> companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.create401kPayrollRun(employees, companyPayrollItems);
        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit 401k Payroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DTOFactory dtoFactory = new DTOFactory();
        EmployeeDTO updatedEE = dtoFactory.create(employees.get(0));
        // Update Employee Core process will remove wage plan DTOs if matching ones are found. So keeping the original request asserting.
        EmployeeDTO originalUpdatedEEInput = dtoFactory.create(employees.get(0));
        ThirdParty401kEmployeeInfoDTO tp401kEEInfo = new ThirdParty401kEmployeeInfoDTO();
        tp401kEEInfo.setEmail("updatedEmail@dawn.com");
        tp401kEEInfo.setPhoneNumber("888-999-9987");
        tp401kEEInfo.setFamilyMember(true);
        tp401kEEInfo.setHighlyCompensatedEmployee(true);
        tp401kEEInfo.setOwnershipPercent(new BigDecimal("59"));

        updatedEE.setBirthDate(new DateDTO("1988-08-08"));
        updatedEE.setTerminationDate(new DateDTO("2009-08-08"));
        updatedEE.setEmployee401kInfo(tp401kEEInfo);
        updatedEE.setFirstName("Lamb");

        originalUpdatedEEInput.setBirthDate(new DateDTO("1988-08-08"));
        originalUpdatedEEInput.setTerminationDate(new DateDTO("2009-08-08"));
        originalUpdatedEEInput.setEmployee401kInfo(tp401kEEInfo);
        originalUpdatedEEInput.setFirstName("Lamb");

        ProcessResult updateResult = PayrollServices.employeeManager.updateEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), updatedEE);
        PSP_PRAssert.assertSuccess("update 401k Employee", updateResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        Employee employee = Employee.findEmployee(company, updatedEE.getEmployeeId());
        DataLoadServices.assertEmployeesEqual(originalUpdatedEEInput, employee);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult cancelServiceCore = PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.ThirdParty401k);
        PSP_PRAssert.assertSuccess("deactivate 401k service", cancelServiceCore);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService companyService = company.getService(ServiceCode.ThirdParty401k);
        assertEquals("canceled 401k service", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        cancelServiceCore = PayrollServices.companyManager.deactivateService(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PSP_PRAssert.assertSuccess("deactivate DD service", cancelServiceCore);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyService = company.getService(ServiceCode.DirectDeposit);
        assertEquals("canceled DD service", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        employee = Employee.findEmployee(company, updatedEE.getEmployeeId());

        updatedEE = dtoFactory.create(employee);
        // Update Employee Core process will remove wage plan DTOs if matching ones are found. So keeping the original request asserting.
        originalUpdatedEEInput = dtoFactory.create(employee);

        tp401kEEInfo = new ThirdParty401kEmployeeInfoDTO();
        tp401kEEInfo.setEmail("updatedEmail2@dawn.com");
        tp401kEEInfo.setPhoneNumber("888-999-9987");
        tp401kEEInfo.setFamilyMember(true);
        tp401kEEInfo.setHighlyCompensatedEmployee(true);
        tp401kEEInfo.setOwnershipPercent(new BigDecimal("59"));

        updatedEE.setBirthDate(new DateDTO("1988-08-08"));
        updatedEE.setTerminationDate(new DateDTO("2009-08-08"));
        updatedEE.setEmployee401kInfo(tp401kEEInfo);
        updatedEE.setFirstName("The Dude");

        originalUpdatedEEInput.setBirthDate(new DateDTO("1988-08-08"));
        originalUpdatedEEInput.setTerminationDate(new DateDTO("2009-08-08"));
        originalUpdatedEEInput.setEmployee401kInfo(tp401kEEInfo);
        originalUpdatedEEInput.setFirstName("The Dude");

        updateResult = PayrollServices.employeeManager.updateEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), updatedEE);
        PSP_PRAssert.assertSuccess("update 401k Employee", updateResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        employee = Employee.findEmployee(company, updatedEE.getEmployeeId());
        DataLoadServices.assertEmployeesEqual(originalUpdatedEEInput, employee);
        PayrollServices.rollbackUnitOfWork();
    }
}
