package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.*;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 23, 2009
 * Time: 10:54:18 AM
 */
public class PayrollVoidTaxTests {
    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();
    private static final String FIT = "1";
    private static final String FICA = "61";
    private int achTaxOffloadOffset;

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");
    }

    private static final ArrayList<LedgerAccountCode> taxLedgerAccounts = new ArrayList<LedgerAccountCode>();

    static {
        taxLedgerAccounts.add(LedgerAccountCode.TaxCurrentLiability);
        taxLedgerAccounts.add(LedgerAccountCode.TaxCurrentCash);
        taxLedgerAccounts.add(LedgerAccountCode.ERPayable);
        taxLedgerAccounts.add(LedgerAccountCode.AgencyTaxRefund);
        taxLedgerAccounts.add(LedgerAccountCode.TaxFutureLiability);
        taxLedgerAccounts.add(LedgerAccountCode.TaxFutureReceivable);
    }


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        for (String agencyId : supportedPaymentTemplates) {
            DataLoadServices.updatePaymentTemplateSupportedDate(agencyId, SpcfCalendar.createInstance(2005, 1, 1));
        }
        beforeEachTest();
        truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testInvalidCompanyParameters() {


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();

        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(null, "1234567", voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Source System Code is not specified.", message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                                       .voidPayroll(SourceSystemCode.QBDT, null, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Source Company ID is not specified.", message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                                       .voidPayroll(null, null, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 2);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Source System Code is not specified.", message.getMessage());

        message = processResult.getMessages().get(1);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Source Company ID is not specified.", message.getMessage());

    }


    @Test
    public void testAchTaxDebitExecuted_PSRV003071() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";
        String[] states = {"AZ"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        //First Payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5000", "10000", "4000", "12", "10"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Offload Debit
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        
        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 1, 12, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);
        //Void First payroll to create Agency Tax Debit transaction
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun1.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();
        //Second Payroll
        payrollDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-11"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5100", "8000", "4200", "27", "12"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit)
                                                                                                                                                       .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created)));
        assertEquals("MMT ACH Credit Created", 1, moneyMovementTransactions.size());

        // Verify AgencyTaxDebit transaction is created after voiding a payroll
        DomainEntitySet<FinancialTransaction> financialTransactions = moneyMovementTransactions.get(0).getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit));
        assertEquals("Agency Tax Debit Created", 1, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

        //offload debit and Taxpayments
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));
        
        statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 1, 14, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);

        //Complete transactions
        DataLoadServices.runACHTransactionProcessor();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar taxInitiationDate = SpcfCalendar.createInstance(2011, 1, 14, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(taxInitiationDate, -achTaxOffloadOffset);
        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit)
                                                                                                             .And(MoneyMovementTransaction.InitiationDate().equalTo(taxInitiationDate)));
        // mmts size == 1
        financialTransactions = moneyMovementTransactions.get(0).getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit));
        //Check if AgencyTaxDebit transaction is present
        assertEquals("Agency Tax Debit", 1, financialTransactions.size());
        //Verify AgencyTaxDebit is completed
        assertEquals("Agency Tax Debit Completed", TransactionStateCode.Completed, financialTransactions.get(0).getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testCompanyDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);


        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();


        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(SourceSystemCode.QBDT, "InvalidCompanyId", voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBDT:InvalidCompanyId does not exist.", message.getMessage());

    }

    @Test
    public void testNullDTO() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);


        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(SourceSystemCode.QBDT, psid, null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5002", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Required 'voidPayrollDTO' input is missing or blank", message.getMessage());

    }

    @Test
    public void testInvalidDTO() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();


        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "130", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Source Payroll Run ID is not specified.", message.getMessage());

        voidPayrollDTO.setSourcePayrollRunId("Source PayrollRun ID with length more than 50 characters");
        List<String> paycheckIds = new ArrayList<String>();
        paycheckIds.add("Source Paycheck Id with length more than 50 characters");
        voidPayrollDTO.setPaycheckIdList(paycheckIds);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                                       .voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 2);

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "SourcePayrollRunId has invalid value", message.getMessage());

        // validate error code
        message = processResult.getMessages().get(1);
        assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "PaycheckId has invalid value", message.getMessage());

    }

    @Test
    public void testPayrollRunDoesNotExist() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId("InvalidPayrollId");

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "194", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Payroll Run with DDTxBatchID InvalidPayrollId does not exist for company QBDT:123456789.", message.getMessage());

    }

    @Test
    public void testPayCheckDoesNotExist() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);

        PayrollServices.rollbackUnitOfWork();


        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> paycheckIds = new ArrayList<String>();
        paycheckIds.add("InvalidPaycheckId");
        voidPayrollDTO.setPaycheckIdList(paycheckIds);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager
                                                                                      .voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", voidProcessResult.getMessages().size() == 1);

        // validate error code
        Message message = voidProcessResult.getMessages().get(0);
        assertEquals("Error Code:", "299", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Paycheck InvalidPaycheckId for company QBDT:123456789 does not exist.", message.getMessage());

    }

    @Test
    public void testTaxImpoundNotOffloaded() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();


        //Try to Void  payroll run  without offloading tax impound
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", voidProcessResult.getMessages().size() == 1);

        // validate error code
        Message message = voidProcessResult.getMessages().get(0);
        assertEquals("Error Code:", "10058", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Cannot void a payroll that has not been offloaded. Please use Recall.", message.getMessage());

    }

    @Test
    public void testVoidPayrollNoActiveCBA() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // change AgencyTaxCredits to EXECUTED state and ERTaxdebit to completed
        PaymentTemplate pt941 = PayrollServices.entityFinder.findById(PaymentTemplate.class, "IRS-941-PAYMENT");
        offloadAgencyTaxCredits(pt941);
        completeERTaxDebit();

        PayrollServices.beginUnitOfWork();

        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyBankAccount companyBankAccount =
                CompanyBankAccount.findActiveCompanyBankAccount(company);

        // set the bank account status to inactive
        ProcessResult<CompanyBankAccount> result = PayrollServices.companyManager.deactivateCompanyBankAccount(SourceSystemCode.QBDT, psid,
                                                                                                               companyBankAccount.getSourceBankAccountId(), true, false);
        PayrollServices.commitUnitOfWork();

        assertSuccess(result);

        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager
                                                                                      .voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", voidProcessResult.getMessages().size() == 1);

        // validate error code
        Message message = voidProcessResult.getMessages().get(0);
        assertEquals("Error Code:", "1062", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", "Company QBDT:123456789 does not have an active bank account.", message.getMessage());

    }


    @Test
    public void testTaxPaycheckAlreadyVoided() {
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2007-10-02"));

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070929000000");
        Application.commitUnitOfWork();

        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payroll.getSourcePayRunId());
        List<String> paycheckIds = new ArrayList<String>();

        PayrollServices.beginUnitOfWork();
        String sourcePaycheckId = Application.refresh(payroll).getPaycheckCollection().getFirst().getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();

        paycheckIds.add(sourcePaycheckId);
        voidPayrollDTO.setPaycheckIdList(paycheckIds);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        // try to void the same paycheck again
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                                       .voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "196", message.getMessageCode());

        // Verify that the correct massage string has returned
        assertEquals("Error Message", String.format("Paycheck %s has already been canceled or voided.", sourcePaycheckId), message.getMessage());

    }

    @Test
    public void testPayrollAlreadyVoided() {
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2007-10-02"));

        // offload and return impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payroll.getSourcePayRunId());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070929000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(processResult);

        // persistence testing
        // verify all Paychecks are voided
        PayrollServices.beginUnitOfWork();
        payroll = Application.findById(PayrollRun.class, payroll.getId());
        company = Application.refresh(company);
        for (Paycheck paycheck : payroll.getPaycheckCollection()) {
            assertEquals("Paycheck voided", true, paycheck.isVoided());
            assertNotNull("Company Void Not Null", paycheck.getCompanyAdjustmentSubmission());
            assertEquals("Company", company.getId(),
                         paycheck.getCompanyAdjustmentSubmission().getCompany().getId());

            SpcfCalendar pspDate = PSPDate.getPSPTime();
            CalendarUtils.clearTime(pspDate);

            SpcfCalendar submissionDate = paycheck.getCompanyAdjustmentSubmission().getSubmissionDate().toLocal();
            CalendarUtils.clearTime(submissionDate);

            assertTrue("Submission Date", CalendarUtils.getDifferenceInDays(pspDate, submissionDate) == 0);
        }
        PayrollServices.commitUnitOfWork();

        // try to void again
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                                       .voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        PSP_PRAssert.assertCount(2, processResult);
        PayrollServices.beginUnitOfWork();
        Application.refresh(payroll);
        for (Paycheck paycheck : payroll.getPaycheckCollection()) {
            PSP_PRAssert.assertContains(String.format("Paycheck %s has already been canceled or voided", paycheck.getSourcePaycheckId()), 196, MessageInfo.MessageLevel.ERROR, processResult);
        }
    }

    @Test
    public void testVoidEntirePayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "0.00", "0.00", "900.00", "-900.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"900.00", "-900.00", "0.00", "0.00", "0.00", "0.00"});
        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        printLedgerBalances(ledgerBalances);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(voidProcessResult);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "-900.00", "900.00", "0.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

        //verify $0 behavior
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction fed941Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.getIRS_941()).find());
        assertEquals(TaxPaymentStatus.ReadyToSend, fed941Payment.getTaxPaymentStatus());
        assertEquals(SpcfCalendar.createInstance(2011, 1, 11, 8, 0, 0, 0), fed941Payment.getInitiationDate());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testPartialVoid() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "0.00", "0.00", "900.00", "-900.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"900.00", "-900.00", "0.00", "0.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(voidProcessResult);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "-900.00", "900.00", "0.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testVoidApplyOverpaymentToNextPayroll() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 23));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addCompanyBankAccount(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-05-26"), emps, new String[]{"1", "61", "62", "63", "64", "66"}, new String[]{"159", "83.46", "83.46", "19.52", "19.52", "10.77"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2011, 5, 24));


        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 3, SpcfTimeZone.getLocalTimeZone()));
        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(voidProcessResult);
        PayrollServices.rollbackUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-10"), emps, new String[]{"1", "61", "62", "63", "64", "66"}, new String[]{"59", "83.46", "83.46", "19.52", "19.52", "10.77"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "-10.77", "-264.96", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoidApplyOverpaymentAcrossFUTAQuarters() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 3, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-03-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 3, 8);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2012, 4, 27);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());

        DataLoadServices.setPSPDate(2012, 4, 28);
        voidAPaycheck(payrollRun);

        DataLoadServices.setPSPDate(2012, 6, 3);
        PayrollRun newPayroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-05-28"));
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        FinancialTransaction agencyTaxOverpaymentAppliedTransaction = assertOne(newPayroll.getFinancialTransactions(TransactionTypeCode.EmployerTaxOverpaymentApplied));
        assertTrue(agencyTaxOverpaymentAppliedTransaction.getLaw().isFUTA());
        assertEquals(new SpcfMoney("132.00"), agencyTaxOverpaymentAppliedTransaction.getFinancialTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testVoidDoesNotApplyOverpaymentCrossFUTAAcrossYears() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));

        DataLoadServices.setPSPDate(2012, 3, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-03-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 3, 8);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2012, 4, 27);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());

        DataLoadServices.setPSPDate(2012, 4, 28);
        voidAPaycheck(payrollRun);

        DataLoadServices.setPSPDate(2013, 1, 14);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-18"));

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        DomainEntitySet<FinancialTransaction> financialTransactions = company.getFinancialTransactions()
                                                                             .find(FinancialTransaction.TransactionType().TransactionTypeCd()
                                                                                                       .in(TransactionTypeCode.AgencyTaxOverpaymentApplied, TransactionTypeCode.EmployerTaxOverpaymentApplied));
        assertEquals(0, financialTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }


    public void voidAPaycheck(PayrollRun payrollRun) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        voidPaychecks.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoidApplyOverpaymentToNextPayrollThenRecall() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 5, 23));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.claimOffer(company, DataLoadServices.WAIVE_ALL_FEES);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-05-26"), emps, new String[]{"1", "61", "62", "63", "64", "66"}, new String[]{"159", "83.46", "83.46", "19.52", "19.52", "10.77"});
        String payrollRun1Id = payrollRunDTO.getPayrollTXBatchId();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2011, 5, 24));
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 3, SpcfTimeZone.getLocalTimeZone()));
        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO));
        PayrollServices.commitUnitOfWork();

        // submit a new payroll
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-10"), emps, new String[]{"1", "61", "62", "63", "64", "66"}, new String[]{"59", "83.46", "83.46", "19.52", "19.52", "10.77"});
        String payrollRun2Id = payrollRunDTO.getPayrollTXBatchId();
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "-10.77", "-264.96", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 6, 3, SpcfTimeZone.getLocalTimeZone()));

        //recall the second payroll run
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // two debits for the 2 payrolls
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHDirectDeposit));
        assertEquals("zero debit", 1, moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO)).size());
        assertEquals(TransactionStateCode.Cancelled, assertOne(Application.refresh(payrollRun).getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit))).getCurrentTransactionState().getTransactionStateCd());
        assertEquals("$375.73 debit", 1, moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("375.73"))).size());

        // 3 payments 2 941, 1 940
        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS));
        assertEquals("zero 941", 0, moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO)
                                                                                           .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("IRS-941-PAYMENT"))).size());
        assertEquals("$364.96 941", 1, moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("364.96"))).size());
        assertEquals("zero 940", 1, moneyMovementTransactions.find(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO)
                                                                                           .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("IRS-940-PAYMENT"))).size());

        // since we recalled the second payroll we should have 10.77 in ERP and 364.96 in ATR
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertEquals("$10.77 in er payable", new SpcfMoney("10.77"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable));
        assertEquals("$364.96 in ATR", new SpcfMoney("364.96"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoidEntirePayroll_SubmitSecondPayroll() {

        testVoidEntirePayroll();

        // Submit a Second Payroll After voiding the First One

        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        List<Employee> employees = new ArrayList<Employee>(company.getDirectDepositEmployees());

        PayrollRunDTO payrollDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-07"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"200", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());

        // Verify Ledger Balances for Payroll 2
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "-900.00", "0.00", "375.00", "-375.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        // Verify Ledger Balances for the Quarter
        ArrayList<PayrollRun> payrollRuns = new ArrayList<PayrollRun>(PayrollRun.findPayrollRunsForQuarter(company, CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate())));
        ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "-900.00", "0.00", "0.00", "375.00", "-375.00"});
        assertLedgerBalances(payrollRuns, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testStateVoidEntirePayroll_OffloadDebit_SubmitSecondPayroll() {

        DataLoadServices.setPSPDate(2011, 1, 5);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalAndCAStateTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateRequiredIDs(company, null, true);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"6", "67"}, new String[]{"75", "60"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2011, 1, 5));

        DataLoadServices.setPSPDate(2011, 1, 6);

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateACHAgentEnabledFlags(company, null, true);

        PayrollServices.beginUnitOfWork();
        assertSuccess(voidProcessResult);
        DomainEntitySet<MoneyMovementTransaction> payments = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT"));
        assertEquals("payment count", 1, payments.size());
        assertEquals("payment status", PaymentStatus.Created, payments.get(0).getStatus());
        assertEquals("tax payment status", TaxPaymentStatus.Ignore, payments.get(0).getTaxPaymentStatus());
        assertEquals("payment amount", SpcfMoney.ZERO, payments.get(0).getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // Submit a second payroll for the same period
        PayrollServices.beginUnitOfWork();
        payrollDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"6", "67"}, new String[]{"100", "50"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payments = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("CA-PITSDI-PAYMENT").And(MoneyMovementTransaction.MoneyMovementTransactionAmount().notEqualTo(SpcfMoney.ZERO)));
        assertEquals("payment count", 1, payments.size());
        assertEquals("payment status", PaymentStatus.Created, payments.get(0).getStatus());
        assertEquals("tax payment status", TaxPaymentStatus.ReadyToSend, payments.get(0).getTaxPaymentStatus());
        assertEquals("payment amount", "450.00", payments.get(0).getMoneyMovementTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testVoidEntirePayroll_AfterPaymentSubmitted() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "0.00", "0.00", "900.00", "-900.00"});
        String[] laws = (new String[]{"1", "61", "63", "66"});
        ArrayList<String> lawIds = new ArrayList<String>(Arrays.asList(laws));

        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);


        PayrollServices.rollbackUnitOfWork();

        // offload impounds and 941 payments
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        PaymentTemplate pt941 = PayrollServices.entityFinder.findById(PaymentTemplate.class, "IRS-941-PAYMENT");
        offloadAgencyTaxCredits(pt941);

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"120.00", "-120.00", "0.00", "0.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(voidProcessResult);
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        // Verify Ledger Balances
        ledgerBalancesToCompare = createCompareMap(new String[]{"-780.00", "-120.00", "120.00", "780.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        //  assertLedgerBalancesLaw(company, "1", ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testVoidEntirePayrollAfterPaymentSubmitted_SubmitSecondPayroll() {

        testVoidEntirePayroll_AfterPaymentSubmitted();

        // Submit a Second Payroll After voiding the First One

        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        List<Employee> employees = new ArrayList<Employee>(company.getDirectDepositEmployees());

        PayrollRunDTO payrollDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-14"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"200", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());

        // Verify Ledger Balances for Payroll 2
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "-120.00", "-780.00", "375.00", "-375.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        // Verify Ledger Balances for the Quarter
        ArrayList<PayrollRun> payrollRuns = new ArrayList<PayrollRun>(PayrollRun.findPayrollRunsForQuarter(company, CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate())));
        ledgerBalancesToCompare = createCompareMap(new String[]{"-780.00", "-120.00", "0.00", "0.00", "375.00", "-375.00"});
        assertLedgerBalances(payrollRuns, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testVoidEntirePayrollAfterPaymentSubmitted_SubmitTwoMorePayrolls() {

        testVoidEntirePayroll_AfterPaymentSubmitted();

        // Submit a Second Payroll After voiding the First One
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        List<Employee> employees = new ArrayList<Employee>(company.getDirectDepositEmployees());

        PayrollRunDTO payrollDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-14"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"200", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());

        // Verify Ledger Balances for Payroll 2
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "-120.00", "-780.00", "375.00", "-375.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        // Verify Ledger Balances for the Quarter
        ArrayList<PayrollRun> payrollRuns = new ArrayList<PayrollRun>(PayrollRun.findPayrollRunsForQuarter(company, CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate())));
        ledgerBalancesToCompare = createCompareMap(new String[]{"-780.00", "-120.00", "0.00", "0.00", "375.00", "-375.00"});
        assertLedgerBalances(payrollRuns, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        employees = new ArrayList<Employee>(company.getDirectDepositEmployees());

        payrollDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-14"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"200", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("Payroll_3");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());

        // Verify Ledger Balances for Payroll 3
        ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "0.00", "0.00", "1275.00", "-1275.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        // Verify Ledger Balances for the Quarter
        payrollRuns = new ArrayList<PayrollRun>(PayrollRun.findPayrollRunsForQuarter(company, CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate())));
        ledgerBalancesToCompare = createCompareMap(new String[]{"-780.00", "-120.00", "0.00", "0.00", "1650.00", "-1650.00"});
        assertLedgerBalances(payrollRuns, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRecallEntirePayroll_100K() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"15000", "20000", "9000", "1500"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        //   DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun,mmtDueDate);
        PayrollServices.rollbackUnitOfWork();

//        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
//        PayrollServices.commitUnitOfWork();

        //Recall entire payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

    }


    @Test
    public void testRecallEntirePayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        //   DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun,mmtDueDate);
        PayrollServices.rollbackUnitOfWork();

//        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
//        PayrollServices.commitUnitOfWork();

        //Recall entire payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

    }

    @Test
    public void testRecallPartialPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();

//        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
//        PayrollServices.commitUnitOfWork();

        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

    }


    @Test
    public void testRecallPartialPayroll_100K() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"15000", "10000", "12000", "4000"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();

