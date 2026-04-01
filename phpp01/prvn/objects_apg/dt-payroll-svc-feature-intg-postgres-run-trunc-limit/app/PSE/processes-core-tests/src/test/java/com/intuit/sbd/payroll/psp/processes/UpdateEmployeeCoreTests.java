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

import com.intuit.bp.wc.common.schema.Payroll;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyDDPlus401kDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeUpdateDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Contains the unit tests for the <CODE>UpdateEmployeeCore</CODE> class.
 *
 * @author: Sean Barenz
 * @version: August 29, 2007
 */
public class UpdateEmployeeCoreTests {
    private static String COMPANY_123456 = "123456";
    private static String COMPANY_INACTIVE = "123456Inactive";
    private static String COMPANY_HOLD = "123456OnHold";
    private static String COMPANY_PENDING_TERM = "123456PendingTermination";
    private static String COMPANY_TERMINATED = "123456Terminated";

    private EmployeeDTO employeeDTO;

    @Before
    public void runBeforeEachTest() {
        EmployeeUpdateDataLoader.before();
        EmployeeUpdateDataLoader.initialize();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updateEmployeeExistsActive() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setFirstName("UPDATEDFirst");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(pr);

        // verify the employee itself
        EmployeeUpdateDataLoader.validateEmployee(COMPANY_123456, employeeDTO, pr.getResult());
    }

