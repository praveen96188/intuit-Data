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
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * AddFeeTransferTransactionDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class AddFeeTransferTransactionDataLoader {
    public static PayrollSubmitDataLoader psd1 = new PayrollSubmitDataLoader();

    public static PayrollRunDTO loadTestFeeTransferProcessForNSFData() {
        Application.beginUnitOfWork();
        //set PSP Date
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO payrollRunDTO = psd1.loadDataForPayrollSubmit();

        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        TransactionType transactionType = TransactionType.findTransactionType(
                TransactionTypeCode.EmployerWriteOff);

        IntuitBankAccount creditIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType,
                CreditDebitCode.Credit);

        IntuitBankAccount debitIntuitBankAccount = IntuitBankAccount.findIntuitBankAccount(transactionType,
                CreditDebitCode.Debit);

       FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(company, payrollRun,
                null, creditIntuitBankAccount.getBankAccount(), debitIntuitBankAccount.getBankAccount(),
                BankAccountOwnerType.Intuit, BankAccountOwnerType.Intuit,
                TransactionTypeCode.EmployerWriteOff, new SpcfMoney("1000.81"), SettlementType.ACH,
                FinancialTransaction.getSettlementDate(TransactionTypeCode.EmployerWriteOff,
                        company.getOffloadGroup()));

        financialTransaction.updateFinancialTransactionState(
                TransactionStateCode.Executed);

        Application.commitUnitOfWork();
        return payrollRunDTO;
    }

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadTestFeeTransferProcessForNSFDataWithSetup() {
        loadBeforeTest();
        loadTestFeeTransferProcessForNSFData();
    }
}
