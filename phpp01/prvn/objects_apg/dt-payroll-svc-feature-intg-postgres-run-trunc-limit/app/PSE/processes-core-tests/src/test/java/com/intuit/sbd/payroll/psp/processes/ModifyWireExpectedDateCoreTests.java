package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddWireExpectedDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jul 2, 2008
 * Time: 11:50:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class ModifyWireExpectedDateCoreTests {

    @Before
    public void runBeforeEachTest() {
        AddWireExpectedDataLoader.runBefore();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO("BatchId01");
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(null, "123272727", wireExpectedDTO);

        Application.commitUnitOfWork();
        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO("BatchId01");
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, null, wireExpectedDTO);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 5001 - PayrollRunId has Invalid value
     */
    @Test
    public void testNullPayrollRunId() {

        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(null);
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "SourcePayrollRunId has invalid value",
                errorMessage.getMessage());
    }

    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testInvalidCompany() {
        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO("BatchId01");
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "Invalid", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:Invalid does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testNullWireExpectedDTO() {

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", null);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "WireExpectedDTO has invalid value",
                errorMessage.getMessage());
        
        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO();
        processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 4, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "SourcePayrollRunId has invalid value",
                errorMessage.getMessage());

        errorMessage = processResult.getMessages().get(1);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "CollectionStage has invalid value",
                errorMessage.getMessage());

        errorMessage = processResult.getMessages().get(2);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "ActionEventCode has invalid value",
                errorMessage.getMessage());

        errorMessage = processResult.getMessages().get(3);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "WireExpectedDate has invalid value",
                errorMessage.getMessage());
    }

    /**
     * Test message 194 - PayrollRunId Does Not Exist
     */
    @Test
    public void testInvalidPayrollRunId() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddWireExpectedDataLoader.psdl.loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO("Invalid");
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message", "Payroll Run with DDTxBatchID Invalid does not exist for company QBOE:123272727.",
                errorMessage.getMessage());
    }

    // wire expected date is weekend/in past
    @Test
    public void testInvalidWireExpectedDate() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddWireExpectedDataLoader.psdl.loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(payrollRunDTO.getPayrollTXBatchId());
        wireExpectedDTO.setWireExpectedDate(new DateDTO("2007-09-15"));
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "10011", errorMessage.getMessageCode());
        assertEquals("Error message", "Cannot record a transaction of Settlement Type Wire and date " 
                + DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()).toString()
                + ", which is not a future banking day.",
                errorMessage.getMessage());

        Application.beginUnitOfWork();
        wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(payrollRunDTO.getPayrollTXBatchId());
        wireExpectedDTO.setWireExpectedDate(new DateDTO("2007-09-01")); // "today" is 9/14
        processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "10011", errorMessage.getMessageCode());
        assertEquals("Error message", "Cannot record a transaction of Settlement Type Wire and date "
                + DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()).toString()
                + ", which is not a future banking day.",
                errorMessage.getMessage());
    }

    @Test
    public void testActionInvalidForpayroll() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddWireExpectedDataLoader.psdl.loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());

        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(payrollRunDTO.getPayrollTXBatchId());
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "1048", errorMessage.getMessageCode());
        assertEquals("Error message", "Action ERWireExpected is not valid for payroll run with DDTxBatchID "+
                payrollRunDTO.getPayrollTXBatchId() +", which has status of Pending.",
                errorMessage.getMessage());
    }

    @Test
    public void testModifyWireExpectedDate_Today_Payroll_Reversed() {
        testModifyWireExpectedDate_Payroll_Reversed(null, "Today's date"); // PSRV000689... should succeed, but was failing process validation
    }

    @Test
    public void testModifyWireExpectedDate_Future_Payroll_Reversed() {
        testModifyWireExpectedDate_Payroll_Reversed(SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone()), "Future date");
    }

    private void testModifyWireExpectedDate_Payroll_Reversed(SpcfCalendar pDate, String pMessage) {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddWireExpectedDataLoader.psdl.loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        //Persist the Transction Return
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(3);
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "Non-NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        TransactionReverseDTO transactionReverseDTO = new TransactionReverseDTO();

        transactionReverseDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionReverseDTO.setDdTransactionIdList(null);
        transactionReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        transactionReverseDTO.setTxDate(null);
        transactionReverseDTO.setChargeFee(false);
        transactionReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult procResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(),
                company.getSourceCompanyId(), transactionReverseDTO);

        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        assertSuccess("Reversal process result", procResult);
        assertEquals("Payroll run status is PendingReversals", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(payrollRunDTO.getPayrollTXBatchId());
        DateDTO dtoDate = new DateDTO(pDate == null ? PSPDate.getPSPTime() : pDate);
        wireExpectedDTO.setWireExpectedDate(dtoDate);
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertSuccess("modifyWireExpectedDate() with " + pMessage, processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DomainEntitySet<FinancialTransaction> cancelledReversals = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        // verify the reversal transaction were cancelled
        assertEquals("Number of ACH ee dd reversal Financial Txs", 2, cancelledReversals.size());
        for (FinancialTransaction currEEDDReversal : cancelledReversals) {
            assertEquals("Payroll Run Id ", payrollRunDTO.getPayrollTXBatchId(), currEEDDReversal.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currEEDDReversal.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currEEDDReversal.getSettlementTypeCd());
        }
        // verify payroll run
        assertEquals("PayrollRun status", PayrollStatus.PendingWire, payrollRun.getPayrollRunStatus());
        assertEquals("Collection Stage", wireExpectedDTO.getCollectionStage().getCollectionStageCode(), payrollRun.getCollectionStageCd());
        assertEquals("Wire Expected Date", DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()),
                payrollRun.getWireExpectedDate().toLocal());

        // verify company events
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.WireExpected, null, null, null);
        assertEquals("Number of Wire Expected Events", 1, companyEvents.size());

        assertEquals("Event status", CompanyEventStatus.Active, companyEvents.get(0).getStatusCd());
        // verify the details
        DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(0).getCompanyEventDetailCollection();
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CollectionStage, eventDetails.get(0).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", CollectionStageCode.FirstCollectionAttempt.toString(), eventDetails.get(0).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.PayrollRunId, eventDetails.get(1).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", payrollRun.getId().toString(), eventDetails.get(1).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.WireExpectedDate, eventDetails.get(2).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", DateDTO.convertToSpcfCalendar(dtoDate).format("yyyyMMdd"), eventDetails.get(2).getValue());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testModifyWireExpectedDate_Payroll_DebitReturned() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddWireExpectedDataLoader.psdl.loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
        //Persist the Transction Return
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R01", "NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        assertEquals("Payroll run status is PendingAutoRedebit", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(payrollRunDTO.getPayrollTXBatchId());
        wireExpectedDTO.setWireExpectedDate(new DateDTO("2007-10-01"));
        wireExpectedDTO.setActionEventCode(ActionEventCode.DDRedebitEdit);
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DomainEntitySet<FinancialTransaction> cancelledReversals = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        // verify the Redebit transaction is cancelled
        assertEquals("Number of ACH ER DD Redebit Financial Txs", 1, cancelledReversals.size());
        for (FinancialTransaction currDDReversal : cancelledReversals) {
            assertEquals("Payroll Run Id ", payrollRunDTO.getPayrollTXBatchId(), currDDReversal.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currDDReversal.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currDDReversal.getSettlementTypeCd());
        }
        // verify payroll run
        assertEquals("PayrollRun status", PayrollStatus.PendingWire, payrollRun.getPayrollRunStatus());
        assertEquals("Collection Stage", wireExpectedDTO.getCollectionStage().getCollectionStageCode(), payrollRun.getCollectionStageCd());
        assertEquals("Wire Expected Date", DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()),
                payrollRun.getWireExpectedDate().toLocal());

        // verify wire expected events
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.WireExpected, null, null, null);
        assertEquals("Number of Wire Expected Events", 0, companyEvents.size());

        companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.ChangeRedebitToWireExpected, null, null, null);
        assertEquals("Number of Wire Expected Events", 1, companyEvents.size());
        CompanyEvent companyEvent = companyEvents.get(0);

        assertEquals("Event status", CompanyEventStatus.Active, companyEvent.getStatusCd());
        // verify the details
        DomainEntitySet<CompanyEventDetail> eventDetails = companyEvent.getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        assertEquals("Number of Wire Expected Event Details", 4, eventDetails.size());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CollectionStage, eventDetails.get(0).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", CollectionStageCode.FirstCollectionAttempt.toString(), eventDetails.get(0).getValue());

        CompanyService ddService = company.getService(ServiceCode.DirectDeposit);
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CompanyServiceId, eventDetails.get(1).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", ddService.getId().toString(), eventDetails.get(1).getValue());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.PayrollRunId, eventDetails.get(2).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", payrollRun.getId().toString(), eventDetails.get(2).getValue());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.WireExpectedDate, eventDetails.get(3).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", "20071001", eventDetails.get(3).getValue());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testModifyWireExpectedDate_Twice() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = AddWireExpectedDataLoader.psdl.loadDataForPayrollSubmit();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();

        //Persist the Transction Return
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "Non-NSF return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        //Call TransactionReturn Handler for Generic Debit Return
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        assertTrue("Process Result", payrollProcess.isSuccess());
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        TransactionReverseDTO transactionReverseDTO = new TransactionReverseDTO();

        transactionReverseDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionReverseDTO.setDdTransactionIdList(null);
        transactionReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        transactionReverseDTO.setTxDate(null);
        transactionReverseDTO.setChargeFee(false);
        transactionReverseDTO.setIntuitInitiatedReversals(true);

        ProcessResult procResult = PayrollServices.payrollManager.reverseTransaction(company.getSourceSystemCd(),
                company.getSourceCompanyId(), transactionReverseDTO);

        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        assertSuccess("Reversal process result", procResult);
        assertEquals("Payroll run status is PendingReversals", PayrollStatus.PendingReversals, payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO = AddWireExpectedDataLoader.createWireExpectedDTO(payrollRunDTO.getPayrollTXBatchId());
        wireExpectedDTO.setWireExpectedDate(new DateDTO("2007-10-01"));
        ProcessResult processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO);
        Application.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DomainEntitySet<FinancialTransaction> cancelledReversals = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        // verify the reversal transaction were cancelled
        assertEquals("Number of ACH ee dd reversal Financial Txs", 2, cancelledReversals.size());
        for (FinancialTransaction currEEDDReversal : cancelledReversals) {
            assertEquals("Payroll Run Id ", payrollRunDTO.getPayrollTXBatchId(), currEEDDReversal.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currEEDDReversal.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currEEDDReversal.getSettlementTypeCd());
        }
        // verify payroll run
        assertEquals("PayrollRun status", PayrollStatus.PendingWire, payrollRun.getPayrollRunStatus());
        assertEquals("Collection Stage", wireExpectedDTO.getCollectionStage().getCollectionStageCode(), payrollRun.getCollectionStageCd());
        assertEquals("Wire Expected Date", DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()),
                payrollRun.getWireExpectedDate().toLocal());

        // verify wire expected events
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.WireExpected, null, null, null);
        assertEquals("Number of Wire Expected Events", 1, companyEvents.size());

        assertEquals("Event status", CompanyEventStatus.Active, companyEvents.get(0).getStatusCd());
        // verify the details
        DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(0).getCompanyEventDetailCollection();
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CollectionStage, eventDetails.get(0).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", CollectionStageCode.FirstCollectionAttempt.toString(), eventDetails.get(0).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.PayrollRunId, eventDetails.get(1).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", payrollRun.getId().toString(), eventDetails.get(1).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.WireExpectedDate, eventDetails.get(2).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", "20071001", eventDetails.get(2).getValue());
        PayrollServices.commitUnitOfWork();

        // call modify wire expected process again with new values
        Application.beginUnitOfWork();
        ModifyWireExpectedDTO wireExpectedDTO2 = AddWireExpectedDataLoader.createWireExpectedDTO(payrollRunDTO.getPayrollTXBatchId());
        wireExpectedDTO2.setWireExpectedDate(new DateDTO("2007-10-02"));
        wireExpectedDTO2.setActionEventCode(ActionEventCode.ERWireExpected);
        DomainEntitySet<CollectionStage> collectionStages = Application.findObjects(CollectionStage.class);
        CollectionStage collectionStage = null;
        for (CollectionStage collStage : collectionStages) {
            if (collStage.getCollectionStageCode() == CollectionStageCode.SecondCollectionAttempt) {
                collectionStage = collStage;
                break;
            }
        }
        wireExpectedDTO2.setCollectionStage(collectionStage);
        processResult = PayrollServices.payrollManager.modifyWireExpectedDate(SourceSystemCode.QBOE, "123272727", wireExpectedDTO2);
        Application.commitUnitOfWork();

        assertTrue(processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        cancelledReversals = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdReversalDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        // verify the reversal transaction were cancelled
        assertEquals("Number of ACH ee dd reversal Financial Txs", 2, cancelledReversals.size());
        for (FinancialTransaction currEEDDReversal : cancelledReversals) {
            assertEquals("Payroll Run Id ", payrollRunDTO.getPayrollTXBatchId(), currEEDDReversal.getPayrollRun().getSourcePayRunId());
            assertEquals("Financial Transaction State ", TransactionStateCode.Cancelled, currEEDDReversal.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("Settlement Type ", SettlementType.ACH, currEEDDReversal.getSettlementTypeCd());
        }
        // verify payroll run
        assertEquals("PayrollRun status", PayrollStatus.PendingWire, payrollRun.getPayrollRunStatus());
        assertEquals("Collection Stage", wireExpectedDTO2.getCollectionStage().getCollectionStageCode(), payrollRun.getCollectionStageCd());
        assertEquals("Wire Expected Date", DateDTO.convertToSpcfCalendar(wireExpectedDTO2.getWireExpectedDate()),
                payrollRun.getWireExpectedDate().toLocal());

        // verify wire expected events
        companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.WireExpected, null, null, null);
        assertEquals("Number of Wire Expected Events", 2, companyEvents.size());

        assertEquals("Event status", CompanyEventStatus.Active, companyEvents.get(0).getStatusCd());
        // verify the details
        eventDetails = companyEvents.get(0).getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CollectionStage, eventDetails.get(0).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", CollectionStageCode.FirstCollectionAttempt.toString(), eventDetails.get(0).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.PayrollRunId, eventDetails.get(1).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", payrollRun.getId().toString(), eventDetails.get(1).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.WireExpectedDate, eventDetails.get(2).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", "20071001", eventDetails.get(2).getValue());

        eventDetails = companyEvents.get(1).getCompanyEventDetailCollection().sort(CompanyEventDetail.EventDetailTypeCd());
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CollectionStage, eventDetails.get(0).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", CollectionStageCode.SecondCollectionAttempt.toString(), eventDetails.get(0).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.PayrollRunId, eventDetails.get(1).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", payrollRun.getId().toString(), eventDetails.get(1).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.WireExpectedDate, eventDetails.get(2).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", "20071002", eventDetails.get(2).getValue());
        PayrollServices.commitUnitOfWork();
    }


}
