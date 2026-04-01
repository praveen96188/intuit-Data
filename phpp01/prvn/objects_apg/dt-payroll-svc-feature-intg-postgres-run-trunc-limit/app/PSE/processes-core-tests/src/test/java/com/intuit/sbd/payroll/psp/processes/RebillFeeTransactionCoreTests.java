package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Jun 16, 2008
 * Time: 11:18:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class RebillFeeTransactionCoreTests {


    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void happyNetDebit() {
        rebill(+1.00); // positive amount is net debit to customer
    }

    @Test
    public void happyNetCredit() {
        rebill(-1.00); // negative amount is net credit to customer
    }

    @Test
    public void happyNetZero() {
        rebill(0.00); // zero amount is no net impact to customer
    }

    private void rebill(double pTargetPreTaxNetDebit) {
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // submit a payroll
        ProcessResult<PayrollRun> prSubmit = submitPayroll();

        PayrollServicesTest.assertSuccess("submitPayroll()", prSubmit);

        PayrollRun payroll = prSubmit.getResult();
        SourceSystemCode srcSystemCd = payroll.getCompany().getSourceSystemCd();
        String srcCompanyId = payroll.getCompany().getSourceCompanyId();

        // add the fee that we will eventually rebill
        PayrollServices.beginUnitOfWork();
        Company company =Company.findCompany("123272727", SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<BillingDetail> origDetails = BillingDetail.createBillingDetailWithPriceOverride(payrollRun, cba, OfferingServiceChargeType.ReversalFee, 1,
                                                           new BigDecimal(75.00 - pTargetPreTaxNetDebit), null, null); // configured list price minus desired net debit to customer

        PayrollServices.commitUnitOfWork();

        assertTrue("Fee was added", origDetails.isNotEmpty());

        for (BillingDetail origDetail : origDetails) {
            PayrollServices.beginUnitOfWork();
            // advance PSPTime to the date when that fee would be offloaded
            PSPDate.setPSPTime( origDetail.getFeeTransaction().getMoneyMovementTransaction().getInitiationDate().toLocal() );
            Application.commitUnitOfWork();

            // offload it
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            Application.beginUnitOfWork();
            // get fresh copy of BillingDetail, with fresh copies of fee and tax FTs so we can check their states
            origDetail = Application.refresh(origDetail);

            // assert fee/tax offloaded, so it can be refunded as part of the rebill
            assertEquals("orig fee FT state", TransactionStateCode.Executed,
                         origDetail.getFeeTransaction().getCurrentTransactionState().getTransactionStateCd());
            assertEquals("orig tax FT state", TransactionStateCode.Executed,
                         origDetail.getTaxTransaction().getCurrentTransactionState().getTransactionStateCd());
            assertEquals("orig fee FT's MMT payment status", PaymentStatus.Executed,
                         origDetail.getFeeTransaction().getMoneyMovementTransaction().getStatus());

            PayrollServices.commitUnitOfWork();


            PayrollServices.beginUnitOfWork();

            // advance the PSPTime by more than (ACH wait period + 1) days so that refund FTs will get the "asap" settlement date
            // (see FinancialTransactionBE.getRefundSettlementDate())
            PSPDate.addDaysToPSPTime(7);

            // now rebill that fee
            RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(origDetail.getFeeTransaction().getId().toString(),
                                                                      null);
            ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

            PayrollServices.commitUnitOfWork();


            PayrollServices.beginUnitOfWork();

            DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                    srcSystemCd, srcCompanyId, TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

            DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                    srcSystemCd, srcCompanyId, TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);

            PayrollServicesTest.assertSuccess("rebill operation", prRebill);
            DomainEntitySet<BillingDetail> rebillDetails = prRebill.getResult();
            for (BillingDetail rebillDetail : rebillDetails) {
                assertEquals("same Fee SKU ",
                            rebillDetail.getFeeTransaction().getSku(),
                            origDetail.getFeeTransaction().getSku());

                assertEquals("same Fee quantity",
                            rebillDetail.getFeeTransaction().getSkuQuantity(),
                            origDetail.getFeeTransaction().getSkuQuantity());

                assertEquals("refunded Fee FTs", 1, refundedFeeFTs.size());

                assertEquals("correct Fee refunded",
                            origDetail.getFeeTransaction().getId(),
                            refundedFeeFTs.get(0).getOriginalTransaction().getId());

                assertEquals("same Tax SKU ",
                            rebillDetail.getTaxTransaction().getSku(),
                            origDetail.getTaxTransaction().getSku());

                assertEquals("same Tax quantity",
                            rebillDetail.getTaxTransaction().getSkuQuantity(),
                            origDetail.getTaxTransaction().getSkuQuantity());

                assertEquals("refunded Tax FTs", 1, refundedTaxFTs.size());

                assertEquals("correct Tax refunded",
                            origDetail.getTaxTransaction().getId(),
                            refundedTaxFTs.get(0).getOriginalTransaction().getId());

                // make sure the fee and tax refund FTs, and the new fee and tax debit FTs, (and only those FTs) got into the same MMT
                MoneyMovementTransaction mmt = rebillDetail.getFeeTransaction().getMoneyMovementTransaction();
                assertEquals("FTs combined into MMT correctly", 4, mmt.getFinancialTransactionCollection().size());
                assertTrue("MMT has Fee Refund FT", mmt.getFinancialTransactionCollection().contains(refundedFeeFTs.get(0)));
                assertTrue("MMT has Tax Refund FT", mmt.getFinancialTransactionCollection().contains(refundedTaxFTs.get(0)));
                assertTrue("MMT has Fee Debit FT",
                            mmt.getFinancialTransactionCollection().contains(rebillDetail.getFeeTransaction()));
                assertTrue("MMT has Tax Debit FT",
                            mmt.getFinancialTransactionCollection().contains(rebillDetail.getTaxTransaction()));

                // adjust the caller's per-tax target difference for actual tax
                SpcfDecimal taxDiff =
                    rebillDetail.getTaxTransaction().getFinancialTransactionAmount().subtract(
                            origDetail.getTaxTransaction().getFinancialTransactionAmount());

                double adjustedNetDebit = pTargetPreTaxNetDebit + Double.valueOf(taxDiff.toString());
                assertEquals("MMT net refund/rebill amount",
                            Math.abs(adjustedNetDebit),
                            Double.valueOf(mmt.getMoneyMovementTransactionAmount().toString()));
            }
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void happyNewTaxStatus() {

        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // submit a payroll
        ProcessResult<PayrollRun> prSubmit = submitPayroll();

        PayrollServicesTest.assertSuccess("submitPayroll()", prSubmit);

        PayrollServices.beginUnitOfWork();
        Company company =Company.findCompany("123272727", SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        CompanyDTO dtoCompany = PayrollServices.dtoFactory.create(company);

        SourceSystemCode srcSystemCd = company.getSourceSystemCd();
        String srcCompanyId = company.getSourceCompanyId();

        // add a fee
        DomainEntitySet<BillingDetail> origDetails = BillingDetail.createBillingDetailWithPriceOverride(payrollRun, cba, OfferingServiceChargeType.ReversalFee, 1,
                                                           BigDecimal.valueOf(12.34), null, null);
        PayrollServices.commitUnitOfWork();
        assertTrue("Fee was added", origDetails.isNotEmpty());

        // advance fee and tax transaction to Completed
        for (BillingDetail origDetail : origDetails) {
            PayrollServices.beginUnitOfWork();
            origDetail = PayrollServices.entityFinder.findById(BillingDetail.class, origDetail.getId());
            TransactionState executedState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
            TransactionState completedState = Application.findById(TransactionState.class, TransactionStateCode.Completed);

            origDetail.getFeeTransaction().addTransactionState(executedState);
            origDetail.getFeeTransaction().addTransactionState(completedState);

            assertTrue(origDetail.getTaxTransaction() != null);
            origDetail.getTaxTransaction().addTransactionState(executedState);
            origDetail.getTaxTransaction().addTransactionState(completedState);

            PayrollServices.commitUnitOfWork();

            // now make the company tax-exempt
            PayrollServices.beginUnitOfWork();

            SpcfCalendar tomorrow = PSPDate.getPSPTime();
            tomorrow.addDays(1);
            dtoCompany.setTaxExemptExpirationDate(new DateDTO(tomorrow));
            dtoCompany.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
            ProcessResult prCompany = PayrollServices.companyManager.updateCompany(dtoCompany.getSourceSystemCd(),
                                                                                   dtoCompany.getCompanyId(),
                                                                                   dtoCompany);
            PayrollServices.commitUnitOfWork();
            PayrollServicesTest.assertSuccess("update company (to make tax exempt)", prCompany);

            // now rebill that fee
            PayrollServices.beginUnitOfWork();

            RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(origDetail.getFeeTransaction().getId().toString(),
                                                                      null);
            ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();

            DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                    srcSystemCd, srcCompanyId, TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

            DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                    srcSystemCd, srcCompanyId, TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);


            PayrollServicesTest.assertSuccess("rebill operation", prRebill);
            DomainEntitySet<BillingDetail> rebillDetails = prRebill.getResult();

            for (BillingDetail rebillDetail : rebillDetails) {
                assertEquals("same Fee SKU ",
                            rebillDetail.getFeeTransaction().getSku(),
                            origDetail.getFeeTransaction().getSku());

                assertEquals("same Fee quantity",
                            rebillDetail.getFeeTransaction().getSkuQuantity(),
                            origDetail.getFeeTransaction().getSkuQuantity());

                assertEquals("refunded Fee FTs", 1, refundedFeeFTs.size());

                assertEquals("correct Fee refunded",
                            origDetail.getFeeTransaction().getId(),
                            refundedFeeFTs.get(0).getOriginalTransaction().getId());

                assertEquals("refunded Tax FTs", 1, refundedTaxFTs.size());

                assertEquals("correct Tax refunded",
                            origDetail.getTaxTransaction().getId(),
                            refundedTaxFTs.get(0).getOriginalTransaction().getId());

                assertTrue("no rebilled tax transaction", rebillDetail.getTaxTransaction() == null);
            }
        }
    }

    @Test
    public void testOverrideAmount() {
        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversed();

        //Find the reversal fee
        PayrollServices.beginUnitOfWork();
        FinancialTransaction executedFeeFT = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    executedFeeFT = currTxn;
                }
            }
        }

        // advance the PSPTime by more than (ACH wait period + 1) days so that refund FTs will get the "asap" settlement date
        // (see FinancialTransactionBE.getRefundSettlementDate())
        PSPDate.addDaysToPSPTime(7);

        // now rebill that fee with the override amount
        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(executedFeeFT.getId().toString(), new SpcfMoney("900.87"));
        ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

        DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);        

        assertSuccess("rebill operation", prRebill);
        assertEquals("Number of refund fee transactions", 1, refundedFeeFTs.size());
        assertEquals("Number of refund tax transactions", 1, refundedTaxFTs.size());
        assertEquals("Number of new fees", 1, newFeeFTs.size());
        assertEquals("Number of new fees", 1, newTaxFTs.size());

        FinancialTransaction newFee = newFeeFTs.get(0);
        FinancialTransaction newTax = newTaxFTs.get(0);
        MoneyMovementTransaction feeMMT = newFee.getMoneyMovementTransaction();
        MoneyMovementTransaction taxMMT = newTax.getMoneyMovementTransaction();

        assertTrue("Fee and tax share the same MMT", feeMMT == taxMMT);
        assertEquals("Fee amount", new SpcfMoney("900.87"), newFee.getFinancialTransactionAmount());
        assertEquals("Fee skus equal", executedFeeFT.getSku(), newFee.getSku());
        assertEquals("Tax skus equal", executedFeeFT.getSku(), newTax.getSku());

        PayrollServices.commitUnitOfWork();

        //Create fee offload events
        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
        eventCreator.createTransactionOffloadedEvents();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> coEventList = CompanyEvent.findCompanyEvents(company,EventTypeCode.FeeOffloaded,
                null,null,null);
        Assert.assertEquals("Company Events", 1, coEventList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testNonPositiveOverrideAmountForNonPayrollTxn() {
        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversed();

        //Find the reversal fee
        PayrollServices.beginUnitOfWork();
        FinancialTransaction executedFeeFT = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    executedFeeFT = currTxn;
                }
            }
        }

        // advance the PSPTime by more than (ACH wait period + 1) days so that refund FTs will get the "asap" settlement date
        // (see FinancialTransactionBE.getRefundSettlementDate())
        PSPDate.addDaysToPSPTime(7);

        // now try to rebill that fee with the override amount
        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(executedFeeFT.getId().toString(), new SpcfMoney("-900.87"));
        ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

        DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        assertEquals("rebill operation returned one error message", 1, prRebill.getMessages().size());
        String message = prRebill.getMessages().get(0).getMessage();

        assertEquals("Message string", "The amount must be a non-zero, positive number.", message);

        assertEquals("Number of refund fee transactions", 0, refundedFeeFTs.size());
        assertEquals("Number of refund tax transactions", 0, refundedTaxFTs.size());
        assertEquals("Number of new fees", 0, newFeeFTs.size());
        assertEquals("Number of new fees", 0, newTaxFTs.size());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInvalidAction() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF return");

        //Find the payroll fee
        PayrollServices.beginUnitOfWork();
        FinancialTransaction executedFeeFT = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Returned);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.DirectDepositFee == osc) {
                    executedFeeFT = currTxn;
                }
            }
        }

        // advance the PSPTime by more than (ACH wait period + 1) days so that refund FTs will get the "asap" settlement date
        // (see FinancialTransactionBE.getRefundSettlementDate())
        PSPDate.addDaysToPSPTime(7);

        // now try to rebill that returned fee
        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(executedFeeFT.getId().toString(), null);
        ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

        DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        assertEquals("rebill operation returned one error message", 1, prRebill.getMessages().size());
        String message = prRebill.getMessages().get(0).getMessage();

        assertEquals("Message string", "Action RefundRebillFee not valid for Financial Transaction with FinTxGseq "+executedFeeFT.getId().toString()+", which has a tx type of EmployerFeeDebit and a tx status of Returned.", message);

        assertEquals("Number of refund fee transactions", 0, refundedFeeFTs.size());
        assertEquals("Number of refund tax transactions", 0, refundedTaxFTs.size());
        assertEquals("Number of new fees", 0, newFeeFTs.size());
        assertEquals("Number of new fees", 0, newTaxFTs.size());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void alreadyRefunded() {
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // submit a payroll
        ProcessResult<PayrollRun> prSubmit = submitPayroll();

        PayrollServicesTest.assertSuccess("submitPayroll()", prSubmit);

        PayrollRun payroll = prSubmit.getResult();
        SourceSystemCode srcSystemCd = payroll.getCompany().getSourceSystemCd();
        String srcCompanyId = payroll.getCompany().getSourceCompanyId();

        // add a fee
        PayrollServices.beginUnitOfWork();
        Company company =Company.findCompany("123272727", SourceSystemCode.QBDT);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(company);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<BillingDetail> origDetails = BillingDetail.createBillingDetailWithPriceOverride(payrollRun, cba, OfferingServiceChargeType.ReversalFee, 1,
                                                           BigDecimal.valueOf(12.34), null, null);
        assertTrue("Fee was added", origDetails.isNotEmpty());
        PayrollServices.commitUnitOfWork();

        for (BillingDetail origDetail : origDetails) {
            // advance fee [and tax] transaction to Completed
            PayrollServices.beginUnitOfWork();
            origDetail = PayrollServices.entityFinder.findById(BillingDetail.class, origDetail.getId());

            TransactionState executedState = Application.findById(TransactionState.class, TransactionStateCode.Executed);
            TransactionState completedState = Application.findById(TransactionState.class, TransactionStateCode.Completed);

            origDetail.getFeeTransaction().addTransactionState(executedState);
            origDetail.getFeeTransaction().addTransactionState(completedState);

            if (origDetail.getTaxTransaction() != null) {
                origDetail.getTaxTransaction().addTransactionState(executedState);
                origDetail.getTaxTransaction().addTransactionState(completedState);
            }
            PayrollServices.commitUnitOfWork();

            // refund that fee
            PayrollServices.beginUnitOfWork();
            PSPDate.addDaysToPSPTime(5);
            DateDTO dtoRefundDate = new DateDTO(PSPDate.getPSPTime());
            ERRefundDTO dtoRefund = new ERRefundDTO(origDetail.getFeeTransaction().getId().toString(),
                                                    origDetail.getFeeTransaction().getFinancialTransactionAmount(),
                                                    dtoRefundDate, SettlementTypeDTO.Wire);
            ProcessResult<FinancialTransaction> prRefund = PayrollServices.financialTransactionManager.refundEmployerTransaction(srcSystemCd, srcCompanyId, dtoRefund);
            PayrollServices.commitUnitOfWork();
            PayrollServicesTest.assertSuccess("Fee refunded", prRefund);

            // now try to rebill that fee
            PayrollServices.beginUnitOfWork();
            RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(origDetail.getFeeTransaction().getId().toString(),
                                                                      null);
            ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);
            PayrollServices.commitUnitOfWork();

            assertTrue("rebill operation failed", ! prRebill.isSuccess());
            assertTrue("one error message", prRebill.getMessages().size()==1);
            assertEquals("error message number", "265", prRebill.getMessages().get(0).getMessageCode()); //ERROR 265: Financial Transaction ... cannot be refunded, because a refund has already been attempted.
        }
    }

    @Test
    public void testRebillFeeForAgent() {
        PayrollServices.beginUnitOfWork();
        AuthRole foundRole = AuthRole.findRole("RMRep");
        ProcessResult processResult1 = PayrollServices.userManager.addUser("TestAdapter",Arrays.asList(foundRole.getRoleId()),"TestAdapter","TestAdapter");
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        AuthUser user = AuthUser.findUser("TestAdapter");
        user = Application.findById(AuthUser.class, user.getId());
        PayrollServices.commitUnitOfWork();
        //Set PSP Principal for the User
        PayrollServices.setCurrentPrincipal(new PspPrincipal(user.getCorpId(), user.getFirstName() + " " + user.getLastName()));

        ACHReturnsDataLoader.loadQBDTPayrollOffloaded();

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(13);
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        Calendar calendar = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2007, 9, 1, SpcfTimeZone.getLocalTimeZone()));
        calendar.add(Calendar.DAY_OF_YEAR, -45);
        Date txDate = calendar.getTime();        
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                "BatchTest09", SettlementTypeDTO.ACH,
                txDate, new SpcfMoney("75.00"),
                OfferingServiceChargeType.ReversalFee, null);

        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Result ",processResult);

        //Offload Reversal Fee
        //PayrollServices.beginUnitOfWork();
        //PSPDate.setPSPTime("20071005000000");
        //PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Create fee offload events