//        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
//        PayrollServices.commitUnitOfWork();

        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

    }

    @Test
    public void testRecallPartialPayroll_SubmitSecondPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();

//        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
//        PayrollServices.commitUnitOfWork();

        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

        // Submit a Second Payroll After recalling the First One
        PayrollServices.beginUnitOfWork();
        payrollDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        List<Employee> employees = new ArrayList<Employee>(company.getDirectDepositEmployees());
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollDTO, company, new DateDTO("2011-01-14"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"200", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());

//        // Verify Ledger Balances for Payroll 2
//        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"0.00", "0.00", "-120.00", "-780.00", "1275.00", "-375.00"});
//        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
//
//        // Verify Ledger Balances for the Quarter
//        ArrayList<PayrollRun> payrollRuns = new ArrayList<PayrollRun>(PayrollRun.findPayrollRunsForQuarter(company, CalendarUtils.getFirstDayOfQuarter(payrollRun.getPaycheckDate())));
//        ledgerBalancesToCompare = createCompareMap(new String[]{"-780.00", "-120.00", "0.00", "0.00", "1275.00", "-375.00"});
//        assertLedgerBalances(payrollRuns, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testVoidEntirePayroll_Resubmit() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(voidProcessResult);

        PayrollServices.beginUnitOfWork();
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("ResubmitPR1");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        SpcfDecimal ledgerBalance = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.TaxCurrentLiability);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testVoidEntirePayroll_Resubmit_Recall() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        //   DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun,mmtDueDate);
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(voidProcessResult);

        PayrollServices.beginUnitOfWork();
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("ResubmitPR1");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        SpcfDecimal ledgerBalance = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.TaxCurrentLiability);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Recall entire payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId("ResubmitPR1");

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

    }


    @Test
    public void testVoidEntirePayroll_Resubmit_Recall_Resubmit() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        //   DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun,mmtDueDate);
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(voidProcessResult);

        // Resubmit Payroll
        PayrollServices.beginUnitOfWork();
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        payrollDTO.setPayrollTXBatchId("ResubmitPR1");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        SpcfDecimal ledgerBalance = LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.TaxCurrentLiability);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Recall entire payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId("ResubmitPR1");

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-15"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"175", "160", "125", "40"});
        payrollDTO.setPayrollTXBatchId("ResubmitPR2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);

        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testVoidPartialPayroll() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110108000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void one paycheck
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        List<String> payCheckIds = new ArrayList<String>();
        payCheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(payCheckIds);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

    }

    @Test
    public void testVoidPartialPayroll_AfterPaymentSubmit_WithBackDate() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // create void dto to submit later
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        List<String> payCheckIds = new ArrayList<String>();
        payCheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(payCheckIds);

        // offload impounds
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 10, SpcfTimeZone.getLocalTimeZone()));

        // Back Date a payroll
        payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // submit the first 941 payroll
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2011, 1, 11, SpcfTimeZone.getLocalTimeZone()));

        //Void one paycheck from the first payroll
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               MoneyMovementTransaction.DueDate().equalTo(SpcfCalendar.createInstance(2011, 1, 12, SpcfTimeZone.getLocalTimeZone())));
        assertEquals("mmt count", 2, moneyMovementTransactions.size());
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            if (moneyMovementTransaction.isPendingTaxPayment()) {
                assertEquals("pending payment amount", new SpcfMoney("270"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
            } else {
                assertEquals("pending payment amount", new SpcfMoney("405"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
            }
        }
    }

    @Test
    public void testVoidPartialPayrollAfterAgencySubmission() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "64", "65", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // offload impound and AgencyTaxCredits

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110108000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PaymentTemplate pt941 = PayrollServices.entityFinder.findById(PaymentTemplate.class, "IRS-941-PAYMENT");
        offloadAgencyTaxCredits(pt941);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 8, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //Void one paycheck
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        List<String> payCheckIds = new ArrayList<String>();
        payCheckIds.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(payCheckIds);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(voidProcessResult);

    }


    @Test
    public void create100KPayrolls() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"5000", "2500", "3000", "4500"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        // DataLoadServices.assertMmt(SettlementType.EFTPS, new SpcfMoney("33000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-24"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"6000", "2500", "3000", "4500"});
        payrollRunDTO1.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        // DataLoadServices.assertMmt(SettlementType.EFTPS, new SpcfMoney("60000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO2 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO2);
        PayrollRunDTO payrollDTO2 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO2, company, new DateDTO("2011-02-04"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"7000", "2500", "4000", "5000"});
        payrollRunDTO2.setPayrollTXBatchId("Payroll_3");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO2);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO2.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO2, payrollRun);
