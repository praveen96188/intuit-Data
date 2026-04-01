package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 28, 2008
 * Time: 1:54:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class FirstNsfReturnTests {


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }
        
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011,1,1));
    }

    @After
    public void runAfterEachTest() {
        Application.rollbackUnitOfWork();
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testBankQuery2() {
        ACHReturnsDataLoader.loadData2DayERGenericReturn();
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "NONNSF Return");

        PayrollServices.beginUnitOfWork();
        Company company = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceSubStatusCode.RiskCollections);
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        ArrayList<Object[]> transactions = TransactionReturn.findTransactionReturnsBySAPCriteria(
                        TransactionReturnStatusCode.Open,
                        null,
                        null,
                        true,
                        "R",
                        null,
                        null,
                        ServiceSubStatusCode.RiskCollections,
                        null,
                        TransactionTypeGroupCode.Debit,
                        true,
                        null,
                        false,
                        -1,
                        -1);

        //Just the two qbdt txns
        int expected = 3;
        int actual = transactions.size();
        assertEquals("Number of txns when risk collections hold exists", expected, actual);
        System.out.println("number of txns: "+transactions.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,company.getSourceCompanyId(),ServiceSubStatusCode.RiskCollections));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ArrayList<Object[]> transactions2 = TransactionReturn.findTransactionReturnsBySAPCriteria(
                        TransactionReturnStatusCode.Open,
                        null,
                        null,
                        true,
                        "R",
                        null,
                        null,
                        ServiceSubStatusCode.RiskCollections,
                        null,
                        TransactionTypeGroupCode.Debit,
                        true,
                        null,
                        false,
                        -1,
                        -1);
        //2 qbdt + 1 qboe

        remove0DollarTransactions(transactions2);
        assertEquals("Number of txns when risk collections hold exists", 4, transactions2.size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company
                .findCompany("1234567", SourceSystemCode.QBOE);
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,company.getSourceCompanyId(),ServiceSubStatusCode.AchRejectOther));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ArrayList<Object[]> transactions3 = TransactionReturn.findTransactionReturnsBySAPCriteria(
                        TransactionReturnStatusCode.Open,
                        null,
                        null,
                        true,
                        "R",
                        null,
                        null,
                        ServiceSubStatusCode.RiskCollections,
                        null,
                        TransactionTypeGroupCode.Debit,
                        true,
                        null,
                        false,
                        -1,
                        -1);

        remove0DollarTransactions(transactions3);
        assertEquals("Number of txns when risk collections hold exists", 4, transactions3.size());
        System.out.println("number of txns: "+transactions3.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testQBDTFirstNSFReturn() {
        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R01", "NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned transactions
        PayrollRun payrollRun = null;

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest09", null, null, null, null, null, null, TransactionStateCode.Returned);
        assertEquals("number of returned transactions", 3, c1FinTxns.size());
        ArrayList<String> txnIds = new ArrayList<String>();
        for (FinancialTransaction currTxn : c1FinTxns) {
            txnIds.add(currTxn.getId().toString());
            payrollRun = currTxn.getPayrollRun();
            company = currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // payroll run status must be PendingAutoRedebit
        assertEquals("PayrollRun status", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());

        // find the fee transactions that should have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                "8574536",
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // there must be a new fee FT and it must be related to this payroll run
        assertEquals("Fee Transactions", 1, feeFTs.size());
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                feeFTs.get(0).getPayrollRun().getSourcePayRunId());

        // that fee must be the DebitReturnFee
        BillingDetail detail = feeFTs.get(0).getBillingDetail();
        assertEquals("Fee type", OfferingServiceChargeType.DebitReturnFee, detail.getOfferingServiceChargeType());

        // find the tax transactions that MUST have been created
        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                "8574536",
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        // find the ER DD Redebit transaction that should have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                "8574536",
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // there must be a new ER DD Redebit FT
        assertEquals("DD Redebit Transactions", 1, redebitFTs.size());
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                redebitFTs.get(0).getPayrollRun().getSourcePayRunId());

        //there must be a new tax FT
        assertEquals("Tax Transactions", 1, taxFTs.size());
        FinancialTransaction taxFT = taxFTs.get(0);
        OfferingServiceChargeType ofct = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(taxFT.getSku());
        assertEquals("Sku is return fee sku", OfferingServiceChargeType.DebitReturnFee, ofct);
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                taxFT.getPayrollRun().getSourcePayRunId());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NSF, CompanyEventStatus.Active, null, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - NSFAutoRedebit
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent nsfEvent = companyEventsList.get(0);

        assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.NSFAutoRedebit),
                nsfEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                nsfEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));


        Collection<String> eventDetailValue = nsfEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", txnIds.size(), eventDetailValue.size());

        for (String currValue : eventDetailValue) {
            assertTrue("Financial transaction id associated with event", txnIds.contains(currValue));
        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txnReturn = TransactionReturn.findTransactionReturns(payrollRun.getSourcePayRunId(), company);
        Application.commitUnitOfWork();

        assertEquals("There is one txn return", 1, txnReturn.size());

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, txnReturn.get(0).getReturnStatusCd());

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) company);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
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
    public void testQBDTFirstNSFReturn_InactiveCBA() {
        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturnedCBADeactivated("R01", "NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned transactions
        PayrollRun payrollRun = null;

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(company, "BatchTest09", null, null, null, null, null, null, TransactionStateCode.Returned);
        assertEquals("number of returned transactions", 3, c1FinTxns.size());
        ArrayList<String> txnIds = new ArrayList<String>();
        for (FinancialTransaction currTxn : c1FinTxns) {
            txnIds.add(currTxn.getId().toString());
            payrollRun = currTxn.getPayrollRun();
            company = currTxn.getCompany();
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, currTxn.getCurrentTransactionState().getTransactionStateCd());
            DomainEntitySet<TransactionResponse> responses = TransactionResponse.findTransactionResponses(currTxn);
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // payroll run status must be DebitReturned
        assertEquals("PayrollRun status", PayrollStatus.DebitReturned, payrollRun.getPayrollRunStatus());

        // find the fee transactions that should NOT have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                "8574536",
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // there must NOT be a new fee FT 
        assertEquals("Fee Transactions", 0, feeFTs.size());

        // find the tax transactions that must NOT have been created
        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                "8574536",
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        // find the ER DD Redebit transaction that should NOT have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT,
                "8574536",
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // there must NOT be a new ER DD Redebit FT
        assertEquals("DD Redebit Transactions", 0, redebitFTs.size());

        //there must NOT be a new tax FT
        assertEquals("Tax Transactions", 0, taxFTs.size());

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

        // make sure the TransactionReturn is NOT Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, txnReturn.get(0).getReturnStatusCd());

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) company);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        Application.commitUnitOfWork();
    }

    @Test
    public void testFirstNSFReturnOffloadedAll() {
        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());

        // find the right handler and execute it
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned ER DD Debit FT
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);
        FinancialTransaction returnedDdDebitFT = null;
        for (FinancialTransaction ft : returnedFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdDebit) {
                returnedDdDebitFT = ft;
                break;
            }
        }
        assertTrue(returnedDdDebitFT != null);

        // payroll run status must be PendingAutoRedebit
        assertEquals("PayrollRun status",
                PayrollStatus.PendingAutoRedebit, returnedDdDebitFT.getPayrollRun().getPayrollRunStatus());

        // make sure each returned FT is now Returned, and included in a TransactionResponse
        DomainEntitySet<TransactionResponse> responses;
        for (FinancialTransaction ftReturned : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, ftReturned.getCurrentTransactionState().getTransactionStateCd());
            responses = TransactionResponse.findTransactionResponses(ftReturned);
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // find the fee transactions that should have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // there must be a new fee FT and it must be related to this payroll run
        assertEquals("Fee Transactions", 1, feeFTs.size());
        assertEquals("Payroll Run", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                feeFTs.get(0).getPayrollRun().getSourcePayRunId());

        // that fee must be the DebitReturnFee
        BillingDetail detail = feeFTs.get(0).getBillingDetail();
        assertEquals("Fee type", OfferingServiceChargeType.DebitReturnFee, detail.getOfferingServiceChargeType());

        // find the tax transactions that MAY have been created
        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        // find the ER DD Redebit transaction that should have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // there must be a new ER DD Redebit FT
        assertEquals("DD Redebit Transactions", 1, redebitFTs.size());
        assertEquals("Payroll Run", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                redebitFTs.get(0).getPayrollRun().getSourcePayRunId());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.NSF, CompanyEventStatus.Active, null, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - NSFAutoRedebit
        assertEquals("Company Events", 1, companyEventsList.size());
        CompanyEvent nsfReturnEvent = companyEventsList.get(0);
        assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.NSFAutoRedebit),
                nsfReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                nsfReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        Collection<String> eventDetailValue = nsfReturnEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 2, eventDetailValue.size());

        boolean found = false;
        for (String currValue : eventDetailValue) {
            if(returnedDdDebitFT.getId().toString().equals(currValue)) {
                found = true;
            }
        }
        assertTrue("Financial transaction id associated with event", found);

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) returnedDdDebitFT.getCompany());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        Application.commitUnitOfWork();
    }


    @Test
    public void testFirstNSFReturnOffloadedAll_CBAChanged() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 1, returnList.size());

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO cbaDTO = CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result;
        result = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBOE,
                c1dl.getCompany1().getCompanyId(), cbaDTO,
                false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        // get the handler and invoke it
        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.findById(TransactionReturn.class, returnList.get(0).getId()); // avoids stale object exception
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        Application.commitUnitOfWork();

        // check post-conditions
        Application.beginUnitOfWork();

        // find the returned ER DD Debit FT
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn);
        FinancialTransaction returnedDdDebitFT = null;
        for (FinancialTransaction ft : returnedFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdDebit) {
                returnedDdDebitFT = ft;
                break;
            }
        }
        assertTrue(returnedDdDebitFT != null);

        // payroll run status must be PendingAutoRedebit
        assertEquals("PayrollRun status",
                PayrollStatus.PendingAutoRedebit, returnedDdDebitFT.getPayrollRun().getPayrollRunStatus());

        // each returned FT must now be Returned, and included in a TransactionResponse
        DomainEntitySet<TransactionResponse> responses;
        for (FinancialTransaction ftReturned : returnedFTs) {
            assertEquals("Returned FT state", TransactionStateCode.Returned,
                    ftReturned.getCurrentTransactionState().getTransactionStateCd());
            responses = TransactionResponse.findTransactionResponses(ftReturned);
            assertEquals("Returned FT in a TransactionResponse", 1, responses.size());
        }

        // find the fee transactions that should have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // there must be a new fee FT and it must be related to this payroll run
        assertEquals("Fee Transactions", 1, feeFTs.size());
        assertEquals("Payroll Run", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                feeFTs.get(0).getPayrollRun().getSourcePayRunId());

        // that fee must be the DebitReturnFee
        BillingDetail detail = feeFTs.get(0).getBillingDetail();
        assertEquals("Fee type", OfferingServiceChargeType.DebitReturnFee, detail.getOfferingServiceChargeType());

        // verify the created FeeDebit transaction is associated with the new bank account
        assertTrue("Fee bank account", changedCompanyBankAccount.getBankAccount().equals(feeFTs.get(0).getDebitBankAccount()));

        // find the tax transactions that MAY have been created
        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        // find the ER DD Redebit transaction that should have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // there must be a new ER DD Redebit FT
        assertEquals("DD Redebit Transactions", 1, redebitFTs.size());
        assertEquals("Payroll Run", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                redebitFTs.get(0).getPayrollRun().getSourcePayRunId());

        // the ER DD Redebit FT must be associated with the new bank account
        assertTrue("FeeDebit Transaction bank account",
                changedCompanyBankAccount.getBankAccount().equals(redebitFTs.get(0).getDebitBankAccount()));
        Application.commitUnitOfWork();

        // make sure the right ACHReturn company event was created
        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.NSF,
                CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent nsfReturnEvent = events.get(0);

        assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.NSFAutoRedebit),
                nsfReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                nsfReturnEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
        Collection<String> eventDetailValue = nsfReturnEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 2, eventDetailValue.size());

        boolean found = false;
        for (String currValue : eventDetailValue) {
            if(returnedDdDebitFT.getId().toString().equals(currValue)) {
                found = true;
            }
        }
        assertTrue("Financial transaction id associated with event", found);

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ",
                TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) returnedDdDebitFT.getCompany());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));

        Application.commitUnitOfWork();
    }

    @Test
    public void testFirstNSFReturn_CBAChangedAfterOffload() {
        ACHReturnsDataLoader.loadQBDTPayrollOffloaded();

        //Create a new CBA
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071006000000");
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult cbaAdd = PayrollServices.companyManager.addCompanyBankAccount(SourceSystemCode.QBDT, "8574536", companyBankAccountDTO, true, true);
        assertSuccess(cbaAdd);
        PayrollServices.commitUnitOfWork();

        //Offload the verification transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009171500");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        offloader.getOffloadBatch().getId();

        //Verify the CBA
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, "123123");
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        for (FinancialTransaction financialTransaction : verificationTransactions) {
            amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
        }
        ProcessResult result = PayrollServices.companyManager.verifyCompanyBankAccount(SourceSystemCode.QBDT, "8574536", "123123", amountsToVerify.get(0), amountsToVerify.get(1), false);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns, "R01", "NSF return");

        assertEquals("Number of fin txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        Application.commitUnitOfWork();

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(Application.refresh(currRet));
            Application.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, "123123");

        DomainEntitySet<FinancialTransaction> redebits = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);
        DomainEntitySet<FinancialTransaction> feeRedebits = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> salesTaxRedebits = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        assertEquals("Number of redebits", 1, redebits.size());
        assertEquals("Number of feeRedebits", 1, feeRedebits.size());
        assertEquals("Number of salesTaxRedebits", 1, salesTaxRedebits.size());

        CompanyBankAccount redebitCBA = redebits.get(0).getCompanyBankAccount();
        CompanyBankAccount feeRedebitCBA = feeRedebits.get(0).getCompanyBankAccount();
        CompanyBankAccount salesTaxRedebitCBA = salesTaxRedebits.get(0).getCompanyBankAccount();

        assertEquals("redebitCBA bank account", companyBankAccount, redebitCBA);
        assertEquals("feeRedebitCBA bank account", companyBankAccount, feeRedebitCBA);
        assertEquals("salesTaxRedebitCBA bank account", companyBankAccount, salesTaxRedebitCBA);

        Application.commitUnitOfWork();


    }


    @Test
    public void testFirstNSFReturn_CBAChangedBeforeOffloadAll() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        loader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBOE,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        // verify financial transactions are moved to new ba
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        assertEquals("Number of employer transacions", 1, c1FinTxns.size());
        assertEquals("Debit bank account", changedCompanyBankAccount.getBankAccount(), c1FinTxns.get(0).getDebitBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> returnList = loader.createFirstNSFReturn();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        Application.commitUnitOfWork();
        // find the handler and call it
        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.findById(TransactionReturn.class, returnList.get(0).getId()); // to avoid stale object exception
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        Application.commitUnitOfWork();

        // check post-conditions

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn);

        // each returned FT must now be Returned, and included in a TransactionResponse
        DomainEntitySet<TransactionResponse> responses;
        for (FinancialTransaction ftReturned : returnedFTs) {
            assertEquals("Returned FT state", TransactionStateCode.Returned,
                    ftReturned.getCurrentTransactionState().getTransactionStateCd());
            responses = TransactionResponse.findTransactionResponses(ftReturned);
            assertEquals("Returned FT in a TransactionResponse", 1, responses.size());
        }

        FinancialTransaction returnedDdDebitFT = returnedFTs.get(0);

        // check the PayrollRun status
        assertEquals("PayrollRun status", PayrollStatus.PendingAutoRedebit, returnedDdDebitFT.getPayrollRun().getPayrollRunStatus());

        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // make sure the right fee was charged
        assertEquals("Fee FTs", 1, feeFTs.size());
        assertEquals("Fee FT PayrollRun ", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                feeFTs.get(0).getPayrollRun().getSourcePayRunId());
        BillingDetail detail = feeFTs.get(0).getBillingDetail();
        assertEquals("Fee type", OfferingServiceChargeType.DebitReturnFee, detail.getOfferingServiceChargeType());
        assertTrue("Fee FT bank account",
                changedCompanyBankAccount.getBankAccount().equals(feeFTs.get(0).getDebitBankAccount()));

        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // make sure there's an ER DD Redebit
        assertEquals("ER DD Redebit FTs", 1, redebitFTs.size());
        assertEquals("ER DD Redebit PayrollRun", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                redebitFTs.get(0).getPayrollRun().getSourcePayRunId());
        assertTrue("ER DD Redebit FT bank account",
                changedCompanyBankAccount.getBankAccount().equals(redebitFTs.get(0).getDebitBankAccount()));
        Application.commitUnitOfWork();

        // make sure the right company event was created
        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.NSF,
                CompanyEventStatus.Active, null, null);

        assertEquals("Company Events", 1, events.size());
        CompanyEvent companyEvent = events.get(0);

        assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.NSFAutoRedebit),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));
