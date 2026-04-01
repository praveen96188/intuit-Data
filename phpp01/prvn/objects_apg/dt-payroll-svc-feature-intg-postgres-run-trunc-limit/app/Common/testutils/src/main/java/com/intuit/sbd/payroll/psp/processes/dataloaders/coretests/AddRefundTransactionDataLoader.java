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

import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * AddRefundTransactionDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddRefundTransactionDataLoader {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();
    public static SpcfMoney finTxnAmt = new SpcfMoney("0");

    public static void loadAddRefundTransctionForACHData() {
        //set PSP Date
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();


        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        Application.commitUnitOfWork();

        org.junit.Assert.assertTrue("Process Result", payrollProcess.isSuccess());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        finTxnAmt = new SpcfMoney("0");
        for (FinancialTransaction eeFinTxn : eeFinancialTxs) {
            finTxnAmt = new SpcfMoney(finTxnAmt.add(eeFinTxn.getFinancialTransactionAmount()));
            eeFinTxn.updateFinancialTransactionState(TransactionStateCode.Cancelled);
        }

        Application.commitUnitOfWork();
    }

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadAddRefundTransctionForACHDataWithSetup() {
        loadBeforeTest();
        loadAddRefundTransctionForACHData();
    }
}
