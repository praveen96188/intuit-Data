package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.hibernate.FlushMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 28, 2008
 * Time: 10:58:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenericDebitReturnTests {


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
        PayrollServices.rollbackUnitOfWork();
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testQBOESecondNonNSFReturn_ZeroBalance() {

        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadQBOEPayrollRetAddRedebitAddWireRedebitRetNonNSF("R02", "NonNSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned transactions
        PayrollRun payrollRun = null;

        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest05", null, null, null, null, null, null, TransactionStateCode.Returned).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        assertEquals("number returned transactions", 2, c1FinTxns.size());
        for (FinancialTransaction currTxn : c1FinTxns) {
            payrollRun = currTxn.getPayrollRun();
            company = currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            if (TransactionType.isRedebitTransactionType(currTxn.getTransactionType().getTransactionTypeCd())) {
                assertEquals("Transaction response for Returned REDEBIT transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned transaction", 1, responses.size());
            }
        }

        // payroll run status must be Complete
        assertEquals("PayrollRun status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = PayrollServices.entityFinder.findById(TransactionReturn.class, returnedTxn.getId());
        Application.commitUnitOfWork();

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());
        Application.commitUnitOfWork();
    }

    private DomainEntitySet<OnHoldReason> getNonExpiredOnHoldReasonList(Company company) {
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());

        return Application.find(OnHoldReason.class, query);
    }

    @Test
    public void testQBDTSecondGenericReturn_ZeroBalance() {
        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadQBDTPayrollRetAddRedebitAddWireRedebitRetNonNSF();

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned transactions
        PayrollRun payrollRun = null;

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest09", null, null, null, null, null, null, TransactionStateCode.Returned);

        assertEquals("number returned transactions", 6, c1FinTxns.size());

        for (FinancialTransaction currTxn : c1FinTxns) {
            payrollRun = currTxn.getPayrollRun();
            company = currTxn.getCompany();

            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());

            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);

            if (TransactionType.isRedebitTransactionType(currTxn.getTransactionType().getTransactionTypeCd())) {
                assertEquals("Transaction response for Returned REDEBIT transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned transaction", 1, responses.size());
            }
        }

        // payroll run status must be Complete
        assertEquals("PayrollRun status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = PayrollServices.entityFinder.findById(TransactionReturn.class, returnedTxn.getId());
        Application.commitUnitOfWork();

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());
        Application.commitUnitOfWork();
    }

    @Test
    public void testGenericDebitReturnsOtherThanOffloadedDebit() {
        ACHReturnsDataLoader.loadData2Day1EERet();

        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                                           TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20070913000000");
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns, "R02",
                "This is a non-NSF description");
        Application.commitUnitOfWork();

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Application.commitUnitOfWork();

        assertEquals("Number of transaction returns", 1, returnList.size());
        TransactionReturn transactionReturn = returnList.get(0);
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        FinancialTransaction returnedDdFT = returnedFTs.get(0);
        String returnedDdFTId = returnedDdFT.getId().toString();
        PayrollStatus associatedPayRunStatus = returnedDdFT.getPayrollRun().getPayrollRunStatus();
        Application.commitUnitOfWork();
        assertEquals("Returned FTs", 1, returnedFTs.size()); // 1 ER DD Debit

        FinancialTransaction erDDDB = returnedFTs.get(0);
        // make sure each returned FT is Returned, and included in a TransactionResponse
        DomainEntitySet<TransactionResponse> responses;

        assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                erDDDB.getCurrentTransactionState().getTransactionStateCd());
        Application.beginUnitOfWork();
        responses = TransactionResponse.findTransactionResponses(erDDDB);
        Application.commitUnitOfWork();
        assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());


        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.DebitReturned, associatedPayRunStatus);

        // make sure refund FTs were cancelled
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> rejectRefundCreditFTs;
        rejectRefundCreditFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBOE,
                "1234567",
                TransactionTypeCode.EmployerDdRejectRefundCredit, TransactionStateCode.Cancelled);
        assertEquals("Number of rejectRefundCreditFTs", 1, rejectRefundCreditFTs.size());
        String actualPayRunId = rejectRefundCreditFTs.get(0).getPayrollRun().getSourcePayRunId();
        String expectedPayRunId = returnedDdFT.getPayrollRun().getSourcePayRunId();

        Application.commitUnitOfWork();

        // make sure each cancelled refund FT is included in a TransactionResponse
        for (FinancialTransaction financialTransaction : rejectRefundCreditFTs) {
            Application.beginUnitOfWork();
            responses = TransactionResponse.findTransactionResponses(financialTransaction);
            Application.commitUnitOfWork();
            assertEquals("Transaction response for Cancelled EmployerDdRejectRefundCredit transaction", 2, responses.size());
        }

        // make sure the cancelled refund FTs are related to the right PayrollRun
        assertEquals("Payroll Run ", expectedPayRunId, actualPayRunId);

        Application.beginUnitOfWork();

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedDdFT.getCompany(),
                EventTypeCode.DDDebitReturn,
                CompanyEventStatus.Active, SpcfCalendar.createInstance(2007, 9, 12), null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        assertEquals("Payroll Status", EnumUtils.getReadableName(PayrollStatus.DebitReturned),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollStatus));
        Collection<String> eventDetailValue = returnEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertTrue("Financial transaction id associated with event", eventDetailValue.contains(returnedDdFTId));

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes = CompanyEvent.findCompanyEvents(
                returnedDdFT.getCompany(),
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.DebitReturned),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));

        //Verify onhold reason
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedDdFT.getCompany());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectOther, onHoldReasonList.get(0).getOnHoldReasonCd());
        Application.commitUnitOfWork();
    }

    @Test
    public void testGenericDebitReturnsForOffloadedDebit() {
        ACHReturnsDataLoader.loadData5Day1ERRet();

        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(company);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Application.commitUnitOfWork();

        assertEquals("Number of transaction returns", 1, returnList.size());
        assertEquals("TransactionReturn status", TransactionReturnStatusCode.Resolved, returnList.get(0).getReturnStatusCd());

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = PayrollServices.entityFinder.findById(TransactionReturn.class, returnList.get(0).getId());
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

        // make sure the right Intuit5DayReturnTransfer FTs were created
        DomainEntitySet<FinancialTransaction> xferFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBOE, "1234567",
                TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Created);
        String actualSourcePayrollRunId = xferFTs.get(0).getPayrollRun().getSourcePayRunId();
        Application.commitUnitOfWork();
        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                actualSourcePayrollRunId);
        DomainEntitySet<TransactionResponse> responses;

        assertEquals("Financial Transactions ", 1, xferFTs.size());
        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                xferFTs.get(0).getPayrollRun().getSourcePayRunId());

        assertEquals("Number of returned txns", 1, returnedFTs.size());
        FinancialTransaction erDDDB = returnedFTs.get(0);

        // make sure each returned FT is Returned, and included in a TransactionResponse

        assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                erDDDB.getCurrentTransactionState().getTransactionStateCd());
        Application.beginUnitOfWork();
        responses = TransactionResponse.findTransactionResponses(erDDDB);
        Application.commitUnitOfWork();
        assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.DebitReturnedCanceled,
                payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDDebitReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        assertEquals("Payroll Status", EnumUtils.getReadableName(PayrollStatus.DebitReturnedCanceled),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollStatus));

        Collection<String> eventDetailValue = returnEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 2, eventDetailValue.size());

        assertTrue("Financial transaction id associated with event", eventDetailValue.contains(erDDDB.getId().toString()));

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.DebitReturnedCanceled),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));

        //Verify onhold reason
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());

        Application.commitUnitOfWork();
    }

    @Test
    public void testGenericDebitQBDT() {
        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned transactions
        PayrollRun payrollRun = null;

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest09", null, null, null, null, null, null, TransactionStateCode.Returned);
        assertEquals("number returned transactions", 3, c1FinTxns.size());
        ArrayList<String> txnIds = new ArrayList<String>();
        for (FinancialTransaction currTxn : c1FinTxns) {
            txnIds.add(currTxn.getId().toString());
            payrollRun = currTxn.getPayrollRun();
            company = currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            assertEquals("Transaction response for Returned transaction", 1, responses.size());
        }

        // payroll run status must be PendingAutoRedebit
        assertEquals("PayrollRun status", PayrollStatus.DebitReturned, payrollRun.getPayrollRunStatus());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDDebitReturn, CompanyEventStatus.Active, null, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - NSFAutoRedebit
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent ddDebitReturnEvent = companyEventsList.get(0);
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                ddDebitReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        Collection<String> eventDetailValue = ddDebitReturnEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", txnIds.size(), eventDetailValue.size());

        for (String currValue : eventDetailValue) {
            assertTrue("Financial transaction id associated with event", txnIds.contains(currValue));
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txnReturn = TransactionReturn.findTransactionReturns(payrollRun.getSourcePayRunId(), company);
        Application.commitUnitOfWork();

        assertEquals("There is one txn return", 1, txnReturn.size());

        // make sure the TransactionReturn isn't Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, txnReturn.get(0).getReturnStatusCd());

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectOther
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectOther, onHoldReasonList.get(0).getOnHoldReasonCd());

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.DebitReturned),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        Application.commitUnitOfWork();
    }

    @Test
    public void testGenericDebitReturns_OnHold_MoreStrikes() {

        ACHReturnsDataLoader.loadData5Day1ERRet_WithStrikes();

        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        DomainEntitySet<TransactionReturn> returnList = TransactionReturn.findTransactionReturns(company);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Application.commitUnitOfWork();

        assertEquals("Number of transaction returns", 1, returnList.size());
        TransactionReturn transactionReturn = returnList.get(0);
        assertEquals("TransactionReturn status", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);

        // make sure the right Intuit5DayReturnTransfer FTs were created
        DomainEntitySet<FinancialTransaction> xferFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBOE, "1234567",
                TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Created);
        String actualSourcePayrollRunId = xferFTs.get(0).getPayrollRun().getSourcePayRunId();
        Application.commitUnitOfWork();
        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                actualSourcePayrollRunId);
        DomainEntitySet<TransactionResponse> responses;

        assertEquals("Financial Transactions ", 1, xferFTs.size());
        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                xferFTs.get(0).getPayrollRun().getSourcePayRunId());

        // make sure each returned FT is Returned, and included in a TransactionResponse
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            Application.beginUnitOfWork();
            responses = TransactionResponse.findTransactionResponses(financialTransaction);
            Application.commitUnitOfWork();
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.DebitReturnedCanceled,
                payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDDebitReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        assertEquals("Payroll Status", EnumUtils.getReadableName(PayrollStatus.DebitReturnedCanceled),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollStatus));

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 4, strikes.size());

        //Verify onhold reason
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());

        Application.commitUnitOfWork();
    }

    @Test
    public void test_FlushModeManual_4StrikesRiskAssessment() {

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.loadData5Day1ERRet_With3StrikesAnd15PayrollCount();

        //Execute the return handlers w/FlushMode.MANUAL to simulate ACHReturns processor settings
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork(FlushMode.MANUAL);
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }


        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        returnList = TransactionReturn.findTransactionReturns(company);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Application.commitUnitOfWork();

        assertEquals("Number of transaction returns", 1, returnList.size());
        TransactionReturn transactionReturn = returnList.get(0);
        assertEquals("TransactionReturn status", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);

        // make sure the right Intuit5DayReturnTransfer FTs were created
        DomainEntitySet<FinancialTransaction> xferFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBOE, "1234567",
                TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Created);
        String actualSourcePayrollRunId = xferFTs.get(0).getPayrollRun().getSourcePayRunId();
        Application.commitUnitOfWork();

        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                actualSourcePayrollRunId);
        DomainEntitySet<TransactionResponse> responses;
        assertEquals("Financial Transactions ", 1, xferFTs.size());
        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                xferFTs.get(0).getPayrollRun().getSourcePayRunId());

        // make sure each returned FT is Returned, and included in a TransactionResponse
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            Application.beginUnitOfWork();
            responses = TransactionResponse.findTransactionResponses(financialTransaction);
            Application.commitUnitOfWork();
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.DebitReturnedCanceled,
                payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDDebitReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        assertEquals("Payroll Status", EnumUtils.getReadableName(PayrollStatus.DebitReturnedCanceled),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollStatus));

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 4, strikes.size());

        //Verify onhold reason
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());

        Application.commitUnitOfWork();
    }

    @Test
    public void test_FlushModeAuto_4StrikesRiskAssessment() {

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.loadData5Day1ERRet_With3StrikesAnd15PayrollCount();

        //Execute the return handlers w/FlushMode.MANUAL to simulate ACHReturns processor settings
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork(FlushMode.AUTO);
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }


        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        returnList = TransactionReturn.findTransactionReturns(company);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        Application.commitUnitOfWork();

        assertEquals("Number of transaction returns", 1, returnList.size());
        TransactionReturn transactionReturn = returnList.get(0);
        assertEquals("TransactionReturn status", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);

        // make sure the right Intuit5DayReturnTransfer FTs were created
        DomainEntitySet<FinancialTransaction> xferFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBOE, "1234567",
                TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Created);
        String actualSourcePayrollRunId = xferFTs.get(0).getPayrollRun().getSourcePayRunId();
        Application.commitUnitOfWork();

        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                actualSourcePayrollRunId);
        DomainEntitySet<TransactionResponse> responses;
        assertEquals("Financial Transactions ", 1, xferFTs.size());
        assertEquals("Payroll Run ", payrollRun.getSourcePayRunId(),
                xferFTs.get(0).getPayrollRun().getSourcePayRunId());

        // make sure each returned FT is Returned, and included in a TransactionResponse
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            Application.beginUnitOfWork();
            responses = TransactionResponse.findTransactionResponses(financialTransaction);
            Application.commitUnitOfWork();
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.DebitReturnedCanceled,
                payrollRun.getPayrollRunStatus());

        Application.beginUnitOfWork();

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.DDDebitReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent returnEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        assertEquals("Payroll Status", EnumUtils.getReadableName(PayrollStatus.DebitReturnedCanceled),
                returnEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollStatus));

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 4, strikes.size());

        //Verify onhold reason
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());

        Application.commitUnitOfWork();
    }


    public void testGenericDebitReturns_AlreadyOnHold_RiskAssessment() {
        // load company

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollServices.commitUnitOfWork();

        // submit first payroll
        PayrollServices.beginUnitOfWork();

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // submit another payroll
        PayrollServices.beginUnitOfWork();
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        payrollRunDTO = psdl.createPayrollRunDTO(company, cba, "PayrollBatch02");
        DateDTO payrollDate = new DateDTO();
        payrollDate.set(2007, Calendar.OCTOBER, 18);
        payrollRunDTO.setTargetPayrollTXDate(payrollDate);
        processResult = PayrollServices.payrollManager
                .submitPayroll(SourceSystemCode.QBOE, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Offload er txn
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Offload er txn
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns, "R02",
                "This is a non-NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 ERDDDB txns", 2, c1FinTxns.size());
        assertEquals("Number of returns", 2, returnList.size());

        //Execute the return handler for first return
//        for (TransactionReturn currRet : returnList) {
        TransactionReturn currRet = returnList.get(0);
        Application.beginUnitOfWork();
        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
        returnHandler.execute(currRet);
        Application.commitUnitOfWork();
//        }

        Application.beginUnitOfWork();

        //Verify onhold reason
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());

        Application.commitUnitOfWork();

