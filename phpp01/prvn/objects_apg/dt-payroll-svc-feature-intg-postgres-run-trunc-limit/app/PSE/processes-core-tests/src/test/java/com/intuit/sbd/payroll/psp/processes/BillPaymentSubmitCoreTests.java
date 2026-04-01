package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.NoticeOfChangeUtils;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.TransactionReturnHandler;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.hibernate.FlushMode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * User: mvillani
 * Date: Sep 26, 2007
 * Time: 11:25:59 AM
 */

public class BillPaymentSubmitCoreTests {


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSubmitPayments() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);

        BillPaymentDTO billPaymentDTO2 = GenerateData.generateBillPayment("Payee2", new DateDTO("2007-09-10"),1);


        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        billPaymentDTOs.add(billPaymentDTO2);

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);

        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived);
        assertEquals("BillPaymentReceived event count", 1, events.size());

        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();


        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        // offload all txns
        offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070911000000");
        Application.commitUnitOfWork();


        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }

    @Test
    public void testSubmitPaymentsWithSameBankInfoDiffSourceId() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 1);

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);

        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived);
        assertEquals("BillPaymentReceived event count", 1, events.size());

        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();

        BillPaymentDTO billPaymentDTO2 = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 1);

        BillPaymentSplitDTO oldBillPaymentSplitDTO = billPaymentDTO.getPaymentTransactions().iterator().next();
        BankAccountDTO oldBankAccountDTO = oldBillPaymentSplitDTO.getPayeeBankAccount().getBankAccount();
        BillPaymentSplitDTO curBillPaymentSplitDTO = billPaymentDTO2.getPaymentTransactions().iterator().next();
        BankAccountDTO curBankAccountDTO = curBillPaymentSplitDTO.getPayeeBankAccount().getBankAccount();

        curBankAccountDTO.setAccountType(oldBankAccountDTO.getAccountType());
        curBankAccountDTO.setAccountNumber(oldBankAccountDTO.getAccountNumber());
        curBankAccountDTO.setRoutingNumber(oldBankAccountDTO.getRoutingNumber());
        curBankAccountDTO.setBankName(oldBankAccountDTO.getBankName());
        curBankAccountDTO.setAchAccountType(oldBankAccountDTO.getAchAccountType());

        billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO2);

        submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);

        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();

        Payee payee = Payee.findPayee(company, "Payee1");
        assertEquals(1, payee.getPayeeBankAccountCollection().size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmitPaymentsWithACHReturnsDueToNSF() {

        String sourceCompanyId = "123272727";
        testSubmitPayments();

        // Return the debits as NSF for bill payment.
        Application.beginUnitOfWork(FlushMode.MANUAL);
        PSPDate.addDaysToPSPTime(1);
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRunList = PayrollRun.findPayrollRunsByType(
                        company,
                        SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()),
                        SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone()),
                        PayrollType.Regular, PayrollType.BillPayment);

        DomainEntitySet<FinancialTransaction> c1FinTxns =
                FinancialTransaction.findFinancialTransactions(payrollRunList.get(0),
                                                               new TransactionTypeCode[] {TransactionTypeCode.EmployerDdDebit,
                                                                                          TransactionTypeCode.EmployerFeeDebit,
                                                                                          TransactionTypeCode.ServiceSalesAndUseTax},
                                                               new TransactionStateCode[] {TransactionStateCode.Executed});

        ACHReturnsDataLoader returnsLoader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = returnsLoader.persistTransactionReturns(c1FinTxns, "R01",
                "This is an NSF description");

        assertEquals("Number of C1 EmployerDDDebit,FeeDebit,ServiceSalesAndUseTax EX txns", 4, c1FinTxns.size());

        // # of MMTs = # of Txn Returns.
        assertEquals("Number of txn returns", 3, returnList.size());


        //Run the RejectReturnHandler to process the returned transactions.
        for (TransactionReturn txnReturn : returnList) {

            TransactionReturnHandler returnHandler = TransactionReturnHandler.
                    getTransactionReturnHandler(txnReturn);
            returnHandler.execute(txnReturn);

        }

        Application.commitUnitOfWork();

        assertEquals("PayrollRun status after RejectReturnHandler",PayrollStatus.PendingAutoRedebit,payrollRunList.get(0).getPayrollRunStatus());

        //Offload EMPLOYER_DD_REDEBIT transactions
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //update payrollRunList
        payrollRunList.clear();
        payrollRunList = PayrollRun.findPayrollRunsByType(
                        company,
                        SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()),
                        SpcfCalendar.createInstance(2007, 9, 11, SpcfTimeZone.getLocalTimeZone()),
                        PayrollType.Regular, PayrollType.BillPayment);

        //Find the Employer DD Redebit financial transactions
        Application.beginUnitOfWork(FlushMode.MANUAL);
        DomainEntitySet<FinancialTransaction> redebitFinTxns =
                        FinancialTransaction.findFinancialTransactions(payrollRunList.get(0),
                                                                       new TransactionTypeCode[] {TransactionTypeCode.EmployerDdRedebit,
                                                                                                  TransactionTypeCode.EmployerFeeRedebit,
                                                                                                  TransactionTypeCode.ServiceSalesAndUseTaxRedebit},
                                                                       new TransactionStateCode[] {TransactionStateCode.Executed});

        assertEquals("Number of C1 ERDDREDB EX txns", 4, redebitFinTxns.size());

        ACHReturnsDataLoader returnsLoader2 = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList2 = returnsLoader2.persistTransactionReturns(redebitFinTxns, "R01",
                "This is an NSF description");

        // # of MMTs = # of Txn Returns.
        assertEquals("Number of txn returns", 3, returnList2.size());

        //Run the RejectReturnHandler to process the returned transactions.
        for (TransactionReturn txnReturn : returnList2) {

            TransactionReturnHandler returnHandler = TransactionReturnHandler.
                    getTransactionReturnHandler(txnReturn);
            returnHandler.execute(txnReturn);

        }
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        assertEquals("PayrollRun status after RejectReturnHandler",PayrollStatus.ReturnedTwice,redebitFinTxns.get(0).getPayrollRun().getPayrollRunStatus());

        DomainEntitySet<BillingDetail> billingDetails = BillingDetail.findBillingDetails(payrollRunList.get(0), OfferingServiceChargeType.DebitReturnFee);
        assertEquals("DebitReturnFee count ", 1, billingDetails.size());
        Application.commitUnitOfWork();
    }

    @Test
    public void testPartialNSFs() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 2, 10, 32, 0, 0));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit, ServiceCode.Cloud, ServiceCode.BillPayment);

        List<Payee> payees = DataLoadServices.addPayees(company, 2);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> bpDTOs = DataLoadServices.createBPPayrollRun(company, payees);
        Iterator<BillPaymentDTO> iter = bpDTOs.iterator();
        BillPaymentDTO dto1 = iter.next();
        dto1.setAmount(new SpcfMoney("94.00"));
        dto1.setDepositDate(new DateDTO("2011-06-06"));
        BillPaymentDTO dto2 = iter.next();
        dto2.setAmount(new SpcfMoney("1400.00"));
        dto2.setDepositDate(new DateDTO("2011-06-06"));

        assertSuccess(PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), bpDTOs));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 7, 5, 22, 0, 0));

        PayrollServices.beginUnitOfWork();
        FinancialTransaction erDDDebit94 = assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerDdDebit).find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("94.00"))));
        FinancialTransaction erFeeDebit = assertOne(FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerFeeDebit).find(FinancialTransaction.FinancialTransactionAmount().equalTo(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2))));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.returnTxns(new DomainEntitySet<FinancialTransaction>(new HashSet<FinancialTransaction>(Arrays.asList(erDDDebit94, erFeeDebit))));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 7, 12, 55, 0, 0));

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 9, 7, 14, 0, 0));

        ProcessACHTransactions post = new ProcessACHTransactions();
        post.process(PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 10, 5, 24, 0, 0));
        DataLoadServices.returnTxns(assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunType().equalTo(PayrollType.BillPayment))), TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.EmployerFeeRedebit);

        post.process(PSPDate.getPSPTime());

        //todo asserts

    }


    @Test
    public void testSubmitZeroPayments() {
        String sourceCompanyId = "123272727";
        String paymentId = "12345";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2, paymentId,false, null);
        billPaymentDTO.setAmount(new SpcfMoney("0.00"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);


        BillPaymentSplitDTO billPaymentSplitDTO = billPaymentDTO.getPaymentTransactions().iterator().next();
        billPaymentSplitDTO.setAmount(new BigDecimal("0.00"));
        billPaymentDTOs.add(billPaymentDTO);

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        Message message5001 = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("616")) {
                message = currMessage;
            }

              if (currMessage.getMessageCode().equals("5001")) {
                message5001 = currMessage;
            }
        }

        assertNotNull(message);
        assertNull(message5001);
        assertEquals("Error Code:", "616", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "Payment to Payee1 Name. has an invalid amount of $0.00. Please try again with an amount greater than zero.",
                message.getMessage());

    }

    @Test
    public void testSubmitNegativePayments() {
        String sourceCompanyId = "123272727";
        String paymentId = "12345";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2, paymentId,false,null);
        billPaymentDTO.setAmount(new SpcfMoney("-2.00"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);


        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("616")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "616", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "Payment to Payee1 Name. has an invalid amount of $-2.00. Please try again with an amount greater than zero.",
                message.getMessage());

    }

    @Test
    public void testSubmitZeroPaymentSplit() {
        String sourceCompanyId = "123272727";
        String paymentId = "12345";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 1, paymentId,false,null);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        BillPaymentSplitDTO billPaymentSplitDTO = billPaymentDTO.getPaymentTransactions().iterator().next();
        billPaymentSplitDTO.setAmount(new BigDecimal("0.00"));
        billPaymentDTOs.add(billPaymentDTO);


        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("5001")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "BillPaymentSplitAmount has invalid value",
                message.getMessage());

    }

    @Test
    public void testSubmitNegativePaymentSplit() {
        String sourceCompanyId = "123272727";
        String paymentId = "12345";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2, paymentId,false,null);
        //billPaymentDTO.setAmount(new SpcfMoney("-2.00"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        BillPaymentSplitDTO billPaymentSplitDTO = billPaymentDTO.getPaymentTransactions().iterator().next();
        billPaymentSplitDTO.setAmount(new BigDecimal("-2.00"));
        billPaymentDTOs.add(billPaymentDTO);


        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("5001")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "BillPaymentSplitAmount has invalid value",
                message.getMessage());

    }

    @Test
    public void testCancelPayments() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.beginUnitOfWork();
        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // cancel  2 txns
        PayrollServices.beginUnitOfWork();
        Collection<String> billPaymentCancelIds = new ArrayList<String>();
        it = billPaymentDTOs.iterator();
        for (int i = 0; i < 2; i++) {
            if (it.hasNext()) {
                billPaymentCancelIds.add(((BillPaymentDTO) it.next()).getBillPaymentId());
            }
        }
        ProcessResult cancelResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentCancelIds,null);
        PayrollServices.commitUnitOfWork();

        // cancel  3 txns
        PayrollServices.beginUnitOfWork();
        billPaymentCancelIds = new ArrayList<String>();
        for (int i = 0; i < 3; i++) {
            if (it.hasNext()) {
                billPaymentCancelIds.add(((BillPaymentDTO) it.next()).getBillPaymentId());
            }
        }
        cancelResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentCancelIds,null);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testLimits() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 1; i <= 2; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }

        for (BillPaymentDTO billPaymentDTOLimit : billPaymentDTOs) {
            for (BillPaymentSplitDTO bps : billPaymentDTOLimit.getPaymentTransactions()) {
                bps.setAmount(new BigDecimal("7500"));
            }
            billPaymentDTOLimit.setAmount(new SpcfMoney("15000"));

        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (int i = 1; i <= 4; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee2" + i, new DateDTO("2007-09-12"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        for (BillPaymentDTO billPaymentDTOLimit : billPaymentDTOs) {
            for (BillPaymentSplitDTO bps : billPaymentDTOLimit.getPaymentTransactions()) {
                bps.setAmount(new BigDecimal("7500"));
            }
            billPaymentDTOLimit.setAmount(new SpcfMoney("15000"));

        }
        for (int i = 1; i <= 6; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee3" + i, new DateDTO("2007-09-15"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // cancel  2 txns
        PayrollServices.beginUnitOfWork();
        Collection<String> billPaymentCancelIds = new ArrayList<String>();
        it = billPaymentDTOs.iterator();
        for (int i = 0; i < 2; i++) {
            if (it.hasNext()) {
                billPaymentCancelIds.add(((BillPaymentDTO) it.next()).getBillPaymentId());
            }
        }
        ProcessResult cancelResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentCancelIds,null);
        PayrollServices.commitUnitOfWork();

        // cancel  3 txns
        PayrollServices.beginUnitOfWork();
        billPaymentCancelIds = new ArrayList<String>();
        for (int i = 0; i < 3; i++) {
            if (it.hasNext()) {
                billPaymentCancelIds.add(((BillPaymentDTO) it.next()).getBillPaymentId());
            }
        }
        cancelResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentCancelIds,null);
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Payment exceeds limits
     */

    @Test
    public void testSubmitPayments_PayeeLimitExceeded_TwoPaymentDates() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getPayeeLimit().add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("613")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "613", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>",
                message.getMessage());


    }

    @Test
    public void testSubmitPayments_PayeeLimitExceeded_OnHold() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getPayeeLimit().add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 4; ++i) {
            PayrollServices.beginUnitOfWork();
            ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
            PayrollServices.commitUnitOfWork();
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
            BPCompanyServiceInfo bpService = (BPCompanyServiceInfo) company.getService(ServiceCode.BillPayment);
            assertEquals(i,bpService.getConsecutiveLimitViolationCount());
            if (i != 4) {
                assertFalse(company.isCompanyOnHold());
            }            
            PayrollServices.commitUnitOfWork();

            Message message = null;
            for (Message currMessage : submitResult.getMessages()) {
                if (currMessage.getMessageCode().equals("613")) {
                    message = currMessage;
                }
            }

            assertNotNull(message);
            assertEquals("Error Code:", "613", message.getMessageCode());

            // Verify that the correct message string has returned
            assertEquals("Error Message",
                    "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>",
                    message.getMessage());
        }

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertTrue(company.isCompanyOnHold());
        assertEquals(ServiceSubStatusCode.BillPaymentLimit, company.getOnHoldReasonCollection().get(0).getOnHoldReasonCd());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSubmitPayments_PayeeLimitExceeded_ConsecutiveLimitViolationCountReset() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getPayeeLimit().add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 4; ++i) {
            PayrollServices.beginUnitOfWork();
            if (i == 4) {
                billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
                billPaymentDTOs.clear();
                billPaymentDTOs.add(billPaymentDTO);
            }

            ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
            BPCompanyServiceInfo bpService = (BPCompanyServiceInfo) company.getService(ServiceCode.BillPayment);

            if (i == 4) {
                assertEquals(0,bpService.getConsecutiveLimitViolationCount());
            } else {
                assertEquals(i,bpService.getConsecutiveLimitViolationCount());
            }

            assertFalse(company.isCompanyOnHold());
            PayrollServices.commitUnitOfWork();

            if (i != 4) {
                Message message = null;
                for (Message currMessage : submitResult.getMessages()) {
                    if (currMessage.getMessageCode().equals("613")) {
                        message = currMessage;
                    }
                }

                assertNotNull(message);
                assertEquals("Error Code:", "613", message.getMessageCode());

                // Verify that the correct message string has returned
                assertEquals("Error Message",
                        "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>",
                        message.getMessage());
            } else {
                assertTrue(submitResult.isSuccess());
            }
        }
    }

    @Test
    public void testSubmitPayments_PayeeLimitExceeded() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getPayeeLimit().add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("613")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "613", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>",
                message.getMessage());


    }

    @Test
    public void testSubmitPayments_PayeeLimitExceededTwoTransmissions() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 1);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();
        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-12"), 1);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getPayeeLimit().subtract(SpcfMoney.createInstance(1.0));
        billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentSplitAmount);
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("613")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "613", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>",
                message.getMessage());
    }

    @Test
    public void testSubmitPayments_CompanyLimitExceededTwoTransmissions() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        billPaymentDTO = GenerateData.generateBillPayment("Payee2", new DateDTO("2007-09-10"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        billPaymentDTO = GenerateData.generateBillPayment("Payee3", new DateDTO("2007-09-10"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        billPaymentDTO = GenerateData.generateBillPayment("Payee4", new DateDTO("2007-09-10"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        for (BillPaymentDTO bpDTO : billPaymentDTOs) {
            BillPaymentSplitDTO split = bpDTO.getPaymentTransactions().iterator().next();
            split.setAmount(split.getAmount().subtract(split.getAmount().subtract(new BigDecimal(1))));
            bpDTO.setAmount(SpcfUtils.convertToSpcfMoney(split.getAmount()));
        }

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        PayrollServices.beginUnitOfWork();
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getPayeeLimit().subtract(SpcfMoney.createInstance(1.0));

        billPaymentDTOs = new ArrayList<BillPaymentDTO>();

        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-12"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        billPaymentDTO = GenerateData.generateBillPayment("Payee2", new DateDTO("2007-09-12"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        billPaymentDTO = GenerateData.generateBillPayment("Payee3", new DateDTO("2007-09-12"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        billPaymentDTO = GenerateData.generateBillPayment("Payee4", new DateDTO("2007-09-12"), 1);
        billPaymentDTOs.add(billPaymentDTO);

        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentSplitAmount);

        for (BillPaymentDTO bpDTO : billPaymentDTOs) {
            BillPaymentSplitDTO split = bpDTO.getPaymentTransactions().iterator().next();
            split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));
            bpDTO.setAmount(billPaymentAmount);
        }

        submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("612")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "612", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "This payment for company QBDT:123272727, dated 09/12/2007, exceeds the current dollar limit for the company and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>", message.getMessage());
    }

    @Test
    public void testSubmitPayments_PayeeLimitDoesNotExceed_TwoPaymentDates() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-15"), 1);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getPayeeLimit().subtract(SpcfDecimal.createInstance("1"));

        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() == 1);

        Message message = submitResult.getMessages().get(0);

        assertNotNull(message);
        assertEquals("Error Code:", "613", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message",
                "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>",
                message.getMessage());        
    }

    @Test
    public void testTotalBillPaymentSubmissionExceedsLimit() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getCompanyLimit().add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() >= 1);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("612")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "612", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "This payment for company QBDT:123272727, dated 09/10/2007, exceeds the current dollar limit for the company and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>", message.getMessage());

    }

    @Test
    public void testTotalBillPaymentSubmissionExceedsLimit_TwoPaymentDates() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getCompanyLimit().add(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-15"), 2);
        billPaymentDTOs.add(billPaymentDTO);


        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() == 4);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("612")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "612", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "This payment for company QBDT:123272727, dated 09/15/2007, exceeds the current dollar limit for the company and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>", message.getMessage());

    }

    @Test
    public void testTotalBillPaymentSubmissionExceeds_TwoPaymentDates() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 1);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getCompanyLimit().subtract(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-15"), 2);
        billPaymentDTOs.add(billPaymentDTO);


        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() == 3);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("613")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "613", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>", message.getMessage());

    }


    @Test
    public void testTotalBillPaymentSubmissionExceeds_NonDefaultLimit() {

        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.companyManager.updateBPLimits(SourceSystemCode.QBDT, sourceCompanyId, new SpcfMoney("100000"), new SpcfMoney("80000"));
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 1);
        Service service = Application.findById(Service.class, ServiceCode.BillPayment);
        BPCompanyServiceInfo companyBPService = (BPCompanyServiceInfo) CompanyService.findCompanyService(company, service.getServiceCd());
        SpcfDecimal billPaymentSplitAmount = companyBPService.getCompanyLimit().subtract(SpcfDecimal.createInstance("1"));
        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        BillPaymentSplitDTO split = billPaymentDTO.getPaymentTransactions().iterator().next();
        SpcfMoney billPaymentAmount = new SpcfMoney(billPaymentDTO.getAmount().subtract(SpcfUtils.convertToSpcfMoney(split.getAmount())).add(billPaymentSplitAmount));
        billPaymentDTO.setAmount(billPaymentAmount);
        split.setAmount(SpcfUtils.convertToBigDecimal(new SpcfMoney(billPaymentSplitAmount)));

        billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-15"), 2);
        billPaymentDTOs.add(billPaymentDTO);

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", submitResult.getMessages().size() == 3);

        Message message = null;
        for (Message currMessage : submitResult.getMessages()) {
            if (currMessage.getMessageCode().equals("613")) {
                message = currMessage;
            }
        }

        assertNotNull(message);
        assertEquals("Error Code:", "613", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "This payment for company QBDT:123272727, to payee Payee1 Name., exceeds the current dollar limit for a single payee and cannot be processed. Reduce the payment amount, or <a href=http://payroll.intuit.com/support/contact/index.jsp>contact customer support.</a>", message.getMessage());

    }


    @Test
    public void testSubmitPaymentWithNOCPendingDifferentPayees() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C01", "");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            PayeeBankAccount payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));

            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();


        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 12; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }

        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();


        assertTrue(submitResult.isSuccess());


    }

    @Test
        public void testSubmitPaymentWithNOCC05() {

            ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
            DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C05", "52");


            TransactionReturn transactionReturn = returnList.get(0);
            PayrollServices.beginUnitOfWork();
            transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

            TransactionReturnHandler returnHandler = TransactionReturnHandler.
                    getTransactionReturnHandler(transactionReturn);

            DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

            FinancialTransaction finTxn = finTxnList.get(0);

            BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

            returnHandler.execute(transactionReturn);

            Application.commitUnitOfWork();

            Application.beginUnitOfWork();
            finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

            finTxn = finTxnList.get(0);
            String companyId = finTxn.getCompany().getSourceCompanyId();
            String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
            bankAccount = finTxn.getNonIntuitBankAccount();

            DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                    EventTypeCode.NOC, CompanyEventStatus.Inactive, null, null);

            //Update Transaction Return Status Rule
            assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Resolved,
                    transactionReturn.getReturnStatusCd());

            PayeeBankAccount payeeBankAccount = null;

            //Assertion for Create NOC System Event Rule
            assertEquals("Company Events", 1, companyEventsList.size());
            String payeeBankAccountId = "";
            for (CompanyEvent companyEvent : companyEventsList) {
                payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
                payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));
                assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
            }

            Application.commitUnitOfWork();
            // submit second payroll without changing the BankAccount info
            Application.beginUnitOfWork();

            payeeBankAccount = Application.refresh(payeeBankAccount);

            Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

            BillPaymentDTO billPaymentDTO;

            Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();

            String payeeId = payeeBankAccount.getPayee().getSourcePayeeId();
            String oldAccount = payeeBankAccount.getBankAccount().getAccountNumber();
            String oldRouting = payeeBankAccount.getBankAccount().getRoutingNumber();
            String oldBankName = payeeBankAccount.getBankAccount().getBankName();
            BankAccountType oldAccountType = payeeBankAccount.getBankAccount().getAccountTypeCd();

            billPaymentDTO = GenerateData.generateBillPayment(payeeId, new DateDTO("2007-09-10"), 1);

            Iterator<BillPaymentSplitDTO> iterator =  billPaymentDTO.getPaymentTransactions().iterator();
            if (iterator.hasNext()) {
                BillPaymentSplitDTO billPaymentSplitDTO = iterator.next();
                BankAccountDTO bankAccountDTO = billPaymentSplitDTO.getPayeeBankAccount().getBankAccount();
                bankAccountDTO.setAccountNumber(oldAccount);
                bankAccountDTO.setRoutingNumber(oldRouting);
                bankAccountDTO.setBankName(oldBankName);
                bankAccountDTO.setAccountType(oldAccountType);
                bankAccountDTO.setAchAccountType(ACHBankAccountType.Loan);
            }
            billPaymentDTOs.add(billPaymentDTO);

            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
            PayrollServices.commitUnitOfWork();

            assertTrue(submitResult.isSuccess());

            PayrollServices.beginUnitOfWork();

            PayeeBankAccount newPayeeBA = PayeeBankAccount.findActivePayeeBankAccount(company,
                                                                                      payeeId,
                                                                                      oldAccount,
                                                                                      oldRouting,
                                                                                      oldAccountType);
            assertEquals(ACHBankAccountType.Loan, newPayeeBA.getBankAccount().getACHAccountTypeCd());

            transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
            assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());


            PayrollServices.rollbackUnitOfWork();
        }

    @Test
    public void testSubmitPaymentWithNOCC06() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C06", "", "12346               52");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        PayeeBankAccount payeeBankAccount = null;

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {
            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));
            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();

        payeeBankAccount = Application.refresh(payeeBankAccount);

        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();

        String payeeId = payeeBankAccount.getPayee().getSourcePayeeId();
        String newAccount = "12346";
        String oldRouting = payeeBankAccount.getBankAccount().getRoutingNumber();
        String oldBankName = payeeBankAccount.getBankAccount().getBankName();
        BankAccountType oldAccountType = payeeBankAccount.getBankAccount().getAccountTypeCd();

        billPaymentDTO = GenerateData.generateBillPayment(payeeId, new DateDTO("2007-09-10"), 1);

        Iterator<BillPaymentSplitDTO> iterator =  billPaymentDTO.getPaymentTransactions().iterator();
        if (iterator.hasNext()) {
            BillPaymentSplitDTO billPaymentSplitDTO = iterator.next();
            BankAccountDTO bankAccountDTO = billPaymentSplitDTO.getPayeeBankAccount().getBankAccount();
            bankAccountDTO.setAccountNumber(newAccount);
            bankAccountDTO.setRoutingNumber(oldRouting);
            bankAccountDTO.setBankName(oldBankName);
            bankAccountDTO.setAccountType(oldAccountType);
            bankAccountDTO.setAchAccountType(ACHBankAccountType.Loan);
        }
        billPaymentDTOs.add(billPaymentDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        assertTrue(submitResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        PayeeBankAccount newPayeeBA = PayeeBankAccount.findActivePayeeBankAccount(company,
                                                                                  payeeId,
                                                                                  newAccount,
                                                                                  oldRouting,
                                                                                  oldAccountType);
        assertEquals(ACHBankAccountType.Loan, newPayeeBA.getBankAccount().getACHAccountTypeCd());

        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());


        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmitPaymentWithNOCC07() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C07", "", "11100061412346            42");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        PayeeBankAccount payeeBankAccount = null;

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {
            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));
            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();

        payeeBankAccount = Application.refresh(payeeBankAccount);

        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();

        String payeeId = payeeBankAccount.getPayee().getSourcePayeeId();
        String newAccount = "12346";
        String newRouting = "111000614";
        String oldBankName = payeeBankAccount.getBankAccount().getBankName();
        BankAccountType oldAccountType = payeeBankAccount.getBankAccount().getAccountTypeCd();

        billPaymentDTO = GenerateData.generateBillPayment(payeeId, new DateDTO("2007-09-10"), 1);

        Iterator<BillPaymentSplitDTO> iterator =  billPaymentDTO.getPaymentTransactions().iterator();
        if (iterator.hasNext()) {
            BillPaymentSplitDTO billPaymentSplitDTO = iterator.next();
            BankAccountDTO bankAccountDTO = billPaymentSplitDTO.getPayeeBankAccount().getBankAccount();
            bankAccountDTO.setAccountNumber(newAccount);
            bankAccountDTO.setRoutingNumber(newRouting);
            bankAccountDTO.setBankName(oldBankName);
            bankAccountDTO.setAccountType(oldAccountType);
            bankAccountDTO.setAchAccountType(ACHBankAccountType.Ledger);
        }
        billPaymentDTOs.add(billPaymentDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        assertTrue(submitResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        PayeeBankAccount newPayeeBA = PayeeBankAccount.findActivePayeeBankAccount(company,
                                                                                  payeeId,
                                                                                  newAccount,
                                                                                  newRouting,
                                                                                  oldAccountType);
        assertEquals(ACHBankAccountType.Ledger, newPayeeBA.getBankAccount().getACHAccountTypeCd());

        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());


        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmitPaymentWithNOCC06DifferentBA() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C06", "", "12346               52");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        PayeeBankAccount payeeBankAccount = null;

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {
            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));
            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();

        payeeBankAccount = Application.refresh(payeeBankAccount);

        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();

        String payeeId = payeeBankAccount.getPayee().getSourcePayeeId();
        String newAccount = "12347";
        String oldRouting = payeeBankAccount.getBankAccount().getRoutingNumber();
        String oldBankName = payeeBankAccount.getBankAccount().getBankName();
        BankAccountType oldAccountType = payeeBankAccount.getBankAccount().getAccountTypeCd();

        billPaymentDTO = GenerateData.generateBillPayment(payeeId, new DateDTO("2007-09-10"), 1);

        Iterator<BillPaymentSplitDTO> iterator =  billPaymentDTO.getPaymentTransactions().iterator();
        if (iterator.hasNext()) {
            BillPaymentSplitDTO billPaymentSplitDTO = iterator.next();
            BankAccountDTO bankAccountDTO = billPaymentSplitDTO.getPayeeBankAccount().getBankAccount();
            bankAccountDTO.setAccountNumber(newAccount);
            bankAccountDTO.setRoutingNumber(oldRouting);
            bankAccountDTO.setBankName(oldBankName);
            bankAccountDTO.setAccountType(oldAccountType);
            bankAccountDTO.setAchAccountType(ACHBankAccountType.Loan);
        }
        billPaymentDTOs.add(billPaymentDTO);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        assertTrue(submitResult.isSuccess());

        PayrollServices.beginUnitOfWork();

        PayeeBankAccount newPayeeBA = PayeeBankAccount.findActivePayeeBankAccount(company,
                                                                                  payeeId,
                                                                                  newAccount,
                                                                                  oldRouting,
                                                                                  oldAccountType);
        assertEquals(ACHBankAccountType.Loan, newPayeeBA.getBankAccount().getACHAccountTypeCd());

        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
        assertEquals(TransactionReturnStatusCode.Resolved, transactionReturn.getReturnStatusCd());


        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmitPaymentWithNOCPendingSamePayee() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C01", "");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            PayeeBankAccount payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));

            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();


        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 12; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }

        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();


        assertTrue(submitResult.isSuccess());

    }

    @Test
    public void testSubmitPaymentWithNOCPendingSamePayeeC04() {
        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForSingleNoticeOfChangePayeeBankAccount("C04", "");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        String baId = "";
        String payeeId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            PayeeBankAccount payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));
            payeeId = payeeBankAccount.getPayee().getSourcePayeeId();
            baId= payeeBankAccount.getSourceBankAccountId();
            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());
        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();


        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 12; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }

        Iterator it = billPaymentDTOs.iterator();
        String payeeName = "";

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            payeeName = payeeDTO.getName();
            payeeDTO.setSourcePayeeId(payeeId);
            billPaymentDTO.getPaymentTransactions().iterator().next().getPayeeBankAccount().getBankAccount().setAccountNumber("newNumber");
            billPaymentDTO.getPaymentTransactions().iterator().next().getPayeeBankAccount().setPayeeBankAccountId(baId);
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
            break;
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        assertTrue(submitResult.isSuccess());

        assertEquals("Number of messages", 1, submitResult.getMessages().size());

        Message message = submitResult.getMessages().get(0);

        assertEquals("Message text", "You have submitted a payment for payee "+payeeName+", against whose account there is a pending Notice of Change (NOC). Intuit has send you the information we received from the bank, and you must resolve the issues in order to process Direct Deposit payments for this payee.", message.getMessage());
        assertEquals("Message getMessageCode", "605", message.getMessageCode());
        assertEquals("Message getLevel", MessageInfo.MessageLevel.WARNING, message.getLevel());

    }

    @Test
    public void testSubmitPaymentWithNOCResolved() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C01", "");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        String baId = "";
        String payeeId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            PayeeBankAccount payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));
            payeeId = payeeBankAccount.getPayee().getSourcePayeeId();
            baId= payeeBankAccount.getSourceBankAccountId();
            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();


        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 12; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }

        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            payeeDTO.setSourcePayeeId(payeeId);
            billPaymentDTO.getPaymentTransactions().iterator().next().getPayeeBankAccount().getBankAccount().setAccountNumber("newNumber");
            billPaymentDTO.getPaymentTransactions().iterator().next().getPayeeBankAccount().setPayeeBankAccountId(baId);
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();


      //  assertTrue(submitResult.isSuccess());


    }

        @Test
    public void testSubmitPaymentWithNOCNotResolved() {

        ACHReturnsDataLoader loader = new ACHReturnsDataLoader();
        DomainEntitySet<TransactionReturn> returnList = loader.loadDataForNoticeOfChangePayeeBankAccount("C01", "");


        TransactionReturn transactionReturn = returnList.get(0);
        PayrollServices.beginUnitOfWork();
        transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());

        TransactionReturnHandler returnHandler = TransactionReturnHandler.
                getTransactionReturnHandler(transactionReturn);

        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        FinancialTransaction finTxn = finTxnList.get(0);

        BankAccount bankAccount = finTxn.getNonIntuitBankAccount();
        String oldAccountNumber = bankAccount.getAccountNumber();

        returnHandler.execute(transactionReturn);

        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        finTxnList = TransactionReturn.findFinancialTransaction(transactionReturn);

        finTxn = finTxnList.get(0);
        String companyId = finTxn.getCompany().getSourceCompanyId();
        String accountNumber = NoticeOfChangeUtils.getCorrectedBankAccountNumber(transactionReturn);
        bankAccount = finTxn.getNonIntuitBankAccount();

        DomainEntitySet<CompanyEvent> companyEventsList = CompanyEvent.findCompanyEvents(finTxn.getCompany(),
                EventTypeCode.NOC, CompanyEventStatus.Active, null, null);

        //Update Transaction Return Status Rule
        assertEquals("Transaction Return Status ", TransactionReturnStatusCode.Open,
                transactionReturn.getReturnStatusCd());

        //Assertion for Create NOC System Event Rule
        assertEquals("Company Events", 1, companyEventsList.size());
        String payeeBankAccountId = "";
        String baId = "";
        String payeeId = "";
        for (CompanyEvent companyEvent : companyEventsList) {

            payeeBankAccountId = companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.PayeeBankAccountId);
            PayeeBankAccount payeeBankAccount = PayrollServices.entityFinder.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(payeeBankAccountId));
            payeeId = payeeBankAccount.getPayee().getSourcePayeeId();
            baId= payeeBankAccount.getSourceBankAccountId();
            assertEquals("Payee Bank Account", bankAccount, payeeBankAccount.getBankAccount());

        }

        Application.commitUnitOfWork();
        // submit second payroll without changing the BankAccount info
        Application.beginUnitOfWork();


        Company company = Company.findCompany(companyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO;

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();


        for (int i = 12; i <= 14; i++) {
            billPaymentDTO = GenerateData.generateBillPayment("Payee" + i, new DateDTO("2007-09-10"), 2);
            billPaymentDTOs.add(billPaymentDTO);
        }

        Iterator it = billPaymentDTOs.iterator();

        while (it.hasNext()) {
            billPaymentDTO = (BillPaymentDTO) it.next();
            PayeeDTO payeeDTO = billPaymentDTO.getPayeeDTO();
            payeeDTO.setSourcePayeeId(payeeId);
           // billPaymentDTO.getPaymentTransactions().iterator().next().getPayeeBankAccount().getBankAccount().setAccountNumber("newNumber");
            billPaymentDTO.getPaymentTransactions().iterator().next().getPayeeBankAccount().setPayeeBankAccountId(baId);
            PayrollServices.billPaymentManager.addOrUpdatePayee(company.getSourceSystemCd(), company.getSourceCompanyId(), payeeDTO);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();


        //assertTrue(submitResult.isSuccess());


    }
    @Test
    public void testReversePayments() {
        String sourceCompanyId = "123272727";
        String paymentId = "12345";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);

        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2, paymentId,false,null);

        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);


        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);

        // offload all txns
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070910000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        
        // offload all txns
        offloader = new OffloadACHTransactions();
        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070911000000");
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070912000000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, paymentId);
        ArrayList<String> transactionIds = new ArrayList<String>();
        for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
            FinancialTransaction financialTransaction = billPaymentSplit.getFinancialTransaction();
            transactionIds.add(billPaymentSplit.getSourceId());
        }

        TransactionReverseDTO transactionReverseDTO = new TransactionReverseDTO();
        transactionReverseDTO.setChargeFee(false);
        transactionReverseDTO.setDdTransactionIdList(transactionIds);
        transactionReverseDTO.setSourcePayrollRunId(billPayment.getPayrollRun().getSourcePayRunId());
        transactionReverseDTO.setIntuitInitiatedReversals(false);
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(new Date("09/15/2007"));
        transactionReverseDTO.setTxDate(calDate);
        transactionReverseDTO.setTxSettlementTypeCd(SettlementTypeDTO.ACH);

        ProcessResult processResult = PayrollServices.payrollManager.reverseTransaction(
                SourceSystemCode.QBDT,
                sourceCompanyId,
                transactionReverseDTO);
        assertTrue("reverse success:" + processResult.toString(), processResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070912000000");
        PayrollServices.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(5);
        PayrollServices.commitUnitOfWork();

        // run transaction processor
        PayrollServices.beginUnitOfWork();
        ProcessACHTransactions post = new ProcessACHTransactions();
        post.process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();
    }

    //@see PayrollSubmitDDTests--this is a PayCard test
    @Test
    public void testBillPaymentPayrollPayeeUsingAPayCardAccountHasFees() {
        String routingNumber = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ROUTING_NUMBER).split(",")).get(0);
        String accountPrefix = Arrays.asList(SystemParameter.findStringValue(SystemParameter.Code.PAYCARD_ACCOUNT_PREFIX).split(",")).get(0);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.BillPayment);
        DataLoadServices.addPayees(company, 1);

        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        Payee p = assertOne(company.getPayeeCollection());

        PayeeBankAccount pba = assertOne(p.getPayeeBankAccountCollection());
        PayeeBankAccountDTO pbaDTO = new PayeeBankAccountDTO();
        pbaDTO.setPayeeBankAccountId(pba.getSourceBankAccountId());
        pbaDTO.setBankAccount(PayrollServices.dtoFactory.create(pba.getBankAccount()));
        pbaDTO.getBankAccount().setRoutingNumber(routingNumber);
        pbaDTO.getBankAccount().setAccountNumber(accountPrefix+"00123456");

        assertSuccess(PayrollServices.billPaymentManager.addOrUpdatePayeeBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), p.getSourcePayeeId(), pbaDTO));

        PayrollServices.commitUnitOfWork();

        // submit BP
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        List<Payee> ps = new ArrayList<Payee>();
        p = Application.findById(Payee.class, p.getId());
        ps.add(p);

        Collection<BillPaymentDTO> bpDTOs = DataLoadServices.createBPPayrollRun(company, ps);

        ProcessResult<Collection<PayrollRun>> processResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), bpDTOs);
        PSP_PRAssert.assertSuccess("submit BP payroll", processResult);
        PayrollServices.commitUnitOfWork();

        Collection<PayrollRun> payrollRuns = processResult.getResult();
        Assert.assertEquals(1, payrollRuns.size());
        PayrollRun payrollRun = payrollRuns.iterator().next();


        Assert.assertEquals(ServiceChargePrices.getNormalPerPayrollServiceChargeWithSalesTaxFY16(), payrollRun.getFeeReceivableAmount());
        Assert.assertEquals("no PayCard event", 0, CompanyEvent.findCompanyEvents(company, EventTypeCode.PayrollReceivedPayCard, CompanyEventStatus.Active, false).size());
    }
    @Test
    public void testBillPaymentsSTDFY16Offering() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateOffering(company, OfferingCode.BillPaymentSTDFY16, "BILLPAYMENTSTD-FY16");
        PayrollServices.beginUnitOfWork();
        BillPaymentDTO billPaymentDTO = GenerateData.generateBillPayment("Payee1", new DateDTO("2007-09-10"), 2);

        BillPaymentDTO billPaymentDTO2 = GenerateData.generateBillPayment("Payee2", new DateDTO("2007-09-10"),1);


        Collection<BillPaymentDTO> billPaymentDTOs = new ArrayList<BillPaymentDTO>();
        billPaymentDTOs.add(billPaymentDTO);
        billPaymentDTOs.add(billPaymentDTO2);

        ProcessResult<Collection<PayrollRun>> submitResult = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);

        DomainEntitySet<CompanyEvent> events = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived);
        assertEquals("BillPaymentReceived event count", 1, events.size());

        PayrollServices.commitUnitOfWork();

        assertTrue("Number of Errors:", submitResult.getMessages().size() == 0);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class,
                                                                         BillingDetail.OfferingServiceChargeType().in(OfferingServiceChargeType.PerPayment)).sort(BillingDetail.OfferingServiceChargeType());
        //DD fee
        assertEquals("Unit Price", new SpcfMoney("1.75"), billingDetails.get(0).getUnitPrice());
        PayrollServices.rollbackUnitOfWork();
    }
}