package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * User: kpaul
 * Date: Dec 20, 2007
 * Time: 2:48:24 PM
 */
public class TransactionCancelCoreTests {
    private static final SpcfCalendar prSubmitDate =
            SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone());

    private PspPrincipal principal = null;



    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        principal = Application.getCurrentPrincipal();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Check the newly created payroll to ensure it was correctly submitted.
     *
     * @param dto The dto for the newly created payroll.
     */
    private void checkPayrollLoad(PayrollRunDTO dto, SourceSystemCode pSrcSystemCd) {
        // Verify persisted data
        PayrollServices.beginUnitOfWork();

        {
            Company company = Company.findCompany("123272727", pSrcSystemCd);

            // Ensure the first payroll run was created correctly
            PayrollRun payrollRun1 = PayrollRun
                    .findPayrollRun(company, dto.getPayrollTXBatchId());

            assertTrue("PayrollRun Not Null", payrollRun1 != null);

            if (payrollRun1 != null) {
                SpcfMoney payrollRunAmount = new SpcfMoney("0.00");

                assertEquals("PayrollRun Id:", payrollRun1.getSourcePayRunId(), dto.getPayrollTXBatchId());

                // Ensure that Paychecks were created correctly
                assertEquals("Number of Paychecks:", payrollRun1.getPaycheckCollection().size(),
                        dto.getPaychecks().size());

                for (PaycheckDTO paycheckDTO : dto.getPaychecks()) {
                    Paycheck paycheck = Paycheck
                            .findPaycheck(company, paycheckDTO.getPaycheckId());

                    assertTrue("Paycheck Not Null", paycheck != null);

                    if (paycheck != null) {
                        assertEquals("Employee Id:", paycheckDTO.getEmployeeId(),
                                paycheck.getDDEmployee().getSourceEmployeeId());
                        assertEquals("Number of Paycheck Splits:", paycheckDTO.getDdTransactions().size(),
                                paycheck.getPaycheckSplits().size());

                        // Ensure that Paycheck Splits were created correctly
                        for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                            PaycheckSplit paycheckSplit = PaycheckSplit
                                    .findNonCanceledPaycheckSplit(company, ddTransactionDTO.getDDTransactionId());

                            assertTrue(paycheckSplit != null);

                            if (paycheckSplit != null) {
                                payrollRunAmount.add(paycheckSplit.getPaycheckSplitAmount());

                                assertEquals("Paycheck Split Amount:",
                                        SpcfUtils.convertToSpcfMoney(ddTransactionDTO.getDDTransactionAmount()),
                                        paycheckSplit.getPaycheckSplitAmount());
                                assertEquals("EmployeeBankAccount:",
                                        ddTransactionDTO.getEmployeeBankAccount().getEmployeeBankAccountId(),
                                        paycheckSplit.getEmployeeBankAccount().getSourceBankAccountId());

                                // Ensure that Financial Transactions were created correctly
                                DomainEntitySet<FinancialTransaction> financialTransactions = paycheckSplit
                                        .getFinancialTransactions();

                                assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

                                FinancialTransaction financialTransaction = (FinancialTransaction) financialTransactions
                                        .get(0);

                                assertNotNull("MM txn", financialTransaction.getMoneyMovementTransaction());
                                assertEquals("Financial Transaction Amount:", paycheckSplit.getPaycheckSplitAmount(),
                                        financialTransaction.getFinancialTransactionAmount());
                                assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployeeDdCredit,
                                        financialTransaction.getTransactionType().getTransactionTypeCd());

                            }
                        }
                    }
                }
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Create a TransactionCancelDTO for use in the various tests.
     *
     * @param pBatchId    The payroll run id
     * @param pSourcePaycheckIdList A list of source paycheck ids to cancel
     * (if null, cancel all transactions for the given payroll run)
     * @return The new TransactionCancelDTO
     */
    private TransactionCancelEEDTO buildTransactionCancelDTO(String pBatchId, List<String> pSourcePaycheckIdList) {
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();

        //dto.setServiceCd(ServiceCode.DirectDeposit);
        dto.setSourcePayrollRunId(pBatchId);
        //dto.setDdTransactionIdList(ddTxnIdList);
        dto.setSourcePaycheckIdList(pSourcePaycheckIdList);

        return dto;
    }

    /**
     * A utility function to set the payroll run status to OFFLOADED_DEBIT.
     *
     * @param pPayrollRun The payroll run on which to set the status.
     */
    private void setPayrollStatusToOffloadedDebit(PayrollRun pPayrollRun) {
        DomainEntitySet<FinancialTransaction> txnList =
                Application.find(FinancialTransaction.class,
                        FinancialTransaction.PayrollRun().equalTo(pPayrollRun)
                                .And(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerDdDebit))));

        if (txnList != null) {
            txnList.get(0).updateFinancialTransactionState(
                    TransactionStateCode.Executed);
        }

        pPayrollRun.setPayrollRunStatus(PayrollStatus.OffloadedDebit);

        Application.save(pPayrollRun);
    }

    /**
     * Load two payrolls so that we can change one and leave the other intact.
     */
    @Test
    public void loadTestPayrollData() {
        loadTestPayrollData(SourceSystemCode.QBOE);
    }

    private void loadTestPayrollData(SourceSystemCode pSrcSystemCd) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        PayrollServices.commitUnitOfWork();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(pSrcSystemCd);

        PayrollServices.beginUnitOfWork();
        Collection<PayrollRunDTO> payrollRunDTOCollection = psdl.loadMultiplePayrollsWithMultiplePaycheckSplitsForCompany123272727(3);
        PayrollServices.commitUnitOfWork();

        for (PayrollRunDTO dto : payrollRunDTOCollection) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                    .submitPayroll(pSrcSystemCd, "123272727", dto);

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
            assertEquals("BillingDetails Size ",1,payrollRun.getBillingDetailCollection().size());

            Offering offering = Offering.findOffering(processResult.getResult().getCompany(), ServiceCode.DirectDeposit);
            OfferingServiceChargeGroup offSvcChargeGrp = OfferingServiceChargeGroup.findOfferingServiceChargeGroup(offering, OfferingServiceChargeType.DirectDepositFee);
            OfferingServiceCharge offServiceCharge = offSvcChargeGrp.selectTier(
                    processResult.getResult().getPaycheckCollection().size());

            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            for(BillingDetail billingDetail : payrollRun.getBillingDetailCollection()){
                //Assertion for the Offering Service Charge Type
                assertEquals("Offering Service Charge Type ", OfferingServiceChargeType.DirectDepositFee,
                        billingDetail.getOfferingServiceChargeType());

                //Assertion for the Item SKU.
                assertEquals("Item SKU " , offServiceCharge.getSKU(), billingDetail.getItemSku());
            }

            assertTrue("Process Result", processResult.isSuccess());
            PayrollServices.commitUnitOfWork();

            checkPayrollLoad(dto, pSrcSystemCd);
        }
    }

    /**
     * Test to ensure the core process will fail on invalid company id.
     */
    @Test
    public void testInvalidCompanyId() {
        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        {
            // invalid company id (null)
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, null, dto));

            // invalid company id (too short - valid range = 1..50)
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "", dto));

            // invalid company id (too long - valid range = 1..50)
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(
                    SourceSystemCode.QBOE, "123456789012345678901234567890123456789012345678901", dto));

            // invalid company id (does not exist)
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "INVALID", dto));
        }
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 4, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "138", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "138", message2.getMessageCode());
        Message message3 = processResult.getMessages().get(2);
        assertEquals("Error Code", "138", message3.getMessageCode());
        Message message4 = processResult.getMessages().get(3);
        assertEquals("Error Code", "169", message4.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText2, message2.getMessage());
        String messageText3 = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText3, message3.getMessage());
        String messageText4 = "Company QBOE:INVALID does not exist.";
        assertEquals("Error Message", messageText4, message4.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid service code.
     */
/*    @Test
    public void testInvalidServiceCd() {
        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        // invalid service code (null)
        //dto.setServiceCd(null);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "118", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Service Code is not specified.";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }*/

    /**
     * Test to ensure the core process will fail on invalid dto (null).
     */
    @Test
    public void testNullDto() {
        ProcessResult processResult = new ProcessResult();

        PayrollServices.beginUnitOfWork();
        // invalid dto (null)
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", null));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "5002", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Required 'TransactionCancelEEDTO' input is missing or blank";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid payroll run (DD Transaction Batch) id.
     */
    @Test
    public void testInvalidDdTxBatchId() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        {
            // invalid dd transaction batch id (null)
            dto.setSourcePayrollRunId(null);
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));

            // invalid dd transaction batch id (too short - valid range = 1..50)
            dto.setSourcePayrollRunId("");
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));

            // invalid dd transaction batch id (too long - valid range = 1..50)
            dto.setSourcePayrollRunId("123456789012345678901234567890123456789012345678901");
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));

            // invalid dd transaction batch id (does not exist)
            dto.setSourcePayrollRunId("INVALID");
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        }
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 4, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "130", message1.getMessageCode());
        Message message2 = processResult.getMessages().get(1);
        assertEquals("Error Code", "130", message2.getMessageCode());
        Message message3 = processResult.getMessages().get(2);
        assertEquals("Error Code", "130", message3.getMessageCode());
        Message message4 = processResult.getMessages().get(3);
        assertEquals("Error Code", "194", message4.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Source Payroll Run ID is not specified.";
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Source Payroll Run ID is not specified.";
        assertEquals("Error Message", messageText2, message2.getMessage());
        String messageText3 = "Source Payroll Run ID is not specified.";
        assertEquals("Error Message", messageText3, message3.getMessage());
        String messageText4 = "Payroll Run with DDTxBatchID INVALID does not exist for company QBOE:123272727.";
        assertEquals("Error Message", messageText4, message4.getMessage());
    }

    /**
     * Test to ensure the core process will fail if the payroll status is not in a valid state to be cancelled.
     *
     * @param pPayrollStatus The invalid payroll status code to test.
     */
    private void testInvalidPayrollStatus(PayrollStatus pPayrollStatus) {
        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);
        dto.setAgentCancel(true);

        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

            // set payroll run status to given (invalid) state
            payrollRun.setPayrollRunStatus(pPayrollStatus);

            Application.save(payrollRun);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error code
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "1048", message1.getMessageCode());

        // Verify that the correct message string was returned
        String messageText1 = "Action DDTransactionCancel is not valid for payroll run with DDTxBatchID BatchId01, which has status of " + pPayrollStatus
                .toString() + ".";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure the core process will fail on invalid payroll status. Only payrolls in a state of PENDING or
     * OFFLOADED_DEBIT are eligible for having any of their financial transactions cancelled.
     */
    @Test
    public void testInvalidPayrollStatus() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        testInvalidPayrollStatus(PayrollStatus.Complete);
        //   testInvalidPayrollStatus(PayrollStatus.Canceled);
        testInvalidPayrollStatus(PayrollStatus.DebitReturnedCanceled);
        testInvalidPayrollStatus(PayrollStatus.DebitReturned);
        testInvalidPayrollStatus(PayrollStatus.ReturnedTwice);
        testInvalidPayrollStatus(PayrollStatus.AutoRedebitOffloaded);
        testInvalidPayrollStatus(PayrollStatus.PendingAutoRedebit);
        testInvalidPayrollStatus(PayrollStatus.NSFCanceled);
        testInvalidPayrollStatus(PayrollStatus.OffloadedAll);
        testInvalidPayrollStatus(PayrollStatus.PendingRedebit);
        testInvalidPayrollStatus(PayrollStatus.RedebitOffloaded);
        testInvalidPayrollStatus(PayrollStatus.WrittenOff);

        // Don't test these status codes since they're valid
        //testInvalidPayrollStatus(PayrollStatus.OffloadedDebit);
        //testInvalidPayrollStatus(PayrollStatus.Pending);
    }

    /**
     * Test to ensure the core process will fail on invalid source transaction id.
     */
    @Test
    public void testInvalidSourceTransactionId() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        String txn1, txn2, txn3;
        List<String> sourcePaycheckIdList = new Vector<String>(3);
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", sourcePaycheckIdList);

        ///////////////////////////////////////////////////////////////////////
        // Test source transaction id(s) that are structurally invalid
        // (this will fail validation at the dto)
        ///////////////////////////////////////////////////////////////////////
        ProcessResult processResult1 = new ProcessResult();

        // invalid source paycheck id (null)
        txn1 = null;
        sourcePaycheckIdList.add(txn1);

        // invalid source paycheck id (empty)
        txn2 = "";
        sourcePaycheckIdList.add(txn2);

        // invalid source paycheck id (too long)
        txn3 = "123456789012345678901234567890123456789012345678901";
        sourcePaycheckIdList.add(txn3);

        PayrollServices.beginUnitOfWork();
        processResult1.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 3, processResult1.getMessages().size());

        // validate error codes
        Message message1 = processResult1.getMessages().get(0);
        assertEquals("Error Code", "11", message1.getMessageCode());
        Message message2 = processResult1.getMessages().get(1);
        assertEquals("Error Code", "11", message2.getMessageCode());
        Message message3 = processResult1.getMessages().get(2);
        assertEquals("Error Code", "11", message3.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Invalid argument: " + txn1;
        assertEquals("Error Message", messageText1, message1.getMessage());
        String messageText2 = "Invalid argument: " + txn2;
        assertEquals("Error Message", messageText2, message2.getMessage());
        String messageText3 = "Invalid argument: " + txn3;
        assertEquals("Error Message", messageText3, message3.getMessage());

        ///////////////////////////////////////////////////////////
        // Test source paycheck id(s) that do not exist
        // (this will fail validation within the core process)
        ///////////////////////////////////////////////////////////
        ProcessResult processResult2 = new ProcessResult();

        sourcePaycheckIdList.clear();

        // invalid source paycheck id (does not exist)
        txn1 = "INVALID1";
        sourcePaycheckIdList.add(txn1);

        // invalid source paycheck id (does not exist)
        txn2 = "INVALID2";
        sourcePaycheckIdList.add(txn2);

        // invalid source paycheck id (does not exist)
        txn3 = "INVALID3";
        sourcePaycheckIdList.add(txn3);

        PayrollServices.beginUnitOfWork();
        processResult2.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 3, processResult2.getMessages().size());

        // validate error codes
        message1 = processResult2.getMessages().get(0);
        assertEquals("Error Code", "299", message1.getMessageCode());
        message2 = processResult2.getMessages().get(1);
        assertEquals("Error Code", "299", message2.getMessageCode());
        message3 = processResult2.getMessages().get(2);
        assertEquals("Error Code", "299", message3.getMessageCode());

        // Verify that the correct message strings were returned
        messageText1 = "Paycheck " + txn1 + " for company QBOE:123272727 does not exist.";
        assertEquals("Error Message", messageText1, message1.getMessage());
        messageText2 = "Paycheck " + txn2 + " for company QBOE:123272727 does not exist.";
        assertEquals("Error Message", messageText2, message2.getMessage());
        messageText3 = "Paycheck " + txn3 + " for company QBOE:123272727 does not exist.";
        assertEquals("Error Message", messageText3, message3.getMessage());
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
        for (ListIterator<PaycheckSplit> splitIter = pSplitList.listIterator(1); splitIter.hasNext();) {
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

    /**
     * Test to ensure the core process will fail if there is an attempt to cancel one transaction on a paycheck with
     * multiple paycheck splits.
     */
    //@Test
    public void testCancellationOfOneTransactionOnPaycheckWithTwoSplits() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        ProcessResult expectedProcessResult = null;
        List<String> dtoTxnList = new Vector<String>(3);
        List<PaycheckSplit> splitList = new Vector<PaycheckSplit>(2);
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", dtoTxnList);
        Employee employee = null;

        // get one of the source transaction id's from a paycheck split so we can attempt to cancel it.
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

            // retrieve the first paycheck in the payroll run that has multiple splits.
            for (Paycheck check : payrollRun.getPaycheckCollection()) {
                if (check.getPaycheckSplitCollection().size() > 1) {
                    // save the employee so we can match up the error text.
                    employee = check.getDDEmployee();

                    // save the splits so we can attempt to cancel one of them and match the other(s) to the error text.
                    for (Iterator<PaycheckSplit> splitIter = check.getPaycheckSplitCollection()
                            .iterator(); splitIter.hasNext();) {
                        splitList.add(splitIter.next());
                    }

                    // we have the data we need, so break.
                    break;
                }
            }

            // we need the employee to check the results
            assertNotNull("Employee does not have multiple splits", employee);

            // we need valid paycheck splits to perform the test
            assertTrue("Paycheck split list is empty", !splitList.isEmpty());

            // use the first transaction in the list for the test
            dtoTxnList.add(splitList.get(0).getSourceDdTxnId());

            // build the expected process result for the failure we're testing
            // (we need to build this because the data changes from test-to-test, so we need to
            //  construct a process result error that will match the error from the core process.)
            expectedProcessResult = buildExpectedProcessResult(employee, splitList);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message expectedMessage = expectedProcessResult.getMessages().get(0);
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code", expectedMessage.getMessageCode(), message.getMessageCode());

        // Verify that the correct message was returned
        assertEquals("Error Message", expectedMessage.getMessage(), message.getMessage());

        System.out.println("Message " + message.getMessage());
    }

    /**
     * Test to ensure the core process will fail if it is past the offload date for the company
     * (this test is influenced by the company's funding model - 2 day or 5 day)
     */
    @Test
    public void testAfterOffloadDate() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);
        SpcfCalendar limitCalendar;

        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            SpcfCalendar checkDepositDate = payrollRun.getPaycheckDate().toLocal();
            int fundingModelDays = company.getFundingModel().getNumberOfFundingDays();

            limitCalendar = company.getOffloadGroup().getCalendarForCutoffTime(checkDepositDate);

            // adjust the limit calendar by the company's funding model setting
            CalendarUtils.addBusinessDays(limitCalendar, -1 * fundingModelDays);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // move the psp time to one day past the offload limit calendar
        CalendarUtils.addBusinessDays(limitCalendar, 1);
        PSPDate.setPSPTime(limitCalendar);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "1015", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Transactions for DDTxBatch BatchId01 for company QBOE:123272727 have already been sent to the bank and cannot be recalled.";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure the core process will fail if the employer dd debit financial transaction is in state
     * Completed for a payroll run that is in Completed state.
     */
    @Test
    public void testInvalidEmployerDdDebitTransactionForCompletedPayroll() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);
        dto.setAgentCancel(true);


        {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                    null);
            PayrollServices.commitUnitOfWork();

            assertNotNull("Transaction list is null", txnList);
            assertTrue("Transaction list is empty", !txnList.isEmpty());

            // offload ER Debit
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20070925000000");
            PayrollServices.commitUnitOfWork();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20071002000000");
            PayrollServices.commitUnitOfWork();

            // Complete ER Debit
            ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
            PayrollServices.beginUnitOfWork();
            processACHTxns.process("20071002");
            PayrollServices.commitUnitOfWork();
        }


        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
