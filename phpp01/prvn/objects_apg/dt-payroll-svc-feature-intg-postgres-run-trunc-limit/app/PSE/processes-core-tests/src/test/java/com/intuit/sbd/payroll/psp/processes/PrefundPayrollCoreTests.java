package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.MoneyMovementControlUtil;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Sep 2, 2009
 * Time: 3:38:40 PM
 */
public class PrefundPayrollCoreTests {


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
    public void testNullCompany() {
        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(SourceSystemCode.QBOE, null, null, null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testNullSourceSystem() {
        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(null, "1234567", null, null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testCompanyDoesNotExist() {
        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(SourceSystemCode.QBOE, "1234567", null, null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1234567 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testPayrollRunDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(),  null, null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "194", errorMessage.getMessageCode());
        assertEquals("Error message", "Payroll Run with DDTxBatchID null does not exist for company QBOE:1234567.",
                errorMessage.getMessage());
    }

    @Test
    public void testPayrollInvalidStatus(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // cancel all of the transactions puting the payroll in a canceled state
        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
        dto.setSourcePayrollRunId("BatchTest05");
        List<String> dtoTxnList = new ArrayList<String>();
        dto.setSourcePaycheckIdList(dtoTxnList);
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        for (Paycheck paycheck  : payrollRun.getPaycheckCollection()) {
            dtoTxnList.add(paycheck.getSourcePaycheckId());
        }
        ProcessResult cancelPayrollResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBOE, "1234567", dto);
        PayrollServices.commitUnitOfWork();

        // Ensure cancelation was succsessful
        assertSuccess("cancelPayroll", cancelPayrollResult);

        // try to add prefunding transactions
        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest05", null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "1048", errorMessage.getMessageCode());
        assertEquals("Error message", "Action " + ActionEventCode.RecordPrefundingWire + " is not valid for payroll run with DDTxBatchID BatchTest05, which has status of " + PayrollStatus.Canceled + ".",
                errorMessage.getMessage());
    }

    @Test
    public void testPayrollAlreadyOffloadedWithACHDebit(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest05", null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "1048", errorMessage.getMessageCode());
        assertEquals("Error message", "Action RecordPrefundingWire is not valid for payroll run with DDTxBatchID BatchTest05, which has status of OffloadedAll.",
                errorMessage.getMessage());
    }

    @Test
    public void testAfterCutoffTime(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // move time after cutoff time
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928190100");
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest05", null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "305", errorMessage.getMessageCode());
        assertEquals("Error message", "Transactions for DDTxBatch BatchTest05 for company QBOE:1234567 have already been sent to the bank and cannot be canceled.",
                errorMessage.getMessage());
    }

    @Test
    public void testInvalidSettlementType(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest05", null, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();


        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", EntityName.SettlementType + " has invalid value",
                errorMessage.getMessage());

        Application.beginUnitOfWork();
        prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest05", SettlementType.ACH, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "165", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid Settlement Type Code " + SettlementType.ACH + " specified.",
                errorMessage.getMessage());
    }

    @Test
    public void testInvalidSettlementDate(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        Application.beginUnitOfWork();
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest05", SettlementType.Wire, null, null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        Application.commitUnitOfWork();


        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        Message errorMessage = prefundPayrollCore.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "settlement date has invalid value",
                errorMessage.getMessage());
    }