//        /Execute the return handler for second return
        currRet = returnList.get(1);
        Application.beginUnitOfWork();
        returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
        returnHandler.execute(currRet);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        //Verify onhold reason is still zero
        onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());

        Application.commitUnitOfWork();

    }



    @Test
    public void testDdDebitNotOffloadedButEeCreditOffloadedBug() {
// TODO: Need to follow up on this
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        // create company and payroll run for company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();

        PayrollRunDTO payrollRun1DTO = c1dl.getCompany1PR_1PC_DoesNotExceedLimits(new DateDTO("2007-10-02"), "1", "100.00");
        c1dl.persistPayrollRun(payrollRun1DTO);

        PayrollRunDTO payrollRun2DTO = c1dl.getCompany1PR_1PC_DoesNotExceedLimits(new DateDTO("2007-10-03"), "2", "200.00");
        c1dl.persistPayrollRun(payrollRun2DTO);
        PayrollServices.commitUnitOfWork();

        // set the psp time to allow offload of verification transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // offload verification transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to allow offload of payroll transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        // offload payroll transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to allow offload of payroll transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        Application.commitUnitOfWork();

        // offload payroll transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to allow offload of payroll transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071001000000");
        Application.commitUnitOfWork();

        // complete executed transactions from 10/02
        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20071002");
        PayrollServices.commitUnitOfWork();
    }

    /**
     * This bug manifests if a returns file contains returns for two different payroll runs for the same company.
     * The Intuit5DayReturnTransfer will be created for the first payroll run, but the Intuit5DayReturnTransfer for
     * the second will fail since the ledger seems out of balance.  This was caused by the core setting the hibernate
     * flush mode to MANUAL, which was causing the subsequent ledger query to return erroneous results.
     */
    @Test
    public void testIntuit5DayReturnTransfer_PSRV001219() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.setPSPTime("20070904000000");
        PayrollServices.commitUnitOfWork();

        // create company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        c1dl.updateTo5DayFundingModel();
        PayrollServices.commitUnitOfWork();

        // set the psp time to allow offload of verification transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        // offload verification transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // create two payroll for same date
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRun1DTO = c1dl.getCompany1PR_1PC_DoesNotExceedLimits(new DateDTO("2007-10-10"), "1", "123.00");
        c1dl.persistPayrollRun(payrollRun1DTO);
        PayrollRunDTO payrollRun2DTO = c1dl.getCompany1PR_1PC_DoesNotExceedLimits(new DateDTO("2007-10-10"), "2", "986.00");
        c1dl.persistPayrollRun(payrollRun2DTO);
        PayrollServices.commitUnitOfWork();

        // set the psp time to complete verification debits and allow offload of both payrolls
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        // complete verification transactions
        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process("20071002");
        PayrollServices.commitUnitOfWork();

        // offload both payrolls
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to next day and process returns
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        Application.commitUnitOfWork();

        // find the edr trace numbers
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, "1");
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "2");
        TransactionType transactionType = PayrollServices.entityFinder.findById(TransactionType.class, TransactionTypeCode.EmployerDdDebit);

        DomainEntitySet<FinancialTransaction> erDdDebit1 =
                PayrollServices.entityFinder.find(FinancialTransaction.class,
                        FinancialTransaction.PayrollRun().equalTo(payrollRun1)
                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)));

        DomainEntitySet<FinancialTransaction> erDdDebit2 =
                PayrollServices.entityFinder.find(FinancialTransaction.class,
                        FinancialTransaction.PayrollRun().equalTo(payrollRun2)
                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)));

        DomainEntitySet<EntryDetailRecord> edr1 =
                PayrollServices.entityFinder.find(EntryDetailRecord.class,
                        EntryDetailRecord.MoneyMovementTransaction().equalTo(erDdDebit1.get(0).getMoneyMovementTransaction())
                                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Debit)));

        DomainEntitySet<EntryDetailRecord> edr2 =
                PayrollServices.entityFinder.find(EntryDetailRecord.class,
                        EntryDetailRecord.MoneyMovementTransaction().equalTo(erDdDebit2.get(0).getMoneyMovementTransaction())
                                .And(EntryDetailRecord.CreditDebitIndicator().equalTo(CreditDebitCode.Debit)));

        // create the returns file
        String amt1 = String.valueOf(new BigDecimal(edr1.get(0).getAmount().toString()).movePointRight(2).longValue());
        String amt2 = String.valueOf(new BigDecimal(edr2.get(0).getAmount().toString()).movePointRight(2).longValue());
        String trace1 = edr1.get(0).getTraceNumber();
        String trace2 = edr2.get(0).getTraceNumber();
        String pad12 = "000000000000";
        String pad14 = pad12 + "00";

        String pr1record6 = "62612200066155555567         0000010#      INTUIT DIRECT DEPOSITS  1000000000000001";
        String pr2record6 = "626021000021722616653        0000020#      INTUIT DIRECT DEPOSITS  1000000000000002";
        String pr1record7 = "799R010#      12200066UNCOLLECTED FUNDS                           000000000000001";
        String pr2record7 = "799R010#      12200066UNCOLLECTED FUNDS                           000000000000002";

        StringBuffer buf = new StringBuffer();
        String newline = System.getProperty("line.separator");
        buf.append("101 021000021972261600006121315551094101JPMORGAN CHASE         INTUIT                         ").append(newline);
        buf.append("5200INTUIT                     INTUIT   1722616679CCDINTUITPAYR071003071003   1021000020000001").append(newline);
        buf.append(pr1record6.replaceFirst("#", pad12.substring(amt1.length()) + amt1)).append(newline);
        buf.append(pr1record7.replaceFirst("#", pad14.substring(trace1.length()) + trace1)).append(newline);
        buf.append("82000000021234567890000000010000000000010000                                           0000001").append(newline);
        buf.append("5200INTUIT                     INTUIT   1722616679CCDINTUITPAYR071003071003   1021000020000002").append(newline);
        buf.append(pr2record6.replaceFirst("#", pad12.substring(amt2.length()) + amt2)).append(newline);
        buf.append(pr2record7.replaceFirst("#", pad14.substring(trace2.length()) + trace2)).append(newline);
        buf.append("82000000021234567890000000020000000000020000                                           0000002").append(newline);
        buf.append("9000002000001000000041234567890000000030000000000030000                                       ").append(newline);
        buf.append("9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999").append(newline);
        buf.append("9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999").append(newline);

        File retFile = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_ftp_recv_dir"), "retfile.txt");

        try {
            FileWriter fw = new FileWriter(retFile);
            fw.write(buf.toString());
            fw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        new ReturnFileParser().processFile(retFile);

        // make sure Intuit5DayReturnTransfer txns have been created for both payroll runs
        transactionType = PayrollServices.entityFinder.findById(TransactionType.class, TransactionTypeCode.Intuit5DayReturnTransfer);

        DomainEntitySet<FinancialTransaction> retXfer1 =
                PayrollServices.entityFinder.find(FinancialTransaction.class,
                        FinancialTransaction.PayrollRun().equalTo(payrollRun1)
                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)));

        DomainEntitySet<FinancialTransaction> retXfer2 =
                PayrollServices.entityFinder.find(FinancialTransaction.class,
                        FinancialTransaction.PayrollRun().equalTo(payrollRun2)
                                .And(FinancialTransaction.TransactionType().equalTo(transactionType)));

        assertEquals("Number of Intuit5DayReturnTransfer txns for payroll 1", 1, retXfer1.size());
        assertEquals("Number of Intuit5DayReturnTransfer txns for payroll 2", 1, retXfer2.size());
    }

    // Tax Debit Returns Tests

    @Test
    public void testVoidEntirePayroll() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 1, 1, SpcfTimeZone.getLocalTimeZone()));


        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "0.00", "0.00", "900.00", "-900.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"900.00", "-900.00", "0.00", "0.00", "0.00", "0.00"});
        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();





    }


    @Test
    public void testPartialVoid() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "0.00", "0.00", "900.00", "-900.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"900.00", "-900.00", "0.00", "0.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(voidProcessResult);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "-900.00", "900.00", "0.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testVoidEntirePayroll_SubmitSecondPayroll() {

        testVoidEntirePayroll();

        // Submit a Second Payroll After voiding the First One

        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        List<Employee> employees = new ArrayList<Employee>(company.getDirectDepositEmployees());

        PayrollRunDTO payrollDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-07"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"200", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // PayrollServices.beginUnitOfWork();

        // PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());

