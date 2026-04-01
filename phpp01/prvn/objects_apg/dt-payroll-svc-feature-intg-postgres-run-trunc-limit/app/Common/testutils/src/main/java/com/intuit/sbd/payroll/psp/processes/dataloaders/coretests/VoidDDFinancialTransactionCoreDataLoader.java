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

import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import static junit.framework.Assert.assertEquals;

/**
 * VoidDDFinancialTransactionCoreDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class VoidDDFinancialTransactionCoreDataLoader {
    public static PayrollRun payrollRun = null;

    public static PayrollRunDTO loadTestTxVoidData() {
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmit();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        return payrollRunDTO;
    }

    public static void loadBeforeTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 6, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    public static void loadTestTxVoidDataACHWithSetup() {
        loadBeforeTest();

        loadTestTxVoidDataACH();
    }

    public static void loadTestTxVoidDataNonACHWithSetup() {
        loadBeforeTest();

        loadTestTxVoidDataACH();
    }

    public static DomainEntitySet<FinancialTransaction> loadTestTxVoidDataACH() {
        PayrollRunDTO payrollRunDTO = loadTestTxVoidData();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        Date txDate = new Date(PSPDate.getPSPTime().toLocal().getTimeInMilliseconds());
        ERFeeAddDTO feeAddDTO  = new ERFeeAddDTO(SourceSystemCode.QBOE, "123272727", payrollRunDTO.getPayrollTXBatchId(),
                                                 SettlementTypeDTO.Wire, txDate, new SpcfMoney("50.00"),
                                                 OfferingServiceChargeType.ReversalFee, null);
        ProcessResult<DomainEntitySet<FinancialTransaction>> feeResult = PayrollServices.financialTransactionManager.addFeeTransaction(feeAddDTO);
        assertSuccess("fee result", feeResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[]{TransactionStateCode.Completed});
        PayrollServices.commitUnitOfWork();
        return financialTxs;
    }

    public static DomainEntitySet<FinancialTransaction> loadTestTxVoidNonACHRedebit() {
        // Create and Offload PayrollRun
        PayrollRunDTO payrollRunDTO = loadTestTxVoidData();

        // Return the employer transaction
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        PSPDate.setPSPTime("20070930000000");
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R01",
                "This is an NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        // Create a Non-Ach Redebit Transaction
        Application.beginUnitOfWork();
        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 30,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());

        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        DomainEntitySet<FinancialTransaction> redebitTxs = (DomainEntitySet<FinancialTransaction>) processResult.getResult();
        PayrollServices.commitUnitOfWork();
        return redebitTxs;
    }

    public static DomainEntitySet<FinancialTransaction> loadTestTxVoidNonACHRedebitBalance(boolean pLeaveBalance) {
        // Create and Offload PayrollRun
        PayrollRunDTO payrollRunDTO = loadTestTxVoidData();

        // Return the employer transaction
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);
        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});
        PSPDate.setPSPTime("20070930000000");
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(financialTxs, "R01",
                "This is an NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EEDDCR txns", 1, financialTxs.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        SpcfMoney ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                VoidDDFinancialTransactionCoreDataLoader.payrollRun.getSourcePayRunId(), VoidDDFinancialTransactionCoreDataLoader.payrollRun.getCompany());
        PayrollServices.commitUnitOfWork();
        // Create a Non-Ach Redebit Transaction
        Application.beginUnitOfWork();
        RedebitImpoundDTO redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 30,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());

        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());
        List<RedebitImpoundDTO> collectionOfRedebitImpounds = new ArrayList<RedebitImpoundDTO>();
        collectionOfRedebitImpounds.add(redebitImpoundDTO);

        redebitImpoundDTO = new RedebitImpoundDTO();
        redebitImpoundDTO.setSettlementType(SettlementTypeDTO.Wire);
        redebitImpoundDTO.setInitiationDate(new DateDTO(SpcfCalendar.createInstance(2007, 9, 30,
                SpcfTimeZone.getLocalTimeZone())));
        redebitImpoundDTO.setOriginalFinancialTxId(financialTxs.get(0).getId().toString());
        redebitImpoundDTO.setAmount(financialTxs.get(0).getFinancialTransactionAmount());
        collectionOfRedebitImpounds.add(redebitImpoundDTO);
        ProcessResult processResult = PayrollServices.financialTransactionManager.addPayrollRelatedNonACHRedebit(
                SourceSystemCode.QBOE, company.getSourceCompanyId(), collectionOfRedebitImpounds);
        DomainEntitySet<FinancialTransaction> redebitTxs = (DomainEntitySet<FinancialTransaction>) processResult.getResult();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        ledgerBalance = LedgerAccount.getLedgerAccountBalanceByPayroll(LedgerAccountCode.ERReturnReceivable,
                VoidDDFinancialTransactionCoreDataLoader.payrollRun.getSourcePayRunId(), VoidDDFinancialTransactionCoreDataLoader.payrollRun.getCompany());
        PayrollServices.commitUnitOfWork();
        return redebitTxs;
    }

    public static DomainEntitySet<FinancialTransaction> loadTestTxDataNonACH() {
        PayrollRunDTO payrollRunDTO = loadTestTxVoidData();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(
                "123272727", SourceSystemCode.QBOE);

        payrollRun = PayrollRun.findPayrollRun(company,
                payrollRunDTO.getPayrollTXBatchId());
        DomainEntitySet<FinancialTransaction> financialTxs =
                payrollRun.getFinancialTransactions(
                        new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[]{TransactionStateCode.Executed});

        // update the tx state to complete
        financialTxs.get(0).updateFinancialTransactionState(TransactionStateCode.Completed);
        PayrollServices.commitUnitOfWork();
        return financialTxs;
    }
}