    @Test
    public void testInvalidAmounts(){
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // find the debit transactions
        PayrollServices.beginUnitOfWork();

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest87", SettlementType.Wire, PSPDate.getPSPTime(), null);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());

        assertEquals("Messages size", 1, prefundPayrollCore.getMessages().size());
        for (Message message : prefundPayrollCore.getMessages()) {
            assertEquals("Error message code", "5001", message.getMessageCode());
            assertEquals("Error message", "The PrefundPayrollTransactionDTO collection has invalid value",
                    message.getMessage());
        }

        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest87");

        ArrayList<SpcfMoney> savesTransactionAmounts = new ArrayList<SpcfMoney>();
        // update all of the amounts to null
        for (PrefundPayrollTransactionDTO transactionDTO : transactionDTOs) {
            // save original amounts
            savesTransactionAmounts.add(transactionDTO.getTransactionAmount());
            if(transactionDTO.getTaxTransactionAmount() != null){
                savesTransactionAmounts.add(transactionDTO.getTaxTransactionAmount());
            }
            transactionDTO.setTaxTransactionAmount(null);
            transactionDTO.setTransactionAmount(null);
        }

        prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest87", SettlementType.Wire, PSPDate.getPSPTime(), transactionDTOs);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());

        assertEquals("Messages size", 3, prefundPayrollCore.getMessages().size());
        for (Message message : prefundPayrollCore.getMessages()) {
            assertEquals("Error message code", "5001", message.getMessageCode());
            assertEquals("Error message", "amount has invalid value",
                    message.getMessage());
        }

        // update all of the amounts to less than 0
        for (PrefundPayrollTransactionDTO transactionDTO : transactionDTOs) {
            transactionDTO.setTaxTransactionAmount(new SpcfMoney("-1.00"));
            transactionDTO.setTransactionAmount(new SpcfMoney("-1.00"));
        }

        prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest87", SettlementType.Wire, PSPDate.getPSPTime(), transactionDTOs);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());

        assertEquals("Messages size", 3, prefundPayrollCore.getMessages().size());
        for (Message message : prefundPayrollCore.getMessages()) {
            assertEquals("Error message code", "267", message.getMessageCode());
            assertEquals("Error message", "For non-ACH transactions, the amount must be a non-zero, positive number.",
                    message.getMessage());
        }

        // update all of the amounts to less than the amount due
        for (PrefundPayrollTransactionDTO transactionDTO : transactionDTOs) {
            transactionDTO.setTaxTransactionAmount(new SpcfMoney("0.01"));
            transactionDTO.setTransactionAmount(new SpcfMoney("0.01"));
        }

        prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest87", SettlementType.Wire, PSPDate.getPSPTime(), transactionDTOs);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());

        assertEquals("Messages size", 3, prefundPayrollCore.getMessages().size());
        for (int i = 0; i<prefundPayrollCore.getMessages().size(); i++) {
            Message message = prefundPayrollCore.getMessages().get(i);
            SpcfMoney orginalAmount = savesTransactionAmounts.get(i);
            assertEquals("Error message code", "303", message.getMessageCode());
            assertEquals("Error message", "New transaction amount (0.01) must be greater than " + orginalAmount.toString() + ".",
                    message.getMessage());
        }
        Application.commitUnitOfWork();

    }

