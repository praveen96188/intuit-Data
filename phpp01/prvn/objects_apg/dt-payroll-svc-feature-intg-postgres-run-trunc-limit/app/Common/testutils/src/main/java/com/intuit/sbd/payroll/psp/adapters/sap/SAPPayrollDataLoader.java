/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.ArrayList;

import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 * SAPPayrollDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class SAPPayrollDataLoader {

     public static void before() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
         ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadQBDTCompanyRequests1TxnReversed(){
        before();
        ACHReturnsDataLoader.loadQBDTCompanyRequests1TxnReversed();
    }

    public static void loadCompanyWith2NSFPayrolls(){
        before();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070822000000");
        Company1Dataloader companyDataloader = new Company1Dataloader();
        companyDataloader.persistCompany1();
        companyDataloader.updateTo2DayFundingModel();
        PayrollRunDTO payrollRunDTO = companyDataloader.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        PayrollServices.commitUnitOfWork();

        assertEquals("company source system", SourceSystemCode.QBOE, companyDataloader.getCompany().getSourceSystemCd());

        //Submit Payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // offload the first payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBOE, companyDataloader.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("number of erDdDebit transactions", 1, financialTransactions.size());

        DomainEntitySet<TransactionReturn> returnList =
                ACHReturnsDataLoader.persistTransactionReturns(financialTransactions, "R01", "This is a non-NSF description");        
        Application.commitUnitOfWork();

        // find the right handler and execute it
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.removeOnHoldReason(companyDataloader.getCompany().getSourceSystemCd(),
                companyDataloader.getCompany().getSourceCompanyId(),
                ServiceSubStatusCode.AchRejectR1R9);
        PayrollServices.commitUnitOfWork();
        assertSuccess("hold removed", result);

        // submit a second payroll
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = companyDataloader.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-04"));
        submitPayrollResult = PayrollServices.payrollManager
                .submitPayroll(companyDataloader.getCompany().getSourceSystemCd(), companyDataloader.getCompany().getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        // Ensure processing was succsessful
        assertSuccess("submitPayroll", submitPayrollResult);

        // offload it
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        financialTransactions = FinancialTransaction.
                findFinancialTransactions(SourceSystemCode.QBOE, companyDataloader.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);

        assertEquals("number of erDdDebit transactions", 1, financialTransactions.size());

        returnList =
                ACHReturnsDataLoader.persistTransactionReturns(financialTransactions, "R01", "This is a non-NSF description");

        Application.commitUnitOfWork();

        // find the right handler and execute it
        Application.beginUnitOfWork();
        transactionReturn = returnList.get(0);
        returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();
    }
}
