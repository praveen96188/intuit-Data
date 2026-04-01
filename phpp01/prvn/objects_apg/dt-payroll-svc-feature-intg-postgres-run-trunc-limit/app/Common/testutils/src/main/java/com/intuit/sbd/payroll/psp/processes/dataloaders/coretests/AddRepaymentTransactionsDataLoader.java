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
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * AddRepaymentTransactionsDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddRepaymentTransactionsDataLoader {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void submitPayroll() {
        //set PSP Date
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        ProcessResult<PayrollRun> payrollProcess = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE,
                "123272727", payrollRunDTO);
        Application.commitUnitOfWork();

        org.junit.Assert.assertTrue("Process Result", payrollProcess.isSuccess());
    }

    public static void submitPayrollWithSetup() {
        loadBeforeTest();
        submitPayroll();
    }
}
