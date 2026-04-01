package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.TransactionReturnTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: May 8, 2008
 * Time: 8:46:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class MovePendingTransactionToBankAccountTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE
                                                            , null, "CBA1", false);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(null, "1234567",
                                                            "CBA1", false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testNullBankAccount() {
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                                                                            "1234567", null, false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "142", errorMessage.getMessageCode());
        assertEquals("Error message", "Bank Account is not specified.",
                errorMessage.getMessage());
    }



    @Test
    public void testCompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();

        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                                                                                    "upd_id_dne", "CBA1", false);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("169", errorMessage.getMessageCode());
        assertEquals("Company QBOE:upd_id_dne does not exist.", errorMessage.getMessage());
    }



    @Test
    public void testCompanyBankAccountDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        CompanyBankAccount cba = dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                                                                                    "123456", "Invalid", false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("170", errorMessage.getMessageCode());
        assertEquals("Bank Account Invalid for company QBOE:123456 does not exist.", errorMessage.getMessage());
    }

    @Test
    public void testCompanyBankAccountNotActive() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        CompanyBankAccount cba = dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyBankAccount> processResult =PayrollServices.companyManager.deactivateCompanyBankAccount(
                                                                    SourceSystemCode.QBOE, "123456", "123123", false, false);
        assertTrue(processResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                                                                                    "123456", "123123", false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);

        assertEquals("186", errorMessage.getMessageCode());
        assertEquals("Bank Account 123123 for company QBOE:123456 is not active in PSP.", errorMessage.getMessage());
    }

    @Test
    public void testMovingPendingTransactionsBeforeCutoff() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Collection<PayrollRunDTO> payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123272727();
        PayrollServices.commitUnitOfWork();

        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<PayrollRun> processResult =
                    PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            // Ensure processing was succsessful
            assertSuccess("submitPayroll", processResult);
        }


        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyBankAccount cba1 = CompanyBankAccount.findCompanyBankAccount(company1, "123123");

        // Verify financial transactions before moving
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        assertEquals("Number of Financial Transactions:", 2, financialTransactions.size());

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
        ProcessResult<CompanyBankAccount> deactivateResult = PayrollServices.companyManager.deactivateCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727", "123123", true, false);
        assertTrue(deactivateResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Add second bank account
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070920000000");
        CompanyBankAccount cba2 = dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount2());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Set system time before cutoff time (17:10)
        PSPDate.setPSPTime("20070925160000");
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                "123272727", "1231232", false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        // Verify financial transactions after moving
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        assertEquals("Number of Financial Transactions:", 2, financialTransactions.size());

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
                    String strExpectedRecordData = "627" + financialTransaction.getDebitBankAccount()
                            .getRoutingNumber() + StringFormatter
                            .formatString(financialTransaction.getDebitBankAccount().getAccountNumber(),
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

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testMovingPendingTransactionsAfterCutoff() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Collection<PayrollRunDTO> payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123272727();
        PayrollServices.commitUnitOfWork();

        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<PayrollRun> processResult =
                    PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            // Ensure processing was succsessful
            assertSuccess("submitPayroll", processResult);
        }

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyBankAccount cba1 = CompanyBankAccount.findCompanyBankAccount(company1, "123123");

        // Verify financial transactions before moving
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        assertEquals("Number of Financial Transactions:", 2, financialTransactions.size());

        List<SpcfUniqueId> entryDetailIdsBefore = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdDebit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion bank account", financialTransaction.getDebitBankAccount(),
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
        ProcessResult<CompanyBankAccount> deactivateResult = PayrollServices.companyManager.deactivateCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727", "123123", true, false);
        assertTrue(deactivateResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Add second bank account
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070920000000");
        company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);

        CompanyBankAccount cba2 = dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount2());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Set system time after cutoff time (17:10)
        PSPDate.setPSPTime("20070925171000");
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                "123272727", "1231232", false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        // Verify financial transactions after moving
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

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
                    String strExpectedRecordData = "627" + financialTransaction.getDebitBankAccount()
                            .getRoutingNumber() + StringFormatter
                            .formatString(financialTransaction.getDebitBankAccount().getAccountNumber(),
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

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testMoveTxnsCompanyOnCreditSide() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        // Add second bank account
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        CompanyBankAccount cba1 = CompanyBankAccount.findCompanyBankAccount(company1, "123123");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        PayrollRun payRun = PayrollRun.findPayrollRun(company1, "BatchId01");
        TransactionReturnTestDataLoader returnsLoader = new TransactionReturnTestDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = TransactionReturnTestDataLoader.persistTransactionReturns(c1FinTxns,
                "R02",
                "This is an EE Return");

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Company1 EEDDCR EX txns", 2, c1FinTxns.size());
        Assert.assertEquals("Number of Company1 EEDDCR Returns", 2, returnList.size());

        PayrollServices.beginUnitOfWork();

        company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);

        // Verify financial transactions before moving
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company);
        assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);

        List<SpcfUniqueId> entryDetailIdsBefore = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdRejectRefundCredit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getCreditBankAccount(),
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
        ProcessResult<CompanyBankAccount> deactivateResult = PayrollServices.companyManager.deactivateCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727", "123123", true, false);
        assertTrue(deactivateResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Add second bank account
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070920000000");
        company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);

        CompanyBankAccount cba2 = dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount2());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Set system time before cutoff time (17:10)
        PSPDate.setPSPTime("20070925160000");
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                "123272727", "1231232", false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        // Verify financial transactions after moving
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company);
        assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);

        List<SpcfUniqueId> entryDetailIdsAfter = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdRejectRefundCredit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getCreditBankAccount(),
                    cba2.getBankAccount());

            DomainEntitySet<EntryDetailRecord> entryDetails = financialTransaction.getMoneyMovementTransaction().getEntryDetailRecordCollection();

            assertEquals("Number Entry Details", 2, entryDetails.size());
            for (Iterator<EntryDetailRecord> iter = entryDetails.iterator(); iter.hasNext();) {
                EntryDetailRecord entryDetail = iter.next();
                entryDetailIdsAfter.add(entryDetail.getId());
                if (entryDetail.getCreditDebitIndicator() == CreditDebitCode.Credit) {
                    // verify the new entrydetail record data has destination bank account
                    String currencyString = StringFormatter
                            .formatCurrencyNoDecimalPoint(
                                    SpcfUtils.convertToBigDecimal(financialTransaction.getFinancialTransactionAmount()), 10);
                    String strExpectedRecordData = "622" + financialTransaction.getCreditBankAccount()
                            .getRoutingNumber() + StringFormatter
                            .formatString(financialTransaction.getCreditBankAccount().getAccountNumber(),
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

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testMoveTxnsFromDeactivatedCBA() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        // offload QBOE EE CR
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payRun = PayrollRun.findPayrollRun(company1, "BatchId01");
        TransactionReturnTestDataLoader returnsLoader = new TransactionReturnTestDataLoader();
        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = TransactionReturnTestDataLoader.persistTransactionReturns(c1FinTxns,
                "R02",
                "This is an EE Return");

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Number of Company1 EEDDCR EX txns", 2, c1FinTxns.size());
        Assert.assertEquals("Number of Company1 EEDDCR Returns", 2, returnList.size());

        PayrollServices.beginUnitOfWork();

        CompanyBankAccount cba1 = CompanyBankAccount.findCompanyBankAccount(company1, "123123");
        // Verify financial transactions before moving
        DomainEntitySet<FinancialTransaction> financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company);
        assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);

        List<SpcfUniqueId> entryDetailIdsBefore = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdRejectRefundCredit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getCreditBankAccount(),
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
        ProcessResult<CompanyBankAccount> deactivateResult = PayrollServices.companyManager.deactivateCompanyBankAccount(
                SourceSystemCode.QBOE, "123272727", "123123", true, false);
        assertTrue(deactivateResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Add second bank account
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070920000000");
        company1 = Company.findCompany("123272727", SourceSystemCode.QBOE);

        CompanyBankAccount cba2 = dataloader.persistCompanyBankAccount(company1, dataloader.getTestCompanyBankAccount2());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // Set system time before cutoff time (17:10)
        PSPDate.setPSPTime("20070925160000");
        ProcessResult result = PayrollServices.companyManager.movePendingTransactionsToBankAccount(SourceSystemCode.QBOE,
                "123272727", "1231232", false);

        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 0, result.getMessages().size());
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();

        // Verify financial transactions after moving
        financialTransactions =
                FinancialTransaction.findPendingFinancialTransactions(company1,
                        TransactionTypeCode.EmployerVerificationDebit,
                        TransactionStateCode.Created,
                        BankAccountOwnerType.Company);
        assertTrue("Number of Financial Transactions:", financialTransactions.size() == 1);

        List<SpcfUniqueId> entryDetailIdsAfter = new ArrayList<SpcfUniqueId>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployerDdRejectRefundCredit,
                    financialTransaction.getTransactionType().getTransactionTypeCd());
            assertEquals("Financial Transacion debit bank account", financialTransaction.getCreditBankAccount(),
                    cba2.getBankAccount());

            DomainEntitySet<EntryDetailRecord> entryDetails = financialTransaction.getMoneyMovementTransaction().getEntryDetailRecordCollection();

            assertEquals("Number Entry Details", 2, entryDetails.size());
            for (Iterator<EntryDetailRecord> iter = entryDetails.iterator(); iter.hasNext();) {
                EntryDetailRecord entryDetail = iter.next();
                entryDetailIdsAfter.add(entryDetail.getId());
                // verify the new entrydetail record data has destination bank account
                if (entryDetail.getCreditDebitIndicator() == CreditDebitCode.Credit) {
                    String currencyString = StringFormatter
                            .formatCurrencyNoDecimalPoint(
                                    SpcfUtils.convertToBigDecimal(financialTransaction.getFinancialTransactionAmount()), 10);
                    String strExpectedRecordData = "622" + financialTransaction.getCreditBankAccount()
                            .getRoutingNumber() + StringFormatter
                            .formatString(financialTransaction.getCreditBankAccount().getAccountNumber(),
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

        PayrollServices.commitUnitOfWork();

    }
}