//        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();


        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "1048", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Action DDTransactionCancel is not valid for payroll run with DDTxBatchID BatchId01, which has status of Complete.";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure the core process will fail if *any* of the employee dd financial transactions are in any other
     * state than CREATED. This test requires the caller to pass in a list of explicit transactions to cancel
     * (otherwise the core process will automatically select only valid transaction to cancel.)
     */
    @Test
    public void testSingleInvalidEmployeeDDTransaction() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", sourcePaycheckIdList);
        String txnId = null;

        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(
                            payrollRun,
                            TransactionStateCode.Created);

            assertNotNull("Transaction list is null", txnList);
            assertTrue("Transaction list is empty", !txnList.isEmpty());

            // set one txn to an invalid state of COMPLETED
            txnId = txnList.get(0).getPaycheckSplit().getSourceDdTxnId();
            txnList.get(0).updateFinancialTransactionState(
                    TransactionStateCode.Completed);
            // add all txn's to the list of txns to be cancelled.
            sourcePaycheckIdList.add(txnList.get(0).getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors", 1, processResult.getMessages().size());

        // validate error codes
        Message message1 = processResult.getMessages().get(0);
        assertEquals("Error Code", "258", message1.getMessageCode());

        // Verify that the correct message strings were returned
        String messageText1 = "Transaction " + txnId + " cannot be canceled, because it is no longer pending.";
        assertEquals("Error Message", messageText1, message1.getMessage());
    }

    /**
     * Test to ensure that no refund is given for a company with their status in an invalid state.
     *
     * @param pServiceSubStatusCd The invalid service status to check.
     */
    private void testInvalidCompanyStatusForOffloadedDebitPayroll(ServiceSubStatusCode pServiceSubStatusCd) {
        PayrollServicesTest.truncateTables();
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

            // set the payroll status to OFFLOADED_DEBIT
            setPayrollStatusToOffloadedDebit(payrollRun);

            // set the service status to the given invalid state for cancelling financial transactions
            ServiceSubStatus serviceSubStatus = Application.findById(ServiceSubStatus.class, pServiceSubStatusCd);
            if (serviceSubStatus.getServiceStatus().getServiceStatusCd() == ServiceStatusCode.OnHold) {
                PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), pServiceSubStatusCd);
            }
            else {
                CompanyService companyService = CompanyService.findCompanyService(company, ServiceCode.DirectDeposit);
                companyService.updateCompanyServiceStatus(pServiceSubStatusCd);
            }

            Application.save(company);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;

            // retrieve all financial transactions for all paycheck splits
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(payrollRun, null);

            // verify all appropriate paycheck split transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertEquals("Transaction State",
                        TransactionStateCode.Cancelled,
                        txn.getCurrentTransactionState().getTransactionStateCd());

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);
                assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());
            }

            // verify the payroll run status was changed to CANCELLED.
            assertEquals("Payroll Run Status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());

            // attempt to retrieve the employer refund transaction (there shouldn't be one...)
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Employer DD Refund Credit transaction count", 0, txnList.size());
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to ensure that no refund is given for a company with their status in an invalid state.
     */
    @Ignore // Transactions cannot be cancelled when company is not active because company cannot become inactive until all transactions are cancelled
    @Test
    public void testAttemptedCancellationOnInvalidCompanyStatusForOffloadedDebitPayroll() {
        // Test Inactive company status
        testInvalidCompanyStatusForOffloadedDebitPayroll(ServiceSubStatusCode.Cancelled);

        // Test OnHold company status
        testInvalidCompanyStatusForOffloadedDebitPayroll(ServiceSubStatusCode.Fraud);

        // Test PendingTermination company status
        testInvalidCompanyStatusForOffloadedDebitPayroll(ServiceSubStatusCode.PendingTermination);

        // Test Terminated company status
        testInvalidCompanyStatusForOffloadedDebitPayroll(ServiceSubStatusCode.Terminated);

        // Do not test Active status since it should work (this case is tested later)
        //testInvalidCompanyStatusForOffloadedDebitPayroll(ServiceStatusCode.Active);
    }

    /**
     * Test to ensure that no refund is given for a company with their service status in an invalid state.
     *
     * @param pServiceSubStatusCd    The invalid service status to check.
     * @param pExpectedTxnCount The number of expected refund transactions that should be created.
     */
    private void testServiceStatusForOffloadedDebitPayroll(ServiceSubStatusCode pServiceSubStatusCd, int pExpectedTxnCount) {
        PayrollServicesTest.truncateTables();
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DDCompanyServiceInfo companyService = (DDCompanyServiceInfo) CompanyService.
                    findCompanyService(company, ServiceCode.DirectDeposit);

            // set the payroll status to OFFLOADED_DEBIT
            setPayrollStatusToOffloadedDebit(payrollRun);

            // set the service status to the given invalid state for cancelling financial transactions
            companyService.updateCompanyServiceStatus(pServiceSubStatusCd);

            Application.save(companyService);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;

            // retrieve all financial transactions for all paycheck splits
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(payrollRun, null);

            // verify all appropriate paycheck split transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertEquals("Transaction State",
                        TransactionStateCode.Cancelled,
                        txn.getCurrentTransactionState().getTransactionStateCd());

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);
                assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());
            }

            // verify the payroll run status was changed to CANCELLED.
            assertEquals("Payroll Run Status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());

            // attempt to retrieve the employer refund transaction
            // (there shouldn't be one unless ServiceSubStatus is Suspended)
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Employer DD Refund Credit transaction count", pExpectedTxnCount, txnList.size());

            if (pExpectedTxnCount > 0) {
                // verify the transaction response was created correctly for the Employer DD Refund Credit transaction
                txnResponseList = TransactionResponse
                        .findTransactionResponses(txnList.get(0));
                assertEquals("Transaction response for ER refund transaction", 1, txnResponseList.size());
            }
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to ensure that no refund is given for a company with their service status in an invalid state.
     */
    @Ignore // Transactions cannot be cancelled when the service is not active because the service cannot become inactive until all transactions are cancelled
    @Test
    public void testAttemptedCancellationOnInvalidServiceStatusForOffloadedDebitPayroll() {
        // Test Inactive service status
        testServiceStatusForOffloadedDebitPayroll(ServiceSubStatusCode.Cancelled, 0);

        // Test PendingActivation service status
        testServiceStatusForOffloadedDebitPayroll(ServiceSubStatusCode.Fraud, 0);

        // Test PendingTermination service status
        testServiceStatusForOffloadedDebitPayroll(ServiceSubStatusCode.PendingTermination, 0);

        // Test Terminated service status
        testServiceStatusForOffloadedDebitPayroll(ServiceSubStatusCode.Terminated, 0);

        // Test Suspended service status (this should create a valid refund transaction)
        testServiceStatusForOffloadedDebitPayroll(ServiceSubStatusCode.SuspendedDirectDeposit, 1);

        // Do not test Active service status since it should work (this case is implicitly tested later)
        //testServiceStatusForOffloadedDebitPayroll(ServiceSubStatus.Active);
    }

    /**
     * Test to ensure that no refund is given for a company with their bank account status in an invalid state.
     * @param pBankAccountStatus The invalid bank account status to check.
     */
    private void testInvalidBankAccountStatusForOffloadedDebitPayroll(BankAccountStatus pBankAccountStatus) {
        runBeforeEachTest();
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);
        dto.setAgentCancel(true);

        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            CompanyBankAccount companyBankAccount =
                    payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

            // set the payroll status to OFFLOADED_DEBIT
            setPayrollStatusToOffloadedDebit(payrollRun);

            // set the bank account status to the given invalid state for cancelling financial transactions
            companyBankAccount.updateBankAccountStatus(pBankAccountStatus);
            //PayrollServices.companyManager.deactivateCompanyBankAccount();

            Application.save(companyBankAccount);

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            fail("Failed to update company bank account status. Exception message is " + e.getMessage());
        }


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();


        // validate cancel result
        assertSuccess(processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;

            // retrieve all financial transactions for all paycheck splits
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(payrollRun, null);

            // verify all appropriate paycheck split transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertEquals("Transaction State",
                        TransactionStateCode.Cancelled,
                        txn.getCurrentTransactionState().getTransactionStateCd());

                // verify a transaction response exists for this transaction
                txnResponseList = TransactionResponse.findTransactionResponses(txn);
                assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());
            }

            // verify the payroll run status was set to Complete.
            assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

            Collection<TransactionTypeCode> types = new Vector<TransactionTypeCode>();
            Collection<TransactionStateCode> states = new Vector<TransactionStateCode>();

            types.add(TransactionTypeCode.EmployerDdRefundCredit);
            states.add(TransactionStateCode.Created);

            // attempt to retrieve the employer refund transaction (there shouldn't be one...)
            txnList = FinancialTransaction.findFinancialTransactionsForPayrollByTypeAndState(
                    payrollRun, types, states);

            assertEquals("Employer DD Refund Credit transaction count", 0, txnList.size());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        runAfterEachTest();
    }

    /**
     * Test to ensure that no refund is given for a company with their service status in an invalid state.
     */
    @Test
    public void testAttemptedCancellationOnInvalidBankAccountStatusForOffloadedDebitPayroll() {
        // Test Inactive bank account status
        testInvalidBankAccountStatusForOffloadedDebitPayroll(BankAccountStatus.Inactive);

        // Test PendingVerification bank account status
        testInvalidBankAccountStatusForOffloadedDebitPayroll(BankAccountStatus.PendingVerification);

        // Do not test Active status since it should work (this case is implicitly tested later)
        //testInvalidBankAccountStatusForOffloadedDebitPayroll(BankAccountStatus.Active);
    }

    /**
     * Verify that a request to cancel all transactions for a PENDING payroll will succeed. This test does not pass an
     * explicit list of transactions to cancel; as a result the core process selects all CREATED paycheck split
     * transactions for the payroll run.
     */
    @Test
    public void testImplicitCancellationOfAllTransactionsForPendingPayroll() {
        loadTestPayrollData(SourceSystemCode.QBDT);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();

        saveBillingDetails(SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;

            // Select all txns for the payroll run (both EE and ER)
            com.intuit.sbd.payroll.psp.DomainEntitySet<FinancialTransaction> txnList = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

            // verify all transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertEquals("Transaction State",
                        TransactionStateCode.Cancelled,
                        txn.getCurrentTransactionState().getTransactionStateCd());

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);

                //If condtion added to check for DdServiceFee & ServiceSalesAndUseTax TransactionTypes. Since we are
                //not creating transaction response for these two transactions no need to assert for TransactionResposne.
                if (!txn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerFeeDebit) &&
                        !txn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.ServiceSalesAndUseTax)) {
                    assertEquals("Transaction response for cancelled transaction", 1, txnResponseList.size());
                }

                //Verify there are not any mm txns for this fin txn
                assertNull("MM txn for cancelled fin txn", txn.getMoneyMovementTransaction());
            }

            // verify the payroll run status was changed to CANCELLED.
            assertEquals("Payroll Run Status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());

            //Assertion for DdServiceFee transaction was Cancelled
            DomainEntitySet<FinancialTransaction> feeTxnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                    new TransactionStateCode[]{TransactionStateCode.Cancelled});

            assertEquals("Number of DdServiceFee Cancelled txns", 1, feeTxnList.size());

            //Assertions for Billing Details.
            checkBillingDetails(payrollRun, 0);
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to cancel all transactions for a PENDING payroll will succeed. This test will pass an
     * explicit list of transactions to cancel that includes all paycheck split transactions for the given payroll run.
     */
    @Test
    public void testExplicitCancellationOfAllTransactionsForPendingPayroll() {
        loadTestPayrollData(SourceSystemCode.QBDT);

        ProcessResult processResult = new ProcessResult();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", sourcePaycheckIdList);

        // collect all paycheck split transactions for the payroll and pass this into the dto for cancellation.
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(
                            payrollRun,
                            TransactionStateCode.Created);

            assertNotNull("Transaction list is null", txnList);
            assertTrue("Transaction list is empty", !txnList.isEmpty());

            // add all source paycheck's to the list of txns to be cancelled.
            for (FinancialTransaction txn : txnList) {
                if(!sourcePaycheckIdList.contains(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId())){
                    sourcePaycheckIdList.add(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
                }
            }
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, "123272727", dto));
        PSPDate.resetPSPTime();

        //Save the updated billing details by calling BillingManager.save() method
        saveBillingDetails(SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;

            // Select all txns for the payroll run (both EE and ER)
            com.intuit.sbd.payroll.psp.DomainEntitySet<FinancialTransaction> txnList = payrollRun.getFinancialTransactionCollection();

            // verify all transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertEquals("Transaction State",
                        TransactionStateCode.Cancelled,
                        txn.getCurrentTransactionState().getTransactionStateCd());

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);

                //If condtion added to check for DdServiceFee & ServiceSalesAndUseTax TransactionTypes. Since we are
                //not creating transaction response for these two transactions no need to assert for TransactionResposne.
                if (!txn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerFeeDebit) &&
                        !txn.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.ServiceSalesAndUseTax)) {
                    assertEquals("Transaction response for cancelled transaction", 1, txnResponseList.size());
                }

                //Verify there are not any mm txns for this fin txn
                assertNull("MM txn for cancelled fin txn", txn.getMoneyMovementTransaction());
            }

            // verify the payroll run status was changed to CANCELLED.
            assertEquals("Payroll Run Status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());

            //Assertion for DdServiceFee transaction was Cancelled
            DomainEntitySet<FinancialTransaction> feeTxnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                    new TransactionStateCode[]{TransactionStateCode.Cancelled});

            assertEquals("Number of DdServiceFee Cancelled txns", 1, feeTxnList.size());

            //Assertions for updated Billing Details.
            checkBillingDetails(payrollRun, 0);
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to cancel all transactions for an OFFLOADED_DEBIT payroll will succeed. This test does not
     * pass an explicit list of transactions to cancel; as a result the core process selects all CREATED paycheck split
     * transactions for the payroll run.
     */
    @Test
    public void testImplicitCancellationOfAllTransactionsForOffloadedDebitPayroll() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", null);
        dto.setAgentCancel(true);

        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

            // set the payroll status to OFFLOADED_DEBIT
            setPayrollStatusToOffloadedDebit(payrollRun);
        }
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;

            // retrieve all financial transactions for all paycheck splits
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(payrollRun, null);

            // verify all appropriate paycheck split transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertEquals("Transaction State",
                        TransactionStateCode.Cancelled,
                        txn.getCurrentTransactionState().getTransactionStateCd());

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);
                assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());
                //Verify there are not any mm txns for this fin txn
                assertNull("MM txn for cancelled fin txn", txn.getMoneyMovementTransaction());

            }

            // verify the payroll run status was changed to Complete.
            assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

            // verify an ER_DD_REFUND_CREDIT transaction was created and is correct in all respects
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Employer DD Refund Credit transaction count", 1, txnList.size());

            // verify the new refund txn was correctly associated with the original debit txn
            FinancialTransaction refundTxn = txnList.get(0);
            FinancialTransaction originalTxn = refundTxn.getOriginalTransaction();
            assertNotNull("Original Employer DD Debit transaction", originalTxn);

            // verify the credit account is from the original txn debit account
            assertEquals("Refund txn credit account",
                    originalTxn.getDebitBankAccount().getId(),
                    refundTxn.getCreditBankAccount().getId());

            // verify the debit account is intuit
            IntuitBankAccount intuitRefundDebitBankAccount = IntuitBankAccount
                    .findIntuitBankAccount(TransactionTypeCode.EmployerDdRefundCredit, CreditDebitCode.Debit);
            assertEquals("Refund txn debit account",
                    intuitRefundDebitBankAccount.getBankAccount().getId(),
                    refundTxn.getDebitBankAccount().getId());

            // verify the refund amount is equal to the original debit amount (since the entire payroll was cancelled)
            assertEquals("Refund txn amount",
                    originalTxn.getFinancialTransactionAmount(),
                    refundTxn.getFinancialTransactionAmount());

            // verify the refund txn settlement type is ACH
            assertEquals("Refund txn settlement type",
                    SettlementType.ACH,
                    refundTxn.getSettlementTypeCd());

            // verify the settlement date for the refund txn
            int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);

            SpcfCalendar expectedSettlementDate = originalTxn.getSettlementDate().toLocal().copy();
            expectedSettlementDate = expectedSettlementDate.toLocal();
            CalendarUtils.addBusinessDays(expectedSettlementDate, achWaitPeriodDays + 1);

            SpcfCalendar txnSettlementDate = refundTxn.getSettlementDate().toLocal().copy();

            assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

            // verify the transaction response was created correctly for the Employer DD Refund Credit transaction
            txnResponseList = TransactionResponse.findTransactionResponses(refundTxn);
            assertEquals("Transaction response for ER refund transaction", 1, txnResponseList.size());

            //Verify the mm txn and entry detail records
            MoneyMovementTransaction originalMMTxn = originalTxn.getMoneyMovementTransaction();
            assertNotNull(originalMMTxn);
            MoneyMovementTransaction mmTxn = refundTxn.getMoneyMovementTransaction();
            assertNotNull(mmTxn);
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to cancel all transactions for an OFFLOADED_DEBIT payroll will succeed. This test will
     * pass an explicit list of transactions to cancel that includes all paycheck split transactions for the given
     * payroll run.
     */
    @Test
    public void testExplicitCancellationOfAllTransactionsForOffloadedDebitPayroll() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", sourcePaycheckIdList);
        dto.setAgentCancel(true);

        // collect all paycheck split transactions for the payroll and pass this into the dto for cancellation.
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(
                            payrollRun,
                            TransactionStateCode.Created);

            assertNotNull("Transaction list is null", txnList);
            assertTrue("Transaction list is empty", !txnList.isEmpty());

            // add all source paycheck's to the list of txns to be cancelled.
            for (FinancialTransaction txn : txnList) {
                if(!sourcePaycheckIdList.contains(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId())){
                    sourcePaycheckIdList.add(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId());
                }
            }

            // set the payroll status to OFFLOADED_DEBIT
            setPayrollStatusToOffloadedDebit(payrollRun);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;

            // retrieve all financial transactions for all paycheck splits
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(payrollRun, null);

            // verify all appropriate paycheck split transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertEquals("Transaction State",
                        txn.getCurrentTransactionState().getTransactionStateCd(),
                        TransactionStateCode.Cancelled);

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);
                assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());

                assertNull("MM txn for cancelled fin txn", txn.getMoneyMovementTransaction());
            }

            // verify the payroll run status was changed to Complete.
            assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

            // verify an ER_DD_REFUND_CREDIT transaction was created and is correct in all respects
            txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Employer DD Refund Credit transaction count", 1, txnList.size());

            // verify the new refund txn was correctly associated with the original debit txn
            FinancialTransaction refundTxn = txnList.get(0);
            FinancialTransaction originalTxn = refundTxn.getOriginalTransaction();
            assertNotNull("Original Employer DD Debit transaction", originalTxn);

            // verify the credit account is from the original txn debit account
            assertEquals("Refund txn credit account",
                    originalTxn.getDebitBankAccount().getId(),
                    refundTxn.getCreditBankAccount().getId());

            // verify the debit account is intuit
            IntuitBankAccount intuitRefundDebitBankAccount = IntuitBankAccount
                    .findIntuitBankAccount(TransactionTypeCode.EmployerDdRefundCredit, CreditDebitCode.Debit);
            assertEquals("Refund txn debit account",
                    intuitRefundDebitBankAccount.getBankAccount().getId(),
                    refundTxn.getDebitBankAccount().getId());

            // verify the refund amount is equal to the original debit amount (since the entire payroll was cancelled)
            assertEquals("Refund txn amount",
                    originalTxn.getFinancialTransactionAmount(),
                    refundTxn.getFinancialTransactionAmount());

            // verify the refund txn settlement type is ACH
            assertEquals("Refund txn settlement type",
                    SettlementType.ACH,
                    refundTxn.getSettlementTypeCd());

            // verify the settlement date for the refund txn
            int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);

            SpcfCalendar expectedSettlementDate = originalTxn.getSettlementDate().toLocal().copy();
            expectedSettlementDate = expectedSettlementDate.toLocal();
            CalendarUtils.addBusinessDays(expectedSettlementDate, achWaitPeriodDays + 1);

            SpcfCalendar txnSettlementDate = refundTxn.getSettlementDate().toLocal().copy();

            SpcfCalendar expectedInitDate = SpcfCalendar.createInstance(2007, SpcfCalendar.October, 2, 0, 0, 0, 0,
                    SpcfTimeZone.getLocalTimeZone());

            assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

            // verify the transaction response was created correctly for the Employer DD Refund Credit transaction
            txnResponseList = TransactionResponse.findTransactionResponses(refundTxn);
            assertEquals("Transaction response for ER refund transaction", 1, txnResponseList.size());

            //Verify the mm txn and entry detail records
            MoneyMovementTransaction originalMMTxn = originalTxn.getMoneyMovementTransaction();
            assertNotNull(originalMMTxn);
            MoneyMovementTransaction mmTxn = refundTxn.getMoneyMovementTransaction();

            PayrollSubmitCoreTests.validateNewMMTxn(mmTxn, refundTxn.getFinancialTransactionAmount(),
                    expectedSettlementDate, expectedInitDate, refundTxn);

            EntryDetailRecord entryDetailRecord = mmTxn.getEntryDetailRecordCollection().iterator().next();
            String currencyString = StringFormatter
                    .formatCurrencyNoDecimalPoint(
                            SpcfUtils.convertToBigDecimal(refundTxn.getFinancialTransactionAmount()), 10);
            String strExpectedRecordData = "622" + originalTxn.getDebitBankAccount()
                    .getRoutingNumber() + StringFormatter
                    .formatString(originalTxn.getDebitBankAccount().getAccountNumber(),
                            17) + currencyString + StringFormatter.formatString(company
                    .getFedTaxId(), 15) + StringFormatter.formatString(company.getLegalName(), 22) + "  0";
            if (entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Credit)) {
                PayrollSubmitCoreTests.validateNewEntryDetailRecord(entryDetailRecord,
                        refundTxn.getFinancialTransactionAmount(), refundTxn.getCompany(), null,
                        mmTxn, strExpectedRecordData);
            } else {
                PayrollSubmitCoreTests.validateNewEntryDetailRecord(entryDetailRecord,
                        refundTxn.getFinancialTransactionAmount(), refundTxn.getCompany(), intuitRefundDebitBankAccount,
                        mmTxn, null);
            }

        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to cancel select transactions for a PENDING payroll will succeed. This test will pass an
     * explicit list of transactions to cancel that are a subset of existing paycheck split transactions for the
     * payroll run.
     */
    @Test
    public void testCancellationOfSelectTransactionsForPendingPayroll() {
            loadTestPayrollData(SourceSystemCode.QBDT);

            ProcessResult processResult = new ProcessResult();
            List<String> sourcePaycheckIdList = new Vector<String>(4);
            BigDecimal newPayrollNetAmt = null;
            TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", sourcePaycheckIdList);
            dto.setAgentCancel(true);

            // collect paycheck split transactions for the first employee and pass this into the dto for cancellation.
            PayrollServices.beginUnitOfWork();
            {
                BigDecimal cancelTxnAmt = new BigDecimal(0);
                Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
                PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
                DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();

                // get the source dd transaction id's for the first paycheck that has multiple splits
                for (Iterator<Paycheck> checkIter = paycheckList.iterator(); checkIter.hasNext() && sourcePaycheckIdList
                        .isEmpty();) {
                    Paycheck check = checkIter.next();
                    DomainEntitySet<PaycheckSplit> splitList = check.getPaycheckSplitCollection();

                    if ((splitList != null) && (splitList.size() > 1)) {
                        sourcePaycheckIdList.add(splitList.get(0).getPaycheck().getSourcePaycheckId());

                        for (PaycheckSplit split : splitList) {
                            cancelTxnAmt = cancelTxnAmt.add(SpcfUtils.convertToBigDecimal(
                                    split.getFinancialTransaction().getFinancialTransactionAmount()));
                        }
                    }
                }

                newPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
                newPayrollNetAmt = newPayrollNetAmt.subtract(cancelTxnAmt);
            }
            PayrollServices.commitUnitOfWork();

            //Set the PSP time to update the price
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 14, SpcfTimeZone.getLocalTimeZone()));
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
            PSPDate.setPSPTime(prSubmitDate);
            dto.setTransmissionId(sourceSystemTransmission.getTransmissionIdentifier());
            processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, "123272727", dto));
            PSPDate.resetPSPTime();
            //Save the updated billing details by calling BillingManager.save() method
            saveBillingDetails(SourceSystemCode.QBDT);
            PayrollServices.commitUnitOfWork();

            assertSuccess("cancelEmployeeTransaction", processResult);

            // Check the state of the database to ensure all is well...
            PayrollServices.beginUnitOfWork();
            {
                Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
                PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
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

                TransactionType erFeeDB = Application.findById(TransactionType.class, TransactionTypeCode.EmployerFeeDebit);
                IntuitBankAccount expectedFeeIntuitBA = IntuitBankAccount
                        .findIntuitBankAccount(erFeeDB, CreditDebitCode.Credit);

                Iterator<EntryDetailRecord> entryDetailRecords = mmTxn.getEntryDetailRecordCollection().iterator();

                String currencyString = StringFormatter
                        .formatCurrencyNoDecimalPoint(
                                SpcfUtils.convertToBigDecimal(amount), 10);
                String strExpectedRecordData = "627" + oldDebitTxn.getDebitBankAccount()
                        .getRoutingNumber() + StringFormatter
                        .formatString(oldDebitTxn.getDebitBankAccount().getAccountNumber(),
                                17) + currencyString + StringFormatter.formatString(company
                        .getFedTaxId(), 15) + StringFormatter.formatString(company.getLegalName(), 22) + "  0";

                while (entryDetailRecords.hasNext()) {
                    EntryDetailRecord entryDetailRecord = entryDetailRecords.next();
                    if ((entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Credit)) && (entryDetailRecord.getIntuitBankAccount().equals(expectedIntuitBA))) {
                        PayrollSubmitCoreTests.validateNewEntryDetailRecord(entryDetailRecord,
                                newDebitTxn.getFinancialTransactionAmount(), newDebitTxn.getCompany(), expectedIntuitBA,
                                mmTxn, null);
                    }
                    else if (entryDetailRecord.getCreditDebitIndicator().equals(CreditDebitCode.Debit)) {
                        PayrollSubmitCoreTests.validateNewEntryDetailRecord(entryDetailRecord,
                                amount, newDebitTxn.getCompany(), null,
                                mmTxn, strExpectedRecordData);
                    }
                    else {
                        SpcfMoney feeAmount = new SpcfMoney(amount.subtract(newDebitTxn.getFinancialTransactionAmount()));
                        PayrollSubmitCoreTests.validateNewEntryDetailRecord(entryDetailRecord,
                                feeAmount, newDebitTxn.getCompany(), expectedFeeIntuitBA,
                                mmTxn, null);
                    }
                }

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
                checkBillingDetails(payrollRun, payrollRun.getPaycheckCollection().size() - 1);
                // Verify Transmission
                DomainEntitySet<TransmissionPayrollRun> transmissionPayrollSet = payrollRun.getTransmissionPayrollRunCollection();
                assertEquals("Number of TransmissionPayrollRuns", 1, transmissionPayrollSet.size());
                assertEquals("Payroll Process Code", PayrollProcessCode.CancelTransaction, transmissionPayrollSet.get(0).getPayrollProcess());
            }
            PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to cancel select transactions for an OFFLOADED_DEBIT payroll will succeed. This test will
     * pass an explicit list of transactions to cancel that are a subset of existing paycheck split transactions for
     * the payroll run.
     *
     * @param pDtoTxnList The list of source transction id's to cancel
     */
    private void testCancellationOfSelectTransactionsForOffloadedDebitPayroll(List<String> pDtoTxnList) {
        ProcessResult processResult = new ProcessResult();
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", pDtoTxnList);
        dto.setAgentCancel(true);
        BigDecimal prevPayrollNetAmt;

        // retrieve the payroll net amount prior to cancelling the transactions for later error checking
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

            prevPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            TransactionState cancelledTxnState = PayrollServices.entityFinder
                    .findById(TransactionState.class, TransactionStateCode.Cancelled);
            DomainEntitySet<TransactionResponse> txnResponseList;
            BigDecimal cancelTxnAmt = new BigDecimal(0);

            // verify all appropriate paycheck split transactions were cancelled.
            for (String sourcePaycheckID : dto.getSourcePaycheckIdList()) {
                Paycheck paycheck = Paycheck.findPaycheck(company, sourcePaycheckID);
                assertNotNull("Paycheck cancelled txn", paycheck);
                for(PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()){
                    FinancialTransaction txn = paycheckSplit.getFinancialTransaction();

                    assertNotNull("Cancelled transction from paycheck eplit", txn);

                    // verify the transaction was cancelled
                    assertEquals("Cancelled transction state", cancelledTxnState, txn.getCurrentTransactionState());

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
                    SpcfUtils.convertToSpcfMoney(prevPayrollNetAmt.subtract(cancelTxnAmt)),
                    payrollRun.getPayrollDirectDepositAmount());

            // verify an ER_DD_REFUND_CREDIT transaction was created and is correct in all respects
            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                            .Where(FinancialTransaction.PayrollRun().equalTo(payrollRun)
                                    .And(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerDdRefundCredit)))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(PayrollServices.entityFinder.<TransactionState>findById(TransactionState.class, TransactionStateCode.Created)))
                                    .And(FinancialTransaction.FinancialTransactionAmount().equalTo(SpcfUtils.convertToSpcfMoney(cancelTxnAmt))))
                            .OrderBy(FinancialTransaction.CreatedDate().Descending());

            DomainEntitySet<FinancialTransaction> txnList = Application.find(FinancialTransaction.class, query);

            // if, by some remote chance, there are two refund transactions of the exact same amount,
            // the order by clause, which sorts descending, will ensure the latest/newest refund txn
            // is the first one in the result set (which is the one we're interested in.)
            assertTrue("Employer DD Refund Credit transaction count", txnList.size() > 0);

            // verify the new refund txn was correctly associated with the original debit txn
            FinancialTransaction refundTxn = txnList.get(0);
            FinancialTransaction originalTxn = refundTxn.getOriginalTransaction();
            assertNotNull("Original Employer DD Debit transaction", originalTxn);

            // verify the credit account is from the original txn debit account
            assertEquals("Refund txn credit account",
                    originalTxn.getDebitBankAccount().getId(),
                    refundTxn.getCreditBankAccount().getId());

            // verify the debit account is intuit
            IntuitBankAccount intuitRefundDebitBankAccount = IntuitBankAccount
                    .findIntuitBankAccount(TransactionTypeCode.EmployerDdRefundCredit, CreditDebitCode.Debit);
            assertEquals("Refund txn debit account",
                    intuitRefundDebitBankAccount.getBankAccount().getId(),
                    refundTxn.getDebitBankAccount().getId());

            // verify the refund txn settlement type is ACH
            assertEquals("Refund txn settlement type",
                    SettlementType.ACH,
                    refundTxn.getSettlementTypeCd());

            // verify the settlement date for the refund txn
            int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);

            SpcfCalendar expectedSettlementDate = originalTxn.getSettlementDate().toLocal().copy();
            expectedSettlementDate = expectedSettlementDate.toLocal();
            CalendarUtils.addBusinessDays(expectedSettlementDate, achWaitPeriodDays + 1);

            SpcfCalendar txnSettlementDate = refundTxn.getSettlementDate().toLocal().copy();

            assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

            // verify the transaction response was created correctly for the Employer DD Refund Credit transaction
            txnResponseList = TransactionResponse.findTransactionResponses(refundTxn);
            assertEquals("Transaction response for ER refund transaction", 1, txnResponseList.size());

            //Verify the mm txn and entry detail records
            MoneyMovementTransaction originalMMTxn = refundTxn.getMoneyMovementTransaction();
            assertNotNull(originalMMTxn);
            MoneyMovementTransaction mmTxn = refundTxn.getMoneyMovementTransaction();
            assertNotNull(mmTxn);
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that two successive cancellations of transactions for an OFFLOADED_DEBIT payroll will create two
     * refund transactions.
     */
    @Test
    public void testTwoSuccessiveCancellationsOfTransactionsForOffloadedDebitPayroll() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        List<String> sourcePaycheckIdList1 = new Vector<String>(2);
        List<String> sourcePaycheckIdList2 = new Vector<String>(2);
        BigDecimal cancelTxnAmt1 = new BigDecimal(0);
        BigDecimal cancelTxnAmt2 = new BigDecimal(0);
        BigDecimal newPayrollNetAmt = new BigDecimal(0);
        BigDecimal originalPayrollNetAmt = new BigDecimal(0);

        // collect paycheck split transactions for the first two employee paychecks.
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<Paycheck> paycheckList = payrollRun.getPaycheckCollection();
            boolean firstCheckDone = false;

            // get the source dd transaction id's for the first two paychecks that have multiple splits
            for (Paycheck aPaycheckList : paycheckList) {
                DomainEntitySet<PaycheckSplit> splitList = aPaycheckList.getPaycheckSplitCollection();

                if ((splitList != null) && (splitList.size() > 1)) {
                    for (Iterator<PaycheckSplit> splitIter = splitList.iterator(); splitIter.hasNext();) {
                        PaycheckSplit split = splitIter.next();

                        if (firstCheckDone) {
                            if (!sourcePaycheckIdList2.contains(split.getPaycheck().getSourcePaycheckId())) {
                                sourcePaycheckIdList2.add(split.getPaycheck().getSourcePaycheckId());
                            }
                            cancelTxnAmt2 = cancelTxnAmt2.add(SpcfUtils.convertToBigDecimal(
                                    split.getFinancialTransaction().getFinancialTransactionAmount()));
                        } else {
                            if (!sourcePaycheckIdList1.contains(split.getPaycheck().getSourcePaycheckId())) {
                                sourcePaycheckIdList1.add(split.getPaycheck().getSourcePaycheckId());
                            }

                            cancelTxnAmt1 = cancelTxnAmt1.add(SpcfUtils.convertToBigDecimal(
                                    split.getFinancialTransaction().getFinancialTransactionAmount()));
                        }
                    }

                    if (firstCheckDone) {
                        break; // we have two collections of paycheck splits txns, so break
                    } else {
                        firstCheckDone = true;
                    }
                }
            }

            // calculate what the new payroll net amount should be after the cancellations
            originalPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
            newPayrollNetAmt = originalPayrollNetAmt.subtract(cancelTxnAmt1).subtract(cancelTxnAmt2);

            // set the payroll status to OFFLOADED_DEBIT
            setPayrollStatusToOffloadedDebit(payrollRun);
        }
        PayrollServices.commitUnitOfWork();

        testCancellationOfSelectTransactionsForOffloadedDebitPayroll(sourcePaycheckIdList1);
        testCancellationOfSelectTransactionsForOffloadedDebitPayroll(sourcePaycheckIdList2);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

            // verify the payroll net amount was updated correctly.
            assertEquals("Payroll Net Amount",
                    SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                    payrollRun.getPayrollDirectDepositAmount());

            // verify two ER_DD_REFUND_CREDIT transactions were created (this is a bit redundant since the refund
            // txns were checked individually as the cancellations occured, but do it here as a sanity check using
            // the amounts of the refund transactions to calculate the expected new payroll net amount.)
            DomainEntitySet<FinancialTransaction> txnList = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                    new TransactionStateCode[]{TransactionStateCode.Created});

            assertEquals("Employer DD Refund Credit transaction count", 2, txnList.size());

            //Ensure mm txns are created for the refund credits

            // verify the payroll net amount was updated correctly.
            BigDecimal refundTxnAmt1 = SpcfUtils
                    .convertToBigDecimal(txnList.get(0).getFinancialTransactionAmount());
            BigDecimal refundTxnAmt2 = SpcfUtils
                    .convertToBigDecimal(txnList.get(1).getFinancialTransactionAmount());
            newPayrollNetAmt = originalPayrollNetAmt.subtract(refundTxnAmt1).subtract(refundTxnAmt2);
            assertEquals("Payroll Net Amount",
                    SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                    payrollRun.getPayrollDirectDepositAmount());
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Verify that a request to cancel transactions for one payroll will leave another (different) payroll intact.
     */
    @Test
    public void testCancellationOfSelectTransactionsForOnePayrollWithoutAffectingOtherPayroll() {
        loadTestPayrollData(SourceSystemCode.QBOE);

        ProcessResult processResult = new ProcessResult();
        List<String> sourcePaycheckIdList = new Vector<String>(4);
        BigDecimal newPayrollNetAmt = null, payroll2NetAmount = null;
        TransactionCancelEEDTO dto = buildTransactionCancelDTO("BatchId01", sourcePaycheckIdList);

        // collect paycheck split transactions for the first employee and pass this into the dto for cancellation.
        PayrollServices.beginUnitOfWork();
        {
            BigDecimal cancelTxnAmt = new BigDecimal(0);
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<Paycheck> paycheckList = payrollRun1.getPaycheckCollection();

            // get the source dd transaction id's for the first paycheck that has multiple splits
            for (Iterator<Paycheck> checkIter = paycheckList.iterator(); checkIter.hasNext() && sourcePaycheckIdList
                    .isEmpty();) {
                Paycheck check = checkIter.next();
                DomainEntitySet<PaycheckSplit> splitList = check.getPaycheckSplitCollection();

                if ((splitList != null) && (splitList.size() > 1)) {
                    for (PaycheckSplit split : splitList) {
                        if (!sourcePaycheckIdList.contains(split.getPaycheck().getSourcePaycheckId())) {
                            sourcePaycheckIdList.add(split.getPaycheck().getSourcePaycheckId());
                        }
                        cancelTxnAmt = cancelTxnAmt.add(SpcfUtils.convertToBigDecimal(
                                split.getFinancialTransaction().getFinancialTransactionAmount()));
                    }
                }
            }

            newPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun1.getPayrollDirectDepositAmount());
            newPayrollNetAmt = newPayrollNetAmt.subtract(cancelTxnAmt);

            PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchId02");
            payroll2NetAmount = SpcfUtils.convertToBigDecimal(payrollRun2.getPayrollDirectDepositAmount());
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(prSubmitDate);
        processResult.merge(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "123272727", dto));
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

        // Check the state of the database to ensure all is well...
        PayrollServices.beginUnitOfWork();
        {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<TransactionResponse> txnResponseList;
            DomainEntitySet<FinancialTransaction> txnList =
                    FinancialTransaction.findPaycheckSplitFinancialTransactions(
                            payrollRun1,
                            TransactionStateCode.Cancelled);

            // verify all appropriate paycheck split transactions were cancelled.
            for (FinancialTransaction txn : txnList) {
                assertTrue("Cancelled transaction", sourcePaycheckIdList.contains(txn.getPaycheckSplit().getPaycheck().getSourcePaycheckId()));

                // verify the transaction response was created correctly for this txn
                txnResponseList = TransactionResponse.findTransactionResponses(txn);
                assertEquals("Transaction response for cancelled EE transaction", 1, txnResponseList.size());
            }

            // verify the payroll net amount was updated correctly.
            assertEquals("Payroll Net Amount",
                    SpcfUtils.convertToSpcfMoney(newPayrollNetAmt),
                    payrollRun1.getPayrollDirectDepositAmount());

            // verify the old EMPLOYER_DD_DEBIT transaction was cancelled
            txnList = payrollRun1.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                    new TransactionStateCode[]{TransactionStateCode.Cancelled});

            assertEquals("Cancelled Employer DD Debit transaction count", 1, txnList.size());

            FinancialTransaction oldDebitTxn = txnList.get(0);

            // verify the transaction response was created correctly for the old EMPLOYER_DD_DEBIT txn
            txnResponseList = TransactionResponse.findTransactionResponses(oldDebitTxn);
            assertEquals("Transaction response for old ER debit transaction", 1, txnResponseList.size());

            // verify a new EMPLOYER_DD_DEBIT transaction was created and is correct in all respects
            txnList = payrollRun1.getFinancialTransactions(
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

            SpcfCalendar txnSettlementDate = newDebitTxn.getSettlementDate().toLocal().copy();

            assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

            // verify the transaction response was created correctly for the new EMPLOYER_DD_DEBIT txn
            txnResponseList = TransactionResponse.findTransactionResponses(newDebitTxn);
            assertEquals("Transaction response for new ER debit transaction", 1, txnResponseList.size());

            /////////////////////////////////////////
            // verify the second payroll is intact
            /////////////////////////////////////////
            PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchId02");

            // Select all txns for the second payroll run (both EE and ER)
            com.intuit.sbd.payroll.psp.DomainEntitySet<FinancialTransaction> txnList2 = payrollRun2.getFinancialTransactionCollection();

            // verify all transactions are still in a CREATED state.
            for (FinancialTransaction txn : txnList2) {
                assertEquals("Transaction State",
                        TransactionStateCode.Created,
                        txn.getCurrentTransactionState().getTransactionStateCd());
            }

            // verify the payroll run status is still PENDING.
            assertEquals("Payroll Run Status", PayrollStatus.Pending, payrollRun2.getPayrollRunStatus());

            // verify the payroll net amount has not been changed.
            assertEquals("Payroll Net Amount",
                    SpcfUtils.convertToSpcfMoney(payroll2NetAmount),
                    payrollRun2.getPayrollDirectDepositAmount());
        }
        PayrollServices.commitUnitOfWork();
    }

    private void saveBillingDetails(SourceSystemCode pSrcSystemCd) {
        Company company1 = Company.findCompany("123272727", pSrcSystemCd);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company1, "BatchId01");
        //Statement to save by calling BillingManager.save() method for the BillingDetails which was added by
        // PayrollSubmitDD process by calling BillingManager.add() method.
        CompanyBankAccount companyBankAccount = payrollRun1
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
    }

    private void checkBillingDetails(PayrollRun pPayrollRun, int pQuantity) {
        CompanyBankAccount companyBankAccount = pPayrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        //Assertions for Billing Details.
        assertEquals("BillingDetail List Size ", 1, pPayrollRun.getBillingDetailCollection().size());
        assertEquals("Quantity ", pQuantity, pPayrollRun.getBillingDetailCollection().get(0).getQuantity());
        if(pQuantity == 0){
            assertNull("Tax Transaction ", pPayrollRun.getBillingDetailCollection().get(0).getTaxTransaction());
            assertNull("Fee Transaction ", pPayrollRun.getBillingDetailCollection().get(0).getFeeTransaction());
        }
    }

    /**
     * Verify that a request to cancel select transactions for a PENDING payroll will succeed. This test will pass an
     * explicit list of transactions to cancel that are a subset of existing paycheck split transactions for the
     * payroll run.
     */
    @Test
    public void testCancellationOfSelectTransactionsForBackDatedPayroll() {
        BigDecimal newPayrollNetAmt = null;
        Company1Dataloader c1dl = new Company1Dataloader();
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-08-25"));
        PayrollRun payrollRun = c1dl.persistPayrollRun(payrollRunDTO);

        // make sure the fee, added during payroll-submission, gets saved
        CompanyBankAccount cba = CompanyBankAccount
                .findActiveCompanyBankAccount(payrollRun.getCompany());
        PayrollServices.commitUnitOfWork();

        //Offload er txn for first payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070904000000");
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        /***Rep cancels one paycheck****/
        TransactionCancelEEDTO txnCancelDTO = new TransactionCancelEEDTO();
        List<String> sourcePaycheckIds = new ArrayList<String>();
        PaycheckSplit split = PaycheckSplit.findPaycheckSplit(payrollRun, "EEBA1PS1");
        sourcePaycheckIds.add(split.getPaycheck().getSourcePaycheckId());
        txnCancelDTO.setSourcePaycheckIdList(sourcePaycheckIds);
        txnCancelDTO.setSourcePayrollRunId("BatchTest05");
        txnCancelDTO.setAgentCancel(true);
        newPayrollNetAmt = SpcfUtils.convertToBigDecimal(payrollRun.getPayrollDirectDepositAmount());
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567",
                txnCancelDTO);
        Application.commitUnitOfWork();

        assertSuccess("cancelEmployeeTransaction", processResult);

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
            for (String sourcePaycheckID : txnCancelDTO.getSourcePaycheckIdList()) {
                Paycheck paycheck = Paycheck.findPaycheck(company, sourcePaycheckID);

                assertNotNull("Paycheck of cancelled txn", paycheck);
                for(PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()){
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
            // verify an ER_DD_REFUND_CREDIT transaction was created and is correct in all respects
            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                            .Where(FinancialTransaction.PayrollRun().equalTo(payrollRun)
                                    .And(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerDdRefundCredit)))
                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(PayrollServices.entityFinder.<TransactionState>findById(TransactionState.class, TransactionStateCode.Created)))
                                    .And(FinancialTransaction.FinancialTransactionAmount().equalTo(SpcfUtils.convertToSpcfMoney(cancelTxnAmt))))
                            .OrderBy(FinancialTransaction.CreatedDate().Descending());

            DomainEntitySet<FinancialTransaction> txnList1 = Application.find(FinancialTransaction.class, query);

            // if, by some remote chance, there are two refund transactions of the exact same amount,
            // the order by clause, which sorts descending, will ensure the latest/newest refund txn
            // is the first one in the result set (which is the one we're interested in.)
            assertTrue("Employer DD Refund Credit transaction count", txnList1.size() > 0);

            // verify the new refund txn was correctly associated with the original debit txn
            FinancialTransaction refundTxn = txnList1.get(0);
            FinancialTransaction originalTxn = refundTxn.getOriginalTransaction();
            assertNotNull("Original Employer DD Debit transaction", originalTxn);

            // verify the credit account is from the original txn debit account
            assertEquals("Refund txn credit account",
                    originalTxn.getDebitBankAccount().getId(),
                    refundTxn.getCreditBankAccount().getId());

            // verify the debit account is intuit
            IntuitBankAccount intuitRefundDebitBankAccount = IntuitBankAccount
                    .findIntuitBankAccount(TransactionTypeCode.EmployerDdRefundCredit, CreditDebitCode.Debit);
            assertEquals("Refund txn debit account",
                    intuitRefundDebitBankAccount.getBankAccount().getId(),
                    refundTxn.getDebitBankAccount().getId());

            // verify the refund txn settlement type is ACH
            assertEquals("Refund txn settlement type",
                    SettlementType.ACH,
                    refundTxn.getSettlementTypeCd());

            // verify the settlement date for the refund txn
            int achWaitPeriodDays = SystemParameter.findIntValue(SystemParameter.Code.ACH_WAIT_PERIOD, 4);

            SpcfCalendar expectedSettlementDate = originalTxn.getSettlementDate().toLocal().copy();
            expectedSettlementDate = expectedSettlementDate.toLocal();
            CalendarUtils.addBusinessDays(expectedSettlementDate, achWaitPeriodDays + 1);

            SpcfCalendar txnSettlementDate = refundTxn.getSettlementDate().toLocal().copy();

            assertEquals("Refund txn settlement date", expectedSettlementDate, txnSettlementDate);

            // verify the transaction response was created correctly for the Employer DD Refund Credit transaction
            txnResponseList = TransactionResponse.findTransactionResponses(refundTxn);
            assertEquals("Transaction response for ER refund transaction", 1, txnResponseList.size());

            //Verify the mm txn and entry detail records
            MoneyMovementTransaction originalMMTxn = refundTxn.getMoneyMovementTransaction();
            assertNotNull(originalMMTxn);
            MoneyMovementTransaction mmTxn = refundTxn.getMoneyMovementTransaction();
            assertNotNull(mmTxn);

        }
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testCancel_CloudDD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        // submit DD payroll
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("fin txns", 1, financialTransactions.size());
        ArrayList<String> transactionIds = new ArrayList<String>();
        transactionIds.add(financialTransactions.get(0).getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePaycheckIdList(transactionIds);
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult cancelResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PSP_PRAssert.assertSuccess("cancel", cancelResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Cancelled});
        assertEquals("canceled fin txns", 1, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCancel_CloudDDBP() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);

        // submit DD payroll
        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit DD payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("fin txns", 1, financialTransactions.size());
        ArrayList<String> transactionIds = new ArrayList<String>();
        transactionIds.add(financialTransactions.get(0).getPaycheckSplit().getPaycheck().getSourcePaycheckId());
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePaycheckIdList(transactionIds);
        transactionCancelDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult cancelResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelDTO);
        PSP_PRAssert.assertSuccess("cancel", cancelResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Cancelled});
        assertEquals("canceled fin txns", 1, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

        // submit bp payment
        List<Payee> payees = DataLoadServices.addPayees(company, 1);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = DataLoadServices.createBPPayrollRun(company, payees);
        ProcessResult<Collection<PayrollRun>> submitBPPayroll = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PSP_PRAssert.assertSuccess("submit BP Payroll", submitBPPayroll);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertBillPaymentsEqual(company, billPaymentDTOs);
        PayrollServices.rollbackUnitOfWork();

        for (PayrollRun payroll : submitBPPayroll.getResult()) {
            PayrollServices.beginUnitOfWork();
            payrollRun = PayrollRun.findPayrollRun(company, payroll.getSourcePayRunId());
            financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Created});
            assertEquals("fin txns", 1, financialTransactions.size());
            ArrayList<String> billPaymentIds = new ArrayList<String>();
            billPaymentIds.add(financialTransactions.get(0).getBillPaymentSplit().getBillPayment().getSourceId());
            ProcessResult cancelBPResult =  PayrollServices.billPaymentManager.cancelBillPaymentTransaction(
                    company.getSourceSystemCd(),
                    company.getSourceCompanyId(),
                    billPaymentIds,null);
            PSP_PRAssert.assertSuccess("cancel BP transaction", cancelBPResult);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            payrollRun = PayrollRun.findPayrollRun(company, payroll.getSourcePayRunId());
            financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Cancelled});
            assertEquals("canceled fin txns", 1, financialTransactions.size());
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testVoid401kPaycheck_cancelDD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        List<Employee> employees = DataLoadServices.addEEs(company, 1, true, true);

        List<CompanyPayrollItem> companyPayrollItems = DataLoadServices.addPayrollItems(company, PayrollItemCode.Compensation, PayrollItemCode.Tp401kEmployeeDeferral, PayrollItemCode.Tp401kEmployerMatch);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        Collection<ServiceBankAccountDTO> companyBankAccounts = new ArrayList<ServiceBankAccountDTO>();
        if (companyBankAccount != null) {
            CompanyBankAccountDTO companyBankAccountDTO = DataLoadServices.createCompanyBankAccount(companyBankAccount);
            companyBankAccounts.add(DataLoadServices.createServiceBankAccountDTO(companyBankAccountDTO, ServiceCode.DirectDeposit));
            payrollRunDTO.setCompanyBankAccounts(companyBankAccounts);
        }

        SpcfCalendar payrollDate = PSPDate.getPSPTime();
        payrollDate.addDays(2);
        payrollRunDTO.setTargetPayrollTXDate(new DateDTO(payrollDate));
        payrollRunDTO.setPayrollTXBatchId("Batch_DD_401k");

        Collection<PaycheckDTO> paychecks = new ArrayList<PaycheckDTO>();
        for (Employee employee : employees) {
            employee = Employee.findEmployee(company, employee.getSourceEmployeeId());
            // Create Paycheck splits
            Collection<DDTransactionDTO> ddTransactions = new ArrayList<DDTransactionDTO>();
            DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = employee.getEmployeeBankAccountCollection();
            for (int i = 0; i < employeeBankAccounts.size(); i++) {
                ddTransactions.add(DataLoadServices.createDDTransactionDTO(DataLoadServices.createEmployeeBankAccount(employeeBankAccounts.get(i)), new BigDecimal(i+1)));
            }

            // Create Paycheck
            PaycheckDTO paycheckDTO = new PaycheckDTO();
            paycheckDTO.setDdTransactions((List<DDTransactionDTO>)ddTransactions);
            paycheckDTO.setEmployeeId(employee.getSourceEmployeeId());
            paycheckDTO.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            SpcfMoney totalPaycheckNetAmount = new SpcfMoney();
            for (DDTransactionDTO currDDTxn : ddTransactions) {
                SpcfMoney currAmount = new SpcfMoney(currDDTxn.getDDTransactionAmount().toString());
                totalPaycheckNetAmount = (SpcfMoney) totalPaycheckNetAmount.add(currAmount);
            }
            paycheckDTO.setPaycheckNetAmount(totalPaycheckNetAmount);

            ArrayList<CompensationTransactionDTO> compensationTransactions = new ArrayList<CompensationTransactionDTO>();
            ArrayList<DeductionTransactionDTO> deductionTransactions = new ArrayList<DeductionTransactionDTO>();
            ArrayList<EmployerContributionTransactionDTO> employerContributionTransactions = new ArrayList<EmployerContributionTransactionDTO>();

            for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
                switch(companyPayrollItem.getPayrollItem().getPayrollItemCode()) {
                    case Compensation:
                        compensationTransactions.add(DataLoadServices.createCompensationTransaction(companyPayrollItem.getSourcePayrollItemId()));
                        break;
                    case Tp401kEmployeeDeferral:
                        deductionTransactions.add(DataLoadServices.createDeductionTransaction(companyPayrollItem.getSourcePayrollItemId()));
                        break;
                    case Tp401kEmployerMatch:
                        employerContributionTransactions.add(DataLoadServices.createEmployerContributionTransaction(companyPayrollItem.getSourcePayrollItemId()));
                        break;
                }
            }

            paycheckDTO.setCompensationTransactions(compensationTransactions);
            paycheckDTO.setDeductionTransactions(deductionTransactions);
            paycheckDTO.setEmployerContributionTransactions(employerContributionTransactions);

            paycheckDTO.setEmployeeId(employee.getSourceEmployeeId());
            SpcfDecimal compensationAmount = SpcfDecimal.createInstance(0.00);
            for (CompensationTransactionDTO compensationTransaction : compensationTransactions) {
                compensationAmount = compensationAmount.add(compensationTransaction.getCompensationAmount());
            }
            paycheckDTO.setPaycheckGrossAmount(new SpcfMoney(compensationAmount));

            BigDecimal deductionAmount = new BigDecimal(0.00);
            for (DeductionTransactionDTO deductionTransaction : deductionTransactions) {
                deductionAmount = deductionAmount.add(deductionTransaction.getDeductionAmount());
            }

            paycheckDTO.setPaycheckNetAmount(new SpcfMoney(compensationAmount.subtract(SpcfUtils.convertToSpcfMoney(deductionAmount))));

            SpcfCalendar periodBeginDate = PSPDate.getPSPTime();
            periodBeginDate.addDays(-7);
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(periodBeginDate));

            SpcfCalendar periodEndDate = PSPDate.getPSPTime();
            periodEndDate.addDays(-2);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(periodEndDate));
            paychecks.add(paycheckDTO);
        }


        payrollRunDTO.setPaychecks(paychecks);


        ProcessResult<PayrollRun> submitPayrollPR =
                PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        PSP_PRAssert.assertSuccess("addFirstPayroll", submitPayrollPR);
        PayrollServices.commitUnitOfWork();

        // void the 401k paycheck
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        ProcessResult voidPaychecksResult = PayrollServices.payrollManager.voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO);
        PSP_PRAssert.assertSuccess("void 401k payroll", voidPaychecksResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            assertEquals("paycheck voided", true, paycheck.isVoided());
            assertEquals("paycheck inactive", PaycheckStatusCode.Inactive, paycheck.getStatus());
        }
        PayrollServices.rollbackUnitOfWork();

        // cancel the dd transaction
        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        ProcessResult cancelPaycheckResult = PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO);
        PSP_PRAssert.assertSuccess("void 401k payroll", cancelPaycheckResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplits()) {
                assertEquals("fin txn cancelled", TransactionStateCode.Cancelled, paycheckSplit.getFinancialTransaction().getCurrentTransactionState().getTransactionStateCd());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

}