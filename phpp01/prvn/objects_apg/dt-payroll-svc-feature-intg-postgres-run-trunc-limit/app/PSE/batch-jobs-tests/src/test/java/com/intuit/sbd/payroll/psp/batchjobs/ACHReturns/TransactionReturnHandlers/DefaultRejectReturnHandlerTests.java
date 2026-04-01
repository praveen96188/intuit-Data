package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup.Codes;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Mar 4, 2008
 * Time: 4:21:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultRejectReturnHandlerTests {


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
    public void testDefaultRejectReturnQBOE() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDefaulltRejectReturnEvent("R01", "NSF description");

        assertEquals("Number of transaction returns", 1, returnList.size());

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);
        Application.commitUnitOfWork();

        // make sure they're all Returned
        for (FinancialTransaction finTxn : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         finTxn.getCurrentTransactionState().getTransactionStateCd());
        }
    }

    @Test
    public void testDefaultRejectReturnQBDT5Day() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDefaulltRejectReturnEventQBDT5Day("R01", "NSF description");

        assertEquals("Number of transaction returns", 1, returnList.size());

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);
        Application.commitUnitOfWork();

        // make sure they're all Returned
        for (FinancialTransaction finTxn : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         finTxn.getCurrentTransactionState().getTransactionStateCd());
        }
    }

    @Test
    public void testDefaultRejectReturnNonNSF_QBOE() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDefaulltRejectReturnEvent("R02", "Non-NSF description");

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);
        Application.commitUnitOfWork();

        // make sure they're all Returned
        for (FinancialTransaction finTxn : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         finTxn.getCurrentTransactionState().getTransactionStateCd());
        }
    }

    @Test
    public void test_QBDT_EmployeeDdCredit_return_creates_QbdtPayrollTransaction() {
        String returnCode = "R02";
        String returnDescription = "Account Closed";

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        String returnBatchId = loader.loadDataForEmployeeDdReturnQBDT(returnCode, returnDescription);

        Application.beginUnitOfWork();

        //
        // Check the TransactionReturn
        //

        SpcfUniqueId batchId = SpcfUniqueId.createInstance(returnBatchId);
        DomainEntitySet<TransactionReturn> returnList = Application.find(TransactionReturn.class, TransactionReturn.ReturnBatch().Id().equalTo(batchId));

        assertEquals("Batch TransactionReturn count", 1, returnList.size());

        //
        // Check the FinancialTransaction
        //

        TransactionReturn txnReturn = returnList.get(0);
        DomainEntitySet<FinancialTransaction> returnedFtList = TransactionReturn.findFinancialTransaction(txnReturn);

        assertEquals("Returned FinancialTransaction count", 1, returnedFtList.size());

        FinancialTransaction returnedFt = returnedFtList.get(0);

        assertEquals("EECR FinancialTransaction State", TransactionTypeCode.EmployeeDdCredit, returnedFt.getTransactionType().getTransactionTypeCd());
        assertEquals("EECR FinancialTransaction State", TransactionStateCode.Returned, returnedFt.getCurrentTransactionState().getTransactionStateCd());

        //
        // Check the QbdtTransactionInfo for the returned transaction
        //

        DomainEntitySet<QbdtTransactionInfo> tiList = Application.find(QbdtTransactionInfo.class, QbdtTransactionInfo.Company().equalTo(returnedFt.getCompany()));
        assertEquals("QbdtTransactionInfo count (all)", 10, tiList.size());

        DomainEntitySet<QbdtTransactionInfo> ptInfoList = tiList.find(QbdtTransactionInfo.QbdtPayrollTransaction().isNotNull());
        assertEquals("QbdtTransactionInfo count (QbdtPayrollTransaction)", 1, ptInfoList.size());

        DomainEntitySet<QbdtTransactionInfo> ptlInfoList = tiList.find(QbdtTransactionInfo.QbdtPayrollTransactionLine().isNotNull());
        assertEquals("QbdtTransactionInfo count (QbdtPayrollTransactionLine)", 1, ptlInfoList.size());

        //
        // Check the QbdtPayrollTransaction
        //

        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(txnReturn.getCompany());
        EmployeeBankAccount eeba = returnedFt.getEmployeeBankAccount();

        QbdtTransactionInfo ptInfo = ptInfoList.get(0);
        assertEquals("QbdtTransactionInfo Cleared", QBOFX.DEFAULT_CLEARED_RESPONSE_STR, ptInfo.getCleared());
        assertEquals("QbdtTransactionInfo AgencyName", QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE, ptInfo.getAgencyName());
        assertEquals("QbdtTransactionInfo AccountName", cba.getSourceBankAccountName(), ptInfo.getAccountName());
        assertTrue("QbdtTransactionInfo OnService", ptInfo.getOnService());

        QbdtPayrollTransaction pt = ptInfo.getQbdtPayrollTransaction();
        assertEquals("QbdtPayrollTransaction TransactionType", QbdtPayrollTransactionType.DDReturn, pt.getTransactionType());
        assertEquals("QbdtPayrollTransaction PeriodEndDate", returnedFt.getSettlementDate(), pt.getPeriodEndDate());
        assertEquals("QbdtPayrollTransaction TransactionDate", txnReturn.getCreatedDate(), pt.getTransactionDate());
        assertEquals("QbdtPayrollTransaction EmployeeName", eeba.getEmployee().getFullName(), pt.getEmployeeName());
        assertEquals("QbdtPayrollTransaction Amount", returnedFt.getFinancialTransactionAmount(), pt.getAmount());
        assertFalse("QbdtPayrollTransaction IsVoided", pt.getIsVoided());

        //
        // Check the QbdtPayrollTransactionLine
        //

        QbdtTransactionInfo ptlInfo = ptlInfoList.get(0);
        assertEquals("QbdtTransactionInfo Memo", returnDescription, ptlInfo.getMemo());
        assertTrue("QbdtTransactionInfo IsDirectDeposit", ptlInfo.getIsDirectDeposit());

        QbdtPayrollTransactionLine ptLine = ptlInfo.getQbdtPayrollTransactionLine();
        assertEquals("QbdtPayrollTransactionLine Amount", returnedFt.getFinancialTransactionAmount().negate(), ptLine.getAmount());

        Application.commitUnitOfWork();
    }

    @Test
    public void testBPDefaultRejectReturnNonNSF() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForBPDefaultRejectReturnEvent("R02", "Non-NSF description");

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);

        // make sure they're all Returned
        for (FinancialTransaction finTxn : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         finTxn.getCurrentTransactionState().getTransactionStateCd());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testBPDefaultRejectReturnNonNSF_SubmitPaymentAgain() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.DirectDeposit, ServiceCode.BillPayment);

        // Create Payees.
        List<Payee> payees = DataLoadServices.addPayees(company, 2);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = DataLoadServices.createBPPayrollRun(company, payees);
        ProcessResult<Collection<PayrollRun>> submitBPPayroll = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        Application.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("Submit BP Payroll", submitBPPayroll);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = submitBPPayroll.getResult().iterator().next();
        DomainEntitySet<FinancialTransaction> txs = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployeeDdCredit);
        BankAccount returnedBankAccount = txs.getFirst().getCreditBankAccount();
        Application.commitUnitOfWork();
        DataLoadServices.returnTxns(txs, "R02", "Non-NSF Return");

        // Verify Transaction Return was created.
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<TransactionReturn> txReturns = TransactionReturn.findTxnRetsForReturnType(company, TransactionReturn.ReturnTypeCodes.RETURN, TransactionReturnStatusCode.Open);
        Application.commitUnitOfWork();
        assertEquals("Pending Return Events", 2, txReturns.size());

        // Submit the payment again.
        PayrollServices.beginUnitOfWork();
        SpcfCalendar paymentDate = PSPDate.getPSPTime();
        paymentDate.addDays(2);

        company = Application.findById(Company.class, company.getId());
        assertEquals("Payee Count", 2, company.getPayeeCollection().size());

        Collection<BillPaymentDTO> bpDTOs = DataLoadServices.createBPPayrollRun(company, payees);

        // Should have failed due to same accounts being used.
        ProcessResult<Collection<PayrollRun>> processResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), bpDTOs);
        assertFalse("Submit 2nd BP payroll", processResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // Change account number and submit the payment again.
        PayrollServices.beginUnitOfWork();
        paymentDate = PSPDate.getPSPTime();
        paymentDate.addDays(2);

        company = Application.findById(Company.class, company.getId());
        Payee payee = Payee.findPayees(company).getFirst();
        payees = new ArrayList<Payee>();
        payees.add(payee);
        bpDTOs = DataLoadServices.createBPPayrollRun(company, payees);

        // Change the bank account number for the checking account that had the original return.
        for (BillPaymentDTO payment : bpDTOs) {
            for (BillPaymentSplitDTO splitDTO : payment.getPaymentTransactions() ) {
                BankAccountDTO baDTO = splitDTO.getPayeeBankAccount().getBankAccount();
                if (returnedBankAccount.getAccountNumber().equals(baDTO.getAccountNumber())) {
                    baDTO.setAccountNumber("34987343459847");
                }
            }
        }
        
        // Should succeed now for this single payee with updated bank account.
        processResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), bpDTOs);
        assertTrue("Submit 3rd BP payroll", processResult.isSuccess());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testBPDefaultRejectReturnNSF() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForBPDefaultRejectReturnEvent("R01", "NSF_Return");

        Application.beginUnitOfWork();
        TransactionReturn transactionReturn = returnList.get(0);
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> returnedFTs = TransactionReturn.findFinancialTransaction(transactionReturn);

        // make sure they're all Returned
        for (FinancialTransaction finTxn : returnedFTs) {
            assertEquals("Update FinancialTransaction Status Rule ", TransactionStateCode.Returned,
                         finTxn.getCurrentTransactionState().getTransactionStateCd());
        }
        Application.commitUnitOfWork();
    }

    @Test
    public void testQBDTSymphonyDefaultRejectReturnNonNSF() {
        String sourceCompanyId = "1234567";

        TransactionReturnHelper transactionReturnHelper = new TransactionReturnHelper();
        transactionReturnHelper.updateSourcePayrollParameterFundingModel(FundingModel.Codes.FIVE_DAY);

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForDefaulltRejectReturnEvent(SourceSystemCode.QBDT, sourceCompanyId, AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(Codes.PSPOFFLOADS), "R02", "Non-NSF description");

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(1);
        TransactionReturn transactionReturn = Application.refresh(returnList.get(0));
        TransactionReturnHandler handler = TransactionReturnHandler.getTransactionReturnHandler(transactionReturn);
        transactionReturn = handler.execute(transactionReturn);
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = TransactionReturn.findFinancialTransaction(transactionReturn);

        TransactionReturnVerifier transactionReturnVerifier = new TransactionReturnVerifier();
        transactionReturnVerifier.verifyDebitFinancialTransactionReturn(returnedFinancialTransactions);

        Application.rollbackUnitOfWork();

        transactionReturnHelper.updateSourcePayrollParameterFundingModel(FundingModel.Codes.TWO_DAY);
    }
}
