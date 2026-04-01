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

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.Test;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;

/**
 * TransactionReverseCoreDataLoader - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class TransactionReverseCoreDataLoader {
    public static CompanyBankAccount sCompanyBankAccount;
    public static final SpcfCalendar prTxnReverseSubmitDate = SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()); // a Wednesday

    /**
     * Check the newly created payroll to ensure it was correctly submitted.
     *
     * @param dto The dto for the newly created payroll.
     */
    public static void checkPayrollLoad(PayrollRunDTO dto) {
        // Verify persisted data

        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);

        // Ensure the first payroll run was created correctly
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, dto.getPayrollTXBatchId());

        org.junit.Assert.assertTrue("PayrollRun Not Null", payrollRun1 != null);

        if (payrollRun1 != null) {
            SpcfMoney payrollRunAmount = new SpcfMoney("0.00");

            org.junit.Assert.assertEquals("PayrollRun Id:", payrollRun1.getSourcePayRunId(), dto.getPayrollTXBatchId());

            // Ensure that Paychecks were created correctly
            org.junit.Assert.assertEquals("Number of Paychecks:", payrollRun1.getPaycheckCollection().size(), dto.getPaychecks().size());

            for (PaycheckDTO paycheckDTO : dto.getPaychecks()) {
                Paycheck paycheck = Paycheck.findPaycheck(company, paycheckDTO.getPaycheckId());

                org.junit.Assert.assertTrue("Paycheck Not Null", paycheck != null);

                if (paycheck != null) {
                    org.junit.Assert.assertEquals("Employee Id:", paycheckDTO.getEmployeeId(), paycheck.getDDEmployee().getSourceEmployeeId());
                    org.junit.Assert.assertEquals("Number of Paycheck Splits:", paycheckDTO.getDdTransactions().size(), paycheck.getPaycheckSplits().size());

                    // Ensure that Paycheck Splits were created correctly
                    for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                        PaycheckSplit paycheckSplit = PaycheckSplit.findNonCanceledPaycheckSplit(company, ddTransactionDTO.getDDTransactionId());

                        org.junit.Assert.assertTrue(paycheckSplit != null);

                        if (paycheckSplit != null) {
                            payrollRunAmount.add(paycheckSplit.getPaycheckSplitAmount());

                            org.junit.Assert.assertEquals("Paycheck Split Amount:", SpcfUtils.convertToSpcfMoney(ddTransactionDTO.getDDTransactionAmount()), paycheckSplit.getPaycheckSplitAmount());
                            org.junit.Assert.assertEquals("EmployeeBankAccount:", ddTransactionDTO.getEmployeeBankAccount().getEmployeeBankAccountId(), paycheckSplit.getEmployeeBankAccount().getSourceBankAccountId());

                            // Ensure that Financial Transactions were created correctly
                            DomainEntitySet<FinancialTransaction> financialTransactions = paycheckSplit.getFinancialTransactions();

                            org.junit.Assert.assertEquals("Number of Financial Transactions:", 1, financialTransactions.size());

                            FinancialTransaction financialTransaction = (FinancialTransaction) financialTransactions.get(0);

                            org.junit.Assert.assertEquals("Financial Transaction Amount:", paycheckSplit.getPaycheckSplitAmount(), financialTransaction.getFinancialTransactionAmount());
                            org.junit.Assert.assertEquals("Financial Transaction Type:", TransactionTypeCode.EmployeeDdCredit, financialTransaction.getTransactionType().getTransactionTypeCd());
                        }
                    }
                }
            }
        }
    }

    public static void loadPayrollRunForTransactionReverseTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime(prTxnReverseSubmitDate);
        PSPDate.addDaysToPSPTime(-1);

        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();

        for (PayrollRunDTO dto : psdl.loadMultiplePayrollsWithMultiplePaycheckSplitsForCompany123272727(2)) {
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "123272727", dto);

            if (!processResult.isSuccess()) {
                for (Message msg : processResult.getMessages()) {
                    System.out.println("Message code: " + msg.getMessageCode() + ", Message: " + msg.getMessage());
                }
            }

            org.junit.Assert.assertTrue("Process Result", processResult.isSuccess());

            checkPayrollLoad(dto);
        }
        PayrollServices.commitUnitOfWork();
        // update the status of payroll BatchId01 to OffloadeAll
//        Company company = Company.findCompany("123272727", SourceSystemCode.QBOE);
//        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "BatchId01");

        // offload all txns of first payroll (BatchId01)
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    public static void loadPayrollRunForBPTransactionReverseTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();


        String sourceCompanyId = "123272727";

        // company setup
     
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull(company);
        
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 14; i++) {
            BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.beginUnitOfWork();

        for (BillPaymentDTO billPaymentDTO : billPaymentDTOs) {            
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO));
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertSuccess(submitResult);
        PayrollServices.commitUnitOfWork();

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

       
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

   
    }
}
