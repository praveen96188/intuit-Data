package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 28, 2008
 * Time: 8:53:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class SecondNsfReturnTests {


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
    public void testQBDTSecondNSFReturn_ZeroBalance()  {
        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadQBDTPayrollReturnedWireRedebitReturned("R01", "NSF description");

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
            company=currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                         TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            if (TransactionType.isRedebitTransactionType(currTxn.getTransactionType().getTransactionTypeCd())) {
                assertEquals("Transaction response for Returned REDEBIT transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned transaction", 1, responses.size());
            }
        }

        // payroll run status must be ReturnedTwice
        assertEquals("PayrollRun status", PayrollStatus.Complete , payrollRun.getPayrollRunStatus());

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
    public void testQBDTSecondNSFReturn()  {
        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadQBDTPayrollReturnedRedebitReturned("R01", "NSF description");

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
            company=currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                         TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            if (TransactionType.isRedebitTransactionType(currTxn.getTransactionType().getTransactionTypeCd())) {
                assertEquals("Transaction response for Returned REDEBIT transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned transaction", 1, responses.size());
            }
        }

        // payroll run status must be ReturnedTwice
        assertEquals("PayrollRun status", PayrollStatus.ReturnedTwice, payrollRun.getPayrollRunStatus());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NSF, CompanyEventStatus.Active, SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone()), null);

        //Assertion for Create NSF System Event Rule - NSFSubType - SecondNSF
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.SecondNSF) ,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }
        Application.commitUnitOfWork();
        
        Application.beginUnitOfWork();
        TransactionReturn txnReturn = PayrollServices.entityFinder.findById(TransactionReturn.class, returnedTxn.getId());
        Application.commitUnitOfWork();

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, txnReturn.getReturnStatusCd());

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());
        Application.commitUnitOfWork();
    }

    @Test
    public void testQBOESecondNSFReturn_ZeroBalance()  {
        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadQBOEPayrollReturnedWireRedebitReturned("R01", "NSF description");

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
            company=currTxn.getCompany();
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
    public void testQBOESecondNSFReturnOnFee_ZeroBalance()  {
        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadQBOEPayrollReturnedWireRedebitReturned("R01", "NSF description");

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
            company=currTxn.getCompany();
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
    public void testNsfReturns()  {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForSecondNSFReturn();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        Application.commitUnitOfWork();

        // check post-conditions
        Application.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn);
        FinancialTransaction returnedDdRedebitFT = null;

        for(FinancialTransaction financialTransaction : returnedFTs){
            if(financialTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerDdRedebit)){
                returnedDdRedebitFT = financialTransaction;
                break;
            }
        }

        // make sure each returned FT is Returned, and included in a TransactionResponse
        DomainEntitySet<TransactionResponse> responses;
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            responses = TransactionResponse.findTransactionResponses(financialTransaction);
            if (financialTransaction.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerFeeDebit) {
                assertEquals("Transaction response for Returned EmployerFeeDebit transaction", 2, responses.size());
            } else {
                assertEquals("Transaction response for Returned EmployerDdRedebit transaction", 2, responses.size());
            }
        }

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.ReturnedTwice,
                     returnedDdRedebitFT.getPayrollRun().getPayrollRunStatus());

        // make sure the right event was created
        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(returnedDdRedebitFT.getCompany(),
                EventTypeCode.NSF, CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 2, events.size());

        for (CompanyEvent companyEvent : events) {
            FinancialTransaction eventFinTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId)));
            if (eventFinTxn.equals(returnedDdRedebitFT)) {
                assertEquals("Verification Status", EnumUtils.getReadableName(NSFSubTypeType.SecondNSF), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
                assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF), companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
            }
        }

        events = CompanyEvent.findCompanyEvents(returnedDdRedebitFT.getCompany(),
                EventTypeCode.FeeReturn,
                CompanyEventStatus.Active, null, null);

        for (CompanyEvent companyEvent : events) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.FeeReturn),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        }        

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes = CompanyEvent.findCompanyEvents(returnedDdRedebitFT.getCompany(),
                EventTypeCode.Strike, CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        for (CompanyEvent strikeEvent : strikes) {
            assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                    strikeEvent.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        }

        // verify company has an onhold with status code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedDdRedebitFT.getCompany());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        Application.commitUnitOfWork();
    }


    @Test
    public void testQBDTSymphonySecondNSFReturn()  {
        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";

        // load all necessary data
        TransactionReturn returnedTxn = ACHReturnsDataLoader.loadQBDTPayrollReturnedRedebitReturned(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS), "R01", "NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = transactionReturnHelper.findReturnedFinancialTransactions(company, sourcePayrollRunId);

        Application.refresh(returnedTxn);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyRedebitFinancialTransactionReturn(returnedFinancialTransactions, 2);

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.ReturnedTwice);

        transactionReturnVerifier.verifyNSFCompanyEvent(company, returnedFinancialTransactions,  SpcfCalendar.createInstance(2007, 10, 9, SpcfTimeZone.getLocalTimeZone()), NSFSubTypeType.SecondNSF, false);

        transactionReturnVerifier.verifyTransactionReturnStatus(returnedTxn, TransactionReturnStatusCode.Open);

        transactionReturnVerifier.verifyCompanyOnHoldReason(transactionReturnHelper.getNonExpiredOnHoldReasonList(company), ServiceSubStatusCode.AchRejectR1R9);

        Application.rollbackUnitOfWork();
    }
}