//        // Verify Ledger Balances for Payroll 2
//        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "-900.00", "0.00", "1275.00", "-375.00"});
//        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
//
//        // Verify Ledger Balances for the Quarter
//        ArrayList<PayrollRun> payrollRuns = new ArrayList<PayrollRun>(PayrollRun.findPayrollRunsForQuarter(company, CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate())));
//        ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "-900.00", "0.00", "0.00", "1275.00", "-375.00"});
//        assertLedgerBalances(payrollRuns, ledgerBalancesToCompare);
//        PayrollServices.rollbackUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());

        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();
        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);
        transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollDTO.getPayrollTXBatchId());
        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        payrollDTO.setPayrollTXBatchId("Payroll_3");
        for (PaycheckDTO paycheckDTO : payrollDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(paycheckDTO.getPaycheckId() + "3");
        }

        // Offload Second Payroll

          // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2007, 9, 13, SpcfTimeZone.getLocalTimeZone()));
       // DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns, "R02",
       //         "This is a non-NSF description");

        //Execute the return handlers
//        for (TransactionReturn currRet : returnList) {
//            Application.beginUnitOfWork();
//            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
//            returnHandler.execute(currRet);
//            Application.commitUnitOfWork();
//        }



    }


    @Test
    public void testQBDTSymphonyGenericDebitQBDT() {
        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";

        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturned(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS), "R02", "Non-NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned transactions
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

        DomainEntitySet<FinancialTransaction> returnFinancialTransactions = FinancialTransaction
                .findFinancialTransactions(company, sourcePayrollRunId, null, null, null, null, null, null, TransactionStateCode.Returned);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyFinancialTransactionReturn(returnFinancialTransactions, true);

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.DebitReturned);

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnFinancialTransactions);

        transactionReturnVerifier.verifyTransactionReturn(company, payrollRun, TransactionReturnStatusCode.Open);

        transactionReturnVerifier.verifyStrikeEvent(company, ServiceSubStatusCode.AchRejectOther, StrikeReason.DebitReturned);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonySecondNonNSFReturnZeroBalance() {
        String sourceCompanyId = "1234567";
        String sourcePayrollRunId = "BatchTest05";

        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadPayrollRetAddRedebitAddWireRedebitRetNonNSF(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS),"R02", "NonNSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        // find the returned transactions
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);;

        DomainEntitySet<FinancialTransaction> returnFinancialTransactions = FinancialTransaction
                .findFinancialTransactions(company, sourcePayrollRunId, null, null, null, null, null, null, TransactionStateCode.Returned).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();

        transactionReturnVerifier.verifyRedebitFinancialTransactionReturn(returnFinancialTransactions, 2);

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.Complete);

        transactionReturnVerifier.verifyTransactionReturnStatus(returnedTxn, TransactionReturnStatusCode.Resolved);

        transactionReturnVerifier.verifyCompanyNotOnHold(company, transactionReturnHelper.getNonExpiredOnHoldReasonList(company));

        Application.rollbackUnitOfWork();
    }

    private static final ArrayList<LedgerAccountCode> taxLedgerAccounts = new ArrayList<LedgerAccountCode>();

    static {
        taxLedgerAccounts.add(LedgerAccountCode.TaxCurrentLiability);
        taxLedgerAccounts.add(LedgerAccountCode.TaxCurrentCash);
        taxLedgerAccounts.add(LedgerAccountCode.ERPayable);
        taxLedgerAccounts.add(LedgerAccountCode.AgencyTaxRefund);
        taxLedgerAccounts.add(LedgerAccountCode.TaxFutureLiability);
        taxLedgerAccounts.add(LedgerAccountCode.TaxFutureReceivable);
    }

    private HashMap<LedgerAccountCode, String> createCompareMap(String[] pAmounts) {
        HashMap<LedgerAccountCode, String> amountMap = new HashMap<LedgerAccountCode, String>();
        amountMap.put(LedgerAccountCode.TaxCurrentLiability, pAmounts[0]);
        amountMap.put(LedgerAccountCode.TaxCurrentCash, pAmounts[1]);
        amountMap.put(LedgerAccountCode.ERPayable, pAmounts[2]);
        amountMap.put(LedgerAccountCode.AgencyTaxRefund, pAmounts[3]);
        amountMap.put(LedgerAccountCode.TaxFutureLiability, pAmounts[4]);
        amountMap.put(LedgerAccountCode.TaxFutureReceivable, pAmounts[5]);
        return amountMap;
    }


    private HashMap<LedgerAccountCode, SpcfMoney> assertLedgerBalances(PayrollRun pPayrollRun, HashMap<LedgerAccountCode, String> pAmountsToCompare) {
        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = LedgerAccount.getLedgerAccountBalances(pPayrollRun.getCompany(), pPayrollRun, taxLedgerAccounts);

        for (LedgerAccountCode ledgerAccount : ledgerBalances.keySet()) {
            assertEquals(ledgerAccount.toString(), pAmountsToCompare.get(ledgerAccount), ledgerBalances.get(ledgerAccount).toString());
        }
        return ledgerBalances;
    }


    private HashMap<LedgerAccountCode, SpcfMoney> assertLedgerBalances(ArrayList<PayrollRun> pPayrollRuns, HashMap<LedgerAccountCode, String> pAmountsToCompare) {

        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = LedgerAccount.getLedgerAccountBalances(pPayrollRuns.get(0).getCompany(), pPayrollRuns, taxLedgerAccounts);

        for (LedgerAccountCode ledgerAccount : ledgerBalances.keySet()) {
            assertEquals(ledgerAccount.toString(), pAmountsToCompare.get(ledgerAccount), ledgerBalances.get(ledgerAccount).toString());
        }
        return ledgerBalances;
    }
}
