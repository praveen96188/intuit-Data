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
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

/**
 * AddWriteOffBadDebtTransactionDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddWriteOffBadDebtTransactionDataLoader {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        Application.beginUnitOfWork();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
    }

    public static void loadWriteOffBadDebtData() {
        ACHReturnsDataLoader.loadData2DayERGenericReturn();
    }

    public static void loadWriteOffBadDebtDataWithSetup() {
        loadBeforeTest();
        loadWriteOffBadDebtData();
    }
}
