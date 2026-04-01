/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes.dataloaders.coretests;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;

/**
 * AddRecoverBadDebtTransactionDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddRecoverBadDebtTransactionDataLoader {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();
    public static SpcfMoney mFinTxnAmt= new SpcfMoney("0");

    public static void addWriteOffBadDebtTransaction(){
        // re-initialize to zero
        mFinTxnAmt= new SpcfMoney("0");
        
        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070926000000");
        Application.commitUnitOfWork();

        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturn();

        junit.framework.Assert.assertEquals("Number of txn returns", 1, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(
                SourceSystemCode.QBOE, "123272727", "BatchId01");

        Application.commitUnitOfWork();
        org.junit.Assert.assertTrue("Process Result", processResult.isSuccess());

        Application.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> finTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerWriteOff},
                new TransactionStateCode[]{TransactionStateCode.Created});

        for (FinancialTransaction finTxn : finTxns) {
            mFinTxnAmt = new SpcfMoney(mFinTxnAmt.add(finTxn.getFinancialTransactionAmount()));
            finTxn.updateFinancialTransactionState(TransactionStateCode.Executed);
        }

        Application.commitUnitOfWork();
    }

    public static DomainEntitySet<TransactionReturn> persistTransactionReturn() {
        Application.beginUnitOfWork();

        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        DomainEntitySet<FinancialTransaction> c1FinTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : c1FinTxns) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R02");
            transactionReturn.setBankReturnDescription("This is an Employer Debit return transaction");
            transactionReturn.setBankReturnTraceNumber(112L);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10,
                    SpcfTimeZone.getLocalTimeZone()));
            transactionReturn.setMoneyMovementTransaction(financialTransaction.getMoneyMovementTransaction());
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setCompany(financialTransaction.getCompany());
            
            returnList.add(Application.save(transactionReturn));
        }
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        Application.commitUnitOfWork();
        return returnList;
    }

    public static PayrollRun writeOff() {
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
        com.intuit.sbd.payroll.psp.domain.Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> ddftList = FinancialTransaction.findFinancialTransactions(srcSystemCd, srcCompanyId, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        FinancialTransaction ddft = ddftList.get(0);

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(payroll.getCompany());
        DomainEntitySet<BillingDetail> billingDetails = BillingDetail.createBillingDetail(payrollRun, cba, OfferingServiceChargeType.ReversalFee, 1,
                ddft.getSettlementDate().toLocal(), null);

        PayrollServices.commitUnitOfWork();

        assertTrue("Fee was added", billingDetails.isNotEmpty());

        PayrollServices.beginUnitOfWork();

        for (BillingDetail billingDetail : billingDetails) {
            // get fresh copies of things
            billingDetail = Application.findById(BillingDetail.class, billingDetail.getId());

            // advance PSPTime to the date when that fee would be offloaded
            PSPDate.setPSPTime(billingDetail.getFeeTransaction().getMoneyMovementTransaction().getInitiationDate());
            Application.commitUnitOfWork();

            // offload it
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            Application.beginUnitOfWork();
            PSPDate.addDaysToPSPTime(1);
            billingDetail = Application.findById(BillingDetail.class, billingDetail.getId());
            MoneyMovementTransaction mmt = billingDetail.getFeeTransaction().getMoneyMovementTransaction();

            billingDetail = Application.findById(BillingDetail.class, billingDetail.getId());

            // assert fee/tax offloaded, so it can be refunded as part of the rebill
            Assert.assertEquals("fee FT state", TransactionStateCode.Executed,
                    billingDetail.getFeeTransaction().getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("tax FT state", TransactionStateCode.Executed,
                    billingDetail.getTaxTransaction().getCurrentTransactionState().getTransactionStateCd());
            Assert.assertEquals("fee FT's MMT payment status", PaymentStatus.Executed, mmt.getStatus());
            Assert.assertEquals("MMT has right number of FTs", 2, mmt.getFinancialTransactionCollection().size()); // 1 DD, 2 fee, 1 tax (for the reversal fee)

            ddftList = FinancialTransaction.findFinancialTransactions(srcSystemCd, srcCompanyId, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

            // simulate a bank return of those transactions
            DomainEntitySet<MoneyMovementTransaction> payrollMMTs = new DomainEntitySet<MoneyMovementTransaction>();
            mmt = PayrollServices.entityFinder.findById(MoneyMovementTransaction.class, mmt.getId());

            payrollMMTs.add(mmt);
            DomainEntitySet<TransactionReturn> returns = ACHReturnsDataLoader.persistTransactionReturns(ddftList, "R02", "Non-NSF Return");
            returns.addAll(ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R02", "Non-NSF Return"));
            for (TransactionReturn txnReturn : returns) {
                TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(txnReturn);
                txnReturn = handler.execute(txnReturn);
            }

            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();

            ProcessResult prWriteoff;
            prWriteoff = PayrollServices.financialTransactionManager.addWriteOffBadDebtTransaction(srcSystemCd, srcCompanyId,
                    payroll.getSourcePayRunId());

            PayrollServices.commitUnitOfWork();

            PayrollServicesTest.assertSuccess("Writeoff", prWriteoff);

            PayrollServices.beginUnitOfWork();

            // advance the PSPTime and offload the writeoff FTs
            DomainEntitySet<FinancialTransaction> writeoffFT = FinancialTransaction.findFinancialTransactions(srcSystemCd, srcCompanyId, TransactionTypeCode.EmployerWriteOff, TransactionStateCode.Created);
            mmt = writeoffFT.get(0).getMoneyMovementTransaction();
            PSPDate.setPSPTime(mmt.getInitiationDate());
            Application.commitUnitOfWork();

            OffloadACHTransactions offloader2 = new OffloadACHTransactions();
            offloader2.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

            PayrollServicesTest.assertSuccess("Writeoff", prWriteoff);
        }

        return payroll;
    }


    private static ProcessResult<PayrollRun> submitPayroll() {
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        // this creates the company and other stuff and offloads the bank verfication debits
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();

        // set the company's tax-exempt expiration date based on the input param
        AddressDTO pLegalAddress = DataLoader.TAXABLE_ADDRESS;
        if (pLegalAddress != null) {
            Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
            DTOFactory fac = new DTOFactory();
            CompanyDTO dtoUpdate = fac.create(company);
            dtoUpdate.setLegalAddress(pLegalAddress);
            ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoUpdate);
            PayrollServicesTest.assertSuccess("Updating company legal address", prUpdate);
        }
        PayrollServices.commitUnitOfWork();

        // this submits the payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> prPayroll = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        return prPayroll;
    }

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadAddRecoverBadDebtDataWithSetup() {
        loadBeforeTest();
        loadAddRecoverBadDebtData();
    }

    public static void loadAddRecoverBadDebtData() {
        Application.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        addWriteOffBadDebtTransaction();
    }

    public static PayrollRun loadAddRecoverBadDebtDataWithFee() {
        loadBeforeTest();
        PayrollRun payroll = writeOff();
        return payroll;
    }
}
