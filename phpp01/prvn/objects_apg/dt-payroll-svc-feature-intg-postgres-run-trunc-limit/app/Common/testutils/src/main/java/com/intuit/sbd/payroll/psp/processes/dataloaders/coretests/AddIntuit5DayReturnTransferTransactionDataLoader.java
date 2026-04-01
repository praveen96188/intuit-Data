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
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * AddIntuit5DayReturnTransferTransactionDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddIntuit5DayReturnTransferTransactionDataLoader {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();
    public static Company1Dataloader c1dl;

    public static com.intuit.sbd.payroll.psp.domain.Company company = null;
    public static PayrollRun payrollRun = null;
    public static PayrollRunDTO payrollRunDTO = null;
    public static SpcfMoney finTxnAmt = null;

    public static PayrollRunDTO loadData(){
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        PSPDate.setPSPTime("20070914000000");
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c1dl.persistPayrollRun(payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        return payrollRunDTO;
    }

    public static void loadTestIntuit5DayReturnTransferProcessData() {
        payrollRunDTO = loadData();

        Application.beginUnitOfWork();
        company = Company.findCompany("1234567", SourceSystemCode.QBOE);

        payrollRun = PayrollRun.findPayrollRun(company, "BatchTest05");

        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployeeDdCredit},
                            new TransactionStateCode[] { TransactionStateCode.Created} );

        finTxnAmt = new SpcfMoney("0");
        for (FinancialTransaction eeFinTxn : eeFinancialTxs) {
            finTxnAmt =
                    new SpcfMoney(finTxnAmt.add(eeFinTxn.getFinancialTransactionAmount()));
            eeFinTxn.updateFinancialTransactionState(TransactionStateCode.Cancelled);
        }


        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();
    }

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadTestIntuit5DayReturnTransferProcessDataWithSetup() {
        loadBeforeTest();
        loadTestIntuit5DayReturnTransferProcessData();
    }
}
