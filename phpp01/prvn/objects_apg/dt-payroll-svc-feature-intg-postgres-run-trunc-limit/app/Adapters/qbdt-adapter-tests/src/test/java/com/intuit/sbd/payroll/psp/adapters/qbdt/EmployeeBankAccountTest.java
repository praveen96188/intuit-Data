package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.EmployeeBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Jun 19, 2009
 * Time: 3:27:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeBankAccountTest {
    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }
    
    @Test
    public void testCompanyEventsForEBA() {

        // Load Data
        PayrollServices.beginUnitOfWork();
        Company company = EmployeeBankAccountDataLoader.LoadEmployeeBankAccounts(1, 0, "Active", null);
        Employee employee = company.getDirectDepositEmployees().get(0);
        EmployeeBankAccountDTO ebaDTO = GenerateData.getEmployeeBankAccountDTO("EBATest");
        PayrollServices.commitUnitOfWork();

        // Test no 1
        PayrollServices.beginUnitOfWork();
        ProcessResult<EmployeeBankAccount> processResult = PayrollServices.employeeManager.addEmployeeBankAccount
                (company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), ebaDTO);

        // Verify that no validation errors have been returned
        assertSuccess("addEmployeeBankAccount", processResult);

        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = PayrollServices.entityFinder.findById(Employee.class, employee.getId());

        // Verify that employeeBankAccount has been saved
        EmployeeBankAccount ebaNew = EmployeeBankAccount.findEmployeeBankAccount(employee, "EBATest");
        PayrollServices.rollbackUnitOfWork();
        assertTrue("Employee Bank Account:", ebaNew != null);

        PayrollServices.beginUnitOfWork();
        // Verify that Event is created in Company_Event table
        DomainEntitySet<CompanyEvent> compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountChange);
        Assert.assertTrue("Number of New EBA events", 1 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        DomainEntitySet<CompanyEventDetail> compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.EmployeeBankAccountChange, EventDetailTypeCode.NewEmployeeBankAccountId, ebaNew.getId().getStandardFormatString());
        assertTrue("Number of New EBA change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.EmployeeBankAccountChange, EventDetailTypeCode.OldEmployeeBankAccountId);
        assertTrue("Number of Old EBA change events", 1 == compEventDetails.size());
        PayrollServices.rollbackUnitOfWork();

        /** Test no 2*/
        /* Same Source_Bank_ID with diff bank acc details via addEmployeeBankAccount().
        *  Exp o/p : Should fail with EBA not found exception*/
        PayrollServices.beginUnitOfWork();
        ebaDTO = GenerateData.getEmployeeBankAccountDTO("EBATest");
        processResult = PayrollServices.employeeManager.addEmployeeBankAccount
                (company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), ebaDTO);

        // Verify that no  validation errors have been returned
        assertFalse("Employee Bank Account:", processResult.isSuccess());

        // Verify that no New Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountChange);
        Assert.assertTrue("Number of New EBA events", 1 == compEvents.size());

        PayrollServices.rollbackUnitOfWork();

        /** Test no 3*/
        /* Same Source_Bank_ID with diff bank acc details via updateEmployeeBankAccount().
        *  Exp o/p : Inactivate old & Activate new EBA and create EBA Change event */
        PayrollServices.beginUnitOfWork();
        ebaDTO = GenerateData.getEmployeeBankAccountDTO("EBATest");
        processResult = PayrollServices.employeeManager.updateEmployeeBankAccount
                (company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), ebaDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("Employee Bank Account:", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Verify that Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountChange);
        Assert.assertTrue("Total Number of EBA change events", 2 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        Application.refresh(employee);
        String oldID = ebaNew.getId().getStandardFormatString();
        ebaNew = EmployeeBankAccount.findEmployeeBankAccount(employee, ebaDTO.getBankAccount().getAccountNumber(), ebaDTO.getBankAccount().getRoutingNumber());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.EmployeeBankAccountChange, EventDetailTypeCode.OldEmployeeBankAccountId, oldID);
        assertTrue("Number of EBA change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.EmployeeBankAccountChange, EventDetailTypeCode.NewEmployeeBankAccountId, ebaNew.getId().getStandardFormatString());
        assertTrue("Number of EBA change events", 1 == compEventDetails.size());
        PayrollServices.rollbackUnitOfWork();

        /** Test no 4*/
        /* Diff Source_Bank_ID with diff bank acc details.
        *  Exp o/p : Activate old & new EBA and create New EBA event */
        PayrollServices.beginUnitOfWork();
        ebaDTO = GenerateData.getEmployeeBankAccountDTO("EBATestNew");
        processResult = PayrollServices.employeeManager.addEmployeeBankAccount
                (company.getSourceSystemCd(), company.getSourceCompanyId(), employee.getSourceEmployeeId(), ebaDTO);

        // Verify that no  validation errors have been returned
        assertSuccess("Employee Bank Account:", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Verify that Event is created in Company_Event table
        compEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.EmployeeBankAccountChange);
        Assert.assertTrue("Total no of First EBA events", 3 == compEvents.size());

        // Verify that Event is created in Company_Event_Detail table
        Application.refresh(employee);
        ebaNew = EmployeeBankAccount.findEmployeeBankAccount(employee, ebaDTO.getBankAccount().getAccountNumber(), ebaDTO.getBankAccount().getRoutingNumber());
        compEventDetails = CompanyEvent.findCompanyEventDetails
                (company, EventTypeCode.EmployeeBankAccountChange, EventDetailTypeCode.NewEmployeeBankAccountId, ebaNew.getId().getStandardFormatString());
        assertTrue("Number of EBA change events", 1 == compEventDetails.size());
        compEventDetails = CompanyEvent.findCompanyEventDetails(company, EventTypeCode.EmployeeBankAccountChange, EventDetailTypeCode.OldEmployeeBankAccountId);
        assertTrue("Total Number of Old EBA change events", 3 == compEventDetails.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testInvalidRoutingNumber() {
        try {
            OFXDataloader ofxDataloader = new OFXDataloader();
            OFX happyPathOfxObj= ofxDataloader.loadHappyPathOFXPayroll1();
            happyPathOfxObj.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().get(0).getIDDLINE().get(0).getIDDACCT().getBANKACCTTO().setBANKID("111111111");
            String ofxStr = OFXManager.javaToOFX(happyPathOfxObj,OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
            QBDTTestHelper.processRequestPayrollErrorDynamicTransmissionError(ofxStr,ErrorMessages.InvalidEERoutingNumber("111111111","Donovan McNabb"));
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            fail(e.getMessage());
        }
    }

    @Test
    public void testEmployeeBankAccountChange() {
        DataLoadServices.setPSPDate(2012, 7, 1);
        String psid = "123456798";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        for (Iterator<Employee> iterator = employees.iterator(); iterator.hasNext(); ) {
            Employee next = iterator.next();
            if(next.getEmployeeBankAccountCollection().size() == 0) {
                iterator.remove();
            }
        }
        Employee employee = assertOne(employees);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 7, 17), employees);
        PayrollServices.rollbackUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> bankAccountChangeEvents = Application.find(CompanyEvent.class, CompanyEvent.EventTypeCd().equalTo(EventTypeCode.EmployeeBankAccountChange));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        employee = Application.refresh(employee);
        EmployeeBankAccount eba1 = assertOne(employee.getEmployeeBankAccountCollection().find(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active)));
        PayrollServices.rollbackUnitOfWork();

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                // add a character to the bank accounts so that new accounts will be created
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setAccountNumber(ddTransactionDTO.getEmployeeBankAccount().getBankAccount().getAccountNumber() + "1");
            }
        }

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        employee = Application.refresh(employee);
        EmployeeBankAccount eba2 = assertOne(employee.getEmployeeBankAccountCollection().find(EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active)));
        DomainEntitySet<CompanyEvent> newBankAccountChangeEvents = Application.find(CompanyEvent.class, CompanyEvent.EventTypeCd().equalTo(EventTypeCode.EmployeeBankAccountChange));
        for (Iterator<CompanyEvent> iterator = newBankAccountChangeEvents.iterator(); iterator.hasNext(); ) {
            CompanyEvent next = iterator.next();
            if(bankAccountChangeEvents.contains(next)) {
                iterator.remove();
            }
        }
        
        assertTrue("", newBankAccountChangeEvents.size() == 2);
        CompanyEvent employeeBankAccountChangeEvent = newBankAccountChangeEvents.get(1);
        assertOne(employeeBankAccountChangeEvent.getCompanyEventDetailCollection().find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.NewEmployeeBankAccountId).And(CompanyEventDetail.Value().equalTo(eba2.getId().toString()))));
        assertOne(employeeBankAccountChangeEvent.getCompanyEventDetailCollection().find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.OldEmployeeBankAccountId).And(CompanyEventDetail.Value().equalTo(eba1.getId().toString()))));
        PayrollServices.rollbackUnitOfWork();
    }

}
