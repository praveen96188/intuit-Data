package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.processes.AddEmployeeReturnTransferTransaction;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company123272727DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;


/**
 * User: mvillani
 * Date: Aug 16, 2007
 * Time: 7:12:03 AM
 */
public class FinancialTransactionBETests {

   @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }                           

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void findEmployeeFinTxnByPayrollRunId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions = null;
        //Test case to test the financial transactions for the company  and payroll run id
        financialTransactions = FinancialTransaction.
                findEmployeeFinancialTransactions(company, "BatchId01", null, null, null,
                        null, null);
        assertEquals("Number of Financial Transactions:", 2, financialTransactions.size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());

            assertEquals("PayRoll Run Id ", payrollRunDTO.getPayrollTXBatchId(),
                    financialTransaction.getPayrollRun().getSourcePayRunId());
        }

        Application.commitUnitOfWork();

    }

    @Test
    public void findEmployeeFinTxnByPayrollRunIdAndEmployee() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        Employee employee = Employee.findEmployee(company, "Emp1");

        DomainEntitySet<FinancialTransaction> financialTransactions = null;

        //Test case to test the financial transactions for the company , payroll run id & employee
        financialTransactions = FinancialTransaction.
                findEmployeeFinancialTransactions(company, "BatchId01", employee, null, null,
                        null, null);

        assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());

            EmployeeBankAccount employeeBankAccount = financialTransaction.getEmployeeBankAccount();

            assertEquals("Employee Id:", employee.getSourceEmployeeId(),
                    employeeBankAccount.getEmployee().getSourceEmployeeId());
        }

        Application.commitUnitOfWork();

    }


    @Test
    public void findEmpFinTxnByPayrollRunIdAndEmpBankAccount() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        Employee employee = Employee.findEmployee(company, "Emp1");

        EmployeeBankAccount inputEmployeeBankAccount = EmployeeBankAccount
                .findEmployeeBankAccount(employee, "Emp1Acct1");

        DomainEntitySet<FinancialTransaction> financialTransactions = null;

        //Test case to test the financial transactions for the company , payroll run id , employee,
        //employee bank account
        financialTransactions = FinancialTransaction.
                findEmployeeFinancialTransactions(company, "BatchId01", employee, inputEmployeeBankAccount, null,
                        null, null);

        assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());

            EmployeeBankAccount employeeBankAccount = financialTransaction.getEmployeeBankAccount();


            assertEquals("Employee Id:", employee.getSourceEmployeeId(),
                    employeeBankAccount.getEmployee().getSourceEmployeeId());

            assertEquals("Employee Bank Account :", inputEmployeeBankAccount.getSourceBankAccountId(),
                    employeeBankAccount.getSourceBankAccountId());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void findEmpFinTxnByPayrollRunIdAndTxnTypeCode() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions = null;

        //Test case to test the financial transactions for the company , payroll run & transaction code
        financialTransactions = FinancialTransaction.
                findEmployeeFinancialTransactions(company, "BatchId01", null, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        Application.commitUnitOfWork();

        assertEquals("Number of Financial Transactions:", 2, financialTransactions.size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());

            assertEquals("Transaction Type Cd", TransactionTypeCode.EmployeeDdCredit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
        }
    }

    @Test
    public void findEmpFinTxnByPayrollRunIdAndTxnTypeAndSettlementDate() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions = null;

        //Test case to test the financial transactions for the company , payroll run , transaction code
        //and settlement date
        financialTransactions = FinancialTransaction.
                findEmployeeFinancialTransactions(company, "BatchId01", null, null,
                        TransactionTypeCode.EmployeeDdCredit,
                        SpcfCalendar.createInstance(2007, 10, 2, SpcfTimeZone.getLocalTimeZone()),
                        null);

        Application.commitUnitOfWork();

        assertEquals("Number of Financial Transactions:", 2, financialTransactions.size());
    }

    @Test
    public void findEmpFinTxnBySettlementDateDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions = null;

        //Test case to test the financial transactions for the company , payroll run , transaction code
        //and settlement date
        financialTransactions = FinancialTransaction.
                findEmployeeFinancialTransactions(company, "BatchId01", null, null,
                        TransactionTypeCode.EmployeeDdCredit,
                        SpcfCalendar.createInstance(2007, 10, 3, SpcfTimeZone.getLocalTimeZone()),
                        null);

        Application.commitUnitOfWork();

        assertEquals("Number of Financial Transactions:", 0, financialTransactions.size());
    }

    @Test
    /**
     * Test case to test the payroll financial transaction collection
     */
    public void getPayrollFinancialTransactionCollection() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        assertEquals("Number of Payroll Financial Transactions", 3, payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Payroll Financial Transactions", 2, financialTransactions.size());

        financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created, TransactionStateCode.Executed});

        assertEquals("Number of Payroll Financial Transactions", 2, financialTransactions.size());

        financialTransactions =
                payrollRun.getFinancialTransactions(
                        null, new TransactionStateCode[]{TransactionStateCode.Executed});

        assertEquals("Number of Payroll Financial Transactions", 0, financialTransactions.size());

        Application.commitUnitOfWork();

    }

    @Test
    public void findFinTxnByPayrollRunId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        //Test case to test the financial transactions for the company  and payroll run id
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company, "BatchId01", null, null, null,
                        null, null, null, null);
        assertEquals("Number of Financial Transactions:", 3, financialTransactions.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());

            assertEquals("PayRoll Run Id ", payrollRunDTO.getPayrollTXBatchId(),
                    financialTransaction.getPayrollRun().getSourcePayRunId());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void findFinTxnByPayrollRunIdAndTxnTypeCode() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();
        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        //Update FinancialTransaction Status Rule
        eeFinancialTxs.get(0).updateFinancialTransactionState(TransactionStateCode.Returned);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        AddEmployeeReturnTransferTransaction eeReturnTransfer = new AddEmployeeReturnTransferTransaction(
                SourceSystemCode.QBOE, "123272727", "BatchId01");
        processResult = eeReturnTransfer.execute();

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());


        Application.beginUnitOfWork();

        //Test case to test the financial transactions for the company , payroll run & transaction code
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company, "BatchId01", null, null, null,
                        TransactionTypeCode.IntuitEmployeeReturnTransfer, null, null, null);

        assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());

            assertEquals("Transaction Type Cd", TransactionTypeCode.IntuitEmployeeReturnTransfer,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void findFinTxnByPayrollRunIdAndTxnTypeCodeDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        //Test case to test the financial transactions for the company , payroll run & transaction code
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company, "BatchId01", null, null, null,
                        TransactionTypeCode.IntuitEmployeeReturnTransfer, null, null, null);

        Application.commitUnitOfWork();

        assertEquals("Number of Financial Transactions:", 0, financialTransactions.size());
    }

    @Test
    public void findFinTxnByCompanyBankAccount() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);


        CompanyBankAccount companyBankAccount = CompanyBankAccount
                .findCompanyBankAccount(company, "123123");

        //Test case to test the financial transactions for the company and company bank account
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company, null, null, null, companyBankAccount, null,
                        null, null, null);

        assertEquals("Number of Financial Transactions:", 1, financialTransactions.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void findFinTxnByPayrollRunIdAndEmployee() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        Employee employee = Employee.findEmployee(company, "Emp1");

        //Test case to test the financial transactions for the company , payroll run id & employee
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(company, "BatchId01", employee, null, null,
                        null, null, null, null);

        assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Company Id ", company.getSourceCompanyId(),
                    financialTransaction.getCompany().getSourceCompanyId());

            EmployeeBankAccount employeeBankAccount = financialTransaction.getEmployeeBankAccount();

            assertEquals("Employee Id:", employee.getSourceEmployeeId(),
                    employeeBankAccount.getEmployee().getSourceEmployeeId());
        }

        Application.commitUnitOfWork();
    }

    @Test
    /**
     *   Test method to test getFinancialTransactionStates by company and financial transaction id
     */
    public void testFinancialTransactionStatesByCompanyAndTxId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerDdRedebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});


        for (FinancialTransaction financialTransaction : financialTransactions) {
            DomainEntitySet<FinancialTransactionState> txStates = financialTransaction.getFinancialTransactionStates();
            assertNotNull(txStates);
            assertTrue("Number of Financial Transactions:", txStates.size() == 1);
            Iterator<FinancialTransactionState> stateItr = financialTransaction.getFinancialTransactionStates().iterator();
            assertTrue(stateItr.hasNext());
            FinancialTransactionState expectedTxState = stateItr.next();
            assertEquals("Financial Transaction States:", expectedTxState, txStates.get(0));
        }

        Application.commitUnitOfWork();
    }

    @Test
    /**
     *   Test method to test getFinancialTransactionStates by company and financial transaction id
     */
    public void testCompanyFinancialTransactionsExcludingType() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findCompanyFinancialTransactionsExcludingType(company,
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created}).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

        assertEquals("Number of Financial Transactions:", 2, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployeeDdCredit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
        }

        Application.commitUnitOfWork();
    }

    @Test
    /**
     * Test case to test the CompanyBankAccount method
     */
    public void getCompanyBankAccount() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Payroll Financial Transactions", 1, financialTransactions.size());

        CompanyBankAccount companyBankAccount = financialTransactions.get(0).getCompanyBankAccount(
        );

        assertEquals("Company Id ", company.getSourceCompanyId(), companyBankAccount.getCompany().getSourceCompanyId());

        Application.commitUnitOfWork();
    }


    @Test
    public void getLedgerDetailsCollection() {

        // 1. Initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        // 2. calculate the expected data
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        List<String> expectedLedgerAmounts = new ArrayList();
        SpcfDecimal debitAmount = SpcfDecimal.createInstance("0.00");
        for (PaycheckDTO payCheck : payChecks) {
            //expectedLedgerAmounts.add(payCheck.getPaycheckNetAmount().toString());  No ledger entry created for EE transaction
            debitAmount = debitAmount.add(payCheck.getPaycheckNetAmount());
        }
        // Two ledger entries created for er credit
        expectedLedgerAmounts.add(debitAmount.toString());
        expectedLedgerAmounts.add(debitAmount.toString());

        // 3. execute query to fetch actual results
        Application.beginUnitOfWork();
        Company company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        null,//source payroll run
                        null); // ledger account code

        Application.commitUnitOfWork();

        // 4. Verify results
        // check count
        assertEquals(3, ledgerEntries.size());
    }

    @Test
    public void getLedgerDetailsCollection1() {

        // 1. Initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        // 2. calculate the expected data
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        List<String> expectedLedgerAmounts = new ArrayList();

        SpcfDecimal debitAmount = SpcfDecimal.createInstance("0.00");
        for (PaycheckDTO payCheck : payChecks) {
            debitAmount = debitAmount.add(payCheck.getPaycheckNetAmount());
            //No ledger entries created for EE - expectedLedgerAmounts.add(payCheck.getPaycheckNetAmount().toString());
        }
        expectedLedgerAmounts.add(debitAmount.toString());

        // 3. execute query to fetch actual results
        Application.beginUnitOfWork();
        Company company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        "BatchId01",//source payroll run
                        LedgerAccountCode.DDFutureLiability);  // ledger account code

        Application.commitUnitOfWork();

        // 4. Verify results
        // check count
        assertEquals(1, ledgerEntries.size());

        List<String> actualLedgerAmounts = new ArrayList();
        for (FinancialTransaction finTxn : ledgerEntries) {
            actualLedgerAmounts.add(finTxn.getFinancialTransactionAmount().toString());
        }

        // check amounts
        assertTrue(actualLedgerAmounts.containsAll(expectedLedgerAmounts));
        assertTrue(expectedLedgerAmounts.containsAll(actualLedgerAmounts));
    }

    @Test
    public void getLedgerDetailsCollection2() {
        // 1. Initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        // 2. calculate the expected data
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        SpcfDecimal expectedDebitAmount = SpcfDecimal.createInstance("0.00");
        for (PaycheckDTO payCheck : payChecks) {
            expectedDebitAmount = expectedDebitAmount.add(payCheck.getPaycheckNetAmount());
        }

        // 3. execute query to fetch actual results
        Application.beginUnitOfWork();
        Company company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        "BatchId01",//source payroll run
                        LedgerAccountCode.DDFutureReceivable);  // ledger account code

        DomainEntitySet<PostingRule> postingRule = PostingRule.findPostingRuleByFinancialTransaction(
                ledgerEntries.get(0), LedgerAccountCode.DDFutureReceivable);

        Application.commitUnitOfWork();

        // 4. Verify results
        // check count
        assertEquals(1, ledgerEntries.size());

        SpcfDecimal actualDebitAmount = ledgerEntries.get(0).getFinancialTransactionAmount();

        // check amounts

        assertEquals(actualDebitAmount.toString(), expectedDebitAmount.toString());

        //
        assertEquals("Ledger Account Check for Ledger AccountCd ", LedgerAccountCode.DDFutureReceivable,
                postingRule.get(0).getLedgerAccount().getLedgerAccountCd());
    }

    @Test
    public void getLedgerDetailsCollection3() {
        // 1. Initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        // 2. calculate the expected data
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        List<String> expectedLedgerAmounts = new ArrayList();
        SpcfDecimal debitAmount = SpcfDecimal.createInstance("0.00");
        for (PaycheckDTO payCheck : payChecks) {
            // No ledger entries created for EE - expectedLedgerAmounts.add(payCheck.getPaycheckNetAmount().toString());
            debitAmount = debitAmount.add(payCheck.getPaycheckNetAmount());
        }
        expectedLedgerAmounts.add(debitAmount.toString());
        expectedLedgerAmounts.add(debitAmount.toString());

        // 3. execute query to fetch actual results
        Application.beginUnitOfWork();
        Company company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        "BatchId01",//source payroll run
                        null);  // ledger account code

        Application.commitUnitOfWork();

        // 4. Verify results
        // check count
        assertEquals(1, ledgerEntries.size());

        List<String> actualLedgerAmounts = new ArrayList();
        for (FinancialTransaction finTxn : ledgerEntries) {
            actualLedgerAmounts.add(finTxn.getFinancialTransactionAmount().toString());
        }
        //check amounts
        assertTrue(actualLedgerAmounts.containsAll(expectedLedgerAmounts));
        assertTrue(expectedLedgerAmounts.containsAll(actualLedgerAmounts));

    }

    @Test
    public void getLedgerDetailsCollection4() {
        // 1. Initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        // 2. calculate the expected data
        Collection<PaycheckDTO> payChecks = payrollRunDTO.getPaychecks();
        List<String> expectedLedgerAmounts = new ArrayList();

        SpcfDecimal debitAmount = SpcfDecimal.createInstance("0.00");
        for (PaycheckDTO payCheck : payChecks) {
            // No ledger entries created for EE - expectedLedgerAmounts.add(payCheck.getPaycheckNetAmount().toString());
            debitAmount = debitAmount.add(payCheck.getPaycheckNetAmount());
        }
        expectedLedgerAmounts.add(debitAmount.toString());

        // 3. execute query to fetch actual results
        Application.beginUnitOfWork();
        Company company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        null,//source payroll run
                        LedgerAccountCode.DDFutureLiability); // ledger account code

        Application.commitUnitOfWork();

        // 4. Verify results
        // check count
        assertEquals(1, ledgerEntries.size());

        List<String> actualLedgerAmounts = new ArrayList();
        for (FinancialTransaction finTxn : ledgerEntries) {
            actualLedgerAmounts.add(finTxn.getFinancialTransactionAmount().toString());
        }

        // check amounts
        assertTrue(actualLedgerAmounts.containsAll(expectedLedgerAmounts));
        assertTrue(expectedLedgerAmounts.containsAll(actualLedgerAmounts));
    }

    @Test
    public void getLedgerDetails_WithoutPayroll() {
        // Initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        // execute query to fetch actual results
        Application.beginUnitOfWork();
        Company company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        null,//source payroll run
                        LedgerAccountCode.FeeIncome); // ledger account code

        Application.commitUnitOfWork();

        //Assertion for Number Of FinancialTransactions
        assertEquals(2, ledgerEntries.size());

        //Assertion for FinancialTransaction Type
        for (FinancialTransaction finTxn : ledgerEntries) {
            assertEquals("Financial Transaction Type ", TransactionTypeCode.EmployerVerificationDebit, finTxn.getTransactionType().getTransactionTypeCd());
        }

        //Offload EmployerDdDebit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload EmployeeDdCredit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Create Transaction Return For EmployerDdDebit
        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        DomainEntitySet<FinancialTransaction> finTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "123272727",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        persistTransactionReturnBatch();

        Application.commitUnitOfWork();
        DataLoadServices.returnTxns(finTxns, "R01", "This is an NSF description");

        //Offload EmployeeDdCredit
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // execute query to fetch actual results using the Ledger Account Code
        Application.beginUnitOfWork();
        company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        null,//source payroll run
                        LedgerAccountCode.FeeIncome); // ledger account code

        Application.commitUnitOfWork();

        //Assertion for Number Of FinancialTransactions Retrieved Using 'FeeIncome' Ledger Account Code
        assertEquals(3, ledgerEntries.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());

        // execute query to fetch actual results without the Ledger Account Code
        Application.beginUnitOfWork();
        company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        null,//source payroll run
                        null); // ledger account code

        Application.commitUnitOfWork();

        //Assertion for Number Of FinancialTransactions without Ledger Account Code
        assertEquals(7, ledgerEntries.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());
    }

    @Test
    public void getLedgerDetails_Payroll() {
        // Initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        //execute query to fetch actual results
        Application.beginUnitOfWork();
        Company company =
                Company.findCompany("123272727",
                        SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> ledgerEntries =
                FinancialTransaction.findFinancialTransactionsByLedgerAccountCode(company,
                        "BatchId01",//source payroll run
                        null); // ledger account code

        Application.commitUnitOfWork();

        //Verify results
        assertEquals(1, ledgerEntries.size());

        //Assertion for FinancialTransaction Type
        for (FinancialTransaction finTxn : ledgerEntries) {
            assertEquals("Financial Transaction Type ", TransactionTypeCode.EmployerDdDebit, finTxn.getTransactionType().getTransactionTypeCd());
        }
    }

    @Test
    public void testMoneyMovementTransactions() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

        FinancialTransaction finTxn = financialTransactions.get(0);

        FinancialTransaction financialTx =
                FinancialTransaction.createFinancialTransaction(finTxn.getCompany(),
                        finTxn.getPayrollRun(),
                        null,
                        finTxn.getCreditBankAccount(),
                        finTxn.getDebitBankAccount(),
                        finTxn.getCreditBankAccountType(),
                        finTxn.getDebitBankAccountType(),
                        finTxn.getTransactionType().getTransactionTypeCd(),
                        new SpcfMoney("100.00"),
                        finTxn.getSettlementTypeCd(),
                        finTxn.getSettlementDate().toLocal());

        financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        MoneyMovementTransaction mmTxn = financialTx.getMoneyMovementTransaction();
        SpcfMoney amount = new SpcfMoney(finTxn.getFinancialTransactionAmount().add(
                financialTx.getFinancialTransactionAmount()));

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of EmployerDdDebit Financial Transactions", 2, financialTransactions.size());
        assertEquals("Number of Financial Transactions Associated with MMT", 2, mmTxn.getFinancialTransactionCollection().find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());
        assertEquals("Money Movement Txn Amount ", amount, mmTxn.getMoneyMovementTransactionAmount());
        assertEquals("Number of Entry Detail Records", 2, entryDetailRecords.size());
        assertEquals("Entry Detail Amount ", amount, entryDetailRecords.iterator().next().getAmount());

        PayrollServices.beginUnitOfWork();
        financialTx = PayrollServices.entityFinder.findById(FinancialTransaction.class, financialTx.getId());
        FinancialTransaction financialTxn = financialTx.updateFinancialTransactionState(TransactionStateCode.Cancelled);
        PayrollServices.commitUnitOfWork();
        assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, financialTxn.getCurrentTransactionState().getTransactionStateCd());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        mmTxn = financialTransactions.get(0).getMoneyMovementTransaction();
        entryDetailRecords = mmTxn.getEntryDetailRecordCollection();

        assertEquals("Number of EmployerDdDebit Financial Transactions", 1, financialTransactions.size());
        assertEquals("Number of Financial Transactions Associated with MMT", 1, mmTxn.getFinancialTransactionCollection().find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());
        assertEquals("Money Movement Txn Amount ", financialTransactions.get(0).getFinancialTransactionAmount(),
                mmTxn.getMoneyMovementTransactionAmount());
        assertEquals("Number of Entry Detail Records", 2, entryDetailRecords.size());
        assertEquals("Entry Detail Amount ",financialTransactions.get(0).getFinancialTransactionAmount() ,
                entryDetailRecords.iterator().next().getAmount());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testMMTInitiationDateInPast() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

        FinancialTransaction finTxn = financialTransactions.get(0);

        SpcfCalendar initDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initDate, 1);
        CalendarUtils.clearTime(initDate);
        FinancialTransaction financialTx =
                FinancialTransaction.createFinancialTransaction(finTxn.getCompany(),
                        finTxn.getPayrollRun(),
                        null,
                        finTxn.getCreditBankAccount(),
                        finTxn.getDebitBankAccount(),
                        finTxn.getCreditBankAccountType(),
                        finTxn.getDebitBankAccountType(),
                        finTxn.getTransactionType().getTransactionTypeCd(),
                        new SpcfMoney("100.00"),
                        finTxn.getSettlementTypeCd(),
                        initDate);

        CalendarUtils.addBusinessDays(initDate, -1);
        financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        MoneyMovementTransaction mmTxn = financialTx.getMoneyMovementTransaction();
        SpcfMoney amount = new SpcfMoney(finTxn.getFinancialTransactionAmount().add(
                financialTx.getFinancialTransactionAmount()));

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();

        PayrollServices.commitUnitOfWork();

        assertEquals("Initiation date", initDate, mmTxn.getInitiationDate());

    }

    @Test
    public void testMMTInitiationDate_AfterCutOffTime() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

        FinancialTransaction finTxn = financialTransactions.get(0);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(),
                PSPDate.getPSPTime().getDay(), 17, 30, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar initDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initDate, 1);
        CalendarUtils.clearTime(initDate);
       try {
                FinancialTransaction.createFinancialTransaction(finTxn.getCompany(),
                        finTxn.getPayrollRun(),
                        null,
                        finTxn.getCreditBankAccount(),
                        finTxn.getDebitBankAccount(),
                        finTxn.getCreditBankAccountType(),
                        finTxn.getDebitBankAccountType(),
                        finTxn.getTransactionType().getTransactionTypeCd(),
                        new SpcfMoney("100.00"),
                        finTxn.getSettlementTypeCd(),
                        initDate);
           fail("Expected exception.  Attempting to insert an MMT with an initiation date of today after the offload time and no second offload.");
       } catch (Throwable t) {}
    }

    @Test
    public void testMMTInitiationDate_BeforeCutOffTime() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

        FinancialTransaction finTxn = financialTransactions.get(0);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(),
                PSPDate.getPSPTime().getDay(), 16, 30, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar initDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initDate, 1);
        CalendarUtils.clearTime(initDate);
        FinancialTransaction financialTx =
                FinancialTransaction.createFinancialTransaction(finTxn.getCompany(),
                        finTxn.getPayrollRun(),
                        null,
                        finTxn.getCreditBankAccount(),
                        finTxn.getDebitBankAccount(),
                        finTxn.getCreditBankAccountType(),
                        finTxn.getDebitBankAccountType(),
                        finTxn.getTransactionType().getTransactionTypeCd(),
                        new SpcfMoney("100.00"),
                        finTxn.getSettlementTypeCd(),
                        initDate);

        CalendarUtils.addBusinessDays(initDate, -1);
        financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        MoneyMovementTransaction mmTxn = financialTx.getMoneyMovementTransaction();
        SpcfMoney amount = new SpcfMoney(finTxn.getFinancialTransactionAmount().add(
                financialTx.getFinancialTransactionAmount()));

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();

        PayrollServices.commitUnitOfWork();

        assertEquals("Initiation date", initDate, mmTxn.getInitiationDate());

    }

    @Test
    public void testMMTInitiationDateInFuture() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        com.intuit.sbd.payroll.psp.domain.PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());

        FinancialTransaction finTxn = financialTransactions.get(0);

        SpcfCalendar initDate = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(initDate, 2);
        CalendarUtils.clearTime(initDate);
        FinancialTransaction financialTx =
                FinancialTransaction.createFinancialTransaction(finTxn.getCompany(),
                        finTxn.getPayrollRun(),
                        null,
                        finTxn.getCreditBankAccount(),
                        finTxn.getDebitBankAccount(),
                        finTxn.getCreditBankAccountType(),
                        finTxn.getDebitBankAccountType(),
                        finTxn.getTransactionType().getTransactionTypeCd(),
                        new SpcfMoney("100.00"),
                        finTxn.getSettlementTypeCd(),
                        initDate);

        CalendarUtils.addBusinessDays(initDate, -1);
        financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        MoneyMovementTransaction mmTxn = financialTx.getMoneyMovementTransaction();
        SpcfMoney amount = new SpcfMoney(finTxn.getFinancialTransactionAmount().add(
                financialTx.getFinancialTransactionAmount()));

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();

        PayrollServices.commitUnitOfWork();

        assertEquals("Initiation date", initDate, mmTxn.getInitiationDate());

    }

    @Test
    public void testEntryDetailsRecords() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        company.setTaxExemptExpirationDate(taxExemptExpirationDate);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany("123272727", SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.
                findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> feeTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 1, financialTransactions.size());
        assertEquals("Number of Fee Transactions", 1, feeTransactions.size());

        MoneyMovementTransaction mmTxn = financialTransactions.get(0).getMoneyMovementTransaction();

        DomainEntitySet<FinancialTransaction> finTxns = mmTxn.getFinancialTransactionCollection();
        SpcfMoney amount = new SpcfMoney("0");
        for (FinancialTransaction finTxn : finTxns) {
            amount = new SpcfMoney(amount.add(finTxn.getFinancialTransactionAmount()));
        }

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = getEntryDetailsInOrder(mmTxn);
        assertEquals("Number of Financial Transactions Associated with MMT", 3, mmTxn.getFinancialTransactionCollection().size());
        assertEquals("Money Movement Txn Amount ", amount, mmTxn.getMoneyMovementTransactionAmount());
        assertEquals("Number of Entry Detail Records", 3, entryDetailRecords.size());
        assertEquals("Entry Detail Amount ", amount, entryDetailRecords.get(2).getAmount());

        //Assertions for NACHABatch Type & NACHA File Type
        for(EntryDetailRecord entryDetailRecord : entryDetailRecords){
            assertEquals("NACHA Batch Type ", NACHABatchType.Payroll, entryDetailRecord.getNACHABatchType());
            assertEquals("NACHA File Type ", NACHAFileType.CCD, entryDetailRecord.getNACHAFileType());
        }

        DomainEntitySet<FinancialTransaction> eeFinTxns =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of Financial Transactions", 2, eeFinTxns.size());
        for(FinancialTransaction finTxn : eeFinTxns){
            entryDetailRecords = getEntryDetailsInOrder(finTxn.getMoneyMovementTransaction());
            //Assertions for NACHABatch Type & NACHA File Type
            for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
                assertEquals("NACHA Batch Type ", NACHABatchType.Payroll, entryDetailRecord.getNACHABatchType());
                assertEquals("NACHA File Type ", NACHAFileType.PPD, entryDetailRecord.getNACHAFileType());
            }
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction financialTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class, financialTransactions.get(0).getId());
        FinancialTransaction financialTxn = financialTransaction.updateFinancialTransactionState(
                TransactionStateCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled,
                financialTxn.getCurrentTransactionState().getTransactionStateCd());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        financialTransactions =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        mmTxn = financialTransactions.get(0).getMoneyMovementTransaction();
        entryDetailRecords = getEntryDetailsInOrder(mmTxn);

        assertEquals("Number of DdServiceFee Financial Transactions", 1, financialTransactions.size());
        assertEquals("Number of Financial Transactions Associated with MMT", 2, mmTxn.getFinancialTransactionCollection().size());
        SpcfMoney total = SpcfMoney.ZERO;
        for (FinancialTransaction transaction : mmTxn.getFinancialTransactionCollection()) {
            total = new SpcfMoney(total.add(transaction.getFinancialTransactionAmount()));
        }
        assertEquals("Money Movement Txn Amount ", total,
                mmTxn.getMoneyMovementTransactionAmount());
        assertEquals("Number of Entry Detail Records", 2, entryDetailRecords.size());
        assertEquals("Entry Detail Amount ", total,
                entryDetailRecords.get(1).getAmount());

        PayrollServices.commitUnitOfWork();
    }

    private DomainEntitySet<EntryDetailRecord> getEntryDetailsInOrder(MoneyMovementTransaction pMMTxn) {
        DomainEntitySet<EntryDetailRecord> entryDetailRecords = pMMTxn.getEntryDetailRecordCollection();
        entryDetailRecords = entryDetailRecords.sort(EntryDetailRecord.Amount());

        return entryDetailRecords;
    }

    /**
     * Test method to test getFinancialTransactionStates by company and financial transaction id
     */
    public void testPendingFinancialTransactionsExcludingType() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PSPDate.setPSPTime("20070925160000");
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created, BankAccountOwnerType.Company);

        assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdDebit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
        }

        PSPDate.setPSPTime("20070925165900");
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created, BankAccountOwnerType.Company);

        assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdDebit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
        }

        PSPDate.setPSPTime("20070925170000");
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created, BankAccountOwnerType.Company);

        assertTrue("Number of Financial Transactions:", financialTransactions.size() == 0);

        Application.commitUnitOfWork();
    }

    @Test
    public void testRandomAmounts() {
        SpcfMoney compareAmt = new SpcfMoney("1");

        double number = (0.0 * 98) + 1;
        SpcfMoney randomAmount = new SpcfMoney(SpcfDecimal.createInstance(number / 100));

        assertEquals("Random Amount ", randomAmount.compareTo(compareAmt), -1);

        number = (0.9999999 * 98) + 1;
        randomAmount = new SpcfMoney(SpcfDecimal.createInstance(number / 100));

        assertEquals("Random Amount ", randomAmount.compareTo(compareAmt), -1);
    }

    /**
     * Function to add the TransactionReturnBatch
     *
     * @return transactionReturnBatch TransactionReturnBatch
     */

    private TransactionReturnBatch persistTransactionReturnBatch() {
        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        return transactionReturnBatch;
    }

    /**
     * Function to add the TransactionReturns to the associated Financial Transactions
     *
     * @param pFinTxnList     DomainEntitySet<FinancialTransaction>
     * @param pBankReturnCd   String
     * @param pBankReturnDesc String
     * @return DomainEntitySet<TransactionReturn>
     */
    public DomainEntitySet<TransactionReturn> persistTransactionReturns(DomainEntitySet<FinancialTransaction> pFinTxnList,
                                                                  String pBankReturnCd, String pBankReturnDesc) {
        TransactionReturnBatch transactionReturnBatch = persistTransactionReturnBatch();
        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : pFinTxnList) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd(pBankReturnCd);
            transactionReturn.setBankReturnDescription(pBankReturnDesc);
            transactionReturn.setBankReturnTraceNumber(112L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());

            returnList.add(Application.save(transactionReturn));
        }

        return returnList;
    }

    @Test
    public void testGetSettlementDate() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        // set psp date to saturday
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 8, 10, 59, 59, 59, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // verify the settlement
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        SpcfCalendar settlementDate = FinancialTransaction.getSettlementDate(company.getOffloadGroup());
        assertEquals("Settlement Date", settlementDate, SpcfCalendar.createInstance(2007, SpcfCalendar.September, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testTransactionSummary() {
        // set up a Company with a CompanyBankAccount
        PayrollServices.beginUnitOfWork();
        {
            PSPDate.setPSPTime("20080102123456"); // non-bank holiday, so bank verification debits can be offloaded and then verified
            DataLoader dataLoader = new DataLoader();
            Company company = dataLoader.persistTestActiveCompany();
            dataLoader.persistTestCompanyService(company);
            dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20080104123456");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        {
            FinancialTransaction ftDebit = createRelatedFT(TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Returned, 10.00, null);
            TransactionSummary summary = ftDebit.summarizeRelatedTransactions();
            verifySummary("orig debit returned", summary, 10.00, 0.00, 0.00, 0.00, 0.00); // all Uncollected

            createRelatedFT(TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Completed, 6.00, ftDebit);
            summary = ftDebit.summarizeRelatedTransactions();
            verifySummary("partial redebit completed", summary, 4.00, 6.00, 0.00, 0.00, 0.00); // redebit amount Uncollected --> Collected

            // a redebit for the remaining amount, but in Created, so it won't affect the summary
            FinancialTransaction ft = createRelatedFT(TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Created, 4.00, ftDebit);
            summary = ftDebit.summarizeRelatedTransactions();
            verifySummary("partial redebit completed", summary, 4.00, 6.00, 0.00, 0.00, 0.00); // no change

            createRelatedFT(TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Executed, 5.00, ft);
            summary = ftDebit.summarizeRelatedTransactions();
            verifySummary("partial refund executed", summary, 4.00, 1.00, 5.00, 0.00, 0.00); // refund amount moves to Refunded

            createRelatedFT(TransactionTypeCode.EmployerWriteOff, TransactionStateCode.Completed, 3.00, ftDebit);
            summary = ftDebit.summarizeRelatedTransactions();
            verifySummary("partial writeoff completed", summary, 1.00, 1.00, 5.00, 3.00, 0.00); // Uncollect --> WrittenOff

            createRelatedFT(TransactionTypeCode.BadDebtRecovery, TransactionStateCode.Completed, 2.00, ft);
            summary = ftDebit.summarizeRelatedTransactions();
            verifySummary("partial recovery completed", summary, 1.00, 1.00, 5.00, 1.00, 2.00);
        }
        PayrollServices.commitUnitOfWork();
    }

    private void verifySummary(String pMessage, TransactionSummary pSummary, double pUncollected, double pCollected,
                               double pRefunded, double pWrittenOff, double pRecovered) {
        Assert.assertEquals(pMessage + ", amount Uncollected", pUncollected, Double.valueOf(pSummary.amtUncollected.toString()));
        Assert.assertEquals(pMessage + ", amount Collected", pCollected, Double.valueOf(pSummary.amtCollected.toString()));
        Assert.assertEquals(pMessage + ", amount Refunded", pRefunded, Double.valueOf(pSummary.amtRefunded.toString()));
        Assert.assertEquals(pMessage + ", amount WrittenOff", pWrittenOff, Double.valueOf(pSummary.amtWrittenOff.toString()));
        Assert.assertEquals(pMessage + ", amount Recovered", pRecovered, Double.valueOf(pSummary.amtRecovered.toString()));
    }

    private FinancialTransaction createRelatedFT(TransactionTypeCode pTypeCd, TransactionStateCode pStateCd,
                                                 double pAmount, FinancialTransaction pOrigFT) {
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit),
                CreditDebitCode.Credit);

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);

        // create the FT
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);
        CalendarUtils.addBusinessDays(settlementDate, 1);

        FinancialTransaction ft = FinancialTransaction.createFinancialTransaction(
                company, null, null, iba.getBankAccount(), cba.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Company, pTypeCd,
                new SpcfMoney(String.valueOf(pAmount)), SettlementType.ACH, settlementDate,
                "SKU", pOrigFT, 1);

        // advance it to the requested state
        if (pStateCd != TransactionStateCode.Created) {
            TransactionState executed = Application.findById(TransactionState.class, TransactionStateCode.Executed);
            ft.addTransactionState(executed);

            if (pStateCd == TransactionStateCode.Completed) {
                TransactionState completed = Application.findById(TransactionState.class, TransactionStateCode.Completed);
                ft.addTransactionState(completed);
            } else if (pStateCd == TransactionStateCode.Returned) {
                TransactionState returned = Application.findById(TransactionState.class, TransactionStateCode.Returned);
                ft.addTransactionState(returned);
            }
        }

        return ft;
    }

    @Test
    public void testFindFinTxnsWithCreditDebitCode() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(
                                                     SourceSystemCode.QBOE,
                                                     "123272727",
                                                     payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        junit.framework.Assert.assertTrue(result.isSuccess());

        //Offload EmployerDdDebit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload EmployerDdDebit
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        List<Object[]> financialTxns =
                FinancialTransaction.findFinancialTransactionsWithCreditDebitCode(company,
                    null, LedgerAccountCode.DDFutureLiability);
        assertEquals("Number of financial transactions", 2, financialTxns.size());

        financialTxns =
                FinancialTransaction.findFinancialTransactionsWithCreditDebitCode(company,
                    result.getResult().getSourcePayRunId(), LedgerAccountCode.DDFutureLiability);
        assertEquals("Number of financial transactions", 2, financialTxns.size());

        financialTxns =
                FinancialTransaction.findFinancialTransactionsWithCreditDebitCode(company,
                    null, LedgerAccountCode.DDFutureReceivable);
        assertEquals("Number of financial transactions", 2, financialTxns.size());

         financialTxns =
                FinancialTransaction.findFinancialTransactionsWithCreditDebitCode(company,
                    result.getResult().getSourcePayRunId(), LedgerAccountCode.DDFutureReceivable);
        assertEquals("Number of financial transactions", 2, financialTxns.size());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFinancialTransactionsByServiceAndExcludingType() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(
                                                     SourceSystemCode.QBOE,
                                                     "123272727",
                                                     payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        //Offload EmployerDdDebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload EmployerDdDebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        Service ddService = Application.findById(Service.class, ServiceCode.DirectDeposit);

        DomainEntitySet<FinancialTransaction> financialTxns =
                FinancialTransaction.findFinancialTransactionsByServiceAndExcludingType(company, ddService,
                        null,
                        new TransactionStateCode[]{TransactionStateCode.Executed});

        assertEquals("Total Number of financial transactions ", 5, financialTxns.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());

        financialTxns =
                FinancialTransaction.findFinancialTransactionsByServiceAndExcludingType(company, ddService,
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerVerificationDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});

        assertEquals("Number of financial transactions By Excluding EmployerVerificationDebit Type ", 3, financialTxns.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());

        Service taxService = Application.findById(Service.class, ServiceCode.Tax);
        financialTxns =
                FinancialTransaction.findFinancialTransactionsByServiceAndExcludingType(company, taxService,
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerVerificationDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});

        assertEquals("Number of financial transactions By Excluding EmployerVerificationDebit Type for Tax Service ", 0, financialTxns.find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());
        PayrollServices.commitUnitOfWork();
    }


}

