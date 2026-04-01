package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.FakeSalesTaxGateway;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.*;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.hibernate.StoredProcedures;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company123272727DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;


/**
 * User: jjones1
 * Date: Oct 30, 2009
 * Time: 2:17:23 PM
 */
public class PayrollRunAdapterTests {

    private PayrollRunAdapter mPayrollRunAdapter;

    public PayrollRunAdapterTests() {
        this.mPayrollRunAdapter = new PayrollRunAdapter();
    }


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        FlexUnitDataLoaderService.AddUsers();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFindPayrollRuns() {

        String psid = "1234567";
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-01-22"), new DateDTO("2011-02-02"));

        ArrayList<String> payrollRunTypes = new ArrayList<String>();
        payrollRunTypes.add(PayrollType.CloudOnly.toString());
        ArrayList<SAPPayrollRun> sapPayrollRuns = null;
        try {
            sapPayrollRuns = mPayrollRunAdapter.findPayrollRunsByDate(psid, "QBDT", payrollRunTypes, null, null);
        } catch (Throwable t) {
            fail(t.getMessage());
        }

        assertEquals("PayrollRun List Count", 1, sapPayrollRuns.size());
        SAPPayrollRun sapPayrollRun = sapPayrollRuns.iterator().next();

        assertEquals("PayrollNetAmount", 183.00, sapPayrollRun.getPayrollNetAmount(), 0);
        assertEquals("SourcePayRunId", payrollRunDTO.getPayrollTXBatchId(), sapPayrollRun.getSourcePayRunId());
        assertNull("StatusEffectiveDate", sapPayrollRun.getStatusEffectiveDate());
        assertEquals("PayrollRunStatus", PayrollStatus.Pending, sapPayrollRun.getPayrollRunStatus());
        assertEquals("CompanyId", psid, sapPayrollRun.getCompanyId());
        assertEquals("SourceSystemId", "QBDT", sapPayrollRun.getSourceSystemId());
        assertNull("CollectionStage", sapPayrollRun.getCollectionStage());
        assertNull("WireExpectedDate", sapPayrollRun.getWireExpectedDate());
        assertNull("ExpectedResolutionDate", sapPayrollRun.getExpectedResolutionDate());
        assertFalse("HasVoidedPaycheck", sapPayrollRun.getHasVoidedPaycheck());
        assertFalse("isHPDE", sapPayrollRun.isHPDE());

        SAPCompanyBankAccount sapCompanyBankAccount = sapPayrollRun.getBankAccount();
        assertNull("AccountId", sapCompanyBankAccount.getAccountId());
        assertTrue("AccountNumber", sapCompanyBankAccount.getAccountNumber().startsWith("ACCNT_"));
        assertEquals("RoutingNumber", "111000025", sapCompanyBankAccount.getRoutingNumber());
        assertTrue("BankName", sapCompanyBankAccount.getBankName().startsWith("TestBank_"));
        assertEquals("AccountType", BankAccountType.Checking, sapCompanyBankAccount.getAccountType());
        assertNull("BankAccountStatusCd", sapCompanyBankAccount.getBankAccountStatusCd());
        assertEquals("VerifyRetryCount", 0, sapCompanyBankAccount.getVerifyRetryCount());
        assertNull("SourceBankAccountName", sapCompanyBankAccount.getSourceBankAccountName());
        assertNull("SourceBankAccountId", sapCompanyBankAccount.getSourceBankAccountId());

        ArrayList<SAPActionEvent> sapActionEvents = sapPayrollRun.getActionCollection();
        assertEquals("", 2, sapActionEvents.size());


    }

    @Test
    public void testFindPayrollRun() {
        String psid = "1234567";
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-01-22"), new DateDTO("2011-02-02"));

        SAPPayrollRun sapPayrollRun = null;
        try {
            sapPayrollRun = mPayrollRunAdapter.findPayrollRun("QBDT", psid, payrollRunDTO.getPayrollTXBatchId());
        } catch (Throwable t) {
            fail(t.getMessage());
        }

        assertEquals("PayrollNetAmount", 183.00, sapPayrollRun.getPayrollNetAmount(), 0);
        assertEquals("SourcePayRunId", payrollRunDTO.getPayrollTXBatchId(), sapPayrollRun.getSourcePayRunId());
        assertNull("StatusEffectiveDate", sapPayrollRun.getStatusEffectiveDate());
        assertEquals("PayrollRunStatus", PayrollStatus.Pending, sapPayrollRun.getPayrollRunStatus());
        assertEquals("CompanyId", psid, sapPayrollRun.getCompanyId());
        assertEquals("SourceSystemId", "QBDT", sapPayrollRun.getSourceSystemId());
        assertNull("CollectionStage", sapPayrollRun.getCollectionStage());
        assertNull("WireExpectedDate", sapPayrollRun.getWireExpectedDate());
        assertNull("ExpectedResolutionDate", sapPayrollRun.getExpectedResolutionDate());
        assertFalse("HasVoidedPaycheck", sapPayrollRun.getHasVoidedPaycheck());
        assertFalse("isHPDE", sapPayrollRun.isHPDE());

        SAPCompanyBankAccount sapCompanyBankAccount = sapPayrollRun.getBankAccount();
        assertNull("AccountId", sapCompanyBankAccount.getAccountId());
        assertTrue("AccountNumber", sapCompanyBankAccount.getAccountNumber().startsWith("ACCNT_"));
        assertEquals("RoutingNumber", "111000025", sapCompanyBankAccount.getRoutingNumber());
        assertTrue("BankName", sapCompanyBankAccount.getBankName().startsWith("TestBank_"));
        assertEquals("AccountType", BankAccountType.Checking, sapCompanyBankAccount.getAccountType());
        assertNull("BankAccountStatusCd", sapCompanyBankAccount.getBankAccountStatusCd());
        assertEquals("VerifyRetryCount", 0, sapCompanyBankAccount.getVerifyRetryCount());
        assertNull("SourceBankAccountName", sapCompanyBankAccount.getSourceBankAccountName());
        assertNull("SourceBankAccountId", sapCompanyBankAccount.getSourceBankAccountId());

        ArrayList<SAPActionEvent> sapActionEvents = sapPayrollRun.getActionCollection();
        assertEquals("", 2, sapActionEvents.size());
    }

