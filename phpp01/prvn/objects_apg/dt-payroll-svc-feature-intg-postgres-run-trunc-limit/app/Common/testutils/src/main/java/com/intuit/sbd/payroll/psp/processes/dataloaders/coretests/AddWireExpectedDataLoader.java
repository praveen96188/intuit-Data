package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: mkinasz
 * Date: Jul 29, 2008
 * Time: 1:35:35 PM
 */
public class AddWireExpectedDataLoader {

    public static PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

    public static void runBefore() {
        PayrollServicesTest.beforeEachTest();
        Application.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
    }

    @SuppressWarnings("unchecked")
    public static void addWireExpectedData() {
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

        payrollRun.updatePayrollRunStatus(PayrollStatus.PendingReversals);
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
        com.intuit.sbd.payroll.psp.DomainEntitySet<CompanyEventDetail> eventDetails = companyEvents.get(0).getCompanyEventDetailCollection();
        eventDetails = eventDetails.sort(CompanyEventDetail.EventDetailTypeCd());

        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.CollectionStage, eventDetails.get(0).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", CollectionStageCode.FirstCollectionAttempt.toString(), eventDetails.get(0).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.PayrollRunId, eventDetails.get(1).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", payrollRun.getId().toString(), eventDetails.get(1).getValue());
        Assert.assertEquals("Event Detail Code", EventDetailTypeCode.WireExpectedDate, eventDetails.get(2).getEventDetailTypeCd());
        Assert.assertEquals("Event Detail Value", DateDTO.convertToSpcfCalendar(wireExpectedDTO.getWireExpectedDate()).toString(), eventDetails.get(2).getValue());
        PayrollServices.commitUnitOfWork();
    }

    public static ModifyWireExpectedDTO createWireExpectedDTO(String pSourcePayrollRunId) {
        DomainEntitySet<CollectionStage> collectionStages = Application.findObjects(CollectionStage.class);
        CollectionStage collectionStage = null;
        for (CollectionStage collStage:collectionStages) {
            if (collStage.getCollectionStageCode() == CollectionStageCode.FirstCollectionAttempt) {
                collectionStage = collStage;
                break;
            }
        }

        DateDTO wireExpectedDate = new DateDTO("2007-09-17");
        ModifyWireExpectedDTO wireExpectedDTO = new ModifyWireExpectedDTO(pSourcePayrollRunId, wireExpectedDate,
                    collectionStage, ActionEventCode.ERWireExpected, false);

        return wireExpectedDTO;
    }

    public static void loadWireExpectedDate() {
        runBefore();
        addWireExpectedData();
    }
}