//        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO2, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
//        DataLoadServices.assertMmt(SettlementType.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 4, 29, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 5, 2, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);

//        PayrollRunDTO payrollRunDTO3 = new PayrollRunDTO();
//        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO3);
//        PayrollRunDTO payrollDTO3 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO3, company, new DateDTO("2011-02-18"), emps, new String[]{"61", "62"}, new String[]{"5000", "5000"});
//        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO3);
        PayrollServices.rollbackUnitOfWork();
//        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
//        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO3.getPayrollTXBatchId());
//        DataLoadServices.assertPayrollsEqual(payrollRunDTO3, payrollRun);
//        DataLoadServices.assertMmt(SettlementType.EFTPS, new SpcfMoney("90000"), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 21, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()), 1);
//        DataLoadServices.assertMmt(SettlementType.EFTPSDirectDebit, new SpcfMoney("30000"), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 21, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), SpcfCalendar.createInstance(2011, 2, 18, SpcfTimeZone.getLocalTimeZone()), 1);
//        DataLoadServices.assertFinancialTransaction(SettlementType.ACH, new SpcfMoney("0"), TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Cancelled, 1);
//        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPS, new SpcfMoney("15000"), TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Cancelled, 2);
//        DataLoadServices.assertFinancialTransaction(SettlementType.EFTPSDirectDebit, new SpcfMoney("15000"), TransactionTypeCode.AgencyDirectCredit, null, 2);

        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testVoidEntirePayroll_100K() throws Exception {

        String psid = "123456789";
        create100KPayrolls();

        // offload impounds - Payroll 1
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        // offload impounds - Payroll 2
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110120000000");
        PayrollServices.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // Void Payroll 2
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "Payroll_2");
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();


        assertSuccess(voidProcessResult);

        PayrollServices.beginUnitOfWork();
        // Verify Ledger Balances
        // HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"900.00", "-900.00", "0.00", "0.00", "0.00", "0.00"});
        // assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testVoidEntireDirectDebitPayroll_100K() throws Exception {

        String psid = "123456789";
        create100KPayrolls();

        DataLoadServices.setPSPDate(2011, 1, 5);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 1, 7);
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DataLoadServices.setPSPDate(2011, 1, 20);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 1, 24);
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        DataLoadServices.setPSPDate(2011, 2, 2);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 2, 4);
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        // Void Payroll 3
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "Payroll_3");
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();


        assertSuccess(voidProcessResult);

        PayrollServices.beginUnitOfWork();
        // Verify Ledger Balances
        // HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"900.00", "-900.00", "0.00", "0.00", "0.00", "0.00"});
        // assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testEFTPS941OnlyPayroll_With_COBRA() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101025000000");
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addCOBRACompanyLaw(company);
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), emps, new String[]{"61", "62", "63", "64", "1", "196"}, new String[]{"5", "12", "5.5", "45", "25", "-27"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", SpcfCalendar.createInstance(2011, 1, 31));

        // Verify Ledger Balances
        HashMap<LedgerAccountCode, String> ledgerBalancesToCompare = createCompareMap(new String[]{"-54.00", "0.00", "0.00", "0.00", "131.00", "-131.00"});
        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        printLedgerBalances(ledgerBalances);

        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101029000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        ledgerBalancesToCompare = createCompareMap(new String[]{"131.00", "-131.00", "0.00", "0.00", "0.00", "0.00"});
        assertLedgerBalances(payrollRun, ledgerBalancesToCompare);
        PayrollServices.commitUnitOfWork();

        offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        ProcessResult processResult1 = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        assertEquals("Cobra payroll Void", 1, processResult1.getErrorMessages().size());
        assertEquals("Cobra payroll Void", "5000", processResult1.getErrorMessages().get(0).getMessageCode());
        assertEquals("Cobra payroll Void", "Positive liability cannot be added to a payroll that has already offloaded", processResult1.getErrorMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testRecallFirstPayroll_100K_AfterSecondPayroll() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 1, 1));

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63"}, new String[]{"5000", "10000", "4000"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"1", "61", "63"}, new String[]{"5100", "8000", "4200"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        ProcessResult<PayrollRun> processResult2 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult2);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).find();
        assertEquals("MMTs ", 2, moneyMovementTransactions.size());
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertEquals("MMT payment method", PaymentMethod.EFTPSDirectDebit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
            assertFalse("MMT Hold reason - enrollment", moneyMovementTransaction.hasActiveOnHoldReason(PaymentOnHoldReason.Enrollment));
        }
        PayrollServices.rollbackUnitOfWork();

        //Recall First payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        transactionCancelDTO.setSourcePayrollRunId("Payroll_1");

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(recallProcessResult);

        // make sure the payroll is now canceled, and all of the transactions have been canceled or voided
        // The Payroll run is not cancelled because the monthly fees are still created.
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, ((PayrollRun) processResult.getResult()).getSourcePayRunId());
        assertEquals("payroll completed", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, new Query<FinancialTransaction>()
                .Where(FinancialTransaction.PayrollRun().equalTo(payrollRun).And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled))
                                           .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Voided))
                                           .And(FinancialTransaction.TransactionType().TransactionTypeCd().notIn(TransactionTypeCode.AgencyHPDEPriorPaymentApplied, TransactionTypeCode.EmployerFeeDebit, TransactionTypeCode.ServiceSalesAndUseTax))));
        assertEquals("non cancelled transactions", 0, financialTransactions.size());
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> movementTransactions = MoneyMovementTransaction.findTaxPayments().setCompany(company).find();
        assertEquals("MMTs ", 1, movementTransactions.size());
        assertEquals("MMT Amount ", new SpcfMoney("51900.00"), movementTransactions.get(0).getMoneyMovementTransactionAmount());
        assertEquals("Payment Method ", PaymentMethod.EFTPS, movementTransactions.get(0).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

    }

    //Payroll Transactions are set to Canceled when a Void is Received
    @Test
    public void test_PSRV002273_EntireRecall() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-03-21"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();

        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertEquals("PayrollRun status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();

        assertSuccess(recallProcessResult);
    }

    @Test
    public void test_PSRV002273_PartialRecall() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-03-21"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"75", "60", "125", "40"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, PayrollStatus.Complete, payrollRun);
        String recallPaycheck = payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId();
        PayrollServices.rollbackUnitOfWork();

        //Recall one paycheck from the payroll run
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        PayrollServices.beginUnitOfWork();
        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertEquals("PayrollRun status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();


        assertSuccess(recallProcessResult);

    }

    @Test
    public void testPayrollStatusAfterRecallingAllPaychecksAndLeavingPositiveAdjustments_PSRV002406() {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.enrollEFTPS(company);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("178.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("150.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        Collection<CompanyAdjustmentSubmissionDTO> comAdjustments = new ArrayList<CompanyAdjustmentSubmissionDTO>();
        comAdjustments.add(companyAdjustmentSubmissionDTO);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-18"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});
        payrollDTO.setCompanyAdjustmentSubmissionDTOs(comAdjustments);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payroll = processResult.getResult();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Recall all paycheck from the payroll run
        PayrollServices.beginUnitOfWork();
        Application.refresh(payroll);
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        for (Paycheck paycheck : payroll.getPaycheckCollection()) {
            paycheckList.add(paycheck.getSourcePaycheckId());
        }
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("Payroll Run status after company termination", PayrollStatus.Complete, payroll.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollStatusAfterRecallingAllPaychecksAndLeavingNegativeAdjustments_PSRV002406() {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 8, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.enrollEFTPS(company);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-08-25"), emps, new String[]{"61", "62", "63", "64", "1", "66"}, new String[]{"19999", "19999", "19999", "19999", "19999", "5.6"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payroll1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-08-25"), emps, new String[]{"61", "62", "63", "64", "1", "66"}, new String[]{"100", "100", "100", "100", "100", "5.6"});
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payroll2 = processResult.getResult();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Recall all paycheck from the payroll run
        PayrollServices.beginUnitOfWork();
        Application.refresh(payroll1);
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        for (Paycheck paycheck : payroll1.getPaycheckCollection()) {
            paycheckList.add(paycheck.getSourcePaycheckId());
        }
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);
    }

    @Test
    public void test_Negative_Liability_Payroll_Void() throws Exception {
        Company company = DataLoadServices.setupCompanyWithNegativeLiability_IRS_NM();

        DataLoadServices.enrollEFTPS(company);

        String psid = company.getSourceCompanyId();
        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        DataLoadServices.setPSPDate(2011, 8, 10);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 8, 12);

        //Void entire payroll run
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(sourcePayrollId);
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate941 = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        PaymentTemplate paymentTemplate940 = PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT");

        //Assert for ATCs
        DomainEntitySet<FinancialTransaction> agencyCredits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of ATCs for 941 payment template", 5,
                     agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATCs for 940 payment template", 1,
                     agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                     agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 192, with $10 ", 1,
                     agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Assert for ATDs
        DomainEntitySet<FinancialTransaction> agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of ATDs for 941 payment template", 5,
                     agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATDs for 940 payment template", 1,
                     agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATD for NM-WC1-PAYMENT - Law Id 191, with $10 ", 2,
                     agencyDebits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());
        assertEquals("ATD for NM-WC1-PAYMENT - Law Id 192, with $10 ", 1,
                     agencyDebits.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Assert for ATOs
        DomainEntitySet<FinancialTransaction> agencyOverPayments = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpayment, TransactionStateCode.Executed);
        assertEquals("Number of ATOs for 941 payment template", 5,
                     agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATOs for 940 payment template", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATO for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());
        assertEquals("ATO for NM-WC1-PAYMENT - Law Id 192, with $10 ", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Assert for ERTOAs
        DomainEntitySet<FinancialTransaction> employerTaxOverPaymentsApplied = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Created);
        assertEquals("Number of ERTOAs for 941 payment template", 5,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ERTOAs for 940 payment template", 1,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 192, with $10 ", 1,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        employerTaxOverPaymentsApplied = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Executed);
        assertEquals("Adjustment payroll ERTOA", 1, employerTaxOverPaymentsApplied.size());
        assertEquals("Adjustment payroll ERTOA Law", "191", employerTaxOverPaymentsApplied.getFirst().getLaw().getLawId());
        assertEquals("Adjustment payroll ERTOA Amount", new SpcfMoney("10"), employerTaxOverPaymentsApplied.getFirst().getFinancialTransactionAmount());

        //Voided Payroll ERTDb status
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        assertEquals("ER Tax Debit amount", SpcfMoney.ZERO, payrollRun.getEmployerTaxDebitTransaction().getFinancialTransactionAmount());
        assertEquals("ER Tax Debit status", TransactionState.findTransactionState(TransactionStateCode.Executed), payrollRun.getEmployerTaxDebitTransaction().getCurrentTransactionState());

        //Adjustment Payroll ERTDb status
        PayrollRun adjustmentPayroll = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Adjustment));
        assertEquals("ER Tax Debit amount", SpcfMoney.ZERO, adjustmentPayroll.getEmployerTaxDebitTransaction().getFinancialTransactionAmount());
        assertEquals("ER Tax Debit status", TransactionState.findTransactionState(TransactionStateCode.Created), adjustmentPayroll.getEmployerTaxDebitTransaction().getCurrentTransactionState());

        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar eftpsInitDate = SpcfCalendar.createInstance(2011, 10, 28, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.submitPayment(eftpsInitDate);
        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 26.00),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -26.00));
    }

    @Test
    public void test_Negative_Liability_Payroll_Void_NegativePaycheck() throws Exception {
        Company company = DataLoadServices.setupCompanyWithNegativeLiability_IRS_NM();

        String psid = company.getSourceCompanyId();
        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        DataLoadServices.setPSPDate(2011, 8, 10);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 8, 12);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        Paycheck negativePaycheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getTaxCollection().find(Tax.Law().LawId().equalTo("1").And(Tax.TaxLiabilityAmount().equalTo(new SpcfMoney("-2")))).size() == 1) {
                negativePaycheck = paycheck;
            }
        }

        //Void only negative liability paycheck
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(sourcePayrollId);
        voidPayrollDTO.setPaycheckIdList(Arrays.asList(negativePaycheck.getSourcePaycheckId()));
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate941 = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        PaymentTemplate paymentTemplate940 = PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT");

        //Assert for ATCs
        DomainEntitySet<FinancialTransaction> agencyCredits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of ATCs for 941 payment template", 5,
                     agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().notEqualTo(new SpcfMoney("1")))).size());
        //Validating 941 MMT amount
        assertEquals("IRS-941 Payment", new SpcfMoney("51"), agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941)).getFirst().getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("Number of ATCs for 940 payment template", 1,
                     agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("16")))).size());
        assertEquals("IRS-940 Payment", new SpcfMoney("15"), agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940)).getFirst().getMoneyMovementTransaction().getMoneyMovementTransactionAmount());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 191, with $10", 1,
                     agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 192, with $10", 1,
                     agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Assert for ATDs
        DomainEntitySet<FinancialTransaction> agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Created ATDs for NM-WC1-PAYMENT - Law Id 191, with $10 ", 2, agencyDebits.find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")).And(FinancialTransaction.Law().LawId().equalTo("191"))).size());
        assertEquals("Number of ATDs for 941 payment template", 5,
                     agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATDs for 940 payment template", 1,
                     agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());

        //Assert for ATOs
        DomainEntitySet<FinancialTransaction> agencyOverPayments = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpayment, TransactionStateCode.Executed);
        assertEquals("Number of ATOs for 941 payment template", 5,
                     agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATOs for 940 payment template", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATO for NM-WC1-PAYMENT - Law Id 191, with $20", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());
        assertEquals("ATO for NM-WC1-PAYMENT - Law Id 192, with $5", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")))).size());

        //Assert for ERTOAs
        DomainEntitySet<FinancialTransaction> employerTaxOverPaymentsApplied = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Created);
        assertEquals("Number of ERTOAs for 941 payment template", 5,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ERTOAs for 940 payment template", 1,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        assertEquals("Executed ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10", 1,
                     FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Executed)
                                         .find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        assertEquals("Executed ER Tax Debit amount", SpcfMoney.ZERO, assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Executed, TransactionTypeCode.EmployerTaxDebit)).getFinancialTransactionAmount());

        //Adjustment Payroll ERTDb status
        PayrollRun adjustmentPayroll = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Adjustment));
        assertEquals("ER Tax Debit amount", new SpcfMoney("66"), adjustmentPayroll.getEmployerTaxDebitTransaction().getFinancialTransactionAmount());

        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 16.00),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -21.00),
                                              new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 5.00),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureLiability, 66.00),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxFutureReceivable, 66.00));

    }

    @Test
    public void test_Negative_Liability_Payroll_Void_PositivePaycheck() throws Exception {
        Company company = DataLoadServices.setupCompanyWithNegativeLiability_IRS_NM();

        String psid = company.getSourceCompanyId();
        String sourcePayrollId = PayrollRun.findFirstCompanyPayrollRun(company).getSourcePayRunId();

        DataLoadServices.setPSPDate(2011, 8, 10);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 8, 12);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        Paycheck positivePaycheck = null;
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            if (paycheck.getTaxCollection().find(Tax.Law().LawId().equalTo("1").And(Tax.TaxLiabilityAmount().equalTo(new SpcfMoney("1")))).size() == 1) {
                positivePaycheck = paycheck;
            }
        }

        //Void only positive liability paycheck
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(sourcePayrollId);
        voidPayrollDTO.setPaycheckIdList(Arrays.asList(positivePaycheck.getSourcePaycheckId()));
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        assertSuccess(voidProcessResult);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate941 = PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT");
        PaymentTemplate paymentTemplate940 = PaymentTemplate.findPaymentTemplate("IRS-940-PAYMENT");

        //Assert for ATCs
        DomainEntitySet<FinancialTransaction> agencyCredits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Created);
        assertEquals("Number of ATCs for 941 payment template", 0, agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941)).size());
        assertEquals("Number of ATCs for 940 payment template", 0, agencyCredits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940)).size());
        assertEquals("ATCs for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                     agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());
        assertEquals("ATCs for NM-WC1-PAYMENT - Law Id 192, with $10 ", 1,
                     agencyCredits.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Assert for ATDs
        DomainEntitySet<FinancialTransaction> agencyDebits = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of ATDs for 941 payment template", 0, agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941)).size());
        assertEquals("Number of ATDs for 940 payment template", 0, agencyDebits.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940)).size());
        assertEquals("ATDs for NM-WC1-PAYMENT - Law Id 191, with $10 ", 2,
                     agencyDebits.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        //Assert for ATOs
        DomainEntitySet<FinancialTransaction> agencyOverPayments = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.AgencyTaxOverpayment, TransactionStateCode.Executed);
        assertEquals("Number of ATCs for 941 payment template", 6,
                     agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("Number of ATCs for 940 payment template", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940).And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("1")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 191, with $20 ", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("20")))).size());
        assertEquals("ATC for NM-WC1-PAYMENT - Law Id 192, with $5 ", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("192").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("5")))).size());
        assertEquals("Number of ATCs for 941 payment template Law Id - 62", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("62").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("11")))).size());
        assertEquals("Number of ATCs for 941 payment template Law Id - 61", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("61").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("12")))).size());
        assertEquals("Number of ATCs for 941 payment template Law Id - 63", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("63").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("13")))).size());
        assertEquals("Number of ATCs for 941 payment template Law Id - 64", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("64").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("14")))).size());
        assertEquals("Number of ATCs for 941 payment template Law Id - 65", 1,
                     agencyOverPayments.find(FinancialTransaction.Law().LawId().equalTo("65").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("15")))).size());

        //Assert for ERTOAs
        DomainEntitySet<FinancialTransaction> employerTaxOverPaymentsApplied = FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Created);
        assertEquals("Number of ERTOAs for 941 payment template", 0,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate941)).size());
        assertEquals("Number of ERTOAs for 940 payment template", 0,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().PaymentTemplate().equalTo(paymentTemplate940)).size());
        assertEquals("ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10 ", 1,
                     employerTaxOverPaymentsApplied.find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        assertEquals("Executed ERTOA for NM-WC1-PAYMENT - Law Id 191, with $10", 1,
                     FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployerTaxOverpaymentApplied, TransactionStateCode.Executed)
                                         .find(FinancialTransaction.Law().LawId().equalTo("191").And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("10")))).size());

        payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollId);
        assertEquals("Executed ER Tax Debit amount", SpcfMoney.ZERO, assertOne(payrollRun.getFinancialTransactions(TransactionStateCode.Executed, TransactionTypeCode.EmployerTaxDebit)).getFinancialTransactionAmount());

        //Adjustment Payroll ERTDb status
        PayrollRun adjustmentPayroll = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.Adjustment));
        assertEquals("ER Tax Debit amount", SpcfMoney.ZERO, adjustmentPayroll.getEmployerTaxDebitTransaction().getFinancialTransactionAmount());

        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.assertLedgerBalances(company, new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 10.00),
                                              new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, -87.00),
                                              new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 77.00));
    }

    @Test
    public void testAchCreditTax_Offload_IgnorePayment_PSRV003219() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";
        String[] states = {"AZ"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        //Submit Payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5000", "10000", "4000", "12", "10"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();
        //Offload impound
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        //Void First payroll to update ACHCredit TaxPaymentStatus to Ignore
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun1.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        //Before Tax Offload ACHCredit payment status
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("AZ-A1-PAYMENT")).find();
        assertEquals("Number of ACHCredits", 1, moneyMovementTransactions.size());
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.getFirst();
        assertEquals("ACHCredit Payment amount", SpcfMoney.ZERO, moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("ACHCredit Payment TaxPaymentStatus", TaxPaymentStatus.Ignore, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("ACHCredit Payment Status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        assertNotNull("Offload Batch", moneyMovementTransaction.getOffloadBatch());
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 1, 12, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);

        //After Tax Offload ACHCredit payment status
        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("ACHCredit Payment amount", SpcfMoney.ZERO, moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("ACHCredit Payment Status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        assertEquals("ACHCredit Payment amount", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAchCreditTax_OnHold_Offload_IgnorePayment_PSRV003219() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";
        String[] states = {"AZ"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updatePaymentTemplateSupportedDate("AZ-A1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        //Submit Payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"1", "61", "63", "66", "5"}, new String[]{"5000", "10000", "4000", "12", "10"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        //Offload impound
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("AZ-A1-PAYMENT").setNonDirect().find());
        assertEquals("Tax Payment status before adding agent hold", TaxPaymentStatus.ReadyToSend, mmt.getTaxPaymentStatus());
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmt, PaymentOnHoldReason.Agent));
        assertEquals("Tax Payment status after adding agent hold", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        
        SpcfCalendar expectedInitiationDate = SpcfCalendar.createInstance(2011, 1, 12, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(expectedInitiationDate, -achTaxOffloadOffset);
        
        assertEquals("Tax Payment Initiation Date", expectedInitiationDate, mmt.getInitiationDate().toLocal());
        PayrollServices.commitUnitOfWork();

        //Void on 12th for 2D tax & 13th for 1D tax to make 14th settlement remains same
        DataLoadServices.setPSPDate(2011, 1, 14 - achTaxOffloadOffset);

        //Void First payroll to update ACHCredit TaxPaymentStatus to Ignore
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun1.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        //Before Tax Offload ACHCredit payment status
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("AZ-A1-PAYMENT")).find();
        assertEquals("Number of ACHCredits", 1, moneyMovementTransactions.size());
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.getFirst();
        assertEquals("ACHCredit Payment amount", SpcfMoney.ZERO, moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("ACHCredit Payment TaxPaymentStatus", TaxPaymentStatus.Ignore, moneyMovementTransaction.getTaxPaymentStatus());
        assertEquals("ACHCredit Payment Status", PaymentStatus.Created, moneyMovementTransaction.getStatus());
        assertNotNull("Offload Batch", moneyMovementTransaction.getOffloadBatch());
        PayrollServices.rollbackUnitOfWork();

        SpcfCalendar statePaymentInitiationDate = SpcfCalendar.createInstance(2011, 1, 14, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(statePaymentInitiationDate, -achTaxOffloadOffset);
        
        DataLoadServices.runOffloadTaxPayments(statePaymentInitiationDate);

        //After Tax Offload ACHCredit payment status
        PayrollServices.beginUnitOfWork();
        Application.refresh(moneyMovementTransaction);
        assertEquals("ACHCredit Payment amount", SpcfMoney.ZERO, moneyMovementTransaction.getMoneyMovementTransactionAmount());
        assertEquals("ACHCredit Payment Status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        assertEquals("ACHCredit Payment amount", TaxPaymentStatus.AcknowledgedByAgency, moneyMovementTransaction.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void test_LA_AchCreditTax_OnHold_Offload_IgnorePayment_PSRV003964() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String psid = "123456789";
        String[] states = {"LA"};
        Company company = assertOne(DataLoadServices.setupCompany(Long.parseLong(psid), 1, states, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);
        DataLoadServices.enrollEFTPS(company);
        DataLoadServices.updatePaymentTemplateSupportedDate("LA-L1-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        //Submit Payroll
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-06"), emps, new String[]{"20"}, new String[]{"5000", "10000", "4000", "12", "10"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = processResult.getResult();
        PayrollServices.commitUnitOfWork();

        //Offload impound
        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("LA-L1-PAYMENT").setNonDirect().find());
        assertEquals("Tax Payment status before adding agent hold", TaxPaymentStatus.ReadyToSend, mmt.getTaxPaymentStatus());
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmt, PaymentOnHoldReason.Agent));
        assertEquals("Tax Payment status after adding agent hold", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 1, 12);

        //Void First payroll to update ACHCredit TaxPaymentStatus to Ignore
        PayrollServices.beginUnitOfWork();
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun1.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("LA-L1-PAYMENT").setNonDirect().find());
        assertEquals("Tax Payment status before adding agent hold", TaxPaymentStatus.Ignore, mmt.getTaxPaymentStatus());
        assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(mmt, PaymentOnHoldReason.Agent));
        assertEquals("Tax Payment status after adding agent hold", TaxPaymentStatus.OnHold, mmt.getTaxPaymentStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testATRApplicationAfterFilingPeriodEnds() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.setPSPDate(2012, 1, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.enrollEFTPS(company);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "122");
        lawAmounts.put("62", "124");
        lawAmounts.put("63", "126");
        lawAmounts.put("64", "128");
        lawAmounts.put("1", "250");
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        List<Employee> employees = new ArrayList<Employee>(Employee.findEmployees(company));
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(SpcfCalendar.createInstance(2012, 1, 20, SpcfTimeZone.getLocalTimeZone())), employees, lawAmounts);
        PayrollRun payrollRun = assertSuccessResult(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(company, 2012, 1, 18);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2012, 1, 24, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        voidPayrollDTO.setPaycheckIdList(new ArrayList<String>());
        voidPayrollDTO.getPaycheckIdList().add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("$750 in ATR", new SpcfMoney("750"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        PayrollServices.rollbackUnitOfWork();

        // move to 15 days after Q2+1m
        DataLoadServices.setPSPDate(2012, 4, 4);

        // submit a payroll for the first quarter
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(SpcfCalendar.createInstance(2012, 3, 28, SpcfTimeZone.getLocalTimeZone())), employees, lawAmounts);
        payrollRun = assertSuccessResult(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        // per PSRV004017, ATR application will depend only on whether or not a TOR has been done.
        PayrollServices.beginUnitOfWork();
        assertEquals("$0 in ATR", SpcfMoney.ZERO, LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testATRApplicationForAgentAfterFilingPeriodEnds() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.setPSPDate(2012, 1, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.enrollEFTPS(company);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "122");
        lawAmounts.put("62", "124");
        lawAmounts.put("63", "126");
        lawAmounts.put("64", "128");
        lawAmounts.put("1", "250");
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        List<Employee> employees = new ArrayList<Employee>(Employee.findEmployees(company));
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(SpcfCalendar.createInstance(2012, 1, 20, SpcfTimeZone.getLocalTimeZone())), employees, lawAmounts);
        PayrollRun payrollRun = assertSuccessResult(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(company, 2012, 1, 18);
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2012, 1, 24, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        voidPayrollDTO.setPaycheckIdList(new ArrayList<String>());
        voidPayrollDTO.getPaycheckIdList().add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("$750 in ATR", new SpcfMoney("750"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 4, 4);
        // set the principal to agent
        DataLoadServices.setPrincipalToAgent();

        // submit a payroll for the first quarter
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(SpcfCalendar.createInstance(2012, 3, 28, SpcfTimeZone.getLocalTimeZone())), employees, lawAmounts);
        payrollRun = assertSuccessResult(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        // none of the ATR should have been used
        PayrollServices.beginUnitOfWork();
        assertEquals("$0 in ATR", new SpcfMoney("0"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        PayrollServices.rollbackUnitOfWork();
    }

    private DomainEntitySet<MoneyMovementTransaction> getMMTs(PayrollRun pPayroll) {
        DomainEntitySet<MoneyMovementTransaction> efeMMTs = new DomainEntitySet<MoneyMovementTransaction>();
        pPayroll = Application.refresh(pPayroll);
        for (FinancialTransaction ft : pPayroll.getFinancialTransactionCollection()) {
            if (ft.getMoneyMovementTransaction().getMoneyMovementPaymentMethod() == PaymentMethod.EFE) {
                efeMMTs.add(ft.getMoneyMovementTransaction());
            }
        }
        return efeMMTs;
    }


    private FinancialTransaction getAgencyTaxCreditForLawIdAndPayroll(String pLawId, PayrollRun pPayrollRun) {
        TransactionType transactionType = PayrollServices.entityFinder.findById(TransactionType.class, TransactionTypeCode.AgencyTaxCredit);
        TransactionState cancelledState = PayrollServices.entityFinder.findById(TransactionState.class, TransactionStateCode.Cancelled);

        Expression<FinancialTransaction> query =
                new Query<FinancialTransaction>()
                        .Where(FinancialTransaction.Company().equalTo(pPayrollRun.getCompany())
                                                   .And(FinancialTransaction.PayrollRun().equalTo(pPayrollRun))
                                                   .And(FinancialTransaction.Law().LawId().equalTo(pLawId))
                                                   .And(FinancialTransaction.TransactionType().equalTo(transactionType))
                                                   .And(FinancialTransaction.CurrentTransactionState().notEqualTo(cancelledState)));

        DomainEntitySet<FinancialTransaction> agencyTaxCredits = Application.find(FinancialTransaction.class, query);
        if (agencyTaxCredits.size() != 1) {
            throw new RuntimeException("Did not find one non-cancelled agency tax credit for lawId " + pLawId + " and payrollRun " + pPayrollRun.getId() + " as expected");
        }

        return agencyTaxCredits.get(0);
    }

    private SpcfMoney getAssociatedAgencyTxnsTotal(MoneyMovementTransaction pMMT, TransactionTypeCode pTransactionTypeCode) {
        SpcfMoney associatedAgencyTxnAmt = new SpcfMoney("0.00");
        Criterion<FinancialTransaction> torCriteria = FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(pTransactionTypeCode);

        DomainEntitySet<FinancialTransaction> financialTransactions = pMMT.getFinancialTransactionCollection();

        DomainEntitySet<FinancialTransaction> mmtAssociatedAgencyTxns = financialTransactions.find(torCriteria);

        for (FinancialTransaction finTxn : mmtAssociatedAgencyTxns) {
            associatedAgencyTxnAmt = new SpcfMoney(associatedAgencyTxnAmt.add(finTxn.getFinancialTransactionAmount()));
        }

        return associatedAgencyTxnAmt;
    }

    private void offloadAgencyTaxCredits(PaymentTemplate pPaymentTemplate) {
        for (MoneyMovementTransaction mmt : Application.<MoneyMovementTransaction>find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().equalTo(pPaymentTemplate).And(MoneyMovementTransaction.TaxPaymentStatus().equalTo(TaxPaymentStatus.ReadyToSend)))) {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(mmt.getInitiationDate());
            PayrollServices.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.EftpsPayment);
        }
    }

    private void completeERTaxDebit() {

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erTaxDebits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "123456789",
                                                               TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed);
        for (FinancialTransaction financialTransaction : erTaxDebits) {
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Completed);
        }
        PayrollServices.commitUnitOfWork();
    }

    private void completeERTaxDirectDebit() {

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erTaxDebits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "123456789",
                                                               TransactionTypeCode.EmployerTaxDirectDebit, TransactionStateCode.Executed);
        for (FinancialTransaction financialTransaction : erTaxDebits) {
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Completed);
        }
        PayrollServices.commitUnitOfWork();
    }

    private HashMap<LedgerAccountCode, String> createCompareMap(String[] pAmounts) {
        HashMap<LedgerAccountCode, String> amountMap = new HashMap<LedgerAccountCode, String>();
        amountMap.put(LedgerAccountCode.TaxCurrentLiability, pAmounts[0]);
        amountMap.put(LedgerAccountCode.TaxCurrentCash, pAmounts[1]);
        amountMap.put(LedgerAccountCode.ERPayable, pAmounts[2]);
        amountMap.put(LedgerAccountCode.AgencyTaxRefund, pAmounts[3]);
        amountMap.put(LedgerAccountCode.TaxFutureLiability, pAmounts[4]);
        amountMap.put(LedgerAccountCode.TaxFutureReceivable, pAmounts[5]);
        return amountMap;
    }


    private HashMap<LedgerAccountCode, SpcfMoney> assertLedgerBalances(PayrollRun pPayrollRun, HashMap<LedgerAccountCode, String> pAmountsToCompare) {
        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = LedgerAccount.getLedgerAccountBalances(pPayrollRun.getCompany(), pPayrollRun, taxLedgerAccounts);

        for (LedgerAccountCode ledgerAccount : ledgerBalances.keySet()) {
            assertEquals(ledgerAccount.toString(), pAmountsToCompare.get(ledgerAccount), ledgerBalances.get(ledgerAccount).toString());
        }
        return ledgerBalances;
    }


    private HashMap<LedgerAccountCode, SpcfMoney> assertLedgerBalances(ArrayList<PayrollRun> pPayrollRuns, HashMap<LedgerAccountCode, String> pAmountsToCompare) {

        HashMap<LedgerAccountCode, SpcfMoney> ledgerBalances = LedgerAccount.getLedgerAccountBalances(pPayrollRuns.get(0).getCompany(), pPayrollRuns, taxLedgerAccounts);

        for (LedgerAccountCode ledgerAccount : ledgerBalances.keySet()) {
            assertEquals(ledgerAccount.toString(), pAmountsToCompare.get(ledgerAccount), ledgerBalances.get(ledgerAccount).toString());
        }
        return ledgerBalances;
    }


    private void printLedgerBalances(HashMap<LedgerAccountCode, SpcfMoney> pLedgerBalances) {
        for (LedgerAccountCode ledgerAccount : pLedgerBalances.keySet()) {
            System.out.println(ledgerAccount.toString() + ": " + pLedgerBalances.get(ledgerAccount).toString());
        }
    }
}