Collection<String> eventDetailValue = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 2, eventDetailValue.size());

        boolean found = false;
        for (String currValue : eventDetailValue) {
            if(returnedDdDebitFT.getId().toString().equals(currValue)) {
                found = true;
            }
        }
        assertTrue("Financial transaction id associated with event", found);
        Application.commitUnitOfWork();
        
        Application.beginUnitOfWork();

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) returnedDdDebitFT.getCompany());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testFirstNSFReturn_CBADeactivated() {
        // load necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        loader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> returnList = loader.createFirstNSFReturn();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        // deactivate company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        ProcessResult<CompanyBankAccount> result;
        result = PayrollServices.companyManager.deactivateCompanyBankAccount(
                SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                dataloader.getTestCompanyBankAccount().getCompanyBankAccountID(), true, false);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(txnReturn);
        // to avoid stale object exception
        txnReturn = Application.findById(TransactionReturn.class, txnReturn.getId());
        returnHandler.execute(txnReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn);
        FinancialTransaction returnedErDdDebitFT = returnedFTs.get(0);

        // make sure each returned FT is Returned, and included in a TransactionResponse
        DomainEntitySet<TransactionResponse> responses;
        for (FinancialTransaction financialTransaction : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());
            responses = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.DebitReturned,
                returnedErDdDebitFT.getPayrollRun().getPayrollRunStatus());

        // make sure the TransactionReturn did not get Resolved
        assertEquals("TransactionReturn is Open", TransactionReturnStatusCode.Open, txnReturn.getReturnStatusCd());

        // make sure the fee wasn't created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                returnedErDdDebitFT.getCompany().getSourceSystemCd(),
                returnedErDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);
        assertEquals("Fee FTs", 0, feeFTs.size());

        // make sure the ER DD Redebit was not created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                returnedErDdDebitFT.getCompany().getSourceSystemCd(),
                returnedErDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);
        assertEquals("ER DD Redebit FTs", 0, redebitFTs.size());

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        // make sure the right event got created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedErDdDebitFT.getCompany(),
                EventTypeCode.DDDebitReturn,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent companyEvent = events.get(0);

        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDDebitReturn),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        Collection<String> eventDetailValue = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 2, eventDetailValue.size());

        boolean found = false;
        for (String currValue : eventDetailValue) {
            if(returnedErDdDebitFT.getId().toString().equals(currValue)) {
                found = true;
            }
        }
        assertTrue("Financial transaction id associated with event", found);

        // make sure the right strike got created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(returnedErDdDebitFT.getCompany(),
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) returnedErDdDebitFT.getCompany());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testFirstNSFReturnOffloadedDebit() {
        // load all data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturnOffloadedDebit("R01", "This is an NSF description");
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // find the handler and call it
        Application.beginUnitOfWork();
        TransactionReturn txnReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        txnReturn = handler.execute(Application.refresh(txnReturn));
        Application.commitUnitOfWork();

        // check post-condition
        Application.beginUnitOfWork();

        // make sure the TransactionReturn is Resolved
        assertEquals("TransactionReturn status", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());

        // make sure each returned FT is in the Returned state, and has a TransactionResponse
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        assertEquals("number of returned fts", 1, returnedFTs.size());
        FinancialTransaction returnedErDdDebitFT = returnedFTs.get(0);

        DomainEntitySet<TransactionResponse> txnResponseList;

            assertEquals("Returned FT state", TransactionStateCode.Returned,
                    returnedErDdDebitFT.getCurrentTransactionState().getTransactionStateCd());
            txnResponseList = TransactionResponse.findTransactionResponses(returnedErDdDebitFT);
            assertEquals("Returned FT has TransactionResponse", 1, txnResponseList.size());


        FinancialTransaction returnedDdDebitFT = returnedFTs.get(0);

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.NSFCanceled,
                returnedDdDebitFT.getPayrollRun().getPayrollRunStatus());

        DomainEntitySet<FinancialTransaction> xferFTs;
        xferFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.Intuit5DayReturnTransfer, TransactionStateCode.Created);

        // make sure the Intuit 5Day Return Transfer FT was created
        assertEquals("Intuit5DayReturnTransfer FTs", 1, xferFTs.size());
        assertEquals("Intuit5DayReturnTransfer PayrollRun ", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                xferFTs.get(0).getPayrollRun().getSourcePayRunId());

        DomainEntitySet<FinancialTransaction> cancelledFTs;
        cancelledFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);

        // make sure each EE DD Credit FT is cancelled, and included in a TransactionResponse
        assertEquals("EE DD Credit FTs cancelled", 2, cancelledFTs.size());
        for (FinancialTransaction ftCancelled : cancelledFTs) {
            assertEquals("Cancelled FT PayrollRun", returnedDdDebitFT.getPayrollRun().getSourcePayRunId(),
                    ftCancelled.getPayrollRun().getSourcePayRunId());
            assertEquals("Cancelled FT state", TransactionStateCode.Cancelled,
                    ftCancelled.getCurrentTransactionState().getTransactionStateCd());
            txnResponseList = TransactionResponse.findTransactionResponses(ftCancelled);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();

        // make sure the right company event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.NSF,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent companyEvent = events.get(0);

        assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.NSFPayrollCancelled),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        Collection<String> eventDetailValue = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 2, eventDetailValue.size());

        boolean found = false;
        for (String currValue : eventDetailValue) {
            if(returnedDdDebitFT.getId().toString().equals(currValue)) {
                found = true;
            }
        }
        assertTrue("Financial transaction id associated with event", found);

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());

        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFPayrollCancelled),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));

        // verify no onholds are created
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedDdDebitFT);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());

        Application.commitUnitOfWork();
    }

    private DomainEntitySet<OnHoldReason> getNonExpiredOnHoldReasonList(FinancialTransaction pFT) {
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                       .Where(OnHoldReason.Company().equalTo(pFT.getCompany())
                              .And(OnHoldReason.ExpirationDate().isNull()))
                       .OrderBy(OnHoldReason.OnHoldReasonCd());

        return Application.find(OnHoldReason.class, query);
    }

    @Test
    public void testFirstNSFReturn_OnHold_MinSuccessfulPayrolls() {

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));

        // verify no onholds are created
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) company);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        Application.commitUnitOfWork();
    }

    @Test
    public void testFirstNSFReturn_OnHold_MoreStrikes() {

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn_WithStrikes(true);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 4, strikes.size());

        // verify no onholds are created
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 0, onHoldReasonList.size());
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.LastChanceNotify,
                CompanyEventStatus.Active, null, null);
        assertEquals("Last Chance Notify Events", 1, events.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFirstNSFReturn_AlreadyOnHoldRiskAssessment() {

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForFirstNSFReturn_WithStrikes(false);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of txn returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        TransactionReturn txnReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        handler.execute(txnReturn);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 4, strikes.size());

        // verify onhold Risk Assessment is created
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) company);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());
        Application.commitUnitOfWork();

        // offload ReDebit
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload Company1 Payroll2 QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        // create return for ER DD DEBIT of second payroll
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        returnList = loader.persistTransactionReturns(c1FinTxns, "R01",
                "This is an NSF description");
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testAddOnHoldReasonAndReplaceHoldsToOtherPayroll(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        Company1Dataloader c1DL = new Company1Dataloader();
        Company company1 = c1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        c1DL.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payRun = c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1DL.persistPayrollRun(payRun);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 28, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        // Return the employer debit using R01
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        Company company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R01",
                "This is an NSF description");
        Application.commitUnitOfWork();

        Assert.assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        Assert.assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // verify onhold with code AchRejectR1R9 is created
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) company);

        PayrollServices.commitUnitOfWork();

        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        //Remove onhold reasons
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        assertSuccess(PayrollServices.companyManager.removeOnHoldReason(SourceSystemCode.QBOE,company.getSourceCompanyId(),ServiceSubStatusCode.AchRejectR1R9));
        PayrollServices.commitUnitOfWork();

        //Submit another payroll for Comapany : 1234567
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payRun2 = c1DL.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-10"));
        c1DL.persistPayrollRun(payRun2);
        PayrollServices.commitUnitOfWork();

        //Ensure payroll2 Transactions are not on Hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchTest002");
        DomainEntitySet<FinancialTransaction> payroll2FinTxns =
                payrollRun2.getFinancialTransactions(
                        null,
                        new TransactionStateCode[]{TransactionStateCode.Created}).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Transactions", 3, payroll2FinTxns.size());
        for(FinancialTransaction finTxn : payroll2FinTxns){
            assertEquals("OnHold ", false, finTxn.getOnHold());
        }

        //Offload the EmployerDdRedebit & EmployerFeeDebit for payroll1
        offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        // Return the EmployerDdRedebit
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        company = Company.findCompany(
                "1234567", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R01",
                "This is an NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // verify onhold with code AchRejectR1R9 is created
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        //Ensure the Payroll2 Transactions are on Hold
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        payrollRun2 = PayrollRun.findPayrollRun(company, "BatchTest002");
        payroll2FinTxns = payrollRun2.getFinancialTransactions(
                null,
                new TransactionStateCode[]{TransactionStateCode.Created}).find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Transactions", 3, payroll2FinTxns.size());

        for (FinancialTransaction finTxn : payroll2FinTxns) {
            assertEquals("OnHold ", true, finTxn.getOnHold());
        }
    }

    @Test
    public void testFirstNSFReturnR01PayrollCompleted() {
        firstNSFReturnPayrollCompleted("R01");
    }

    @Test
    public void testFirstNSFReturnR09PayrollCompleted() {
        firstNSFReturnPayrollCompleted("R09");    
    }

    private void firstNSFReturnPayrollCompleted(String pReturnReason) {
        // load all necessary data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        loader.loadDataHappyPath();

        PSPDate.setPSPTime("20070904000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070907000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070917000000");
        Application.commitUnitOfWork();

        // Run ACH Transaction processor batch job to complete the Payroll.
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        PayrollServices.beginUnitOfWork();
        processACHTxns.process("20070917");
        PayrollServices.commitUnitOfWork();

        // verify the payroll status is complete
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");
        PayrollServices.commitUnitOfWork();
        assertEquals("Payroll Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, "1234567",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Completed);

        DomainEntitySet<TransactionReturn> returnList = loader.persistTransactionReturns(c1FinTxns, pReturnReason,
                "This is an NSF description");
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of C1 EmployerDDDebit EX txns", 1, c1FinTxns.size());
        assertEquals("Number of txn returns", 1, returnList.size());

        // find the right handler and execute it
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        // verify post-conditions
        Application.beginUnitOfWork();

        // find the returned ER DD Debit FT
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);
        FinancialTransaction returnedDdDebitFT = null;
        for (FinancialTransaction ft : returnedFTs) {
            if (ft.getTransactionType().getTransactionTypeCd() == TransactionTypeCode.EmployerDdDebit) {
                returnedDdDebitFT = ft;
                break;
            }
        }
        assertTrue(returnedDdDebitFT != null);

        // payroll run status must be PendingAutoRedebit
        assertEquals("PayrollRun status",
                PayrollStatus.DebitReturned, returnedDdDebitFT.getPayrollRun().getPayrollRunStatus());

        // make sure each returned FT is now Returned, and included in a TransactionResponse
        DomainEntitySet<TransactionResponse> responses;
        for (FinancialTransaction ftReturned : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ",
                    TransactionStateCode.Returned, ftReturned.getCurrentTransactionState().getTransactionStateCd());
            responses = TransactionResponse.findTransactionResponses(ftReturned);
            assertEquals("Transaction response for Returned EmployerDdDebit transaction", 1, responses.size());
        }

        // find the fee transactions that should have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        // verify there are no fee transactions
        assertEquals("Fee Transactions", 0, feeFTs.size());

        // find the tax transactions that MAY have been created
        DomainEntitySet<FinancialTransaction> taxFTs;
        taxFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        // find the ER DD Redebit transaction that should have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                returnedDdDebitFT.getCompany().getSourceSystemCd(),
                returnedDdDebitFT.getCompany().getSourceCompanyId(),
                TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);

        // there must not be a ER DD Redebit FT
        assertEquals("DD Redebit Transactions", 0, redebitFTs.size());
        

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList((Company) returnedDdDebitFT.getCompany());
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.DebitReturned),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        Application.commitUnitOfWork();
    }

    @Test
    public void testFirstNSFReturnOffloadedDebit_WithMigratedPayrolls() {
        // load all data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        ACHReturnsDataLoader.loadDataHappyPathQBDT();
        PayrollServices.commitUnitOfWork();

        //Set the migrated payroll count as 2
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        company.getQuickbooksInfo().setAS400PayrollCount(10);
        Application.save(company);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();
                                    
        //Offload the payroll
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);        

        //Return the debit from the first payroll
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<FinancialTransaction> returnedDdFTs = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, company.getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        PSPDate.setPSPTime("20070910000000");
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(returnedDdFTs, "R01",
                "This is an NSF description");
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of C1 EmployerDdDebit txns", 1, returnedDdFTs.size());
        assertEquals("Number of returns", 1, returnList.size());

        // find the handler and call it
        PayrollServices.beginUnitOfWork();
        TransactionReturn txnReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
        txnReturn = handler.execute(txnReturn);
        PayrollServices.commitUnitOfWork();        

        // make sure the TransactionReturn is Resolved
        assertEquals("TransactionReturn status", TransactionReturnStatusCode.Resolved, txnReturn.getReturnStatusCd());

        PayrollServices.beginUnitOfWork();

        // make sure each returned FT is in the Returned state, and has a TransactionResponse
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(txnReturn);
        assertEquals("number of returned fts", 3, returnedFTs.size());
        
        FinancialTransaction returnedErDdDebitFT = returnedFTs.get(0);

        DomainEntitySet<TransactionResponse> txnResponseList;

        assertEquals("Returned FT state", TransactionStateCode.Returned,
                returnedErDdDebitFT.getCurrentTransactionState().getTransactionStateCd());
        txnResponseList = TransactionResponse.findTransactionResponses(returnedErDdDebitFT);
        assertEquals("Returned FT has TransactionResponse", 1, txnResponseList.size());


        FinancialTransaction returnedDdDebitFT = returnedFTs.get(0);

        // check the PayrollRun status
        assertEquals("Update PayrollRun Status Rule ", PayrollStatus.PendingAutoRedebit,
                returnedDdDebitFT.getPayrollRun().getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        // make sure the right company event was created
        DomainEntitySet<CompanyEvent> events;
        events = CompanyEvent.findCompanyEvents(returnedDdDebitFT.getCompany(),
                EventTypeCode.NSF,
                CompanyEventStatus.Active, null, null);
        assertEquals("Company Events", 1, events.size());
        CompanyEvent companyEvent = events.get(0);

        assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.NSFAutoRedebit),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

        Collection<String> eventDetailValue = companyEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 3, eventDetailValue.size());

        // verify no onholds are created
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(returnedDdDebitFT);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAssistedFirstNSFReturn(){
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
        PSPDate.setPSPTime("20110108000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        DomainEntitySet<FinancialTransaction> impoundTxns = company.getFinancialTransactions().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit));
        TransactionReturn transactionReturn = assertOne(ACHReturnsDataLoader.persistTransactionReturns(impoundTxns, "R01", "Return desc"));
        TransactionReturnHandler.getTransactionReturnHandler(transactionReturn).execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        // payroll run status must be PendingAutoRedebit
        assertEquals("PayrollRun status", PayrollStatus.PendingAutoRedebit, payrollRun.getPayrollRunStatus());

        // find the fee transactions that should have been created
        DomainEntitySet<FinancialTransaction> feeFTs;
        feeFTs = FinancialTransaction.findFinancialTransactions(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);
        FinancialTransaction feeTT = assertOne(feeFTs);

        // there must be a new fee FT and it must be related to this payroll run
        assertEquals("Fee Transactions", 1, feeFTs.size());
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                feeTT.getPayrollRun().getSourcePayRunId());

        // that fee must be the DebitReturnFee
        BillingDetail detail = feeFTs.get(0).getBillingDetail();
        assertEquals("Fee type", OfferingServiceChargeType.DebitReturnFee, detail.getOfferingServiceChargeType());

        // find the ER Tax Redebit transaction that should have been created
        DomainEntitySet<FinancialTransaction> redebitFTs;
        redebitFTs = FinancialTransaction.findFinancialTransactions(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                TransactionTypeCode.EmployerTaxRedebit, TransactionStateCode.Created);

        // there must be a new ER Tax Redebit FT
        assertEquals("Tax Redebit Transactions", 1, redebitFTs.size());
        assertEquals("Payroll Run", payrollRun.getSourcePayRunId(),
                redebitFTs.get(0).getPayrollRun().getSourcePayRunId());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.NSF, CompanyEventStatus.Active, null, null);

        //Assertion for Create NSF System Event Rule - NSFSubType - NSFAutoRedebit
        assertEquals("Company Events", 1, companyEventsList.size());

        CompanyEvent nsfEvent = companyEventsList.get(0);

        assertEquals("NSF Sub Type", EnumUtils.getReadableName(NSFSubTypeType.NSFAutoRedebit),
                nsfEvent.getCompanyEventDetailValue(EventDetailTypeCode.NSFSubType));
        assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.NSF),
                nsfEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));


        Collection<String> eventDetailValue = nsfEvent.getCompanyEventDetailValues(EventDetailTypeCode.FinancialTransactionId);
        assertEquals("number of event details", 3, eventDetailValue.size());

        boolean found = false;
        for (String currValue : eventDetailValue) {
            if(impoundTxns.find(FinancialTransaction.Id().equalTo(SpcfUniqueId.createInstance(currValue))).size() > 0) {
                found = true;
            }
        }
        assertTrue("Financial transaction id associated with event", found);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txnReturn = TransactionReturn.findTransactionReturns(payrollRun.getSourcePayRunId(), company);
        PayrollServices.commitUnitOfWork();

        assertEquals("There is one txn return", 1, txnReturn.size());

        // make sure the TransactionReturn is Resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved, txnReturn.get(0).getReturnStatusCd());

        PayrollServices.beginUnitOfWork();

        // verify onhold is created with code AchRejectR1R9
        DomainEntitySet<OnHoldReason> onHoldReasonList = getNonExpiredOnHoldReasonList(company);
        assertEquals("Number of On hold reasons", 1, onHoldReasonList.size());
        assertEquals("On hold status", ServiceSubStatusCode.AchRejectR1R9, onHoldReasonList.get(0).getOnHoldReasonCd());

        // make sure the right strike was created
        DomainEntitySet<CompanyEvent> strikes;
        strikes = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.Strike,
                CompanyEventStatus.Active, null, null);
        assertEquals("Strike Events", 1, strikes.size());
        CompanyEvent strike = strikes.get(0);

        assertEquals("Strike Reason", EnumUtils.getReadableName(StrikeReason.NSFAutoRedebit),
                strike.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        PayrollServices.commitUnitOfWork();

        //Assertion for UncollectedTaxAmount
        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        HashMap<FinancialTransaction, SpcfMoney> taxAmounts = payrollRun.getUncollectedTaxAmount();
        FinancialTransaction erTaxDebitTxn = payrollRun.getEmployerTaxDebitTransaction();
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of Tax txns", 1, taxAmounts.size());
        for (FinancialTransaction taxTransaction : taxAmounts.keySet()) {
            assertEquals("Financial Transaction", erTaxDebitTxn.getId(), taxTransaction.getId());
            assertEquals("Uncollected Tax Amount ", erTaxDebitTxn.getFinancialTransactionAmount(), taxAmounts.get(taxTransaction));
        }
    }

    private void remove0DollarTransactions(ArrayList<Object[]> pTransactions) {
        for (Iterator<Object[]> iterator = pTransactions.iterator(); iterator.hasNext(); ) {
            Object[] next = iterator.next();
            if(((SpcfMoney)next[12]).isZero()) {
                iterator.remove();
            }
        }
    }

    /**
     * Debit Only - NSF Return for QBDT Symphony Company
     *
     */
    @Test
    public void testQBDTSymphonyFirstNSFReturn() {
        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";

        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturned(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS), "R01", "NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = transactionReturnHelper.findReturnedFinancialTransactions(company, sourcePayrollRunId);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.PendingAutoRedebit);

        transactionReturnVerifier.verifyDebitReturnFee(company, payrollRun);

        transactionReturnVerifier.verifyRedebitFinancialTransactions(company, payrollRun);

        transactionReturnVerifier.verifyNSFCompanyEvent(company, returnedFinancialTransactions, NSFSubTypeType.NSFAutoRedebit);

        transactionReturnVerifier.verifyTransactionReturn(company, payrollRun, TransactionReturnStatusCode.Resolved);

        transactionReturnVerifier.verifyStrikeEvent(company, ServiceSubStatusCode.AchRejectR1R9, StrikeReason.NSFAutoRedebit);

        Map<EventEmailParamTypeCode, String> emailParamTypeCodeStringMap = new HashMap<>();
        emailParamTypeCodeStringMap.put(EventEmailParamTypeCode.PayrollAdminEmail, "PayrollAdmin@aol.com");
        emailParamTypeCodeStringMap.put(EventEmailParamTypeCode.FailureReason, "Insufficient Funds");
        emailParamTypeCodeStringMap.put(EventEmailParamTypeCode.PayrollDebitAmount, "$777.77");
        emailParamTypeCodeStringMap.put(EventEmailParamTypeCode.IntuitHandlingFee, "$100.08");
        emailParamTypeCodeStringMap.put(EventEmailParamTypeCode.AdjustedPayrollDebitAmount, "$877.85");
        emailParamTypeCodeStringMap.put(EventEmailParamTypeCode.CompanyBankAccountLastFour, "4747");
        emailParamTypeCodeStringMap.put(EventEmailParamTypeCode.NextBusinessDate, "October 9, 2007");

        transactionReturnVerifier.verifyCompanyEventEmail(company, EventTypeCode.NSF, EventEmailTemplateTypeCode.AutoRedebit3, emailParamTypeCodeStringMap);

        Application.rollbackUnitOfWork();
    }


    @Test
    public void testQBDTSymphonyFirstNSFReturnInactiveCBA() {
        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";

        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturnedCBADeactivated(AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS),"R01", "NSF description");

        // verify post-conditions
        Application.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = transactionReturnHelper.findReturnedFinancialTransactions(company, sourcePayrollRunId);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyPayrollStatus(payrollRun, PayrollStatus.DebitReturned);

        transactionReturnVerifier.verifyNoDebitReturnFee(company);

        transactionReturnVerifier.verifyDDDebitReturnCompanyEvent(company, returnedFinancialTransactions);

        transactionReturnVerifier.verifyTransactionReturn(company, payrollRun, TransactionReturnStatusCode.Open);

        transactionReturnVerifier.verifyStrikeEvent(company, ServiceSubStatusCode.AchRejectR1R9, StrikeReason.NSFAutoRedebit);

        Application.rollbackUnitOfWork();
    }


}
