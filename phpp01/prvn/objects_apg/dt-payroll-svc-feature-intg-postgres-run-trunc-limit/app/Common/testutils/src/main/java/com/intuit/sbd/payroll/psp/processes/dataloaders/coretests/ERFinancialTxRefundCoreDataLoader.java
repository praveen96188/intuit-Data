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
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.TransactionReturn;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.processes.dataloaders.RedebitAddTestDataLoader;

/**
 * ERFinancialTxRefundCoreDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class ERFinancialTxRefundCoreDataLoader {
    public static void addEmployerDDRejectRefundTransaction() throws Exception{

        PayrollServicesTest.truncateTables();

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

        Application.beginUnitOfWork();
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


        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071002000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        //Post the transaction return for Employer DD Reject Refund Credit
        returnList = txRetDataLoader.persistTransactionReturn(
                "R01",
                TransactionTypeCode.EmployerDdRejectRefundCredit);
        Application.commitUnitOfWork();

        //Execute Refund Return Event
        Application.beginUnitOfWork();
        transactionReturn = returnList.get(0);

        returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        Application.commitUnitOfWork();
    }
}