    @Test
    public void testCancelPayrollTransaction() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
            DomainEntitySet<FinancialTransaction> financialTransactions =  FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployeeDdCredit);

            ArrayList<String> transIds = new ArrayList<String>();
            for (FinancialTransaction financialTransaction : financialTransactions) {
                transIds.add(financialTransaction.getPaycheckSplit().getSourceDdTxnId());
            }
            PayrollServices.rollbackUnitOfWork();

            mPayrollRunAdapter.cancelPayrollTransaction("1234567", "QBOE", transIds, "BatchTest05");
        } catch (Throwable t) {
            Assert.fail(t.getMessage());
            t.printStackTrace();
        }
    }
    @Test
    public void testLedgerAccountBalance() throws Throwable {
        //this mainly tests LedgerAccount.getLedgerAccountBalance

        //do stuff with the ledger, make sure the balance is calculated right before and after the batch job is run

        //initialize data
        PayrollServices.beginUnitOfWork();
        Company123272727DataLoader dataloader = new Company123272727DataLoader();

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        //find the stupid bank verification amounts
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        SpcfMoney bankVerificationTotal = new SpcfMoney("0.00");
        for (FinancialTransaction bankFinTxn : company.getCompanyBankAccountCollection().get(0).getVerificationTransactions()) {
            bankVerificationTotal = new SpcfMoney(bankVerificationTotal.add(bankFinTxn.getFinancialTransactionAmount()));
        }

        SpcfMoney[] paycheckAmounts = new SpcfMoney[payrollRunDTO.getPaychecks().size()];
        SpcfMoney payrollAmount = new SpcfMoney("0.00");
        int paycheckNumber=0;
        for (PaycheckDTO paycheck : payrollRunDTO.getPaychecks()) {
            paycheckAmounts[paycheckNumber++] = paycheck.getPaycheckNetAmount();
            payrollAmount = new SpcfMoney(payrollAmount.add(paycheck.getPaycheckNetAmount()));
        }


        dataloader.savePayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();


        assertLedgerAccountsBeforeAndAfterBatchJob("123272727", "QBOE"
                , new CodeBalance(LedgerAccountCode.DDFutureLiability, payrollAmount)
                , new CodeBalance(LedgerAccountCode.DDFutureReceivable, payrollAmount)
                , new CodeBalance(LedgerAccountCode.FeeCashRevenue, bankVerificationTotal)
                , new CodeBalance(LedgerAccountCode.FeeIncome, bankVerificationTotal)
        );

        //Offload Employer Debit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloadBatchJob = new OffloadACHTransactions();
        offloadBatchJob.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, PSPDate.getPSPTime());

        assertLedgerAccountsBeforeAndAfterBatchJob("123272727", "QBOE"
                ,new CodeBalance(LedgerAccountCode.DDCurrentCash, payrollAmount)
                ,new CodeBalance(LedgerAccountCode.DDCurrentLiability, payrollAmount)
                ,new CodeBalance(LedgerAccountCode.FeeCashRevenue, bankVerificationTotal)
                ,new CodeBalance(LedgerAccountCode.FeeIncome, bankVerificationTotal)
        );

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 26, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        //Persist the Transaction Return
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "Non-NSF return");
        PayrollServices.commitUnitOfWork();

        //Call TransactionReturn Handler for Generic Debit Return
        PayrollServices.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        Application.refresh(transactionReturn);
        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        assertLedgerAccountsBeforeAndAfterBatchJob("123272727", "QBOE"
                , new CodeBalance(LedgerAccountCode.DDCurrentCash, payrollAmount)
                , new CodeBalance(LedgerAccountCode.DDCurrentLiability, payrollAmount)
                , new CodeBalance(LedgerAccountCode.ERReturnCash, payrollAmount)
                , new CodeBalance(LedgerAccountCode.ERReturnReceivable, payrollAmount)
                , new CodeBalance(LedgerAccountCode.FeeCashRevenue, bankVerificationTotal)
                , new CodeBalance(LedgerAccountCode.FeeIncome, bankVerificationTotal)
        );

    }

    @Test
    public void testLedgerAccountBalanceRedebits() throws Throwable {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        //find the stupid bank verification amounts
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        SpcfMoney bankVerificationTotal = new SpcfMoney("0.00");
        for (FinancialTransaction bankFinTxn : company.getCompanyBankAccountCollection().get(0).getVerificationTransactions()) {
            bankVerificationTotal = new SpcfMoney(bankVerificationTotal.add(bankFinTxn.getFinancialTransactionAmount()));
        }
        PayrollServices.commitUnitOfWork();

        assertLedgerAccountsBeforeAndAfterBatchJob("8574536", "QBDT"
                ,new CodeBalance(LedgerAccountCode.ERReturnCash, new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2).add(SpcfDecimal.createInstance(777.85))))
                ,new CodeBalance(LedgerAccountCode.ERReturnReceivable, new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2).add(SpcfDecimal.createInstance(777.85))))
                ,new CodeBalance(LedgerAccountCode.FeeCashRevenue, new SpcfMoney(bankVerificationTotal.add(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2))))
                ,new CodeBalance(LedgerAccountCode.FeeIncome, new SpcfMoney(bankVerificationTotal.add(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2))))
                ,new CodeBalance(LedgerAccountCode.SalesAndUseTax, 0.08)
        );

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 1, feeHashMap.size());
        assertEquals("Number of sales tax txns", 1, salesTaxHashMap.size());

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction feeTxn = feeHashMap.keySet().iterator().next();
        FinancialTransaction taxTxn = salesTaxHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("700.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("2.09"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.03"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        //offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        assertLedgerAccountsBeforeAndAfterBatchJob("8574536", "QBDT"
                ,new CodeBalance(LedgerAccountCode.ERReturnCash, new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2).add(SpcfDecimal.createInstance(75.73))))
                ,new CodeBalance(LedgerAccountCode.ERReturnReceivable, new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2).add(SpcfDecimal.createInstance(75.73))))
                ,new CodeBalance(LedgerAccountCode.FeeCashRevenue, new SpcfMoney(bankVerificationTotal.add(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(2))))
                ,new CodeBalance(LedgerAccountCode.FeeIncome, new SpcfMoney(bankVerificationTotal.add(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2))))
                ,new CodeBalance(LedgerAccountCode.SalesAndUseTax, 0.08)
        );

        //complete the txns
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071114000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        processACHTxns.process("20071114");
        PayrollServices.commitUnitOfWork();

        //ensure payrollrun status is back to debit returned
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.DebitReturned, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        //Do another addOrEditRedebit
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        payrollHashMap = payrollRun.getUncollectedDDAmount();
        feeHashMap = payrollRun.getUncollectedFeeAmounts();
        salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 1, feeHashMap.size());
        assertEquals("Number of sales tax txns", 1, salesTaxHashMap.size());

        payrolltxn = payrollHashMap.keySet().iterator().next();
        feeTxn = feeHashMap.keySet().iterator().next();
        taxTxn = salesTaxHashMap.keySet().iterator().next();

        collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("77.77"), new DateDTO(PSPDate.getPSPTime()));
        feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("1.41"), new DateDTO(PSPDate.getPSPTime()));
        taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.05"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("2nd redebit", procResult);

        //offload
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        assertLedgerAccountsBeforeAndAfterBatchJob("8574536", "QBDT"
                ,new CodeBalance(LedgerAccountCode.FeeCashRevenue, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("3.58"))))
                ,new CodeBalance(LedgerAccountCode.FeeIncome, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("3.50"))))
                ,new CodeBalance(LedgerAccountCode.SalesAndUseTax, 0.08)
        );

        //complete the txns
        PayrollServices.beginUnitOfWork();
        processACHTxns = new ProcessACHTransactions();
        processACHTxns.process("20071214");
        PayrollServices.commitUnitOfWork();

        //ensure payrollrun status is now Complete
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();


        //run a new payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        PayrollRunDTO newPayroll = ACHReturnsDataLoader.loadPayrollQBDT_NoBankAccountChange();
        ProcessResult<PayrollRun> newPRPR = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "8574536", newPayroll);
        assertSuccess(newPRPR);
        PayrollServices.commitUnitOfWork();

        assertLedgerAccountsBeforeAndAfterBatchJob("8574536", "QBDT"
                ,new CodeBalance(LedgerAccountCode.DDFutureLiability, 11.11)
                ,new CodeBalance(LedgerAccountCode.DDFutureReceivable, 11.11)
                ,new CodeBalance(LedgerAccountCode.FeeCashRevenue, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("3.58"))))
                ,new CodeBalance(LedgerAccountCode.FeeIncome, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("3.50"))))
                ,new CodeBalance(LedgerAccountCode.SalesAndUseTax, 0.08)
        );

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        assertLedgerAccountsBeforeAndAfterBatchJob("8574536", "QBDT"
                ,new CodeBalance(LedgerAccountCode.FeeCashRevenue, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("7.16"))))
                ,new CodeBalance(LedgerAccountCode.FeeIncome, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("7.00"))))
                ,new CodeBalance(LedgerAccountCode.SalesAndUseTax, 0.16)
        );


        String txnId=null;
        for (Paycheck paycheck : newPRPR.getResult().getPaycheckCollection()) {
            if (paycheck.getNetAmount().equals(new SpcfMoney("2.23"))) {
                txnId = paycheck.getPaycheckSplits().get(0).getSourceDdTxnId();
            }
        }

        ArrayList<String> transactionIds = new ArrayList<String>();
        transactionIds.add(txnId);

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();
        new PayrollRunAdapter().reversePayrollRunTransactions("QBDT", "8574536", transactionIds, newPRPR.getResult().getSourcePayRunId(), true, null, "ACH", false);

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        assertLedgerAccountsBeforeAndAfterBatchJob("8574536", "QBDT"
                ,new CodeBalance(LedgerAccountCode.EEReturnCash, 2.23)
                ,new CodeBalance(LedgerAccountCode.EEReturnLiablility, 2.23)
                ,new CodeBalance(LedgerAccountCode.FeeCashRevenue, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("82.24"))))
                ,new CodeBalance(LedgerAccountCode.FeeIncome, new SpcfMoney(bankVerificationTotal.add(new SpcfMoney("82.00"))))
                ,new CodeBalance(LedgerAccountCode.SalesAndUseTax, 0.24)
        );
    }

    /**
     * Tests
     * FT With Null Credit Account:
     * PSRV002385
     *
     * @throws Throwable
     */
    @Test
    public void testFTWithNullCreditAccount() throws Throwable {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        List<Employee> employees = DataLoadServices.addEEs(company, 3);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        /*  Create & submit first payroll    */
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-02-04"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"7000", "2500", "4000", "5000"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110204000000");
        PayrollServices.commitUnitOfWork();
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /*  Void the payroll before payment */
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "Payroll_1");
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

        /*  Create & submit second payroll    */
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-02-11"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"10000", "5000", "8000", "7500"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110209000000");
        PayrollServices.commitUnitOfWork();
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /*  Test the adapter for any errors */
        try {
            SpcfCalendar fewMonthsBack=PSPDate.getPSPTime();
            fewMonthsBack.addMonths(-3);
            List<SAPMoneyMovementTransaction> mmts = new PayrollRunAdapter().findMoneyMovementTransactions(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), new Date(fewMonthsBack.getTimeInMilliseconds()));
            assertEquals("Incorrect number of MMTs",1,mmts.size());
        }
        catch (Throwable pThrowable) {
            fail("Error retrieving MMTs.");
        }
    }

    @Test
    public void testFindMoneyMovementTransactionsExcludesDDCreditMMTs() throws Throwable {
        DataLoadServices.setPSPDate(2010, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        //activation creates a payroll to use

        DataLoadServices.setPSPDate(2010, 1, 4);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2010, 1, 7);
        DataLoadServices.runOffload();

        //make sure EE transactions really did offload
        PayrollServices.beginUnitOfWork();
        assertEquals(1, FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed).size());
        PayrollServices.rollbackUnitOfWork();

        ArrayList<SAPMoneyMovementTransaction> moneyMovementTransactions = new PayrollRunAdapter().findMoneyMovementTransactions(company.getSourceSystemCd().name(),
                company.getSourceCompanyId(),
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2009, 1, 1)));

        SAPMoneyMovementTransaction sapMoneyMovementTransaction = assertOne(moneyMovementTransactions);
        assertEquals(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16().add(new SpcfMoney("1.00")), SAPTranslator.getSpcfMoneyFromDoubleNoSentinel(sapMoneyMovementTransaction.getAchAmount()));

    }

    //if not specified, must be 0
    private void assertLedgerAccounts(ArrayList<SAPCompanyLedgerAccount> actual, CodeBalance... expected) {
        for (SAPCompanyLedgerAccount account : actual) {
            double expectedBalance=0.;
            for (CodeBalance codeBalance : expected) {
                if (codeBalance.ledgerAccountCode == account.getLedgerAccountCode()) {
                    expectedBalance = codeBalance.balance;
                }
            }
            assertEquals(account.getName(), expectedBalance, account.getBalance(), 0.001);
        }
    }

    private void assertLedgerAccountsBeforeAndAfterBatchJob(String sourceCompanyId, String sourceSystemCd, CodeBalance... expected) throws Throwable {
        ArrayList<SAPCompanyLedgerAccount> actual = new PayrollRunAdapter().findLedgerAccounts(sourceCompanyId, sourceSystemCd);
        assertLedgerAccounts(actual, expected);
        PayrollServices.beginUnitOfWork();
        Application.executeSqlProcedure(StoredProcedures.PRC_UPDATE_LEDGER_BALANCE, true);
        PayrollServices.commitUnitOfWork();
        actual = new PayrollRunAdapter().findLedgerAccounts(sourceCompanyId, sourceSystemCd);
        assertLedgerAccounts(actual, expected);
    }

    private class CodeBalance {
        public LedgerAccountCode ledgerAccountCode;
        public double balance;

        private CodeBalance(LedgerAccountCode ledgerAccountCode, double balance) {
            this.ledgerAccountCode = ledgerAccountCode;
            this.balance = balance;
        }

        private CodeBalance(LedgerAccountCode ledgerAccountCode, SpcfMoney balance) {
            this.ledgerAccountCode = ledgerAccountCode;
            this.balance = Double.parseDouble(balance.toString());
        }
    }

    @Ignore("This test is just for data setup--no real asserts")
    @Test
    public void testManyPayrolls() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 14, 4, 4, 4, 0));
        PayrollServices.commitUnitOfWork();
        Company c = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);
        List<Employee> ees = DataLoadServices.addEEs(c, 10, true, false);


        for (int i=0; i< 100; i++) {
            PayrollServices.beginUnitOfWork();
            List<Employee> payrollEEs = new ArrayList<Employee>();
            for (int j=0; j< i%10+1; j++) {
                payrollEEs.add(ees.get(j));
            }
            PayrollRunDTO dto = DataLoadServices.createDDPayrollRun(c, new DateDTO(SpcfCalendar.createInstance(2010, 10, 20, 4, 4, 4, 0)), payrollEEs);
            assertSuccess(PayrollServices.payrollManager.submitPayroll(c.getSourceSystemCd(), c.getSourceCompanyId(), dto));
            PayrollServices.commitUnitOfWork();
        }

    }
    @Test
    public void testYearOrderInYtDQTD() {
        DataLoadServices.reinitialize();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 14, 4, 4, 4, 0));
        PayrollServices.commitUnitOfWork();
        String psid=  "1234567";
        Company c = DataLoadServices.newCompany(SourceSystemCode.QBDT,psid,true, ServiceCode.DirectDeposit);
        List<Employee> ees = DataLoadServices.addEEs(c, 10, true, false);
        Employee ee= c.getEmployees().find(Employee.SourceEmployeeId().equalTo("2")).getFirst();
        List<Employee> payrollEEs = new ArrayList<Employee>();
        payrollEEs.add(ee) ;
        for (int i=0; i< 1; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO dto = DataLoadServices.createDDPayrollRun(c, new DateDTO(SpcfCalendar.createInstance(2010, 10, 20, 4, 4, 4, 0)), payrollEEs);
            assertSuccess(PayrollServices.payrollManager.submitPayroll(c.getSourceSystemCd(), c.getSourceCompanyId(), dto));
            PayrollServices.commitUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 10, 14, 4, 4, 4, 0));
        PayrollServices.commitUnitOfWork();
        for (int i=0; i< 1; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO dto = DataLoadServices.createDDPayrollRun(c, new DateDTO(SpcfCalendar.createInstance(2011, 10, 20, 4, 4, 4, 0)), payrollEEs);
            assertSuccess(PayrollServices.payrollManager.submitPayroll(c.getSourceSystemCd(), c.getSourceCompanyId(), dto));
            PayrollServices.commitUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 10, 14, 4, 4, 4, 0));
        PayrollServices.commitUnitOfWork();
        for (int i=0; i< 1; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO dto = DataLoadServices.createDDPayrollRun(c, new DateDTO(SpcfCalendar.createInstance(2012, 10, 20, 4, 4, 4, 0)), payrollEEs);
            assertSuccess(PayrollServices.payrollManager.submitPayroll(c.getSourceSystemCd(), c.getSourceCompanyId(), dto));
            PayrollServices.commitUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 10, 14, 4, 4, 4, 0));
        PayrollServices.commitUnitOfWork();
        for (int i=0; i< 1; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO dto = DataLoadServices.createDDPayrollRun(c, new DateDTO(SpcfCalendar.createInstance(2013, 10, 20, 4, 4, 4, 0)), payrollEEs);
            assertSuccess(PayrollServices.payrollManager.submitPayroll(c.getSourceSystemCd(), c.getSourceCompanyId(), dto));
            PayrollServices.commitUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 10, 14, 4, 4, 4, 0));
        PayrollServices.commitUnitOfWork();
        for (int i=0; i< 1; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollRunDTO dto = DataLoadServices.createDDPayrollRun(c, new DateDTO(SpcfCalendar.createInstance(2014, 10, 20, 4, 4, 4, 0)), payrollEEs);
            assertSuccess(PayrollServices.payrollManager.submitPayroll(c.getSourceSystemCd(), c.getSourceCompanyId(), dto));
            PayrollServices.commitUnitOfWork();
        }
        EmployeeAdapter empAd=new EmployeeAdapter();
        try {
            List<SAPEmployeeLineItemYear> employeeLineItemYear= empAd.getEmployeeProfileQTDYTDDetails(SourceSystemCode.QBDT.name().toString(), psid, ee.getSourceEmployeeId());
            assertNotNull("Failed to assert QTD/YTD values in order as result is null",employeeLineItemYear);
            int startYear=2010;
            for(SAPEmployeeLineItemYear sAPEmployeeLineItemYear:employeeLineItemYear){
                assertEquals("QTD/YTD Years are not in order",startYear,sAPEmployeeLineItemYear.getYear());
                startYear++;
            }
        } catch (Throwable pThrowable) {
            assertTrue("Failed to assert QTD/YTD values in order",false);
        }
    }

    @Test
    public void testFindTransactionsByLedgerAccount_StateIsPostingRule() throws Throwable {
        DataLoadServices.setPSPDate(2010, 12, 29);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.runOffload();
        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRuns(company).findEntity(PayrollRun.PayrollDirectDepositAmount().equalTo(new SpcfMoney("1.0")));
        assertEquals(TransactionStateCode.Completed, payrollRun.getDdDebit().getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();
        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
        ArrayList<SAPPayrollTransaction> sapPayrollTransactionList = payrollRunAdapter.findTransactionsByLedgerAccountAndPayroll(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), LedgerAccountCode.DDCurrentCash.toString(), payrollRun.getSourcePayRunId());
        assertEquals(2, sapPayrollTransactionList.size());
        for(SAPPayrollTransaction sapPayrollTransaction:sapPayrollTransactionList) {
            assertEquals(TransactionStateCode.Executed, sapPayrollTransaction.getStatus());
        }
        sapPayrollTransactionList = payrollRunAdapter.findTransactionsByLedgerAccount(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), LedgerAccountCode.DDCurrentCash.toString());
        assertEquals(2, sapPayrollTransactionList.size());
        for(SAPPayrollTransaction sapPayrollTransaction:sapPayrollTransactionList) {
            assertEquals(TransactionStateCode.Executed, sapPayrollTransaction.getStatus());
        }
    }

    @Test
    public void testAddAndCancelFees() throws Throwable {
        DataLoadServices.setPSPDate(2012, 10, 10);
        String psid = "12345";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        CompanyAdapter companyAdapter = new CompanyAdapter();
        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();

        ArrayList<SAPOfferingServiceChargePrice> sapOfferingServiceChargePrices = companyAdapter.getFeeOfferingServiceChargePrices(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null);
        for (SAPOfferingServiceChargePrice sapOfferingServiceChargePrice : sapOfferingServiceChargePrices) {
            sapOfferingServiceChargePrice.setChecked(true);
            sapOfferingServiceChargePrice.setChargedPrice(10.00);
            sapOfferingServiceChargePrice.setMemo(sapOfferingServiceChargePrice.getDisplayName());
        }
        payrollRunAdapter.addFeeTransactions(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), null, "ACH", new Date(2012, 10, 10), sapOfferingServiceChargePrices);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = assertOne(Application.find(PayrollRun.class, PayrollRun.PayrollRunType().equalTo(PayrollType.FeeOnly)))
                .getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit));
        PayrollServices.rollbackUnitOfWork();

        for (FinancialTransaction financialTransaction : financialTransactions) {
            payrollRunAdapter.cancelTransaction(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), financialTransaction.getId().toString());
        }
    }
    @Test
    public void testAddFeesLimit() throws Throwable {
        int oldLimitValue = 500;
        try {
            Application.beginUnitOfWork();
            oldLimitValue = SystemParameter.findIntValue(SystemParameter.Code.DEFAULT_EMPLOYER_FEE_LIMIT);
            SystemParameter.update(SystemParameter.Code.DEFAULT_EMPLOYER_FEE_LIMIT, String.valueOf(500));
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        DataLoadServices.setPSPDate(2012, 10, 10);
        String psid = "12345";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);

        CompanyAdapter companyAdapter = new CompanyAdapter();
        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();

        ArrayList<SAPOfferingServiceChargePrice> sapOfferingServiceChargePrices = companyAdapter.getFeeOfferingServiceChargePrices(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null);
        for (SAPOfferingServiceChargePrice sapOfferingServiceChargePrice : sapOfferingServiceChargePrices) {
            sapOfferingServiceChargePrice.setChecked(true);
            sapOfferingServiceChargePrice.setChargedPrice(501.00);
            sapOfferingServiceChargePrice.setMemo(sapOfferingServiceChargePrice.getDisplayName());
        }

        try {
            payrollRunAdapter.addFeeTransactions(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), null, "ACH", new Date(2012, 10, 10), sapOfferingServiceChargePrices);
            org.junit.Assert.fail("Expected Exception");
        } catch (SAPException e) {
            assertEquals("The fee amount you have entered is higher than the permissible limit. Enter an amount less than $500 to create the entry.",
                    e.getMessage());
        }
        try {
            Application.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.DEFAULT_EMPLOYER_FEE_LIMIT,String.valueOf(oldLimitValue));
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    @Test
    public void testSupersededPaycheckIds() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-12-01"));

        //guid
        assertFalse(assertOne(new PayrollRunAdapter().findPayrollRunsByDate(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), null, null, null)).getIsSuperseded());

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        payrollRun.getPaycheckCollection().getFirst().setSourcePaycheckId("979");
        PayrollServices.commitUnitOfWork();
        assertFalse(assertOne(new PayrollRunAdapter().findPayrollRunsByDate(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), null, null, null)).getIsSuperseded());

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        payrollRun.getPaycheckCollection().getFirst().setSourcePaycheckId("-5678");
        PayrollServices.commitUnitOfWork();
        assertTrue(assertOne(new PayrollRunAdapter().findPayrollRunsByDate(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), null, null, null)).getIsSuperseded());


        //PSRV004026
        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        payrollRun.getPaycheckCollection().getFirst().setSourcePaycheckId("1234-5678");
        PayrollServices.commitUnitOfWork();
        assertFalse(assertOne(new PayrollRunAdapter().findPayrollRunsByDate(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), null, null, null)).getIsSuperseded());


    }

    @Test
    public void testNegativePaychecks() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-12-01"));

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        payrollRun.getPaycheckCollection().getFirst().setSourcePaycheckId("-5678");

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        int paycheckSize = Paycheck.findNonSupersededPaychecksByEmployee(company, payrollRun.getPaycheckCollection().getFirst().getSourceEmployee(), null, null).size();

        PayrollServices.commitUnitOfWork();

        assertNotSame("Negative Paychecks included for Employees", 1, paycheckSize);
    }

    private Company company;
    private PayrollRun payrollRun;
    private void setupCompanyForReturnWithWire() {
        //PSP-3946
        DataLoadServices.setPSPDate(2013, 1, 1);
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRuns(company).findEntity(PayrollRun.PayrollRunStatus().equalTo(PayrollStatus.Pending));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 2);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 12);
        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> fts = payrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax)
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Executed)));
        Application.rollbackUnitOfWork();
        DataLoadServices.returnTxns(fts, "R16", "unspeakable horrible");
    }

    @Test
    public void testCanRecordWireAndFeeAndNotGenerateBalanceEmail() throws Throwable {
        setupCompanyForReturnWithWire();

        SAPPayrollBillingTransactions sapPayrollBillingTransactions = assertOne(new PayrollRunAdapter().findPayrollUncollectedBalances(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), payrollRun.getSourcePayRunId()));
        SAPBillingTransaction ddTransaction = assertOne(sapPayrollBillingTransactions.getDdTransactions());
        assertEquals(1.0, ddTransaction.getFinancialAmount(), 0.00001);

        SAPBillingTransaction feeTransaction = assertOne(sapPayrollBillingTransactions.getFeeTransactions());
        double feeAmount = Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString());
        assertEquals(feeAmount, feeTransaction.getFinancialAmount(), 0.00001);
        double feeSalesAmount = Double.parseDouble(FakeSalesTaxGateway.getFakeTaxAmount().toString());
        assertEquals(feeSalesAmount, feeTransaction.getSalesTaxAmount(), 0.00001);

        SAPBillingTransaction handlingFeeTransaction = sapPayrollBillingTransactions.getHandlingFeeTransaction();
        assertEquals(100.0, handlingFeeTransaction.getFinancialAmount(), 0.00001);

        sapPayrollBillingTransactions.getDdTransactions().get(0).setFinancialReturnAmount(1.);
        sapPayrollBillingTransactions.getFeeTransactions().get(0).setFinancialReturnAmount(feeAmount);
        sapPayrollBillingTransactions.getFeeTransactions().get(0).setSalesTaxReturnAmount(feeSalesAmount);
        sapPayrollBillingTransactions.getHandlingFeeTransaction().setFinancialReturnAmount(100.);

        new PayrollRunAdapter().redebitPayrollTransactions(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                SettlementType.Wire.toString(),
                SAPTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime()),
                new ArrayList<SAPPayrollBillingTransactions>(Arrays.asList(sapPayrollBillingTransactions)));

        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeRedebit)
                .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("100.00")))));
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.IntuitFeeTransfer)
                .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("100.00")))));

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.NonAchPaymentReceived));
        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection());
        assertEquals(EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, companyEventEmail.getEmailTemplateTypeCd());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testCanRecordWireAndFeeAndNotGenerateBalanceEmailAlreadyRecordedFee() throws Throwable {
        setupCompanyForReturnWithWire();

        SAPOfferingServiceChargePrice sapOfferingServiceChargePrice = new SAPOfferingServiceChargePrice();
        sapOfferingServiceChargePrice.setChargedPrice(100);
        sapOfferingServiceChargePrice.setChecked(true);
        sapOfferingServiceChargePrice.setServiceChargeTypeCode(OfferingServiceChargeType.DebitReturnFee.toString());
        new PayrollRunAdapter().addFeeTransactions(
                company.getSourceCompanyId(),
                company.getSourceSystemCd().toString(),
                payrollRun.getSourcePayRunId(),
                SettlementType.Wire.toString(),
                SAPTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime()),
                new ArrayList<SAPOfferingServiceChargePrice>(Arrays.asList(sapOfferingServiceChargePrice)));

        SAPPayrollBillingTransactions sapPayrollBillingTransactions = assertOne(new PayrollRunAdapter().findPayrollUncollectedBalances(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), payrollRun.getSourcePayRunId()));
        SAPBillingTransaction ddTransaction = assertOne(sapPayrollBillingTransactions.getDdTransactions());
        assertEquals(1.0, ddTransaction.getFinancialAmount(), 0.00001);

        SAPBillingTransaction feeTransaction = assertOne(sapPayrollBillingTransactions.getFeeTransactions());
        double feeAmount = Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString());
        assertEquals(feeAmount, feeTransaction.getFinancialAmount(), 0.00001);
        double feeSalesAmount = Double.parseDouble(FakeSalesTaxGateway.getFakeTaxAmount().toString());
        assertEquals(feeSalesAmount, feeTransaction.getSalesTaxAmount(), 0.00001);

        SAPBillingTransaction handlingFeeTransaction = sapPayrollBillingTransactions.getHandlingFeeTransaction();
        assertNull(handlingFeeTransaction);

        sapPayrollBillingTransactions.getDdTransactions().get(0).setFinancialReturnAmount(1.);
        sapPayrollBillingTransactions.getFeeTransactions().get(0).setFinancialReturnAmount(feeAmount);
        sapPayrollBillingTransactions.getFeeTransactions().get(0).setSalesTaxReturnAmount(feeSalesAmount);

        new PayrollRunAdapter().redebitPayrollTransactions(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                SettlementType.Wire.toString(),
                SAPTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime()),
                new ArrayList<SAPPayrollBillingTransactions>(Arrays.asList(sapPayrollBillingTransactions)));

        Application.beginUnitOfWork();
        Application.refresh(payrollRun);

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.NonAchPaymentReceived));
        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection());
        assertEquals(EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, companyEventEmail.getEmailTemplateTypeCd());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testCanRecordWireAndThenRecordFee() throws Throwable {
        setupCompanyForReturnWithWire();

        SAPPayrollBillingTransactions sapPayrollBillingTransactions = assertOne(new PayrollRunAdapter().findPayrollUncollectedBalances(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), payrollRun.getSourcePayRunId()));
        SAPBillingTransaction ddTransaction = assertOne(sapPayrollBillingTransactions.getDdTransactions());
        assertEquals(1.0, ddTransaction.getFinancialAmount(), 0.00001);

        SAPBillingTransaction feeTransaction = assertOne(sapPayrollBillingTransactions.getFeeTransactions());
        double feeAmount = Double.parseDouble(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString());
        assertEquals(feeAmount, feeTransaction.getFinancialAmount(), 0.00001);
        double feeSalesAmount = Double.parseDouble(FakeSalesTaxGateway.getFakeTaxAmount().toString());
        assertEquals(feeSalesAmount, feeTransaction.getSalesTaxAmount(), 0.00001);
        assertEquals(100., sapPayrollBillingTransactions.getHandlingFeeTransaction().getFinancialAmount(), 0.00001);


        sapPayrollBillingTransactions.getDdTransactions().get(0).setFinancialReturnAmount(1.);
        sapPayrollBillingTransactions.getFeeTransactions().get(0).setFinancialReturnAmount(feeAmount);
        sapPayrollBillingTransactions.getFeeTransactions().get(0).setSalesTaxReturnAmount(feeSalesAmount);

        new PayrollRunAdapter().redebitPayrollTransactions(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                SettlementType.Wire.toString(),
                SAPTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime()),
                new ArrayList<SAPPayrollBillingTransactions>(Arrays.asList(sapPayrollBillingTransactions)));

        sapPayrollBillingTransactions = assertOne(new PayrollRunAdapter().findPayrollUncollectedBalances(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), payrollRun.getSourcePayRunId()));

        ddTransaction = assertOne(sapPayrollBillingTransactions.getDdTransactions());
        assertEquals(0., ddTransaction.getFinancialAmount(), 0.00001);

        assertEquals(100., sapPayrollBillingTransactions.getHandlingFeeTransaction().getFinancialAmount(), 0.00001);

        sapPayrollBillingTransactions.getHandlingFeeTransaction().setFinancialReturnAmount(100.);
        new PayrollRunAdapter().redebitPayrollTransactions(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                SettlementType.Wire.toString(),
                SAPTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime()),
                new ArrayList<SAPPayrollBillingTransactions>(Arrays.asList(sapPayrollBillingTransactions)));

        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeRedebit)
                .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("100.00")))));
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.IntuitFeeTransfer)
                .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("100.00")))));

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.NonAchPaymentReceived);
        assertEquals(2, companyEvents.size());
        CompanyEvent companyEvent = companyEvents.sort(CompanyEvent.EventTimeStamp().Descending()).getFirst();
        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection());
        assertEquals(EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, companyEventEmail.getEmailTemplateTypeCd());
        Application.rollbackUnitOfWork();
    }

    @Ignore("just a spike for perf testing)")
    @Test
    public void lots() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MA-M941-PAYMENT", SpcfCalendar.createInstance(2012, 1, 1));


        DataLoadServices.setPSPDate(2012, 11, 1);
        Company company = DataLoadPalette.setupTaxCompany();


        for (int i = 0; i <= 500; i=i+5) {
            SpcfCalendar calendar = SpcfCalendar.createInstance(2012, 11, 1, SpcfTimeZone.getLocalTimeZone());
            calendar.addDays(i);
            while (CalendarUtils.isWeekendOrHoliday(calendar)) {
                calendar.addDays(1);
            }
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(calendar);
            PayrollServices.commitUnitOfWork();
            SpcfCalendar checkDate = calendar.copy();
            CalendarUtils.addBusinessDays(checkDate, 2);
            PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(checkDate));
            DataLoadServices.runOffload();
            if (i % 15 == 0) {
                voidAPaycheck(payrollRun);
            }
        }

    }

    @Test
    public void testCancelPayrollTxnSymphonyCurrentState() throws Throwable {
        testCancelPayrollTxnSymphony(SourceSystemCode.QBDT, "8574536", "BatchTest09",
                OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD));

    }

    @Test
    public void testCancelPayrollTxnSymphonyTestState() throws Throwable {
        testCancelPayrollTxnSymphony(SourceSystemCode.QBDT, "8574536", "BatchTest09",
                OffloadGroup.findOffloadGroup(OffloadGroup.Codes.PSPOFFLOADS));
    }

    private void testCancelPayrollTxnSymphony(SourceSystemCode sourceSystemCode,
                                              String companyId, String pSourcePayrollRunId, OffloadGroup offloadGroup) throws Throwable {
        // Initialize the data
        dataCreatorForCancelPayrollSymp(offloadGroup);

        PayrollRunAdapterVerifier payrollRunAdapterVerifier = new PayrollRunAdapterVerifier();

        payrollRunAdapterVerifier.sapCancelVerifier(sourceSystemCode, companyId, pSourcePayrollRunId, mPayrollRunAdapter);
    }

    private void dataCreatorForCancelPayrollSymp(OffloadGroup offloadGroup) {

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPathQBDT(DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, offloadGroup);
        PayrollServices.commitUnitOfWork();
    }


    @Test
    public void testCanRecordWireSymphonyCurrentState() throws Throwable {
        testCanRecordWireSymphony(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD), "R16", "unspeakable horrible",
                "8574536", "BatchTest09",777.77);
    }

    @Test
    public void testCanRecordWireSymphonyTestState() throws Throwable {
        testCanRecordWireSymphony(OffloadGroup.findOffloadGroup(OffloadGroup.Codes.PSPOFFLOADS), "R16", "unspeakable horrible",
                "8574536", "BatchTest09",777.77);
    }



    private void testCanRecordWireSymphony(OffloadGroup offloadGroup, String pReturnCode, String pReturnDesc, String companyId,  String sourcePayrollRunId,  double financialReturnAmt) throws Throwable {

        // load all necessary data
        ACHReturnsDataLoader.loadQBDTPayrollReturned(DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD), pReturnCode, pReturnDesc);

        PayrollRunAdapterVerifier payrollRunAdapterVerifier = new PayrollRunAdapterVerifier();
        payrollRunAdapterVerifier.sapRecordWireVerifier(companyId, sourcePayrollRunId, financialReturnAmt);
    }



    public void voidAPaycheck(PayrollRun payrollRun) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        voidPaychecks.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();
    }

    public static void main(String[] args) {
        try {
            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
            Application.initialize();
            ApplicationSecondary.initialize();
            StopWatch sw = new StopWatch();
            System.out.println("Start");
            sw.start();
            ArrayList<SAPPayrollRun> payrollRunsByDate = new PayrollRunAdapter().findPayrollRunsByDate("TEST_0001", "QBDT", new ArrayList<String>(Arrays.asList("Regular", "CloudOnly", "Adjustment", "FeeOnly")), new Date("01/01/2012"), new Date("01/01/2013"));
            //ArrayList<SAPPayrollRun> payrollRunsByDate = new PayrollRunAdapter().findPayrollRunsByDate("448025820", "QBDT", new ArrayList<String>(Arrays.asList("Regular", "CloudOnly", "Adjustment", "FeeOnly")), new Date("11/16/2011"), new Date("11/16/2013"));
            //new EmployeeAdapter().getEmployeeProfileQTDYTDDetails("QBDT", "351006763", "60");
            sw.stop();
            System.out.println(sw.getElapsedTimeString());
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();
        }
    }
}