    @Test
    public void updateEmployeeLastName() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setLastName("UPDATEDLast");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        // verify the employee itself
        EmployeeUpdateDataLoader.validateEmployee(COMPANY_123456, employeeDTO, pr.getResult());
    }

    @Test
    public void updateEmployeeId() {
        CompanyDDPlus401kDataLoader c401kdl = new CompanyDDPlus401kDataLoader();
        PayrollServices.beginUnitOfWork();
        c401kdl.persistQBCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8575577", SourceSystemCode.QBDT);
        Employee persistedEE = Employee.findEmployee(company, "401kEeId");
        EmployeeDTO employeeDTO = c401kdl.getEmployee1();
        employeeDTO.setExistingEmployeeGuid(persistedEE.getId().toString());
        employeeDTO.setEmployeeId("New401kEeIdUpdated");
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, "8575577", employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(pr);

        // verify the employee itself
        EmployeeUpdateDataLoader.validateEmployee("8575577", employeeDTO, pr.getResult());
    }

    @Test
    public void updateEmployeeMiddleName() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setMiddleName("UMI");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        // verify the employee itself
        EmployeeUpdateDataLoader.validateEmployee(COMPANY_123456, employeeDTO, pr.getResult());
    }

    @Test
    public void updateEmployeeTaxId() {
        EmployeeUpdateDataLoader.updateEmployeeTaxId();
    }

    @Test
    public void updateEmployeeNotExists() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("IDONTEXIST");
        employeeDTO.setSocialSecurityNumber("123456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "168", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee IDONTEXIST for company QBOE:" + COMPANY_123456 + " does not exist.", message.getMessage());
    }

    @Test
    public void updateCompanyNotSpecified() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setSocialSecurityNumber("123456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, null, employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Source Company ID is not specified.", message.getMessage());
    }

    @Test
    public void updateCompanyInactive() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setSocialSecurityNumber("123456789");

        DataLoadServices.setPrincipalToQBDT();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_INACTIVE, employeeDTO);
        PayrollServices.commitUnitOfWork();
        System.out.println(pr);

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate errors
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "168", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee TESTACTV for company QBOE:123456Inactive does not exist.", message.getMessage());
    }

    @Test
    public void updateCompanyOnHold() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setSocialSecurityNumber("123456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_HOLD, employeeDTO);
        PayrollServices.commitUnitOfWork();
        System.out.println(pr);

        // validate error count
        assertEquals("Number of Errors:", 2, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456OnHold in its current state.", message.getMessage());

        message = pr.getMessages().get(1);
        assertEquals("Error Code:", "168", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee TESTACTV for company QBOE:123456OnHold does not exist.", message.getMessage());
    }

    @Test
    public void updateCompanyPendingTermination() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setSocialSecurityNumber("123456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_PENDING_TERM, employeeDTO);
        PayrollServices.commitUnitOfWork();
        System.out.println(pr);

        // validate error count
        assertEquals("Number of Errors:", 2, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "1101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456PendingTermination in its current state.", message.getMessage());

        message = pr.getMessages().get(1);
        assertEquals("Error Code:", "168", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee TESTACTV for company QBOE:123456PendingTermination does not exist.", message.getMessage());
    }

    @Test
    public void updateCompanyTerminated() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setSocialSecurityNumber("123456789");

        DataLoadServices.setPrincipalToAgent();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_TERMINATED, employeeDTO);
        PayrollServices.commitUnitOfWork();
        System.out.println(pr);

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "168", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee TESTACTV for company QBOE:123456Terminated does not exist.", message.getMessage());
    }

    @Test
    public void updateCompanyNotExists() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();
        employeeDTO.setEmployeeId("TESTACTV");
        employeeDTO.setSocialSecurityNumber("123456789");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, "IDONTEXIST", employeeDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Company QBOE:IDONTEXIST does not exist.", message.getMessage());
    }

    @Test
    public void updateEmployeeNotSpecified() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "101", message.getMessageCode());
        assertEquals("Error Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Employee is not specified.", message.getMessage());
    }

    @Test
    public void updateEmployee_WagePlan() {

        DataLoadServices.setPSPDate(2013, 2, 21);
        List<Employee> employees = DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        for (Employee employee : employees) {
            assertEquals("Wage Plans", 1, employee.getEmployeeWagePlanCollection().size());
        }
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 2, 22);

        SpcfCalendar today = PSPDate.getPSPTime();

        Employee employee = employees.get(0);

        EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);

        //Update the first employee without any changes in wage plan
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Wage Plans", 1, employee.getEmployeeWagePlanCollection().size());
        EmployeeWagePlan employeeWagePlan = employee.getEmployeeWagePlanCollection().getFirst();
        assertNull("Invalid date", employeeWagePlan.getInvalidDate());
        assertTrue("Employee Wage plan modified date", employeeWagePlan.getModifiedDate().before(today));   // Modified date is not changed
        PayrollServices.rollbackUnitOfWork();


        employee = employees.get(1);

        employeeDTO = PayrollServices.dtoFactory.create(employee);

        //Update the first employee with new changes in wage plan
        PayrollServices.beginUnitOfWork();
        employeeDTO.getWagePlanDTOs().get(0).setWagePlanValue("New Value");
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Wage Plans", 2, employee.getEmployeeWagePlanCollection().size());
        assertEquals("Wage Plans", 1, employee.getEmployeeWagePlanCollection().find(EmployeeWagePlan.InvalidDate().isNull()).size());
        assertEquals("Wage Plans", 1, employee.getEmployeeWagePlanCollection().find(EmployeeWagePlan.InvalidDate().isNotNull()).size());
        EmployeeWagePlan invalidEmpWagePlan = employee.getEmployeeWagePlanCollection().find(EmployeeWagePlan.InvalidDate().isNull()).getFirst();
        EmployeeWagePlan validEmpWagePlan = employee.getEmployeeWagePlanCollection().find(EmployeeWagePlan.InvalidDate().isNotNull()).getFirst();

        assertTrue("Employee Wage plan modified date", invalidEmpWagePlan.getModifiedDate().after(today));   // Modified date is changed
        assertTrue("Employee Wage plan modified date", validEmpWagePlan.getModifiedDate().after(today));     // Modified date is changed

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void updateEmployee_PayrollItem() {

        DataLoadServices.setPSPDate(2013, 2, 21);
        List<Employee> employees = DataLoadServices.setupCompany("123456789");

        PayrollServices.beginUnitOfWork();
        for (Employee employee : employees) {
            assertEquals("Wage Plans", 1, employee.getEmployeeWagePlanCollection().size());
        }
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 2, 22);
        Employee employee = employees.get(0);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyPayrollItem> payItemResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, "123456789", DataLoadServices.createCompanyPayrollItem("10", PayrollItemCode.Salary));
        PayrollServices.commitUnitOfWork();

        assertSuccess(payItemResult);
        EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
        EmployeePayrollItemDTO empPayItemDTO = new EmployeePayrollItemDTO();
        empPayItemDTO.setAmount(10.0d);
        empPayItemDTO.setAmountType(QbdtNumericType.Percentage);
        empPayItemDTO.setPaylineType(PaylineType.Wage);
        empPayItemDTO.setPayrollItemId("10");
        List<EmployeePayrollItemDTO> empPayItemList = new ArrayList<EmployeePayrollItemDTO>();
        empPayItemList.add(empPayItemDTO);
        employeeDTO.setEmployeePayrollItemDTOs(empPayItemList);

        //Create the payroll item
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify payroll item created
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Payroll Items", 1, employee.getEmployeePayrollItemCollection().size());
        assertEquals("Payroll Item Amount", 10.0d, employee.getEmployeePayrollItemCollection().get(0).getAmount());
        PayrollServices.rollbackUnitOfWork();

        //Update the payroll item with no change in the payroll item
        PayrollServices.beginUnitOfWork();
        employee = employees.get(0);
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.getEmployeePayrollItemDTOs().get(0).setAmount(10.0d);
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify that the payroll item has not changed
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Payroll Items", 1, employee.getEmployeePayrollItemCollection().size());
        assertEquals("Payroll Item Amount", 10.0d, employee.getEmployeePayrollItemCollection().get(0).getAmount());
        PayrollServices.rollbackUnitOfWork();

        //Update the payroll item with changes
        PayrollServices.beginUnitOfWork();
        employee = employees.get(0);
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.getEmployeePayrollItemDTOs().get(0).setAmount(20.0d);
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify that the payroll item is updated
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Payroll Items", 1, employee.getEmployeePayrollItemCollection().size());
        assertEquals("Payroll Item Amount", 20.0d, employee.getEmployeePayrollItemCollection().get(0).getAmount());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void updateEmployee_PayrollItemArchived() {
        String psId="123456789";
        DataLoadServices.setPSPDate(2013, 2, 21);
        List<Employee> employees = DataLoadServices.setupCompany(psId,true,true);
        Company company=DataLoadServices.getCompanyNoEagerLoad(psId) ;
        PayrollServices.beginUnitOfWork();
        for (Employee employee : employees) {
            assertEquals("Wage Plans", 1, employee.getEmployeeWagePlanCollection().size());
        }
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 2, 22);
        Employee employee = employees.get(0);
        CompanyPayrollItemDTO companyPayrollItemDTO= DataLoadServices.createCompanyPayrollItem("10", PayrollItemCode.Salary);
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyPayrollItem> payItemResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, psId, companyPayrollItemDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(payItemResult);
         companyPayrollItemDTO= DataLoadServices.createCompanyPayrollItem("11", PayrollItemCode.DirectDeposit);
         qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        PayrollServices.beginUnitOfWork();
        payItemResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT, psId, companyPayrollItemDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(payItemResult);
        PayrollServices.beginUnitOfWork();
        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findDirectDepositPayrollItem(company);
        PayrollServices.rollbackUnitOfWork();
        assertNotNull("companyPayrollItem is null",companyPayrollItem);

        companyPayrollItemDTO= DataLoadServices.createCompanyPayrollItem("11", PayrollItemCode.DirectDeposit);
        companyPayrollItemDTO.setArchived(true);
        PayrollServices.beginUnitOfWork();
        payItemResult = PayrollServices.companyManager.addOrUpdateCompanyPayrollItem(SourceSystemCode.QBDT,psId , companyPayrollItemDTO);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        companyPayrollItem = CompanyPayrollItem.findDirectDepositPayrollItem(company);
        PayrollServices.rollbackUnitOfWork();
        assertNull("companyPayrollItem should be null",companyPayrollItem);

        PayrollServices.beginUnitOfWork();
        companyPayrollItem = CompanyPayrollItem.findDirectDepositPayrollItem(company,true);
        PayrollServices.rollbackUnitOfWork();
        assertNull("companyPayrollItem should be null",companyPayrollItem);

        PayrollServices.beginUnitOfWork();
        companyPayrollItem = CompanyPayrollItem.findDirectDepositPayrollItem(company,false);
        PayrollServices.rollbackUnitOfWork();
        assertNotNull("companyPayrollItem is null",companyPayrollItem);
    }

    @Test
    public void updateEmployee_TaxItems() {

        DataLoadServices.setPSPDate(2013, 2, 21);
        List<Employee> employees = DataLoadServices.setupCompany("123456789");
        Company company = employees.get(0).getCompany();

        DataLoadServices.setPSPDate(2013, 2, 22);
        Employee employee = employees.get(0);

        //Create employee tax item DTO
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "LA");
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        Application.refresh(employee);
        CompanyLaw companylaw = company.getCompanyAgencyCollection().get(0).getCompanyLawCollection().get(0);
        EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
        EmployeeTaxDTO employeeTaxDTO = new EmployeeTaxDTO();
        employeeTaxDTO.setAllowances(99);
        employeeTaxDTO.setCompanyLawId(companylaw.getSourceId());
        employeeTaxDTO.setState("LA");
        employeeTaxDTO.setTaxType(EmployeeTaxType.FIT);
        //Tax Table misc data
        Map<Integer, String> miscDataMap = new HashMap<Integer, String>();
        miscDataMap.put(20, "Test");
        employeeTaxDTO.setTaxTableMiscData(miscDataMap);
        List<EmployeeTaxDTO> empTaxList = new ArrayList<EmployeeTaxDTO>();
        empTaxList.add(employeeTaxDTO);
        employeeDTO.setEmployeeTaxDTOs(empTaxList);
        PayrollServices.commitUnitOfWork();
        //Create employee tax item
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify that tax item is created
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Tax Items", 1, employee.getEmployeeTaxCollection().size());
        assertEquals("Tax Allowances", 99, employee.getEmployeeTaxCollection().get(0).getAllowances());
        assertEquals("Tax Item Misc Data Count", 1, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().size());
        assertEquals("Tax Item Misc Data Order", 20, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getMiscDataOrder());
        assertEquals("Tax Item Misc Data Value", "Test", employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();

        //Update employee tax with no changes
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.getEmployeeTaxDTOs().get(0).setCompanyLawId(companylaw.getSourceId());
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify tax item is unchanged
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Tax Items", 1, employee.getEmployeeTaxCollection().size());
        assertEquals("Tax Allowances", 99, employee.getEmployeeTaxCollection().get(0).getAllowances());
        assertEquals("Tax Item Misc Data Count", 1, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().size());
        assertEquals("Tax Item Misc Data Order", 20, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getMiscDataOrder());
        assertEquals("Tax Item Misc Data Value", "Test", employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();

        //Update the tax item with changes to employee tax allowances
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.getEmployeeTaxDTOs().get(0).setCompanyLawId(companylaw.getSourceId());
        Map<Integer, String> updatedMiscDataMap = new HashMap<Integer, String>();
        updatedMiscDataMap.put(10, "Test2");
        employeeDTO.getEmployeeTaxDTOs().get(0).setAllowances(100);
        employeeDTO.getEmployeeTaxDTOs().get(0).setTaxTableMiscData(updatedMiscDataMap);
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify tax item is updated
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Tax Items", 1, employee.getEmployeeTaxCollection().size());
        assertEquals("Tax Allowances", 100, employee.getEmployeeTaxCollection().get(0).getAllowances());
        assertEquals("Tax Item Misc Data Count", 1, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().size());
        assertEquals("Tax Item Misc Data Order", 10, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getMiscDataOrder());
        assertEquals("Tax Item Misc Data Value", "Test2", employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();

        //Update the tax item with no changes to employee tax misc data
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.getEmployeeTaxDTOs().get(0).setCompanyLawId(companylaw.getSourceId());
        updatedMiscDataMap = new HashMap<Integer, String>();
        updatedMiscDataMap.put(10, "Test2");
        employeeDTO.getEmployeeTaxDTOs().get(0).setTaxTableMiscData(updatedMiscDataMap);
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify tax item is updated
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Tax Items", 1, employee.getEmployeeTaxCollection().size());
        assertEquals("Tax Allowances", 100, employee.getEmployeeTaxCollection().get(0).getAllowances());
        assertEquals("Tax Item Misc Data Count", 1, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().size());
        assertEquals("Tax Item Misc Data Order", 10, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getMiscDataOrder());
        assertEquals("Tax Item Misc Data Value", "Test2", employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();

        //Update the tax item with changes to employee tax misc data
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.getEmployeeTaxDTOs().get(0).setCompanyLawId(companylaw.getSourceId());
        updatedMiscDataMap = new HashMap<Integer, String>();
        updatedMiscDataMap.put(11, "Test3");
        employeeDTO.getEmployeeTaxDTOs().get(0).setTaxTableMiscData(updatedMiscDataMap);
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify tax item is updated
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Tax Items", 1, employee.getEmployeeTaxCollection().size());
        assertEquals("Tax Allowances", 100, employee.getEmployeeTaxCollection().get(0).getAllowances());
        assertEquals("Tax Item Misc Data Count", 1, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().size());
        assertEquals("Tax Item Misc Data Order", 11, employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getMiscDataOrder());
        assertEquals("Tax Item Misc Data Value", "Test3", employee.getEmployeeTaxCollection().get(0).getTaxTableMiscDataCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void updateEmployee_CustomItems() {

        DataLoadServices.setPSPDate(2013, 2, 21);
        List<Employee> employees = DataLoadServices.setupCompany("123456789");
        Company company = employees.get(0).getCompany();

        DataLoadServices.setPSPDate(2013, 2, 22);
        Employee employee = employees.get(0);

        //Create employee custom field DTO
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "LA");
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        Application.refresh(employee);
        EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
        EmployeeCustomFieldDTO employeeCustomFieldDTO = new EmployeeCustomFieldDTO();
        employeeCustomFieldDTO.setName("Other Info");
        employeeCustomFieldDTO.setOrder(10);
        employeeCustomFieldDTO.setValue("Value");
        List<EmployeeCustomFieldDTO> employeeCustomFieldDTOs = new ArrayList<EmployeeCustomFieldDTO>();
        employeeCustomFieldDTOs.add(employeeCustomFieldDTO);
        employeeDTO.setEmployeeCustomFields(employeeCustomFieldDTOs);
        PayrollServices.commitUnitOfWork();
        //Create employee custom fields
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify that tax item is created
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Custom Field", 1, employee.getEmployeeCustomFieldCollection().size());
        assertEquals("Custom Field Name", "Other Info", employee.getEmployeeCustomFieldCollection().get(0).getName());
        assertEquals("Custom Field Order", 10, employee.getEmployeeCustomFieldCollection().get(0).getFieldOrder());
        assertEquals("Custom Field Value", "Value", employee.getEmployeeCustomFieldCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();

        //Update employee custom field with no changes
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify employee custom field is unchanged
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Custom Field", 1, employee.getEmployeeCustomFieldCollection().size());
        assertEquals("Custom Field Name", "Other Info", employee.getEmployeeCustomFieldCollection().get(0).getName());
        assertEquals("Custom Field Order", 10, employee.getEmployeeCustomFieldCollection().get(0).getFieldOrder());
        assertEquals("Custom Field Value", "Value", employee.getEmployeeCustomFieldCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();

        //Update the custom field with changes
        PayrollServices.beginUnitOfWork();
        employee = employees.get(0);
        Application.refresh(employee);
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.getEmployeeCustomFields().get(0).setName("Other Info2");
        employeeDTO.getEmployeeCustomFields().get(0).setOrder(20);
        result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        PayrollServices.commitUnitOfWork();
        //Verify custom field is updated
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals("Custom Field", 1, employee.getEmployeeCustomFieldCollection().size());
        assertEquals("Custom Field Name", "Other Info2", employee.getEmployeeCustomFieldCollection().get(0).getName());
        assertEquals("Custom Field Order", 20, employee.getEmployeeCustomFieldCollection().get(0).getFieldOrder());
        assertEquals("Custom Field Value", "Value", employee.getEmployeeCustomFieldCollection().get(0).getValue());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void updateEmployeeEventCheckForQBOE() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        // Set old employee to a standard value
        employeeDTO.setEmployeeId("TESTACTV");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();
        
        assertTrue("Test Result:", pr.isSuccess());
        
        employeeDTO.setStatusCd(EmployeeStatus.Inactive);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setStatusCd(EmployeeStatus.Active);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());
        
        employeeDTO.setSocialSecurityNumber("333-22-4444");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());
        
        employeeDTO.setFirstName("UPDATEDFirst");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());
        
        employeeDTO.setLastName("UPDATEDLast");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());
        
        employeeDTO.setMiddleName("UPDATEDMiddle");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setBirthDate(new DateDTO(1916, 12, 31));
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());
        
        employeeDTO.setLiveAddress(createSampleAddressDTO());
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());
        
        // verify the employee itself
        EmployeeUpdateDataLoader.validateEmployee(COMPANY_123456, employeeDTO, pr.getResult());

        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO.getEmployeeId(), new DateDTO("2016-01-01"));
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.reactivateEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO.getEmployeeId(), new DateDTO("2016-01-02"));
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.deleteEmployee(SourceSystemCode.QBOE, COMPANY_123456, employeeDTO.getEmployeeId());
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE), EventTypeCode.EmployeeAdded);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE), EventTypeCode.EmployeeDeleted);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateEmployeeEventCheckForNonDD() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        Company company=null;
        PayrollServices.beginUnitOfWork();
        company=Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE);
        company.setSourceSystemCd(SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();

        // Set old employee to a standard value
        employeeDTO.setEmployeeId("TESTACTV");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setStatusCd(EmployeeStatus.Inactive);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setStatusCd(EmployeeStatus.Active);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setSocialSecurityNumber("333-22-4444");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setFirstName("UPDATEDFirst");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setLastName("UPDATEDLast");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setMiddleName("UPDATEDMiddle");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setBirthDate(new DateDTO(1916, 12, 31));
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO.setLiveAddress(createSampleAddressDTO());
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        // verify the employee itself
        EmployeeUpdateDataLoader.validateEmployee(COMPANY_123456, employeeDTO, pr.getResult());

        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO.getEmployeeId(), new DateDTO("2016-01-01"));
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.reactivateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO.getEmployeeId(), new DateDTO("2016-01-02"));
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.deleteEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO.getEmployeeId());
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeAdded);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeDeleted);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateEmployeeEventCheck() {
        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        Company company=null;
        PayrollServices.beginUnitOfWork();
        company=Company.findCompany(COMPANY_123456, SourceSystemCode.QBOE);
        company.setSourceSystemCd(SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollServices.commitUnitOfWork();

        // Initial event counts
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeAdded);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeDeleted);
        assertEquals("Company Events", 0, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        // Set old employee to a standard value
        employeeDTO.setEmployeeId("TESTACTV");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Employee> pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 1, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setStatusCd(EmployeeStatus.Inactive);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeDeleted);
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Detail Code", 1,
                    companyEventsList.get(0).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
        assertEquals("EventDetail Employee mismatch", companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId), Employee.findEmployee(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), employeeDTO.getEmployeeId()).getId().toString());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setStatusCd(EmployeeStatus.Active);
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeAdded);
        assertEquals("Company Events", 1, companyEventsList.size());
        assertEquals("Event Detail Code", 1,
                    companyEventsList.get(0).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
        assertEquals("EventDetail Employee mismatch", companyEventsList.get(0).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId), Employee.findEmployee(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), employeeDTO.getEmployeeId()).getId().toString());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setSocialSecurityNumber("333-22-4444");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 1, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setFirstName("UPDATEDFirst");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 2, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setLastName("UPDATEDLast");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 3, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setMiddleName("UPDATEDMiddle");
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 4, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setBirthDate(new DateDTO(1916, 12, 31));
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        assertEquals("Company Events", 5, companyEventsList.size());
        PayrollServices.commitUnitOfWork();

        employeeDTO.setLiveAddress(createSampleAddressDTO());
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.updateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        // verify the employee itself
        EmployeeUpdateDataLoader.validateEmployee(COMPANY_123456, employeeDTO, pr.getResult());
        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeUpdated);
        //This accounts for the two other employees created in the BeforeClass
        assertEquals("Company Events", 6, companyEventsList.size());
        for(int i=0;i<6;i++){
            assertEquals("Event Detail Code", 1,
                    companyEventsList.get(i).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
            assertEquals("Event Detail Value", Employee.findEmployee(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), employeeDTO.getEmployeeId()).getId().toString(), companyEventsList.get(i).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId));
        }
        PayrollServices.commitUnitOfWork();

        employeeDTO = EmployeeUpdateDataLoader.getTestEmployee();

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.addEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        String employeeID=Employee.findEmployee(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), employeeDTO.getEmployeeId()).getId().toString();

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO.getEmployeeId(), new DateDTO("2016-01-01"));
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.reactivateEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO.getEmployeeId(), new DateDTO("2016-01-02"));
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.employeeManager.deleteEmployee(SourceSystemCode.QBDT, COMPANY_123456, employeeDTO.getEmployeeId());
        PayrollServices.commitUnitOfWork();

        assertTrue("Test Result:", pr.isSuccess());

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeAdded);
        assertEquals("Company Events", 3, companyEventsList.size());
        int j=0;
        for(int i=0;i<3;i++) {
            assertEquals("Event Detail Code", 1,
                    companyEventsList.get(i).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
            if(companyEventsList.get(i).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId).equals(employeeID))
            {
                j++;
            }
        }
        assertEquals("Number of events created", 2, j);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(Company.findCompany(COMPANY_123456, SourceSystemCode.QBDT), EventTypeCode.EmployeeDeleted);
        assertEquals("Company Events", 3, companyEventsList.size());
        j=0;
        for(int i=0;i<3;i++) {
            assertEquals("Event Detail Code", 1,
                    companyEventsList.get(i).getCompanyEventDetails(EventDetailTypeCode.EmployeeId).size());
            if(companyEventsList.get(i).getCompanyEventDetailValue(EventDetailTypeCode.EmployeeId).equals(employeeID))
            {
                j++;
            }
        }
        assertEquals("Number of events created", 2, j);
        PayrollServices.commitUnitOfWork();
    }

    private AddressDTO createSampleAddressDTO() {
        AddressDTO pAddressDTO=new AddressDTO();
        pAddressDTO.setAddressLine1("A");
        pAddressDTO.setAddressLine2("B");
        pAddressDTO.setAddressLine3("C");
        pAddressDTO.setCity("D");
        pAddressDTO.setState("E");
        pAddressDTO.setCountry("F");
        pAddressDTO.setZipCode("G");
        pAddressDTO.setZipCodeExtension("H");
        return pAddressDTO;
    }
}
