package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionReverseDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.util.EmailUtils;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 27, 2008
 * Time: 5:45:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectDepositReversalReturnTests {

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
    public void testDDReversalReturnForDD4V() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        String sourceCompanyId = "123272727";
        // 1. company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        //2. Set up bill payments with 2 Payees
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        BillPaymentDTO billPaymentDTO2 = GenerateData.generateBillPayment("Payee2", new DateDTO("2007-09-10"),1);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        billPaymentDTOs.add(billPaymentDTO2);
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        Collection<PayrollRun> billPaymentResults = submitResult.getResult() ;
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived);
        assertEquals("BillPaymentReceived event count", 1, events.size());
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        // 3. offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // 4. Return the offloaded transactions as Non Sufficient Funds
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun1 = billPaymentResults.toArray(new PayrollRun[billPaymentResults.size()])[0];
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRun1.getSourcePayRunId());
        DomainEntitySet<FinancialTransaction> offLoadedTransactions = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[] {TransactionStateCode.Executed});
        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070911000000");
        PayrollServices.commitUnitOfWork();
        Application.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        //loader.c1dl.reverseEntirePayroll(payrollRun.getSourcePayRunId());

        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult result = PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), txnReverseDTO);
        assertSuccess(result);
        Application.commitUnitOfWork();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                        .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                                   TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Executed);
        c1FinTxns.addAll(FinancialTransaction
                        .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                                                   TransactionTypeCode.EmployeeDdReversalDebit, TransactionStateCode.Completed));

        DomainEntitySet<TransactionReturn> returnList = loader.persistTransactionReturns(c1FinTxns, "R01", "NSF Return");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 3, returnList.size());
        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        txnReturn = handler.execute(txnReturn);
        Application.commitUnitOfWork();
    }


    @Test
    public void testDDReversalReturn() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDDReversalReturn();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 2, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        txnReturn = handler.execute(txnReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn);
        FinancialTransaction reversalFT = returnedFTs.get(0);
        Application.commitUnitOfWork();

        // make sure all FTs are Returned
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }

        Application.beginUnitOfWork();

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(reversalFT.getCompany(),
                EventTypeCode.ReversalReturn,
                CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ReversalReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        // make sure it got Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());
        Application.commitUnitOfWork();        
    }

    @Test
    public void testDDReversalReturnNonNSF() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDDReversalReturnNonNSF();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 2, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        txnReturn = handler.execute(txnReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn);
        FinancialTransaction reversalFT = returnedFTs.get(0);
        Application.commitUnitOfWork();

        // make sure all FTs are Returned
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }

        Application.beginUnitOfWork();

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(reversalFT.getCompany(),
                EventTypeCode.ReversalReturn,
                CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ReversalReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        // make sure it got Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());
        Application.commitUnitOfWork();
    }


    /**
     * A company on 2 day has its entire payroll offloaded, then the entire payroll is reversed, and then the batch
     * job completes it.  Then, an untimely reversal return is received
     */
    @Test
    public void testDDReversalReturnComplete() {
        //Load data and do reversals
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        loader.loadDataForDDReversals2Day();
        PayrollServices.commitUnitOfWork();

        //Complete the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071020000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        processACHTxns.process(PSPDate.getPSPTime());
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        PayrollServices.commitUnitOfWork();
        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        //Process returns on the reversals
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20071130000000");
        DomainEntitySet<TransactionReturn> reversalReturns = loader.loadDDReversalReturn("R01", "NSF Return");
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        // make sure they're all Returned
        for (FinancialTransaction financialTransaction : allReturnedFTs) {
            //Assertion for Update FinancialTransactionRule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }

        Application.beginUnitOfWork();
        company = Application.refresh(company);
        payrollRun = Application.refresh(payrollRun);
        boolean bIsCompanyOnHold = company.isCompanyOnHold();
        DomainEntitySet<TransactionReturn> txnRets = TransactionReturn.findTransactionReturns(company);
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ReversalReturn, CompanyEventStatus.Active, null, null);

        // make sure the right events got created
        assertEquals("Company Events", 2, companyEventsList.size());
        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ReversalReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }

        for (TransactionReturn txnRet : txnRets) {
            //Assertion for Update TransactionReturn Status Rule
            assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                    txnRet.getReturnStatusCd());
        }

        Application.commitUnitOfWork();
        // reversal was customer-initiated, so payroll status should not have changed
        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
    }

    /**
     * A company on 2 day has its entire payroll offloaded, gets a return on one of the ee dd reversals, then the entire payroll is written off.
     * Then, an untimely reversal return is received
     */
    @Test
    public void testDDReversalReturnWrittenOff() {
        //Load data and agent inits reversals
        ACHReturnsDataLoader.loadData2DayERNSFsAgentReversesPayroll();

        OffloadACHTransactions offloader2 = new OffloadACHTransactions();
        offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Process returns on 1 reversals
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20071030000000");
        DomainEntitySet<TransactionReturn> reversalReturns = ACHReturnsDataLoader.loadFirstDDReversalReturn();
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        //Write off the remaining balance on the payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult abdWOProc = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(SourceSystemCode.QBOE, "1234567", "BatchTest05");
        PayrollServices.commitUnitOfWork();
        assertSuccess(abdWOProc);

        //Process untimely return on the remaining reversal
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071130000000");
        DomainEntitySet<TransactionReturn> reversalReturns2 = ACHReturnsDataLoader.load2ndDDReversalReturn();
        for (TransactionReturn transactionReturn : reversalReturns2) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        for (FinancialTransaction financialTransaction : allReturnedFTs) {
            //Assertion for Update FinancialTransactionRule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }

        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        boolean bIsCompanyOnHold = company.isCompanyOnHold();
        DomainEntitySet<TransactionReturn> txnRets = TransactionReturn.findTransactionReturns(company);
        //Only get events created after NSF was created
        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ReversalReturn, CompanyEventStatus.Active, SpcfCalendar.createInstance(2007, 9, 7, SpcfTimeZone.getLocalTimeZone()), null);
        Application.commitUnitOfWork();

        //Assertion for Create Direct Deposit Reversal Return System Event rule
        assertEquals("Company Events", 2, companyEventsList.size());

        for (TransactionReturn txnRet : txnRets) {
            //Assertion for Update TransactionReturn Status Rule
            assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                    txnRet.getReturnStatusCd());
        }

        assertEquals("Payroll Run Status", PayrollStatus.ReversalsFinished, payrollRun.getPayrollRunStatus());
        assertTrue("Company is on hold", bIsCompanyOnHold);
    }

    @Test
    public void testDDReversal_ee1() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();

        Application.beginUnitOfWork();
        loader.persistCompany1On2Day_1ee2PaycheckSplits();
        Application.commitUnitOfWork();
        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Reverse single paycheck split from paycheck having two paycheck splits.
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();
        loader.c1dl.reverseSingleTransactionInPayroll("BatchTest05", "EEBA1PS1");

        SpcfMoney amount = new SpcfMoney("30.00");
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findAllFinancialTransaction(loader.c1dl.getCompany(), TransactionTypeCode.EmployeeDdReversalDebit);
        assertEquals("EmployeeDdReversalDebit FTs", 1, financialTransactions.size());
        assertEquals("EmployeeDdReversalDebit FT Amount", amount, financialTransactions.get(0).getFinancialTransactionAmount());

        DomainEntitySet<CompanyEventEmail> companyEventEmails = CompanyEventEmail.findEmailEventsByTemplateAndStatus(EventEmailStatus.Pending, EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1);
        assertEquals("CompanyEventEmail records", 1, companyEventEmails.size());
        DomainEntitySet<CompanyEventEmailParam> companyEventEmailParams = companyEventEmails.get(0).getEmailParamForEmailEvent(EventEmailParamTypeCode.ReversalPendingList);
        assertEquals("CompanyEventEmailParam with ReversalPendingLists", 1, companyEventEmailParams.size());

        String emailString = "&#8217;s direct deposit in the amount "+EmailUtils.formatMoney(amount)+" will be reversed<br>";
        assertTrue("Amount in Email param", companyEventEmailParams.get(0).getValue().indexOf(emailString) > 0);       

    }

    @Test
    public void testQBDTSymphonyDDReversalReturn() {
        String sourceCompanyId = "1234567";

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDDReversalReturn(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 2, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        txnReturn = handler.execute(txnReturn);
        Application.commitUnitOfWork();

        // Validation
        Application.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(txnReturn);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyReversalReturnCompanyEvent(company, 1);

        transactionReturnVerifier.verifyTransactionReturnStatus(txnReturn, TransactionReturnStatusCode.Resolved);

        Application.rollbackUnitOfWork();
    }

    /**
     * A company on 2 day has its entire payroll offloaded, then the entire payroll is reversed, and then the batch
     * job completes it.  Then, an untimely reversal return is received
     */
    @Test
    public void testQBDTSymphonyDDReversalReturnComplete() {
        String sourceCompanyId = "1234567";
        String sourcePayrollRunId = "BatchTest05";

        //Load data and do reversals
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        loader.loadDataForDDReversals2Day(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        //Complete the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071020000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        processACHTxns.process(PSPDate.getPSPTime());
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);
        PayrollServices.commitUnitOfWork();
        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        //Process returns on the reversals
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> allReturnedFTs = new DomainEntitySet<FinancialTransaction>();
        PSPDate.setPSPTime("20071130000000");
        DomainEntitySet<TransactionReturn> reversalReturns = loader.loadDDReversalReturn(SourceSystemCode.QBDT, sourceCompanyId,"R01", "NSF Return");
        for (TransactionReturn transactionReturn : reversalReturns) {
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
            returnHandler.execute(transactionReturn);
            allReturnedFTs.addAll(TransactionReturn.findFinancialTransaction(transactionReturn));
        }
        Application.commitUnitOfWork();

        // Validation
        Application.beginUnitOfWork();
        company = Application.refresh(company);
        payrollRun = Application.refresh(payrollRun);

        DomainEntitySet<TransactionReturn> txnRets = TransactionReturn.findTransactionReturns(company);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyFinancialTransactionReturn(allReturnedFTs, false);

        transactionReturnVerifier.verifyReversalReturnCompanyEvent(company, 2);

        for (TransactionReturn txnRet : txnRets) {
            transactionReturnVerifier.verifyTransactionReturnStatus(txnRet, TransactionReturnStatusCode.Resolved);
        }

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.Complete);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyDDReversalSinglePaycheckSplit() {
        String sourceCompanyId = "1234567";

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();

        Application.beginUnitOfWork();
        loader.persistCompany1On2Day_1ee2PaycheckSplits(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        Application.commitUnitOfWork();
        //Offload the payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(Codes.PSPOFFLOADS, null);

        //Reverse single paycheck split from paycheck having two paycheck splits.
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();
        loader.c1dl.reverseSingleTransactionInPayroll(SourceSystemCode.QBDT, "BatchTest05", "EEBA1PS1");


        PayrollServices.beginUnitOfWork();

        SpcfMoney reversalAmount = new SpcfMoney("30.00");

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyEmployeeDdReversalDebitFinancialTransaction(company);

        transactionReturnVerifier.verifyEmployeeDdReversalDebitCompanyEmailEvent(reversalAmount);

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testQBDTSymphonyDDReversalReturnNonNSF() {
        String sourceCompanyId = "1234567";

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDDReversalReturnNonNSF(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 2, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        txnReturn = handler.execute(txnReturn);
        Application.commitUnitOfWork();

        // Validation
        Application.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(txnReturn);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyFinancialTransactionReturn(returnedFinancialTransactions, false);

        transactionReturnVerifier.verifyReversalReturnCompanyEvent(company, 1);

        transactionReturnVerifier.verifyTransactionReturnStatus(txnReturn, TransactionReturnStatusCode.Resolved);

        Application.rollbackUnitOfWork();
    }
}
