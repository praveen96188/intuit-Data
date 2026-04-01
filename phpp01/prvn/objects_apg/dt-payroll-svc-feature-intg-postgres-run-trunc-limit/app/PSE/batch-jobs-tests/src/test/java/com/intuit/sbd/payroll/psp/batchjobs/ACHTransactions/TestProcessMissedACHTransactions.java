package com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company2Dataloader;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Apr 9, 2008
 * Time: 1:52:10 PM
 */
public class TestProcessMissedACHTransactions {
    private static Company1Dataloader c1dl;
    private static Company2Dataloader c2dl;

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    private void loadData() {
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        c2dl = new Company2Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        loadDataHappyPath();
        PayrollServices.commitUnitOfWork();        
    }

    @Test
    public void testProcessNoMissedACHTxns() {
        loadData();
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload only QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload only QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20070928");
        PayrollServices.commitUnitOfWork();

        // verify no txns are cancelled
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 0);

        payroll1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Executed});

        // verify no transaction responses are created
        DomainEntitySet<TransactionResponse> txResponses =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 0, txResponses.size());

        txResponses = TransactionResponse.findTransactionResponses(payroll1FinTxns.get(1));
        assertEquals("Number of Transaction Responses", 0, txResponses.size());

        txResponses = TransactionResponse.findTransactionResponses(payroll1FinTxns.get(2));
        assertEquals("Number of Transaction Responses", 0, txResponses.size());

        PayrollServices.commitUnitOfWork();

        // verify no notification message
        assertEquals("Notification Message ", null, notificationMessage);

    }

    @Test
    public void testProcessOffloadDateInFuture() {
        loadData();
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload only QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload only QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String errorMessage = null;
        try {
            missedTxProcessor.process("20070929");
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
        assertEquals("Error message", "Invalid processing date specified: 20070929 (must be <= 20070928)", errorMessage);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testProcessMissedACHTxnsForSingleCompany_HappyPath() {
        loadData();
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload only QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // add one more payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
        PayrollServices.commitUnitOfWork();

        // offload Company1 2nd payroll EE DD CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        missedTxProcessor.process("20071005");
        String errorMessage = missedTxProcessor.getErrorMessage();
        PayrollServices.commitUnitOfWork();

        // verify the ER txns are cancelled
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 1);
        DomainEntitySet<FinancialTransaction> payroll2FinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll2FinTxns.size(), 1);

        // verify no transaction responses are created
        DomainEntitySet<TransactionResponse> txResponses1 =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 1, txResponses1.size());

        DomainEntitySet<TransactionResponse> txResponses2 = TransactionResponse.findTransactionResponses(payroll2FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 1, txResponses2.size());

        // verify the two transaction respones are the same
        assertEquals("Transaction Responses", txResponses1.get(0), txResponses2.get(0));

        PayrollServices.commitUnitOfWork();

        String message =
                "Company 1\n" +
                        "   Source System Code: QBOE\n" +
                        "   Source Company ID:  1234567\n" +
                        "   Company Legal Name: Intuit\n" +
                        "   DD Service Status:  ActiveCurrent\n" +
                        "   Payroll 1\n" +
                        "      Src Payroll Run ID: BatchTest002\n" +
                        "      Created Date:       " + payRun2C1.getCreatedDate().toLocal().toString() + "\n" +
                        "      Paycheck Date:      2007/10/10 00:00:00.0\n" +
                        "      Net Payroll Amount: $250.00\n" +
                        "      Tx 1\n" +
                        "         Src DD Txn ID:   N/A\n" +
                        "         Txn Type Code:   EmployerFeeDebit\n" +
                        "         Initiation Date: 2007/10/02 00:00:00.0\n" +
                        "         Settlement Date: 2007/10/03 00:00:00.0\n" +
                        "         Txn Amount:      $0.00\n" +
                        "      Tx 2\n" +
                        "         Src DD Txn ID:   N/A\n" +
                        "         Txn Type Code:   EmployerDdDebit\n" +
                        "         Initiation Date: 2007/10/02 00:00:00.0\n" +
                        "         Settlement Date: 2007/10/03 00:00:00.0\n" +
                        "         Txn Amount:      $250.00\n" +
                        "   Payroll 2\n" +
                        "      Src Payroll Run ID: BatchTest05\n" +
                        "      Created Date:       " + payRun1C1.getCreatedDate().toLocal().toString() + "\n" +
                        "      Paycheck Date:      2007/10/02 00:00:00.0\n" +
                        "      Net Payroll Amount: $180.00\n" +
                        "      Tx 1\n" +
                        "         Src DD Txn ID:   N/A\n" +
                        "         Txn Type Code:   EmployerFeeDebit\n" +
                        "         Initiation Date: 2007/09/25 00:00:00.0\n" +
                        "         Settlement Date: 2007/09/26 00:00:00.0\n" +
                        "         Txn Amount:      $0.00\n" +
                        "      Tx 2\n" +
                        "         Src DD Txn ID:   N/A\n" +
                        "         Txn Type Code:   EmployerDdDebit\n" +
                        "         Initiation Date: 2007/09/25 00:00:00.0\n" +
                        "         Settlement Date: 2007/09/26 00:00:00.0\n" +
                        "         Txn Amount:      $180.00";

        // verify the notification message
        assertEquals("Notification Message ", message, errorMessage.trim());
    }

    /**
     * This test method verifies the non-payroll and payroll txns of two different companies are cancelled
     */
    @Test
    public void testProcessMissedACHTxnsForMultipleCompanies_HappyPath() {
        loadData();
        // add company2 and payroll
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        Company company2 = dataloader.persistCompany(c2dl.getCompany1());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDDService(company2);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.addCompanyBankAccount(
                company2.getSourceSystemCd(), company2.getSourceCompanyId(), c2dl.getCompany1BankAccount(), true, true));
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload only QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // add one more payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
        PayrollServices.commitUnitOfWork();

        // offload Company1 2nd payroll EE DD CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        missedTxProcessor.process("20071005");
        String notificationMessage = missedTxProcessor.getNotificationMessage();
        String errorMessage = missedTxProcessor.getErrorMessage();
        PayrollServices.commitUnitOfWork();

        // verify the txns are cancelled
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        company2 = Company.findCompany(c2dl.getCompany1().getCompanyId(),
                                         SourceSystemCode.valueOf(c2dl.getCompany1().getSourceSystemCd().toString()));
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 1);
        DomainEntitySet<FinancialTransaction> payroll2FinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll2FinTxns.size(), 1);

        DomainEntitySet<FinancialTransaction> c2PayrollFinTxns =
                FinancialTransaction.findFinancialTransactions(company2.getSourceSystemCd(),
                        company2.getSourceCompanyId(), TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Cancelled);
        SpcfMoney amount1 = c2PayrollFinTxns.get(0).getFinancialTransactionAmount();
        SpcfMoney amount2 = c2PayrollFinTxns.get(1).getFinancialTransactionAmount();
        if (amount1.compareTo(amount2) > 0) {
            amount1 = c2PayrollFinTxns.get(1).getFinancialTransactionAmount();
            amount2 = c2PayrollFinTxns.get(0).getFinancialTransactionAmount();
        }
        assertEquals("Number Of cancelled Transactions for Payroll1", c2PayrollFinTxns.size(), 2);

        // verify no transaction responses are created
        DomainEntitySet<TransactionResponse> txResponses1 =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 1, txResponses1.size());

        DomainEntitySet<TransactionResponse> txResponses2 = TransactionResponse.findTransactionResponses(payroll2FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 1, txResponses2.size());
        // verify the two transaction respones are the same
        assertEquals("Transaction Responses", txResponses1.get(0), txResponses2.get(0));

        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(c2PayrollFinTxns.get(0));
        assertEquals("Number of Transaction Responses", 0, txResponses.size());

        PayrollServices.commitUnitOfWork();

        StringBuilder message = new StringBuilder();

        message.append("Company 1\n")
               .append("   Source System Code: QBOE\n")
               .append("   Source Company ID:  2222222\n")
               .append("   Company Legal Name: Dawn Company 2\n")
               .append("   DD Service Status:  PendingBankVerification\n")
               .append("   Non-Payroll Transactions \n")
               .append("      Tx 1\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerVerificationDebit\n")
               .append("         Initiation Date: 2007/09/14 00:00:00.0\n")
               .append("         Settlement Date: 2007/09/17 00:00:00.0\n")
               .append("         Txn Amount:      ").append(String.format("$%,.2f", SpcfUtils.convertToBigDecimal(amount1))).append("\n")
               .append("      Tx 2\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerVerificationDebit\n")
               .append("         Initiation Date: 2007/09/14 00:00:00.0\n")
               .append("         Settlement Date: 2007/09/17 00:00:00.0\n")
               .append("         Txn Amount:      ").append(String.format("$%,.2f", SpcfUtils.convertToBigDecimal(amount2))).append("\n")
               .append("Company 2\n")
               .append("   Source System Code: QBOE\n")
               .append("   Source Company ID:  1234567\n")
               .append("   Company Legal Name: Intuit\n")
               .append("   DD Service Status:  ActiveCurrent\n")
               .append("   Payroll 1\n")
               .append("      Src Payroll Run ID: BatchTest002\n")
               .append("      Created Date:       ").append(payRun2C1.getCreatedDate().toLocal().toString()).append("\n")
               .append("      Paycheck Date:      2007/10/10 00:00:00.0\n")
               .append("      Net Payroll Amount: $250.00\n")
               .append("      Tx 1\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerFeeDebit\n")
               .append("         Initiation Date: 2007/10/02 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/03 00:00:00.0\n")
               .append("         Txn Amount:      $0.00\n")
               .append("      Tx 2\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerDdDebit\n")
               .append("         Initiation Date: 2007/10/02 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/03 00:00:00.0\n")
               .append("         Txn Amount:      $250.00\n")
               .append("   Payroll 2\n")
               .append("      Src Payroll Run ID: BatchTest05\n")
               .append("      Created Date:       ").append(payRun1C1.getCreatedDate().toLocal().toString()).append("\n")
               .append("      Paycheck Date:      2007/10/02 00:00:00.0\n")
               .append("      Net Payroll Amount: $180.00\n")
               .append("      Tx 1\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerFeeDebit\n")
               .append("         Initiation Date: 2007/09/25 00:00:00.0\n")
               .append("         Settlement Date: 2007/09/26 00:00:00.0\n")
               .append("         Txn Amount:      $0.00\n")
               .append("      Tx 2\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerDdDebit\n")
               .append("         Initiation Date: 2007/09/25 00:00:00.0\n")
               .append("         Settlement Date: 2007/09/26 00:00:00.0\n")
               .append("         Txn Amount:      $180.00\n");


        // verify the notification message
        assertEquals("Error Message ", message.toString().trim(), errorMessage.trim());
        assertNull(notificationMessage);
    }

    /**
     * This test method verifies a single transaction response is created per company for all cancelled
     * finanical transactions belonging to that company
     */
    @Test
    public void testMissedACHTxnsForMultipleCompanies_TxnResponsePerCompany() {
        loadData();

        // add company2 and payroll
        PayrollServices.beginUnitOfWork();
        persistCompany2();
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload only QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // add one more payroll to company1
        PayrollServices.beginUnitOfWork();
        addCompany1Payroll2();
        PayrollServices.commitUnitOfWork();

        // offload Company1 2nd payroll EE DD CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071005000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        missedTxProcessor.process("20071005");
        PayrollServices.commitUnitOfWork();

        // verify the txns are cancelled
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        Company company2 = Company.findCompany(c2dl.getCompany1().getCompanyId(),
                                         SourceSystemCode.valueOf(c2dl.getCompany1().getSourceSystemCd().toString()));
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");
        PayrollRun payRun2C1 = PayrollRun.findPayrollRun(company1, "BatchTest002");
//        PayrollRun payRun1C2 = PayrollRun.findPayrollRun(company2, "BatchTest05");

        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 1);
        DomainEntitySet<FinancialTransaction> payroll2FinTxns = payRun2C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll2FinTxns.size(), 1);

        DomainEntitySet<FinancialTransaction> c2PayrollFinTxns =
                FinancialTransaction.findFinancialTransactions(company2.getSourceSystemCd(),
                        company2.getSourceCompanyId(), TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);

        assertEquals("Number Of cancelled Transactions for Company2 Payroll1", c2PayrollFinTxns.size(), 1);

        // verify no transaction responses are created
        DomainEntitySet<TransactionResponse> txResponses1 =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 1, txResponses1.size());

        DomainEntitySet<TransactionResponse> txResponses2 = TransactionResponse.findTransactionResponses(payroll2FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 1, txResponses2.size());
        // verify the two transaction respones are the same
        assertEquals("Transaction Responses", txResponses1.get(0), txResponses2.get(0));

        DomainEntitySet<TransactionResponse> txResponses = TransactionResponse.findTransactionResponses(c2PayrollFinTxns.get(0));
        assertEquals("Number of Transaction Responses", 1, txResponses.size());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testRefundAndOtherTxnTypesForOnHoldCompany() {
        //Return the EE txn to get a refund going
        ACHReturnsDataLoader.loadData2Day1EERet();

        Application.beginUnitOfWork();

        PSPDate.setPSPTime("20070910000000");
        PayrollRunDTO payrollRunDTO = ACHReturnsDataLoader.c1dl.get3rdCompany1PR_DoesNotExceedLimits(new DateDTO("2007-09-12"));
        // We have to update something in the account to get by the Return Handling.
        updateReturnedBankAccountNumber(payrollRunDTO, "34509343458937");
        ProcessResult<PayrollRun> submitPayrollResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServicesTest.assertSuccess("submit payroll", submitPayrollResult);
        PayrollRun batch81Payroll = submitPayrollResult.getResult();
        String batch81CreationDate = batch81Payroll.getCreatedDate().toLocal().toString();
        assertSuccess(submitPayrollResult);
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        //Now return the debit from another payroll
        Application.beginUnitOfWork();

        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        PayrollRun batch05Payroll = PayrollRun.findPayrollRun(company, "BatchTest05");
        String batch05CreationDate = batch05Payroll.getCreatedDate().toLocal().toString();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction.findFinancialTransactions(
                company, "BatchTest81", null, null, null, TransactionTypeCode.EmployerDdDebit, null, null, TransactionStateCode.Executed);

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns, "R01",
                "This is a non-NSF description");
        Application.commitUnitOfWork();

        assertEquals("Number of C1 EmployerDdDebit txns", 1, c1FinTxns.size());
        assertEquals("Number of returns", 1, returnList.size());

        //Execute the return handlers
        for (TransactionReturn currRet : returnList) {
            Application.beginUnitOfWork();
            TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(currRet);
            returnHandler.execute(currRet);
            Application.commitUnitOfWork();
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071010000000");
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        String notificationMessage = missedTxProcessor.process("20071010");
        String errorMessage = missedTxProcessor.getErrorMessage();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071011000000");
        ProcessMissedACHTransactions missedTxProcessor2 = new ProcessMissedACHTransactions();
        String notificationMessage2 = missedTxProcessor2.process("20071011");
        String errorMessage2 = missedTxProcessor2.getErrorMessage();
        PayrollServices.commitUnitOfWork();

        String message = "Company 1\n" +
                "   Source System Code: QBOE\n" +
                "   Source Company ID:  1234567\n" +
                "   Company Legal Name: Intuit\n" +
                "   DD Service Status:  (ACH Reject R01-R09)\n" +
                "   Payroll 1\n" +
                "      Src Payroll Run ID: BatchTest81\n" +
                "      Created Date:       "+batch81CreationDate+"\n" +
                "      Paycheck Date:      2007/09/12 00:00:00.0\n" +
                "      Net Payroll Amount: $1,108.00\n" +
                "      Tx 1\n" +
                "         Src DD Txn ID:   N/A\n" +
                "         Txn Type Code:   EmployerFeeRedebit\n" +
                "         Initiation Date: 2007/09/11 00:00:00.0\n" +
                "         Settlement Date: 2007/09/12 00:00:00.0\n" +
                "         Txn Amount:      $0.00\n" +
                "      Tx 2\n" +
                "         Src DD Txn ID:   N/A\n" +
                "         Txn Type Code:   EmployerFeeDebit\n" +
                "         Initiation Date: 2007/09/11 00:00:00.0\n" +
                "         Settlement Date: 2007/09/12 00:00:00.0\n" +
                "         Txn Amount:      $100.00\n" +
                "      Tx 3\n" +
                "         Src DD Txn ID:   N/A\n" +
                "         Txn Type Code:   EmployerDdRedebit\n" +
                "         Initiation Date: 2007/09/11 00:00:00.0\n" +
                "         Settlement Date: 2007/09/12 00:00:00.0\n" +
                "         Txn Amount:      $1,108.00";

        String expectedNotificationMessage = "Company 1\n" +
                "   Source System Code: QBOE\n" +
                "   Source Company ID:  1234567\n" +
                "   Company Legal Name: Intuit\n" +
                "   DD Service Status:  (ACH Reject R01-R09)\n" +
                "   Payroll 1\n" +
                "      Src Payroll Run ID: BatchTest05\n" +
                "      Created Date:       "+batch05CreationDate+"\n" +
                "      Paycheck Date:      2007/09/11 00:00:00.0\n" +
                "      Net Payroll Amount: $180.00\n" +
                "      Tx 1\n" +
                "         Src DD Txn ID:   N/A\n" +
                "         Txn Type Code:   EmployerDdRejectRefundCredit\n" +
                "         Initiation Date: 2007/09/14 00:00:00.0\n" +
                "         Settlement Date: 2007/09/17 00:00:00.0\n" +
                "         Txn Amount:      $30.00";

        // verify the notification message
        assertEquals("Error Message ", message, errorMessage.trim());
        assertEquals("Notification Message ", expectedNotificationMessage, notificationMessage.trim());

        assertNull("Notification Message 2: ",notificationMessage2);
        assertNull("Error Message 2: ",errorMessage2);
    }

    @Test
    public void testProcessMissedPayrollTxs_MultipleTxnsPerPayroll() {
        loadData();
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload only QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload only QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // create an ER return for company1 payroll DB tx
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] {TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns,
                                                                                          "R01",
                                                                                          "This is an ER Return");

        assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        assertEquals("Number of Company1 ERDDDB Returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.financialTransactionManager.addEscalation(SourceSystemCode.QBOE, "1234567", "BatchTest05", false, SettlementType.Wire, SpcfUtils.convertToBigDecimal(new SpcfMoney("50.00")), new DateDTO(PSPDate.getPSPTime()));
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        missedTxProcessor.process("20071003");
        String notificationMessage = missedTxProcessor.getNotificationMessage();
        String errorMessage = missedTxProcessor.getErrorMessage();
        PayrollServices.commitUnitOfWork();

        // verify the ER Redebit and Fee Debit txns are cancelled
        PayrollServices.beginUnitOfWork();

        payRun1C1 = PayrollServices.entityFinder.findById(PayrollRun.class, payRun1C1.getId());
        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit,
                                   TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 2);

        // verify a single common transaction response is created for both the cancelled txns
        DomainEntitySet<TransactionResponse> txResponses1 =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 2, txResponses1.size());

        DomainEntitySet<TransactionResponse> txResponses2 =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(1));
        assertEquals("Number of Transaction Responses", 2, txResponses2.size());
        TransactionResponse txnResponse1 = getTxnResponseWithMaxToken(txResponses1);
        TransactionResponse txnResponse2 = getTxnResponseWithMaxToken(txResponses2);
        // verify both txn responses are same
        assertEquals("Transaction Responses", txnResponse1, txnResponse2);
        PayrollServices.commitUnitOfWork();

        StringBuilder expectedErrorMessage = new StringBuilder();

        expectedErrorMessage.append("Company 1\n")
               .append("   Source System Code: QBOE\n")
               .append("   Source Company ID:  1234567\n")
               .append("   Company Legal Name: Intuit\n")
               .append("   DD Service Status:  (ACH Reject R01-R09)\n")
               .append("   Payroll 1\n")
               .append("      Src Payroll Run ID: BatchTest05\n")
               .append("      Created Date:       " + payRun1C1.getCreatedDate().toLocal().toString() + "\n")
               .append("      Paycheck Date:      2007/10/02 00:00:00.0\n")
               .append("      Net Payroll Amount: $180.00\n")
               .append("      Tx 1\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerFeeRedebit\n")
               .append("         Initiation Date: 2007/10/01 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/02 00:00:00.0\n")
               .append("         Txn Amount:      $0.00\n")
               .append("      Tx 2\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerFeeDebit\n")
               .append("         Initiation Date: 2007/10/01 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/02 00:00:00.0\n")
               .append("         Txn Amount:      $100.00\n")
               .append("      Tx 3\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerDdRedebit\n")
               .append("         Initiation Date: 2007/10/01 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/02 00:00:00.0\n")
               .append("         Txn Amount:      $180.00");

        // verify the notification message
        assertEquals("Error Message ", expectedErrorMessage.toString().trim(), errorMessage.trim());
        assertNull("Notification Message should be null", notificationMessage);
    }

    @Test
    public void testProcessMissedPayrollTxs_VerifyTxResponse() {
        loadData();
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload only QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070925000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload only QBOE EE CR
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        // create an ER return for company1 payroll DB tx
        PayrollServices.beginUnitOfWork();
        Company company1 = Company.findCompany(c1dl.getCompany().getSourceCompanyId(),
                                                                     c1dl.getCompany().getSourceSystemCd());
        PayrollRun payRun1C1 = PayrollRun.findPayrollRun(company1, "BatchTest05");

        DomainEntitySet<FinancialTransaction> c1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit},
                        new TransactionStateCode[] {TransactionStateCode.Executed});
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.persistTransactionReturns(c1FinTxns,
                                                                                          "R01",
                                                                                          "This is an ER Return");

        assertEquals("Number of txn returns", 1, returnList.size());

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of Company1 ERDDDB EX txns", 1, c1FinTxns.size());
        assertEquals("Number of Company1 ERDDDB Returns", 1, returnList.size());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071003000000");
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        missedTxProcessor.process("20071003");
        String errorMessage = missedTxProcessor.getErrorMessage();
        String notificationMessage = missedTxProcessor.getNotificationMessage();
        PayrollServices.commitUnitOfWork();

        // verify the ER Redebit and Fee Debit txns are cancelled
        PayrollServices.beginUnitOfWork();

        payRun1C1 = PayrollServices.entityFinder.findById(PayrollRun.class, payRun1C1.getId());
        DomainEntitySet<FinancialTransaction> payroll1FinTxns = payRun1C1.getFinancialTransactions(
                new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployeeDdCredit,
                                   TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[] {TransactionStateCode.Cancelled});
        assertEquals("Number Of cancelled Transactions for Payroll1", payroll1FinTxns.size(), 2);

        // verify a single common transaction response is created for both the cancelled txns
        DomainEntitySet<TransactionResponse> txResponses1 =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(0));
        assertEquals("Number of Transaction Responses", 2, txResponses1.size());

        DomainEntitySet<TransactionResponse> txResponses2 =
                        TransactionResponse.findTransactionResponses(payroll1FinTxns.get(1));
        assertEquals("Number of Transaction Responses", 2, txResponses2.size());
        TransactionResponse txnResponse1 = getTxnResponseWithMaxToken(txResponses1);
        TransactionResponse txnResponse2 = getTxnResponseWithMaxToken(txResponses2);
        // verify both txn responses are same
        assertEquals("Transaction Responses", txnResponse1, txnResponse2);
        PayrollServices.commitUnitOfWork();

        StringBuilder message = new StringBuilder();

        message.append("Company 1\n")
               .append("   Source System Code: QBOE\n")
               .append("   Source Company ID:  1234567\n")
               .append("   Company Legal Name: Intuit\n")
               .append("   DD Service Status:  (ACH Reject R01-R09)\n")
               .append("   Payroll 1\n")
               .append("      Src Payroll Run ID: BatchTest05\n")
               .append("      Created Date:       ").append(payRun1C1.getCreatedDate().toLocal().toString()).append("\n")
               .append("      Paycheck Date:      2007/10/02 00:00:00.0\n")
               .append("      Net Payroll Amount: $180.00\n")
               .append("      Tx 1\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerFeeRedebit\n")
               .append("         Initiation Date: 2007/10/01 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/02 00:00:00.0\n")
               .append("         Txn Amount:      $0.00\n")
               .append("      Tx 2\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerFeeDebit\n")
               .append("         Initiation Date: 2007/10/01 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/02 00:00:00.0\n")
               .append("         Txn Amount:      $100.00\n")
               .append("      Tx 3\n")
               .append("         Src DD Txn ID:   N/A\n")
               .append("         Txn Type Code:   EmployerDdRedebit\n")
               .append("         Initiation Date: 2007/10/01 00:00:00.0\n")
               .append("         Settlement Date: 2007/10/02 00:00:00.0\n")
               .append("         Txn Amount:      $180.00");

        // verify the notification message
        assertEquals("Error Message ", message.toString().trim(), errorMessage.trim());
        assertNull("Notification Message",notificationMessage);
    }

    /**
     * PSP-11809: Test to verify whether EmployerFeeDebit and ServiceSalesAndUseTax transactions
     * get offloaded if they get created after company moves onHold due to returns.
     */
    @Test
    public void testProcessMissedACHTransactionsForNSFFees() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,05, 16,14,00,0,0, SpcfTimeZone.getLocalTimeZone()));
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "198210096", "987656321", false, ServiceCode.Tax);;
        DataLoadServices.activateDDService(company);
        DataLoadServices.addBillPaymentService(company);
        DataLoadServices.addPayees(company, 1);

        PayrollServices.beginUnitOfWork();
        PayeeDTO pdto=new PayeeDTO();
        pdto.setTaxId("20-8465914");
        pdto.setSourcePayeeId("4bd99312-a862-48e2-958a-d9f46725dd73");
        pdto.setName("Tax and Accounting Group");
        pdto.setIs1099(true);
        pdto.setEmail("julio@tagcp.com");
        AddressDTO addr=new AddressDTO();
        addr.setAddressLine1("21300 Sherman Way, Unit 13");
        addr.setCity("Canoga Park");
        addr.setState("CA");
        addr.setZipCode("91303");
        pdto.setMailingAddress(addr);
        assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), pdto));
        PayeeBankAccountDTO pbaDTO = new PayeeBankAccountDTO();
        pbaDTO.setPayeeBankAccountId("d583ac12-afa7-4aa8-b8f2-78cf43597322");
        BankAccountDTO bdto=new BankAccountDTO();
        bdto.setAccountNumber("0976773118");
        bdto.setAccountType(BankAccountType.Checking);
        bdto.setBankName("Bank of America");
        bdto.setRoutingNumber("122000661");
        pbaDTO.setBankAccount(bdto);
        assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), pdto.getSourcePayeeId(), pbaDTO));
        PayrollServices.commitUnitOfWork();

        // submit BP
        PayrollServices.beginUnitOfWork();
        Payee p = Payee.findPayee(company,pdto.getSourcePayeeId());

        List<Payee> ps = new ArrayList<Payee>();
        p = Application.findById(Payee.class, p.getId());
        ps.add(p);
        BillPaymentDTO billPaymentDTO = new BillPaymentDTO();
        billPaymentDTO.setBillPaymentId("de9e82ca-26eb-481e-a81b-1c7575b61360");
        billPaymentDTO.setTransactionType(BillPaymentTransactionType.PayBills);
        billPaymentDTO.setMemo("PR 5/21/2016");
        billPaymentDTO.setPayeeDTO(pdto);
        BillPaymentSplitDTO splitDto=new BillPaymentSplitDTO();
        splitDto.setAmount(new BigDecimal(70.0));
        splitDto.setBillPaymentSplitId("e338672f-20cb-4589-9709-0f938c53af24");
        splitDto.setPayeeBankAccount(pbaDTO);
        Collection<BillPaymentSplitDTO> billPaymentSplits = new ArrayList<BillPaymentSplitDTO>();
        billPaymentSplits.add(splitDto);
        billPaymentDTO.setPaymentTransactions(billPaymentSplits);
        billPaymentDTO.setDepositDate(new DateDTO("2016-05-20T00:00:00-07:00"));
        SpcfMoney amount = (new SpcfMoney("0.0"));
        for (BillPaymentSplitDTO bps:billPaymentSplits) {
            amount = new SpcfMoney(amount.add(new SpcfMoney(bps.getAmount().toString())));
        }
        billPaymentDTO.setAmount(amount);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        ProcessResult<Collection<PayrollRun>> processResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        assertSuccess("submit BP payroll", processResult);
        PayrollRun payrollRun = processResult.getResult().iterator().next();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        BillPaymentDTO billPaymentDTO1 = new BillPaymentDTO();
        billPaymentDTO1.setBillPaymentId("de9e82ca-26eb-481e-a81b-1c7375b61360");
        billPaymentDTO1.setTransactionType(BillPaymentTransactionType.PayBills);
        billPaymentDTO1.setMemo("PR 5/21/2016");
        billPaymentDTO1.setPayeeDTO(pdto);
        BillPaymentSplitDTO splitDto1=new BillPaymentSplitDTO();
        splitDto1.setAmount(new BigDecimal(170.0));
        splitDto1.setBillPaymentSplitId("e339672f-20cb-4589-9709-0f938c53af24");
        splitDto1.setPayeeBankAccount(pbaDTO);
        Collection<BillPaymentSplitDTO> billPaymentSplits1 = new ArrayList<BillPaymentSplitDTO>();
        billPaymentSplits1.add(splitDto1);
        billPaymentDTO1.setPaymentTransactions(billPaymentSplits1);
        billPaymentDTO1.setDepositDate(new DateDTO("2016-05-20T01:00:00-07:00"));
        SpcfMoney amount1 = (new SpcfMoney("0.0"));
        for (BillPaymentSplitDTO bps:billPaymentSplits1) {
            amount1 = new SpcfMoney(amount1.add(new SpcfMoney(bps.getAmount().toString())));

        }
        billPaymentDTO1.setAmount(amount1);
        Collection<BillPaymentDTO> billPaymentDTOs1 = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs1.add(billPaymentDTO1);
        ProcessResult<Collection<PayrollRun>> processResult1 = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs1);
        assertSuccess("submit BP payroll", processResult1);
        PayrollRun payrollRun1 = processResult1.getResult().iterator().next();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20160518003500");
        PayrollServices.commitUnitOfWork();
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(2016, 05, 18, SpcfTimeZone.getLocalTimeZone());
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> c1FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                        payrollRun.getSourcePayRunId(), null, null, null,TransactionTypeCode.EmployerDdDebit,
                        null, null, TransactionStateCode.Executed);

        assertEquals("Number of txns", 1, c1FinTxns.size());
        DomainEntitySet<FinancialTransaction> c2FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun.getCompany(),
                        payrollRun.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerFeeDebit,
                        null, null, TransactionStateCode.Executed);

        assertEquals("Number of txns", 1, c2FinTxns.size());
        c1FinTxns.addAll(c2FinTxns);
        Application.commitUnitOfWork();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);
        DataLoadServices.returnTxns(c1FinTxns, "R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20160518210000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns1 =
                FinancialTransaction.findFinancialTransactions(payrollRun1.getCompany(),
                        payrollRun1.getSourcePayRunId(), null, null, null,TransactionTypeCode.EmployerDdDebit,
                        null, null, TransactionStateCode.Executed);

        assertEquals("Number of txns", 1, c1FinTxns1.size());
        DomainEntitySet<FinancialTransaction> c2FinTxns1 =
                FinancialTransaction.findFinancialTransactions(payrollRun1.getCompany(),
                        payrollRun1.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerFeeDebit,
                        null, null, TransactionStateCode.Executed);

        assertEquals("Number of txns", 1, c2FinTxns1.size());
        c1FinTxns1.addAll(c2FinTxns1);
        Application.commitUnitOfWork();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AchReturnsBatchJob);
        DataLoadServices.returnTxns(c1FinTxns1, "R01", "This is an NSF description");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20160518000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(1);
        PayrollServices.commitUnitOfWork();

        offloadDate = SpcfCalendar.createInstance(2016, 05, 19, SpcfTimeZone.getLocalTimeZone());

        PayrollServices.setCurrentPrincipal(SystemPrincipal.AchOffloadBatchJob);


        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,05, 20,00,00,0,0, SpcfTimeZone.getLocalTimeZone()));

        //Fetch the Set of EmployerFeeDebit transactions for the second payroll in Executed state
        DomainEntitySet<FinancialTransaction> feeTxnsSet =
                FinancialTransaction.findFinancialTransactions(payrollRun1.getCompany(),
                        payrollRun1.getSourcePayRunId(), null, null, null, TransactionTypeCode.EmployerFeeDebit,
                        null, null, TransactionStateCode.Executed);
        //Number of EmployerFeeDebit Transactions with offering service charge type as DebitReturnFee which moved to Executed state after Company went onHold
        assertEquals("Number of Employer Fee Debit txns", 1, feeTxnsSet.size());

        //Fetch the Set of ServiceSalesAndUseTax transactions for the second payroll in Executed state
        DomainEntitySet<FinancialTransaction> taxTxnsSet =
                FinancialTransaction.findFinancialTransactions(payrollRun1.getCompany(),
                        payrollRun1.getSourcePayRunId(), null, null, null, TransactionTypeCode.ServiceSalesAndUseTax,
                        null, null, TransactionStateCode.Executed);

        //Number of ServiceSalesAndUseTax Transactions with offering service charge type as DebitReturnFee which moved to Executed state after Company went onHold
        assertEquals("Number of ServiceSalesAndUseTax txns", 1, taxTxnsSet.size());
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,05, 21,00,00,0,0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        ProcessMissedACHTransactions missedTxProcessor = new ProcessMissedACHTransactions();
        missedTxProcessor.process("20160521");
        PayrollServices.commitUnitOfWork();

        // verify the ER Redebit and Fee Debit txns are cancelled
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> cancelledFinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRun1, new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.ServiceSalesAndUseTax,
                                TransactionTypeCode.EmployerFeeRedebit,TransactionTypeCode.ServiceSalesAndUseTaxRedebit,
                                TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerFeeDebit},
                        new TransactionStateCode[] {TransactionStateCode.Cancelled});

        assertEquals("Number Of cancelled Transactions for Payroll1", cancelledFinTxns.size(), 0);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Methods to load data
     */
    private static void loadDataHappyPath() {
        PSPDate.setPSPTime("20070904000000");
        persistCompany1();
    }

    private static void persistCompany1() {
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
    }

    private static void addCompany1Payroll2() {
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR2_DoesNotExceedLimits(new DateDTO("2007-10-10"));
        Company1Dataloader.persistPayrollRun(payrollRunDTO);
    }

    private static void persistCompany2() {
        c2dl.persistCompany2();
        PayrollRunDTO payrollRunDTO = c2dl.getCompany2PR_DoesNotExceedLimits(new DateDTO("2007-10-02"));
        c2dl.persistPayrollRun(payrollRunDTO);
    }

    private TransactionResponse getTxnResponseWithMaxToken(DomainEntitySet<TransactionResponse> txnResponses) {
        TransactionResponse txnResponseWithMaxToken = new TransactionResponse();
        txnResponseWithMaxToken.setTransactionTokenNumber(0L);
        for(TransactionResponse transactionResponse:txnResponses) {
            if (transactionResponse.getTransactionTokenNumber() > txnResponseWithMaxToken.getTransactionTokenNumber()) {
                txnResponseWithMaxToken = transactionResponse;
            }
        }
        return txnResponseWithMaxToken;
    }

    private void updateReturnedBankAccountNumber(PayrollRunDTO payrollRun, String bankAccountNumber) {

        // Find the EE bank account that was returned.
        DomainEntitySet<FinancialTransaction> txs = Application.find(FinancialTransaction.class,
                                 FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployeeDdCredit)
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Returned)));
        BankAccount returnedBankAccount = assertOne(txs).getCreditBankAccount();

        // Look through the payroll run for the returned account and update the account number if found.
        for (PaycheckDTO check : payrollRun.getPaychecks()) {
            for (DDTransactionDTO ddTrans : check.getDdTransactions()) {
                BankAccountDTO baDTO = ddTrans.getEmployeeBankAccount().getBankAccount();
                if (returnedBankAccount.getAccountNumber().equals(baDTO.getAccountNumber())) {
                    baDTO.setAccountNumber(bankAccountNumber);
                }
            }
        }
    }
}
