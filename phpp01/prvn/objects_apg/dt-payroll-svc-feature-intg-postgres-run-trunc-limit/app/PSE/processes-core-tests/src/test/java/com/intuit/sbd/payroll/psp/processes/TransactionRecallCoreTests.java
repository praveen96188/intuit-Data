package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo.MessageLevel;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

public class TransactionRecallCoreTests {


    private static final String COMPANY1 = "123272727";
    //private static final String SOURCE_PAYROLL_ID1 = "PAYROLLP1";
    private static final String SOURCE_PAYROLL_ID1 = "BatchId01";
    private static final String REQUEST_ID1 = "1";
    private static final Long TOKEN1 = 0L;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, SpcfCalendar.September, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    public void loadData() {
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        PayrollServices.beginUnitOfWork();
        Collection<PayrollRunDTO> payrollRunDTOCollection = psdl.loadMultiplePayrollsWithMultiplePaycheckSplitsForCompany123272727(2);
        PayrollServices.commitUnitOfWork();

        for (PayrollRunDTO dto : payrollRunDTOCollection) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                    .submitPayroll(SourceSystemCode.QBDT, "123272727", dto);

            //Statement to save the billing details by calling BillingManager.save() method for the bills,
            // which was added by PayrollSubmitDD process by calling BillingManager.add() method.

            CompanyBankAccount companyBankAccount = processResult.getResult()
                    .getCompanyBankAccountForService(ServiceCode.DirectDeposit);

            PayrollRun payrollRun = processResult.getResult();

            PayrollServices.commitUnitOfWork();

            if (!processResult.isSuccess()) {
                for (Message msg : processResult.getMessages()) {
                    System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
                }
            }

            PayrollServices.beginUnitOfWork();
            //Assertion for the BillingDetails list size
            assertEquals("BillingDetails Size ", 1, payrollRun.getBillingDetailCollection().size());

            Offering offering = Offering.findOffering(processResult.getResult().getCompany(), ServiceCode.DirectDeposit);
            OfferingServiceChargeGroup offSvcChargeGrp = OfferingServiceChargeGroup.findOfferingServiceChargeGroup(offering, OfferingServiceChargeType.DirectDepositFee);
            OfferingServiceCharge offServiceCharge = offSvcChargeGrp.selectTier(
                    processResult.getResult().getPaycheckCollection().size());

            for (BillingDetail billingDetail : payrollRun.getBillingDetailCollection()) {
                //Assertion for the Offering Service Charge Type
                assertEquals("Offering Service Charge Type ", OfferingServiceChargeType.DirectDepositFee,
                        billingDetail.getOfferingServiceChargeType());

                //Assertion for the Item SKU.
                assertEquals("Item SKU ", offServiceCharge.getSKU(), billingDetail.getItemSku());
            }
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO recallDTO = getBaseDTO();
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(null, COMPANY1, recallDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages Returned:", 1, results.getMessages().size());

        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "137", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Message Text:", "Source System Code is not specified.", message.getMessage());
    }

    @Test
    public void testNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO recallDTO = getBaseDTO();
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, null, recallDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals("Error Count:", 1, results.getMessages().size());
        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "138", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Message Text:", "Source Company ID is not specified.", message.getMessage());
    }