//        CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
//        eventCreator.createTransactionOffloadedEvents();
        
        //Find the reversal fee
        PayrollServices.beginUnitOfWork();
        FinancialTransaction executedFeeFT = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.ReversalFee == osc) {
                    executedFeeFT = currTxn;
                }
            }
        }

        // now rebill that fee with the override amount
        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(executedFeeFT.getId().toString(), new SpcfMoney("900.87"));
        ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

        DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> refundedTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newTaxFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Created);

        assertSuccess("rebill operation", prRebill);
        assertEquals("Number of refund fee transactions", 1, refundedFeeFTs.size());
        assertEquals("Number of refund tax transactions", 1, refundedTaxFTs.size());
        assertEquals("Number of new fees", 1, newFeeFTs.size());
        assertEquals("Number of new fees", 1, newTaxFTs.size());

        FinancialTransaction newFee = newFeeFTs.get(0);
        FinancialTransaction newTax = newTaxFTs.get(0);
        MoneyMovementTransaction feeMMT = newFee.getMoneyMovementTransaction();
        MoneyMovementTransaction taxMMT = newTax.getMoneyMovementTransaction();

        assertTrue("Fee and tax share the same MMT", feeMMT == taxMMT);
        assertEquals("Fee amount", new SpcfMoney("900.87"), newFee.getFinancialTransactionAmount());
        assertEquals("Fee skus equal", executedFeeFT.getSku(), newFee.getSku());
        assertEquals("Tax skus equal", executedFeeFT.getSku(), newTax.getSku());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        DomainEntitySet<CompanyEvent> coEventList = CompanyEvent.findCompanyEvents(company,EventTypeCode.FeeOffloaded,
                null,null,null);
        Assert.assertEquals("Company Events", 1, coEventList.size());

        DomainEntitySet<CompanyEvent> feeCreatedEventList = CompanyEvent.findCompanyEvents(company,EventTypeCode.FeeCreated,
                null,null,null);
        Assert.assertEquals("Fee Created Events", 1, feeCreatedEventList.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void rebillRedebit() {
        ACHReturnsDataLoader.loadDataReversalFeeAndPayrollReturned("R02", "non-NSF return");
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> uncollectedFTs = payrollRun.getUncollectedDDAmount();
        uncollectedFTs.putAll(payrollRun.getUncollectedFeeAmounts());
        uncollectedFTs.putAll(payrollRun.getUncollectedSalesTaxAmounts());
        assertEquals("number of FTs with uncollected amounts", 5, uncollectedFTs.size());

        List<RedebitImpoundDTO> redebitDTOs = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for the total uncollected amounts for each txn
        for (Map.Entry<FinancialTransaction, SpcfMoney> entry : uncollectedFTs.entrySet()) {
            FinancialTransaction ft = entry.getKey();
            SpcfMoney amount = entry.getValue();
            RedebitImpoundDTO dto = new RedebitImpoundDTO(ft.getId().toString(), amount, new DateDTO(PSPDate.getPSPTime()));
            redebitDTOs.add(dto);
        }

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", redebitDTOs);
        PayrollServices.commitUnitOfWork();
        assertSuccess(procResult);

        // find a fee redebit and offload/complete it, so we can ultimately rebill it
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeRedebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Created);
        assertTrue(feeRedebits.size() >= 1);
        FinancialTransaction theRedebit = null;
        for (FinancialTransaction ft : feeRedebits) {
            if (OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(ft.getSku()) == OfferingServiceChargeType.ReversalFee) {
                theRedebit = ft;
                break;
            }
        }

        // offload
        SpcfCalendar initDate = theRedebit.getMoneyMovementTransaction().getInitiationDate();
        PSPDate.setPSPTime(initDate);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload("STD", null);        

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(5);
        PayrollServices.commitUnitOfWork();

        // complete it
        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processor = new ProcessACHTransactions();
        processor.process(PSPDate.getPSPTime());
        theRedebit = Application.refresh(theRedebit);
        TransactionStateCode redebitStateCd = theRedebit.getCurrentTransactionState().getTransactionStateCd();
        FinancialTransaction origDebit = theRedebit.getOriginalTransaction();
        TransactionSummary summaryBefore = origDebit.summarizeRelatedTransactions();
        PayrollServices.commitUnitOfWork();
        assertEquals("fee redebit state", TransactionStateCode.Completed, redebitStateCd);
        SpcfDecimal ZERO = SpcfDecimal.createInstance(0);
        assertTrue("collected amount > 0 before rebill", summaryBefore.amtCollected.compareTo(ZERO) > 0);
        assertTrue("refunded amount == 0 before rebill", summaryBefore.amtRefunded.compareTo(ZERO) == 0);

        // rebill it
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(7);
        try {
            RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(theRedebit.getId().toString(), null);
            ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);
            PayrollServices.commitUnitOfWork();
            assertSuccess("rebill", prRebill);
        }
        catch (RuntimeException rx) {
            // PSRV000848 was about the process throwing this exception when the input FT was not an EmployerFeeDebit
            PayrollServices.rollbackUnitOfWork();
            rx.printStackTrace();
            Assert.fail("RuntimeException: " + rx.getMessage());
        }

        // offload the refund
        OffloadACHTransactions refundOffloader = new OffloadACHTransactions();
        refundOffloader.offloadAndPostOffload("STD", null);

        // recompute summary amount and make sure they're right
        PayrollServices.beginUnitOfWork();
        origDebit = Application.findById(FinancialTransaction.class, origDebit.getId());
        TransactionSummary summaryAfter = origDebit.summarizeRelatedTransactions();
        PayrollServices.commitUnitOfWork();
        assertTrue("collected amount == 0 after rebill", summaryAfter.amtCollected.compareTo(ZERO) == 0); // Executed debits not counted
        assertTrue("refunded amount > 0 after rebill", summaryAfter.amtRefunded.compareTo(ZERO) > 0); // Executed refunds are counted
    }

    private ProcessResult<PayrollRun> submitPayroll() {
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        // this creates the company and other stuff and offloads the bank verfication debits
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(company);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoUpdate);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Updating company legal address for taxability", prUpdate);

        // this submits the payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        return prPayroll;
    }

    //PSRV000873
    /**
     * Test case to Rebill the payroll Fee and make sure the EmployerFeeRefundCredit & EmployerFeeDebit should combine into
     * Single MoneyMovement Transaction
     */
    @Test
    public void testRebillPayrollFee() {
        ACHReturnsDataLoader.loadQBDTPayrollOffloaded();

        //Find the payroll fee
        PayrollServices.beginUnitOfWork();
        FinancialTransaction executedFeeFT = null;
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed);
        for (FinancialTransaction currTxn : c1FinTxns) {
            if (TransactionTypeCode.EmployerFeeDebit == currTxn.getTransactionType().getTransactionTypeCd()) {
                OfferingServiceChargeType osc = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(currTxn.getSku());
                if (OfferingServiceChargeType.DirectDepositFee == osc) {
                    executedFeeFT = currTxn;
                }
            }
        }

        // advance the PSPTime by more than (ACH wait period + 1) days so that refund FTs will get the "asap" settlement date
        // (see FinancialTransactionBE.getRefundSettlementDate())
        PSPDate.addDaysToPSPTime(7);

        // rebill the Payroll Fee
        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(executedFeeFT.getId().toString(), null);
        ProcessResult<DomainEntitySet<BillingDetail>> prRebill = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);

        PayrollServices.commitUnitOfWork();
        assertEquals("rebill operation returned one error message", 0, prRebill.getMessages().size());
        
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> refundedFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRefundCredit, TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> newFeeFTs = FinancialTransaction.findFinancialTransactions(
                SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Created);

        assertEquals("Number of refund fee transactions", 1, refundedFeeFTs.size());
        assertEquals("Number of new fees", 1, newFeeFTs.size());

        MoneyMovementTransaction feeRefundCreditMMT = refundedFeeFTs.get(0).getMoneyMovementTransaction();
        MoneyMovementTransaction feeDebitMMT = newFeeFTs.get(0).getMoneyMovementTransaction();

        assertEquals("Fee Refund Credit & Fee Debit Share Same MMT ",feeRefundCreditMMT,feeDebitMMT);        
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testModifyUnits() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.addEEs(company, 50, true, true);


        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        List<String> lawIds = DataLoadServices.getCompanyLawsIds(company);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        for (CompanyAgency companyAgency : Application.refresh(company).getCompanyAgencyCollection()) {
            for (CompanyLaw companyLaw : companyAgency.getCompanyLawCollection()) {
                //don't add dead laws
                if (!companyLaw.getLaw().shouldExcludeFromUI()) {
                    lawIds.add(companyLaw.getLaw().getLawId());
                }
            }

        }

        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO(2013, 1, 10), new ArrayList<Employee>(company.getCloudEmployees()), lawIds.toArray(new String[lawIds.size()]), lawIds.toArray(new String[lawIds.size()]));
        payrollRunDTO.setEmployeesPaidInTransmission(50);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollRun pr = processResult.getResult();
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        Application.refresh(pr);
        FinancialTransaction employeesPaidFT =
                assertOne(pr.getFinancialTransactionCollection().find(
                        FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid))));

        RebillFeeTransactionDTO dto = new RebillFeeTransactionDTO(employeesPaidFT.getId().toString(), new SpcfMoney("1.50"), 25);

        ProcessResult<DomainEntitySet<BillingDetail>> rebillProcessResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(dto);
        assertSuccess(rebillProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = rebillProcessResult.getResult();
        for (BillingDetail billingDetail : billingDetails) {
            Application.refresh(billingDetail);
            assertEquals(25, billingDetail.getQuantity());
            assertEquals(new SpcfMoney("1.50"), billingDetail.getUnitPrice());
            assertEquals(new SpcfMoney("37.50"), billingDetail.getFeeTransaction().getFinancialTransactionAmount());
        }
        PayrollServices.rollbackUnitOfWork();
    }
}