    @Test
    public void test_HappyPath() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);
        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is pending
        assertEquals("payroll status pending", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());

        // offload the ee credits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
            }
        }
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20071002");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20071002");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        // check the status of the payroll
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runACHTransactionProcessor();

        // check ee fin transctions to make sure they are now complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            if(financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployeeDdCredit){
                assertEquals("ee credit complete", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            }
        }
        PayrollServices.commitUnitOfWork();

        // try to submit a third payroll that is over the limits
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = companyDataloader.get3rdCompany2PR_ExceedsOldLimits(new DateDTO("2007-11-01"));
        submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure we still reject the company's payroll if it is over the limit
        assertFalse("submit Payroll", submitPayrollResult.isSuccess());
        assertEquals("Messages size", 1, submitPayrollResult.getMessages().size());
        Message message = submitPayrollResult.getMessages().get(0);
        assertEquals("Error message code", "1043", message.getMessageCode());
        assertEquals("Error message", "Payroll Run BatchTest10 for company QBDT:8574536 exceeded current DD limits and could not be processed.",
                message.getMessage());
    }

    @Test
    public void test_other_pending_payrolls() {

        try {
            // skip the FT validation for this testcase
            MoneyMovementControlUtil.setSkipValidation(true);
            Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

            // offload the first payroll
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime("20070928000000");
            PayrollServices.commitUnitOfWork();
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            // place the company on hold
            PayrollServices.beginUnitOfWork();
            ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                    companyDataloader.getCompany().getSourceCompanyId(),
                                                                                    ServiceSubStatusCode.PendingPrefundingWire);
            PayrollServices.commitUnitOfWork();
            assertSuccess("placed on hold", result);

            // submit a second payroll over the dd limit
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
            ProcessResult submitPayrollResult = PayrollServices.payrollManager
                    .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            // Ensure processing was succsessful
            assertSuccess("submitPayroll", submitPayrollResult);

            // submit a third payroll over the dd limit
            PayrollServices.beginUnitOfWork();
            payrollRunDTO = companyDataloader.get3rdCompany2PR_ExceedsOldLimits(new DateDTO("2007-10-04"));
            submitPayrollResult = PayrollServices.payrollManager
                    .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
            PayrollServices.commitUnitOfWork();

            // Ensure processing was succsessful
            assertSuccess("submitPayroll", submitPayrollResult);

            // find the debit transactions
            PayrollServices.beginUnitOfWork();
            SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(settlementDate);
            ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
            ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
            assertSuccess("prefunding", prefundPayrollCore);
            PayrollServices.commitUnitOfWork();

            // make sure company was not taken off of hold
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                        companyDataloader.getCompany().getSourceSystemCd());
            assertNotNull("company hold", company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
            PayrollServices.commitUnitOfWork();
        } finally {
            MoneyMovementControlUtil.setSkipValidation(false);
        }
    }

    @Test
    public void test_HappyPath_over_paid_wire() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");

        // increase the amount paid on the wire to create refunds
        for (PrefundPayrollTransactionDTO transactionDTO : transactionDTOs) {
            if(transactionDTO.getOriginalTransactionId() != null){
                transactionDTO.setTransactionAmount((SpcfMoney)transactionDTO.getTransactionAmount().add(new SpcfMoney("5.00")));
            }
            if(transactionDTO.getOriginalTaxTransactionId() != null){
                transactionDTO.setTaxTransactionAmount((SpcfMoney)transactionDTO.getTaxTransactionAmount().add(new SpcfMoney("5.00")));
            }
        }

        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);
        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
         PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        // also make sure refunds were created for the debits
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        int erRefundCreditCount = 0;
        int erFeeRefundCreditCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                case EmployerDdRefundCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    assertEquals("refund amount", new SpcfMoney("5.00"), financialTransaction.getFinancialTransactionAmount());
                    erRefundCreditCount++;
                    break;
                case EmployerFeeRefundCredit:
                case ServiceSalesAndUseTaxRefundCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    assertEquals("refund amount", new SpcfMoney("5.00"), financialTransaction.getFinancialTransactionAmount());
                    erFeeRefundCreditCount++;
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        assertEquals("number of dd refund credit transactions", 1, erRefundCreditCount);
        assertEquals("number of fee refund credit transactions", 2, erFeeRefundCreditCount);
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is pending
        assertEquals("payroll status pending", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());

        // offload the ee credits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        // make sure the credits move to executed
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
            }
        }
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        // offload the refunds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // make sure refunds were offloaded
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdRefundCredit:
                    assertEquals("transaction status", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                case EmployerFeeRefundCredit:
                case ServiceSalesAndUseTaxRefundCredit:
                    assertEquals("transaction status ", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
            }
        }
        PayrollServices.commitUnitOfWork();

        // if a wire is added at a later point make sure it creates a refund
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> erDdDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        transactionDTOs = new ArrayList<PrefundPayrollTransactionDTO>();
        PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = new PrefundPayrollTransactionDTO();
        prefundPayrollTransactionDTO.setOriginalTransactionId(erDdDebitTransactions.get(0).getId().toString());
        prefundPayrollTransactionDTO.setTransactionAmount(new SpcfMoney("10.00"));
        transactionDTOs.add(prefundPayrollTransactionDTO);
        SpcfCalendar prefundSettlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(prefundSettlementDate);

        prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, prefundSettlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);
        PayrollServices.commitUnitOfWork();

        // make sure the refund transaction was created
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> refundTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRefundCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("refund transactions", 1, refundTransactions.size());
        assertEquals("refund amount", new SpcfMoney("10.00"), refundTransactions.get(0).getFinancialTransactionAmount());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_void_prefunding_wire_before_offload() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");

        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();

        // void the dd debit transaction
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("number of debit transactions", 1, debitTransactions.size());
        // void the transaction
        assertSuccess("void transaction",
                PayrollServices.financialTransactionManager.voidTransaction(companyDataloader.getCompany().getSourceSystemCd(),
                                                                            companyDataloader.getCompany().getSourceCompanyId(),
                                                                            debitTransactions.get(0).getId().toString()));
        PayrollServices.commitUnitOfWork();

        // make sure the company was put back on hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertNotNull("company hold", company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_cancel_employee_paychecks_before_offload() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");

        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
         PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();

        //Add user to cancel transactions
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("Admin");

        ProcessResult<AuthUser> processResult =
                PayrollServices.userManager.addUser("UnitTestAgent1", Arrays.asList(foundRole.getRoleId()), "pFirstName", "Last1");

        assertSuccess("Add User ProcessResult ", processResult);
        AuthUser user = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        user = AuthUser.findUser(user.getCorpId());
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));
        PayrollServices.commitUnitOfWork();

        // try to cancel an employee paycheck
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        TransactionCancelEEDTO TransactionCancelEEDTO = new TransactionCancelEEDTO();
        TransactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> paycheckIds = new ArrayList<String>();
        TransactionCancelEEDTO.setSourcePaycheckIdList(paycheckIds);
        TransactionCancelEEDTO.setAgentCancel(true);
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            paycheckIds.add(paycheck.getSourcePaycheckId());
        }
        ProcessResult cancelResult = PayrollServices.payrollManager.cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), TransactionCancelEEDTO);
        assertFalse("cancel paychecks", cancelResult.isSuccess());

        assertEquals("Messages size", 1, cancelResult.getMessages().size());
        Message message = cancelResult.getMessages().get(0);
        assertEquals("Error message code", "306", message.getMessageCode());
        assertEquals("Error message", "Prefunding transactions have been recorded for DDTxBatch BatchTest06 for company QBDT:8574536. Please void all prefunding tranasction before canceling any of the employee transactions.",
                message.getMessage());
        Application.commitUnitOfWork();

        // void the dd debit transaction
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("number of debit transactions", 3, debitTransactions.size());
        // void all of the transactions
        for (FinancialTransaction financialTransaction : debitTransactions) {
            assertSuccess("void transaction",
                    PayrollServices.financialTransactionManager.voidTransaction(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                financialTransaction.getId().toString()));
        }
        PayrollServices.commitUnitOfWork();

        // make sure the company was put back on hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertNotNull("company hold", company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        // try to cancel one of the employee transactions
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        TransactionCancelEEDTO = new TransactionCancelEEDTO();
        TransactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        TransactionCancelEEDTO.setAgentCancel(true);
        paycheckIds = new ArrayList<String>();
        TransactionCancelEEDTO.setSourcePaycheckIdList(paycheckIds);
        paycheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        cancelResult = PayrollServices.payrollManager.cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), TransactionCancelEEDTO);
        assertFalse("cancel paychecks", cancelResult.isSuccess());

        assertEquals("Messages size", 1, cancelResult.getMessages().size());
        message = cancelResult.getMessages().get(0);
        assertEquals("Error message code", "307", message.getMessageCode());
        assertEquals("Error message", "A prefunding transaction has been recorded for DDTxBatch BatchTest06 for company QBDT:8574536. All employee transactions must be canceled in order to completely cancel the payroll.",
                message.getMessage());
        Application.commitUnitOfWork();

        // cancel all of the employee transactions
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        TransactionCancelEEDTO = new TransactionCancelEEDTO();
        TransactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        TransactionCancelEEDTO.setAgentCancel(true);
        paycheckIds = new ArrayList<String>();
        TransactionCancelEEDTO.setSourcePaycheckIdList(paycheckIds);
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            paycheckIds.add(paycheck.getSourcePaycheckId());
        }
        cancelResult = PayrollServices.payrollManager.cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), TransactionCancelEEDTO);
        assertSuccess("cancel paychecks", cancelResult);
        Application.commitUnitOfWork();

        // make sure the payroll is now canceled, and all of the tranasctions have been canceled or voided
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll canceled", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.PayrollRun().equalTo(payrollRun).And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled))
                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Voided))));
        assertEquals("non cancelled transactions", 0, financialTransactions.size());
        Application.commitUnitOfWork();
    }

    @Test
    public void test_recall_employee_paychecks_before_offload() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();

        // try to recall one of the employee transactions
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest06");
        List<String> paycheckIds = new ArrayList<String>();
        paycheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        recallDTO.setSourcePaycheckIdList(paycheckIds);
        recallDTO.setRequestId("dummy");
        ProcessResult<TransactionResponse> recallResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), recallDTO);
        assertEquals("Error Count:", 1, recallResult.getMessages().size());
        Message message = recallResult.getMessages().get(0);
        assertEquals("Message Code:", "308", message.getMessageCode());
        assertEquals("Message Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "A prefunding transaction has been recorded for DDTxBatch BatchTest06 for company QBDT:8574536, so no transactions can be recalled.",
                message.getMessage());
        Application.commitUnitOfWork();

        // try to recall the entire payroll
        PayrollServices.beginUnitOfWork();
        recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest06");
        recallDTO.setSourcePaycheckIdList(null);
        recallDTO.setRequestId("dummy");
        recallResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), recallDTO);
        assertEquals("Error Count:", 1, recallResult.getMessages().size());
        message = recallResult.getMessages().get(0);
        assertEquals("Message Code:", "308", message.getMessageCode());
        assertEquals("Message Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "A prefunding transaction has been recorded for DDTxBatch BatchTest06 for company QBDT:8574536, so no transactions can be recalled.",
                message.getMessage());
        Application.commitUnitOfWork();

        // void the dd debit transaction
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("number of debit transactions", 2, debitTransactions.size());
        // void all of the transactions
        for (FinancialTransaction financialTransaction : debitTransactions) {
            assertSuccess("void transaction",
                    PayrollServices.financialTransactionManager.voidTransaction(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                financialTransaction.getId().toString()));
        }
        PayrollServices.commitUnitOfWork();

        // make sure the company was put back on hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertNotNull("company hold", company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        // try to recall one of the employee transactions
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest06");
        paycheckIds = new ArrayList<String>();
        paycheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        recallDTO.setSourcePaycheckIdList(paycheckIds);
        recallDTO.setRequestId("dummy");
        recallResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), recallDTO);
        assertEquals("Error Count:", 1, recallResult.getMessages().size());
        message = recallResult.getMessages().get(0);
        assertEquals("Message Code:", "308", message.getMessageCode());
        assertEquals("Message Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "A prefunding transaction has been recorded for DDTxBatch BatchTest06 for company QBDT:8574536, so no transactions can be recalled.",
                message.getMessage());
        Application.commitUnitOfWork();

        // try to recall the entire payroll
        PayrollServices.beginUnitOfWork();
        recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest06");
        recallDTO.setSourcePaycheckIdList(null);
        recallDTO.setRequestId("dummy");
        recallResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), recallDTO);
        assertEquals("Error Count:", 1, recallResult.getMessages().size());
        message = recallResult.getMessages().get(0);
        assertEquals("Message Code:", "308", message.getMessageCode());
        assertEquals("Message Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals(
                "Message Text:",
                "A prefunding transaction has been recorded for DDTxBatch BatchTest06 for company QBDT:8574536, so no transactions can be recalled.",
                message.getMessage());
        Application.commitUnitOfWork();
    }

    @Test
    public void test_missed_payrolls_while_on_hold_QBDT() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        assertEquals("company source system", SourceSystemCode.QBDT, companyDataloader.getCompany().getSourceSystemCd());

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // try and offload it
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20071002");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20071002");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
        SpcfCalendar newInitDate = SpcfCalendar.createInstance();
        newInitDate.setValues(2007, 10, 3, 7, 0, 0, 0);
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals("fin txn status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            assertEquals("new mmt date", newInitDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate());
        }
        PayrollServices.commitUnitOfWork();

        // more than 10 days out
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071021000000");
        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        process = new ProcessMissedPayrolls();
        process.process("20071021");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        missedTxProcessor = new ProcessMissedACHTransactions();
        notificationMessage = missedTxProcessor.process("20071021");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals("fin txn status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_missed_payrolls_while_on_hold_QBOE() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        assertEquals("company source system", SourceSystemCode.QBOE, companyDataloader.getCompany().getSourceSystemCd());

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = companyDataloader.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-04"));
        submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // try and offload it
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20071002");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20071002");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest01");
        assertEquals("payroll status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO))) {
            assertEquals("fin txn status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_missed_payrolls_while_on_hold_QBOE_wire_complete() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        assertEquals("company source system", SourceSystemCode.QBOE, companyDataloader.getCompany().getSourceSystemCd());

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = companyDataloader.getCompany1PR_ExceedsLimits(new DateDTO("2007-10-02"));
        submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest01");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest01", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // make sure company was not taken off hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertNotNull("company hold", company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        // try to offload the payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070928");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest01");
        assertEquals("payroll status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                assertEquals("fin txn status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_missed_payrolls_while_on_hold_QBDT_wire_complete() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        assertEquals("company source system", SourceSystemCode.QBDT, companyDataloader.getCompany().getSourceSystemCd());

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-02"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // make sure company was not taken off hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertNotNull("company hold", company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        // try to offload the payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20070928");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
        SpcfCalendar newInitDate = SpcfCalendar.createInstance();
        newInitDate.setValues(2007, 10, 1, 7, 0, 0, 0);
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            if(financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployeeDdCredit){
                assertEquals("fin txn status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                assertEquals("new mmt date", newInitDate, financialTransaction.getMoneyMovementTransaction().getInitiationDate());
            }
        }
        PayrollServices.commitUnitOfWork();

        // more than 10 days out
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071021000000");
        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        process = new ProcessMissedPayrolls();
        process.process("20071021");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        missedTxProcessor = new ProcessMissedACHTransactions();
        notificationMessage = missedTxProcessor.process("20071021");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                assertEquals("fin txn status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_HappyPath_with_fee() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);
        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is pending
        assertEquals("payroll status pending", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());

        // offload the ee credits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                case EmployerFeeDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH && financialTransaction.getFinancialTransactionAmount().compareTo(new SpcfMoney("10.00")) == 0){
                        assertEquals("transaction status", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    }
                    break;
            }
        }
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is complete
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        // add a fee
        Calendar feeDate = Calendar.getInstance();
        feeDate.set(2007, 10, 5);
        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06",
                                                 SettlementTypeDTO.ACH, feeDate.getTime(), new SpcfMoney("10.00"),
                                                 OfferingServiceChargeType.PaymentArrangementFee, null);
        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess("add fee", processResult);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> feeDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        assertEquals("new fee transactions", 2, feeDebitTransactions.size());
        PayrollServices.commitUnitOfWork();

        // offload the fee debits
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        feeDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        assertEquals("executed fee transactions", 2, feeDebitTransactions.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void test_recall_employee_paychecks_before_prefunding() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // try to recall one of the employee transactions
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest06");
        List<String> paycheckIds = new ArrayList<String>();
        paycheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        recallDTO.setSourcePaycheckIdList(paycheckIds);
        recallDTO.setRequestId("dummy");
        ProcessResult<TransactionResponse> recallResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), recallDTO);
        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("recall paycheck", recallResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", false, company.isCompanyHold(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        int eeCreditCreatedCount = 0;
        int eeCreditCanceledCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    if(financialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created){
                        eeCreditCreatedCount++;
                    }
                    else {
                        eeCreditCanceledCount++;
                    }
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 2, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 4, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        assertEquals("number of ee credit created transactions", 1, eeCreditCreatedCount);
        assertEquals("number of ee credit canceled transactions", 1, eeCreditCanceledCount);
        PayrollServices.commitUnitOfWork();

        // offload the ee credits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> eeCreditTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        assertEquals("employee credits offloaded", 1, eeCreditTransactions.size());
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is complete
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        DataLoadServices.runACHTransactionProcessor();

        // check ee fin transctions to make sure they are now complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        eeCreditTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("employee credits complete", 1, eeCreditTransactions.size());
        PayrollServices.commitUnitOfWork();

    }


    @Test
    public void test_recall_employee_paychecks_from_a_seperate_payroll() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        int eeCreditCreatedCount = 0;
        int eeCreditCanceledCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    if(financialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created){
                        eeCreditCreatedCount++;
                    }
                    else {
                        eeCreditCanceledCount++;
                    }
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        assertEquals("number of ee credit created transactions", 2, eeCreditCreatedCount);
        assertEquals("number of ee credit canceled transactions", 0, eeCreditCanceledCount);
        PayrollServices.commitUnitOfWork();

        // try to recall one of the employee transactions
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest87");
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("BatchTest87");
        List<String> paycheckIds = new ArrayList<String>();
        paycheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        recallDTO.setSourcePaycheckIdList(paycheckIds);
        recallDTO.setRequestId("dummy");
        ProcessResult<TransactionResponse> recallResult = PayrollServices.payrollManager
                .cancelEmployeeTransaction(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), recallDTO);
        Application.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("recall paycheck", recallResult);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.removeOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("hold removed", result);

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload the ee credits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> eeCreditTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        assertEquals("employee credits offloaded", 2, eeCreditTransactions.size());
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runACHTransactionProcessor();

        // check ee fin transctions to make sure they are now complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        eeCreditTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("employee credits complete", 2, eeCreditTransactions.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest87");
        eeCreditTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("employee credits complete", 1, eeCreditTransactions.size());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void test_prefund_with_ee_reject() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                                                                                companyDataloader.getCompany().getSourceCompanyId(),
                                                                                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);
        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is pending
        assertEquals("payroll status pending", PayrollStatus.Pending, payrollRun.getPayrollRunStatus());

        // offload the ee credits
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
            }
        }
        PayrollServices.commitUnitOfWork();

        // make sure the payroll status for the second payroll is complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        //Process Missed Payrolls
        PayrollServices.beginUnitOfWork();
        ProcessMissedPayrolls process = new ProcessMissedPayrolls();
        process.process("20071002");
        PayrollServices.commitUnitOfWork();

        // Process missed transctions
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20071002");
        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

        // check the status of the payroll
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        assertEquals("payroll status complete", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        // return one of the employee transactions
        Application.beginUnitOfWork();
        Employee employee1 = Employee.findEmployee(companyDataloader.getCompany(), "EE1_1");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findEmployeeFinancialTransactions(company, "BatchTest06", employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        PSPDate.setPSPTime("20071005000000");
        Assert.assertEquals("Number of C1 EEDDCR txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R02", "This is a non-NSF description");

        DataLoadServices.runOffload();

        DataLoadServices.runACHTransactionProcessor();

        // check ee fin transctions to make sure they are now complete
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("er ach debit", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    }
                    else {
                        assertEquals("er non-ach debit", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("er ach fee debit", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    }
                    else {
                        assertEquals("er non-ach fee debit", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    }
                    break;
                case EmployerDdRejectRefundCredit:
                    assertEquals("refund credit", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                case EmployeeDdCredit:
                    if(!(financialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Completed ||
                            financialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Returned)){
                        fail("employee credit in wrong state");
                    }
                    break;
            }
        }
        PayrollServices.commitUnitOfWork();

    }

    //----- utils -----

    private PrefundPayrollTransactionDTO getPrefundPayrollTransactionDTOFromFinancialTransactions(FinancialTransaction financialTransaction) {
        PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = new PrefundPayrollTransactionDTO();

        prefundPayrollTransactionDTO.setOriginalTransactionId(financialTransaction.getId().toString());
        prefundPayrollTransactionDTO.setTransactionAmount(financialTransaction.getFinancialTransactionAmount());

        return prefundPayrollTransactionDTO;
    }

    private ArrayList<PrefundPayrollTransactionDTO> findDebitTransactions(Company company, String sourcePayrollRunId) {
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = new ArrayList<PrefundPayrollTransactionDTO>();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);
        DomainEntitySet<FinancialTransaction> erDdDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction erDdDebitTransaction : erDdDebitTransactions) {
            transactionDTOs.add(getPrefundPayrollTransactionDTOFromFinancialTransactions(erDdDebitTransaction));
        }

        Map<BillingDetail,  PrefundPayrollTransactionDTO> billingDetailMap = new HashMap<BillingDetail, PrefundPayrollTransactionDTO>();
        DomainEntitySet<FinancialTransaction> feeDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction feeTransaction : feeDebitTransactions) {
            BillingDetail billingDetail = feeTransaction.getBillingDetail();
            PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = getPrefundPayrollTransactionDTOFromFinancialTransactions(feeTransaction);
            transactionDTOs.add(prefundPayrollTransactionDTO);
            billingDetailMap.put(billingDetail,  prefundPayrollTransactionDTO);
        }

        DomainEntitySet<FinancialTransaction> taxDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        for (FinancialTransaction taxTransaction : taxDebitTransactions) {
            BillingDetail billingDetail = taxTransaction.getBillingDetail();
            PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = billingDetailMap.get(billingDetail);
            if(prefundPayrollTransactionDTO != null){
                prefundPayrollTransactionDTO.setOriginalTaxTransactionId(taxTransaction.getId().toString());
                prefundPayrollTransactionDTO.setTaxTransactionAmount(taxTransaction.getFinancialTransactionAmount());
            }
        }

        return transactionDTOs;
    }

    private ArrayList<PrefundPayrollTransactionDTO> findCanceledDebitTransactions(Company company, String sourcePayrollRunId) {
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = new ArrayList<PrefundPayrollTransactionDTO>();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);
        DomainEntitySet<FinancialTransaction> erDdDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        for (FinancialTransaction erDdDebitTransaction : erDdDebitTransactions) {
            transactionDTOs.add(getPrefundPayrollTransactionDTOFromFinancialTransactions(erDdDebitTransaction));
        }

        Map<BillingDetail,  PrefundPayrollTransactionDTO> billingDetailMap = new HashMap<BillingDetail, PrefundPayrollTransactionDTO>();
        DomainEntitySet<FinancialTransaction> feeDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        for (FinancialTransaction feeTransaction : feeDebitTransactions) {
            BillingDetail billingDetail = feeTransaction.getBillingDetail();
            PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = getPrefundPayrollTransactionDTOFromFinancialTransactions(feeTransaction);
            transactionDTOs.add(prefundPayrollTransactionDTO);
            billingDetailMap.put(billingDetail,  prefundPayrollTransactionDTO);
        }

        DomainEntitySet<FinancialTransaction> taxDebitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});
        for (FinancialTransaction taxTransaction : taxDebitTransactions) {
            BillingDetail billingDetail = taxTransaction.getBillingDetail();
            PrefundPayrollTransactionDTO prefundPayrollTransactionDTO = billingDetailMap.get(billingDetail);
            if(prefundPayrollTransactionDTO != null){
                prefundPayrollTransactionDTO.setOriginalTaxTransactionId(taxTransaction.getId().toString());
                prefundPayrollTransactionDTO.setTaxTransactionAmount(taxTransaction.getFinancialTransactionAmount());
            }
        }

        return transactionDTOs;
    }

    private Company3Dataloader createQBDTCompanyAndSubmitFirstPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company3Dataloader companyDataloader = new Company3Dataloader();
        companyDataloader.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = companyDataloader.getCompany();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company);
        dtoUpdate.setTaxExemptExpirationDate(null);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), dtoUpdate);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Updating company for tax-exempt-expiration and legal address", prUpdate);

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        return companyDataloader;
    }

    @Test
    public void test_prefund_void_prefund_prefund_again() {
        Company3Dataloader companyDataloader = createQBDTCompanyAndSubmitFirstPayroll();

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // place the company on hold
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.PendingPrefundingWire);
        PayrollServices.commitUnitOfWork();
        assertSuccess("placed on hold", result);

        // submit a second payroll over the dd limit
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany3PR_ExceedsLimitsQualifiesForIncrease(new DateDTO("2007-10-04"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // find the debit transactions
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar settlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(settlementDate);

        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, settlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
         PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                                                                    companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        // check debits to make sure the ach debits were canceled and the non-ach ones are complete
        int erDdDebitAchCount = 0;
        int erDdDebitNonAchCount = 0;
        int erFeeDebitAchCount = 0;
        int erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        assertEquals("transaction status", TransactionStateCode.Completed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 1, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 2, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();

        // void the dd debit transaction
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Completed});
        assertEquals("number of debit transactions", 3, debitTransactions.size());
        // void all of the transactions
        for (FinancialTransaction financialTransaction : debitTransactions) {
            assertSuccess("void transaction",
                    PayrollServices.financialTransactionManager.voidTransaction(companyDataloader.getCompany().getSourceSystemCd(),
                            companyDataloader.getCompany().getSourceCompanyId(),
                            financialTransaction.getId().toString()));
        }
        PayrollServices.commitUnitOfWork();

        // make sure the company was put back on hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                companyDataloader.getCompany().getSourceSystemCd());
        assertNotNull("company hold", company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        // re-add the prefunding transactions
        PayrollServices.beginUnitOfWork();
        transactionDTOs = findCanceledDebitTransactions(companyDataloader.getCompany(), "BatchTest06");
        SpcfCalendar prefundSettlementDate = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(prefundSettlementDate);

        prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), "BatchTest06", SettlementType.Wire, prefundSettlementDate, transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);

        PayrollServices.commitUnitOfWork();

        // check if the company was taken off of hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(companyDataloader.getCompany().getSourceCompanyId(),
                companyDataloader.getCompany().getSourceSystemCd());
        assertEquals("company hold", null, company.getCurrentOnHoldReason(ServiceSubStatusCode.PendingPrefundingWire));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(companyDataloader.getCompany(), "BatchTest06");
        erDdDebitAchCount = 0;
        erDdDebitNonAchCount = 0;
        erFeeDebitAchCount = 0;
        erFeeDebitNonAchCount = 0;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            switch (financialTransaction.getTransactionType().getTransactionTypeCd()){
                case EmployerDdDebit:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erDdDebitAchCount++;
                    }
                    else {
                        erDdDebitNonAchCount++;
                    }
                    break;
                case EmployerFeeDebit:
                case ServiceSalesAndUseTax:
                    if(financialTransaction.getSettlementTypeCd() == SettlementType.ACH){
                        assertEquals("transaction status", TransactionStateCode.Cancelled, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                        erFeeDebitAchCount++;
                    }
                    else {
                        erFeeDebitNonAchCount++;
                    }
                    break;
                case EmployeeDdCredit:
                    assertEquals("transaction status", TransactionStateCode.Created, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
                    break;
                default:
                    fail("Unexpected transaction type");
            }
        }
        assertEquals("number of dd debit ach transactions", 1, erDdDebitAchCount);
        assertEquals("number of dd debit non-ach transactions", 2, erDdDebitNonAchCount);
        assertEquals("number of fee debit ach transactions", 2, erFeeDebitAchCount);
        assertEquals("number of fee debit non-ach transactions", 4, erFeeDebitNonAchCount);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInitiationDateNotChanged (){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20130603000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        Company company = companyDataloader.getCompany();
       // PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        // Set Date for payroll that will have PrefundPayroll
        DataLoadServices.setPSPDate(2013,7,8);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,7,8,8,42,0,0,SpcfTimeZone.getLocalTimeZone()));
        // Begin UOW to create a payroll run with CheckDate = 7/10/13
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO ddPayrollRun = DataLoadServices.createDDPayrollRun(company, new DateDTO("2013-07-10"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), ddPayrollRun);
        assertSuccess(submitPayrollResult);
        PayrollServices.commitUnitOfWork();

        // Add OnHold reason for PendingPrefundingWire
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.PendingPrefundingWire);

        // Begin UOW to find PreFundPayroll transactions

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,7,8,23,16,0,0));
        // PSPDate.setPSPTime("20130708163000");
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(company, ddPayrollRun.getPayrollTXBatchId());
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                                                      ddPayrollRun.getPayrollTXBatchId(), SettlementType.Wire,
                                                                                                      SpcfCalendar.createInstance(2013, 7, 8, SpcfTimeZone.getLocalTimeZone()),
                                                                                                      transactionDTOs);
        assertSuccess("prefunding", prefundPayrollCore);
        PayrollServices.commitUnitOfWork();

        // Remove OnHold reason  - Hold is being removed by the PrefundWire received.
        //DataLoadServices.removeCompanyOnHoldReasons(company);

        // Begin UOW
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        PayrollRun payrollRun = PayrollRun.findLatestCompanyPayrollRun(company);
        FinancialTransaction eeCredit = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType()
                                                                                                              .TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeDdCredit)
                                                                                                              .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created))).getFirst();
        Assert.assertEquals(SpcfCalendar.createInstance(2013,7,8,SpcfTimeZone.getLocalTimeZone()),eeCredit.getMoneyMovementTransaction().getInitiationDate().toLocal());

    }

    @Test
    public void testInitiationDateAfterCutoff (){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20130603000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        Company company = companyDataloader.getCompany();
        // PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        // Set Date for payroll that will have PrefundPayroll
        DataLoadServices.setPSPDate(2013,7,8);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,7,8,8,42,0,0,SpcfTimeZone.getLocalTimeZone()));
        // Begin UOW to create a payroll run with CheckDate = 7/10/13
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO ddPayrollRun = DataLoadServices.createDDPayrollRun(company, new DateDTO("2013-07-10"));
        ProcessResult submitPayrollResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), ddPayrollRun);
        assertSuccess(submitPayrollResult);
        PayrollServices.commitUnitOfWork();

        // Add OnHold reason for PendingPrefundingWire
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.PendingPrefundingWire);

        // Begin UOW to find PreFundPayroll transactions

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,7,8,17,17,20,0, SpcfTimeZone.getLocalTimeZone()));
        // PSPDate.setPSPTime("20130708163000");
        PayrollServices.beginUnitOfWork();
        ArrayList<PrefundPayrollTransactionDTO> transactionDTOs = findDebitTransactions(company, ddPayrollRun.getPayrollTXBatchId());
        ProcessResult prefundPayrollCore = PayrollServices.financialTransactionManager.prefundPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                                                      ddPayrollRun.getPayrollTXBatchId(), SettlementType.Wire,
                                                                                                      SpcfCalendar.createInstance(2013, 7, 8, SpcfTimeZone.getLocalTimeZone()),
                                                                                                      transactionDTOs);
        assertFalse("prefunding", prefundPayrollCore.isSuccess());
        PayrollServices.commitUnitOfWork();




    }
}
