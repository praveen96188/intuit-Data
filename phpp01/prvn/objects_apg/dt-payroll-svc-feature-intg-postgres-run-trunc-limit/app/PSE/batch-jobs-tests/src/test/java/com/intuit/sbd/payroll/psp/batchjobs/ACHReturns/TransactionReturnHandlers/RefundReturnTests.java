package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 28, 2008
 * Time: 10:41:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class RefundReturnTests {



    @AfterClass
    public static void afterClass() {

        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011,1,1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testRefundReturn() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForRefundReturnEvent();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);
        FinancialTransaction firstReturnedFT = returnedFTs.get(0);
        Application.commitUnitOfWork();

        // make sure it's Returned
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }


        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(firstReturnedFT.getCompany(),
                                                                      EventTypeCode.ERRefundReturn,
                                                                      CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ERRefundReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        Application.commitUnitOfWork();
    }

    @Test
    public void testRefundReturnNSF() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNSFRefundReturnEvent();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);
        FinancialTransaction firstReturnedFT = returnedFTs.get(0);
        Application.commitUnitOfWork();

        // make sure it's Returned
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }


        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(firstReturnedFT.getCompany(),
                                                                      EventTypeCode.ERRefundReturn,
                                                                      CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ERRefundReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        Application.commitUnitOfWork();
    }

    @Test
    public void testRefundRebillNetDebit() {
        ACHReturnsDataLoader.loadRefundRebillNetDebit();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest09", null, null, null, null, null, null, TransactionStateCode.Returned);
        assertEquals("4 returned transactions", 4, c1FinTxns.size());
        for (FinancialTransaction currTxn : c1FinTxns) {
            payrollRun = currTxn.getPayrollRun();
            company=currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                         TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            if (currTxn.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeRefundCredit
                    || currTxn.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit) {
                assertEquals("Transaction response for Returned "+currTxn.getTransactionType().getTransactionTypeCd()+" transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned "+currTxn.getTransactionType().getTransactionTypeCd()+" transaction", 1, responses.size());
            }
        }
        PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus(); 
        PayrollServices.commitUnitOfWork();

        assertEquals("Payroll status", PayrollStatus.DebitReturned, payrollStatus);
        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                                                                      EventTypeCode.DDDebitReturn,
                                                                      CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));


        DomainEntitySet<TransactionReturn> txnReturn = TransactionReturn.findTransactionReturns(payrollRun.getSourcePayRunId(), company);

        assertEquals("There is one txn return", 1, txnReturn.size());

        // make sure the TransactionReturn isn't Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, txnReturn.get(0).getReturnStatusCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testRefundRebillNetCredit() {
        ACHReturnsDataLoader.loadRefundRebillNetCredit();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest09", null, null, null, null, null, null, TransactionStateCode.Returned);
        assertEquals("4 returned transactions", 4, c1FinTxns.size());
        for (FinancialTransaction currTxn : c1FinTxns) {
            payrollRun = currTxn.getPayrollRun();
            company=currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                         TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            if (currTxn.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeRefundCredit
                    || currTxn.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit) {
                assertEquals("Transaction response for Returned "+currTxn.getTransactionType().getTransactionTypeCd()+" transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned "+currTxn.getTransactionType().getTransactionTypeCd()+" transaction", 1, responses.size());
            }
        }
        PayrollStatus payrollStatus = payrollRun.getPayrollRunStatus();
        PayrollServices.commitUnitOfWork();

        assertEquals("Payroll status", PayrollStatus.OffloadedAll, payrollStatus);
        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                                                                      EventTypeCode.ERRefundReturn,
                                                                      CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ERRefundReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        Application.commitUnitOfWork();
        
        Application.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txnReturn = TransactionReturn.findTransactionReturns(payrollRun.getSourcePayRunId(), company);
        Application.commitUnitOfWork();

        assertEquals("There is one txn return", 1, txnReturn.size());

        // make sure the TransactionReturn isn't Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, txnReturn.get(0).getReturnStatusCd());
    }

    @Test
    public void testTaxRefundTest() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO("1");
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "1", payrollDTO);
        PayrollRun payrollRun = (PayrollRun) processResult.getResult();
        Company company = payrollRun.getCompany();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> paychecksToVoid = new ArrayList<String>();
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            paychecksToVoid.add(paycheck.getSourcePaycheckId());
        }
        voidPayrollDTO.setPaycheckIdList(paychecksToVoid);
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("void remove money from ERPayable",
                LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable), new SpcfMoney("189.00"));
        PayrollServices.commitUnitOfWork();

        //create a refund
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.refundERPayable(company.getSourceSystemCd(), company.getSourceCompanyId(), SettlementTypeDTO.ACH, new SpcfMoney("189.00")));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("refund creates refundable money in ERPayable",
                LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable), SpcfMoney.ZERO);
        PayrollServices.commitUnitOfWork();

        // offload the refund
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110106080000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // return the refund
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110108010000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erRefundTxns = company.getFinancialTransactions().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCredit));
        FinancialTransaction erReturnTxn = assertOne(erRefundTxns);

        TransactionReturn transactionReturn = assertOne(ACHReturnsDataLoader.persistTransactionReturns(erRefundTxns, "R02", "Return desc"));

        TransactionReturnHandler.getTransactionReturnHandler(transactionReturn).execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ERRefundReturn,
                CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.ERRefundReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(erReturnTxn);
        assertEquals("Transaction response for Returned EmployerTaxDebit transaction", 1, responses.size());
        Application.commitUnitOfWork();

        //check transfer
        Application.beginUnitOfWork();
        FinancialTransaction transferTransaction = assertOne(company.getFinancialTransactions().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxCreditReturnedTransfer)));
        assertEquals(new SpcfMoney("189.00"), transferTransaction.getFinancialTransactionAmount());
        DataLoadServices.assertIntuitBankAccounts(transferTransaction, DataLoadServices.IntuitBankAccountType.ER_Return, DataLoadServices.IntuitBankAccountType.Tax);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyRefundReturnNSF() {
        String sourceCompanyId = "8574536";

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();

        transactionReturnHelper.updateSourcePayrollParameterFundingModel(FundingModel.Codes.FIVE_DAY);

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNSFRefundReturnEvent(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        Application.refresh(transactionReturn);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        handler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();


        // Validation

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFTs, false);

        transactionReturnVerifier.verifyERRefundReturnCompanyEvent(company);

        Application.rollbackUnitOfWork();

        transactionReturnHelper.updateSourcePayrollParameterFundingModel(FundingModel.Codes.TWO_DAY);
    }

    @Test
    public void testQBDTSymphonyRefundRebillNetDebit() {

        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";

        ACHReturnsDataLoader.loadRefundRebillNetDebit(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));

        // Validation
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest09", null, null, null, null, null, null, TransactionStateCode.Returned);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyRefundFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.DebitReturned);

        transactionReturnVerifier.verifyDDDebitReturnCompanyEvent(company, returnedFinancialTransactions, null, false);

        transactionReturnVerifier.verifyTransactionReturn(company, payrollRun, TransactionReturnStatusCode.Open);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyRefundRebillNetCredit() {
        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";

        ACHReturnsDataLoader.loadRefundRebillNetCredit(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = FinancialTransaction
                .findFinancialTransactions(company, sourcePayrollRunId, null, null, null, null, null, null, TransactionStateCode.Returned);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyRefundFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.OffloadedAll);

        transactionReturnVerifier.verifyERRefundReturnCompanyEvent(company);

        transactionReturnVerifier.verifyTransactionReturn(company, payrollRun, TransactionReturnStatusCode.Open);

        PayrollServices.rollbackUnitOfWork();

    }
}