    @Test
    public void testCompanyNotExists() {
        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO recallDTO = getBaseDTO();
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, "IDONTEXIST", recallDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals("Error Count:", 1, results.getMessages().size());
        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "169", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Message Text:", "Company QBDT:IDONTEXIST does not exist.", message
                .getMessage());
    }

    @Test
    public void testTransactionRecallPayrollNotExist() {
        //Load payroll data
        loadData();

        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO recallDTO = getBaseDTO();
        recallDTO.setSourcePayrollRunId("IDONTEXIST");
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);
        PayrollServices.commitUnitOfWork();

        // Verify Error Message
        Message message = results.getMessages().get(0);
        assertEquals("Error Count:", 1, results.getMessages().size());
        assertEquals("Message Code:", "194", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "Payroll Run with DDTxBatchID IDONTEXIST does not exist for company QBDT:123272727.",
                message.getMessage());
    }

    @Test
    public void testTransactionRecallCompanyServiceCancelled() {
        //Load payroll data
        loadData();

        // Cancel two payrolls
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();

        dto.setSourcePayrollRunId("BatchId01");
        dto.setSourcePaycheckIdList(null);

        //Set Current Principal as agent
        PspPrincipal principal = Application.getCurrentPrincipal();
        PayrollServices.beginUnitOfWork();
        DataLoader.setPrincipalIsAgent();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult cancelResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, "123272727", dto);
        PayrollServices.commitUnitOfWork();
        assertSuccess(cancelResult);

        PayrollServices.beginUnitOfWork();
        dto.setSourcePayrollRunId("BatchId02");
        cancelResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, "123272727", dto);
        // Remove Agent from Principal
        PayrollServices.userManager.deleteUser("UnitTestAgent");
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();
        assertSuccess(cancelResult);

        // Cancell company service
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<ServiceSubStatus> subStatusList = new DomainEntitySet<ServiceSubStatus>();
        ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, ServiceSubStatusCode.Cancelled);
        subStatusList.add(serviceSubStatus);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateSubStatuses(SourceSystemCode.QBDT,
                "123272727",
                ServiceCode.DirectDeposit, subStatusList);
        PayrollServices.commitUnitOfWork();
        assertSuccess(result);

        DataLoadServices.setPrincipalToAgent();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar sysDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 28, 17, 1, 0, 0,
                SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(sysDate);
        TransactionCancelEEDTO recallDTO = getBaseDTO();
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals("Error Count:", 1, results.getMessages().size());
        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "1101", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "The operation RecallPayroll is not allowed for company QBDT:123272727 in its current state.",
                message.getMessage());
    }

    @Test
    public void testTransactionRecallWholePayrollAfterCutoff() {
        //Load payroll data
        loadData();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar sysDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 28, 17, 11, 0, 0,
                SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(sysDate);
        TransactionCancelEEDTO recallDTO = getBaseDTO();
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals("Error Count:", 1, results.getMessages().size());
        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "1015", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "Transactions for DDTxBatch " + SOURCE_PAYROLL_ID1 + " for company QBDT:123272727 have already been sent to the bank and cannot be recalled.",
                message.getMessage());
    }

    @Test
    public void testTransactionRecallWholePayrollBeforeCutoff() {

        ProcessResult<TransactionResponse> results = recallWholePayrollBeforeCutoff();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        // Double check transactions
        PayrollRun payrollRun = PayrollRun
                .findPayrollRun(company, SOURCE_PAYROLL_ID1);
        for (FinancialTransaction transaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals("Transaction Status", TransactionStateCode.Cancelled, transaction
                    .getCurrentTransactionState().getTransactionStateCd());
        }
        // Double check transactions are all canceled
        for (FinancialTransaction transaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals("Transaction Status", TransactionStateCode.Cancelled, transaction
                    .getCurrentTransactionState().getTransactionStateCd());
        }

        // Transaction Responses
        TransactionResponse response = results.getResult();
        assertNotNull("Response Exists", response);

        //Assertion for DdServiceFee transaction was Cancelled
        DomainEntitySet<FinancialTransaction> feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        assertEquals("Number of DdServiceFee Cancelled txns", 1, feeTxnList.size());

        //Assertions for Billing Details.
        checkBillingDetails(0);
        PayrollServices.commitUnitOfWork();
    }

    private ProcessResult<TransactionResponse> recallWholePayrollBeforeCutoff() {
        //Load payroll data
        loadData();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);

        SpcfCalendar sysDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 25, 16, 55, 0, 0,
                SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar cutoffTime = company.getOffloadGroup().getCalendarForCutoffTime(sysDate);
        sysDate = cutoffTime.copy();
        sysDate.addMinutes(-1);
        PSPDate.setPSPTime(sysDate);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO recallDTO = getBaseDTO();
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);

        saveBillingDetails();
        PayrollServices.commitUnitOfWork();
        assertSuccess("Recall whole payroll:", results);
        // Verify Event Creation
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> recallPayrollEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Payroll Recall Event:", recallPayrollEvents.size() > 0);
        DomainEntitySet<CompanyEvent> recallPaycheckEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PaycheckRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Paycheck Recall Events:", recallPaycheckEvents.size() > 0);
        return results;
    }

    @Test
    public void testTransactionRecallPayrollCanceled() {
        //Recall Whole Payroll
        recallWholePayrollBeforeCutoff();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        TransactionCancelEEDTO recallDTO = getBaseDTO();
        recallDTO.setRequestId("CANCELED");
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Error Count:", 1, results.getMessages().size());
        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "1017", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "Payroll Run with DDTxBatchID " + SOURCE_PAYROLL_ID1 + " for company QBDT:123272727 has already been canceled.",
                message.getMessage());
    }

    @Test
    public void testTransactionRecallPayrollOffloaded() {
        //Load payroll data
        loadData();

        //Offload All the Transactions for Payroll Run : BatchId01
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        TransactionCancelEEDTO recallDTO = getBaseDTO();
        recallDTO.setRequestId("EXECUTED");
        recallDTO.setSourcePayrollRunId("BatchId01");
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);
        PayrollServices.commitUnitOfWork();

        assertEquals("Error Count:", 1, results.getMessages().size());
        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "1015", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "Transactions for DDTxBatch BatchId01 for company QBDT:123272727 have already been sent to the bank and cannot be recalled.",
                message.getMessage());
    }

    @Test
    public void testTransactionRecallTxNotExist() {
        //Load payroll data
        loadData();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 25, 16, 59, 0, 0, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);

        TransactionCancelEEDTO recallDTO = getBaseDTO();

        List<String> paycheckIds = new ArrayList<String>();
        paycheckIds.add("P1-IDONTEXIST");
        recallDTO.setSourcePaycheckIdList(paycheckIds);

        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);
        PayrollServices.commitUnitOfWork();

        // Verify Error Message
        Message message = results.getMessages().get(0);
        assertEquals("Error Count:", 1, results.getMessages().size());
        assertEquals("Message Code:", "299", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals("Message Text:",
                "Paycheck P1-IDONTEXIST for company QBDT:123272727 does not exist.",
                message.getMessage());
    }

    @Test
    public void testRecallSelectedTransactionsForPendingPayroll() {
        loadData();

        List<String> sourcePaycheckIdList = new Vector<String>(4);
        BigDecimal newPayrollNetAmt = null;
        PayrollServices.beginUnitOfWork();
        // Set time to before cutoff
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 25, 16, 59, 0, 0, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        TransactionCancelEEDTO recallDTO = getBaseDTO();

        Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun
                .findPayrollRun(company, recallDTO.getSourcePayrollRunId());

        BigDecimal cancelTxnAmt = new BigDecimal(0);
        DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();

        // get the source dd transaction id's for the first paycheck that has multiple splits
        for (Iterator<Paycheck> checkIter = paycheckList.iterator(); checkIter.hasNext() && sourcePaycheckIdList
                .isEmpty(); ) {
            Paycheck check = checkIter.next();
            DomainEntitySet<PaycheckSplit> splitList = check.getPaycheckSplitCollection();

            if ((splitList != null) && (splitList.size() > 1)) {
                sourcePaycheckIdList.add(splitList.get(0).getPaycheck().getSourcePaycheckId());
                for (Iterator<PaycheckSplit> splitIter = splitList.iterator(); splitIter.hasNext(); ) {
                    PaycheckSplit split = splitIter.next();
                    //dtoTxnList.add(split.getSourceDdTxnId());
                    cancelTxnAmt = cancelTxnAmt.add(SpcfUtils.convertToBigDecimal(
                            split.getFinancialTransaction().getFinancialTransactionAmount()));
                }
            }
        }

        newPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
        newPayrollNetAmt = newPayrollNetAmt.subtract(cancelTxnAmt);
        recallDTO.setSourcePaycheckIdList(sourcePaycheckIdList);
        PayrollServices.commitUnitOfWork();

        // Create a Transmission
        String transmissionId = SpcfUniqueId.createInstance(true).toString();
        SourceSystemTransmissionDTO sourceSystemTransmissionDTO = new SourceSystemTransmissionDTO();
        sourceSystemTransmissionDTO.setRequestToken(1L);

        sourceSystemTransmissionDTO.setRequestDocument("REQUEST OFX");
        sourceSystemTransmissionDTO.setTransmissionType(TransmissionType.Sync);
        ProcessResult<SourceSystemTransmission> result = PayrollServices.transmissionManagerSecondary.initializeTransmission(SourceSystemCode.QBDT,
                "123272727", transmissionId, sourceSystemTransmissionDTO);
        // Check that transmission was successfully created
        assertSuccess("initializeSourceSystemTransmission", result);
        SourceSystemTransmission sourceSystemTransmission = result.getResult();

        PayrollServices.beginUnitOfWork();
        recallDTO.setTransmissionId(sourceSystemTransmission.getTransmissionIdentifier());
        ProcessResult<TransactionResponse> processResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);

        //Save the updated billing details by calling BillingManager.save() method
        saveBillingDetails();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(COMPANY1, SourceSystemCode.QBDT);
        payrollRun = PayrollRun
                .findPayrollRun(company, SOURCE_PAYROLL_ID1);

        DomainEntitySet<TransactionResponse> txnResponseList;
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findPaycheckSplitFinancialTransactions(
                        payrollRun,
                        TransactionStateCode.Cancelled);

        // verify all appropriate paycheck split transactions were cancelled.
        for (FinancialTransaction txn : txnList) {
            assertTrue("Cancelled transaction", sourcePaycheckIdList.contains(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId()));

            // verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(txn);
            assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

            assertNull("MM txn for cancelled fintxn", txn.getMoneyMovementTransaction());
        }

        // verify the payroll net amount was updated correctly.
        assertEquals("Payroll Net Amount",
                SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                payrollRun.getPayrollDirectDepositAmount());

        // verify the old EMPLOYER_DD_DEBIT transaction was cancelled
        txnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        assertEquals("Cancelled Employer DD Debit transaction count", 1, txnList.size());

        FinancialTransaction oldDebitTxn = txnList.get(0);

        // verify the transaction response was created correctly for the old EMPLOYER_DD_DEBIT txn
        txnResponseList = TransactionResponse.findTransactionResponses(oldDebitTxn);
        assertEquals("Transaction response for old ER debit transaction", 1, txnResponseList.size());

        // verify a new EMPLOYER_DD_DEBIT transaction was created and is correct in all respects
        txnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("New Employer DD Debit transaction count", 1, txnList.size());

        FinancialTransaction newDebitTxn = txnList.get(0);

        // verify the credit account is from the original txn credit account
        assertEquals("New Employer DD Debit transaction credit account",
                oldDebitTxn.getCreditBankAccount().getId(),
                newDebitTxn.getCreditBankAccount().getId());

        // verify the debit account is from the original txn debit account
        assertEquals("New Employer DD Debit transaction debit account",
                oldDebitTxn.getDebitBankAccount().getId(),
                newDebitTxn.getDebitBankAccount().getId());

        // verify the new debit amount is correct (equal to newPayrollNetAmt)
        assertEquals("New Employer DD Debit transaction debit amount",
                SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                newDebitTxn.getFinancialTransactionAmount());

        // verify the new debit txn settlement type is ACH
        assertEquals("New Employer DD Debit transaction settlement type",
                SettlementType.ACH,
                newDebitTxn.getSettlementTypeCd());

        // verify the settlement date for the debit txn
        SpcfCalendar expectedSettlementDate = oldDebitTxn.getSettlementDate().toLocal().copy();
        CalendarUtils.clearTime(expectedSettlementDate);

        SpcfCalendar txnSettlementDate = newDebitTxn.getSettlementDate().toLocal().copy();
        CalendarUtils.clearTime(txnSettlementDate);

        SpcfCalendar expectedInitDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 28, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());

        assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

        // verify the transaction response was created correctly for the new EMPLOYER_DD_DEBIT txn
        txnResponseList = TransactionResponse.findTransactionResponses(newDebitTxn);
        assertEquals("Transaction response for new ER debit transaction", 1, txnResponseList.size());

        //Verify the mm txn and entry detail records
        MoneyMovementTransaction originalMMTxn = oldDebitTxn.getMoneyMovementTransaction();
        assertNull(originalMMTxn);
        MoneyMovementTransaction mmTxn = newDebitTxn.getMoneyMovementTransaction();
        assertNotNull(mmTxn);
        DomainEntitySet<FinancialTransaction> finTxns = mmTxn.getFinancialTransactionCollection();
        SpcfMoney amount = new SpcfMoney("0");
        for (FinancialTransaction finTxn : finTxns) {
            amount = new SpcfMoney(amount.add(finTxn.getFinancialTransactionAmount()));
        }

        PayrollSubmitCoreTests.validateNewMMTxn(mmTxn, amount,
                txnSettlementDate, expectedInitDate, newDebitTxn);

        TransactionType erddDB = Application.findById(TransactionType.class, TransactionTypeCode.EmployerDdDebit);
        IntuitBankAccount expectedIntuitBA = IntuitBankAccount
                .findIntuitBankAccount(erddDB, CreditDebitCode.Credit);

        EntryDetailRecord entryDetailRecord = mmTxn.getEntryDetailRecordCollection().iterator().next();
        String currencyString = StringFormatter
                .formatCurrencyNoDecimalPoint(
                        SpcfUtils.convertToBigDecimal(newDebitTxn.getFinancialTransactionAmount()), 10);
        String strExpectedRecordData = "627" + oldDebitTxn.getDebitBankAccount()
                .getRoutingNumber() + StringFormatter
                .formatString(oldDebitTxn.getDebitBankAccount().getAccountNumber(),
                        17) + currencyString + StringFormatter.formatString(company
                .getFedTaxId(), 15) + StringFormatter.formatString(company.getLegalName(), 22) + "  0";

        //Assert Old DdServiceFee transaction was Cancelled and new DdServiceFee Transaction posted with new amount
        DomainEntitySet<FinancialTransaction> feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        assertEquals("Number of DdServiceFee Cancelled txns", 1, feeTxnList.size());

        feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of DdServiceFee Created txns", 1, feeTxnList.size());

        //Assert the Billing Detials by passing the PayrollRun & Number of NonCancelled Paychecks
        //by subtracting the cancelled paychecks from the total paychecks
        checkBillingDetails(payrollRun.getPaycheckCollection().size() - 1);
        // Verify Events
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> recallPayrollEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Payroll Recall Event:", recallPayrollEvents.size() == 0);
        DomainEntitySet<CompanyEvent> recallPaycheckEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PaycheckRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Paycheck Recall Events:", recallPaycheckEvents.size() == 1);
        // Verify Transmission
        DomainEntitySet<TransmissionPayrollRun> transmissionPayrollSet = payrollRun.getTransmissionPayrollRunCollection();
        assertEquals("Number of TransmissionPayrollRuns", 1, transmissionPayrollSet.size());
        assertEquals("Payroll Process Code", PayrollProcessCode.RecallTransaction, transmissionPayrollSet.get(0).getPayrollProcess());
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Obtains a baseline TransactionRecallDTO
     *
     * @return Returns the base DTO for use with transaction recall operations
     */

    private TransactionCancelEEDTO getBaseDTO() {
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId(SOURCE_PAYROLL_ID1);
        recallDTO.setRequestId(REQUEST_ID1);
        //recallDTO.setServiceCd(ServiceCode.DirectDeposit);
        return recallDTO;
    }

    /**
     * Build a process result that is specific to the current payroll. This is needed since the test payrolls are
     * generated with random financial transaction amounts.
     *
     * @param pEmployee  The employee who's transaction(s) are being cancelled.
     * @param pSplitList The list of paycheck splits for the given employee.
     * @return The process result as it should appear after the test fails.
     */
    private ProcessResult buildExpectedProcessResult(Employee pEmployee, List<PaycheckSplit> pSplitList) {
        ProcessResult processResult = new ProcessResult();
        String name, employeeName, amountStr = "";

        // assemble the employee name
        name = pEmployee.getFirstName();
        employeeName = (((name != null) && (name.length() > 0)) ? name + " " : "");
        name = pEmployee.getMiddleName();
        employeeName += (((name != null) && (name.length() > 0)) ? name + " " : "");
        name = pEmployee.getLastName();
        employeeName += (((name != null) && (name.length() > 0)) ? name + " " : "");

        // build a string of amounts (not including the first one, which we attempted to cancel)
        for (ListIterator<PaycheckSplit> splitIter = pSplitList.listIterator(1); splitIter.hasNext(); ) {
            PaycheckSplit split = splitIter.next();
            amountStr += ((amountStr.length() == 0) ? "" : ", ") + split.getPaycheckSplitAmount().toString();
        }

        // build the error as it should appear based on the failure
        processResult.getMessages().CannotCancelPartialPaychecks(
                EntityName.DDTransaction,
                pSplitList.get(0).getSourceDdTxnId(),
                employeeName.trim(),
                pSplitList.get(0).getPaycheckSplitAmount().toString(),
                amountStr);

        return processResult;
    }

    private void saveBillingDetails() {
        Company company1 = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company1, "BatchId01");
        //Statement to save by calling BillingManager.save() method for the BillingDetails which was added by
        // PayrollSubmitDD process by calling BillingManager.add() method.
        CompanyBankAccount companyBankAccount = payrollRun1
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
    }

    private void checkBillingDetails(int pQuantity) {
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        // Double check transactions
        PayrollRun payrollRun = PayrollRun
                .findPayrollRun(company, SOURCE_PAYROLL_ID1);

        CompanyBankAccount companyBankAccount = payrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        //Assertions for Billing Details.
        assertEquals("BillingDetail List Size ", 1, payrollRun.getBillingDetailCollection().size());
        assertEquals("Quantity ", pQuantity, payrollRun.getBillingDetailCollection().get(0).getQuantity());
        if (pQuantity == 0) {
            assertNull("Tax Transaction ", payrollRun.getBillingDetailCollection().get(0).getTaxTransaction());
            assertNull("Fee Transaction ", payrollRun.getBillingDetailCollection().get(0).getFeeTransaction());
        }
    }

    @Test
    public void testRecallSelectedTransactionsForPendingPayroll_RecallOnePayCheckAtATime() {
        //Load payroll data
        loadData();

        //Recall First Paycheck
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        BigDecimal newPayrollNetAmt = null;
        PayrollServices.beginUnitOfWork();
        // Set time to before cutoff
        SpcfCalendar testTime = SpcfCalendar.createInstance(2007, 9, 25, 16, 59, 0, 0, SpcfTimeZone.getLocalTimeZone());
        PSPDate.setPSPTime(testTime);
        TransactionCancelEEDTO recallDTO = getBaseDTO();

        Company company = Company.findCompany(COMPANY1, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun
                .findPayrollRun(company, recallDTO.getSourcePayrollRunId());

        BigDecimal cancelTxnAmt = new BigDecimal(0);
        DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();

        // get the source dd transaction id's for the first paycheck that has multiple splits
        for (Iterator<Paycheck> checkIter = paycheckList.iterator(); checkIter.hasNext() && sourcePaycheckIdList
                .isEmpty(); ) {
            Paycheck check = checkIter.next();
            DomainEntitySet<PaycheckSplit> splitList = check.getPaycheckSplitCollection();

            if ((splitList != null) && (splitList.size() > 1)) {
                sourcePaycheckIdList.add(splitList.get(0).getPaycheck().getSourcePaycheckId());
                for (Iterator<PaycheckSplit> splitIter = splitList.iterator(); splitIter.hasNext(); ) {
                    PaycheckSplit split = splitIter.next();
                    cancelTxnAmt = cancelTxnAmt.add(SpcfUtils.convertToBigDecimal(
                            split.getFinancialTransaction().getFinancialTransactionAmount()));
                }
            }
        }

        newPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
        newPayrollNetAmt = newPayrollNetAmt.subtract(cancelTxnAmt);
        recallDTO.setSourcePaycheckIdList(sourcePaycheckIdList);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<TransactionResponse> processResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO);

        //Save the updated billing details by calling BillingManager.save() method
        saveBillingDetails();
        PayrollServices.commitUnitOfWork();

        // If any errors do occur, write them out to stdout before the assertion...
        if (!processResult.isSuccess()) {
            for (Message msg : processResult.getMessages()) {
                System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
            }
        }

        // validate error count
        assertEquals("Number of Errors", 0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(COMPANY1, SourceSystemCode.QBDT);
        payrollRun = PayrollRun
                .findPayrollRun(company, SOURCE_PAYROLL_ID1);

        DomainEntitySet<TransactionResponse> txnResponseList;
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findPaycheckSplitFinancialTransactions(
                        payrollRun,
                        TransactionStateCode.Cancelled);

        // verify all appropriate paycheck split transactions were cancelled.
        for (FinancialTransaction txn : txnList) {
            assertTrue("Cancelled transaction", sourcePaycheckIdList.contains(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId()));

            // verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(txn);
            assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

            assertNull("MM txn for cancelled fintxn", txn.getMoneyMovementTransaction());
        }

        // verify the payroll net amount was updated correctly.
        assertEquals("Payroll Net Amount",
                SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                payrollRun.getPayrollDirectDepositAmount());

        // verify the old EMPLOYER_DD_DEBIT transaction was cancelled
        txnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        assertEquals("Cancelled Employer DD Debit transaction count", 1, txnList.size());

        FinancialTransaction oldDebitTxn = txnList.get(0);

        // verify the transaction response was created correctly for the old EMPLOYER_DD_DEBIT txn
        txnResponseList = TransactionResponse.findTransactionResponses(oldDebitTxn);
        assertEquals("Transaction response for old ER debit transaction", 1, txnResponseList.size());

        // verify a new EMPLOYER_DD_DEBIT transaction was created and is correct in all respects
        txnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("New Employer DD Debit transaction count", 1, txnList.size());

        FinancialTransaction newDebitTxn = txnList.get(0);

        // verify the credit account is from the original txn credit account
        assertEquals("New Employer DD Debit transaction credit account",
                oldDebitTxn.getCreditBankAccount().getId(),
                newDebitTxn.getCreditBankAccount().getId());

        // verify the debit account is from the original txn debit account
        assertEquals("New Employer DD Debit transaction debit account",
                oldDebitTxn.getDebitBankAccount().getId(),
                newDebitTxn.getDebitBankAccount().getId());

        // verify the new debit amount is correct (equal to newPayrollNetAmt)
        assertEquals("New Employer DD Debit transaction debit amount",
                SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                newDebitTxn.getFinancialTransactionAmount());

        // verify the new debit txn settlement type is ACH
        assertEquals("New Employer DD Debit transaction settlement type",
                SettlementType.ACH,
                newDebitTxn.getSettlementTypeCd());

        // verify the settlement date for the debit txn
        SpcfCalendar expectedSettlementDate = oldDebitTxn.getSettlementDate().toLocal().copy();
        CalendarUtils.clearTime(expectedSettlementDate);

        SpcfCalendar txnSettlementDate = newDebitTxn.getSettlementDate().toLocal().copy();
        CalendarUtils.clearTime(txnSettlementDate);

        SpcfCalendar expectedInitDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 28, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());

        assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

        // verify the transaction response was created correctly for the new EMPLOYER_DD_DEBIT txn
        txnResponseList = TransactionResponse.findTransactionResponses(newDebitTxn);
        assertEquals("Transaction response for new ER debit transaction", 1, txnResponseList.size());

        //Verify the mm txn and entry detail records
        MoneyMovementTransaction originalMMTxn = oldDebitTxn.getMoneyMovementTransaction();
        assertNull(originalMMTxn);
        MoneyMovementTransaction mmTxn = newDebitTxn.getMoneyMovementTransaction();
        assertNotNull(mmTxn);
        DomainEntitySet<FinancialTransaction> finTxns = mmTxn.getFinancialTransactionCollection();
        SpcfMoney amount = new SpcfMoney("0");
        for (FinancialTransaction finTxn : finTxns) {
            amount = new SpcfMoney(amount.add(finTxn.getFinancialTransactionAmount()));
        }

        PayrollSubmitCoreTests.validateNewMMTxn(mmTxn, amount,
                txnSettlementDate, expectedInitDate, newDebitTxn);

        TransactionType erddDB = Application.findById(TransactionType.class, TransactionTypeCode.EmployerDdDebit);
        IntuitBankAccount expectedIntuitBA = IntuitBankAccount
                .findIntuitBankAccount(erddDB, CreditDebitCode.Credit);

        EntryDetailRecord entryDetailRecord = mmTxn.getEntryDetailRecordCollection().iterator().next();
        String currencyString = StringFormatter
                .formatCurrencyNoDecimalPoint(
                        SpcfUtils.convertToBigDecimal(newDebitTxn.getFinancialTransactionAmount()), 10);
        String strExpectedRecordData = "627" + oldDebitTxn.getDebitBankAccount()
                .getRoutingNumber() + StringFormatter
                .formatString(oldDebitTxn.getDebitBankAccount().getAccountNumber(),
                        17) + currencyString + StringFormatter.formatString(company
                .getFedTaxId(), 15) + StringFormatter.formatString(company.getLegalName(), 22) + "  0";

        //Assert Old DdServiceFee transaction was Cancelled and new DdServiceFee Transaction posted with new amount
        DomainEntitySet<FinancialTransaction> feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        assertEquals("Number of DdServiceFee Cancelled txns", 1, feeTxnList.size());

        feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of DdServiceFee Created txns", 1, feeTxnList.size());

        //Assert the Billing Detials by passing the PayrollRun & Number of NonCancelled Paychecks
        //by subtracting the cancelled paychecks from the total paychecks
        checkBillingDetails(payrollRun.getPaycheckCollection().size() - 1);

        // Verify Event Creation
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> recallPayrollEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Payroll Recall Event:", recallPayrollEvents.size() == 0);
        DomainEntitySet<CompanyEvent> recallPaycheckEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PaycheckRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Paycheck Recall Events:", recallPaycheckEvents.size() == 1);
        PayrollServices.commitUnitOfWork();

        //Recall Second Paycheck
        PayrollServices.beginUnitOfWork();

        List<String> sourcePaycheckIdList1 = new Vector<String>(4);
        TransactionCancelEEDTO recallDTO1 = getBaseDTO();
        Paycheck nonCancelledPayCheck = null;
        // get the non Cancelled Paycheck from the paycheck collection for the PayrollRun "BatchId01"
        for (Iterator<Paycheck> checkIter = paycheckList.iterator(); checkIter.hasNext() && sourcePaycheckIdList1
                .isEmpty(); ) {
            Paycheck check = checkIter.next();
            nonCancelledPayCheck = Paycheck.findNonCanceledPaycheck(company, check.getSourcePaycheckId());

            if (nonCancelledPayCheck != null) {
                break;
            }
        }

        DomainEntitySet<PaycheckSplit> splitList = nonCancelledPayCheck.getPaycheckSplitCollection();

        if ((splitList != null) && (splitList.size() > 1)) {
            sourcePaycheckIdList1.add(splitList.get(0).getPaycheck().getSourcePaycheckId());
            for (Iterator<PaycheckSplit> splitIter = splitList.iterator(); splitIter.hasNext(); ) {
                PaycheckSplit split = splitIter.next();
                cancelTxnAmt = cancelTxnAmt.add(SpcfUtils.convertToBigDecimal(
                        split.getFinancialTransaction().getFinancialTransactionAmount()));
            }
        }
        recallDTO1.setSourcePaycheckIdList(sourcePaycheckIdList1);
        recallDTO1.setRequestId("2");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBDT, COMPANY1, recallDTO1);

        //Save the updated billing details by calling BillingManager.save() method
        saveBillingDetails();
        PayrollServices.commitUnitOfWork();

        assertSuccess("ProcessResult ", processResult);


        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        for (FinancialTransaction transaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals("Transaction Status", TransactionStateCode.Cancelled, transaction
                    .getCurrentTransactionState().getTransactionStateCd());
        }
        //Assertion for DdServiceFee transaction was Cancelled
        feeTxnList = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        assertEquals("Number of EmployerFeeDebit Cancelled txns", 2, feeTxnList.size());

        //Assertions for Billing Details.
        checkBillingDetails(0);
        // Verify Event Creation
        company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        recallPayrollEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Payroll Recall Event:", recallPayrollEvents.size() == 0);
        recallPaycheckEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PaycheckRecalled, CompanyEventStatus.Active, null, null);
        assertTrue("Paycheck Recall Events:", recallPaycheckEvents.size() == 2);
        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void testPartialTransactionRecallAfterDebitOffloaded() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        PSPDate.setPSPTime("20070904000000");
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925171500");
        Application.commitUnitOfWork();

        OffloadACHTransactions offload = new OffloadACHTransactions();
        offload.offloadAndPostOffload("STD", null);


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        PSPDate.setPSPTime("20070926100000");
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest05");

        List<String> sourcePaycheckIds = new ArrayList<String>();
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());
        recallDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        recallDTO.setRequestId("dummy");

        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", recallDTO);
        PayrollServices.commitUnitOfWork();
        assertEquals("Error Count:", 1, results.getMessages().size());
        Message message = results.getMessages().get(0);
        assertEquals("Message Code:", "1015", message.getMessageCode());
        assertEquals("Message Level:", MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "Transactions for DDTxBatch BatchTest05 for company QBOE:1234567 have already been sent to the bank and cannot be recalled.",
                message.getMessage());
    }

    @Test
    public void testRecallSelectedTransactionsForBackDatedPayroll() {
        BigDecimal newPayrollNetAmt = null;
        Company1Dataloader c1dl = new Company1Dataloader();
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-07"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(payrollRunDTO);

        // make sure the fee, added during payroll-submission, gets saved
        CompanyBankAccount cba = CompanyBankAccount
                .findActiveCompanyBankAccount(payrollRun.getCompany());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest05");

        List<String> sourcePaycheckIds = new ArrayList<String>();
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());

        recallDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        recallDTO.setRequestId("dummy");

        newPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        PaycheckSplit cancelledSplit = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        newPayrollNetAmt = newPayrollNetAmt.subtract(SpcfUtils.convertToBigDecimal(cancelledSplit.getPaycheckSplitAmount()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<TransactionResponse> results = PayrollServices.payrollManager
                .cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", recallDTO);
        PayrollServices.commitUnitOfWork();
        System.out.println("Result" + results.getMessages());
        assertSuccess("RecallTransaction", results);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            company = Company.findCompany("1234567", SourceSystemCode.QBOE);
            payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
            DomainEntitySet<TransactionResponse> txnResponseList;
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(
                            payrollRun,
                            TransactionStateCode.Cancelled);

            // verify all appropriate paycheck split transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertTrue("Cancelled transaction", sourcePaycheckIds.contains(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId()));

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);
                assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

                assertNull("MM txn for cancelled fintxn", txn.getMoneyMovementTransaction());
            }

            BigDecimal cancelTxnAmt = new BigDecimal(0);
            // verify all appropriate paycheck split transactions were cancelled.
            for (String txnId : recallDTO.getSourcePaycheckIdList()) {
                Paycheck paycheck = Paycheck.findPaycheck(company, txnId);
                assertNotNull("Paycheck of cancelled txn", paycheck);

                for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
                    FinancialTransaction txn = paycheckSplit.getFinancialTransaction();

                    assertNotNull("Cancelled transction from paycheck eplit", txn);

                    // verify the transaction was cancelled
                    assertEquals("Cancelled transction state", TransactionStateCode.Cancelled, txn.getCurrentTransactionState().getTransactionStateCd());

                    // verify the transaction response was created correctly for this txn
                    txnResponseList = TransactionResponse.findTransactionResponses(txn);
                    assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

                    // keep a running total of the sum of the cancelled txns
                    cancelTxnAmt = cancelTxnAmt.add(SpcfUtils.convertToBigDecimal(txn.getFinancialTransactionAmount()));

                    //Make sure the mm txn was deleted
                    assertNull("MM txn for cancelled fin txn", txn.getMoneyMovementTransaction());
                }
            }

            // verify the payroll net amount was updated correctly.
            assertEquals("Payroll Net Amount",
                    SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                    payrollRun.getPayrollDirectDepositAmount());

            // verify the old EMPLOYER_DD_DEBIT transaction was cancelled
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                    new TransactionStateCode[]{TransactionStateCode.Cancelled});

            assertEquals("Cancelled Employer DD Debit transaction count", 1, txnList.size());

            FinancialTransaction oldDebitTxn = txnList.get(0);

            // Verify Event Creation
            company = Company.findCompany("1234567", SourceSystemCode.QBOE);
            DomainEntitySet<CompanyEvent> recallPayrollEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollRecalled, CompanyEventStatus.Active, null, null);
            assertTrue("Payroll Recall Event:", recallPayrollEvents.size() == 0);
            DomainEntitySet<CompanyEvent> recallPaycheckEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.PaycheckRecalled, CompanyEventStatus.Active, null, null);
            assertTrue("Paycheck Recall Events:", recallPaycheckEvents.size() == 1);

            // verify the transaction response was created correctly for the old EMPLOYER_DD_DEBIT txn
            txnResponseList = TransactionResponse.findTransactionResponses(oldDebitTxn);
            assertEquals("Transaction response for old ER debit transaction", 1, txnResponseList.size());

            // verify a new EMPLOYER_DD_DEBIT transaction was created and is correct in all respects
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("New Employer DD Debit transaction count", 1, txnList.size());

            FinancialTransaction newDebitTxn = txnList.get(0);

            // verify the credit account is from the original txn credit account
            assertEquals("New Employer DD Debit transaction credit account",
                    oldDebitTxn.getCreditBankAccount().getId(),
                    newDebitTxn.getCreditBankAccount().getId());

            // verify the debit account is from the original txn debit account
            assertEquals("New Employer DD Debit transaction debit account",
                    oldDebitTxn.getDebitBankAccount().getId(),
                    newDebitTxn.getDebitBankAccount().getId());

            // verify the new debit amount is correct (equal to newPayrollNetAmt)

            assertEquals("New Employer DD Debit transaction debit amount",
                    SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                    newDebitTxn.getFinancialTransactionAmount());

            // verify the new debit txn settlement type is ACH
            assertEquals("New Employer DD Debit transaction settlement type",
                    SettlementType.ACH,
                    newDebitTxn.getSettlementTypeCd());

            // verify the settlement date for the debit txn
            SpcfCalendar expectedSettlementDate = oldDebitTxn.getSettlementDate().toLocal().copy();
            CalendarUtils.clearTime(expectedSettlementDate);

            SpcfCalendar txnSettlementDate = newDebitTxn.getSettlementDate().toLocal().copy();
            CalendarUtils.clearTime(txnSettlementDate);

            SpcfCalendar expectedInitDate = SpcfCalendar.createInstance(2007, SpcfCalendar.September, 14, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());

            assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

            // verify the transaction response was created correctly for the new EMPLOYER_DD_DEBIT txn
            txnResponseList = TransactionResponse.findTransactionResponses(newDebitTxn);
            assertEquals("Transaction response for new ER debit transaction", 1, txnResponseList.size());

            //Verify the mm txn and entry detail records
            MoneyMovementTransaction originalMMTxn = oldDebitTxn.getMoneyMovementTransaction();
            assertNull(originalMMTxn);
            MoneyMovementTransaction mmTxn = newDebitTxn.getMoneyMovementTransaction();
            assertNotNull(mmTxn);
            DomainEntitySet<FinancialTransaction> finTxns = mmTxn.getFinancialTransactionCollection();
            SpcfMoney amount = new SpcfMoney("0");
            for (FinancialTransaction finTxn : finTxns) {
                amount = new SpcfMoney(amount.add(finTxn.getFinancialTransactionAmount()));
            }

            PayrollSubmitCoreTests.validateNewMMTxn(mmTxn, amount,
                    txnSettlementDate, expectedInitDate, newDebitTxn);
        }

    }

    @Test
    public void test_Negative_Liability_Payroll_Recall() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        Company company = DataLoadServices.setupCompanyWithNegativeLiability_IRS_NM();

        String psid = company.getSourceCompanyId();
        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        PayrollServices.beginUnitOfWork();
        //Recall entire payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(sourcePayrollId);
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        assertSuccess(recallProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate941 = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        PaymentTemplate paymentTemplate940 = PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT");

        //Assert for ATCs
        DomainEntitySet<FinancialTransaction> agencyCredits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of ATCs for 941 payment template", 5,
                agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATCs for 940 payment template", 1,
                agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());

        //Recalled Payroll ATC status
        assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled)
                .find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))));

        //Assert for ATDs
        DomainEntitySet<FinancialTransaction> agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of ATDs for 941 payment template", 5,
                agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATDs for 940 payment template", 1,
                agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATD for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                agencyDebits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());

        //Recalled Payroll ATD status
        assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Cancelled)
                .find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))));

        //Assert for ATOs
        DomainEntitySet<FinancialTransaction> agencyOverPayments = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpayment, TransactionStateCode.Executed);
        assertEquals("Number of ATOs for 941 payment template", 5,
                agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATOs for 940 payment template", 1,
                agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATO for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());

        //Assert for ETOAs
        DomainEntitySet<FinancialTransaction> employerTaxOverPaymentsApplied = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Created);
        assertEquals("Number of ETOAs for 941 payment template", 5,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ETOAs for 940 payment template", 1,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ETOA for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());

        //Recalled Payroll ETOA status
        assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Cancelled)
                .find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))));

        //Recalled Payroll ERTDb status
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        assertEquals("ER Tax Debit amount", SpcfMoney.ZERO, assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Cancelled, TransactionTypeCode.EmployerTaxDebit)).getFinancialTransactionAmount());

        //Adjustment Payroll ERTDb status
        PayrollRun adjustmentPayroll = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Adjustment));
        assertEquals("ER Tax Debit amount", SpcfMoney.ZERO, adjustmentPayroll.getEmployerTaxDebitTransaction().getFinancialTransactionAmount());
        assertEquals("ER Tax Debit status", TransactionState.findTransactionState(TransactionStateCode.Created), adjustmentPayroll.getEmployerTaxDebitTransaction().getCurrentTransactionState());

        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 26.00),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -26.00));
    }

    @Test
    public void test_Negative_Liability_Payroll_Recall_NegativePaycheck() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        Company company = DataLoadServices.setupCompanyWithNegativeLiability_IRS_NM();

        String psid = company.getSourceCompanyId();
        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        Paycheck negativePaycheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getTaxCollection().find(Tax.Law().LawId().equalTo("1").And(Tax.TaxLiabilityAmount().equalTo(new SpcfMoney("-2")))).size() == 1) {
                negativePaycheck = paycheck;
            }
        }

        //Recall only negative paycheck
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(sourcePayrollId);
        transactionCancelDTO.setSourcePaycheckIdList(Arrays.asList(negativePaycheck.getSourcePaycheckId()));
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        assertSuccess(recallProcessResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate941 = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        PaymentTemplate paymentTemplate940 = PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT");

        //Assert for ATCs
        DomainEntitySet<FinancialTransaction> agencyCredits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of ATCs for 941 payment template", 5,
                agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().notEqualTo(new SpcfMoney("1")))).size());
        assertEquals("IRS-941 Payment", new SpcfMoney("51"), agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941)).getFirst().getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("Number of ATCs for 940 payment template", 1,
                agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("16")))).size());
        assertEquals("IRS-940 Payment", new SpcfMoney("15"), agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940)).getFirst().getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 192, with $5 ", 1,
                agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")))).size());

        //Partial Recalled payroll ATC
        FinancialTransaction financialTransaction = assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled));
        assertEquals("Cancelled AgencyCredit Law Id", "192", financialTransaction.getLaw().getLawId());
        assertEquals("Cancelled AgencyCredit Amount", new SpcfMoney("10"), financialTransaction.getFinancialTransactionAmount());

        //Assert for ATDs
        DomainEntitySet<FinancialTransaction> agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of ATDs for 941 payment template", 5,
                agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATDs for 940 payment template", 1,
                agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATD for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                agencyDebits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Partial Recalled payroll ATDs
        agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Cancelled)
                .find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")).And(FinancialTransaction.Law().LawId().equalTo("191")));
        assertEquals("Number of Cancelled ATDs", 1, agencyDebits.size());
        agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created)
                .find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")).And(FinancialTransaction.Law().LawId().equalTo("191")));
        assertEquals("Number of Created ATDs", 1, agencyDebits.size());

        //Assert for ATOs
        DomainEntitySet<FinancialTransaction> agencyOverPayments = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpayment, TransactionStateCode.Executed);
        assertEquals("Number of ATOs for 941 payment template", 5,
                agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATOs for 940 payment template", 1,
                agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATO for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());

        //Assert for ERTOAs
        DomainEntitySet<FinancialTransaction> employerTaxOverPaymentsApplied = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Created);
        assertEquals("Number of ERTOAs for 941 payment template", 5,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ERTOAs for 940 payment template", 1,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Partial Recalled payroll ERTOA
        assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Cancelled)
                .find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))));
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 191, with $5", 1,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")))).size());

        payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        assertEquals("Cancelled ER Tax Debit amount", SpcfMoney.ZERO, assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Cancelled, TransactionTypeCode.EmployerTaxDebit)).getFinancialTransactionAmount());

        //Adjustment Payroll ERTDb status
        PayrollRun adjustmentPayroll = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Adjustment));
        assertEquals("ER Tax Debit amount", new SpcfMoney("66"), adjustmentPayroll.getEmployerTaxDebitTransaction().getFinancialTransactionAmount());
        assertEquals("Created ER Tax Debit amount", SpcfMoney.ZERO, assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Cancelled, TransactionTypeCode.EmployerTaxDebit)).getFinancialTransactionAmount());

        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 21.00),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -26.00),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 5.00),
                new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 66.00),
                new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 66.00));
    }

    @Test
    public void test_Negative_Liability_Payroll_Recall_PositivePaycheck() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        Company company = DataLoadServices.setupCompanyWithNegativeLiability_IRS_NM();

        String psid = company.getSourceCompanyId();
        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        Paycheck positivePaycheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getTaxCollection().find(Tax.Law().LawId().equalTo("1").And(Tax.TaxLiabilityAmount().equalTo(new SpcfMoney("1")))).size() == 1) {
                positivePaycheck = paycheck;
            }
        }

        //Recall only positive paycheck
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(sourcePayrollId);
        transactionCancelDTO.setSourcePaycheckIdList(Arrays.asList(positivePaycheck.getSourcePaycheckId()));
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        assertSuccess(recallProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate941 = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        PaymentTemplate paymentTemplate940 = PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT");

        //Assert for ATCs
        DomainEntitySet<FinancialTransaction> agencyCredits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of ATCs for 941 payment template", 0, agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941)).size());
        assertEquals("Number of ATCs for 940 payment template", 0, agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940)).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 192, with $5 ", 1,
                agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")))).size());
        FinancialTransaction financialTransaction = assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled));
        assertEquals("Cancelled AgencyCredit Law Id", "192", financialTransaction.getLaw().getLawId());
        assertEquals("Cancelled AgencyCredit Amount", new SpcfMoney("10"), financialTransaction.getFinancialTransactionAmount());

        //Assert for ATDs
        DomainEntitySet<FinancialTransaction> agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Cancelled)
                .find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")).And(FinancialTransaction.Law().LawId().equalTo("191")));
        assertEquals("Number of Cancelled ATDs", 1, agencyDebits.size());
        agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created)
                .find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")).And(FinancialTransaction.Law().LawId().equalTo("191")));
        assertEquals("Number of Created ATDs", 1, agencyDebits.size());
        agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created)
                .find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")).And(FinancialTransaction.Law().LawId().equalTo("191")));
        assertEquals("Number of Created ATDs", 1, agencyDebits.size());

        //Assert for ATOs
        DomainEntitySet<FinancialTransaction> agencyOverPayments = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpayment, TransactionStateCode.Executed);
        assertEquals("Number of ATOs for 941 payment template", 6,
                agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATOs for 940 payment template", 1,
                agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATO for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());
        assertEquals("Number of ATOs for 941 payment template Law Id - 62", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("62").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("11")))).size());
        assertEquals("Number of ATOs for 941 payment template Law Id - 61", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("61").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("12")))).size());
        assertEquals("Number of ATOs for 941 payment template Law Id - 63", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("63").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("13")))).size());
        assertEquals("Number of ATOs for 941 payment template Law Id - 64", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("64").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("14")))).size());
        assertEquals("Number of ATOs for 941 payment template Law Id - 65", 1,
                agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("65").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("15")))).size());

        //Assert for ERTOAs
        DomainEntitySet<FinancialTransaction> employerTaxOverPaymentsApplied = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Created);
        assertEquals("Number of ERTOAs for 941 payment template", 0,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941)).size());
        assertEquals("Number of ERTOAs for 940 payment template", 0,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940)).size());
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10", 1,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 191, with $5", 1,
                employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")))).size());
        assertEquals("Cancelled ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10", 1,
                FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Cancelled)
                        .find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        assertEquals("Cancelled ER Tax Debit amount", SpcfMoney.ZERO, assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Cancelled, TransactionTypeCode.EmployerTaxDebit)).getFinancialTransactionAmount());
        assertEquals("Created ER Tax Debit amount", SpcfMoney.ZERO, assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Created, TransactionTypeCode.EmployerTaxDebit)).getFinancialTransactionAmount());

        //Adjustment Payroll ERTDb status
        PayrollRun adjustmentPayroll = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Adjustment));
        assertEquals("ER Tax Debit amount", SpcfMoney.ZERO, adjustmentPayroll.getEmployerTaxDebitTransaction().getFinancialTransactionAmount());

        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 15.00),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -92.00),
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 77.00));
    }

}
