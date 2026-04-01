package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.RedebitAddCoreDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * User: rsakhamuri
 * Date: Dec 12, 2007
 * Time: 3:05:44 PM
 */
public class RedebitAddOrEditCoreTests {



    @Before
    public void runBeforeEachTest() {
        RedebitAddCoreDataLoader.loadBeforeTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCompanyDNE() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 1, feeHashMap.size());

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction feeTxn = feeHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("700.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceCharge(2), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBOE, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse("Redebit failed", procResult.isSuccess());

        assertEquals("Number of messages", 1, procResult.getMessages().size());
        Message returnedMessage = procResult.getMessages().get(0);
        assertEquals("Message text", "Company QBOE:8574536 does not exist.", returnedMessage.getMessage());
        assertEquals("Message id", "169", returnedMessage.getMessageCode());

    }

    @Test
    public void testTxnDNE() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader.persistPayrollRun(c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-08-22")));
        Company1Dataloader.persistPayrollRun(c1DL.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-08-22")));
       // PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        //Offload both payrolls
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
         PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();
        //Return both debits for R02
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(3);
        DomainEntitySet<FinancialTransaction> finTxns = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "1234567", TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        Application.commitUnitOfWork();
        Assert.assertEquals("Number of ERDDDBs executed txns", 2, finTxns.size());

        DataLoadServices.returnTxns(finTxns, "R02",
                                    "Non-NSF description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, "BatchTest05");
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchTest002");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun1.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap2 = payrollRun2.getUncollectedDDAmount();
        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of payroll txns", 1, payrollHashMap2.size());
        FinancialTransaction payroll1txn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction payroll2txn = payrollHashMap2.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        String fakeGUID = "87474747-4848-4444-843A-843aa4bdf948";
        RedebitImpoundDTO payroll1Redebit = new RedebitImpoundDTO(payroll1txn.getId().toString(), new SpcfMoney("180.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO payroll2Redebit = new RedebitImpoundDTO(fakeGUID, new SpcfMoney("50.00"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payroll1Redebit);
        collectionOfRedebitImpounds.add(payroll2Redebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBOE, "1234567", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse("Process result", procResult.isSuccess());
        assertEquals("Number of errors", 1, procResult.getMessages().size());
        Message errorMessage = procResult.getMessages().get(0);
        assertEquals("Error message text", "Financial Transaction " + fakeGUID + " does not exist for company QBOE:1234567.", errorMessage.getMessage());
        assertEquals("Error message code", "264", errorMessage.getMessageCode());
        assertEquals("Error message level", MessageInfo.MessageLevel.ERROR, errorMessage.getLevel());
    }

    @Test
    public void testAmountNotPositive() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 1, feeHashMap.size());
        assertEquals("Number of sales tax txns", 1, salesTaxHashMap.size());

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction feeTxn = feeHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("0.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("0.00"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();
        assertFalse("process result succeeded", procResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erDDRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);
        DomainEntitySet<FinancialTransaction> erFeeRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Created);
        DomainEntitySet<FinancialTransaction> erSalesTaxRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Created);

        //one er dd redebit from the dataloader
        assertEquals("number of pending ERDDRED", 1, erDDRed.size());
        assertTrue("Redebit amount for er dd redebit", erDDRed.get(0).getFinancialTransactionAmount().compareTo(new SpcfMoney("0.00")) > 0);
        assertEquals("number of pending erFeeRed", 0, erFeeRed.size());
        assertEquals("number of pending erSalesTaxRed", 0, erSalesTaxRed.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testInvalidSettlementType() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 1, feeHashMap.size());
        assertEquals("Number of sales tax txns", 1, salesTaxHashMap.size());

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction feeTxn = feeHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for the partial uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("50.00"), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("1.00"), new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();
        assertFalse("process result succeeded", procResult.isSuccess());
        assertEquals("Number of errors", 1, procResult.getMessages().size());
        Message errorMessage = procResult.getMessages().get(0);
        assertEquals("Error message text", "Invalid Settlement Type Code Wire specified.", errorMessage.getMessage());
        assertEquals("Error message code", "165", errorMessage.getMessageCode());
        assertEquals("Error message level", MessageInfo.MessageLevel.ERROR, errorMessage.getLevel());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erDDRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Created);
        DomainEntitySet<FinancialTransaction> erFeeRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Created);
        DomainEntitySet<FinancialTransaction> erSalesTaxRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Created);

