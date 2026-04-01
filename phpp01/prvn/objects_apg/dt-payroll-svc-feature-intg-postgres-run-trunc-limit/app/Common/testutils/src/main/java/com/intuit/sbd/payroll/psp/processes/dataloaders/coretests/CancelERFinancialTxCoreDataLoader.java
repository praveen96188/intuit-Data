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
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionReverseDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * CancelERFinancialTxCoreDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class CancelERFinancialTxCoreDataLoader {
    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadTestERCancelData() {
        PayrollServices.beginUnitOfWork();
        //Submit payroll Data
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        txRetDataLoader.loadTxData();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        //Post the transaction return for Employee DD Credit
        DomainEntitySet<TransactionReturn> returnList = txRetDataLoader.persistTransactionReturn(
                "R02",
                TransactionTypeCode.EmployeeDdCredit);
        PayrollServices.commitUnitOfWork();

        //Execute DirectDeposit Reject Return Event
        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();

    }

    public static void loadTestERCancelDataWithSetup() {
        loadBeforeTest();
        loadTestERCancelData();
    }

    public static void loadEEReversalData(boolean pIntuitInitiated) {
        PayrollServices.beginUnitOfWork();
        //Submit payroll Data
        RedebitAddTestDataLoader txRetDataLoader = new RedebitAddTestDataLoader();
        txRetDataLoader.loadTxData();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        if (pIntuitInitiated) {
            PayrollServices.beginUnitOfWork();
            PSPDate.addDaysToPSPTime(1);
            Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");
            DomainEntitySet<FinancialTransaction> payrollFTs = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                    new TransactionStateCode[]{TransactionStateCode.Executed});
            DomainEntitySet<MoneyMovementTransaction> payrollMMTs = ACHReturnsDataLoader.getMoneyMovementTransactions(payrollFTs, true); // Executed-only
            DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.createTransactionReturns(payrollMMTs, "R01", "NSF return");
            PayrollServices.commitUnitOfWork();
            assertEquals("Number of txn returns", 1, returnList.size());

            Application.beginUnitOfWork();
            TransactionReturn transactionReturn = returnList.get(0);

            TransactionReturnHandler returnHandler = TransactionReturnHandler.
                    getTransactionReturnHandler(transactionReturn);

            returnHandler.execute(transactionReturn);
            Application.commitUnitOfWork();
        }
        

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        TransactionReverseDTO txnReverseDTO = new TransactionReverseDTO();
        txnReverseDTO.setChargeFee(!pIntuitInitiated);
        txnReverseDTO.setIntuitInitiatedReversals(pIntuitInitiated);
        txnReverseDTO.setSourcePayrollRunId("BatchId01");
//        txnReverseDTO.setDdTransactionIdList(txnsToReverse);
//        txnReverseDTO.setTxDate(null);
        txnReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);
        ProcessResult reverseTxnProcResult =
                PayrollServices.payrollManager.reverseTransaction(SourceSystemCode.QBOE, "123272727", txnReverseDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(reverseTxnProcResult);
    }


}
