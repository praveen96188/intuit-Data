package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.System.out;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: May 13, 2008
 * Time: 10:24:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChangeCompanyBankAccountCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testInvalidCompanyParameters() {

        // Load Company Data
        PayrollServices.beginUnitOfWork();
        Company company = CompanyBankAccountDataLoader.loadCompany();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.changeCompanyBankAccount(null,
                company.getSourceCompanyId(), companyBankAccountDTO, false, true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE, null,
                                    companyBankAccountDTO, false, true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

     @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void changeCompanyBankAccount_CompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        PayrollServices.commitUnitOfWork();

        // Set invalid company
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.changeCompanyBankAccount(
                company.getSourceSystemCd(), "InvalidCompanyId", companyBankAccountDTO,
                false, true, true);
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());
    }
    
    @Test
    /**
     *  Test error message 170 - BankAccount for company does not exist
     */

    public void changeCompanyBankAccount_AccountDoesNotExist() {

        // Load Company Data
        PayrollServices.beginUnitOfWork();
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        // Load DTO with a non existent company bank account
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.changeCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO,
                false, true, true);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "170", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 does not exist.", message.getMessage());
    }

    @Test
    /**
     *  Test error message 186 - Company Bank Account is not Active
     */

    public void changeCompanyBankAccount_NotActive() {
        PayrollServices.beginUnitOfWork();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        String sourceCompanyId = company.getSourceCompanyId();
        SourceSystemCode sourceSystemCd = company.getSourceSystemCd();
        // Load CompanyBankAccount
        CompanyBankAccount companyBankAccount = CompanyBankAccountDataLoader.addTestCompanyBankAccount(company);
        CompanyBankAccountDTO companyBankAccountDTO = CompanyBankAccountDataLoader.getCompanyBankAccountDTO(companyBankAccount);
        PayrollServices.commitUnitOfWork();

        // Deactivate the CBA
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        ProcessResult<CompanyBankAccount> deactivateProcessResult = PayrollServices.companyManager.deactivateCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO.getCompanyBankAccountID(),false, false);
        assertEquals("Deactivate result: ", true, deactivateProcessResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.changeCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccountDTO,
                false, true, true);
        out.println(processResult);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // vaildate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "186", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 is not active in PSP.", message.getMessage());
    }

    @Test
    public void testNullCompanyBankAccountDTO() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();

        // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =  null;
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.changeCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, false, true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "121", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Company Bank Account is not specified.";
        assertEquals("Error Message", message.getMessage(), messageText);

    }

    @Test
    public void testInvalidBankAccountDTO() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
       // Load Company Data
        Company company = CompanyBankAccountDataLoader.loadCompany();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(), companyBankAccountDTO, true, true);
        PayrollServices.commitUnitOfWork();


        assertEquals("Result", true, processResult.isSuccess());

        // Test Invalid Routing Number
        PayrollServices.beginUnitOfWork();
        companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        companyBankAccountDTO.getBankAccountDTO().setRoutingNumber("1234");
        processResult = PayrollServices.companyManager.changeCompanyBankAccount(dataLoader.getSourceSystemCd(),
                dataLoader.getSourceCompanyId(), companyBankAccountDTO, false, true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "255", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Invalid Routing Number 1234 specified.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test method to verify the change bank account process with conflicting parameters.
     * shouldMovePendingTransactionsToAccount - true
     * shouldAddRandomDebits - true  
     *
     */
    @Test
    public void changeCBA_Conflict() {
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

        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", companyBankAccountDTO, true,  true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1200", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Cannot move pending transactions to an account that is not active.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    /**
     * Test method to verify the change bank account process with conflicting parameters.
     * shouldMovePendingTransactionsToAccount - false
     * shouldAllowPendingTransactions - false
     *

    @Test
    public void changeCBA_Conflict2() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        assertTrue(processResult.isSuccess());
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", companyBankAccountDTO, true,  false, false);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "1201", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "If pending transactions are not moving to a new account, moving pending transactions must be allowed.";
        assertEquals("Error Message", messageText, message.getMessage());
    }*/

    /**
     * Test method to verify the bank account verification transactions are not moved.
     */
    @Test
    public void testVerificationTransactionsNotMoved() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();

        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        CompanyBankAccount companyBA = processResult.getResult();
        out.println(processResult);

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        assertTrue(processResult.isSuccess());
        // Commit
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        companyBankAccountDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", companyBankAccountDTO, false,  true, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Change CAB Result:", true == processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        // verify verification transactions are with first bank account and in cancelled state
        companyBA = Application.findById(CompanyBankAccount.class, companyBA.getId());
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBA.getVerificationTransactions();
        for (FinancialTransaction finTx: verificationTransactions) {
            assertTrue("Verification Financial Transaction state",
                    finTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Cancelled);
        }
        PayrollServices.commitUnitOfWork();
        
    }

    /**
     * Test method to verify the change bank account process with pending transactions and if the deactivation doesn't
     * allow pending transactions.
     * shouldMovePendingTransactionsToAccount - true
     * shouldAllowPendingTransactions - false  
     *
     */
    @Test
    public void testChangeCBAWithPendingTransactions() {
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

        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyBankAccountDTO companyBankAccountDTO = dataloader.getTestCompanyBankAccount();
        ProcessResult processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", companyBankAccountDTO, false,  false, true);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "218", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Bank Account 123123 for company QBOE:123272727 has pending transactions and cannot be deactivated.";
        assertEquals("Error Message", messageText, message.getMessage());
    }

    @Test
    public void changeCBA_Success() {
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

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.claimOffer(company1, DataLoadServices.WAIVE_ALL_FEES);

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyBankAccount cba1 = CompanyBankAccount.findCompanyBankAccount(company1, "123123");
        BankAccount ba1 = cba1.getBankAccount();


        // Verify financial transactions before moving
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        Assert.assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

        List<SpcfUniqueId> entryDetailIdsBefore = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdDebit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getDebitBankAccount(),
                    cba1.getBankAccount());

            DomainEntitySet<EntryDetailRecord> entryDetails = financialTransaction.getMoneyMovementTransaction().getEntryDetailRecordCollection();

            assertEquals("Number Entry Details", 2, entryDetails.size());
            for (Iterator<EntryDetailRecord> iter = entryDetails.iterator(); iter.hasNext();) {
                EntryDetailRecord entryDetail = iter.next();
                entryDetailIdsBefore.add(entryDetail.getId());
            }
        }

        PayrollServices.commitUnitOfWork();
        // deactivate first bank account
        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult prChangeCBA = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                "123272727", companyBankAccountDTO, false, true, true);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess(prChangeCBA);

        PayrollServices.beginUnitOfWork();
        CompanyBankAccount cba2 = CompanyBankAccount.findCompanyBankAccount(company1,
                companyBankAccountDTO.getCompanyBankAccountID());

        // verify the two bank accounts are not equal
        assertFalse("BankAccounts", ba1.equals(cba2.getBankAccount()));
        // verify the new bank account data
        assertEquals("New bank account account number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                cba2.getBankAccount().getAccountNumber());
        assertEquals("New bank account routing number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                cba2.getBankAccount().getRoutingNumber());
        assertEquals("New bank account account type", companyBankAccountDTO.getBankAccountDTO().getAccountType().toString(),
                cba2.getBankAccount().getAccountTypeCd().toString());
        assertEquals("New bank account bank name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                cba2.getBankAccount().getBankName());

        // Verify financial transactions after moving
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        Assert.assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);

        List<SpcfUniqueId> entryDetailIdsAfter = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdDebit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getDebitBankAccount(),
                    cba2.getBankAccount());

            DomainEntitySet<EntryDetailRecord> entryDetails = financialTransaction.getMoneyMovementTransaction().getEntryDetailRecordCollection();

            assertEquals("Number Entry Details", 2, entryDetails.size());
            for (Iterator<EntryDetailRecord> iter = entryDetails.iterator(); iter.hasNext();) {
                EntryDetailRecord entryDetail = iter.next();
                entryDetailIdsAfter.add(entryDetail.getId());
                if (entryDetail.getCreditDebitIndicator() == CreditDebitCode.Debit) {
                    // verify the new entrydetail record data has destination bank account
                    String currencyString = StringFormatter
                            .formatCurrencyNoDecimalPoint(
                                    SpcfUtils.convertToBigDecimal(financialTransaction.getFinancialTransactionAmount()), 10);
                    String strExpectedRecordData = "627" + cba2.getBankAccount()
                            .getRoutingNumber() + StringFormatter
                            .formatString(cba2.getBankAccount().getAccountNumber(),
                                    17) + currencyString + StringFormatter.formatString(company1
                            .getFedTaxId(), 15) + StringFormatter.formatString(company1.getLegalName(), 22) + "  0";
                    assertEquals("Entry detail record", strExpectedRecordData, entryDetail.getRecordData());
                }
            }
        }
        // Verify no old entry detail record is present
        for (SpcfUniqueId uniqueId : entryDetailIdsBefore) {
            assertFalse("Entry Detail Records", entryDetailIdsAfter.contains(uniqueId));
        }
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company1,
                EventTypeCode.CompanyBankAccountChange,
                null, null, null);
        assertTrue("Number of company bank account change events", 2 == companyEvents.size());

        assertDateEquals(PSPDate.getPSPTime(), companyEvents.get(1).getEventTimeStamp().toLocal());
        assertTrue("Event state", CompanyEventStatus.Active == companyEvents.get(1).getStatusCd());

        companyEvents = CompanyEvent.findCompanyEvents(company1,
                EventTypeCode.CompanyBankAccountStatusChange,
                null, null, null);
        assertTrue("Number of company bank account status change events", 3 == companyEvents.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void changeCBA_DontMoveTransactions() {
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

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyBankAccount cba1 = CompanyBankAccount.findCompanyBankAccount(company1, "123123");
        BankAccount ba1 = cba1.getBankAccount();

        // Verify financial transactions before moving
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        Assert.assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);

        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdDebit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getDebitBankAccount(),
                    cba1.getBankAccount());

            DomainEntitySet<EntryDetailRecord> entryDetails = financialTransaction.getMoneyMovementTransaction().getEntryDetailRecordCollection();

            assertEquals("Number Entry Details", 2, entryDetails.size());
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                "123272727", companyBankAccountDTO, false, true, false);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        Assert.assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        CompanyBankAccount cba2 = CompanyBankAccount.findCompanyBankAccount(company1,
                companyBankAccountDTO.getCompanyBankAccountID());
        // verify the two bank accounts are not equal
        assertFalse("BankAccounts", ba1.equals(cba2.getBankAccount()));

        // verify the new bank account data
        assertEquals("New bank account account number", companyBankAccountDTO.getBankAccountDTO().getAccountNumber(),
                cba2.getBankAccount().getAccountNumber());
        assertEquals("New bank account routing number", companyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                cba2.getBankAccount().getRoutingNumber());
        assertEquals("New bank account account type", companyBankAccountDTO.getBankAccountDTO().getAccountType().toString(),
                cba2.getBankAccount().getAccountTypeCd().toString());
        assertEquals("New bank account bank name", companyBankAccountDTO.getBankAccountDTO().getBankName(),
                cba2.getBankAccount().getBankName());

        // Verify financial transactions are not moved
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        Assert.assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);

        List<SpcfUniqueId> entryDetailIdsAfter = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdDebit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getDebitBankAccount(), ba1);

            DomainEntitySet<EntryDetailRecord> entryDetails = financialTransaction.getMoneyMovementTransaction().getEntryDetailRecordCollection();

            assertEquals("Number Entry Details", 2, entryDetails.size());
            for (Iterator<EntryDetailRecord> iter = entryDetails.iterator(); iter.hasNext();) {
                EntryDetailRecord entryDetail = iter.next();
                entryDetailIdsAfter.add(entryDetail.getId());
                if (entryDetail.getCreditDebitIndicator() == CreditDebitCode.Debit) {
                    // verify the new entrydetail record data has destination bank account
                    String currencyString = StringFormatter
                            .formatCurrencyNoDecimalPoint(
                                    SpcfUtils.convertToBigDecimal(financialTransaction.getFinancialTransactionAmount()), 10);
                    String strExpectedRecordData = "627" + ba1.getRoutingNumber() + StringFormatter
                            .formatString(ba1.getAccountNumber(),
                                    17) + currencyString + StringFormatter.formatString(company1
                            .getFedTaxId(), 15) + StringFormatter.formatString(company1.getLegalName(), 22) + "  0";
                    assertEquals("Entry detail record", strExpectedRecordData, entryDetail.getRecordData());
                }
            }
        }

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company1,
                EventTypeCode.CompanyBankAccountChange,
                null, null, null);
        assertTrue("Number of company bank account change events", 2 == companyEvents.size());

        assertDateEquals(PSPDate.getPSPTime(), companyEvents.get(1).getEventTimeStamp().toLocal());
        assertTrue("Event state", CompanyEventStatus.Active == companyEvents.get(1).getStatusCd());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Try to change the account with "Created" transactions and without the "allow pending transactions" flag
     */
    @Test
    public void failWithCreated() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        PayrollRunDTO dtoPayroll = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                                                                                           "123272727", dtoPayroll);

        DomainEntitySet<FinancialTransaction> createdFTs =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("submit payroll", prPayroll);
        assertTrue("at least one FT in Created", createdFTs.size() > 0);

        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyBankAccountDTO dtoCBA = dataloader.getTestCompanyBankAccount();
        ProcessResult processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", dtoCBA, false, false, false);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "218", message.getMessageCode()); // 218 is "has pending transactions" error
    }

    /**
     * Change the account with "Created" transactions and WITH the "allow pending transactions" flag
     */
    @Test
    public void passWithCreated() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO dtoPayroll = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                                                                                           "123272727", dtoPayroll);

        DomainEntitySet<FinancialTransaction> createdFTs =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("submit payroll", prPayroll);
        assertTrue("at least one FT in Created", createdFTs.size() > 0);

        // change the bank account
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyBankAccountDTO dtoCBA = dataloader.getTestCompanyBankAccount();
        ProcessResult processResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", dtoCBA, false, true, true);
        Company company = prPayroll.getResult().getCompany();
        CompanyBankAccount cba = CompanyBankAccount.findCompanyBankAccount(company, dtoCBA.getCompanyBankAccountID());
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Change with 'Created' transactions and shouldAllowPendingTransactions=true", processResult);
        assertEquals("CBA status after calling change with unchanged data", BankAccountStatus.Active, cba.getStatusCd());
    }

    /**
     * Change the account with "Executed" transactions and WITHOUT the "allow pending transactions" flag
     */
    @Test
    public void passWithExecutedFlagFalse() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        PayrollRunDTO dtoPayroll = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                                                                                           "123272727", dtoPayroll);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("submit payroll", prPayroll);

        // offload ER stuff
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000001");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> executedFTs =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        PayrollServices.commitUnitOfWork();
        assertTrue("at least one FT in Executed", executedFTs.size() > 0);

        // change the bank account, dis-allowing pending transactions
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyBankAccountDTO dtoCBA = dataloader.getTestCompanyBankAccount();
        ProcessResult prWithoutFlag = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", dtoCBA, false, false, false);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Change with 'Executed' transactions and shouldAllowPendingTransactions=FALSE", prWithoutFlag);
    }

    /**
     * Change the account with "Executed" transactions and WITH the "allow pending transactions" flag
     */
    @Test
    public void passWithExecutedFlagTrue() {
        // Load Data
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                                                                                           "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("submit payroll", prPayroll);

        // offload ER stuff
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000001");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> executedFTs =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "123272727",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        PayrollServices.commitUnitOfWork();
        assertTrue("at least one FT in Executed", executedFTs.size() > 0);

        // change the bank account, dis-allowing pending transactions
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyBankAccountDTO dtoCBA = dataloader.getTestCompanyBankAccount();
        ProcessResult prWithoutFlag = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", dtoCBA, false, true, true);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Change with 'Executed' transactions and shouldAllowPendingTransactions=TRUE", prWithoutFlag);
    }

    private void assertDateEquals(SpcfCalendar pTimeToCompare, SpcfCalendar pPSPTime) {
        assertEquals(pTimeToCompare.getDay(), pPSPTime.getDay());
        assertEquals(pTimeToCompare.getMonth(), pPSPTime.getMonth());
        assertEquals(pTimeToCompare.getYear(), pPSPTime.getYear());
        assertEquals(pTimeToCompare.getHour(), pPSPTime.getHour());
        assertEquals(pTimeToCompare.getMinute(), pPSPTime.getMinute());
//        assertEquals(pTimeToCompare.getSecond(), pPSPTime.getSecond());
    }

    @Test
    /**
     * Test case to verify the company service status update by changing the company bank account by bypassing the
     * random debits.
     */
    public void veriftCompanyServiceStatusUpdate_QBOECompany() {
        PayrollServices.beginUnitOfWork();

        // Load Data
        CompanyBankAccountDataLoader dataLoader = new CompanyBankAccountDataLoader();
        // Load Company Data
        CompanyBankAccountDataLoader.loadCompany();

        PayrollServices.commitUnitOfWork();
        DataLoader dataloader = new DataLoader();
        // Load CompanyBankAccount
        CompanyBankAccountDTO companyBankAccountDTO =
                dataloader.getTestCompanyBankAccount();
        // Call process to add company bank account
        PayrollServices.beginUnitOfWork();
        ProcessResult<com.intuit.sbd.payroll.psp.domain.CompanyBankAccount> processResult = PayrollServices.companyManager
                .addCompanyBankAccount(dataLoader.getSourceSystemCd(), dataLoader.getSourceCompanyId(),
                        companyBankAccountDTO, true, true);
        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        // Commit
        PayrollServices.commitUnitOfWork();

        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        Company company = companies.get(0);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Source Bank Account Id:", companyBankAccountDTO.getCompanyBankAccountID(),
                companyBankAccount.getSourceBankAccountId());
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();

        //Verify Service status for all the company services before changing the company bank account
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            assertEquals("Company Status:", ServiceSubStatusCode.PendingBankVerification,
                    companyService.getStatusCd());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        dataloader = new DataLoader();
        CompanyBankAccountDTO dtoCBA = dataloader.getTestCompanyBankAccount();
        ProcessResult result = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                                                     "123272727", dtoCBA, false, true, true);
        PayrollServices.commitUnitOfWork();
        assertEquals("Result", true, result.isSuccess());


        //Verify Service status for all the company services after changing the company bank account
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            assertEquals("Company Status:", ServiceSubStatusCode.PendingFirstPayroll,
                    companyService.getStatusCd());
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Test case to verify the company service status update by changing the company bank account by bypassing the
     * random debits.
     */
    public void veriftCompanyServiceStatusUpdate_QBDTCompany() {

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        DataLoader dataloader = new DataLoader();
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = c3dl.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");
        Company company = dataloader.persistCompany(company1);

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c3dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), c3dl.getCompany1BankAccount(), true, true);
        assertEquals(0, processResult.getMessages().size());
        PayrollServices.commitUnitOfWork();


        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        company = companies.get(0);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();

        //Verify Service status for all the company services before changing the company bank account
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            assertEquals("Company Status:", ServiceSubStatusCode.PendingBankVerification,
                    companyService.getStatusCd());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO dtoCBA = c3dl.getCompany1BankAccount();
        ProcessResult result = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBDT,
                                                     "8574536", dtoCBA, false, true, true);
        PayrollServices.commitUnitOfWork();
        assertEquals("Result", true, result.isSuccess());

        //Verify Service status for all the company services after changing the company bank account
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            assertEquals("Company Status:", ServiceSubStatusCode.PendingPinCreation,
                    companyService.getStatusCd());
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    /**
     * Test case to verify the company service status update by changing the company bank account by bypassing the
     * random debits.
     */
    public void change_CBA_AchRejectOther() {

        PayrollServices.beginUnitOfWork();
        Company3Dataloader c3dl = new Company3Dataloader();
        DataLoader dataloader = new DataLoader();
        // Create Company and CompanyBankAccount
        CompanyDTO company1 = c3dl.getCompany1();
        // Set QBDT next ids
        company1.setNextEmployeeId("1");
        company1.setNextPaycheckId("1");
        company1.setNextPayrollItemId("1");
        company1.setNextPayrollTransactionId("1");
        Company company = dataloader.persistCompany(company1);

        CompanyService ddCompanyService = dataloader.persistCompanyService(company, c3dl.getCompany1Service());

        ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.addCompanyBankAccount(
                company.getSourceSystemCd(), company.getSourceCompanyId(), c3dl.getCompany1BankAccount(), true, true);
        assertEquals(0, processResult.getMessages().size());
        PayrollServices.commitUnitOfWork();


        // Verify that companybankaccount has been saved
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = PayrollServices.entityFinder.find(Company.class, Company.SourceSystemCd().notEqualTo(SourceSystemCode.PSP));
        assertEquals(companies.size(), 1);
        company = companies.get(0);
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().iterator().next();
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        companyBankAccount = PayrollServices.entityFinder
                .findById(CompanyBankAccount.class, companyBankAccount.getId());

        // Ensure there is just one company bank account associated with the company
        assertEquals(company.getCompanyBankAccountCollection().size(), 1);

        //Verify that Company Bank Account has the correct values
        assertEquals("Company Bank Account Status:", BankAccountStatus.PendingVerification,
                companyBankAccount.getStatusCd());

        // Verify that the 2 verification transactions have been created and the amounts are between $0.01 and $0.99
        assertEquals("Number of Verification Transactions: ", 2, verificationTransactions.size());
        PayrollServices.commitUnitOfWork();

        //Verify Service status for all the company services before changing the company bank account
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            assertEquals("Company Status:", ServiceSubStatusCode.PendingBankVerification,
                    companyService.getStatusCd());
        }

        company.addOnHoldReason(ServiceSubStatusCode.AchRejectOther);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyBankAccountDTO dtoCBA = c3dl.getCompany1BankAccount();

        dtoCBA.getBankAccountDTO().setAchAccountType(ACHBankAccountType.Savings);
        dtoCBA.getBankAccountDTO().setAccountType(BankAccountType.Savings);
        dtoCBA.getBankAccountDTO().setAccountNumber("1212121212");

        ProcessResult result = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBDT,
                                                     "8574536", dtoCBA, false, true, true);
        PayrollServices.commitUnitOfWork();
        assertEquals("Result", true, result.isSuccess());

        //Verify Service status for all the company services after changing the company bank account
        PayrollServices.beginUnitOfWork();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        for (CompanyService companyService : company.getCompanyServiceCollection()) {
            assertEquals("Company Status:", ServiceSubStatusCode.PendingPinCreation,
                    companyService.getStatusCd());
        }
        PayrollServices.commitUnitOfWork();
    }
}