        //one er dd redebit from the dataloader
        assertEquals("number of pending ERDDRED", 1, erDDRed.size());
        assertTrue("Redebit amount for er dd redebit", erDDRed.get(0).getFinancialTransactionAmount().compareTo(new SpcfMoney("0.00")) > 0);
        assertEquals("number of pending erFeeRed", 0, erFeeRed.size());
        assertEquals("number of pending erSalesTaxRed", 0, erSalesTaxRed.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAllTxnsNotInSamePayroll() {
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1DL = new Company1Dataloader();
        c1DL.persistCompany1();
        Company1Dataloader.persistPayrollRun(c1DL.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-07")));
        Company1Dataloader.persistPayrollRun(c1DL.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-09-07")));
        PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        //Offload both payrolls
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070917000000");
        Application.commitUnitOfWork();

        //Return both debits for R02
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> finTxns = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBOE, "1234567", TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        Application.commitUnitOfWork();
        Assert.assertEquals("Number of ERDDDBs executed txns", 2, finTxns.size());

        DataLoadServices.returnTxns(finTxns, "R02", "Non-NSF description");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, "BatchTest05");
        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(company, "BatchTest002");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun1.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap2 = payrollRun2.getUncollectedDDAmount();
        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of payroll txns", 1, payrollHashMap2.size());
        FinancialTransaction payroll1txn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction payroll2txn = payrollHashMap2.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        RedebitImpoundDTO payroll1Redebit = new RedebitImpoundDTO(payroll1txn.getId().toString(), new SpcfMoney("180.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO payroll2Redebit = new RedebitImpoundDTO(payroll2txn.getId().toString(), new SpcfMoney("50.00"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payroll1Redebit);
        collectionOfRedebitImpounds.add(payroll2Redebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBOE, "1234567", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertFalse("Process result", procResult.isSuccess());
        assertEquals("Number of errors", 1, procResult.getMessages().size());
        Message errorMessage = procResult.getMessages().get(0);
        assertEquals("Error message text", "All redebits must be associated with the same payroll run for company QBOE:1234567.", errorMessage.getMessage());
        assertEquals("Error message code", "1302", errorMessage.getMessageCode());
        assertEquals("Error message level", MessageInfo.MessageLevel.ERROR, errorMessage.getLevel());
    }

    @Test
    public void testAddAndEditWithNonPayrollFeeDiffSettlementDates() {
        ACHReturnsDataLoader.loadDataReversalFeeAndPayrollReturned("R02", "non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 2, feeHashMap.size());
        assertEquals("Number of sales tax txns", 2, salesTaxHashMap.size());

        FinancialTransaction feeTxn = null;
        FinancialTransaction taxTxn = null;
        FinancialTransaction reversalFeeTxn = null;
        FinancialTransaction reversalFeeTaxTxn = null;

        for (FinancialTransaction currFinTxn : feeHashMap.keySet()) {
            System.out.println(currFinTxn.getTransactionType().getTransactionTypeCd() + " has $" + feeHashMap.get(currFinTxn) + " uncollected");

            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTxn = currFinTxn;
            } else {
                feeTxn = currFinTxn;
            }
        }

        for (FinancialTransaction currFinTxn : salesTaxHashMap.keySet()) {
            System.out.println(currFinTxn.getTransactionType().getTransactionTypeCd() + " has $" + salesTaxHashMap.get(currFinTxn) + " uncollected");

            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTaxTxn = currFinTxn;
            } else {
                taxTxn = currFinTxn;
            }
        }

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("777.77"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.08"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalFeeRedebit = new RedebitImpoundDTO(reversalFeeTxn.getId().toString(), reversalFeeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalTaxRedebit = new RedebitImpoundDTO(reversalFeeTaxTxn.getId().toString(), reversalFeeTaxTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);
        collectionOfRedebitImpounds.add(reversalTaxRedebit);
        collectionOfRedebitImpounds.add(reversalFeeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertSuccess(procResult);

        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(taxRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        assertRedebitEquals(reversalTaxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        assertRedebitEquals(reversalFeeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);

        MoneyMovementTransaction mmt_payroll = FinancialTransaction.getPendingRedebitTransaction(payrolltxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee = FinancialTransaction.getPendingRedebitTransaction(feeTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax = FinancialTransaction.getPendingRedebitTransaction(taxTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTaxTxn.getId().toString()).getMoneyMovementTransaction();

        //Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll, mmt_fee);
        assertEquals("MMTs equal", mmt_payroll, mmt_tax);

        //Non-Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_fee_rev, mmt_tax_rev);

        //payroll and non-payroll should NOT be in the same mmt txn
        assertFalse("Payoll and non-payroll MMTs are not in the same txn", mmt_fee_rev.equals(mmt_fee));
        reversalTaxRedebit = new RedebitImpoundDTO(reversalFeeTaxTxn.getId().toString(), new SpcfMoney("0.01"), new DateDTO("2007-10-11"));

        List<RedebitImpoundDTO> collectionOfRedebitImpounds2 = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds2.add(reversalTaxRedebit);

        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds2);
        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //The reversal fee + sales tax mmt transaction is resolved since it got a full redebit
        //The original payroll + fee + sales tax redebit mmt is NOT resolved since it got a partial redebit
    assertRedebitEquals(reversalTaxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

        mmt_payroll = FinancialTransaction.getPendingRedebitTransaction(payrolltxn.getId().toString()).getMoneyMovementTransaction();
        mmt_fee = FinancialTransaction.getPendingRedebitTransaction(feeTxn.getId().toString()).getMoneyMovementTransaction();
        mmt_tax = FinancialTransaction.getPendingRedebitTransaction(taxTxn.getId().toString()).getMoneyMovementTransaction();
        mmt_fee_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTxn.getId().toString()).getMoneyMovementTransaction();
        mmt_tax_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTaxTxn.getId().toString()).getMoneyMovementTransaction();

        //Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll, mmt_fee);
        assertEquals("MMTs equal", mmt_payroll, mmt_tax);

        //Non-Payroll Redebits should NOT be in the same MMT since date changed
        assertFalse("MMTs equal", mmt_fee_rev.equals(mmt_tax_rev));

        //payroll and non-payroll (each) should NOT be in the same mmt txn
        assertFalse("Payoll and non-payroll MMTs are not in the same txn", mmt_fee_rev.equals(mmt_fee));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddAndEditWithNonPayrollFee() {
        ACHReturnsDataLoader.loadDataReversalFeeAndPayrollReturned("R02", "non-NSF return");
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 2, feeHashMap.size());
        assertEquals("Number of sales tax txns", 2, salesTaxHashMap.size());

        FinancialTransaction feeTxn = null;
        FinancialTransaction taxTxn = null;
        FinancialTransaction reversalFeeTxn = null;
        FinancialTransaction reversalFeeTaxTxn = null;

        for (FinancialTransaction currFinTxn : feeHashMap.keySet()) {
            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTxn = currFinTxn;
            }
            else {
                feeTxn = currFinTxn;
            }
        }

        for (FinancialTransaction currFinTxn : salesTaxHashMap.keySet()) {
            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTaxTxn = currFinTxn;
            } else {
                taxTxn = currFinTxn;
            }
        }

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("777.77"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.08"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalFeeRedebit = new RedebitImpoundDTO(reversalFeeTxn.getId().toString(), reversalFeeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalTaxRedebit = new RedebitImpoundDTO(reversalFeeTaxTxn.getId().toString(), reversalFeeTaxTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);
        collectionOfRedebitImpounds.add(reversalTaxRedebit);
        collectionOfRedebitImpounds.add(reversalFeeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertSuccess(procResult);

        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(taxRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        assertRedebitEquals(reversalTaxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        assertRedebitEquals(reversalFeeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);

        MoneyMovementTransaction mmt_payroll = FinancialTransaction.getPendingRedebitTransaction(payrolltxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee = FinancialTransaction.getPendingRedebitTransaction(feeTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax = FinancialTransaction.getPendingRedebitTransaction(taxTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTaxTxn.getId().toString()).getMoneyMovementTransaction();

        //Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll, mmt_fee);
        assertEquals("MMTs equal", mmt_payroll, mmt_tax);

        //Non-Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_fee_rev, mmt_tax_rev);

        //payroll and non-payroll should NOT be in the same mmt txn
        assertFalse("Payoll and non-payroll MMTs are not in the same txn", mmt_fee_rev.equals(mmt_fee));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddAndEditWithNonPayrollFeePartialAmounts() {
        ACHReturnsDataLoader.loadDataReversalFeeAndPayrollReturned("R02", "non-NSF return");
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 2, feeHashMap.size());
        assertEquals("Number of sales tax txns", 2, salesTaxHashMap.size());

        FinancialTransaction feeTxn = null;
        FinancialTransaction reversalFeeTxn = null;
        FinancialTransaction reversalFeeTaxTxn = null;

        for (FinancialTransaction currFinTxn : feeHashMap.keySet()) {
            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTxn = currFinTxn;
            }
            else {
                feeTxn = currFinTxn;
            }
        }

        for (FinancialTransaction currFinTxn : salesTaxHashMap.keySet()) {
            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTaxTxn = currFinTxn;
            }
        }

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("771.77"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceCharge(2), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalFeeRedebit = new RedebitImpoundDTO(reversalFeeTxn.getId().toString(), reversalFeeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalTaxRedebit = new RedebitImpoundDTO(reversalFeeTaxTxn.getId().toString(), reversalFeeTaxTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(reversalTaxRedebit);
        collectionOfRedebitImpounds.add(reversalFeeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertSuccess(procResult);

        //The reversal fee + sales tax mmt transaction is resolved since it got a full redebit
        //The original payroll + fee + sales tax redebit mmt is NOT resolved since it got a partial redebit
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(reversalTaxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        assertRedebitEquals(reversalFeeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);

        MoneyMovementTransaction mmt_payroll = FinancialTransaction.getPendingRedebitTransaction(payrolltxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee = FinancialTransaction.getPendingRedebitTransaction(feeTxn.getId().toString()).getMoneyMovementTransaction();

        MoneyMovementTransaction mmt_fee_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTaxTxn.getId().toString()).getMoneyMovementTransaction();

        //Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll, mmt_fee);

        //Non-Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_fee_rev, mmt_tax_rev);

        //payroll and non-payroll should NOT be in the same mmt txn
        assertFalse("Payoll and non-payroll MMTs are not in the same txn", mmt_fee_rev.equals(mmt_fee));

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddAndEditWithNonPayrollFeePartialAmounts_CBAChanged() {
        ACHReturnsDataLoader.loadDataReversalFeeAndPayrollReturned("R02", "non-NSF return");
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 2, feeHashMap.size());
        assertEquals("Number of sales tax txns", 2, salesTaxHashMap.size());

        FinancialTransaction feeTxn = null;
        FinancialTransaction reversalFeeTxn = null;
        FinancialTransaction reversalFeeTaxTxn = null;

        for (FinancialTransaction currFinTxn : feeHashMap.keySet()) {
            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTxn = currFinTxn;
            }
            else {
                feeTxn = currFinTxn;
            }
        }

        for (FinancialTransaction currFinTxn : salesTaxHashMap.keySet()) {
            if (isReversalFeeTransaction(currFinTxn)) {
                reversalFeeTaxTxn = currFinTxn;
            }
        }

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("771.77"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceCharge(2), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalFeeRedebit = new RedebitImpoundDTO(reversalFeeTxn.getId().toString(), reversalFeeTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO reversalTaxRedebit = new RedebitImpoundDTO(reversalFeeTaxTxn.getId().toString(), reversalFeeTaxTxn.getFinancialTransactionAmount(), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(reversalTaxRedebit);
        collectionOfRedebitImpounds.add(reversalFeeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertSuccess(procResult);

        //The reversal fee + sales tax mmt transaction is resolved since it got a full redebit
        //The original payroll + fee + sales tax redebit mmt is NOT resolved since it got a partial redebit
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(reversalTaxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        assertRedebitEquals(reversalFeeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);

        MoneyMovementTransaction mmt_payroll = FinancialTransaction.getPendingRedebitTransaction(payrolltxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee = FinancialTransaction.getPendingRedebitTransaction(feeTxn.getId().toString()).getMoneyMovementTransaction();

        MoneyMovementTransaction mmt_fee_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTaxTxn.getId().toString()).getMoneyMovementTransaction();

        //Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll, mmt_fee);

        //Non-Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_fee_rev, mmt_tax_rev);

        //payroll and non-payroll should NOT be in the same mmt txn
        assertFalse("Payoll and non-payroll MMTs are not in the same txn", mmt_fee_rev.equals(mmt_fee));

        //change cba
        Company3Dataloader c3dl = new Company3Dataloader();
        CompanyBankAccountDTO origBA = c3dl.getCompany1BankAccount();
        CompanyBankAccountDTO cbaChanged = origBA;
        BankAccountDTO origBADTO = origBA.getBankAccountDTO();
        origBADTO.setAccountNumber("638383838");
        cbaChanged.setBankAccountDTO(origBADTO);
        ProcessResult cbaChangedProcResult = PayrollServices.companyManager.changeCompanyBankAccount(SourceSystemCode.QBDT, "8574536", cbaChanged, false, true, false);
        assertSuccess("Changed cba", cbaChangedProcResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        reversalTaxRedebit = new RedebitImpoundDTO(reversalFeeTaxTxn.getId().toString(), new SpcfMoney("0.06"), new DateDTO(PSPDate.getPSPTime()));

        List<RedebitImpoundDTO> collectionOfRedebitImpounds2 = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds2.add(reversalTaxRedebit);

        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds2);
        assertSuccess(procResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //The reversal fee + sales tax mmt transaction is resolved since it got a full redebit
        //The original payroll + fee + sales tax redebit mmt is NOT resolved since it got a partial redebit
        assertRedebitEquals(reversalTaxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

        mmt_payroll = FinancialTransaction.getPendingRedebitTransaction(payrolltxn.getId().toString()).getMoneyMovementTransaction();
        mmt_fee = FinancialTransaction.getPendingRedebitTransaction(feeTxn.getId().toString()).getMoneyMovementTransaction();

        mmt_fee_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTxn.getId().toString()).getMoneyMovementTransaction();
        mmt_tax_rev = FinancialTransaction.getPendingRedebitTransaction(reversalFeeTaxTxn.getId().toString()).getMoneyMovementTransaction();

        //Payroll Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll, mmt_fee);

        //Non-Payroll Redebits should NOT be in the same MMT since CBA changed
        assertFalse("MMTs equal", mmt_fee_rev.equals(mmt_tax_rev));

        //payroll and non-payroll (each) should NOT be in the same mmt txn
        assertFalse("Payoll and non-payroll MMTs are not in the same txn", mmt_fee_rev.equals(mmt_fee));

        PayrollServices.commitUnitOfWork();
    }

    private boolean isReversalFeeTransaction(FinancialTransaction pFeeOrTaxFT) {
        String sku = pFeeOrTaxFT.getSku();
        OfferingServiceChargeType chargeType = OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(sku);
        return (chargeType == OfferingServiceChargeType.ReversalFee);
    }

    @Test
    public void testAddOrEditRedebitWithZeroes() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
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
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("0.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.00"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();
        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);

        DomainEntitySet<FinancialTransaction> erFeeRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Created);
        assertEquals("number of pending EmployerFeeRedebit txns", 0, erFeeRed.size());

        DomainEntitySet<FinancialTransaction> erTaxRed = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Created);
        assertEquals("number of pending ServiceSalesAndUseTaxRedebit txns", 0, erFeeRed.size());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.RedebitAmountUpdated, CompanyEventStatus.Active, null, null);
        DomainEntitySet<CompanyEvent> companyEvents_date = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.RedebitDateUpdated, CompanyEventStatus.Active, null, null);
        DomainEntitySet<CompanyEvent> companyEvents_status = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ACHReturnStatusChanged, CompanyEventStatus.Active, null, null);

        assertEquals("Number of RedebitAmountUpdated events", 1, companyEvents.size());
        assertEquals("Number of RedebitDateUpdated events", 0, companyEvents_date.size());
        //This is from the first time we did a redebit for the payroll (in the dataloader)
        assertEquals("Number of ACHReturnStatusChanged events", 1, companyEvents_status.size());
        DomainEntitySet<CompanyEventDetail> companyEventDetails =
                companyEvents.get(0).getCompanyEventDetailCollection();

        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId),
                payrollRun.getId().toString());
        assertEquals("Event Detail", new SpcfMoney("777.77").toString(),
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.OldAmount));
        assertEquals("Event Detail",
                new SpcfMoney("700.00").toString(),
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewAmount));

        PayrollServices.commitUnitOfWork();

        //offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071114000000");
        Application.commitUnitOfWork();

        //complete the txns
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
        feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2), new DateDTO(PSPDate.getPSPTime()));
        taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.08"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertSuccess("2nd redebit", procResult);

        PayrollServices.beginUnitOfWork();

        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(taxRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

        FinancialTransaction original_payroll_ft = PayrollServices.entityFinder.findById(FinancialTransaction.class, payrolltxn.getId());
        FinancialTransaction original_fee_ft = PayrollServices.entityFinder.findById(FinancialTransaction.class, feeTxn.getId());
        FinancialTransaction original_tax_ft = PayrollServices.entityFinder.findById(FinancialTransaction.class, taxTxn.getId());

        MoneyMovementTransaction mmt_payroll_orig = original_payroll_ft.getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee_orig = original_fee_ft.getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax_orig = original_tax_ft.getMoneyMovementTransaction();

        MoneyMovementTransaction mmt_payroll = FinancialTransaction.getPendingRedebitTransaction(payrolltxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_fee = FinancialTransaction.getPendingRedebitTransaction(feeTxn.getId().toString()).getMoneyMovementTransaction();
        MoneyMovementTransaction mmt_tax = FinancialTransaction.getPendingRedebitTransaction(taxTxn.getId().toString()).getMoneyMovementTransaction();

        //Redebits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll, mmt_fee);
        assertEquals("MMTs equal", mmt_fee, mmt_tax);

        //Original debits should be in the same MMT
        assertEquals("MMTs equal", mmt_payroll_orig, mmt_fee_orig);
        assertEquals("MMTs equal", mmt_fee_orig, mmt_tax_orig);

        assertFalse("Original mmt and redebit mmt do not equal", mmt_payroll.equals(mmt_payroll_orig));

        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.RedebitAmountUpdated, CompanyEventStatus.Active, null, null);
        companyEvents_date = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.RedebitDateUpdated, CompanyEventStatus.Active, null, null);
        companyEvents_status = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ACHReturnStatusChanged, CompanyEventStatus.Active, null, null);

        assertEquals("Number of RedebitAmountUpdated events", 1, companyEvents.size());
        assertEquals("Number of RedebitDateUpdated events", 0, companyEvents_date.size());
        //This is from the first time we did a redebit for the payroll (in the dataloader)
        assertEquals("Number of ACHReturnStatusChanged events", 2, companyEvents_status.size());

        for (CompanyEvent currStatusEvent : companyEvents_status) {
            assertEquals("Event Detail",
                    currStatusEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId),
                    payrollRun.getId().toString());
            assertEquals("Event Detail",
                    currStatusEvent.getCompanyEventDetailValue(EventDetailTypeCode.OldPayrollStatus),
                    EnumUtils.getReadableName(PayrollStatus.DebitReturned));
            assertEquals("Event Detail",
                    currStatusEvent.getCompanyEventDetailValue(EventDetailTypeCode.NewPayrollStatus),
                    EnumUtils.getReadableName(PayrollStatus.PendingRedebit));
        }

        PayrollServices.commitUnitOfWork();

        //offload
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

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
    }

    @Test
    public void testAddorEditRedebitReturned() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
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

        Collection<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("700.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("1.97"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.05"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);

        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(taxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        assertEquals("Payroll Run Status", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // Verify the company events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ACHReturnStatusChanged, CompanyEventStatus.Active, null, null);
        assertEquals("Number of ACHReturnStatusChanged events", 1, companyEvents.size());
        DomainEntitySet<CompanyEventDetail> companyEventDetails =
                companyEvents.get(0).getCompanyEventDetailCollection();

        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId),
                payrollRun.getId().toString());
        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.OldPayrollStatus),
                EnumUtils.getReadableName(PayrollStatus.DebitReturned));
        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewPayrollStatus),
                EnumUtils.getReadableName(PayrollStatus.PendingRedebit));

        PayrollServices.commitUnitOfWork();

        //offload
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //return the txns
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Executed);

        Assert.assertEquals("Number of fin txns", 1, c1FinTxns.size());
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(c1FinTxns, "R01", "NSF Description");

        //ensure payrollrun status is ReturnedTwice
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.ReturnedTwice, payrollRun.getPayrollRunStatus());
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
        feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("0.01"), new DateDTO(PSPDate.getPSPTime()));
        taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.04"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(taxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);
        PayrollServices.commitUnitOfWork();

        assertSuccess("2nd redebit", procResult);

        //ensure payrollrun status is pending redebit
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();

        //offload
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071114000000");
        Application.commitUnitOfWork();

        //complete the txns
        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions processACHTxns = new ProcessACHTransactions();
        processACHTxns.process("20071114");
        PayrollServices.commitUnitOfWork();

        //ensure payrollrun status is back to DebitReturned
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.DebitReturned, payrollRun.getPayrollRunStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddorEditPendingAutoRedebit() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R01", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        DomainEntitySet<TransactionReturn> txnRets = TransactionReturn.findTransactionReturnsExcludedStatus(company, TransactionReturnStatusCode.Resolved);
        assertEquals("number of unresolved returns", 0, txnRets.size());

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 1, feeHashMap.size());
        assertEquals("Number of sales tax txns", 1, salesTaxHashMap.size());

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction feeTxn = feeHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("700.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("1.97"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);

        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> cancelledERDDredebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Cancelled);
        DomainEntitySet<FinancialTransaction> cancelledFeeredebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Cancelled);
        DomainEntitySet<FinancialTransaction> cancelledTaxredebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Cancelled);

        //We cancelled the NSF auto-redebit in the dataloader and then added another redebit in the dataloader.  Both of these are the cancelled er dd dbs
        assertEquals("Number of cancelled er dd redebits", 2, cancelledERDDredebits.size());
        assertEquals("Number of cancelledFeeredebits", 1, cancelledFeeredebits.size());
        assertEquals("Number of cancelledTaxredebits", 0, cancelledTaxredebits.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void bug652() {
        // like testAddorEditPendingAutoRedebit(), but updated fee redebit amount is 0.00 and updated payroll redebit amount is < orig redebit,
        // was throwing an NPE when AddOrEditPayrollRelatedRedebit.process() called CompanyEventBE.addManualRedebitCreatedEvent()

        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R01", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        DomainEntitySet<TransactionReturn> txnRets = TransactionReturn.findTransactionReturnsExcludedStatus(company, TransactionReturnStatusCode.Resolved);
        assertEquals("number of unresolved returns", 0, txnRets.size());

        HashMap<FinancialTransaction, SpcfMoney> payrollHashMap = payrollRun.getUncollectedDDAmount();
        HashMap<FinancialTransaction, SpcfMoney> feeHashMap = payrollRun.getUncollectedFeeAmounts();
        HashMap<FinancialTransaction, SpcfMoney> salesTaxHashMap = payrollRun.getUncollectedSalesTaxAmounts();

        assertEquals("Number of payroll txns", 1, payrollHashMap.size());
        assertEquals("Number of fee txns", 1, feeHashMap.size());
        assertEquals("Number of sales tax txns", 1, salesTaxHashMap.size());

        FinancialTransaction payrolltxn = payrollHashMap.keySet().iterator().next();
        FinancialTransaction feeTxn = feeHashMap.keySet().iterator().next();

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();

        //Create redebits for less than the total uncollected amounts for each txn
//        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("700.00"), new DateDTO(PSPDate.getPSPTime()));
//        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("1.97"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO payrollRedebit = new RedebitImpoundDTO(payrolltxn.getId().toString(), new SpcfMoney("1.00"), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("0"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
//        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);

        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        assertEquals("Payroll Run Status", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> cancelledERDDredebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerDdRedebit, TransactionStateCode.Cancelled);
        DomainEntitySet<FinancialTransaction> cancelledFeeredebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeRedebit, TransactionStateCode.Cancelled);
        DomainEntitySet<FinancialTransaction> cancelledTaxredebits = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.ServiceSalesAndUseTaxRedebit, TransactionStateCode.Cancelled);

        //We cancelled the NSF auto-redebit in the dataloader and then added another redebit in the dataloader.  Both of these are the cancelled er dd dbs
        assertEquals("Number of cancelled er dd redebits", 2, cancelledERDDredebits.size());
        assertEquals("Number of cancelledFeeredebits", 1, cancelledFeeredebits.size());
        assertEquals("Number of cancelledTaxredebits", 0, cancelledTaxredebits.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddOrEditRedebitAddAndUpdate() {
        ACHReturnsDataLoader.loadQBDTPayrollReturnedAddPayrollRedebit("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
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
        RedebitImpoundDTO feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2).subtract(SpcfDecimal.createInstance(0.01))), new DateDTO(PSPDate.getPSPTime()));
        RedebitImpoundDTO taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.03"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(taxRedebit, TransactionReturnStatusCode.Open, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

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

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071114000000");
        PayrollServices.commitUnitOfWork();

        //complete the txns
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
        feeRedebit = new RedebitImpoundDTO(feeTxn.getId().toString(), new SpcfMoney("0.01"), new DateDTO(PSPDate.getPSPTime()));
        taxRedebit = new RedebitImpoundDTO(taxTxn.getId().toString(), new SpcfMoney("0.05"), new DateDTO(PSPDate.getPSPTime()));

        collectionOfRedebitImpounds.add(payrollRedebit);
        collectionOfRedebitImpounds.add(feeRedebit);
        collectionOfRedebitImpounds.add(taxRedebit);

        procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        PayrollServices.commitUnitOfWork();

        assertSuccess("2nd redebit", procResult);

        PayrollServices.beginUnitOfWork();
        assertRedebitEquals(payrollRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerDdRedebit);
        assertRedebitEquals(feeRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.EmployerFeeRedebit);
        assertRedebitEquals(taxRedebit, TransactionReturnStatusCode.Resolved, TransactionTypeCode.ServiceSalesAndUseTaxRedebit);

        PayrollServices.commitUnitOfWork();

        //offload
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

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
    }

    @Test
    public void testAddorEditRedebitHappy() {
        ACHReturnsDataLoader.loadQBDTPayrollReturned("R02", "Non-NSF return");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071009000000");

        Company company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        assertTrue(financialTxs.size() == 1);
        FinancialTransaction originalTxn = financialTxs.get(0);
        String originalTxnId = originalTxn.getId().toString();
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxnId);

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebitImpounds);
        assertRedebitEquals(redebitDTO, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        // Commit
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        assertEquals("PayrollRunStatus", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // verify events
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.ACHReturnStatusChanged, CompanyEventStatus.Active, null, null);
        assertEquals("Number of ACHReturnStatusChanged events", 1, companyEvents.size());
        DomainEntitySet<CompanyEventDetail> companyEventDetails =
                companyEvents.get(0).getCompanyEventDetailCollection();

        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId),
                payrollRun.getId().toString());
        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.OldPayrollStatus),
                EnumUtils.getReadableName(PayrollStatus.DebitReturned));
        assertEquals("Event Detail",
                companyEvents.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewPayrollStatus),
                EnumUtils.getReadableName(PayrollStatus.PendingRedebit));

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        RedebitImpoundDTO redebitDTO2 = new RedebitImpoundDTO();
        redebitDTO2.setAmount(new SpcfMoney("8.00"));
        redebitDTO2.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO2.setOriginalFinancialTxId(originalTxnId);

        List<RedebitImpoundDTO> collectionOfRedebit2Impounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebit2Impounds.add(redebitDTO2);
        ProcessResult procResult2 = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBDT, "8574536", collectionOfRedebit2Impounds);
        assertRedebitEquals(redebitDTO2, TransactionReturnStatusCode.Open, TransactionTypeCode.EmployerDdRedebit);
        PayrollServices.commitUnitOfWork();

        assertSuccess(procResult2);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("8574536", SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest09");

        assertEquals("PayrollRunStatus", PayrollStatus.PendingRedebit, payrollRun.getPayrollRunStatus());

        // verify events
        DomainEntitySet<CompanyEvent> companyEvents2 = CompanyEvent.findCompanyEvents(company,
                EventTypeCode.RedebitAmountUpdated, CompanyEventStatus.Active, null, null);
        assertEquals("Number of RedebitAmountUpdated events", 1, companyEvents2.size());
        DomainEntitySet<CompanyEventDetail> companyEventDetails2 =
                companyEvents2.get(0).getCompanyEventDetailCollection();

        assertEquals("Event Detail",
                companyEvents2.get(0).getCompanyEventDetailValue(EventDetailTypeCode.PayrollRunId),
                payrollRun.getId().toString());
        assertEquals("Event Detail",
                companyEvents2.get(0).getCompanyEventDetailValue(EventDetailTypeCode.OldAmount),
                "777.77");
        assertEquals("Event Detail",
                companyEvents2.get(0).getCompanyEventDetailValue(EventDetailTypeCode.NewAmount),
                "8.00");

        PayrollServices.commitUnitOfWork();

    }

    @Test
    /**
     *  Test error message 169 - Company Does Not Exist
     */
    public void redebitAddOrEditCompanyDoesNotExist() {

        PayrollServices.beginUnitOfWork();
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        txRetDataLoader.loadDataForTransactionReturn();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        FinancialTransaction originalTxn = null;
        // Get the employer debit transactions returned for the payroll
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Returned});
        assertTrue(financialTxs.size() == 1);
        originalTxn = financialTxs.get(0);
        RedebitImpoundDTO redebitDTO = new RedebitImpoundDTO();
        redebitDTO.setAmount(originalTxn.getFinancialTransactionAmount());
        redebitDTO.setInitiationDate(new DateDTO(PSPDate.getPSPTime()));
        redebitDTO.setOriginalFinancialTxId(originalTxn.getId().toString());

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitDTO);
        ProcessResult procResult = PayrollServices.financialTransactionManager.addOrEditPayrollRelatedRedebitImpound(SourceSystemCode.QBOE, "InvalidCompanyId", collectionOfRedebitImpounds);

        // Commit
        PayrollServices.commitUnitOfWork();

        out.println(procResult);

        // validate error count
        assertTrue("Number of Errors:", procResult.getMessages().size() == 1);

        // vaildate error code
        Message message = procResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());

    }

    private void assertRedebitEquals(RedebitImpoundDTO pRedebitImpoundDTO, TransactionReturnStatusCode pTxnRetStatusCode, TransactionTypeCode pExpectedTxnTypeCode) {
        //Test new redebit
        FinancialTransaction originalTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                SpcfUniqueId.createInstance(pRedebitImpoundDTO.getOriginalFinancialTxId()));

        assertNotNull("Original transaction exists", originalTransaction);

        FinancialTransaction pendingRedebit = FinancialTransaction.getPendingRedebitTransaction(pRedebitImpoundDTO.getOriginalFinancialTxId());

        assertNotNull("Pending redebit exists", pendingRedebit);
        assertEquals("Amount", pRedebitImpoundDTO.getAmount(), pendingRedebit.getFinancialTransactionAmount());
        assertEquals("Original transaction", originalTransaction, pendingRedebit.getOriginalTransaction());
        assertEquals("Sku", originalTransaction.getSku(), pendingRedebit.getSku());

        SpcfCalendar expectedSettlementDate = DateDTO.convertToSpcfCalendar(pRedebitImpoundDTO.getInitiationDate());
        expectedSettlementDate.addDays(1);

        assertEquals("Expected txn type", pExpectedTxnTypeCode, pendingRedebit.getTransactionType().getTransactionTypeCd());
        assertEquals("Settlement Date", expectedSettlementDate, pendingRedebit.getSettlementDate().toLocal());
        assertEquals("Settlement Type", SettlementType.ACH, pendingRedebit.getSettlementTypeCd());
        assertEquals("Payroll Run", originalTransaction.getPayrollRun(), pendingRedebit.getPayrollRun());
        assertEquals("Company", originalTransaction.getCompany(), pendingRedebit.getCompany());

        IntuitBankAccount intuit = IntuitBankAccount.findIntuitBankAccount(
                TransactionType.findTransactionType(pExpectedTxnTypeCode),
                CreditDebitCode.Credit);

        assertEquals("getCreditBankAccount", intuit.getBankAccount(), pendingRedebit.getCreditBankAccount());
        assertEquals("getCreditBankAccountType", BankAccountOwnerType.Intuit, pendingRedebit.getCreditBankAccountType());

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(originalTransaction.getCompany());

        assertEquals("getDebitBankAccount", cba.getBankAccount(), pendingRedebit.getDebitBankAccount());
        assertEquals("getDebitBankAccountType", BankAccountOwnerType.Company, pendingRedebit.getDebitBankAccountType());
        assertEquals("getSkuQuantity", originalTransaction.getSkuQuantity(), pendingRedebit.getSkuQuantity());

        //Test transaction response
        DomainEntitySet<TransactionResponse> txnResponses = TransactionResponse.findTransactionResponses(pendingRedebit);

        assertEquals("One txn response", 1, txnResponses.size());

        TransactionResponse txnResponse = txnResponses.get(0);

        assertEquals("Company", originalTransaction.getCompany(), txnResponse.getCompany());
        assertNull("SourceRequestId", txnResponse.getSourceRequestId());
        assertNotNull("Token", txnResponse.getTransactionTokenNumber());

        //Test transaction return
        TransactionReturn txnReturn = TransactionReturn.findTransactionReturns(originalTransaction).get(0);

        assertEquals("Transaction Return status code", pTxnRetStatusCode, txnReturn.getReturnStatusCd());
    }
}
