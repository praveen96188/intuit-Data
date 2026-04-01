package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.NoticeOfChangeUtils;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertCount;
import static java.lang.System.out;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

/**
 * User: mvillani
 * Date: Sep 26, 2007
 * Time: 11:25:59 AM
 */

public class PayrollSubmitCoreTests {

    int backDatedPayrollPaymentOffset = SystemParameter.findIntValue(SystemParameter.Code.BACKDATED_PAYROLL_INITIATION_DATE_OFFSET);


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSubmitPayroll() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        System.out.println("Payroll Submit Starts Here");

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);
    }

    @Test
    public void testRecordPayroll() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        // Verify persisted data
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        // Ensure that Payroll Run was created correctly
        PayrollRun payrollRun = PayrollRun
                .findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        assertTrue("PayrollRun Not Null", payrollRun != null);

        SpcfMoney payrollRunAmount = new SpcfMoney("0.00");
        if (payrollRun != null) {
            assertEquals("PayrollRun Id:", payrollRun.getSourcePayRunId(), payrollRunDTO.getPayrollTXBatchId());

            // Ensure that Paychecks were created correctly
            assertEquals("Number of Paychecks:", payrollRun.getPaycheckCollection().size(),
                    payrollRunDTO.getPaychecks().size());
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                Paycheck paycheck = Paycheck.findPaycheck(company, paycheckDTO.getPaycheckId());

                assertTrue("Paycheck Not Null", paycheck != null);
                if (paycheck != null) {
                    assertEquals("Employee Id:", paycheckDTO.getEmployeeId(),
                            paycheck.getDDEmployee().getSourceEmployeeId());
                    assertEquals("Number of Paycheck Splits:", paycheckDTO.getDdTransactions().size(),
                            paycheck.getPaycheckSplits().size());
                    assertEquals("Paycheck Net Amount :", paycheckDTO.getPaycheckNetAmount(),
                            paycheck.getNetAmount());

                    // Ensure that Paycheck Splits were created correctly
                    for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                        PaycheckSplit paycheckSplit = PaycheckSplit
                                .findNonCanceledPaycheckSplit(company, ddTransactionDTO.getDDTransactionId());
                        assertTrue(paycheckSplit != null);
                        if (paycheckSplit != null) {
                            payrollRunAmount = new SpcfMoney(
                                    payrollRunAmount.add(paycheckSplit.getPaycheckSplitAmount()));
                            assertEquals("Paycheck Split Amount:",
                                    SpcfUtils.convertToSpcfMoney(ddTransactionDTO.getDDTransactionAmount()),
                                    paycheckSplit.getPaycheckSplitAmount());
                            assertEquals("EmployeeBankAccount:",
                                    ddTransactionDTO.getEmployeeBankAccount().getEmployeeBankAccountId(),
                                    paycheckSplit.getEmployeeBankAccount().getSourceBankAccountId());
                            // Ensure that Financial Transactions were created correctly
                            DomainEntitySet<FinancialTransaction> financialTransactions = paycheckSplit.getFinancialTransactions();
                            assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());
                            FinancialTransaction financialTransaction = (FinancialTransaction) financialTransactions
                                    .get(0);
                            assertEquals("Financial Transaction Amount:", paycheckSplit.getPaycheckSplitAmount(),
                                    financialTransaction.getFinancialTransactionAmount());
                            assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployeeDdCredit,
                                    financialTransaction.getTransactionType().getTransactionTypeCd());
                            assertEquals("Financial Transaction State:", TransactionStateCode.Created,
                                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

                            SpcfCalendar expectedInitDate = SpcfCalendar
                                    .createInstance(2007, SpcfCalendar.September, 28, 0, 0, 0, 0,
                                            SpcfTimeZone.getLocalTimeZone());
                            SpcfCalendar expectedSettlementDate = SpcfCalendar
                                    .createInstance(2007, SpcfCalendar.October, 2, 0, 0, 0, 0,
                                            SpcfTimeZone.getLocalTimeZone());
                            TransactionType eeddCR = Application
                                    .findById(TransactionType.class, TransactionTypeCode.EmployeeDdCredit);
                            IntuitBankAccount expectedIntuitBA = IntuitBankAccount.findIntuitBankAccount(eeddCR, CreditDebitCode.Debit);

                            MoneyMovementTransaction mmTxn = financialTransaction.getMoneyMovementTransaction();
                            validateNewMMTxn(mmTxn, paycheckSplit.getPaycheckSplitAmount(), expectedSettlementDate, expectedInitDate,
                                    financialTransaction);

                            DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();
                            assertEquals("Num entry detail records", 2, entryDetailRecords.size());
                            EntryDetailRecord entryDetailRecord = entryDetailRecords.iterator().next();
                            String currencyString = StringFormatter
                                    .formatCurrencyNoDecimalPoint(
                                            SpcfUtils.convertToBigDecimal(paycheckSplit.getPaycheckSplitAmount()), 10);
                            String middleName = paycheck.getDDEmployee().getMiddleName();
                            if (middleName != null) {
                                middleName = " " + middleName;
                            } else {
                                middleName = "";
                            }
                            String strExpectedRecordData = "622" + ddTransactionDTO.getEmployeeBankAccount()
                                    .getBankAccount().getRoutingNumber() + StringFormatter
                                    .formatString(ddTransactionDTO.getEmployeeBankAccount()
                                            .getBankAccount().getAccountNumber(),
                                            17) + currencyString + StringFormatter
                                    .formatString(ddTransactionDTO.getEmployeeBankAccount()
                                            .getBankAccount().getAccountNumber(), 15) + StringFormatter
                                    .formatString(paycheck.getDDEmployee().getLastName() + ", " + paycheck.getDDEmployee()
                                            .getFirstName() + middleName, 22) + "  0";
                            if (entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Credit)) {
                                validateNewEntryDetailRecord(entryDetailRecord, paycheckSplit.getPaycheckSplitAmount(), company, null, mmTxn, strExpectedRecordData);
                            } else if (entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Debit)) {
                                validateNewEntryDetailRecord(entryDetailRecord, paycheckSplit.getPaycheckSplitAmount(), company, expectedIntuitBA, mmTxn, null);
                            }
                        }
                    }
                }
            }

        }

        //Persistence test for the employer financial transaction
        CompanyBankAccount company1BankAccount = CompanyBankAccount
                .findCompanyBankAccount(company, "123123");
        SpcfCalendar expectedInitDate = SpcfCalendar
                .createInstance(2007, SpcfCalendar.September, 25, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar expectedSettlementDate = SpcfCalendar
                .createInstance(2007, SpcfCalendar.September, 26, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        TransactionType erddDB = Application.findById(TransactionType.class, TransactionTypeCode.EmployerDdDebit);
        IntuitBankAccount expectedIntuitBA = IntuitBankAccount.findIntuitBankAccount(erddDB, CreditDebitCode.Credit);

        DomainEntitySet<FinancialTransaction> foundFinTxns = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        assertEquals("Financial Txn Collection size", 1, foundFinTxns.size());

        FinancialTransaction erDDDB = foundFinTxns.get(0);
        assertEquals("Company", company, erDDDB.getCompany());
        assertEquals("Credit Bank Account", expectedIntuitBA.getBankAccount(), erDDDB.getCreditBankAccount());
        assertEquals("Credit Bank Account Type", BankAccountOwnerType.Intuit, erDDDB.getCreditBankAccountType());
        assertEquals("Current Transaction State", TransactionStateCode.Created,
                erDDDB.getCurrentTransactionState().getTransactionStateCd());
        assertEquals("Debit Bank Account", company1BankAccount.getBankAccount(), erDDDB.getDebitBankAccount());
        assertEquals("Debit Bank Account Type", BankAccountOwnerType.Company, erDDDB.getDebitBankAccountType());
        assertEquals("Txn Amount", payrollRunAmount, erDDDB.getFinancialTransactionAmount());
        assertEquals("Num of fin txn states", 1, erDDDB.getFinancialTransactionStates().size());
        assertEquals("Settlement Date", expectedSettlementDate, erDDDB.getSettlementDate().toLocal());
        assertEquals("Settlement Type", SettlementType.ACH, erDDDB.getSettlementTypeCd());
        assertEquals("Transaction Type", TransactionTypeCode.EmployerDdDebit,
                erDDDB.getTransactionType().getTransactionTypeCd());

        MoneyMovementTransaction mmTxn = erDDDB.getMoneyMovementTransaction();
        assertNotNull(mmTxn);

        validateNewMMTxn(mmTxn, payrollRunAmount, expectedSettlementDate, expectedInitDate, erDDDB);

        DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection();
        assertEquals("Num entry detail records", 2, entryDetailRecords.size());
        EntryDetailRecord entryDetailRecord = entryDetailRecords.iterator().next();
        String currencyString = StringFormatter
                .formatCurrencyNoDecimalPoint(SpcfUtils.convertToBigDecimal(payrollRunAmount), 10);
        String strExpectedRecordData = "627" + company1BankAccount.getBankAccount().getRoutingNumber() + StringFormatter
                .formatString(company1BankAccount.getBankAccount().getAccountNumber(),
                        17) + currencyString + StringFormatter.formatString(company
                .getFedTaxId(), 15) + StringFormatter.formatString(company.getLegalName(), 22) + "  0";

        if (entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Credit)) {
            validateNewEntryDetailRecord(entryDetailRecord, payrollRunAmount, company, expectedIntuitBA, mmTxn, null);
        } else {
            validateNewEntryDetailRecord(entryDetailRecord, payrollRunAmount, company, null, mmTxn, strExpectedRecordData);
        }


        PayrollServices.commitUnitOfWork();
    }

    public static void validateNewMMTxn(MoneyMovementTransaction pMMTxn, SpcfMoney pExpectedAmount,
                                        SpcfCalendar pExpectedSettlementDate, SpcfCalendar pExpectedInitDate,
                                        FinancialTransaction pExpectedFinTxn) {
        assertNotNull(pMMTxn);

        assertEquals("Amount", pExpectedAmount, pMMTxn.getMoneyMovementTransactionAmount());
        //assertEquals("Deposit Frequency", null, pMMTxn.getDepositFrequency());
        //assertEquals("Offload Batch", null, pMMTxn.getOffloadBatch());
        assertEquals("Due Date", pExpectedSettlementDate.getTimeInMilliseconds(), pMMTxn.getDueDate().toLocal().getTimeInMilliseconds());
        assertEquals("Initiation Date", pExpectedInitDate, pMMTxn.getInitiationDate().toLocal());
        assertEquals("Payment method", PaymentMethod.ACHDirectDeposit, pMMTxn.getMoneyMovementPaymentMethod());
        assertEquals("Status", PaymentStatus.Created, pMMTxn.getStatus());
    }

    public static void validateNewEntryDetailRecord(EntryDetailRecord pEntryDetailRecord, SpcfMoney pExpectedAmount,
                                                    Company pCompany, IntuitBankAccount pExpectedCRIBA,
                                                    MoneyMovementTransaction pMMtxn,
                                                    String pRecordData) {
        assertEquals("Amount", pExpectedAmount, pEntryDetailRecord.getAmount());
        assertEquals("Company", pCompany, pEntryDetailRecord.getCompany());
        assertEquals("CR Intuit bank account", pExpectedCRIBA, pEntryDetailRecord.getIntuitBankAccount());
        assertEquals("CR trace num", null, pEntryDetailRecord.getTraceNumber());
        //assertEquals("DB Intuit bank account", pExpectedDBIBA, pEntryDetailRecord.getDebitIntuitBankAccount());
        //assertEquals("DB trace num", null, pEntryDetailRecord.getDebitTraceNumber());
        //assertEquals("NACHA File", null, pEntryDetailRecord.getNACHAFile());

        assertEquals("Record data", pRecordData, pEntryDetailRecord.getRecordData());
        assertEquals("Associated mm txn", pMMtxn, pEntryDetailRecord.getMoneyMovementTransaction());
    }

    /**
     * Test message 183 - PayrollRun already exists
     */
    @Test
    public void testRecordDuplicatePayroll() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        // Try to create another PayrollRun with the same ID
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRunDTO duplicatePayrollRunDTO = psdl.createPayrollRunDTO(company,
                company.getCompanyBankAccountCollection().iterator().next(), payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", duplicatePayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "183", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Payroll Run BatchId01 in company QBOE:123272727 already exists.",
                message.getMessage());
    }

    /**
     * Test message 10002 - Paycheck duplicated in the request
     */
    @Test
    public void testDuplicatePaycheckId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        Iterator<PaycheckDTO> iter = payrollRunDTO.getPaychecks().iterator();
        PaycheckDTO firstPaycheck = iter.next();
        PaycheckDTO secondPaycheck = iter.next();
        secondPaycheck.setPaycheckId(firstPaycheck.getPaycheckId());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "10002", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "PaycheckID " + firstPaycheck
                .getPaycheckId() + " in Company QBOE:123272727 was duplicated in the request.", message.getMessage());
    }

    /**
     * Test Paychecks not rejected if cancelled for OE customers.
     */
    @Test
    public void testDuplicatePaycheckIdSupportsResubmitOfCancelled() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);
        TransactionCancelEEDTO pDto = new TransactionCancelEEDTO();

        ArrayList paycheckCancelList = new ArrayList();
        for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
            paycheckCancelList.add(paycheck.getPaycheckId());
        }

        pDto.setSourcePaycheckIdList(paycheckCancelList);
        pDto.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", pDto);
        // Remove Agent from Principal
        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO.setPayrollTXBatchId("test2");
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

    }

    /**
     * Test Paychecks rejected when cancelled for QBDT customers.
     */
    @Test
    public void testDuplicatePaycheckIdRejectsResubmitOfCancelled() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);
        TransactionCancelEEDTO pDto = new TransactionCancelEEDTO();

        ArrayList paycheckCancelList = new ArrayList();
        for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
            paycheckCancelList.add(paycheck.getPaycheckId());
        }

        pDto.setSourcePaycheckIdList(paycheckCancelList);
        pDto.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, "123272727", pDto);
        // Remove Agent from Principal
        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();
        assertTrue(result.isSuccess());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO.setPayrollTXBatchId("test2");
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", processResult.getMessages().size() > 0);
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "184", message.getMessageCode());

    }

    /**
     * Test message 184 - Paycheck already exists
     */
    @Test
    public void testPaycheckAlreadyExists() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        // Try to create another PayrollRun with the same ID
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRunDTO secondPayrollRun = psdl
                .createPayrollRunDTO(company, company.getCompanyBankAccountCollection().iterator().next(), "BatchId02");
        PaycheckDTO firstPaycheckDTO = payrollRunDTO.getPaychecks().iterator().next();
        secondPayrollRun.getPaychecks().iterator().next().setPaycheckId(firstPaycheckDTO.getPaycheckId());
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", secondPayrollRun);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "184", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Paycheck " + firstPaycheckDTO
                .getPaycheckId() + " in company QBOE:123272727 already exists.", message.getMessage());
    }

    /**
     * Test message 10001 - Paycheck split duplicated in the request
     */
    @Test
    public void testDuplicatePaycheckSplitId() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        Iterator<PaycheckDTO> iter = payrollRunDTO.getPaychecks().iterator();
        PaycheckDTO firstPaycheck = iter.next();
        PaycheckDTO secondPaycheck = iter.next();
        String duplicateId = secondPaycheck.getDdTransactions().iterator().next().getDDTransactionId();
        firstPaycheck.getDdTransactions().iterator().next().setDDTransactionId(duplicateId);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "10001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "DDTransactionID " + duplicateId
                + " in Company QBOE:123272727 was duplicated in the request.", message.getMessage());
    }

    /**
     * Test message 170 - Company Bank Account Does Not Exist
     */
    @Test
    public void testCompanyBankAccountDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        // Change the Company Bank Account to an invalid one
        payrollRunDTO.getCompanyBankAccounts().iterator().next().getCompanyBankAccount()
                .setCompanyBankAccountID("InvalidBankAccount");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "170", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account InvalidBankAccount for company QBOE:123272727 does not exist.",
                message.getMessage());
    }

    /**
     * Test message 186 - Company Bank Account is not Active
     */
    @Test
    public void testCompanyBankAccountIsNotActive_Deactivated() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        //Deactivate Company Bank Account
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBOE, "123272727", "123123", true, false);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("Deactivate Company Bank Account", result);

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        System.out.println("Errors " + processResult.getMessages());

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "186", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account 123123 for company QBOE:123272727 is not active in PSP.",
                message.getMessage());
    }

    /**
     * Test error message - CBA is PendingVerification
     */
    @Test
    public void testCompanyBankAccountIsNotActive_PendingVerification() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();


        DataLoader dataLoader = new DataLoader();
        dataLoader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);

        Company company = dataLoader.persistTestActiveCompany();
        dataLoader.persistTestCompanyService(company);

        // create the CBA, but don't verify it
        ProcessResult<CompanyBankAccount> prCBA = PayrollServices.companyManager.addCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), dataLoader.getTestCompanyBankAccount(), true, true);
        CompanyBankAccount cba = prCBA.getResult();
        assertEquals("CBA status", BankAccountStatus.PendingVerification, cba.getStatusCd());

        CompanyService cs = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
        assertEquals("ServiceSubStatus", ServiceSubStatusCode.PendingBankVerification, cs.getStatusCd());

        // skip the PIN

        // Create Employees and Employee Bank Accounts
        company = Application.findById(Company.class, company.getId()); // refresh
        GenerateData.generateEmployees(company, 2);
        GenerateData.generateEmployeeBankAccounts(GenerateData.getEmployeeCollection(company.getDirectDepositEmployees()), 1, "Active");

        PayrollServices.commitUnitOfWork();

        // try to submit a payroll
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId()); // refresh
        PayrollRunDTO dtoPayroll = psdl.createPayrollRunDTO(company, cba, "BatchId01");
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoPayroll);
        PayrollServices.commitUnitOfWork();

        assertErrorsInclude("submitPayroll()", "1101", prPayroll);
        assertErrorsInclude("submitPayroll()", "186", prPayroll);
    }

    /**
     * Test error message - "Company currently has a status of Terminated and cannot submit transactions"
     */
    @Test
    public void testCompany_Terminated() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO dtoPayroll = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        // try to submit a payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult prTerm = PayrollServices.companyManager.terminateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("terminateCompany()", prTerm);

        // try to submit a payroll
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoPayroll);
        PayrollServices.commitUnitOfWork();

        assertErrorsInclude("submitPayroll()", "1101", prPayroll);
    }

    /**
     * Test error message - "Company currently has a status of Cancelled and cannot submit transactions"
     */
    @Test
    public void testCompany_ServiceCancelled() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO dtoPayroll = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        // try to submit a payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult prDeactivate = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "123272727", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("deactivateService()", prDeactivate);

        // try to submit a payroll
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoPayroll);
        PayrollServices.commitUnitOfWork();

        assertErrorsInclude("submitPayroll()", "1101", prPayroll);
    }

    private void assertErrorsInclude(String pText, String pErrorCode, ProcessResult pResult) {
        //System.out.println(pText+": Looking for error code "+pErrorCode+":");
        boolean bFound = false;
        for (Message msg : pResult.getMessages()) {
            //System.out.println(msg.toString());
            if (msg.getMessageCode().equals(pErrorCode)) {
                bFound = true;
            }
        }
        if (!bFound) {
            fail(pText + ": message with error code " + pErrorCode + " not found among " + pResult.getMessages().size() + " messages");
        }
    }

    /**
     * Test message 168 - Employee does not exist
     */
    @Test
    public void testEmployeeDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        //Set invalid employee id
        payrollRunDTO.getPaychecks().iterator().next().setEmployeeId("InvalidEmployeeId");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "168", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Employee InvalidEmployeeId for company QBOE:123272727 does not exist.",
                message.getMessage());
    }


    /**
     * Test message 178 - Employee is not active
     */
    @Test
    public void testEmployeeIsNotActive() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        //Set employee status to "Inactive"
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        Employee emp1 = null;
        for (Employee emp : company.getDirectDepositEmployees()) {
            if (emp.getSourceEmployeeId().equals("Emp1")) {
                emp1 = emp;
                break;
            }
        }
        PayrollServices.employeeManager.deactivateEmployee(SourceSystemCode.QBOE, "123272727", "Emp1", null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "178", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Employee Emp1 for company QBOE:123272727 is not active.", message.getMessage());


    }

    /**
     * Test message 166 - Employee Bank Account Not Found
     * Employee Bank Account does not exist and no bank account information is provided
     */
    @Test
    public void testEmployeeBankAccountNotFound() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        //Set invalid employee bank account id
        EmployeeBankAccountDTO employeeBankAccountDTO = null;
        for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
            if (paycheck.getEmployeeId().equals("Emp1")) {
                employeeBankAccountDTO = paycheck.getDdTransactions().iterator().next().getEmployeeBankAccount();
                break;
            }
        }
        employeeBankAccountDTO.setEmployeeBankAccountId("InvalidBankAccountId");
        employeeBankAccountDTO.setBankAccount(null);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "166", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account InvalidBankAccountId for employee Emp1 does not exist.",
                message.getMessage());
    }


    /**
     * Test message 187 - Employee Bank Account is not active
     * Employee Bank Account is inactive and no bank account information is provided
     */

    @Test
    public void testEmployeeBankAccountIsNotActive() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
            if (paycheck.getEmployeeId().equals("Emp1")) {
                paycheck.getDdTransactions().iterator().next().getEmployeeBankAccount().setBankAccount(null);
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        //Set employee bank account status to "Inactive"
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        for (Employee emp : company.getDirectDepositEmployees()) {
            if (emp.getSourceEmployeeId().equals("Emp1")) {
                emp.getEmployeeBankAccountCollection().get(0).setStatusCd(BankAccountStatus.Inactive);
                break;
            }
        }
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "187", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Bank Account Emp1Acct1 for employee Emp1 is not active in the PSE.",
                message.getMessage());
    }

    /**
     * Employee Bank Account does not exist and  bank account information is provided
     * A new bank account for the employee has to be added
     */
    @Test
    public void testAddEmployeeBankAccountNotFound() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        //Set new employee bank account id
        EmployeeBankAccountDTO employeeBankAccountDTO = payrollRunDTO.getPaychecks().iterator().next()
                .getDdTransactions().iterator().next().getEmployeeBankAccount();
        employeeBankAccountDTO.setEmployeeBankAccountId("NewBankAccountId");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);
    }


    /**
     * Employee Bank Account is inactive and  bank account information is provided
     * A new employee bank account has to be added
     */

    @Test
    public void testAddEmployeeBankAccountIsNotActive() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        EmployeeBankAccountDTO employeeBankAccountDTO = payrollRunDTO.getPaychecks().iterator().next()
                .getDdTransactions().iterator().next().getEmployeeBankAccount();
        PayrollServices.commitUnitOfWork();

        //Set employee bank account status to "Inactive"
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        company.getDirectDepositEmployees().get(0).getEmployeeBankAccountCollection().get(0)
                .setStatusCd(BankAccountStatus.Inactive);
        PayrollServicesTest.save(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);
    }


    /**
     * Employee Bank Account is active and  bank account information is provided
     * Scenario 1 - Only bank account name has changed - current account is updated
     * Bank account has to be updated
     */

    @Test
    public void testUpdateEmployeeBankAccountBankAcctName() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        payrollRunDTO.getPaychecks().iterator().next().
                getDdTransactions().iterator().next().getEmployeeBankAccount().getBankAccount().setBankName("New Bank");


        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        //Ensure Employee Bank Account has the updated value
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = company.getDirectDepositEmployees().get(0).getEmployeeBankAccountCollection();

        BankAccount bankAccount = company.getDirectDepositEmployees().get(0).getEmployeeBankAccountCollection().get(0).getBankAccount();

        assertTrue("Number of Employee Bank Accounts", employeeBankAccounts.size() == 1);
        assertEquals("Bank Name", "New Bank", bankAccount.getBankName());
        PayrollServices.commitUnitOfWork();

    }


    /**
     * Employee Bank Account is active and  bank account information is provided
     * Scenario 2 - Something other than bank account name has changed
     * Bank account has to be updated - current account is expired and a new one is created
     */

    @Test
    public void testUpdateEmployeeBankAccount() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        Collection<PaycheckDTO> paycheckDTOCollection = payrollRunDTO.getPaychecks();

        for (PaycheckDTO paycheckDTO : paycheckDTOCollection){
            if (paycheckDTO.getEmployeeId().equals("Emp2")) {
                Collection<DDTransactionDTO> ddTransactionDTOS = paycheckDTO.getDdTransactions();
                DDTransactionDTO ddTransactionDTO = ddTransactionDTOS.iterator().next();
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setBankName("New Bank");
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setAccountNumber("New Acct Number");
                break;
            }
        }

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        //Ensure Employee Bank Account has the updated value
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<Employee> directDepositEmployees = company.getDirectDepositEmployees();

        Employee targetEmployee = null;

        for (Employee employee : directDepositEmployees) {
            if (employee.getSourceEmployeeId().equals("Emp2")){
                targetEmployee = employee;
                break;
            }
        }

        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = targetEmployee.getEmployeeBankAccountCollection();
        EmployeeBankAccount activeBankAccount = null;
        EmployeeBankAccount inactiveBankAccount = null;
        for (EmployeeBankAccount eba : employeeBankAccounts) {
            if (eba.getStatusCd().equals(BankAccountStatus.Active)) {
                activeBankAccount = eba;
            } else {
                inactiveBankAccount = eba;
            }
        }

        assertTrue("Number of Employee Bank Accounts", employeeBankAccounts.size() == 2);
        assertTrue("Inactive Bank Account", inactiveBankAccount != null);
        assertTrue("Active Bank Account", activeBankAccount != null);
        assertEquals("Bank Name", "New Bank", activeBankAccount.getBankAccount().getBankName());
        assertEquals("Account Number", "New Acct Number", activeBankAccount.getBankAccount().getAccountNumber());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Employee Bank Account is active and  bank account information is not provided
     */

    @Test
    public void testUpdateEmployeeBankAccountBankAccountInfoNotProvided() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        EmployeeBankAccountDTO eeBankAccountDTO = payrollRunDTO.getPaychecks().iterator().next().
                getDdTransactions().iterator().next().getEmployeeBankAccount();
        eeBankAccountDTO.setBankAccount(null);


        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        //Ensure Employee Bank Account has the updated value
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = company.getDirectDepositEmployees().get(0).getEmployeeBankAccountCollection();

        assertTrue("Number of Employee Bank Accounts", employeeBankAccounts.size() == 1);
        PayrollServices.commitUnitOfWork();

    }

    /**
     * Test Paycheck Date on Weekend - Paycheck Settlement Date should be adjusted to the next
     * business day
     */
    @Test
    public void testPaycheckDateOnWeekend() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 29);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());

        PayrollRun payrollRun = processResult.getResult();

        // validate original paycheck date
        assertTrue("Original Paycheck Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckDate().toLocal(), SpcfCalendar.createInstance(2007, SpcfCalendar.September, 29)) == 0);

        // validate adjusted payroll settlement date

        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2007, SpcfCalendar.October, 1);
        assertTrue("New Settlement Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckSettlementDate().toLocal(), newSettlementDate) == 0);
    }

    /**
     * Test Paycheck Date on a Holiday-  Paycheck Settlement Date should be adjusted to the next
     * business day
     */
    @Test
    public void testPaycheckDateOnHoliday() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.JULY, 4);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);
        PayrollRun payrollRun = processResult.getResult();

        // validate original paycheck date
        assertTrue("Original Paycheck Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckDate().toLocal(), SpcfCalendar.createInstance(2007, SpcfCalendar.July, 4)) == 0);

        // validate adjusted payroll settlement date

        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2007, SpcfCalendar.July, 5, SpcfTimeZone.getLocalTimeZone());
        assertTrue("New Settlement Date:", CalendarUtils.getDifferenceInDays(
                payrollRun.getPaycheckSettlementDate().toLocal(), newSettlementDate) == 0);
    }

    /**
     * Test Paycheck Date on a Holiday-  Paycheck Settlement Date should be adjusted to the next
     * business day
     */
    @Test
    public void testSubmitDateOnHoliday() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 7, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.JULY, 11);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);
        PayrollRun payrollRun = processResult.getResult();

        // validate original paycheck date
        assertTrue("Original Paycheck Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckDate().toLocal(), SpcfCalendar.createInstance(2007, SpcfCalendar.July, 11)) == 0);

        // validate adjusted payroll settlement date
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2007, SpcfCalendar.July, 12, SpcfTimeZone.getLocalTimeZone());
        assertTrue("New Settlement Date:", CalendarUtils.getDifferenceInDays(
                payrollRun.getPaycheckSettlementDate().toLocal(), newSettlementDate) == 0);

        // validate backdated payroll event present
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(payrollRun.getCompany(), EventTypeCode.BackdatedPayrollReceived, CompanyEventStatus.Active, true);
        assertEquals(1, events.size());
        events = CompanyEvent.findCompanyEvents(payrollRun.getCompany(), EventTypeCode.PayrollReceived, CompanyEventStatus.Active, true);
        assertEquals(1, events.size());
    }

    /**
     * Test Paycheck Date on a Holiday-  Paycheck Settlement Date should be adjusted to the next
     * business day
     */
    @Test
    public void testHolidayBetweenSubmitAndSettlement() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 6, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 7, 3, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.JULY, 10);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);
        PayrollRun payrollRun = processResult.getResult();

        // validate original paycheck date
        assertTrue("Original Paycheck Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckDate().toLocal(), SpcfCalendar.createInstance(2007, SpcfCalendar.July, 10)) == 0);

        // validate adjusted payroll settlement date
        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2007, SpcfCalendar.July, 11, SpcfTimeZone.getLocalTimeZone());
        assertTrue("New Settlement Date:", CalendarUtils.getDifferenceInDays(
                payrollRun.getPaycheckSettlementDate().toLocal(), newSettlementDate) == 0);

        // validate backdated payroll event present
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(payrollRun.getCompany(), EventTypeCode.BackdatedPayrollReceived, CompanyEventStatus.Active, true);
        assertEquals(1, events.size());
        events = CompanyEvent.findCompanyEvents(payrollRun.getCompany(), EventTypeCode.PayrollReceived, CompanyEventStatus.Active, true);
        assertEquals(1, events.size());
    }


    /**
     * Test Backdated payroll
     */

    @Test
    public void testBackdatedPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.AUGUST, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());
        PayrollRun payrollRun = processResult.getResult();

        // validate original paycheck date
        assertTrue("Original Paycheck Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckDate().toLocal(), SpcfCalendar.createInstance(2007, SpcfCalendar.August, 15)) == 0);

        // validate adjusted payroll settlement date

        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2007, SpcfCalendar.August, 27);
        assertTrue("New Settlement Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckSettlementDate().toLocal(), newSettlementDate) == 0);

    }

    /**
     * Test Past Cutoff Time Same Day
     */

    @Test
    public void testNextValidCheckDate_PastCutoffSameDay() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        //Set up the system date as 5 business days away from the check date one minute after the cutoff time (paycheck date is October 2)
        SpcfCalendar sysDate = SpcfCalendar
                .createInstance(2007, SpcfCalendar.September, 25, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar cutoffTime = company.getOffloadGroup()
                .getCalendarForCutoffTime(sysDate);
        sysDate = cutoffTime.copy();
        sysDate.addMinutes(1);
        PSPDate.setPSPTime(sysDate);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());
        PayrollRun payrollRun = processResult.getResult();

        // validate original paycheck date
        assertTrue("Original Paycheck Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckDate().toLocal(), SpcfCalendar.createInstance(2007, SpcfCalendar.October, 2)) == 0);

        // validate adjusted payroll settlement date

        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2007, SpcfCalendar.October, 3);
        assertTrue("New Settlement Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckSettlementDate().toLocal(), newSettlementDate) == 0);

    }

    /*
    *   Test before cutoff - same day
    */

    @Test
    public void testNextValidCheckDate_BeforeCutoffSameDay() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        //Set up the system date as 5 business days away from the check date, one minute before the cutoff time
        SpcfCalendar sysDate = SpcfCalendar
                .createInstance(2007, SpcfCalendar.September, 25, SpcfTimeZone.getLocalTimeZone());
        sysDate = company.getOffloadGroup()
                .getCalendarForCutoffTime(sysDate);
        sysDate.addMinutes(-1);
        PSPDate.setPSPTime(sysDate);


        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertTrue("Process Result", processResult.isSuccess());
        PayrollRun payrollRun = processResult.getResult();

        // validate original paycheck date
        assertTrue("Original Paycheck Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckDate().toLocal(), SpcfCalendar.createInstance(2007, SpcfCalendar.October, 2)) == 0);

        // validate adjusted payroll settlement date

        SpcfCalendar newSettlementDate = SpcfCalendar.createInstance(2007, SpcfCalendar.October, 2);
        assertTrue("New Settlement Date:", CalendarUtils.getDifferenceInDays(payrollRun.getPaycheckSettlementDate().toLocal(), newSettlementDate) == 0);
    }

    /*
   *  Test message 109 - Paycheck Date too far in the future
    */

    @Test
    public void testPaycheckDateTooFarInTheFuture() {
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(company, ServiceCode.DirectDeposit);

        // Get the max number of days a transaction can be warehoused  and
        // set up the system date more than this number of days before the check date

        SourcePayrollParameter maxWarehouseTransactionDays =
                SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.QBOE, SourcePayrollParameterCode.MaxWarehouseTransactionDays);
        int numberOfDays = Integer.parseInt(maxWarehouseTransactionDays.getParameterValue());

        SpcfCalendar sysDate = DateDTO.convertToSpcfCalendar(payrollRunDTO.getTargetPayrollTXDate());
        sysDate.addDays((numberOfDays + 1) * -1);

        PSPDate.setPSPTime(sysDate);


        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        // Commit
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "109", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", String.format("Transactions must not be dated more than %s days ahead of the current date.  Ensure all Liability Payments PERIOD Ending Date and/or Paychecks DATE are within the %s day range.", numberOfDays, numberOfDays),
                message.getMessage());
    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */

    public void payrollSubmitCompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "InvalidCompanyId", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());
    }

    @Test
    /**
     *  Test error message 177 - Company is not active
     */

    public void payrollSubmitCompanyNotActive() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Set company status to "Inactive"
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        // Deactivate company
        ProcessResult<CompanyService> cancelServiceProcessResult = PayrollServices.companyManager.deactivateService(
                company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        assertSuccess("deactivateService", cancelServiceProcessResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : processResult.getMessages()) {
            if (currMessage.getMessageCode().equals("1101")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "1101", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message",
                "The operation SubmitPayroll is not allowed for company QBOE:1234567 in its current state.",
                message.getMessage());
    }

    @Test
    public void testInvalidCompanyParameters() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(null, "123272727", payrollRunDTO);
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
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, null, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() >= 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }

    @Test
    public void testSubmitPayrollWithC04NOCPending() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C04", "");

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertFalse("Bank Account Number", bankAccount.getAccountNumber().equals(accountNumber));

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String employeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_NoBankAccountChange();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        // validate Message count - there should be a warning issued for the NOC Pending
        assertTrue("Number of Messages:", submitPayrollResult.getMessages().size() >= 1);
        assertSuccess(submitPayrollResult);
        assertEquals("Error Code", "301", submitPayrollResult.getMessages().get(0).getMessageCode());
        // Check that PayrollSubmit with NOC Pending Event has been created
        companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
        assertTrue("PayrollSubmittedWithPendingNOC Created", companyEventsList.size() == 1);
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("Employee Bank Account Id", employeeBankAccountId,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId));

        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testSubmitPayrollWithNOCPendingWithRollback() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadQBDTDataWithOneNOCReturn("C01", "", "EE1_1", "12100035825625625651325454321");

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Bank Account Number Rule
        assertFalse("Bank Account Number", bankAccount.getAccountNumber().equals(accountNumber));

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String employeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            assertEquals("New Account Number", bankAccount.getAccountNumber(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

        }
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_NoBankAccountChange();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        // validate Message count - there should be a warning issued for the NOC Pending
        assertTrue("Number of Messages:", submitPayrollResult.getMessages().size() >= 1);
        assertFalse(submitPayrollResult.isSuccess());
        assertEquals("Error Code", "2301", submitPayrollResult.getMessages().get(0).getMessageCode());
//        assertEquals("Error Message", "Maximum number of attemts(3) to submit payroll with a NOC return pending for employee ThirdCompEELast, ThirdCompEEFirst TMI has been exceeded.",
//                submitFourthPayrollResult.getMessages().get(0).getMessage());
        // Check that PayrollSubmit with NOC Pending Event has been created
        companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
        assertTrue("PayrollSubmittedWithPendingNOC Created", companyEventsList.size() == 1);
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("Employee Bank Account Id", employeeBankAccountId,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId));

        }
        Application.commitUnitOfWork();

    }

    @Test
    public void testSubmitPayrollWithNOCPending() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C01", "");

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String employeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_NoBankAccountChange();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        // validate Message count - there should be a warning issued for the NOC Pending
        assertTrue("Number of Messages:", submitPayrollResult.getMessages().size() >= 1);
        assertFalse(submitPayrollResult.isSuccess());
        // Check that PayrollSubmit with NOC Pending Event has not been created
        companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
        assertTrue("PayrollSubmittedWithPendingNOC Created", companyEventsList.size() == 1);
        Company company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);
        assertFalse("Company On Hold", company.isCompanyOnHold());

        // verify the error message
        assertEquals("Error Code", "2301", submitPayrollResult.getMessages().get(0).getMessageCode());
