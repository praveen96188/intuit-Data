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
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnBatch;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnBatchStatusCode;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnStatusCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * AddEmployeeReturnRefundTransactionDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddEmployeeReturnRefundTransactionDataLoader {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();

    public static void submitPayroll(){
        //Submit the Payroll
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();

        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 18, SpcfTimeZone.getLocalTimeZone()));
        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        org.junit.Assert.assertTrue("Process Result", payrollProcess.isSuccess());
    }

    public static void loadTestEEReturnRefundProcessForACHData() {
        submitPayroll();

        //Post the transaction return for Employer DD Credit
        DomainEntitySet<TransactionReturn> returnList = persistTransactionReturn();

        //Execute DirectDepositRejectReturn Handler
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> ddRejectRefundCrTxns = payrollRun.
                getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRejectRefundCredit},
                        new TransactionStateCode[]{TransactionStateCode.Created});

        for(FinancialTransaction financialTransaction:ddRejectRefundCrTxns ){
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Cancelled);
        }

        PayrollServices.commitUnitOfWork();
    }

    private static DomainEntitySet<TransactionReturn> persistTransactionReturn() {

        Application.beginUnitOfWork();

        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName("");
        transactionReturnBatch.setReturnDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusEffectiveDate(SpcfCalendar.createInstance(2007, 12, 10, SpcfTimeZone.getLocalTimeZone()));
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

        transactionReturnBatch = Application.save(transactionReturnBatch);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        TransactionReturn transactionReturn;
        DomainEntitySet<TransactionReturn> returnList = new DomainEntitySet<TransactionReturn>();

        for (FinancialTransaction financialTransaction : eeFinancialTxs) {
            transactionReturn = new TransactionReturn();
            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("This is an Employee DD Refund return transaction");
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

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadTestEEReturnRefundProcessForACHDataWithSetup() {
        loadBeforeTest();
        loadTestEEReturnRefundProcessForACHData();
    }
}
