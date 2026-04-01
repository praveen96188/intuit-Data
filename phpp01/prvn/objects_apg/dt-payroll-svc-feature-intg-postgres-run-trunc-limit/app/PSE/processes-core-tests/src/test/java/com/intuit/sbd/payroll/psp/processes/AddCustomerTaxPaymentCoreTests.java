package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Jan 26, 2011
 * Time: 10:54:18 AM
 * To change this template use File | Settings | File Templates.
 */

public class AddCustomerTaxPaymentCoreTests {
    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();
    private static final String psid = "123456789";
    private static final String FIT = "1";
    private static final String FICA = "61";
    private static final String Medicare = "63";
    private static final String FUTA = "66";
    private static final String COBRA = "196";


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
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 5, SpcfTimeZone.getLocalTimeZone()));
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
    public void testInvalidCompanyParameters() {

        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();

        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(null, psid, null, companyAdjustmentSubmissionDTO, null, liabilityAdjustmentOptionsDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code                                                              
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source System Code is not specified.", message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, null, null, companyAdjustmentSubmissionDTO, null, liabilityAdjustmentOptionsDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source Company ID is not specified.", message.getMessage());

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(null, null, null, companyAdjustmentSubmissionDTO, null, liabilityAdjustmentOptionsDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 2);

        // validate error code
        message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source System Code is not specified.", message.getMessage());

        message = processResult.getMessages().get(1);
        Assert.assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Source Company ID is not specified.", message.getMessage());

    }

    @Test
    public void testCompanyDoesNotExist() {

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, "InvalidCompanyId", null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Company QBDT:InvalidCompanyId does not exist.", message.getMessage());

    }

    @Test
    public void testNullDTO() {

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, null, null, null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "5002", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "Required 'CompanyAdjustmentSubmissionDTO' input is missing or blank", message.getMessage());

    }

    @Test
    public void testInvalidDTO() {

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(null, "1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, null, liabilityAdjustmentOptionsDTO);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 1);

        // validate error code
        Message message = processResult.getMessages().get(0);
        Assert.assertEquals("Error Code:", "5001", message.getMessageCode());

        // Verify that the correct massage string has returned
        Assert.assertEquals("Error Message", "LawId has invalid value", message.getMessage());
    }


    @Test
    public void testWageAdjustmentsOnly() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("0.0"), new SpcfMoney("2727.25"), new SpcfMoney("0.0"), false);
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);
        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(false);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 1, liabilityAdjustments.size());

        PayrollRun payrollRun = null;
        for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            assertEquals("Amount", "0.00", liabilityAdjustment.getAmount().toString());
            assertEquals("Taxable Wages", "2727.25", liabilityAdjustment.getTaxableWages().toString());
            assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
            if (payrollRun == null) {
                payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
            }
            assertNotNull("Payroll Run", payrollRun);
        }
        assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure no financial transactions were created for this Payroll Run
        assertEquals("Financial Transaction Count", 0, payrollRun.getFinancialTransactionCollection().size());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testLiabilityAdjustmentsOnly() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 2, liabilityAdjustments.size());

        PayrollRun payrollRun = null;
        for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            if (liabilityAdjustment.getLaw().isFIT()) {
                assertEquals("FIT Amount", "27.20", liabilityAdjustment.getAmount().toString());
            }
            if (liabilityAdjustment.getLaw().isFICA()) {
                assertEquals("FICA Amount", "200.27", liabilityAdjustment.getAmount().toString());
            }
            assertEquals("Taxable Wages", "0.00", liabilityAdjustment.getTaxableWages().toString());
            assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
            if (payrollRun == null) {
                payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
            }
            assertNotNull("Payroll Run", payrollRun);
        }
        assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure the right financial transactions were created for this Payroll Run

        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit));
        assertEquals("Debit Transaction Count", 1, debitTransactions.size());
        FinancialTransaction debitTransaction = debitTransactions.get(0);
        assertEquals("Debit Amount", "227.47", debitTransaction.getFinancialTransactionAmount().toString());
        DomainEntitySet<FinancialTransaction> taxPaymentTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit));
        assertEquals("Agency Tax Credit Transaction Count", 2, taxPaymentTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }


    @Test
    public void testHistoricalAdjustments() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), true);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), true);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(false);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 2, liabilityAdjustments.size());

        PayrollRun payrollRun = null;
        for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            if (liabilityAdjustment.getLaw().isFIT()) {
                assertEquals("Amount", "27.20", liabilityAdjustment.getAmount().toString());
            }
            if (liabilityAdjustment.getLaw().isFICA()) {
                assertEquals("Amount", "200.27", liabilityAdjustment.getAmount().toString());
            }
            assertEquals("Taxable Wages", "0.00", liabilityAdjustment.getTaxableWages().toString());
            assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
            if (payrollRun == null) {
                payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
            }
            assertNotNull("Payroll Run", payrollRun);
        }
        assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure no financial transactions were created for this Payroll Run
        assertEquals("Financial Transaction Count", 0, payrollRun.getFinancialTransactionCollection().size());

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testMultipleLiabilityAdjustmentsSameLaw() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 2, liabilityAdjustments.size());

        PayrollRun payrollRun = null;
        LiabilityAdjustment liabilityAdjustment = liabilityAdjustments.get(0);
        assertEquals("Taxable Wages", "0.00", liabilityAdjustment.getTaxableWages().toString());
        assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
        assertNotNull("Payroll Run", payrollRun);
        assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure the right financial transactions were created for this Payroll Run

        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit));
        assertEquals("Debit Transaction Count", 1, debitTransactions.size());
        FinancialTransaction debitTransaction = debitTransactions.get(0);
        assertEquals("Debit Amount", "227.47", debitTransaction.getFinancialTransactionAmount().toString());
        DomainEntitySet<FinancialTransaction> taxPaymentTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit));
        assertEquals("Agency Tax Credit Transaction Count", 1, taxPaymentTransactions.size());
        assertEquals("Agency Tax Credit Amount", "227.47", taxPaymentTransactions.get(0).getFinancialTransactionAmount().toString());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testMultipleLiabilityAdjustments() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(Medicare, Medicare, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("10.09"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FUTA, FUTA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("100.11"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 4, liabilityAdjustments.size());

        PayrollRun payrollRun = null;
        for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            if (liabilityAdjustment.getLaw().isFIT()) {
                assertEquals("Amount", "27.20", liabilityAdjustment.getAmount().toString());
            }
            if (liabilityAdjustment.getLaw().isFICA()) {
                assertEquals("Amount", "200.27", liabilityAdjustment.getAmount().toString());
            }
            if (liabilityAdjustment.getLaw().isMED()) {
                assertEquals("Amount", "10.09", liabilityAdjustment.getAmount().toString());
            }
            if (liabilityAdjustment.getLaw().isFUTA()) {
                assertEquals("Amount", "100.11", liabilityAdjustment.getAmount().toString());
            }
            assertEquals("Taxable Wages", "0.00", liabilityAdjustment.getTaxableWages().toString());
            assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
            if (payrollRun == null) {
                payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
            }
            assertNotNull("Payroll Run", payrollRun);
        }
        assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure the right financial transactions were created for this Payroll Run

        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit));
        assertEquals("Debit Transaction Count", 1, debitTransactions.size());
        FinancialTransaction debitTransaction = debitTransactions.get(0);
        assertEquals("Debit Amount", "337.67", debitTransaction.getFinancialTransactionAmount().toString());
        DomainEntitySet<FinancialTransaction> taxPaymentTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit));
        assertEquals("Agency Tax Credit Transaction Count", 4, taxPaymentTransactions.size());
        for (FinancialTransaction taxPaymentTransaction : taxPaymentTransactions) {
            if (taxPaymentTransaction.getLaw().isFIT()) {
                assertEquals("Amount", "27.20", taxPaymentTransaction.getFinancialTransactionAmount().toString());
                // Check MMT Amount for 941 - FIT + FICA + MED
                PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, "IRS-941-PAYMENT");
                MoneyMovementTransaction mmtPayrollTaxPayment = paymentTemplate.getTaxPayment(payrollRun);
                assertEquals("MMT Amount", "237.56", mmtPayrollTaxPayment.getMoneyMovementTransactionAmount().toString());
            }
            if (taxPaymentTransaction.getLaw().isFIT()) {
                assertEquals("Amount", "27.20", taxPaymentTransaction.getFinancialTransactionAmount().toString());
            }
            if (taxPaymentTransaction.getLaw().isFICA()) {
                assertEquals("Amount", "200.27", taxPaymentTransaction.getFinancialTransactionAmount().toString());
            }
            if (taxPaymentTransaction.getLaw().isMED()) {
                assertEquals("Amount", "10.09", taxPaymentTransaction.getFinancialTransactionAmount().toString());
            }
            if (taxPaymentTransaction.getLaw().isFUTA()) {
                assertEquals("Amount", "100.11", taxPaymentTransaction.getFinancialTransactionAmount().toString());
                PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, "IRS-940-PAYMENT");
                MoneyMovementTransaction mmtPayrollTaxPayment = paymentTemplate.getTaxPayment(payrollRun);
                assertEquals("MMT Amount", "100.11", mmtPayrollTaxPayment.getMoneyMovementTransactionAmount().toString());
            }
        }

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testMultipleLiabilityAdjustmentsExistingMMT() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

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
        ProcessResult<PayrollRun> payrollRunProcessResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(payrollRunProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar mmtDueDate = SpcfCalendar.createInstance(2011, 5, 2);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO1, company, new DateDTO("2011-01-24"), emps, new String[]{"1", "61", "63", "66"}, new String[]{"6000", "2500", "3000", "4500"});
        payrollRunDTO1.setPayrollTXBatchId("Payroll_2");
        payrollRunProcessResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO1);
        assertSuccess(payrollRunProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO1, payrollRun);
        DataLoadServices.assertEFTPSPayrolls(payrollRunDTO1, payrollRun, "IRS-941-PAYMENT", mmtDueDate);
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO("2011-01-24"));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO("2011-01-24"), new SpcfMoney("27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO("2011-01-24"), new SpcfMoney("200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO("2011-01-24"), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 2, liabilityAdjustments.size());

        LiabilityAdjustment liabilityAdjustment = liabilityAdjustments.get(0);
        assertEquals("Taxable Wages", "0.00", liabilityAdjustment.getTaxableWages().toString());
        assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
        assertNotNull("Payroll Run", payrollRun);
        assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure the right financial transactions were created for this Payroll Run

        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit));
        assertEquals("Debit Transaction Count", 1, debitTransactions.size());
        FinancialTransaction debitTransaction = debitTransactions.get(0);
        assertEquals("Debit Amount", "227.47", debitTransaction.getFinancialTransactionAmount().toString());
        DomainEntitySet<FinancialTransaction> taxPaymentTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxCredit));
        assertEquals("Agency Tax Credit Transaction Count", 2, taxPaymentTransactions.size());

        // Tax transactions from the adjustments have to be linked to the same MMT created for the Tax transactions for Payroll 2
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO1.getPayrollTXBatchId());
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, "IRS-941-PAYMENT");
        MoneyMovementTransaction mmtPayrollTaxPayment = paymentTemplate.getTaxPayment(payrollRun);
        assertEquals("MMT", mmtPayrollTaxPayment, taxPaymentTransactions.get(0).getMoneyMovementTransaction());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testNetNegativeLiabilityAdjustments() throws Exception {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 3);

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-27.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-200.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        // Verify that we created an ATFPayrollsToProcess record.
        Expression <ATFPayrollsToProcess> query = new Query<ATFPayrollsToProcess>()
                .Where(ATFPayrollsToProcess.PayrollRun().equalTo(processResult.getResult().getPayrollRun()));
        DomainEntitySet<ATFPayrollsToProcess> payrollsToProcess = Application.find(ATFPayrollsToProcess.class, query);
        assertEquals("ATF Payrolls to Process", 1, payrollsToProcess.size());
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 2, liabilityAdjustments.size());

        PayrollRun payrollRun = null;
        for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
            if (liabilityAdjustment.getLaw().isFIT()) {
                assertEquals("Amount", "-27.20", liabilityAdjustment.getAmount().toString());
            }
            if (liabilityAdjustment.getLaw().isFICA()) {
                assertEquals("Amount", "-200.27", liabilityAdjustment.getAmount().toString());
            }
            assertEquals("Taxable Wages", "0.00", liabilityAdjustment.getTaxableWages().toString());
            assertEquals("Total Wages", "0.00", liabilityAdjustment.getTotalWages().toString());
            if (payrollRun == null) {
                payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, liabilityAdjustment.getPayrollRun().getId());
            }
            assertNotNull("Payroll Run", payrollRun);
        }
        assertEquals("Payroll Run Type", PayrollType.Adjustment, payrollRun.getPayrollRunType());

        // Make sure the right financial transactions were created for this Payroll Run

        DomainEntitySet<FinancialTransaction> debitTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDebit));
        assertEquals("Debit Transaction Count", 0, debitTransactions.size());