//        assertEquals("Error Message", "Maximum number of attemts(3) to submit payroll with a NOC return pending for employee ThirdCompEELast, ThirdCompEEFirst TMI has been exceeded.",
//                          submitFourthPayrollResult.getMessages().get(0).getMessage());
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals("Transaction Return Status", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
        Application.commitUnitOfWork();


        // Submit another payrollrun with a corrected bank account
        Application.beginUnitOfWork();
        payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_EEBankAccountChanged();
        payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 18);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        PayrollServicesTest.assertSuccess(submitPayrollResult);
        Application.commitUnitOfWork();

        // verify there are no active 'PayrollSubmittedWithPendingNOC' events
        PayrollServices.beginUnitOfWork();
        companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
        assertTrue("PayrollSubmittedWithPendingNOC Created", companyEventsList.size() == 0);
        // verify the NOC return is resolved
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals("Transaction Return Status", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testSubmitPayrollWithNOCPending_ChangeBankAccountType() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C01", "");

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String newAccountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                                                         EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                     transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String employeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_NoBankAccountChange();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        // validate Message count - there should be a warning issued for the NOC Pending
        assertTrue("Number of Messages:", submitPayrollResult.getMessages().size() >= 1);
        assertFalse(submitPayrollResult.isSuccess());
        // Check that PayrollSubmit with NOC Pending Event has not been created
        companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                           EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
        assertTrue("PayrollSubmittedWithPendingNOC Created", companyEventsList.size() == 1);
        Company company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);
        assertFalse("Company On Hold", company.isCompanyOnHold());

        // verify the error message
        assertEquals("Error Code", "2301", submitPayrollResult.getMessages().get(0).getMessageCode());
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals("Transaction Return Status", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
        Application.commitUnitOfWork();


        // Submit another payrollrun with a changed account type
        Application.beginUnitOfWork();
        payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_EEBankAccountChanged();
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setAccountNumber(oldAccountNumber);
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setAccountType(BankAccountType.Savings);
            }
        }
        payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 18);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        assertEquals("Error Code", "2301", submitPayrollResult.getMessages().get(0).getMessageCode());
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals("Transaction Return Status", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
        Application.commitUnitOfWork();
    }

    @Test
    public void testSubmitPayrollWithC05NOCPending_ChangeBankAccountType() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C05", "31");

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                                                                                 getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                                                         EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                     transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String employeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_NoBankAccountChange();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        // validate Message count - there should be a warning issued for the NOC Pending
        assertTrue("Number of Messages:", submitPayrollResult.getMessages().size() >= 1);
        assertFalse(submitPayrollResult.isSuccess());
        // Check that PayrollSubmit with NOC Pending Event has not been created
        companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                                                           EventTypeCode.PayrollSubmittedWithPendingNOC, CompanyEventStatus.Active, null, null);
        assertTrue("PayrollSubmittedWithPendingNOC Created", companyEventsList.size() == 1);
        Company company = Company
                .findCompany("8574536", SourceSystemCode.QBDT);
        assertFalse("Company On Hold", company.isCompanyOnHold());

        // verify the error message
        assertEquals("Error Code", "2301", submitPayrollResult.getMessages().get(0).getMessageCode());
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals("Transaction Return Status", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
        Application.commitUnitOfWork();


        // Submit another payroll run with a changed account type
        Application.beginUnitOfWork();
        payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_EEBankAccountChanged();
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setAccountNumber(oldAccountNumber);
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setAccountType(BankAccountType.Savings);
            }
        }
        payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 18);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, companyId, payrollRunDTO));
    }

    @Test
    public void testSubmitPayrollWithNOCCorrectedBankAccount() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadQBDTDataForNoticeOfChangeEmployeeBankAccount("C01", "");

        TransactionReturn transactionReturn = returnList.get(0);
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Update Bank Account Number Rule
        assertFalse("Bank Account Number", bankAccount.getAccountNumber().equals(accountNumber));

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String employeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            assertEquals("New Account Number", bankAccount.getAccountNumber(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_EEBankAccountChanged();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);

        PayrollServicesTest.assertSuccess(submitPayrollResult);
        Application.commitUnitOfWork();


    }

    @Test
    public void testSubmitPayrollWithNOCEENotInPayrollRun() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadQBDTDataWithOneNOCReturn("C01", "", "EE1_1", "12100035825625625651325454321");

        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Bank Account Number Rule
        assertFalse("Bank Account Number", bankAccount.getAccountNumber().equals(accountNumber));

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String employeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NOC),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Old Account Number", oldAccountNumber,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            assertEquals("New Account Number", bankAccount.getAccountNumber(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldAccountNumber));

            employeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.EmployeeBankAccountId);
            EmployeeBankAccount employeeBankAccount = PayrollServices.entityFinder.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(employeeBankAccountId));

            assertEquals("Employee Bank Account", bankAccount, employeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_NoEEWithNOC();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);
        PayrollServicesTest.assertSuccess(submitPayrollResult);

        Application.commitUnitOfWork();
    }


    /**
     * Test case to submit the payroll successfullly and submit additional payrolls of over the limits until exceeds
     * the Consecutive Limit Violation count to put the company On Hold DD Limit. And test that previous pending tax transactions are not put on hold
     */

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testSubmitPayrollsOvertheLimitWithTaxes() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        PayrollServices.commitUnitOfWork();
        String[] statelist = {"MA"};
        List<Company> companies = DataLoadServices.setupCompany(1234567L, 1, statelist, PaymentTemplateCategory.Withholding);
        Company company = companies.get(0);

        PayrollServices.beginUnitOfWork();
        c1DL.setCompany1withBankAccountEmployees(company);
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(2007, 10, 02);
        DataLoadServices.runPayrollRun(company, statelist, SpcfCalendar.createInstance(2007, 10, 02), new DateDTO("2007-10-02"), false);

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        //Submit additional payrolls of over the limit until exceeds the Consecutive Limit Violation Limit
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBDT:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBDT:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBDT:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBDT:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBDT);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);

        // This assert will fail because we need to check the On Hold Reason codes
        assertTrue(companyForFindingService.getOnHoldReasonCollection().size() == 1);
        Iterator<OnHoldReason> onHoldIterator = companyForFindingService.getOnHoldReasonCollection().iterator();
        OnHoldReason holdReason = onHoldIterator.next();
        assertEquals("Service Status", ServiceSubStatusCode.DirectDepositLimit, holdReason.getOnHoldReasonCd());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Offload the transaction
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        //Offload Employer Debit transaction
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> erDebitFinTxns = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> eeCreditFinTxns = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> agencyCreditFinTxns = FinancialTransaction.findFinancialTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        PayrollServices.commitUnitOfWork();

        assertEquals("EmployerDdDebit Executed Txn Collection size", 1, erDebitFinTxns.size());
        assertEquals("EmployeeDdCredit Executed Txn Collection size", 2, eeCreditFinTxns.size());
        assertEquals("AgencyTaxCredit Created Txn Collection size", 1, agencyCreditFinTxns.size());
    }

    @Test
    public void testSubmitPayrollsOvertheLimit() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        //Submit additional payrolls of over the limit until exceeds the Consecutive Limit Violation Limit
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        payrollRunDTO = c1DL.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Message size", 1, processResult.getMessages().size());
        assertEquals("Message code", "1043", processResult.getMessages().get(0).getMessageCode());
        assertEquals("Message code",
                "Payroll Run BatchTest01 for company QBOE:1234567 exceeded current DD limits and could not be processed.",
                processResult.getMessages().get(0).getMessage());

        PayrollServices.beginUnitOfWork();
        Company companyForFindingService = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        DDCompanyServiceInfo ddCompanyService = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(companyForFindingService, ServiceCode.DirectDeposit);

        // This assert will fail because we need to check the On Hold Reason codes
        assertTrue(companyForFindingService.getOnHoldReasonCollection().size() == 1);
        Iterator<OnHoldReason> onHoldIterator = companyForFindingService.getOnHoldReasonCollection().iterator();
        OnHoldReason holdReason = onHoldIterator.next();
        assertEquals("Service Status", ServiceSubStatusCode.DirectDepositLimit, holdReason.getOnHoldReasonCd());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Offload the transaction
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Offload Employee Credit transaction
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        //Offload Employer Debit transaction
        PSPDate.setPSPTime("20070925000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);

        DomainEntitySet<FinancialTransaction> erDebitFinTxns = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<FinancialTransaction> eeCreditFinTxns = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> agencyCreditFinTxns = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);
        PayrollServices.commitUnitOfWork();

        assertEquals("EmployerDdDebit Executed Txn Collection size", 1, erDebitFinTxns.size());
        assertEquals("EmployeeDdCredit Executed Txn Collection size", 2, eeCreditFinTxns.size());
    }

    @Test
    public void testSubmitPayrolls_DoesNotViolateLimits() {
        /*********************Begin setup******************************/
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070710000000");
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();

        PayrollRunDTO currentPayrollRunDTO = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-07-31"));
        Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionAmount(new BigDecimal("10500.00"));
            }

            SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
            for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
            }
            currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
        }

        //c1DL.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "1234567", currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        PayrollServices.beginUnitOfWork();
        c1DL.persistEmployeesAndBankAccounts();

        PayrollRunDTO payrollRunDTO = c1DL.getCompany1PR_MultiplePaycheckSplitsDifferentBA(new DateDTO("2007-08-08"));
        paychecks = payrollRunDTO.getPaychecks();
        BigDecimal amount = new BigDecimal("1000");
        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                amount = amount.add(new BigDecimal("3500"));
                currDDTxn.setDDTransactionAmount(amount);
                amount = new BigDecimal("0.00");
            }

            SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
            for (DDTransactionDTO currDDTxn : currPaycheck.getDdTransactions()) {
                SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
            }
            currPaycheck.setPaycheckNetAmount(totalPaycheckNetAmount);
        }
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

    }

    //PSRV001084

    @Test
    public void testSubmitPayrollForNOC_WithOutChangingEmployeeBankAccountInfo() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        TransactionReturn transactionReturn = loader.loadQBDTDataWithOneNOCReturn("C03", "", "EE1_1", "111000025               12345");

        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        returnHandler.execute(transactionReturn);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOCWithOutChanges, CompanyEventStatus.Active, null, null);

        //Assertion for Create NOCWithOutChanges Event
        assertEquals("Company Events", 1, companyEventsList.size());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        assertEquals("Transaction Return Status", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.loadPayrollQBDT_NoEEWithNOC();
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.SEPTEMBER, 15);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);

        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(
                SourceSystemCode.QBDT, companyId, payrollRunDTO);
        PayrollServicesTest.assertSuccess(submitPayrollResult);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInvalidLiabilityDTO() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Collection<PaycheckDTO> paycheckDTOs = payrollDTO.getPaychecks();
        Collection<LiabilityTransactionDTO> liabilityTransactions = paycheckDTOs.iterator().next().getLiabilityTransactions();
        LiabilityTransactionDTO invalidDTO = liabilityTransactions.iterator().next();
        invalidDTO.setLawId("Law Id with length greater than allowed 50 characters");

        invalidDTO.setLiabilityTaxableWages(null);
        invalidDTO.setLiabilityTotalWages(null);
        invalidDTO.setLiabilityAmount(null);
        LiabilityTransactionDTO anotherInvalidDTO = new LiabilityTransactionDTO();
        liabilityTransactions.add(anotherInvalidDTO);
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 8);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "LawId has invalid value", message.getMessage());
        message = processResult.getMessages().get(1);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "LiabilityTransactionAmount has invalid value", message.getMessage());
        message = processResult.getMessages().get(2);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "LiabilityTotalWages has invalid value", message.getMessage());
        message = processResult.getMessages().get(3);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "LiabilityTaxableWages has invalid value", message.getMessage());
        message = processResult.getMessages().get(4);
        assertEquals("Error Code:", "5001", message.getMessageCode());
    }

    @Test
    public void testInvalidCompensationTransactionDTO() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Collection<PaycheckDTO> paycheckDTOs = payrollDTO.getPaychecks();
        Collection<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
        CompensationTransactionDTO compensationDTO = new CompensationTransactionDTO();
        compensationTransactions.add(compensationDTO);
        paycheckDTOs.iterator().next().setCompensationTransactions(compensationTransactions);
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "SourceCompensationId has invalid value", message.getMessage());


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation);

        //Assertion for invalid amount
        PayrollServices.beginUnitOfWork();
        compensationTransactions = paycheckDTOs.iterator().next().getCompensationTransactions();
        CompensationTransactionDTO invalidDTO = compensationTransactions.iterator().next();
        invalidDTO.setSourcePayrollItemId("PayrollItem_1");
        invalidDTO.setCompensationAmount(null);
        invalidDTO.setHoursWorked(null);

        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count

        assertCount(1, processResult);

        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "CompensationAmount has invalid value", message.getMessage());
    }

    @Test
    public void testInvalidDeductionTransactionDTO() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Collection<PaycheckDTO> paycheckDTOs = payrollDTO.getPaychecks();
        Collection<DeductionTransactionDTO> deductions = new ArrayList<DeductionTransactionDTO>();
        DeductionTransactionDTO deductionDTO = new DeductionTransactionDTO();
        deductions.add(deductionDTO);
        paycheckDTOs.iterator().next().setDeductionTransactions(deductions);
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "SourceDeductionId has invalid value", message.getMessage());

        //Assertion for invalid amount
        PayrollServices.beginUnitOfWork();
        Collection<DeductionTransactionDTO> deductionTransactions = paycheckDTOs.iterator().next().getDeductionTransactions();
        DeductionTransactionDTO invalidDTO = deductionTransactions.iterator().next();
        invalidDTO.setSourcePayrollItemId("DED123");
        invalidDTO.setDeductionAmount(null);

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "DeductionAmount has invalid value", message.getMessage());
    }

    /**
     * Test case to submit the payroll with compensations & deductions.
     */

    @Test
    public void testSubmitPayroll_WithCompensationsAndDeductions() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        //Add Compensations amounts
        PaycheckDTO paycheckDTO = payrollRunDTO.getPaychecks().iterator().next();
        psdl.createPaycheckDTO_CompensationAmounts(paycheckDTO);

        //Add Deduction amounts
        psdl.createPaycheckDTO_DeductionAmounts(paycheckDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        assertNotNull(company);
        assertEquals("CompanyService", ServiceCode.DirectDeposit, company.getCompanyServiceCollection().get(0).getService().getServiceCd());
        assertEquals("Company Service Status", ServiceSubStatusCode.ActiveCurrent, company.getCompanyServiceCollection().get(0).getStatusCd());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxCredits = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of Agency Credit Transactions", 0, agencyTaxCredits.size());

        DomainEntitySet<FinancialTransaction> erTaxDebits = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Created);

        assertEquals("Number of Tax Debit Transactions", 0, erTaxDebits.size());

        DomainEntitySet<FinancialTransaction> erDebits = FinancialTransaction
                .findFinancialTransactions(
                        company.getSourceSystemCd(), company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);

        assertEquals("Number of Employer DD Debit Transactions", 1, erDebits.size());
        PayrollServices.commitUnitOfWork();

        //Assertion for Compensation & Deductions
        PayrollServices.beginUnitOfWork();
        Paycheck paycheck = Paycheck.findPaycheck(company, paycheckDTO.getPaycheckId());
        DomainEntitySet<Compensation> compensationCollection = paycheck.getCompensationCollection();
        assertEquals("Compensations ", 1, compensationCollection.size());

        DomainEntitySet<Deduction> deductionCollection = paycheck.getDeductionCollection();
        assertEquals("Deductions ", 1, deductionCollection.size());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPayrollSubmit_CloudTax() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        List<Employee> employees = DataLoadServices.addEEs(company, 1, false, true);

        List<CompanyPayrollItem> companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.create401kPayrollRun(employees, companyPayrollItems);
        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit 401k payroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRecallPayroll_With_COBRA_PSRV002382_1() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "196", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "-95", "6.5", "6.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        //Recall payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testRecallPayroll_With_COBRA_PSRV002382_2() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"196"}, new String[]{"-95"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        DomainEntitySet<FinancialTransaction> atoFTs = payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxOverpayment);
        assertEquals("Agency Tax over payments", 1, atoFTs.size());
        assertEquals("Agency Tax Over payment amount", new SpcfMoney("190"), atoFTs.get(0).getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRecallPayroll_With_COBRA_PSRV002382_3() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "196", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "-5", "6.5", "6.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        PayrollServices.rollbackUnitOfWork();

        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2010-11-02"), emps, new String[]{"196"}, new String[]{"-25"});

        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult2);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, PayrollStatus.Complete, payrollRun2);
        DomainEntitySet<FinancialTransaction> atdbFTsFirstPayroll = payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxDebit);
        assertEquals("Agency Tax over payments", 1, atdbFTsFirstPayroll.size());
        assertEquals("Agency Tax Over payment amount", new SpcfMoney("10"), atdbFTsFirstPayroll.get(0).getFinancialTransactionAmount());
        DomainEntitySet<FinancialTransaction> atdbFTsSecondPayroll = payrollRun2.getFinancialTransactions(TransactionTypeCode.AgencyTaxOverpayment);
        assertEquals("Agency Tax over payments", 1, atdbFTsSecondPayroll.size());
        assertEquals("Agency Tax Over payment amount", new SpcfMoney("50"), atdbFTsSecondPayroll.get(0).getFinancialTransactionAmount());
        assertEquals("First Payroll Run Status", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRegular_BackDated_Sequence_NoChangeIn_InitDate() throws Exception {
        String psid = "123456789";
        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        //Submit regular payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "6.5", "6.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        assertBackdatedPayrollPayments(company, payrollRun, initDate, initDate, new SpcfMoney("185"), new SpcfMoney("26.2"), null, null, true);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-03"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate, initDate, new SpcfMoney("368"), new SpcfMoney("50.4"), null, null, true);

        //Submit Regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-08"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate, initDate, new SpcfMoney("553"), new SpcfMoney("76.6"), null, null, true);

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-04"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate, initDate, new SpcfMoney("736"), new SpcfMoney("100.8"), null, null, true);

        DataLoadServices.enrollEFTPS(company);

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate, initDate, new SpcfMoney("736"), new SpcfMoney("100.8"), null, null, false);

    }

    @Test
    public void testRegular_BackDated_Sequence_adjustInitDate() throws Exception {
        String psid = "123456789";
        SpcfCalendar initDate941 = SpcfCalendar.createInstance(2010, 11, 4, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar backDatedInitDate941 = SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate940 = SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        //Submit regular payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "6.5", "6.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        assertBackdatedPayrollPayments(company, payrollRun, initDate941, initDate940, new SpcfMoney("185"), new SpcfMoney("26.2"), null, null, true);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-01"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate941, initDate940, backDatedInitDate941, new SpcfMoney("185"), new SpcfMoney("50.4"), new SpcfMoney("183"), null, true);

        //Submit Regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate941, initDate940, backDatedInitDate941, new SpcfMoney("370"), new SpcfMoney("76.6"), new SpcfMoney("183"), null, true);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 3, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        
        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Removing enrollment hold to check payments are not combined with different initiation dates.
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find();
        assertEquals("Number of 941 payments", 3, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("370.00"), SpcfCalendar.createInstance(2010, 11, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("183.00"), SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("183.00"), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DomainEntitySet<MoneyMovementTransaction>  payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("100.80"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

    }    

    @Test
    public void testBackDated_Regular_Sequence_adjustInitDate() throws Exception {
        String psid = "123456789";
        SpcfCalendar initDate941 = SpcfCalendar.createInstance(2010, 11, 4, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar backDatedInitDate941 = SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate940 = SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backDated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-31"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2010, 11, 5));
        PayrollServices.rollbackUnitOfWork();

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate941, initDate940, backDatedInitDate941, null, new SpcfMoney("24.2"), new SpcfMoney("183"), null, true);

        //Submit regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-01"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();
        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate941, initDate940, backDatedInitDate941, new SpcfMoney("185"), new SpcfMoney("50.4"), new SpcfMoney("183"), null, true);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-01"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find();
        assertEquals("Number of 941 payments", 3, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("185.00"), SpcfCalendar.createInstance(2010, 11, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("183.00"), SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("183.00"), SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DomainEntitySet<MoneyMovementTransaction>  payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("74.60"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-2"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "12", "5.5", "45", "25", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        //Removing enrollment hold to check payments are not combined with different initiation dates.
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find();
        assertEquals("Number of 941 payments", 3, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("370.00"), SpcfCalendar.createInstance(2010, 11, 4, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("183.00"), SpcfCalendar.createInstance(2010, 11, 8, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("183.00"), SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("100.80"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBackDated_adjustInitDate() throws Exception {
        String psid = "123456789";
        SpcfCalendar initDate941 = SpcfCalendar.createInstance(2011, 6, 14, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate940 = SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backDated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-6-7"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 6, 10));
        PayrollServices.rollbackUnitOfWork();

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate941, initDate940, null, new SpcfMoney("24.2"), new SpcfMoney("183"), null, true);

    }

    @Test
    public void testBackDated_adjustInitDate_To_5DaysFromNextValidInitDate() throws Exception {
        String psid = "123456789";
        SpcfCalendar initDate941 = SpcfCalendar.createInstance(2011, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate940 = SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backDated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-6-13"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 6, 17));
        PayrollServices.rollbackUnitOfWork();

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate941, initDate940, null, new SpcfMoney("24.2"), new SpcfMoney("183"), null, true);

    }

    @Test
    public void testBackDated_adjustInitDate_To_5DaysFromNextValidInitDate_NoChange() throws Exception {
        String psid = "123456789";
        SpcfCalendar initDate941 = SpcfCalendar.createInstance(2011, 10, 21, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate940 = SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backDated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-7-15"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 10, 31));
        PayrollServices.rollbackUnitOfWork();

        assertBackdatedPayrollPayments(company, processResult.getResult(), initDate941, initDate940, null, new SpcfMoney("24.2"), new SpcfMoney("183"), null, true);

    }

    @Test
    public void testRegular_Backdated_100K_NoChangeIn_InitDate() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 1, 1));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 30, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit regular payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-1"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"20000", "15000", "6500", "4500", "2500", "6.5", "6.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DomainEntitySet<MoneyMovementTransaction> payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find();
        assertEquals("Number of 941 payments", 1, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        DomainEntitySet<MoneyMovementTransaction> payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("26.2"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-28"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 0, payments941.size());        
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("50.4"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit Regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-08"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1200", "550", "450", "250", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 1, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5900.00"), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 12, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 6, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("76.6"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-05"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 2, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5900.00"), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 12, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 6, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 11, 16, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 3, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("100.8"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-04"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Removing enrollment hold to check payments are not combined with different initiation dates.
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 2, payments941.size());

        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5900.00"), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 12, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 6, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("11400.00"), SpcfCalendar.createInstance(2010, 11, 16, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 3, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 5, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("125.0"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();
        
    }

    @Test
    public void testBackdated_NewInitiationDate_in_Past() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.enrollEFTPS(company);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backdated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-18"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DomainEntitySet<MoneyMovementTransaction> payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find();
        assertEquals("Number of 941 payments", 1, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        DomainEntitySet<MoneyMovementTransaction> payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("24.2"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-28"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"20000", "15000", "6500", "4500", "2500", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 0, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 19, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 18, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 18, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("50.4"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();        
    }

    @Test
    public void testBackdated_Regular_100K_NoChangeIn_InitDate() throws Exception {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.enrollEFTPS(company);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Submit backdated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-20"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));
        DomainEntitySet<MoneyMovementTransaction> payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().find();
        assertEquals("Number of 941 payments", 1, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        DomainEntitySet<MoneyMovementTransaction> payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("24.2"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-29"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"20000", "15000", "6500", "4500", "2500", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 0, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 21, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("50.4"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit Regular payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-08"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1200", "550", "450", "250", "6.5", "6.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 1, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 21, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5900.00"), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 12, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 6, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("76.6"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Move from 10/25 to 10/28
        for (SpcfCalendar initdate = PSPDate.getPSPTime().copy(); initdate.compareTo(SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone())) < 0; CalendarUtils.addBusinessDays(initdate, 1)) {
            DataLoadServices.runOffload(initdate);
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
        }        

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        
        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-27"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"50", "100", "55", "45", "25", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 1, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 21, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5900.00"), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 12, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 6, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("550.00"), SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 27, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 27, SpcfTimeZone.getLocalTimeZone()),1);
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("100.8"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

        //Submit backdated payroll
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-27"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set941().setNonDirectEFTPS().find();
        assertEquals("Number of 941 payments", 1, payments941.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("97000.00"), SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 29, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 21, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 20, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("5900.00"), SpcfCalendar.createInstance(2010, 11, 10, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 11, 12, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 11, 6, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("550.00"), SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 27, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 27, SpcfTimeZone.getLocalTimeZone()),1);
        DataLoadServices.assertMmt(PaymentMethod.EFTPSDirectDebit, new SpcfMoney("5700.00"), SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2010, 10, 28, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 27, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 10, 27, SpcfTimeZone.getLocalTimeZone()),1);        
        payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(company).set940().find();
        assertEquals("Number of 940 payments", 1, payments940.size());
        DataLoadServices.assertMmt(PaymentMethod.EFTPS, new SpcfMoney("125.0"), SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 31, SpcfTimeZone.getLocalTimeZone()),
                SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()),SpcfCalendar.createInstance(2010, 12, 31, SpcfTimeZone.getLocalTimeZone()),1);
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testNegativeTaxPayment_PSRV002464() throws Exception {
        String psid = "123456789";
        SpcfCalendar initDate941 = SpcfCalendar.createInstance(2010, 11, 4, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar backDatedInitDate941 = SpcfCalendar.createInstance(2010, 11, 9, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar initDate940 = SpcfCalendar.createInstance(2011, 1, 28, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone()));

        //Submit backdated payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-01"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"5", "11", "5.5", "45", "25", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollRun payrollRun1 = processResult.getResult();

        assertBackdatedPayrollPayments(company, payrollRun1, initDate941, initDate940, backDatedInitDate941, null, new SpcfMoney("24.2"), new SpcfMoney("183"), null, true);

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2010, 11, 2, SpcfTimeZone.getLocalTimeZone())) ;

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2010, 11, 3, SpcfTimeZone.getLocalTimeZone()));
        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-01"), emps, new String[]{"196"}, new String[]{"-30"});

        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //IRS941 payment amount got reduced by negative adjustment amount - 60
        assertBackdatedPayrollPayments(company, payrollRun1, initDate941, initDate940, backDatedInitDate941, null, new SpcfMoney("24.2"), new SpcfMoney("123"), null, true);

        PayrollServices.beginUnitOfWork();
        assertEquals("FTs on adjustment payroll", 1, processResult.getResult().getFinancialTransactionCollection().size());
        DomainEntitySet<FinancialTransaction> agencyTaxDbts = processResult.getResult().getFinancialTransactions(TransactionState.findTransactionState(TransactionStateCode.Created),
                                                            TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxDebit));
        assertEquals("Agency tax debit on adjustment payroll", 1, agencyTaxDbts.size());
        assertEquals("Agency tax dbt amount", new SpcfMoney("60.00"), agencyTaxDbts.get(0).getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testForDueDatePolicyIssue_PSRV002677() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-UIETT-PAYMENT", null);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 11, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-09-30"), emps, new String[]{"1","61","62", "63","64","65","66","6","67","87","142"}, new String[]{"1000","6100","6200", "6300","6400","650","660","6","67","87","142"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollSubmitForMS() throws Exception {
        String psid = "123456789";

        SpcfCalendar payrollRunDate = SpcfCalendar.createInstance(2011, 10, 13, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar supportStartDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(supportStartDate);
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "65", "143", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());


        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(payrollRunDate);
        PayrollServices.commitUnitOfWork();

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("143", "14.3");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-10-14"), emps, lawAmounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTaxAndDD_NoBankAccountVerification() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addCompanyBankAccount(company, true);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService ddService = company.getService(ServiceCode.DirectDeposit);
        assertEquals("DD service not in pending bank verification", ServiceSubStatusCode.PendingBankVerification, ddService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 10, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid,
                                                                                               DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-10-14"), emps, lawAmounts));
        PayrollServices.commitUnitOfWork();

        assertFalse("Payroll accepted with bank account in pending verification", processResult.isSuccess());
        assertEquals("Message code", "186", processResult.getMessages().get(0).getMessageCode());
    }

    private CompanyBankAccountDTO getTestCompanyBankAccount() {

        // Create Bank Account
        BankAccountDTO bankAccount = new BankAccountDTO();
        bankAccount.setAccountNumber("123099");
        bankAccount.setAccountType(BankAccountType.Checking);
        bankAccount.setBankName("Bank of America");
        bankAccount.setRoutingNumber("263182914");

        // Create Company Bank Account
        CompanyBankAccountDTO companyBankAccount = new CompanyBankAccountDTO();
        companyBankAccount.setBankAccountDTO(bankAccount);
        companyBankAccount.setCompanyBankAccountID("123121");
        companyBankAccount.setSourceBankAccountName("BOFA");

        return companyBankAccount;
    }

    private ServiceBankAccountDTO createServiceBankAccountDTO(CompanyBankAccountDTO pCompanyBankAccountDTO, ServiceCode pServiceCode) {
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(pCompanyBankAccountDTO);
        serviceBankAccountDTO.setServiceCode(pServiceCode);
        return serviceBankAccountDTO;
    }

    private void assertBackdatedPayrollPayments(Company pCompany, PayrollRun pPayrollRun, SpcfCalendar pInitDate941, SpcfCalendar pInitDate940, SpcfMoney regular941PaymentAmount, SpcfMoney regular940PaymentAmount, SpcfMoney backdated941PaymentAmount,
                                                SpcfMoney backdated940PaymentAmount, boolean isOnEnrollmentHold){
        SpcfCalendar backdatedPaymentInitDate941 = pInitDate941.copy();
        CalendarUtils.addBusinessDays(backdatedPaymentInitDate941, backDatedPayrollPaymentOffset);
        assertBackdatedPayrollPayments(pCompany, pPayrollRun, pInitDate941, pInitDate940, backdatedPaymentInitDate941, regular941PaymentAmount, regular940PaymentAmount, backdated941PaymentAmount, backdated940PaymentAmount, isOnEnrollmentHold);
    }

    private void assertBackdatedPayrollPayments(Company pCompany, PayrollRun pPayrollRun, SpcfCalendar pInitDate941, SpcfCalendar pInitDate940,SpcfCalendar pBackDatedInitDate941, SpcfMoney regular941PaymentAmount, SpcfMoney regular940PaymentAmount, SpcfMoney backdated941PaymentAmount,
                                                SpcfMoney backdated940PaymentAmount, boolean isOnEnrollmentHold){
        SpcfCalendar backdatedPaymentInitDate940 = pInitDate940.copy();
        CalendarUtils.addBusinessDays(backdatedPaymentInitDate940, backDatedPayrollPaymentOffset);
        int payment941Counts = 0;
        int payment940Counts = 0;
        if(regular941PaymentAmount != null){
            payment941Counts++;
        }
        if(regular940PaymentAmount != null){
            payment940Counts++;
        }
        if(backdated941PaymentAmount != null){
            payment941Counts++;
        }
        if(backdated940PaymentAmount != null){
            payment940Counts++;
        }
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> payments941 = MoneyMovementTransaction.findTaxPayments().setCompany(pCompany).setPaycheckDate(pPayrollRun.getPaycheckDate()).set941().find();
        assertEquals("IRS 941 payments", payment941Counts, payments941.size());
        DomainEntitySet<MoneyMovementTransaction> payments940 = MoneyMovementTransaction.findTaxPayments().setCompany(pCompany).setPaycheckDate(pPayrollRun.getPaycheckDate()).set940().find();
        assertEquals("IRS 940 payments", payment940Counts, payments940.size());

        if(regular941PaymentAmount != null){
            DomainEntitySet<MoneyMovementTransaction> regularPayroll941Payments = payments941.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(regular941PaymentAmount));
            assertEquals("IRS 941 payment Amount", 1, regularPayroll941Payments.size());
            assertEquals("IRS 941 payment Amount", pInitDate941, regularPayroll941Payments.get(0).getInitiationDate().toLocal());
        }
        if(backdated941PaymentAmount != null){
            DomainEntitySet<MoneyMovementTransaction> backdatedPayroll941Payments = payments941.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(backdated941PaymentAmount));
            assertEquals("IRS 941 payment Amount for backdated payrolls", 1, backdatedPayroll941Payments.size());
            assertEquals("IRS 941 payment Amount for backdated payrolls", pBackDatedInitDate941, backdatedPayroll941Payments.get(0).getInitiationDate().toLocal());
        }
        if(regular940PaymentAmount != null){
            DomainEntitySet<MoneyMovementTransaction> regularPayroll940Payments = payments940.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(regular940PaymentAmount));
            assertEquals("IRS 940 payment Amount", 1, regularPayroll940Payments.size());
            assertEquals("IRS 940 payment Amount", pInitDate940, regularPayroll940Payments.get(0).getInitiationDate().toLocal());
        }
        if(backdated940PaymentAmount != null){
            DomainEntitySet<MoneyMovementTransaction> backdatedPayroll940Payments = payments940.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(backdated940PaymentAmount));
            assertEquals("IRS 941 payment Amount for backdated payrolls", 1, backdatedPayroll940Payments.size());
            assertEquals("IRS 941 payment Amount for backdated payrolls", backdatedPaymentInitDate940, backdatedPayroll940Payments.get(0).getInitiationDate().toLocal());
        }
        if(isOnEnrollmentHold){
            assertEquals("All EFTPS payments are on enrollment hold", payment940Counts+payment941Counts, MoneyMovementTransaction.findTaxPayments().setCompany(pCompany).setOnHold().setNonDirect().find().size());
        } else {
            assertEquals("All EFTPS payments are not on hold", payment940Counts+payment941Counts, DataLoadServices.getReadyToSendNonDirectPayments(pCompany).size());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollSubmitStepPricingCOSTCO49() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2011, 10, 4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED, SpcfCalendar.createInstance(2012,1,1));

        DataLoadServices.updateOffering(company, OfferingCode.COSTCO49, "COSTCO-49");

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 20, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 10, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-10-14"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().equalTo(
                                                                                 OfferingServiceChargeType.EmployeesPaid)).sort(BillingDetail.Quantity());
        assertEquals("EmployeesPaid Count", 2, billingDetails.size());

        assertEquals("Quantity Count", 5, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 5 employee(s) paid", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("8.83"), billingDetails.get(0).getItemTotal());

        assertEquals("Quantity Count", 15, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("0"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "No fee for 15 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("0"), billingDetails.get(1).getItemTotal());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollSubmitStepPricingCOSTCO67FY15() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2015, 3, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED, SpcfCalendar.createInstance(2015,3,1));

        DataLoadServices.updateOffering(company, OfferingCode.COSTCO67FY15, "COSTCO-67FY15");

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 20, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2015,3, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-3-14"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().in(
                                                                                 OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee)).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.45"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.45 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("29.08"), billingDetails.get(0).getItemTotal());

         //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("109.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Memo", "Monthly processing fee for Mar 2015", billingDetails.get(2).getMemo());
        assertEquals("Item Total", new SpcfMoney("109.08"), billingDetails.get(2).getItemTotal());


        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testPayrollSubmitStepPricingSYM3FY14() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2011, 10, 4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED_SYMPHONY_USAGE, SpcfCalendar.createInstance(2012,1,1));

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 15, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 10, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-10-14"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(15);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().equalTo(
                                                                                 OfferingServiceChargeType.EmployeesPaid)).sort(BillingDetail.Quantity());
        assertEquals("EmployeesPaid Count", 2, billingDetails.size());

        assertEquals("Quantity Count", 5, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("2"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 5 employee(s) paid", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("10.08"), billingDetails.get(0).getItemTotal());

        assertEquals("Quantity Count", 10, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("5"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 10 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("50.08"), billingDetails.get(1).getItemTotal());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollSubmitStepPricingSYM3FY14Update() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2011, 10, 4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED_SYMPHONY_USAGE, SpcfCalendar.createInstance(2012,1,1));

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 15, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 10, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-10-14"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(15);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        BillingDetail.updateBillingDetail(payrollRun, payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit),
                                          OfferingServiceChargeType.EmployeesPaid, 14, company.getOffering(ServiceCode.DirectDeposit).getOffering().getOfferingCode());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().equalTo(
                                                                                 OfferingServiceChargeType.EmployeesPaid)).sort(BillingDetail.Quantity());
        assertEquals("EmployeesPaid Count", 2, billingDetails.size());

        assertEquals("Quantity Count", 4, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("2"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 4 employee(s) paid", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("8.08"), billingDetails.get(0).getItemTotal());

        assertEquals("Quantity Count", 10, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("5"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 10 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("50.08"), billingDetails.get(1).getItemTotal());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollSubmitStepPricingSYM3FY14Update2() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2011, 10, 4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED_SYMPHONY_USAGE, SpcfCalendar.createInstance(2012,1,1));

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 15, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 10, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-10-14"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(15);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        BillingDetail.updateBillingDetail(payrollRun, payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit),
                                          OfferingServiceChargeType.EmployeesPaid, 9, company.getOffering(ServiceCode.DirectDeposit).getOffering().getOfferingCode());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().equalTo(
                                                                                 OfferingServiceChargeType.EmployeesPaid)).sort(BillingDetail.Quantity());
        assertEquals("EmployeesPaid Count", 2, billingDetails.size());

        assertEquals("Quantity Count", 0, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("2"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "No fee for 0 employee(s) paid", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("0"), billingDetails.get(0).getItemTotal());

        assertEquals("Quantity Count", 9, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("5"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 9 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("45.08"), billingDetails.get(1).getItemTotal());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollSubmitStepPricingSYM3FY14Update3() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2011, 10, 4);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED_SYMPHONY_USAGE, SpcfCalendar.createInstance(2012,1,1));

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 9, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2011, 10, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2011-10-14"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(9);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        BillingDetail.updateBillingDetail(payrollRun, payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit),
                                          OfferingServiceChargeType.EmployeesPaid, 15, company.getOffering(ServiceCode.DirectDeposit).getOffering().getOfferingCode());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().equalTo(
                                                                                 OfferingServiceChargeType.EmployeesPaid)).sort(BillingDetail.Quantity());
        assertEquals("EmployeesPaid Count", 2, billingDetails.size());

        assertEquals("Quantity Count", 5, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("2"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 5 employee(s) paid", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("10.08"), billingDetails.get(0).getItemTotal());

        assertEquals("Quantity Count", 10, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("5"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 10 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("50.08"), billingDetails.get(1).getItemTotal());

        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testPayrollSubmitStepPricingAP79FY16() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2015, 3, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED, SpcfCalendar.createInstance(2015,3,1));

        DataLoadServices.updateOffering(company, OfferingCode.AP79FY16, "AP79FY16");

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 20, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2015,3, 5);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-3-14"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().in(
                                                                                 OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee)).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());

        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("79.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Memo", "Monthly processing fee for Mar 2015", billingDetails.get(2).getMemo());
        assertEquals("Item Total", new SpcfMoney("79.08"), billingDetails.get(2).getItemTotal());


        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testPayrollSubmitStepPricingDIYDDFY16() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2015, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.Tax);
        //DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addEntitlementUnit(company, "1", "1", null, null, DataLoadServices.AssetItemNumber.ASSISTED, SpcfCalendar.createInstance(2015,3,1));

        DataLoadServices.updateOffering(company, OfferingCode.AP79FY16, "AP79FY16");

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 20, true, true);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(2015,2, 1);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        CompanyBankAccountDTO companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-2-2"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().in(
                                                                                 OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee)).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());

        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("79.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Item Total", new SpcfMoney("79.08"), billingDetails.get(2).getItemTotal());
        PayrollServices.rollbackUnitOfWork();
        //FY16
        DataLoadServices.setPSPDate(2015, 3, 1);
        DataLoadServices.updateOffering(company, OfferingCode.AP89FY16, "AP89FY16");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-3-3"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = PSPDate.getPSPTime().copy();
        createdDate.addHours(-1);
        billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().in(
                                                                                 OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee).And(BillingDetail.CreatedDate().greaterOrEqualThan(createdDate))).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());

        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("89.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Item Total", new SpcfMoney("89.08"), billingDetails.get(2).getItemTotal());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2015, 4, 2);
        DataLoadServices.updateOffering(company, OfferingCode.AP99FY16, "AP99FY16");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-4-1"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
         createdDate = PSPDate.getPSPTime().copy();
        createdDate.addHours(-1);
        billingDetails = Application.find(BillingDetail.class,
                                          BillingDetail.OfferingServiceChargeType().in(
                                                  OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee).And(BillingDetail.CreatedDate().greaterOrEqualThan(createdDate))).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());

        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("99.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Item Total", new SpcfMoney("99.08"), billingDetails.get(2).getItemTotal());
        PayrollServices.rollbackUnitOfWork();

        //AP79MEFY16
        DataLoadServices.setPSPDate(2015, 5, 1);
        DataLoadServices.updateOffering(company, OfferingCode.AP79MEFY16, "AP79MEFY16");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-5-2"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        createdDate = PSPDate.getPSPTime().copy();
        createdDate.addHours(-1);
        billingDetails = Application.find(BillingDetail.class,
                                          BillingDetail.OfferingServiceChargeType().in(
                                                  OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee).And(BillingDetail.CreatedDate().greaterOrEqualThan(createdDate))).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());
        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("79.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Item Total", new SpcfMoney("79.08"), billingDetails.get(2).getItemTotal());
        PayrollServices.rollbackUnitOfWork();

        //AP89MEFY16
        DataLoadServices.setPSPDate(2015, 6, 1);
        DataLoadServices.updateOffering(company, OfferingCode.AP89MEFY16, "AP89MEFY16");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-6-2"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        createdDate = PSPDate.getPSPTime().copy();
        createdDate.addHours(-1);
        billingDetails = Application.find(BillingDetail.class,
                                          BillingDetail.OfferingServiceChargeType().in(
                                                  OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee).And(BillingDetail.CreatedDate().greaterOrEqualThan(createdDate))).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());
        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("89.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Item Total", new SpcfMoney("89.08"), billingDetails.get(2).getItemTotal());
        PayrollServices.rollbackUnitOfWork();

        //AP99MEFY16
        DataLoadServices.setPSPDate(2015, 8, 1);
        DataLoadServices.updateOffering(company, OfferingCode.AP99MEFY16, "AP99MEFY16");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-8-2"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        createdDate = PSPDate.getPSPTime().copy();
        createdDate.addHours(-1);
        billingDetails = Application.find(BillingDetail.class,
                                          BillingDetail.OfferingServiceChargeType().in(
                                                  OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee).And(BillingDetail.CreatedDate().greaterOrEqualThan(createdDate))).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());
        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("99.00"), billingDetails.get(2).getUnitPrice());
        assertEquals("Item Total", new SpcfMoney("99.08"), billingDetails.get(2).getItemTotal());
        PayrollServices.rollbackUnitOfWork();
        //PAP84FY16
        DataLoadServices.setPSPDate(2015, 9, 1);
        DataLoadServices.updateOffering(company, OfferingCode.PAP84FY16, "PAP84FY16");
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        companyBankAccountDTO = PayrollServices.dtoFactory.create(companyBankAccount);
        companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        for (CompanyService companyService : company.getCompanyServiceCollection().find(CompanyService.Service().ServiceCd().in(ServiceCode.Tax, ServiceCode.DirectDeposit))) {
            companyBankAccounts.add(createServiceBankAccountDTO(companyBankAccountDTO, companyService.getService().getServiceCd()));
        }
        payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO("2015-9-2"), emps, lawAmounts);
        payrollRunDTO.setEmployeesPaidInTransmission(20);
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        createdDate = PSPDate.getPSPTime().copy();
        createdDate.addHours(-1);
        billingDetails = Application.find(BillingDetail.class,
                                          BillingDetail.OfferingServiceChargeType().in(
                                                  OfferingServiceChargeType.EmployeesPaid,OfferingServiceChargeType.DirectDepositFee,OfferingServiceChargeType.MonthlyFee).And(BillingDetail.CreatedDate().greaterOrEqualThan(createdDate))).sort(BillingDetail.OfferingServiceChargeType());
        assertEquals("EmployeesPaid Count", 3, billingDetails.size());
        //DD fee
        assertEquals("Quantity Count", 20, billingDetails.get(0).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        assertEquals("Memo", "Fee for 20 direct deposit(s) at $1.75 each", billingDetails.get(0).getMemo());
        assertEquals("Item Total", new SpcfMoney("35.08"), billingDetails.get(0).getItemTotal());
        //Employee paid
        assertEquals("Quantity Count", 20, billingDetails.get(1).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("1.50"), billingDetails.get(1).getUnitPrice());
        assertEquals("Memo", "Fee for 20 employee(s) paid", billingDetails.get(1).getMemo());
        assertEquals("Item Total", new SpcfMoney("30.08"), billingDetails.get(1).getItemTotal());
        //Monthly fee
        assertEquals("Quantity Count", 1, billingDetails.get(2).getQuantity());
        assertEquals("Unit Price", new SpcfMoney("84.15"), billingDetails.get(2).getBasePrice());
        assertEquals("Item Total", new SpcfMoney("84.23"), billingDetails.get(2).getItemTotal());
        PayrollServices.rollbackUnitOfWork();
    }
    
	@Test
	public void testLiabilityAdjustmentOnlyPayrollForPreviousQuarter() {
		PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

		SpcfCalendar payCheckDate = SpcfCalendar.createInstance(2016, 2, 28, SpcfTimeZone.getLocalTimeZone());

		PayrollServices.beginUnitOfWork();
		PSPDate.setPSPTime(payCheckDate);
		PayrollServices.commitUnitOfWork();

		Company company = psdl.createAssistedCompany("123272727");

		PayrollServices.beginUnitOfWork();
		company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
		PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit(company, payCheckDate);
		ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
				"123272727", payrollRunDTO);

		assertTrue(processResult.isSuccess());

		PayrollRun payrollRun = processResult.getResult();

		// Add entry to calculate the Quarterly Employee Totals(QET)
		EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
		EmpTotalsPayrollRun empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(company,
				CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate()), EmpTotalsPayrollStatus.Pending);
		assertNotNull("Employee totals payroll run not present", empTotalsPayrollRun);
		empTotalsPayrollRun.updateEmpTotalsPayrollRunStatus(EmpTotalsPayrollStatus.Processed);

		PayrollServices.commitUnitOfWork();

		// Run liability adjustment only for previous quarter
		PayrollServices.beginUnitOfWork();
		SpcfCalendar liabilityPayCheckDate = SpcfCalendar.createInstance(2016, 7, 30, SpcfTimeZone.getLocalTimeZone());
		PSPDate.setPSPTime(liabilityPayCheckDate);
		payrollRunDTO = psdl.loadDataForLiabilityAdjustmentOnlyPayroll(company, liabilityPayCheckDate, payCheckDate);
		processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
		assertTrue(processResult.isSuccess());
		payrollRun = processResult.getResult();

		PayrollServices.commitUnitOfWork();

		// Verify pending EmpTotalsPayrollRun to ensure re-calculation of
		// Quarterly Employee Totals(QET)
		PayrollServices.beginUnitOfWork();
		empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(company,
				CalendarUtils.getFirstDayOfQuarter(payCheckDate), EmpTotalsPayrollStatus.Pending);
		assertNotNull("Employee totals recalc not forced", empTotalsPayrollRun);
		PayrollServices.commitUnitOfWork();
	}

	@Test
	public void testPaycheckAndLiabilityAdjustmentPayrollForPreviousQuarter() {
		PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

		SpcfCalendar payCheckDate = SpcfCalendar.createInstance(2016, 2, 28, SpcfTimeZone.getLocalTimeZone());

		PayrollServices.beginUnitOfWork();
		PSPDate.setPSPTime(payCheckDate);
		PayrollServices.commitUnitOfWork();

		Company company = psdl.createAssistedCompany("123272727");

		PayrollServices.beginUnitOfWork();
		company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
		PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit(company, payCheckDate);
		ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
				"123272727", payrollRunDTO);

		assertTrue(processResult.isSuccess());

		PayrollRun payrollRun = processResult.getResult();

		// Add entry to calculate the Quarterly Employee Totals(QET)
		EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
		EmpTotalsPayrollRun empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(company,
				CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate()), EmpTotalsPayrollStatus.Pending);
		assertNotNull("Employee totals payroll run not present", empTotalsPayrollRun);
		empTotalsPayrollRun.updateEmpTotalsPayrollRunStatus(EmpTotalsPayrollStatus.Processed);

		PayrollServices.commitUnitOfWork();

		// Run paycheck for current quarter and liability adjustment for
		// previous quarter
		PayrollServices.beginUnitOfWork();
		SpcfCalendar liabilityPayCheckDate = SpcfCalendar.createInstance(2016, 7, 30, SpcfTimeZone.getLocalTimeZone());
		PSPDate.setPSPTime(liabilityPayCheckDate);
		payrollRunDTO = psdl.loadDataForPaycheckAndLiabilityAdjustmentPayroll(company, liabilityPayCheckDate,
				payCheckDate);
		processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
		assertTrue(processResult.isSuccess());
		payrollRun = processResult.getResult();

		PayrollServices.commitUnitOfWork();

		// Verify pending EmpTotalsPayrollRun to ensure re-calculation of
		// Quarterly Employee Totals(QET)
		PayrollServices.beginUnitOfWork();
		empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(company,
				CalendarUtils.getFirstDayOfQuarter(payCheckDate), EmpTotalsPayrollStatus.Pending);
		assertNotNull("Employee totals recalc not forced", empTotalsPayrollRun);
		PayrollServices.commitUnitOfWork();
	}

	@Test
	public void testPaycheckAndLiabilityAdjustmentPayrollForCurrentQuarter() {
		PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

		SpcfCalendar payCheckDate = SpcfCalendar.createInstance(2016, 7, 28, SpcfTimeZone.getLocalTimeZone());

		PayrollServices.beginUnitOfWork();
		PSPDate.setPSPTime(payCheckDate);
		PayrollServices.commitUnitOfWork();

		Company company = psdl.createAssistedCompany("123272727");

		PayrollServices.beginUnitOfWork();
		company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
		PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit(company, payCheckDate);
		ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT,
				"123272727", payrollRunDTO);

		assertTrue(processResult.isSuccess());

		PayrollRun payrollRun = processResult.getResult();

		// Add entry to calculate the Quarterly Employee Totals(QET)
		EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
		EmpTotalsPayrollRun empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(company,
				CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate()), EmpTotalsPayrollStatus.Pending);

		PayrollServices.commitUnitOfWork();

		// Run paycheck for current quarter and liability adjustment for current
		// quarter
		PayrollServices.beginUnitOfWork();
		SpcfCalendar liabilityPayCheckDate = SpcfCalendar.createInstance(2016, 7, 30, SpcfTimeZone.getLocalTimeZone());
		PSPDate.setPSPTime(liabilityPayCheckDate);
		payrollRunDTO = psdl.loadDataForPaycheckAndLiabilityAdjustmentPayroll(company, liabilityPayCheckDate,
				payCheckDate);
		processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
		assertTrue(processResult.isSuccess());
		payrollRun = processResult.getResult();

		PayrollServices.commitUnitOfWork();

		// Verify pending EmpTotalsPayrollRun to ensure re-calculation of
		// Quarterly Employee Totals(QET)
		PayrollServices.beginUnitOfWork();
		empTotalsPayrollRun = EmpTotalsPayrollRun.findLatestEmpTotalsPayrollRun(company,
				CalendarUtils.getFirstDayOfQuarter(payCheckDate), EmpTotalsPayrollStatus.Pending);
		assertNotNull("Employee totals recalc not forced", empTotalsPayrollRun);
		PayrollServices.commitUnitOfWork();
	}
}
