package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: May 9, 2008
 * Time: 2:02:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class DirectDepositRejectReturnTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testDirectDepositReturnForActiveCompany() {
        ACHReturnsDataLoader.loadData2Day1EERet();

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany("1234567", SourceSystemCode.QBOE);
        Employee employee1 = Employee.findEmployee(company, "EE1");
        DomainEntitySet<FinancialTransaction> finTxnList = FinancialTransaction
                .findEmployeeFinancialTransactions(company, "BatchTest05", employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        DomainEntitySet<TransactionReturn> txnRetList = TransactionReturn.findTransactionReturns("BatchTest05", company);

        TransactionReturn txnRet = txnRetList.get(0);

        FinancialTransaction finTxn = finTxnList.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        DomainEntitySet<TransactionResponse> txnResponseList;

        for (FinancialTransaction financialTransaction : finTxnList) {
            //Assertion for Update FinancialTransaction Status Rule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            //verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 1, ddRejectRefundFinTxns.size());
        assertEquals("Payroll Run ", finTxn.getPayrollRun().getSourcePayRunId(),
                ddRejectRefundFinTxns.get(0).getPayrollRun().getSourcePayRunId());

        CompanyBankAccount companyBankAccount = employerDdDebitFinTxns.get(0).getCompanyBankAccount(
        );

        assertEquals("Company Bank Account ", companyBankAccount.getBankAccount(),
                ddRejectRefundFinTxns.get(0).getCreditBankAccount());

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.Issued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", null,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }
        //Assertion for Update TransactionReturn Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                txnRet.getReturnStatusCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testDirectDepositReturnForActiveCompany_CBAChanged() {
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.loadData2DayBothEERetDoNotHandle();

        assertEquals("Number of txn returns", 2, returnList.size());

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBOE,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        DomainEntitySet<TransactionResponse> txnResponseList;

        for (FinancialTransaction financialTransaction : finTxnList) {
            //Assertion for Update FinancialTransaction Status Rule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            //verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 1, ddRejectRefundFinTxns.size());
        assertEquals("Payroll Run ", finTxn.getPayrollRun().getSourcePayRunId(),
                ddRejectRefundFinTxns.get(0).getPayrollRun().getSourcePayRunId());
        // verify the created FeeDebit transaction is associated with the new bank account
        assertTrue("Refund Transaction bank account",
                changedCompanyBankAccount.getBankAccount().equals(ddRejectRefundFinTxns.get(0).getCreditBankAccount()));

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.Issued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", null,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }
        //Assertion for Update TransactionReturn Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testDirectDepositReturn_CBAChangedBeforeOffload() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader;
        ACHReturnsDataLoader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBOE,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        // verify financial transactions are moved to new ba
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        assertEquals("Number of employer transacions", 1, c1FinTxns.size());
        assertEquals("Debit bank account", changedCompanyBankAccount.getBankAccount(), c1FinTxns.get(0).getDebitBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.createDefualtRejectReturnEvent();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 2, returnList.size());


        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        DomainEntitySet<TransactionResponse> txnResponseList;

        for (FinancialTransaction financialTransaction : finTxnList) {
            //Assertion for Update FinancialTransaction Status Rule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            //verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 1, ddRejectRefundFinTxns.size());
        assertEquals("Payroll Run ", finTxn.getPayrollRun().getSourcePayRunId(),
                ddRejectRefundFinTxns.get(0).getPayrollRun().getSourcePayRunId());
        // verify the created FeeDebit transaction is associated with the new bank account
        assertTrue("Refund Transaction bank account",
                changedCompanyBankAccount.getBankAccount().equals(ddRejectRefundFinTxns.get(0).getCreditBankAccount()));

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.Issued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", null,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }
        //Assertion for Update TransactionReturn Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testNSFDirectDepositReturn_CBAChangedBeforeOffload() {
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader;
        ACHReturnsDataLoader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBOE,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        // verify financial transactions are moved to new ba
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBOE, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        assertEquals("Number of employer transacions", 1, c1FinTxns.size());
        assertEquals("Debit bank account", changedCompanyBankAccount.getBankAccount(), c1FinTxns.get(0).getDebitBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.createDDRejectReturnNSF();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 2, returnList.size());


        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        DomainEntitySet<TransactionResponse> txnResponseList;

        for (FinancialTransaction financialTransaction : finTxnList) {
            //Assertion for Update FinancialTransaction Status Rule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            //verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 1, ddRejectRefundFinTxns.size());
        assertEquals("Payroll Run ", finTxn.getPayrollRun().getSourcePayRunId(),
                ddRejectRefundFinTxns.get(0).getPayrollRun().getSourcePayRunId());
        // verify the created FeeDebit transaction is associated with the new bank account
        assertTrue("Refund Transaction bank account",
                changedCompanyBankAccount.getBankAccount().equals(ddRejectRefundFinTxns.get(0).getCreditBankAccount()));

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.Issued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", null,
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }
        //Assertion for Update TransactionReturn Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                transactionReturn.getReturnStatusCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testDirectDepositReturn_CBADeactivated() {
        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.loadData2DayBothEERetDoNotHandle();

        assertEquals("Number of txn returns", 2, returnList.size());

        // deactivate company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.deactivateCompanyBankAccount(
                        SourceSystemCode.QBOE,
                        c1dl.getCompany1().getCompanyId(),
                        dataloader.getTestCompanyBankAccount().getCompanyBankAccountID(), true, false);
        assertTrue("Deactivate company bank account", result.isSuccess());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        DomainEntitySet<TransactionResponse> txnResponseList;

        for (FinancialTransaction financialTransaction : finTxnList) {
            //Assertion for Update FinancialTransaction Status Rule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            //verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 0, ddRejectRefundFinTxns.size());

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.NotIssued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", EnumUtils.getReadableName(RefundStatusReasonType.BankAccountInactive),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));

            assertEquals("EE Txn Id", finTxn.getId().toString(),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.FinancialTransactionId));
        }
        //Assertion to ensure transaction return was not resolved
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open, transactionReturn.getReturnStatusCd());

        Application.commitUnitOfWork();
    }

    @Test
    public void testDirectDepositReturnForOnHoldCompany() {
        //Load payroll data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPath();
        PayrollServices.commitUnitOfWork();

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBOE,
                "1234567",
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        System.out.println("Result " + result.getMessages());

        //Add TransactionReturn for Employee Transaction
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDirectDepositReturnOnHoldCompany();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 2, returnList.size());

        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        DomainEntitySet<TransactionResponse> txnResponseList;

        for (FinancialTransaction financialTransaction : finTxnList) {
            //Assertion for Update FinancialTransaction Status Rule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            //verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 0, ddRejectRefundFinTxns.size());

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.NotIssued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", EnumUtils.getReadableName(RefundStatusReasonType.CompanyOnHold),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testBPReturnForOnHoldCompany() {
        //Load payroll data

        ACHReturnsDataLoader.loadPayrollRunForBPACHReturnTest2();


        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        String sourceCompanyId = "123272727";
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                sourceCompanyId,
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        System.out.println("Result " + result.getMessages());

        //Add TransactionReturn for Employee Transaction
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForBPReturnOnHoldCompany();
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 4, returnList.size());

        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        DomainEntitySet<FinancialTransaction> ddRejectRefundFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdRejectRefundCredit,
                        TransactionStateCode.Created);

        DomainEntitySet<FinancialTransaction> employerDdDebitFinTxns = FinancialTransaction.
                findFinancialTransactions(finTxn.getCompany().getSourceSystemCd(), finTxn.getCompany().getSourceCompanyId(),
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionStateCode.Executed);

        DomainEntitySet<TransactionResponse> txnResponseList;

        for (FinancialTransaction financialTransaction : finTxnList) {
            //Assertion for Update FinancialTransaction Status Rule
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                    financialTransaction.getCurrentTransactionState().getTransactionStateCd());

            //verify the transaction response was created correctly for this txn
            txnResponseList = TransactionResponse.findTransactionResponses(financialTransaction);
            assertEquals("Transaction response for Returned EmployeeDdCredit transaction", 1, txnResponseList.size());
        }

        //Assertion for Create Direct Deposit Refund Transaction Rule
        assertEquals("Financial Transactions ", 0, ddRejectRefundFinTxns.size());

        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.DDReject, CompanyEventStatus.Active, null, null);

        //Assertion for DDReject System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());

        for (CompanyEvent companyEvent : companyEventsList) {
            assertEquals("ACH Return Type", EnumUtils.getReadableName(ACHReturnType.DDReject),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ReturnType));

            assertEquals("Refund Status ", EnumUtils.getReadableName(RefundStatusType.NotIssued),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatus));

            assertEquals("Refund Status Reason ", EnumUtils.getReadableName(RefundStatusReasonType.CompanyOnHold),
                    companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.RefundStatusReason));
        }

        Application.commitUnitOfWork();
    }


    @Test
    public void testQBDTSymphonyDirectDepositReturnForActiveCompany() {
        String sourceCompanyId = "1234567";
        String sourcePayrollRunId = "BatchTest05";

        ACHReturnsDataLoader.loadData2Day1EERet(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));

        // Validation

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        Employee employee1 = Employee.findEmployee(company, "EE1");

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = FinancialTransaction
                .findEmployeeFinancialTransactions(company, sourcePayrollRunId, employee1, null,
                        TransactionTypeCode.EmployeeDdCredit, null, null);

        DomainEntitySet<TransactionReturn> transactionReturns   = TransactionReturn.findTransactionReturns(sourcePayrollRunId, company);

        transactionReturnVerifier.verifyEmployerDdRejectRefundCreditUsingSameBankAccount(returnedFinancialTransactions);

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyDDRejectReturnCompanyEvent(company);

        transactionReturnVerifier.verifyTransactionReturnStatus(transactionReturns.getFirst(), TransactionReturnStatusCode.Open);

        Application.rollbackUnitOfWork();
    }


    @Test
    public void testQBDTSymphonyDirectDepositReturnForOnHoldCompany() {
        String sourceCompanyId = "1234567";

        //Load payroll data
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader.loadDataHappyPath(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        //Add OnHold Reason for company
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT,
                sourceCompanyId,
                ServiceSubStatusCode.Fraud);
        PayrollServices.commitUnitOfWork();

        System.out.println("Result " + result.getMessages());

        //Add TransactionReturn for Employee Transaction
        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDirectDepositReturnOnHoldCompany(SourceSystemCode.QBDT, sourceCompanyId);
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 2, returnList.size());

        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));

        TransactionReturnHandler returnHandler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);

        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        // Validation

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        transactionReturnVerifier.verifyNoEmployerDdRejectRefundCredit(company);

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyDDRejectReturnOnHoldCompanyEvent(company);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyDirectDepositReturnForActiveCompanyCBAChanged() {
        String sourceCompanyId = "1234567";

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.loadData2DayBothEERetDoNotHandle(SourceSystemCode.QBDT, "1234567", AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));

        assertEquals("Number of txn returns", 2, returnList.size());

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBDT,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        // Validation
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(transactionReturn);

        transactionReturnVerifier.verifyEmployerDdRejectRefundCreditUsingChangedBankAccount(returnedFinancialTransactions, changedCompanyBankAccount);

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyDDRejectReturnCompanyEvent(company);

        transactionReturnVerifier.verifyTransactionReturnStatus(transactionReturn, TransactionReturnStatusCode.Open);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyDirectDepositReturnCBAChangedBeforeOffload() {
        String sourceCompanyId = "1234567";

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader;
        ACHReturnsDataLoader.loadDataHappyPath(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBDT,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        // verify financial transactions are moved to new ba
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        assertEquals("Number of employer transacions", 1, c1FinTxns.size());
        assertEquals("Debit bank account", changedCompanyBankAccount.getBankAccount(), c1FinTxns.get(0).getDebitBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.createDefualtRejectReturnEvent(SourceSystemCode.QBDT, sourceCompanyId, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 2, returnList.size());


        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        // Validation
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        transactionReturnVerifier.verifyEmployerDdRejectRefundCreditUsingChangedBankAccount(returnedFinancialTransactions, changedCompanyBankAccount);

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyDDRejectReturnCompanyEvent(company);

        transactionReturnVerifier.verifyTransactionReturnStatus(transactionReturn, TransactionReturnStatusCode.Open);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyDirectDepositReturnCBADeactivated() {
        String sourceCompanyId = "1234567";

        DomainEntitySet<TransactionReturn> returnList = ACHReturnsDataLoader.loadData2DayBothEERetDoNotHandle(SourceSystemCode.QBDT, "1234567", AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));

        assertEquals("Number of txn returns", 2, returnList.size());

        // deactivate company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        DataLoader dataloader = new DataLoader();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.deactivateCompanyBankAccount(
                        SourceSystemCode.QBDT,
                        c1dl.getCompany1().getCompanyId(),
                        dataloader.getTestCompanyBankAccount().getCompanyBankAccountID(), true, false);
        assertTrue("Deactivate company bank account", result.isSuccess());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        // Validation
        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        transactionReturnVerifier.verifyNoEmployerDdRejectRefundCredit(company);

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyDDRejectReturnCBADeactivatedCompanyEvent(company, returnedFinancialTransactions.getFirst());

        transactionReturnVerifier.verifyTransactionReturnStatus(transactionReturn, TransactionReturnStatusCode.Open);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyNSFDirectDepositReturnCBAChangedBeforeOffload() {
        String sourceCompanyId = "1234567";

        PayrollServices.beginUnitOfWork();
        ACHReturnsDataLoader loader;
        ACHReturnsDataLoader.loadDataHappyPath(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        // change company bank account
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        CompanyBankAccountDTO companyBankAccountDTO =
                CompanyBankAccountDataLoader.getTestCompanyBankAccountDTOWithNewBankAccount();
        ProcessResult<CompanyBankAccount> result =
                PayrollServices.companyManager.changeCompanyBankAccount(
                        SourceSystemCode.QBDT,
                        c1dl.getCompany1().getCompanyId(),
                        companyBankAccountDTO, false, true, true);
        assertTrue("Change company bank account", result.isSuccess());
        CompanyBankAccount changedCompanyBankAccount = result.getResult();
        PayrollServices.commitUnitOfWork();

        // verify financial transactions are moved to new ba
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> c1FinTxns = FinancialTransaction
                .findFinancialTransactions(SourceSystemCode.QBDT, c1dl.getCompany1().getCompanyId(),
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Created);
        assertEquals("Number of employer transacions", 1, c1FinTxns.size());
        assertEquals("Debit bank account", changedCompanyBankAccount.getBankAccount(), c1FinTxns.get(0).getDebitBankAccount());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.createDDRejectReturnNSF(SourceSystemCode.QBDT, sourceCompanyId, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS));
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of txn returns", 2, returnList.size());


        PayrollServices.beginUnitOfWork();

        TransactionReturn transactionReturn = returnList.get(0);

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);
        // to avoid stale object exception
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        returnHandler.execute(transactionReturn);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(
                transactionReturn);

        transactionReturnVerifier.verifyEmployerDdRejectRefundCreditUsingChangedBankAccount(returnedFinancialTransactions, changedCompanyBankAccount);

        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        transactionReturnVerifier.verifyDDRejectReturnCompanyEvent(company);

        transactionReturnVerifier.verifyTransactionReturnStatus(transactionReturn, TransactionReturnStatusCode.Resolved);

        Application.rollbackUnitOfWork();
    }
}