//        FinancialTransaction debitTransaction = debitTransactions.get(0);
//        assertEquals("Debit Amount", "227.47", debitTransaction.getFinancialTransactionAmount().toString());
        DomainEntitySet<FinancialTransaction> taxPaymentTransactions = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxOverpayment));
        assertEquals("Agency Tax Credit Transaction Count", 2, taxPaymentTransactions.size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAddCustomerPayment() {
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
        //assertLedgerBalances(payrollRun, ledgerBalancesToCompare);

        PayrollServices.rollbackUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110105000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();

        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        CustomerTaxPaymentDTO customerTaxPaymentDTO = new CustomerTaxPaymentDTO();
        customerTaxPaymentDTO.setPaymentDate(new DateDTO(2011, 1, 5));
        customerTaxPaymentDTO.setQuarter(1);
        customerTaxPaymentDTO.setYear(2011);
        customerTaxPaymentDTO.setPaymentTemplateId("IRS-941-PAYMENT");
        HashMap<String, BigDecimal> lawAmounts = new HashMap<String, BigDecimal>();
        lawAmounts.put(Law.FIT, new BigDecimal("200.27"));
        lawAmounts.put(Law.ERFICA, new BigDecimal("100.11"));
        customerTaxPaymentDTO.setPaymentAmounts(lawAmounts);
        assertSuccess(PayrollServices.payrollManager.addCustomerTaxPayment(SourceSystemCode.QBDT, psid, customerTaxPaymentDTO ));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentMethod[] paymentMethods = {PaymentMethod.HPDE};
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentMethods(paymentMethods).find());

        DomainEntitySet<ATFPaymentsToProcess> paymentsToProcess = Application.find(ATFPaymentsToProcess.class, ATFPaymentsToProcess.MoneyMovementTransaction().equalTo(moneyMovementTransaction));
        Assert.assertEquals("Payment to process", 2, paymentsToProcess.size());
        PayrollServices.rollbackUnitOfWork();

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


}
