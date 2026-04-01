package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedUnprocessedRequestTests;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.TaxAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadATFFinalizedPayments;
import com.intuit.sbd.payroll.psp.batchjobs.achdebitoffload.OffloadNYDTFPayments;
import com.intuit.sbd.payroll.psp.batchjobs.eftps.EdiManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.PaymentTemplateCategory;
import com.intuit.sbd.payroll.psp.domain.TaxPaymentStatus;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.PayrollSubmitTaxTests;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import junit.framework.Assert;
import org.junit.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * User: dweinberg
 * Date: Dec 15, 2010
 * Time: 11:21:53 AM
 */
public class TaxAdapterTests {

    private static final SpcfLogger logger = Application.getLogger(PayrollSubmitTaxTests.class);

    @BeforeClass
    public static void beforeClass() {
        SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        SftpFactory.setInstanceClass(Transporter.class);
    }

    @Before
    public void runBeforeEachTest() {
       PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        logger.debug("Tax Adapter tests starting");

    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testTaxTransactionsPayrollLiabilities_HappyPath() throws Throwable {
        DataLoadServices.setPSPDate(2012, 10, 1);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.MONTHLY);
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        String[] lawIds = {"61", "62", "63", "64", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "25"};
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2012-11-02"), employeeList, lawIds, amounts);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPLawTransactions> lawTransactions;
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", null, null, new Date("01/01/2012"), new Date("12/31/2012"), true);
        Assert.assertEquals("Incorrect number of law transactions", 6, lawTransactions.size());/* 5 laws + summary */
        Assert.assertEquals("Incorrect summary amount", 185.0d, lawTransactions.get(5).getCurrentTaxesSum());
        for (SAPLawTransactions lawTransaction : lawTransactions) {
            if ("1".equals(lawTransaction.getLaw().getLawId())) {
                Assert.assertEquals("Amount incorrect for Law: 1. ", 50.0d, lawTransaction.getCurrentTaxesSum());
            }
            else if ("61".equals(lawTransaction.getLaw().getLawId())) {
                Assert.assertEquals("Amount incorrect for Law: 61. ", 10.0d, lawTransaction.getCurrentTaxesSum());
            }
            else if ("62".equals(lawTransaction.getLaw().getLawId())) {
                Assert.assertEquals("Amount incorrect for Law: 62. ", 24.0d, lawTransaction.getCurrentTaxesSum());
            }
            else if ("63".equals(lawTransaction.getLaw().getLawId())) {
                Assert.assertEquals("Amount incorrect for Law: 63. ", 11.0d, lawTransaction.getCurrentTaxesSum());
            }
            else if ("64".equals(lawTransaction.getLaw().getLawId())) {
                Assert.assertEquals("Amount incorrect for Law: 64. ", 90.0d, lawTransaction.getCurrentTaxesSum());
            }
        }
    }

    @Test
    public void testTaxTransactionsPayrollLiabilities_UnhappyPaths() throws Throwable {
        /*  Prep company    */
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        /* Prepare and submit payroll   */
        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO("2010-11-22"), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        String[] lawIds = {"61", "62", "63", "64", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "25"};
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-02"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("Payroll could not be submitted. ", processResult);

        /*  Find tax transactions and test them */
        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPLawTransactions> lawTransactions = null;

        /*  Test invalid agency
* TODO: Add another agency to this company (when we support that model) and then test for that vs. IRS    */
        /*lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), null, "CAEDD", "IRS-941-PAYMENT", null, null, null, null
       , true);
assertEquals("Incorrect number of tax transactions for this agency.", 0, lawTransactions.size());*/

        /*  Test invalid payment-code    */
        /*lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "CAEDD-PAYMENT", null, null, null, null
      , true);
assertEquals("Incorrect number of tax transactions for this payment code.", 0, lawTransactions.size());*/

        /*  Test One law Id only    */
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", "61", null, new Date("1/1/2010"), new Date("12/31/2010") , true);

        assertEquals("Incorrect number of law transactions", 1, lawTransactions.size());/* 1 law */
        assertEquals("Incorrect tax amount", 10.0d, lawTransactions.get(0).getCurrentTaxesSum());

        /*  Test date combos  */
        /*  Correct quarter start and end date for this payroll */
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", null, null, new Date("9/1/2010"), new Date("12/31/2010") , true);
        assertEquals("Incorrect number of tax transactions", 6, lawTransactions.size());
        assertEquals("Incorrect tax amount", 185.0d, lawTransactions.get(5).getCurrentTaxesSum());

        /*  Incorrect quarter start and end date for this payroll */
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", null, null, new Date("1/1/2010"), new Date("3/31/2010"), true);
        assertEquals("Incorrect number of tax transactions for this quarter.", 1, lawTransactions.size());  /*  Only a summary transaction  */

        /*  Incorrect year for quarter start and end date for this payroll */
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", null, null, new Date("9/1/2009"), new Date("12/31/2009"), true);
        assertEquals("Incorrect number of tax transactions for this quarter/ year.", 1, lawTransactions.size());    /*  Only a summary transaction  */


    }

    /**
     * Tests Tax Transaction for correctly finding payroll generated with correct tax lines over 2 quarter
     */

    @Test
    public void testTaxTransactionsPayrollLiabilities_QTD() throws Throwable {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 01, 01, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 01, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-02"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);

        PayrollServices.beginUnitOfWork();
        checkDate.addMonths(1);
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), employeeList, lawIds, amounts);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);


        PayrollServices.beginUnitOfWork();
        checkDate.addMonths(-5);
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-06-02"), employeeList, lawIds, amounts);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);


        DataLoadServices.setAllPaymentTemplateProcessDates();
        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPLawTransactions> lawTransactions = null;
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", null, null,
                                                         new Date("1/1/2010"), new Date("12/31/2010"), true);


        assertEquals("Incorrect number of law transactions", 6, lawTransactions.size()); /* 5 laws (excluding AEIC) + summary*/
        assertEquals("Incorrect summary amount", 185.0d, lawTransactions.get(5).getTaxTransactions().get(0).getQTDTaxes());
        assertEquals("Incorrect summary amount", 185.0d, lawTransactions.get(5).getTaxTransactions().get(1).getQTDTaxes());
        assertEquals("Incorrect summary amount", 370.0d, lawTransactions.get(5).getTaxTransactions().get(2).getQTDTaxes());
    }

    /**
     * Tests YTD tax accumulation over a year and across years
     */
    @Test
    public void testTaxTransactionsPayrollLiabilities_YTD() throws Throwable {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-12-02"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);

        PayrollServices.beginUnitOfWork();
        checkDate.addMonths(1);
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-11-02"), employeeList, lawIds, amounts);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);


        PayrollServices.beginUnitOfWork();
        checkDate.addMonths(-10);
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2009-04-02"), employeeList, lawIds, amounts);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);

        PayrollServices.beginUnitOfWork();
        checkDate.addMonths(6);
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2009-10-02"), employeeList, lawIds, amounts);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);

        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPLawTransactions> lawTransactions = null;
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", null, null, new Date("1/1/2009"), new Date("12/31/2009") , true);
        assertEquals("Incorrect number of law transactions", 6, lawTransactions.size());
        assertEquals("Incorrect summary amount", 370.0d, lawTransactions.get(5).getCurrentTaxesSum());
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payroll", "IRS", "IRS-941-PAYMENT", null, null, new Date("1/1/2010"), new Date("12/31/2010"), true);

        assertEquals("Incorrect summary amount", 370.0d, lawTransactions.get(5).getCurrentTaxesSum());
    }

    @Test
    public void testTaxTransactionsPayrollCLAs() {

    }

    @Test
    public void testTaxTransactionsPayrollLiabilitiesVoided() {

    }

    @Test
    public void testTaxTransactionsPayrollCLAsVoided() {

    }

    @Test
    public void testTaxTransactionsPayrollRecalled() {

    }

    @Test
    public void testTaxTransactionsPayments() throws Throwable {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 2));
        PayrollServices.commitUnitOfWork();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        SpcfCalendar initialDate = PSPDate.getPSPTime();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);

        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.MONTHLY);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-02"), employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);

        PayrollServices.beginUnitOfWork();
        checkDate.addMonths(1);
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-02-02"), employeeList, lawIds, amounts);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 1));
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);

        PayrollServices.beginUnitOfWork();
        checkDate.addMonths(1);
        payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-03-02"), employeeList, lawIds, amounts);
        processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);

        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPLawTransactions> lawTransactions = null;
        lawTransactions = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), company.getSourceCompanyId(), "Payment", "IRS", "IRS-941-PAYMENT", null, null, new Date("1/1/2011"), new Date("12/31/2011") , true);
        assertEquals("Incorrect number of law transactions", 6, lawTransactions.size());/* 5 laws (excluding AEIC) + summary */
        assertEquals("Incorrect summary amount for 1st payment. ", -189.0d, lawTransactions.get(5).getTaxTransactions().get(0).getCurrentTaxes());
        assertEquals("Incorrect summary amount for 2nd payment. ", -189.0d, lawTransactions.get(5).getTaxTransactions().get(1).getCurrentTaxes());
        assertEquals("Incorrect summary amount for 3rd payment. ", -189.0d, lawTransactions.get(5).getTaxTransactions().get(2).getCurrentTaxes());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(initialDate);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testTaxTransactionsReturns() {
        //returns, recreated, etc. (all those scenarios)
    }

    @Test
    public void testTaxTransactions100KPayers() {

    }

    @Test
    public void testTaxTransactions100KPayerReturns() {

    }

    @Test
    public void testTaxTransactionsTORs() {

    }

    @Test
    public void testTaxTransactionsTORCancelled() {

    }

    @Test
    public void testTaxTransactionsHPDELiabilities() {

    }

    @Test
    public void testTaxTransactionsHPDEPayments() {

    }

    @Test
    public void testTaxTransactionsHPDEVoided() {

    }

    @Test
    public void testTaxTransactionsHPDERefunds() {

    }

    @Test
    public void testTaxTransactionsELAs() {

    }

    @Test
    public void testTaxTransactionsELAsVoided() {

    }

    @Test
    public void testTaxTransactionsQTDYTDAcrossYears() {

    }

    @Test
    public void testTaxTransactionsCOBRA() {

    }

    @Test
    public void testTaxTransactionsSupersededPayrolls() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2014, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.setPSPDate(2014, 1, 3);
        PayrollRun supersededPayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));

        //simulate superseded PR
        Application.beginUnitOfWork();
        Application.refresh(supersededPayrollRun);
        supersededPayrollRun.setPayrollRunStatus(PayrollStatus.Superseded);
        Application.commitUnitOfWork();

        ArrayList<SAPLawTransactions> taxTransactions = new TaxAdapter().findTaxTransactions(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null, null, null, null, null, new Date("01/01/2014"), new Date("3/21/2014"), true);
        assertEquals(1, taxTransactions.get(0).getTaxTransactions().size());
    }

    @Test
    public void testNegativePaycheckIdsTaxTranscations() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2014, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.setPSPDate(2014, 1, 3);
        PayrollRun supersededPayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));
        Application.beginUnitOfWork();
        int i = 1;
        for (Paycheck paycheck : supersededPayrollRun.getPaycheckCollection()) {
            Application.refresh(paycheck);
            paycheck.setSourcePaycheckId("-" + i);
            i++;
            Application.save(paycheck);
        }
        Application.commitUnitOfWork();
        ArrayList<SAPLawTransactions> taxTransactions = new TaxAdapter().findTaxTransactions(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null, null, null, null, null, new Date("01/01/2014"), new Date("3/21/2014"), true);
        assertEquals(1, taxTransactions.get(0).getTaxTransactions().size());
        assertNotSame(taxTransactions.get(0).getTaxTransactions().get(0).getPayrollRunId(), supersededPayrollRun.getId().toString());
    }

    @Test
    public void testNegativePaycheckIdsDifferentAgencySupportDatesAndYTDetQTDAmounts() throws Throwable {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        /*  Set Agency support date */
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        /*  Set up the company  */
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        List<Employee> employees = DataLoadServices.addEEs(company, 3);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-02-04"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"7000", "2500", "4000", "5000"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110204000000");
        PayrollServices.commitUnitOfWork();
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110511000000");
        PayrollServices.commitUnitOfWork();
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /*  Get payrollId for the payroll */
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "Payroll_1");
        Application.refresh(payrollRun);
        payrollRun.getPaycheckCollection().getFirst().setSourcePaycheckId("-123");

        String payrollId = payrollRun.getId().toString();
        PayrollServices.commitUnitOfWork();

        TaxAdapter adapter = new TaxAdapter();
        /*  Agency is supported prior to the payroll, so all ledger items should be returned    */
        SAPLedgerItemDetailsCriterion ledgerItemDetailsCriterion = new SAPLedgerItemDetailsCriterion(SourceSystemCode.QBDT.toString(), psid, null, payrollId, null, "IRS-941-PAYMENT", "1", false, true, true);
        ArrayList<SAPEmployeeTaxLedgerItem> items = adapter.findEmployeeLedgerItems(ledgerItemDetailsCriterion);
        assertEquals("Incorrect number of ledger items", 2, items.size());
        assertEquals("Incorrect amount for ledger item", 7000d, items.get(0).getTaxAmount());


    }



    @Test
    public void testGetEnrollmentRejections() throws Throwable {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPEnrollmentDetail> enrollmentDetails = new ArrayList<SAPEnrollmentDetail>();


        enrollmentDetails = taxAdapter.getEFTPSEnrollmentRejections();
        /*  Company is in PendingEnrollmentState, shouldn't have any rejections */
        assertEquals("No rejections expected.", 0, enrollmentDetails.size());
        /*  Move the new enrollment to PendingAcceptance state    */
        EdiManager.processEnrollments();
        /*  Reject the enrollment   */
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Rejected);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();
        /*  Get the history again   */
        enrollmentDetails = taxAdapter.getEFTPSEnrollmentRejections();

        /*  Generate another company just for the data  */
        psid = "456456456";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        EdiManager.processEnrollments();
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Rejected));
        PayrollServices.commitUnitOfWork();
        /*  and another  */
        psid = "123698745";
        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        EdiManager.processEnrollments();
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Rejected));
        PayrollServices.commitUnitOfWork();



        /*  Expecting 2 enrollments, one got cancelled for the EIN change, the other got rejected   */
        assertEquals("Incorrect number of enrollment rejections. ", 1, enrollmentDetails.size());
/*
        assertEquals("Incorrect Legal Name on the detail", "TEST_COMPANY_1", enrollmentDetails.get(0).getCompanyName());
        assertEquals("Incorrect PSID on the detail", "123456789", enrollmentDetails.get(0).getCompanyKey().getCompanyId());
        assertEquals("Incorrect EIN on the detail", "000000001", enrollmentDetails.get(0).getEin());
*/
    }

    @Test
    public void testGetEnrollmentHistory() throws Throwable {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        TaxAdapter taxAdapter = new TaxAdapter();
        SAPEftpsEnrollmentHistory enrollmentHistory = new SAPEftpsEnrollmentHistory();

        enrollmentHistory = taxAdapter.getEftpsEnrollmentsHistory("QBDT", "123456789");
        /*  Company IS in PendingEnrollmentState, shouldn't be allowed to re-enroll */
        assertFalse("Company doesn't have any enrollments.", enrollmentHistory.getEnrollments().isEmpty());
        assertFalse("Company not allowed to re-enroll.", enrollmentHistory.isCanRe_enroll());
        EdiManager.processEnrollments();
        /*  Change company EIN, should cause another enrollment to be created   */
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("987654321");
        assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();
        /*  Move the new enrollment to PendingAcceptance state    */
        EdiManager.processEnrollments();
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Rejected));
        PayrollServices.commitUnitOfWork();
        /*  Get the history again   */
        enrollmentHistory = taxAdapter.getEftpsEnrollmentsHistory("QBDT", "123456789");
        /*  Expecting 2 enrollments, one got cancelled for the EIN change, the other got rejected   */
        assertEquals("Incorrect number of enrollment history records. ", 2, enrollmentHistory.getEnrollments().size());
    }

    @Test
    public void testGetRAFEnrollmentHistory() throws Throwable {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        TaxAdapter taxAdapter = new TaxAdapter();
        SAPRAFEnrollmentHistory enrollmentHistory = new SAPRAFEnrollmentHistory();

        enrollmentHistory = taxAdapter.getRAFEnrollmentsHistory("QBDT", "123456789");
        /*  Company IS in PendingEnrollmentState, shouldn't be allowed to re-enroll */
        assertFalse("Company doesn't have any enrollments.", enrollmentHistory.getEnrollments().isEmpty());
        assertFalse("Company not allowed to re-enroll.", enrollmentHistory.isCanRe_enroll());
        EdiManager.processEnrollments();
        /*  Change company EIN, should cause another enrollment to be created   */
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("987654321");
        assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();
        /*  Move the new enrollment to PendingAcceptance state    */
        EdiManager.processEnrollments();
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Rejected));
        PayrollServices.commitUnitOfWork();
        /*  Get the history again   */
        enrollmentHistory = taxAdapter.getRAFEnrollmentsHistory("QBDT", "123456789");
        /*  Expecting 2 enrollments, one got cancelled for the EIN change, the other got rejected   */
        assertEquals("Incorrect number of enrollment history records. ", 1, enrollmentHistory.getEnrollments().size());
    }

    //todo review this test since it's probably entirely wrong
    @Test
    public void testGetRAFEnrollmentByStatus() throws Throwable {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        TaxAdapter taxAdapter = new TaxAdapter();

        //New company should be in pending enrollment state

        ArrayList<SAPRAFEnrollmentDetail> saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollment.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollments", 1, saprafEnrollmentDetails.size());
        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollmentTape.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollmentTapes", 0, saprafEnrollmentDetails.size());

        //Change the company state to PendingEnrollmentTape

        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentTape);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollmentTape.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollmentTapes", 1, saprafEnrollmentDetails.size());
        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollment.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollments", 0, saprafEnrollmentDetails.size());

        //Change the company state to PendingEnrollmentResponse by initiating tape creation or directly
        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.PendingEnrollmentResponse);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);


        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollmentResponse.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollmentResponses", 1, saprafEnrollmentDetails.size());
        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollmentTape.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollmentTapes", 0, saprafEnrollmentDetails.size());
        ArrayList<SAPEnrollmentFile> SAPEnrollmentFiles = taxAdapter.findEnrollmentFiles(RAFActionCode.Add.toString());
        assertEquals("Incorrect number of RAF EnrollmentFiles", 0, SAPEnrollmentFiles.size());

        //Assume there was an error and we need to reject the enrollment

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.rejectRAFEnrollment(company.getCurrentRAFEnrollment(), "Some thing is very wrong here!");
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);


        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.Rejected.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF Enrolls", 1, saprafEnrollmentDetails.size());
        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollmentResponse.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollmentResponses", 0, saprafEnrollmentDetails.size());

        //Assume that the rejection reason wasn't good enough and we now want to RAF enroll the company

        PayrollServices.beginUnitOfWork();
        result = PayrollServices.companyManager.updateRAFEnrollmentStatus(
                company.getSourceSystemCd(), company.getSourceCompanyId(), company.getCurrentRAFEnrollment(),
                RAFEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();
        assertSuccess("Update enrollment status result", result);

        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.Enrolled.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF Enrolls", 1, saprafEnrollmentDetails.size());
        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.Rejected.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollmentResponses", 0, saprafEnrollmentDetails.size());


        //Change company EIN, should cause another enrollment to be created
        PayrollServices.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.setFein("987654321");
        assertSuccess(PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO));
        PayrollServices.commitUnitOfWork();

        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.PendingEnrollmentResponse.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF PendingEnrollments", 0, saprafEnrollmentDetails.size());
        saprafEnrollmentDetails = taxAdapter.getRAFEnrollmentsByStatusAndCompany(new SAPRAFEnrollmentSearch(RAFEnrollmentStatus.Cancelled.toString(), null, null, null, null, null), true, -1, -1).getReturnsList();
        assertEquals("Incorrect number of RAF Cancelled", 1, saprafEnrollmentDetails.size());



    }

    @Test
    public void testInitiateReEnrollment() throws Throwable {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        TaxAdapter taxAdapter = new TaxAdapter();

        taxAdapter.initiateReEnrollment(company.getSourceSystemCd().toString(), company.getSourceCompanyId());

        /*  Company is in PendingEnrollment state, shouldn't be allowed to re-enroll */
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EftpsEnrollment> eftpsEnrollments = company.getAllEnrollments();
        assertEquals("No re-enrollments possible.", 1, eftpsEnrollments.size());
        assertEquals("Incorrect status for most recent enrollment", EftpsEnrollmentStatus.PendingEnrollment, eftpsEnrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();
        /*  Move the new enrollment to PendingAcceptance state    */

        EdiManager.processEnrollments();

        PayrollServices.beginUnitOfWork();
        eftpsEnrollments = company.getAllEnrollments();
        assertEquals("No re-enrollments possible.", 1, eftpsEnrollments.size());
        assertEquals("Incorrect status for most recent enrollment", EftpsEnrollmentStatus.PendingAcceptance, eftpsEnrollments.get(0).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        taxAdapter.initiateReEnrollment(company.getSourceSystemCd().toString(), company.getSourceCompanyId());

        PayrollServices.beginUnitOfWork();
        /*  Expect to see anther enrollment */
        eftpsEnrollments = company.getAllEnrollments();
        assertEquals("Incorrect number of enrollments", 2, eftpsEnrollments.size());
        assertEquals("Incorrect status for most recent enrollment", EftpsEnrollmentStatus.PendingEnrollment, eftpsEnrollments.get(0).getStatusCd());
        assertEquals("Incorrect status for older enrollment", EftpsEnrollmentStatus.Cancelled, eftpsEnrollments.get(1).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testAgencyInfoDTO_MultipleRates_HappyPath() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        setupLawRates(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        SAPAgencyInfoDTO agencyInfoDTO = null;
        List<SAPAgencyInfoDTO> sapAgencyInfoDTOs = new TaxAdapter().getAgencyInfoArray(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        for (SAPAgencyInfoDTO sapAgencyInfoDTO : sapAgencyInfoDTOs) {
            if(sapAgencyInfoDTO.getAgency().getAgencyId().equals("IRS")) {
                agencyInfoDTO = sapAgencyInfoDTO;
                break;
            }
        }


        assertEquals("Incorrect number of company templates. ", 2, agencyInfoDTO.getCompanyPaymentTemplates().size());

        SAPCompanyPaymentTemplate sapCompanyPaymentTemplate941 = null;
        for (SAPCompanyPaymentTemplate sapCompanyPaymentTemplate : agencyInfoDTO.getCompanyPaymentTemplates()) {
            if(sapCompanyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd().equals("IRS-941-PAYMENT")) {
                sapCompanyPaymentTemplate941 = sapCompanyPaymentTemplate;
                break;
            }
        }
        assertNotNull("SAP 941 Company payment template is null", sapCompanyPaymentTemplate941);
        assertEquals("Incorrect number of company law rates. ", 2, sapCompanyPaymentTemplate941.getLawRates().size());
        assertEquals("Incorrect name for company law. ", "Medicare EE", sapCompanyPaymentTemplate941.getLawRates().get(0).getLawName());
        assertEquals("Incorrect rate for company law. ", 5d, sapCompanyPaymentTemplate941.getLawRates().get(0).getRate());
        assertEquals("Incorrect name for company law. ", "Medicare ER", sapCompanyPaymentTemplate941.getLawRates().get(1).getLawName());
        assertEquals("Incorrect rate for company law. ", Double.NaN, sapCompanyPaymentTemplate941.getLawRates().get(1).getRate());
    }

    @Test
    public void testLawRatesHistory_HappyPath() throws Throwable {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        setupLawRates(company);
        TaxAdapter taxAdapter = new TaxAdapter();
        SAPCompanyLawRatesHistory lawRatesHistory = null;
        lawRatesHistory = taxAdapter.getCompanyLawRatesHistory(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "IRS-941-PAYMENT");
        assertEquals("Incorrect number of laws. ", 2, lawRatesHistory.getCompanyLawNames().size());
        assertEquals("Incorrect number of laws rates. ", 6, lawRatesHistory.getCompanyLawRateDetails().size());
        assertEquals("Incorrect rate for Medicare EE (Jul'10-Oct'10). ", 6d, lawRatesHistory.getCompanyLawRateDetails().get(0).getRate());
    }

    @Test
    public void testDepositFrequencyHistory_HappyPath() throws Throwable {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        setupLawRates(company);
        TaxAdapter taxAdapter = new TaxAdapter();
        ArrayList<SAPDepositFrequency> sapDepositFrequencies = taxAdapter.getDepositFrequencyHistory(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "IRS-941-PAYMENT");
        /*
        assertEquals("Incorrect number of laws. ", 2, lawRatesHistory.getCompanyLawNames().size());
        assertEquals("Incorrect number of laws rates. ", 6, lawRatesHistory.getCompanyLawRateDetails().size());
        assertEquals("Incorrect rate for Medicare EE (Jul'10-Oct'10). ", 6d, lawRatesHistory.getCompanyLawRateDetails().get(0).getRate());
        assertEquals("Incorrect rate for Medicare ER. ", -1d, lawRatesHistory.getCompanyLawRateDetails().get(5).getRate());
        */
    }

    private void setupLawRates(Company company) {
        String sourceId = "1";
        String sourceId2 = "2";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110115000000");
        CompanyLawDTO companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");

        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());

        /* Zero-th  LawRate   */
        CompanyLawRateDTO companyLawRateDTO0 = new CompanyLawRateDTO();
        companyLawRateDTO0.setEffectiveDate(new DateDTO("2010-07-01"));
        companyLawRateDTO0.setRate(6d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO0);

        /* First LawRate   */
        CompanyLawRateDTO companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2010-10-01"));
        companyLawRateDTO1.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);
        /* Second LawRate   */
        CompanyLawRateDTO companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-04-01"));
        companyLawRateDTO2.setRate(7d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO2);
        /* Third LawRate   */
        CompanyLawRateDTO companyLawRateDTO3 = new CompanyLawRateDTO();
        companyLawRateDTO3.setEffectiveDate(new DateDTO("2011-07-01"));
        companyLawRateDTO3.setRate(6d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO3);

        /*  Create another company law without any rates    */
        CompanyLawDTO companyLawDTO1 = new CompanyLawDTO();
        companyLawDTO1.setLawId("64");
        companyLawDTO1.setSourceDescription("Another Company tax");
        companyLawDTO1.setSourceId(sourceId2);
        companyLawDTO1.setStatus(PayrollItemStatus.Active);
        companyLawDTO1.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());

        /*  Process both companyLawDTOs */
        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO1);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, sourceId);
        DomainEntitySet<CompanyLawRate> companyLawRates = companyLaw.getCompanyLawRateCollection();
        assertEquals(4, companyLawRates.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110215000000");
        companyLawDTO = new CompanyLawDTO();
        companyLawDTO.setLawId("63");
        companyLawDTO.setSourceDescription("Company tax");
        companyLawDTO.setSourceId(sourceId);
        companyLawDTO.setStatus(PayrollItemStatus.Active);
        companyLawDTO.setQBDTPayrollItemInfoDTO(new QBDTPayrollItemInfoDTO());
        /* First LawRate   */
        companyLawRateDTO1 = new CompanyLawRateDTO();
        companyLawRateDTO1.setEffectiveDate(new DateDTO("2011-01-01"));
        companyLawRateDTO1.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO1);
        /* Second LawRate   */
        companyLawRateDTO2 = new CompanyLawRateDTO();
        companyLawRateDTO2.setEffectiveDate(new DateDTO("2011-04-01"));
        companyLawRateDTO2.setRate(7d);
         companyLawDTO.getRateDTOs().add(companyLawRateDTO2);
        /* Third LawRate   */
        companyLawRateDTO3 = new CompanyLawRateDTO();
        companyLawRateDTO3.setEffectiveDate(new DateDTO("2011-07-01"));
        companyLawRateDTO3.setRate(5d);
        companyLawDTO.getRateDTOs().add(companyLawRateDTO3);

        processResult = PayrollServices.companyManager.addOrUpdateCompanyLaw(company.getSourceSystemCd(), company.getSourceCompanyId(), companyLawDTO);
        assertSuccess("companyLawRateUpdated", processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testZeroDollarEFTPSPayments() throws Throwable {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        List<Employee> employees = DataLoadServices.addEEs(company, 3);

        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);


        DateDTO paycheckDate = new DateDTO("2011-01-07");
        String[] laws = new String[]{"1", "61", "63", "66"};
        String[] amounts = new String[]{"5000", "2500", "3000", "4500"};

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRun100kDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRun100kDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRun100kDTO, company, paycheckDate, employees, laws, amounts);
        payrollDTO.setPayrollTXBatchId("Payroll_01");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRun100kDTO.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        DataLoadServices.assertPayrollsEqual(payrollRun100kDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();

        // offload impounds - Payroll 1
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101203000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        TaxAdapter taxAdapter = new TaxAdapter();
        SAPSearchResults pendingPaymentsSearchResults = taxAdapter.findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", "IRS-941-PAYMENT", null, null, null, null, null, null, null, false), 0, 10, null, false);
        assertEquals("Incorrect number of pendingTaxPayments", 1, pendingPaymentsSearchResults.getTotalRecords());
        SAPPaymentTemplateQuarterPayment quarterPayment = taxAdapter.getPaymentTemplateQuarterPayment("QBDT", psid, "IRS-941-PAYMENT", "2011", "Q1");
        assertEquals("Incorrect quarter payment for Q1, 2011", 1, quarterPayment.getPendingPayments().size());
        assertEquals("Incorrect quarter payment for Q1, 2011", 31500.0d, quarterPayment.getPendingPayments().get(0).getAmount());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110405000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRun100kDTO1 = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRun100kDTO1);
        PayrollRunDTO payrollDTO1 = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRun100kDTO1, company, new DateDTO("2011-04-25"), employees, laws, amounts);
        payrollDTO1.setPayrollTXBatchId("Payroll_02");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO1);
        assertSuccess(processResult);
        PayrollRun payrollRun1 = PayrollRun.findPayrollRun(company, payrollRun100kDTO1.getPayrollTXBatchId());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun1);
        DataLoadServices.assertPayrollsEqual(payrollRun100kDTO1, payrollRun1);
        PayrollServices.commitUnitOfWork();

        pendingPaymentsSearchResults = taxAdapter.findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", "IRS-941-PAYMENT", null, null, null, null, null, null, null, false), 0, 10, null, false);
        assertEquals("Incorrect number of pendingTaxPayments", 2, pendingPaymentsSearchResults.getTotalRecords());
        quarterPayment = taxAdapter.getPaymentTemplateQuarterPayment("QBDT", psid, "IRS-941-PAYMENT", "2011", "Q1");
        assertEquals("Incorrect quarter payment for Q1, 2011", 1, quarterPayment.getPendingPayments().size());
        assertEquals("Incorrect quarter payment for Q1, 2011", 31500.0d, quarterPayment.getPendingPayments().get(0).getAmount());
        quarterPayment = taxAdapter.getPaymentTemplateQuarterPayment("QBDT", psid, "IRS-941-PAYMENT", "2011", "Q2");
        assertEquals("Incorrect quarter payment for Q2, 2011", 1, quarterPayment.getPendingPayments().size());
        assertEquals("Incorrect quarter payment for Q2, 2011", 31500.0d, quarterPayment.getPendingPayments().get(0).getAmount());

        /*  Void Payroll 1  */
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRun voidPayrollRun = PayrollRun.findPayrollRun(company, "Payroll_01");
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(voidPayrollRun.getSourcePayRunId());
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        PayrollServices.commitUnitOfWork();

        assertSuccess(voidProcessResult);
        pendingPaymentsSearchResults = taxAdapter.findTaxPayments(new SAPPaymentSearch("Pending", null, "IRS", "IRS-941-PAYMENT", null, null, null, null, null, null, null, false), 0, 10, null, false);
        assertEquals("Incorrect number of pendingTaxPayments", 1, pendingPaymentsSearchResults.getTotalRecords());
        quarterPayment = taxAdapter.getPaymentTemplateQuarterPayment("QBDT", psid, "IRS-941-PAYMENT", "2011", "Q1");
        assertEquals("Incorrect quarter payment for Q1, 2011", 0, quarterPayment.getPendingPayments().size());
        quarterPayment = taxAdapter.getPaymentTemplateQuarterPayment("QBDT", psid, "IRS-941-PAYMENT", "2011", "Q2");
        assertEquals("Incorrect quarter payment for Q2, 2011", 1, quarterPayment.getPendingPayments().size());
        assertEquals("Incorrect quarter payment for Q2, 2011", 31500.0d, quarterPayment.getPendingPayments().get(0).getAmount());
    }

    /**
     * Tests Different Agency Support Dates checking QTD & YTD Amounts
     * PSRV002387
     * @throws Throwable
     */
    @Test
    public void test_DifferentAgencySupportDatesAndYTDetQTDAmounts() throws Throwable {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        /*  Set Agency support date */
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        /*  Set up the company  */
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        List<Employee> employees = DataLoadServices.addEEs(company, 3);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-02-04"), employees, new String[]{"1", "61", "63", "66"}, new String[]{"7000", "2500", "4000", "5000"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110204000000");
        PayrollServices.commitUnitOfWork();
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110511000000");
        PayrollServices.commitUnitOfWork();
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        /*  Get payrollId for the payroll */
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, "Payroll_1");
        String payrollId = payrollRun.getId().toString();
        PayrollServices.rollbackUnitOfWork();

        TaxAdapter adapter=new TaxAdapter();
        /*  Agency is supported prior to the payroll, so all ledger items should be returned    */
        SAPLedgerItemDetailsCriterion ledgerItemDetailsCriterion = new SAPLedgerItemDetailsCriterion(SourceSystemCode.QBDT.toString(), psid, null, payrollId, null, "IRS-941-PAYMENT", "1", false, true, true);
        ArrayList<SAPEmployeeTaxLedgerItem> items = adapter.findEmployeeLedgerItems(ledgerItemDetailsCriterion);
        assertEquals("Incorrect number of ledger items", 3, items.size());
        assertEquals("Incorrect amount for ledger item",7000d,items.get(0).getTaxAmount());
    }

    /**
     * Test for Q2 and Q3 QTD & YTD Amounts validations
     * PSRV002663
     * @throws Throwable
     */
    @Test
    public void test_Q2AndQ3_2011_YTDAndQTDAmounts() throws Throwable {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        /*  Set Agency support date */
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        /*  Set up the company  */
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        List<Employee> employees = DataLoadServices.addEEs(company, 1);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.enrollEFTPS(company);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 23, SpcfTimeZone.getLocalTimeZone()));

        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-06-30"), employees, new String[]{"1", "61", "62", "63", "64"}, new String[]{"2001", "509.25", "751.77", "175.82", "175.82"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-03-31"), employees, new String[]{"1", "61", "62", "63", "64"}, new String[]{"2411", "609.01", "899.03", "210.25", "210.25"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload(PSPDate.getPSPTime());


        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 24, SpcfTimeZone.getLocalTimeZone()));
        
        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-07-06"), employees, new String[]{"1", "61", "62", "63", "64"}, new String[]{"2411", "609.01", "899.03", "210.25", "210.25"});
        payrollDTO.setPayrollTXBatchId("Payroll_3");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-07-21"), employees, new String[]{"1", "61", "62", "63", "64"}, new String[]{"2411", "609.02", "899.00", "210.25", "210.25"});
        payrollDTO.setPayrollTXBatchId("Payroll_4");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload(PSPDate.getPSPTime());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-07-29"), employees, new String[]{"1", "61", "62", "63", "64"}, new String[]{"207", "65.56", "96.78", "22.63", "22.63"});
        payrollDTO.setPayrollTXBatchId("Payroll_5");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        /*  Create & submit the payroll. Assert its success.    */
        PayrollServices.beginUnitOfWork();
        payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-07-29"), employees, new String[]{"1", "61", "62", "63", "64"}, new String[]{"12143", "1574.29", "2323.94", "543.51", "543.51"});
        payrollDTO.setPayrollTXBatchId("Payroll_6");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        DataLoadServices.runOffload(PSPDate.getPSPTime());

        TaxAdapter adapter=new TaxAdapter();

        Calendar quarterStartDate = new GregorianCalendar(2011, 6, 1);
        Calendar quarterEndDate = new GregorianCalendar(2011, 8, 31);

        ArrayList<SAPLawTransactions> sapLawTransactions = adapter.findTaxTransactions("QBDT", company.getSourceCompanyId(), null, "IRS", "IRS-941-PAYMENT", null, null, quarterStartDate.getTime(), quarterEndDate.getTime(), false );

        assertEquals("Number Law Transactions", 6, sapLawTransactions.size());

        for (SAPLawTransactions sapLawTransaction : sapLawTransactions) {
            if(sapLawTransaction.getLaw().getLawId() != null && sapLawTransaction.getLaw().getLawId().equals("61")) {
                for (SAPTaxTransaction sapTaxTransaction : sapLawTransaction.getTaxTransactions()) {
                    if(sapTaxTransaction.getQTDTaxes() == 609.01d) {
                        assertEquals("YTD Wages", 11182.60d, sapTaxTransaction.getYTDWages());
                        assertEquals("YTD Taxes", 1118.26d, sapTaxTransaction.getYTDTaxes());
                    }
                    if(sapTaxTransaction.getPaymentMethod() != null && sapTaxTransaction.getPaymentMethod().equals("EFTPS")) {
                        assertEquals("Summary YTD Wages", 33671.30d, sapTaxTransaction.getYTDWages());
                        assertEquals("Summary YTD Taxes", 3367.13d, sapTaxTransaction.getYTDTaxes());
                    }
                }
            }
        }
    }

    @Test
    public void testVoidedTOROnTaxLedger() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 18);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-20"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 24);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 2, 1);
        DataLoadServices.voidAPaycheck(payrollRun);

        DataLoadServices.setPSPDate(2013, 4, 20);
        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> torPR = PayrollServices.financialTransactionManager.addTORTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(), PaymentTemplate.IRS_941, SpcfCalendar.createInstance(2013, 3, 31, SpcfTimeZone.getLocalTimeZone()));
        assertSuccess(torPR);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 4, 21);

        PayrollServices.beginUnitOfWork();
        PayrollRun torPayroll = Application.refresh(torPR.getResult());
        for (FinancialTransaction torFT : torPayroll.getFinancialTransactions(TransactionTypeCode.AgencyRefundTOR)) {
            assertSuccess(PayrollServices.financialTransactionManager.voidTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), torFT.getId().toString()));
        }
        PayrollServices.commitUnitOfWork();

        ArrayList<SAPLawTransactions> taxTransactions = new TaxAdapter().findTaxTransactions(company.getSourceSystemCd().toString(),
                                                                                             company.getSourceCompanyId(),
                                                                                             null,
                                                                                             Agency.IRS,
                                                                                             PaymentTemplate.IRS_941,
                                                                                             null,
                                                                                             null,
                                                                                             SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2013, 1).getFirstDayOfQuarter()),
                                                                                             SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2013, 1).getLastDayOfQuarter()),
                                                                                             true);

        SAPLawTransactions fitTransactions = taxTransactions.get(0);
        assertEquals(5, fitTransactions.getTaxTransactions().size());
        SAPTaxTransaction fitTOR = fitTransactions.getTaxTransactions().get(3);
        assertEquals("Take on Return", fitTOR.getTxnDescription());
        assertEquals("Complete", fitTOR.getPaymentStatus());
        assertEquals(2.0, fitTOR.getCurrentTaxes());

        SAPTaxTransaction fitTORVoid = fitTransactions.getTaxTransactions().get(4);
        assertEquals("Take on Return", fitTORVoid.getTxnDescription());
        assertNull(fitTORVoid.getPaymentStatus());
        assertEquals(-2.0, fitTORVoid.getCurrentTaxes());

    }

    /**
     * Test for PSRV002388. Tips for voided payroll
     * @throws Throwable
     */
    @Test
    public void test_tipsForVoidedPayroll() throws Throwable {
        String psid = "123456789";
        DataLoadServices.setPSPDate(2011, 1, 5);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        List<Employee> employees = DataLoadServices.addEEs(company, 3, false, true);

        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        /*  Create first payroll and submit it  */
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAmountsAndFICATips(payrollRunDTO, company, new DateDTO("2011-01-26"), employees, new String[]{"1", "61", "62", "63", "66"}, new String[]{"500", "250", "400", "300", "450"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        String payrollRunId = payrollRun.getId().toString();
        PayrollServices.commitUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110124000000");
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110129000000");
        PayrollServices.commitUnitOfWork();

        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollDTO.getPayrollTXBatchId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
        assertSuccess(voidProcessResult);
        String voidId = voidProcessResult.getResult().getId().toString();
        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        /*  Get the voided payroll  */
        PayrollServices.commitUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAmountsAndFICATips(payrollRunDTO, company, new DateDTO("2011-02-02"), employees, new String[]{"1", "61", "62", "63", "66"}, new String[]{"500", "250", "400", "300", "450"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // offload impounds
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110131000000");
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        TaxAdapter adapter=new TaxAdapter();
        SAPLedgerItemDetailsCriterion ledgerItemDetailsCriterion = new SAPLedgerItemDetailsCriterion(SourceSystemCode.QBDT.toString(), psid, null, payrollRunId, voidId, "IRS-941-PAYMENT", "62", false, false, true);
        ArrayList<SAPEmployeeTaxLedgerItem> items= adapter.findEmployeeLedgerItems(ledgerItemDetailsCriterion);
        assertEquals("Incorrect number of tax ledger items for the voided payroll",3,items.size());
        assertEquals("Incorrect amount for FICA ER tips for the voided payroll",-200d,items.get(0).getTaxTips());
    }


    @Test
    //verify for performance changes in PSP-4463
    public void testBasicEmployeeLedgerItems() throws Throwable {
        DataLoadServices.reinitialize();
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2011, 1, 5);

        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.addEEs(company, 3, false, true);

        DataLoadServices.setPSPDate(2012, 1, 6);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 1, 10));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 3, 8);
        PayrollRun voidPayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 3, 10));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 3, 10);
        DataLoadServices.voidAPaycheck(voidPayrollRun);

        DataLoadServices.setPSPDate(2012, 5, 5);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 5, 10));


        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2012-05-10"), new ArrayList<Employee>(company.getEmployees()));
        Collection<CompanyAdjustmentSubmissionDTO> cAS = new ArrayList<CompanyAdjustmentSubmissionDTO>();
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("CAS001", new DateDTO("2012-05-10"));
        QBDTTransactionInfoDTO cATransactionInfo = DataLoadServices.createQBDTTransactionInfoDTO("CAS001");
        companyAdjustmentSubmissionDTO.setQBDTTransactionInfoDTO(cATransactionInfo);
        Collection<LiabilityAdjustmentDTO> liabilityAdjustments = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustment = DataLoadServices.createLiabilityAdjustmentDTO("61", null, new DateDTO("2012-05-10"));
        QBDTTransactionInfoDTO laTransactionInfo = DataLoadServices.createQBDTTransactionInfoDTO("LA001");
        liabilityAdjustment.setQBDTTransactionInfoDTO(laTransactionInfo);
        liabilityAdjustments.add(liabilityAdjustment);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustments);
        cAS.add(companyAdjustmentSubmissionDTO);
        payrollRunDTO.setCompanyAdjustmentSubmissionDTOs(cAS);
        assertSuccess(PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();


        DataLoadServices.setPSPDate(2012, 7, 5);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 7, 10));

        DataLoadServices.setPSPDate(2012, 9, 5);
        PayrollRun penultimatePayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 9, 10));

        DataLoadServices.setPSPDate(2012, 11, 5);
        PayrollRun lastPayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2012, 11, 10));

        SAPLedgerItemDetailsCriterion ledgerItemDetailsCriterion = new SAPLedgerItemDetailsCriterion(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null, lastPayrollRun.getId().toString(), null, "IRS-941-PAYMENT", "61", false, true, true);
        ArrayList<SAPEmployeeTaxLedgerItem> items = new TaxAdapter().findEmployeeLedgerItems(ledgerItemDetailsCriterion);

        assertEquals(6, items.size());

        Collections.sort(items, new Comparator<SAPEmployeeTaxLedgerItem>() {
            public int compare(SAPEmployeeTaxLedgerItem o1, SAPEmployeeTaxLedgerItem o2) {
                return o1.getEmployeeName().compareTo(o2.getEmployeeName());
            }
        });

        assertEmployeeTaxLedgerItem(items.get(0), 1500, 1200, 27);
        assertEmployeeTaxLedgerItem(items.get(1), 0, 6100, 610);
        assertEmployeeTaxLedgerItem(items.get(2), 0, 7320, 732);


        ledgerItemDetailsCriterion = new SAPLedgerItemDetailsCriterion(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null, penultimatePayrollRun.getId().toString(), null, "IRS-941-PAYMENT", "61", false, true, true);
        items = new TaxAdapter().findEmployeeLedgerItems(ledgerItemDetailsCriterion);

        assertEquals(6, items.size());

        Collections.sort(items, new Comparator<SAPEmployeeTaxLedgerItem>() {
            public int compare(SAPEmployeeTaxLedgerItem o1, SAPEmployeeTaxLedgerItem o2) {
                return o1.getEmployeeName().compareTo(o2.getEmployeeName());
            }
        });

        assertEmployeeTaxLedgerItem(items.get(0), 1500, 1200, 27);
        assertEmployeeTaxLedgerItem(items.get(1), 0, 4880, 488);
        assertEmployeeTaxLedgerItem(items.get(2), 0, 6100, 610);
    }

    private void assertEmployeeTaxLedgerItem(SAPEmployeeTaxLedgerItem item, double wages, double taxableWages, double taxAmount) {
        assertEquals(wages, item.getTotalWages(), 0.0001);
        assertEquals(taxableWages, item.getTaxableWages(), 0.0001);
        assertEquals(taxAmount, item.getTaxAmount(), 0.0001);
    }

    /**
     * Test:
     * Generated payrolls in different quarters. Checks to see what we get in ManualLedgerLines for QTD.
     * PSRV002401
     * 
     * @throws Throwable
     */
    @Test
    public void test_DifferentQuarterPayrollsForQTDInManualLedgerView() throws Throwable {
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 01, 05, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        List<Employee> employees = DataLoadServices.addEEs(company, 3, false, true);

        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalTaxCompanyLaws(company);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        /*  Create first payroll in Q1  */
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-26"), employees, new String[]{"1", "61", "62", "63", "66"}, new String[]{"500", "250", "400", "300", "450"});
        payrollDTO.setPayrollTXBatchId("Payroll_1");
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        String payrollRunId = payrollRun.getId().toString();
        PayrollServices.commitUnitOfWork();



//        payrollRunDTO = new PayrollRunDTO();
//        PayrollServices.beginUnitOfWork();
//        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
//        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-26"), employees, new String[]{"1", "61", "62", "63", "66"}, new String[]{"500", "250", "400", "300", "450"});
//        payrollDTO.setPayrollTXBatchId("Payroll_2");
//        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
//        assertSuccess(processResult);
//        PayrollServices.commitUnitOfWork();


        /*  offload impounds  */
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110124000000");
        PayrollServices.commitUnitOfWork();

        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


//        PayrollServices.beginUnitOfWork();
//        PSPDate.setPSPTime("20110129000000");
//        PayrollServices.commitUnitOfWork();
//
//        //Void entire payroll run
//        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
//        voidPayrollDTO.setSourcePayrollRunId(payrollDTO.getPayrollTXBatchId());
//
//        PayrollServices.beginUnitOfWork();
//        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, psid, voidPayrollDTO);
//        assertSuccess(voidProcessResult);
//        String voidId = voidProcessResult.getResult().getId().toString();
//        payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
//        PayrollServices.commitUnitOfWork();

        /*  Create second payroll in Q2 */
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110420000000");
        PayrollServices.commitUnitOfWork();

        payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-04-22"), employees, new String[]{"1", "61", "62", "63", "66"}, new String[]{"500", "250", "400", "300", "450"});
        payrollDTO.setPayrollTXBatchId("Payroll_2");
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        // offload impounds
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
//
//        TaxAdapter adapter=new TaxAdapter();
    }

    @Test
    public void test_ManualLedger_RecordCustomerPayment_NetNegative_Option1() throws Throwable {
        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("196", new Double("-1"));
        lawAmounts.put("61", new Double("2"));
        lawAmounts.put("62", new Double("3"));
        lawAmounts.put("1", new Double("-4"));
        lawAmounts.put("63", new Double("-5"));
        lawAmounts.put("64", new Double("6"));
        lawAmounts.put("200", 0.);
        lawAmounts.put("Total", new Double("1"));

        int recordingOption = 2;

        manualLedger_RecordCustomerPayment(lawAmounts, recordingOption);
    }

    @Test
    public void test_ManualLedger_RecordCustomerPayment_NetNegative_Option2() throws Throwable {
        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("196", new Double("-1"));
        lawAmounts.put("61", new Double("2"));
        lawAmounts.put("62", new Double("3"));
        lawAmounts.put("1", new Double("-4"));
        lawAmounts.put("63", new Double("-5"));
        lawAmounts.put("64", new Double("6"));
        lawAmounts.put("200", 0.);
        lawAmounts.put("Total", new Double("1"));

        int recordingOption = 3;

        manualLedger_RecordCustomerPayment(lawAmounts, recordingOption);
    }

    @Test
    public void test_ManualLedger_RecordCustomerPayment_NetPositive_Option1() throws Throwable {
        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("196", new Double("6"));
        lawAmounts.put("61", new Double("-5"));
        lawAmounts.put("62", new Double("-4"));
        lawAmounts.put("1", new Double("3"));
        lawAmounts.put("63", new Double("-2"));
        lawAmounts.put("64", new Double("1"));
        lawAmounts.put("200", 0.);
        lawAmounts.put("Total", new Double("-1"));

        int recordingOption = 2;

        manualLedger_RecordCustomerPayment(lawAmounts, recordingOption);
    }

    @Test
    public void test_ManualLedger_RecordCustomerPayment_NetPositive_Option2() throws Throwable {
        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("196", new Double("6"));
        lawAmounts.put("61", new Double("-5"));
        lawAmounts.put("62", new Double("-4"));
        lawAmounts.put("1", new Double("3"));
        lawAmounts.put("63", new Double("-2"));
        lawAmounts.put("64", new Double("1"));
        lawAmounts.put("200", 0.);
        lawAmounts.put("Total", new Double("-1"));

        int recordingOption = 3;

        manualLedger_RecordCustomerPayment(lawAmounts, recordingOption);
    }

    @Test
    public void test_ManualLedger_RecordCustomerPayment_NetPositive_FUTA_Option2() throws Throwable {

        //Run a payroll
        String psid = "123456789";
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 2, SpcfTimeZone.getLocalTimeZone()));
        
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);


        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.enrollEFTPS(company);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-07"), emps, new String[]{"61", "62", "63", "64", "65", "66", "1"}, new String[]{"6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "10"});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, processResult.getResult());


        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("66", new Double("600"));
        lawAmounts.put("Total", new Double("600"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));
        int recordingOption = 3;

        GregorianCalendar datePaid = new GregorianCalendar(2011, 7, 12);
        GregorianCalendar checkDate = new GregorianCalendar(2011, 2, 31);

        createManualLedgerEntries(lawAmounts, recordingOption, psid, "IRS-940-PAYMENT", checkDate, datePaid);
    }

    @Test
    public void test_ManualLedger_RecordCustomerPayment_NetZero_Option1() throws Throwable {
        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("196", new Double("6"));
        lawAmounts.put("61", new Double("-5"));
        lawAmounts.put("62", new Double("-4"));
        lawAmounts.put("1", new Double("3"));
        lawAmounts.put("63", new Double("0"));
        lawAmounts.put("64", new Double("0"));
        lawAmounts.put("200", 0.);
        lawAmounts.put("Total", new Double("0"));

        int recordingOption = 2;

        manualLedger_RecordCustomerPayment(lawAmounts, recordingOption);
    }

    @Test
    public void test_ManualLedger_RecordCustomerPayment_NetZero_Option2() throws Throwable {
        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("196", new Double("6"));
        lawAmounts.put("61", new Double("-5"));
        lawAmounts.put("62", new Double("-4"));
        lawAmounts.put("1", new Double("3"));
        lawAmounts.put("63", new Double("0"));
        lawAmounts.put("64", new Double("0"));
        lawAmounts.put("200", 0.);
        lawAmounts.put("Total", new Double("0"));

        int recordingOption = 3;

        manualLedger_RecordCustomerPayment(lawAmounts, recordingOption);
    }

    @Test
    public void test_ManualLedger_recordLiabilities() throws Throwable {
        HashMap<String, Double> lawAmounts = new HashMap<String, Double>();
        lawAmounts.put("61", new Double("5"));
        lawAmounts.put("62", new Double("4"));
        lawAmounts.put("1", new Double("3"));
        lawAmounts.put("63", new Double("2"));
        lawAmounts.put("64", new Double("1"));
        lawAmounts.put("200", 0.);
        lawAmounts.put("Total", new Double("15"));
        manualLedger_Liabilities(lawAmounts);
    }

    @Test
    public void testManualLedgerIntoPriorToServiceQuarter() throws Throwable {
        //verifies PSRV003666
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2007, 1, 1));

        DataLoadServices.setPSPDate(2012, 7, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        ArrayList<SAPManualLedgerTaxLine> sapManualLedgerTaxLines = new TaxAdapter().getManualLedgerLines(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "IRS-940-PAYMENT", null, CalendarUtils.convertToDate(PSPDate.getPSPTime()));
        assertOne(sapManualLedgerTaxLines).setAmount(5.);

        FlexUnitDataLoaderService.AddUsers();
        PayrollServices.beginUnitOfWork();
        PspPrincipal principal = AuthUser.findUser("AL_admin").createPrincipal();
        PayrollServices.setCurrentPrincipal(principal);
        PayrollServices.commitUnitOfWork();

        new TaxAdapter().createManualLedgerEntry(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "Liabilities/Wages", sapManualLedgerTaxLines, new Date("01/01/2012"), "Testing", 0, CalendarUtils.convertToDate(PSPDate.getPSPTime()), true);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertOne(PayrollRun.findPayrollRuns(company));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals(0,company.findPendingTaxPayments().size());
        LiabilityCheck liabilityCheck = assertOne(Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)));
        assertEquals(Company.EXCLUDE_TOKEN, liabilityCheck.getQbdtTransactionInfo().getToken());
        PayrollServices.rollbackUnitOfWork();

    }

    public void manualLedger_RecordCustomerPayment(HashMap<String, Double> pLawAmounts, int pRecordingOption) throws Throwable {
        String psid = "123456789";
        new PayrollSubmitTaxTests().testEFTPS941OnlyPayroll_With_COBRA();

        String paymentTemplateCd = "IRS-941-PAYMENT";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));

        GregorianCalendar datePaid = new GregorianCalendar(2011, 7, 12);
        GregorianCalendar checkDate = new GregorianCalendar(2011, 2, 31);

        createManualLedgerEntries(pLawAmounts, pRecordingOption, psid, paymentTemplateCd, checkDate, datePaid);
    }

    private void createManualLedgerEntries(HashMap<String, Double> pLawAmounts, int pRecordingOption, String psid, String paymentTemplateCd, GregorianCalendar checkDate, GregorianCalendar datePaid) throws Throwable {
        TaxAdapter taxAdapter = new TaxAdapter();

        ArrayList<SAPManualLedgerTaxLine> sapManualLedgerTaxLines = taxAdapter.getManualLedgerLines("QBDT", psid, paymentTemplateCd, null, datePaid.getTime());
        if(sapManualLedgerTaxLines.size() > 1) {
            sapManualLedgerTaxLines.remove(sapManualLedgerTaxLines.size()-1);   
        }

        for (SAPManualLedgerTaxLine sapManualLedgerTaxLine : sapManualLedgerTaxLines) {
            sapManualLedgerTaxLine.setAmount(pLawAmounts.getOrDefault(sapManualLedgerTaxLine.getLaw().getLawId(), 0.0));
        }

        taxAdapter.createManualLedgerEntry("QBDT", psid, "Record Customer Payment", sapManualLedgerTaxLines, checkDate.getTime(), "Testing", pRecordingOption, datePaid.getTime(), true);

        assertManualLedgerKeying(psid, paymentTemplateCd, pLawAmounts, checkDate.getTime(), datePaid.getTime(), pRecordingOption);
    }

    private void assertManualLedgerKeying(String pPsid, String pPaymentTemplateCd, HashMap<String, Double> pLawAmounts, Date pCheckDate, Date pDatePaid, int pRecordingOption) {
        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(pPaymentTemplateCd);

        SpcfMoney totalAmount = new SpcfMoney(pLawAmounts.get("Total").toString());
        if (totalAmount.isLessThan(SpcfMoney.ZERO)) {
            totalAmount = (SpcfMoney) totalAmount.negate();
        }

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).setNonHPDE().setPending().find();
        SpcfDecimal pendingBalance = SpcfMoney.ZERO;
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            pendingBalance = pendingBalance.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());
        }
        boolean isPendingBalanceExceedPayment = false;
        pendingBalance = pendingBalance.subtract(totalAmount);
        if(pendingBalance.isGreaterThan(totalAmount)) {
            isPendingBalanceExceedPayment = true;
        }

        SpcfCalendar paidDate = SpcfCalendar.createInstance(pDatePaid.getTime());
        SpcfCalendar checkDate = SpcfCalendar.createInstance(pCheckDate.getTime());
        IPaymentPeriod iPaymentPeriod = MoneyMovementTransaction.getPaymentPeriod(paymentTemplate.getPaymentTemplateCd(), "QUARTERLY", CalendarUtils.convertToRulesCalendar(checkDate));

        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplate)
                .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(totalAmount)).And(MoneyMovementTransaction.PaymentPeriodBegin().equalTo(CalendarUtils.convertToSpcfCalendar(iPaymentPeriod.getFromAccrualDate()))
                .And(MoneyMovementTransaction.PaymentPeriodEnd().equalTo(CalendarUtils.convertToSpcfCalendar(iPaymentPeriod.getToAccrualDate())))).And(MoneyMovementTransaction.InitiationDate().equalTo(paidDate)));

        assertEquals("Money Movement Transactions", 1, moneyMovementTransactions.size());

        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.get(0);

        for (String lawId : pLawAmounts.keySet()) {
            if(!lawId.equals("Total")) {
                Law law = paymentTemplate.getLawCollection().findEntity(Law.LawId().equalTo(lawId));
                Double amount = pLawAmounts.get(lawId);
                TransactionTypeCode transactionTypeCd;
                TransactionType agencyTransactionType;
                SpcfMoney ftAmount = new SpcfMoney(amount.toString());
                if(ftAmount.isZero()) {
                    continue;
                }
                if(ftAmount.isLessThan(SpcfMoney.ZERO)) {
                    ftAmount = (SpcfMoney) ftAmount.negate();
                    if (law.isCOBRA()) {
                        transactionTypeCd = TransactionTypeCode.AgencyHPDETaxPayment;
                        agencyTransactionType = isPendingBalanceExceedPayment ? TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxDebit) : TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxOverpayment);
                    } else {
                        transactionTypeCd = TransactionTypeCode.AgencyHPDETaxRefund;
                        agencyTransactionType = isPendingBalanceExceedPayment ? TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxCredit) : TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxOverpayment);
                    }
                } else {
                    if (law.isCOBRA()) {
                        transactionTypeCd = TransactionTypeCode.AgencyHPDETaxRefund;
                        agencyTransactionType = isPendingBalanceExceedPayment ? TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxOverpayment) : TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxDebit);
                    } else {
                        transactionTypeCd = TransactionTypeCode.AgencyHPDETaxPayment;
                        agencyTransactionType = isPendingBalanceExceedPayment ? TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxOverpayment) : TransactionType.findTransactionType(TransactionTypeCode.AgencyTaxDebit);
                    }
                }
                DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, pPsid, transactionTypeCd, TransactionStateCode.Completed);
                assertEquals("Financial Transaction", 1, financialTransactions.find(FinancialTransaction.FinancialTransactionAmount().equalTo(ftAmount).And(FinancialTransaction.Law().equalTo(law))
                        .And(FinancialTransaction.SettlementTypeCd().equalTo(SettlementType.HPDE)).And(FinancialTransaction.MoneyMovementTransaction().equalTo(moneyMovementTransaction))).size());
                if(pRecordingOption == 3) {
                    Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);
                    financialTransactions = Application.find(FinancialTransaction.class, FinancialTransaction.Law().equalTo(law).And(FinancialTransaction.Company().equalTo(company))
                            .And(FinancialTransaction.TransactionType().equalTo(agencyTransactionType)));
                    assertTrue("Financial Transaction", financialTransactions.size() > 0);
                }               
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    public void manualLedger_Liabilities(HashMap<String, Double> pLawAmounts) throws Throwable {
        String psid = "123456789";
        new PayrollSubmitTaxTests().testEFTPS941OnlyPayroll_With_COBRA();

        String paymentTemplateCd = "IRS-941-PAYMENT";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));

        GregorianCalendar datePaid = new GregorianCalendar(2011, 8, 12);
        GregorianCalendar checkDate = new GregorianCalendar(2011, 8, 31);

        TaxAdapter taxAdapter = new TaxAdapter();

        ArrayList<SAPManualLedgerTaxLine> sapManualLedgerTaxLines = taxAdapter.getManualLedgerLines("QBDT", psid, paymentTemplateCd, null, datePaid.getTime());
        if(sapManualLedgerTaxLines.size() > 1) {
            sapManualLedgerTaxLines.remove(sapManualLedgerTaxLines.size()-1);
        }

        for (SAPManualLedgerTaxLine sapManualLedgerTaxLine : sapManualLedgerTaxLines) {
            if(pLawAmounts.get(sapManualLedgerTaxLine.getLaw().getLawId()) != null) {
                sapManualLedgerTaxLine.setAmount(pLawAmounts.get(sapManualLedgerTaxLine.getLaw().getLawId()));
            }            
        }

        int recordingOption = 1; 
        taxAdapter.createManualLedgerEntry("QBDT", psid, "Liabilities/Wages", sapManualLedgerTaxLines, checkDate.getTime(), "Testing", recordingOption, datePaid.getTime(), true);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals("Number of payroll runs", 2, payrollRuns.size());
        PayrollRun payrollRun = payrollRuns.findEntity(PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment));
        assertEquals("Liability adjustments", 5, payrollRun.getLiabilityAdjustmentCollection().size());
        assertEquals("Financial transactions", 0, payrollRun.getFinancialTransactionCollection().size());        
        assertEquals("Adjustment PayrollRun status", PayrollStatus.Complete, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();


    }

    @Test
    public void testPaymentDetailsIncludesFutureQuarters() throws Throwable {
        Company company = setupCompany();

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 3));
        runPayroll(company, new DateDTO("2011-01-10"), "1");

        ArrayList<SAPPaymentTemplateQuarterPayment> paymentTemplateQuarters =
                new TaxAdapter().getPaymentTemplateQuarters(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), true);

        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2011", "Q1"));
        assertFalse(containsYearQuarter(paymentTemplateQuarters, "2011", "Q2"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 25));
        runPayroll(company, new DateDTO("2011-04-10"), "7");

        paymentTemplateQuarters =
                new TaxAdapter().getPaymentTemplateQuarters(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), true);

        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2011", "Q1"));
        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2011", "Q2"));
        assertFalse(containsYearQuarter(paymentTemplateQuarters, "2011", "Q3"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 12, 25));
        runPayroll(company, new DateDTO("2012-01-10"), "13");

        paymentTemplateQuarters =
                new TaxAdapter().getPaymentTemplateQuarters(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), true);

        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2011", "Q1"));
        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2011", "Q2"));
        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2011", "Q3"));
        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2011", "Q4"));
        assertTrue(containsYearQuarter(paymentTemplateQuarters, "2012", "Q1"));
        assertFalse(containsYearQuarter(paymentTemplateQuarters, "2012", "Q2"));


    }

    @Test
    public void testCreatePendingTaxRefund() throws Throwable {
        String[] states = {"HI"};
        long psid = 12345678l;
        DataLoadServices.setPSPDate(2014, 1, 1);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("HI-VP1-PAYMENT", PSPDate.getPSPTime());
        Company company = assertOne(DataLoadServices.setupCompany(psid, 1, states, PaymentTemplateCategory.Withholding));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2014, 2, 1, SpcfTimeZone.getLocalTimeZone()),
                new DateDTO(2014, 2, 5), true, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHCredit};
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("HI-VP1-PAYMENT").setPaymentMethods(paymentMethods).find());
        PayrollServices.rollbackUnitOfWork();
        assertEquals("MoneyMovementTransaction Amount before refund", new SpcfMoney("26.00"), mmt.getMoneyMovementTransactionAmount());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2014, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 5, 13, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        TaxAdapter taxAdapter = new TaxAdapter();

        taxAdapter.createPendingTaxRefund(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), mmt.getId().toString(), "Test Memo");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction refundedMMT = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("HI-VP1-PAYMENT").setPaymentMethods(paymentMethods).find());
        PayrollServices.rollbackUnitOfWork();
        assertEquals("MoneyMovementTransaction Amount after refund", new SpcfMoney("0.00"), refundedMMT.getMoneyMovementTransactionAmount());

        ArrayList<SAPLawTransactions> taxTransactions = new TaxAdapter().findTaxTransactions(company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                null,
                "HIDOT",
                "HI-VP1-PAYMENT",
                null,
                null,
                SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2014, 1).getFirstDayOfQuarter()),
                SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2014, 1).getLastDayOfQuarter()),
                true);

        assertEquals("Law type count", 1, taxTransactions.size());
        assertEquals("Transaction type", "Refund", taxTransactions.get(0).getTaxTransactions().get(1).getTxnDescription());
    }

    @Test
    public void testCreatePendingTaxRefundQuarterly() throws Throwable {
        String[] states = {"CA"};
        long psid = 12345678l;
        DataLoadServices.setPSPDate(2014, 1, 1);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(PSPDate.getPSPTime());
        Company company = assertOne(DataLoadServices.setupCompany(psid, 1, states, PaymentTemplateCategory.SUI));

        DataLoadServices.runPayrollRun(company, states, SpcfCalendar.createInstance(2014, 1, 1, SpcfTimeZone.getLocalTimeZone()),
                new DateDTO(2014, 1, 5), true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);

        PaymentMethod[] paymentMethods = new PaymentMethod[]{PaymentMethod.ACHCredit};
        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("CA-UIETT-PAYMENT").setPaymentMethods(paymentMethods).find());
        PayrollServices.rollbackUnitOfWork();
        assertEquals("MoneyMovementTransaction Amount before refund", new SpcfMoney("458.00"), mmt.getMoneyMovementTransactionAmount());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2014, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 5, 13, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        TaxAdapter taxAdapter = new TaxAdapter();

        taxAdapter.createPendingTaxRefund(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), mmt.getId().toString(), "Test Memo");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction refundedMMT = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("CA-UIETT-PAYMENT").setPaymentMethods(paymentMethods).find());
        PayrollServices.rollbackUnitOfWork();
        assertEquals("MoneyMovementTransaction Amount after refund", new SpcfMoney("0.00"), refundedMMT.getMoneyMovementTransactionAmount());

        ArrayList<SAPLawTransactions> taxTransactions = new TaxAdapter().findTaxTransactions(company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                null,
                "CAEDD",
                "CA-UIETT-PAYMENT",
                null,
                null,
                SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2014, 1).getFirstDayOfQuarter()),
                SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2014, 1).getLastDayOfQuarter()),
                true);

        assertEquals("Law type count", 3, taxTransactions.size());
        assertEquals("Transaction type", "Refund", taxTransactions.get(0).getTaxTransactions().get(1).getTxnDescription());

    }

    @Test
    public void testCreatePendingTaxRefundAfterManualLegerEntry() throws Throwable {
        String[] statesList = {"CA"};
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        // submit payroll for Wednesday
        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("6", "2000");
        lawAmounts.put("1", "5001");
        lawAmounts.put("61", "1000");
        lawAmounts.put("62", "1500");
        lawAmounts.put("63", "1000");
        lawAmounts.put("64", "1000");
        lawAmounts.put("66", "800");

        DateDTO payrollDate = new DateDTO("2012-02-08");

        DataLoadServices.setPSPDate(2012, 2, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"))
                .setDueDate(SpcfCalendar.createInstance(2012, 2, 15, SpcfTimeZone.getLocalTimeZone())).setCompany(company).setReadyToSend().find());
        assertEquals("First payroll state Payment Amount", new SpcfMoney("4134.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // submit payroll for Monday
        lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("6", "150");
        lawAmounts.put("1", "160");
        lawAmounts.put("61", "150");
        lawAmounts.put("62", "200");
        lawAmounts.put("63", "250");
        lawAmounts.put("64", "125");
        lawAmounts.put("66", "100");

        payrollDate = new DateDTO("2012-02-06");

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction secondPayrollMmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"))
                .setDueDate(SpcfCalendar.createInstance(2012, 2, 10, SpcfTimeZone.getLocalTimeZone())).setCompany(company).setReadyToSend().find());
        assertEquals("Second Payroll state Payment Amount", new SpcfMoney("434.00"), secondPayrollMmt.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        HashMap<String, Double> customerPaymentLawAmounts = new HashMap<String, Double>();
        customerPaymentLawAmounts.put("6", new Double("5"));
        customerPaymentLawAmounts.put("67", new Double("25"));
        customerPaymentLawAmounts.put("Total", new Double("30"));

        String paymentTemplateCd = "CA-PITSDI-PAYMENT";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));

        GregorianCalendar datePaid = new GregorianCalendar(2012, 1, 28);
        GregorianCalendar checkDate = new GregorianCalendar(2012, 1, 15);

        createManualLedgerEntries(customerPaymentLawAmounts, 3, company.getSourceCompanyId(), paymentTemplateCd, checkDate, datePaid);

        //Validate that payment is applied to the earliest due payment
        PayrollServices.beginUnitOfWork();
        Application.refresh(secondPayrollMmt);
        assertEquals("Second Payroll state Payment Amount", new SpcfMoney("404.00"), secondPayrollMmt.getMoneyMovementTransactionAmount());
        MoneyMovementTransaction hpdePayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"))
                .setDueDate(SpcfCalendar.createInstance(2012, 2, 28, SpcfTimeZone.getLocalTimeZone())).setCompany(company).setTaxPaymentStatuses(TaxPaymentStatus.None).find());
        assertEquals("HPDE Payment amount", new SpcfMoney("30"), hpdePayment.getMoneyMovementTransactionAmount());
        assertEquals("HPDE Payment method", PaymentMethod.HPDE, hpdePayment.getMoneyMovementPaymentMethod());

        Application.refresh(moneyMovementTransaction);
        assertEquals("Second Payroll state Payment Amount", new SpcfMoney("4134.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2012, 2, 2, SpcfTimeZone.getLocalTimeZone()));

        TaxAdapter taxAdapter = new TaxAdapter();
        taxAdapter.createPendingTaxRefund(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), secondPayrollMmt.getId().toString(), "Test Memo");

        PayrollServices.beginUnitOfWork();
        Application.refresh(secondPayrollMmt);
        assertEquals("Second Payroll amount after refund", new SpcfMoney("0.00"), secondPayrollMmt.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAmountDecimalValue(){
        assertEquals("Amount without decimal value", "10.00" ,SpcfUtils.convertToBigDecimal(new SpcfMoney("10")).toString());
        assertEquals("Amount with one decimal value", "10.00" ,SpcfUtils.convertToBigDecimal(new SpcfMoney("10.0")).toString());
        assertEquals("Amount with two decimal value", "10.00" ,SpcfUtils.convertToBigDecimal(new SpcfMoney("10.00")).toString());
        assertEquals("Amount with four decimal value", "10.00" ,SpcfUtils.convertToBigDecimal(new SpcfMoney("10.0000")).toString());
    }

    @Test
    public void testCreatePendingTaxRefundSemiWeakly() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("GA-GAV-PAYMENT", SpcfCalendar.createInstance(2011, 9, 1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 6, 1, SpcfTimeZone.getLocalTimeZone()));

        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "GA-GAV-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2011, 9, 1));
        DataLoadServices.updateACHAgentEnabledFlags(company, "GA-GAV-PAYMENT", false);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 9, 18, SpcfTimeZone.getLocalTimeZone()));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2011-09-20"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction mmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("GA-GAV-PAYMENT").find());
        SpcfUniqueId mmtId = mmt.getId();
        PayrollServices.rollbackUnitOfWork();
        assertEquals("MoneyMovementTransaction Amount before refund", new SpcfMoney("48.00"), mmt.getMoneyMovementTransactionAmount());

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2011, 9, 21, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2014, 5, 13, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        TaxAdapter taxAdapter = new TaxAdapter();
        taxAdapter.createPendingTaxRefund(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), mmt.getId().toString(), "Test Memo");

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction refundedMMT = Application.findById(MoneyMovementTransaction.class, mmtId);
        PayrollServices.rollbackUnitOfWork();
        assertEquals("MoneyMovementTransaction Amount after refund", new SpcfMoney("0.00"), refundedMMT.getMoneyMovementTransactionAmount());

        ArrayList<SAPLawTransactions> taxTransactions = new TaxAdapter().findTaxTransactions(company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                null,
                "GADOR",
                "GA-GAV-PAYMENT",
                null,
                null,
                SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2011, 3).getFirstDayOfQuarter()),
                SAPTranslator.getDateFromSpcfCalendar(new SAPQuarter(2011, 3).getLastDayOfQuarter()),
                true);

        assertEquals("Law type count", 1, taxTransactions.size());
        assertEquals("Transaction type", "Refund", taxTransactions.get(0).getTaxTransactions().get(0).getTxnDescription());

    }

    private static boolean containsYearQuarter(ArrayList<SAPPaymentTemplateQuarterPayment> paymentTemplateQuarters, String year, String quarter) {
        boolean found = false;
        for (SAPPaymentTemplateQuarterPayment paymentTemplateQuarter : paymentTemplateQuarters) {
            if (paymentTemplateQuarter.getPaymentTemplateCd().equals("IRS-941-PAYMENT")
                    && paymentTemplateQuarter.getYear().equals(year)
                    && paymentTemplateQuarter.getQuarter().equals(quarter)) {
                found = true;
            }
        }
        return found;
    }

    private Company setupCompany() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, false, true);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);
        return company;
    }

    private PayrollRun runPayroll(Company company, DateDTO date, String amount) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, new ArrayList<Employee>(company.getCloudEmployees()), new String[]{"61", "62", "63", "64", "66"}, new String[]{amount, amount, amount, amount, amount});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }

    @Test
    public void testFindEmployeesWithoutCurrentYearPaychecks() throws Exception {
        DataLoadServices.setPSPDate(2008, 1, 1);
        Company company = setupCompany();

        DataLoadServices.setPSPDate(2011,1,1);
        assertNumberOfEmployeesWithoutCurrentYearPaychecks(company, 0);
        List<Employee> ees = DataLoadServices.addEEs(company, 10);
        assertNumberOfEmployeesWithoutCurrentYearPaychecks(company, 10);
        runPayrollForEmployees(ees, new DateDTO("2011-1-1"));
        assertNumberOfEmployeesWithoutCurrentYearPaychecks(company, 0);

        DataLoadServices.setPSPDate(2011,12,31);
        assertNumberOfEmployeesWithoutCurrentYearPaychecks(company, 0);

        DataLoadServices.setPSPDate(2012,1,1);
        assertNumberOfEmployeesWithoutCurrentYearPaychecks(company, 10);
        runPayrollForEmployees(ees.subList(0, 2), new DateDTO("2011-1-1"));
        assertNumberOfEmployeesWithoutCurrentYearPaychecks(company, 10);
        runPayrollForEmployees(ees.subList(0, 2), new DateDTO("2012-2-1"));
        assertNumberOfEmployeesWithoutCurrentYearPaychecks(company, 8);

    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testRecordMultipleCustomerPayments() throws Throwable {
        DataLoadServices.setPSPDate(2012, 1, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.PendingAcceptance);
        PayrollServices.companyManager.updateEftpsEnrollment(company.getSourceSystemCd(), company.getSourceCompanyId(), EftpsEnrollmentStatus.Enrolled);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO("2012-01-05"), new ArrayList<Employee>(employees));
        List<ServiceBankAccountDTO> serviceBankAccountDTOs = new ArrayList<ServiceBankAccountDTO>();
        ServiceBankAccountDTO serviceBankAccountDTO = new ServiceBankAccountDTO();
        serviceBankAccountDTO.setCompanyBankAccount(PayrollServices.dtoFactory.create(CompanyBankAccount.findActiveCompanyBankAccount(company)));
        serviceBankAccountDTO.setServiceCode(ServiceCode.Tax);
        serviceBankAccountDTOs.add(serviceBankAccountDTO);
        payrollRunDTO.setCompanyBankAccounts(serviceBankAccountDTOs);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult payrollResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(payrollResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2012, 1, 3, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        employees = Employee.findEmployees(company);
        payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO("2012-01-12"), new ArrayList<Employee>(employees));
        payrollRunDTO.setCompanyBankAccounts(serviceBankAccountDTOs);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(payrollResult);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2012, 1, 10, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2012, 1, 18);
        PayrollServices.beginUnitOfWork();
        new ProcessACHTransactions().process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo("IRS-941-PAYMENT"));
        assertEquals("941 movementTransactions", 2, moneyMovementTransactions.size());
        //Agent hold
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertSuccess(PayrollServices.paymentManager.addTaxPaymentOnHoldReason(moneyMovementTransaction, PaymentOnHoldReason.Agent));
        }


        PayrollServices.commitUnitOfWork();

        // the two payrolls above should now two separate movementTransactions that can be recorded against
        TaxAdapter taxAdapter = new TaxAdapter();

        // record first payment
        ArrayList<SAPManualLedgerTaxLine> sapManualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        sapManualLedgerTaxLines.add(createSapManualLedgerTaxLine(50, "IRS-941-PAYMENT", "1"));
        sapManualLedgerTaxLines.add(createSapManualLedgerTaxLine(4, "IRS-941-PAYMENT", "143"));
        sapManualLedgerTaxLines.add(createSapManualLedgerTaxLine(10, "IRS-941-PAYMENT", "61"));
        sapManualLedgerTaxLines.add(createSapManualLedgerTaxLine(24, "IRS-941-PAYMENT", "62"));
        sapManualLedgerTaxLines.add(createSapManualLedgerTaxLine(11, "IRS-941-PAYMENT", "63"));
        sapManualLedgerTaxLines.add(createSapManualLedgerTaxLine(90, "IRS-941-PAYMENT", "64"));

        taxAdapter.createManualLedgerEntry(company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                "Record Customer Payment", sapManualLedgerTaxLines, new Date("01/01/2012"), "Memo", 3, new Date("01/11/2012"), true);

        // make sure that the recorded payment applied to the first payment
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                              MoneyMovementTransaction.DueDate().equalTo(SpcfCalendar.createInstance(2012, 1, 11, SpcfTimeZone.getLocalTimeZone()))
                                                                                      .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)));
        assertEquals("941 payment", 1, moneyMovementTransactions.size());
        assertEquals("payment amount", SpcfMoney.ZERO, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        assertEquals("ER Payable amount", new SpcfMoney("189.00"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable));
        assertEquals("Agency Tax Refund amount", SpcfMoney.ZERO, LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        PayrollServices.rollbackUnitOfWork();

        // record the second payment
        taxAdapter.createManualLedgerEntry(company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                                           "Record Customer Payment", sapManualLedgerTaxLines, new Date("01/01/2012"), "Memo",
                                           3,  new Date("01/18/2012"), true);


        // make the payments
        DataLoadServices.submitPayment(SpcfCalendar.createInstance(2012, 1, 18, SpcfTimeZone.getLocalTimeZone()));

        // make sure that the recorded payment applied to the second payment
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                     MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS));
        assertEquals("941 movementTransactions", 2, moneyMovementTransactions.size());
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            assertEquals("payment amount", SpcfMoney.ZERO, moneyMovementTransaction.getMoneyMovementTransactionAmount());
            assertEquals("payment status", PaymentStatus.Executed, moneyMovementTransaction.getStatus());
        }
        assertEquals("payment amount", SpcfMoney.ZERO, moneyMovementTransactions.get(0).getMoneyMovementTransactionAmount());
        assertEquals("ER Payable amount", new SpcfMoney("378.00"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERPayable));
        assertEquals("Agency Tax Refund amount", SpcfMoney.ZERO, LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.AgencyTaxRefund));
        PayrollServices.rollbackUnitOfWork();
    }

    private void assertNumberOfEmployeesWithoutCurrentYearPaychecks(Company company, int number) {
        PayrollServices.beginUnitOfWork();
        //DLS creates 2 EEs, but we won't use them, so pass in the number of employees created afterwards wo/ paychecks
        assertEquals(2+number, Employee.findEmployeesWithoutCurrentYearPaychecks(company).size());
        PayrollServices.rollbackUnitOfWork();
    }

    private void runPayrollForEmployees(List<Employee> ees, DateDTO date) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        Company company = ees.get(0).getCompany();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, date, ees, new String[]{"1"}, new String[]{"1"});
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

    }
    
    private SAPManualLedgerTaxLine createSapManualLedgerTaxLine(double pAmount, String pPaymentTemplateCd, String pLawId) {
        SAPManualLedgerTaxLine sapManualLedgerTaxLine = new SAPManualLedgerTaxLine();
        sapManualLedgerTaxLine.setAmount(pAmount);
        SAPLawItem sapLawItem = new SAPLawItem();
        sapLawItem.setLawId(pLawId);
        sapLawItem.setPaymentTemplateCd(pPaymentTemplateCd);
        sapManualLedgerTaxLine.setLaw(sapLawItem);
        return sapManualLedgerTaxLine;
    }

    @Test
    public void testGetMoneyMovementTransactionsForVerification_NullAgentTaxPayerId() throws Throwable{
        DataLoadServices.updatePaymentTemplateSupportedDate("OR-OTCWH-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "OR");
        DataLoadServices.updateAgencyTaxpayerId(company, "OR-OTCWH-PAYMENT", null);

        List<Employee> employeeList = DataLoadServices.addEEs(company, 5);

        String[] laws = {"39","61", "62", "63", "64", "66", "1"};
        String[] amounts = {"100","61", "62", "63", "64", "66", "1"};
        HashMap <String, String> lawAmounts = new HashMap<String, String>();
        for(int i=0;i<laws.length && i<amounts.length; i++)
        {
            lawAmounts.put(laws[i], amounts[i]);
        }
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(new PayrollRunDTO(), company, new DateDTO("2011-01-01"), employeeList, lawAmounts);
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.companyManager.addCheckPrintTestBatch(SourceSystemCode.QBDT, company.getSourceCompanyId());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(2011, 5, 1);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("OR-OTCWH-PAYMENT"));
        DataLoadServices.setPrincipalToAgent(OperationId.ViewFullBankAccountNumbers);
        TaxAdapter taxAdapter = new TaxAdapter();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String dateFrom = "2011/01/01";
        String dateTo = "2011/31/05";
        List<SAPPaymentForVerification> sapPaymentForVerificationList= taxAdapter.getMoneyMovementTransactionsForVerification(company.getSourceSystemCd().toString(), company.getSourceCompanyId(),
                                                               simpleDateFormat.parse(dateFrom), simpleDateFormat.parse(dateTo), "", "", "", "", "");
        assertFalse(sapPaymentForVerificationList.isEmpty());
        assertEquals("", sapPaymentForVerificationList.get(0).getTaxpayerAgencyId());
        DataLoadServices.setPSPDate(2011, 5, 1);
    }

    @Test
    public void testGetDataSyncDetails_TransmissionId() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.enrollEFTPS(company);
        List<Employee> employeeList=DataLoadServices.addEEs(company, 5);
        String laws[] = {"1","61","66", "63"};
        String amounts[] = {"1","61","66", "63"};
        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        for(int i=0;i<laws.length;i++) {
            lawAmounts.put(laws[i],amounts[i]);
        }
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRun(new PayrollRunDTO(), company, new DateDTO(2011, 1, 1), employeeList, lawAmounts);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        ProcessResult processResult_payroll=PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult_payroll);
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTO.setSystemGenerated(true);
        qbdtTransactionInfoDTO.setIsDeleted(false);
        qbdtTransactionInfoDTO.setIsDirectDeposit(true);
        qbdtTransactionInfoDTO.setOnService(true);
        qbdtTransactionInfoDTO.setOnService(true);
        qbdtTransactionInfoDTO.setOnService(true);
        LiabilityCheckDTO liabilityCheckDTO = new LiabilityCheckDTO();
        liabilityCheckDTO.setAmount(new SpcfMoney("100.00"));
        liabilityCheckDTO.setIsVoid(false);
        liabilityCheckDTO.setLiabilityCheckType(LiabilityCheckType.EmployerDebit);
        liabilityCheckDTO.setClientUpdate(true);
        liabilityCheckDTO.setTransactionDate(SpcfCalendar.createInstance(2011, 1, 1));
        liabilityCheckDTO.setLiabilityCheckLineDTOs(null);
        liabilityCheckDTO.setPeriodEndDate(SpcfCalendar.createInstance(2011, 12, 31));
        liabilityCheckDTO.setSystemModifiedToken(1L);
        liabilityCheckDTO.setSourceId("14");
        liabilityCheckDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        liabilityCheckDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
        ProcessResult processResult = PayrollServices.companyManager.addOrUpdateLiabilityCheck(company.getSourceSystemCd(), company.getSourceCompanyId(), liabilityCheckDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        TaxAdapter taxAdapter = new TaxAdapter();
        SAPSearchResults<? extends SAPDataSyncDetail> sapDataSyncDetaillist= taxAdapter.getDataSyncDetails(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "Payroll Transactions", "TransactionID", 13, 14, null, null, null, "", null, null, 100, null, false, 0);
        assertEquals(1, sapDataSyncDetaillist.getTotalRecords());
        SAPDataSyncDetailPayrollTransaction sapDataSyncPayrollTransaction = (SAPDataSyncDetailPayrollTransaction)sapDataSyncDetaillist.getReturnsList().get(0);
        assertEquals(14, sapDataSyncPayrollTransaction.getPayrollTransactionId());

    }
    @Test
    public void testGetDataSyncDetails_PaycheckShowsYTDOrPaycheck() {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2011, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        DataLoadServices.addFederalTaxCompanyLaws(company);

        DataLoadServices.enrollEFTPS(company);

        String[] laws = {"61", "62", "63", "64", "66", "1"};
        String[] amounts = {"61", "62", "63", "64", "66", "1"};
        HashMap <String, String> lawAmounts = new HashMap<String, String>();
        for(int i=0;i<laws.length && i<amounts.length; i++)
        {
            lawAmounts.put(laws[i], amounts[i]);
        }
        PayrollServices.beginUnitOfWork();
        List<Employee> employeeList = new ArrayList<Employee>(company.getCloudEmployees());
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(new PayrollRunDTO(), company, new DateDTO("2011-01-01"), employeeList, lawAmounts);
        DataLoadServices.addAssistedBankAccounts(company, payrollDTO);
        List<PaycheckDTO> paycheckList = (List<PaycheckDTO>) payrollDTO.getPaychecks();
        paycheckList.get(0).setPaycheckId("14");
        paycheckList.get(0).setIsYTDAdjustment(false);
        paycheckList.get(1).setPaycheckId("15");
        paycheckList.get(1).setIsYTDAdjustment(true);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        TaxAdapter taxAdapter = new TaxAdapter();

        SAPSearchResults<? extends SAPDataSyncDetail> sapDataSyncDetailList= taxAdapter.getDataSyncDetails(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "Paychecks", "PaycheckID", 14, 14, null, null, null, "", null, null, 100, null, false, 0);
        assertEquals(1, sapDataSyncDetailList.getTotalRecords());
        SAPDataSyncDetailPaycheck sapDataSyncDetailPaycheck = (SAPDataSyncDetailPaycheck) sapDataSyncDetailList.getReturnsList().get(0);
        assertEquals("Paycheck", sapDataSyncDetailPaycheck.getPaycheckType());

        sapDataSyncDetailList = taxAdapter.getDataSyncDetails(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "Paychecks", "PaycheckID", 15, 15, null, null, null, "", null, null, 100, null, false, 0);
        assertEquals(1, sapDataSyncDetailList.getTotalRecords());
        sapDataSyncDetailPaycheck = (SAPDataSyncDetailPaycheck) sapDataSyncDetailList.getReturnsList().get(0);
        assertEquals("YTD",sapDataSyncDetailPaycheck.getPaycheckType());
    }

    @Test
    public void testThreshold_CA_VoidResubmit_ACHCredit() throws Throwable {
        String[] statesList = {"CA"};
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));

        // submit payroll for Wednesday
        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("6", "2000");
        lawAmounts.put("1", "5001");
        lawAmounts.put("61", "1000");
        lawAmounts.put("62", "1500");
        lawAmounts.put("63", "1000");
        lawAmounts.put("64", "1000");
        lawAmounts.put("66", "800");

        DateDTO payrollDate = new DateDTO("2012-02-08");

        DataLoadServices.setPSPDate(2012, 2, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "CA-PITSDI-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"))
                                                            .setDueDate(SpcfCalendar.createInstance(2012, 2, 15, SpcfTimeZone.getLocalTimeZone())).setCompany(company).setReadyToSend().find());
        assertEquals("First payroll state Payment Amount", new SpcfMoney("4134.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        // submit payroll for Monday
        lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("6", "150");
        lawAmounts.put("1", "160");
        lawAmounts.put("61", "150");
        lawAmounts.put("62", "200");
        lawAmounts.put("63", "250");
        lawAmounts.put("64", "125");
        lawAmounts.put("66", "100");

        payrollDate = new DateDTO("2012-02-06");

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction secondPayrollMmt = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"))
                            .setDueDate(SpcfCalendar.createInstance(2012, 2, 10, SpcfTimeZone.getLocalTimeZone())).setCompany(company).setReadyToSend().find());
        assertEquals("Second Payroll state Payment Amount", new SpcfMoney("434.00"), secondPayrollMmt.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

        HashMap<String, Double> customerPaymentLawAmounts = new HashMap<String, Double>();
        customerPaymentLawAmounts.put("6", new Double("5"));
        customerPaymentLawAmounts.put("67", new Double("25"));
        customerPaymentLawAmounts.put("Total", new Double("30"));

        String paymentTemplateCd = "CA-PITSDI-PAYMENT";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 8, 11, SpcfTimeZone.getLocalTimeZone()));

        GregorianCalendar datePaid = new GregorianCalendar(2012, 1, 28);
        GregorianCalendar checkDate = new GregorianCalendar(2012, 1, 15);

        createManualLedgerEntries(customerPaymentLawAmounts, 3, company.getSourceCompanyId(), paymentTemplateCd, checkDate, datePaid);

        //Validate that payment is applied to the earliest due payment
        PayrollServices.beginUnitOfWork();
        Application.refresh(secondPayrollMmt);
        assertEquals("Second Payroll state Payment Amount", new SpcfMoney("404.00"), secondPayrollMmt.getMoneyMovementTransactionAmount());
        MoneyMovementTransaction hpdePayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"))
                            .setDueDate(SpcfCalendar.createInstance(2012, 2, 28, SpcfTimeZone.getLocalTimeZone())).setCompany(company).setTaxPaymentStatuses(TaxPaymentStatus.None).find());
        assertEquals("HPDE Payment amount", new SpcfMoney("30"), hpdePayment.getMoneyMovementTransactionAmount());
        assertEquals("HPDE Payment method", PaymentMethod.HPDE, hpdePayment.getMoneyMovementPaymentMethod());

        Application.refresh(moneyMovementTransaction);
        assertEquals("Second Payroll state Payment Amount", new SpcfMoney("4134.00"), moneyMovementTransaction.getMoneyMovementTransactionAmount());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testApplyPreviousFFCRACreditToCOVIDAdvance() throws Throwable {
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, new String[]{}, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyLaws(company, "214");

        // submit payroll, these amounts are doubled because there are 2 employees created and the amounts are per paycheck
        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("1", "5001");
        lawAmounts.put("61", "1000");
        lawAmounts.put("62", "1500");
        lawAmounts.put("63", "1000");
        lawAmounts.put("64", "1000");
        lawAmounts.put("214", "-20000");

        DateDTO payrollDate = new DateDTO("2020-05-08");

        DataLoadServices.setPSPDate(2020, 5, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, new String[]{}, lawAmounts, payrollDate);

        DataLoadServices.assertLedgerBalance(company, new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 20998.0));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 5, 8, SpcfTimeZone.getLocalTimeZone()));

        Calendar datePaid = new GregorianCalendar(2020, Calendar.MAY, 8);
        Calendar checkDate = new GregorianCalendar(2020, Calendar.MAY, 8);

        ArrayList<SAPManualLedgerTaxLine> createManualLedgerLines = new ArrayList<>();
        ArrayList<SAPManualLedgerTaxLine> manualLedgerLines = new TaxAdapter().getManualLedgerLines(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), PaymentTemplate.IRS_941, null, new Date("05/01/2020"));
        for (SAPManualLedgerTaxLine manualLedgerLine : manualLedgerLines) {
            if (manualLedgerLine.getLaw().getLawId() != null && manualLedgerLine.getLaw().getLawId().equals(Law.COVID_ADVANCE_CREDIT)) {
                assertTrue(manualLedgerLine.isCompanyLawExists());
                manualLedgerLine.setAmount(new BigDecimal("500").doubleValue());
                createManualLedgerLines.add(manualLedgerLine);
            }
        }

        assertOne(createManualLedgerLines);

        new TaxAdapter().createManualLedgerEntry("QBDT", company.getSourceCompanyId(), "Liabilities/Wages", createManualLedgerLines, checkDate.getTime(), "Testing", 0, datePaid.getTime(), true);

        // atr is reduced by the amount recorded for the covid advance
        DataLoadServices.assertLedgerBalance(company, new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 20498.0));

        // make sure a liability check wasn't created
        PayrollServices.beginUnitOfWork();
        assertOne(Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company)));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCOVIDAdvanceLargerThanAvailableATR() throws Throwable {
        int oldLimitValue = 10000;
        try {
            Application.beginUnitOfWork();
            oldLimitValue = SystemParameter.findIntValue(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT);
            SystemParameter.update(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT, String.valueOf(40000));
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        String[] statesList = new String[]{};
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyLaws(company, "214");

        // submit payroll, these amounts are doubled because there are 2 employees created and the amounts are per paycheck
        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("1", "5001");
        lawAmounts.put("61", "1000");
        lawAmounts.put("62", "1500");
        lawAmounts.put("63", "1000");
        lawAmounts.put("64", "1000");
        lawAmounts.put("214", "-20000");

        DateDTO payrollDate = new DateDTO("2020-05-08");

        DataLoadServices.setPSPDate(2020, 5, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        DataLoadServices.assertLedgerBalance(company, new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 20998.0));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 5, 8, SpcfTimeZone.getLocalTimeZone()));

        Calendar datePaid = new GregorianCalendar(2020, Calendar.MAY, 8);
        Calendar checkDate = new GregorianCalendar(2020, Calendar.MAY, 8);

        ArrayList<SAPManualLedgerTaxLine> createManualLedgerLines = new ArrayList<>();
        ArrayList<SAPManualLedgerTaxLine> manualLedgerLines = new TaxAdapter().getManualLedgerLines(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), PaymentTemplate.IRS_941, null, new Date("05/01/2020"));
        for (SAPManualLedgerTaxLine manualLedgerLine : manualLedgerLines) {
            if (manualLedgerLine.getLaw().getLawId() != null && manualLedgerLine.getLaw().getLawId().equals(Law.COVID_ADVANCE_CREDIT)) {
                manualLedgerLine.setAmount(new BigDecimal("30000.00").doubleValue());
                createManualLedgerLines.add(manualLedgerLine);
            }
        }

        assertOne(createManualLedgerLines);

        try {
            new TaxAdapter().createManualLedgerEntry("QBDT", company.getSourceCompanyId(), "Liabilities/Wages", createManualLedgerLines, checkDate.getTime(), "Testing", 0, datePaid.getTime(), true);
            org.junit.Assert.fail("Expected Exception");
        } catch (SAPException e) {
            assertEquals("Error creating manual ledger entry\n" +
                                 "Details: ERROR (5000) COVID Advance amount must be less that ATR when recording financial transactions. ATR: 20998.00\n" +
                                 "at com.intuit.sbd.payroll.psp.processes.AddLiabilityAdjustmentsCore.validate(AddLiabilityAdjustmentsCore.java:231)\n",
                         e.getMessage());
        }
        try {
            Application.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT,String.valueOf(oldLimitValue));
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

    }
    @Test
    public void testcreateManualLedgerEntryLimit() throws Throwable {
        int oldLimitValue = 10000;
        try {
            Application.beginUnitOfWork();
            oldLimitValue = SystemParameter.findIntValue(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT);
            SystemParameter.update(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT, String.valueOf(10000));
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        String[] statesList = new String[]{};
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyLaws(company, "214");

        // submit payroll, these amounts are doubled because there are 2 employees created and the amounts are per paycheck
        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("1", "5001");
        lawAmounts.put("61", "1000");
        lawAmounts.put("62", "1500");
        lawAmounts.put("63", "1000");
        lawAmounts.put("64", "1000");
        lawAmounts.put("214", "-20000");

        DateDTO payrollDate = new DateDTO("2020-05-08");

        DataLoadServices.setPSPDate(2020, 5, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        DataLoadServices.assertLedgerBalance(company, new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 20998.0));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 5, 8, SpcfTimeZone.getLocalTimeZone()));

        Calendar datePaid = new GregorianCalendar(2020, Calendar.MAY, 8);
        Calendar checkDate = new GregorianCalendar(2020, Calendar.MAY, 8);

        ArrayList<SAPManualLedgerTaxLine> createManualLedgerLines = new ArrayList<>();
        ArrayList<SAPManualLedgerTaxLine> manualLedgerLines = new TaxAdapter().getManualLedgerLines(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), PaymentTemplate.IRS_941, null, new Date("05/01/2020"));
        for (SAPManualLedgerTaxLine manualLedgerLine : manualLedgerLines) {
            if (manualLedgerLine.getLaw().getLawId() != null && manualLedgerLine.getLaw().getLawId().equals(Law.COVID_ADVANCE_CREDIT)) {
                manualLedgerLine.setAmount(new BigDecimal("30000.00").doubleValue());
                createManualLedgerLines.add(manualLedgerLine);
            }
        }

        assertOne(createManualLedgerLines);

        try {
            new TaxAdapter().createManualLedgerEntry("QBDT", company.getSourceCompanyId(), "Liabilities/Wages", createManualLedgerLines, checkDate.getTime(), "Testing", 0, datePaid.getTime(), false);
            org.junit.Assert.fail("Expected Exception");
        } catch (SAPException e) {
            assertEquals("The liability amount you've entered is higher than the permissible limit. Enter an amount less than $10000 to create the entry.",
                    e.getMessage());
        }
        try {
            Application.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.MANUAL_LEDGER_TAX_BLOCK_LIMIT,String.valueOf(oldLimitValue));
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    @Test
    public void testCOVIDAdvanceCreatedWithOtherLedgerKeying() throws Throwable {
        String[] statesList = new String[]{};
        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = assertOne(DataLoadServices.setupCompany(123456789L, 1, statesList, PaymentTemplateCategory.Withholding));
        DataLoadServices.addCompanyLaws(company, "214");

        // submit payroll, these amounts are doubled because there are 2 employees created and the amounts are per paycheck
        HashMap<String, String> lawAmounts  = new HashMap<String, String>();
        lawAmounts.put("1", "5001");
        lawAmounts.put("61", "1000");
        lawAmounts.put("62", "1500");
        lawAmounts.put("63", "1000");
        lawAmounts.put("64", "1000");
        lawAmounts.put("214", "-20000");

        DateDTO payrollDate = new DateDTO("2020-05-08");

        DataLoadServices.setPSPDate(2020, 5, 1);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY, SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company, statesList, lawAmounts, payrollDate);

        DataLoadServices.assertLedgerBalance(company, new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 20998.0));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2020, 5, 8, SpcfTimeZone.getLocalTimeZone()));

        Calendar datePaid = new GregorianCalendar(2020, Calendar.MAY, 8);
        Calendar checkDate = new GregorianCalendar(2020, Calendar.MAY, 8);

        ArrayList<SAPManualLedgerTaxLine> createManualLedgerLines = new ArrayList<>();
        ArrayList<SAPManualLedgerTaxLine> manualLedgerLines = new TaxAdapter().getManualLedgerLines(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), PaymentTemplate.IRS_941, null, new Date("05/01/2020"));
        for (SAPManualLedgerTaxLine manualLedgerLine : manualLedgerLines) {
            if (manualLedgerLine.getLaw().getLawId() != null && !Law.IRS_CREDIT_LAWS.contains(manualLedgerLine.getLaw().getLawId())) {
                manualLedgerLine.setAmount(new BigDecimal("500").doubleValue());
                createManualLedgerLines.add(manualLedgerLine);
            }
        }

        try {
            new TaxAdapter().createManualLedgerEntry("QBDT", company.getSourceCompanyId(), "Liabilities/Wages", createManualLedgerLines, checkDate.getTime(), "Testing", 0, datePaid.getTime(), true);
            org.junit.Assert.fail("Expected Exception");
        } catch (SAPException e) {
            assertEquals("Error creating manual ledger entry\n" +
                                 "Details: ERROR (5000) COVID Advance must be recorded by itself to ensure ATR is applied correctly\n" +
                                 "at com.intuit.sbd.payroll.psp.processes.AddLiabilityAdjustmentsCore.validate(AddLiabilityAdjustmentsCore.java:225)\n",
                         e.getMessage());
        }
    }

    @Test
    //This doesn't test any particular normal scenario--just several options in order ensure integration between adapter and CP doesn't change
    public void testSUIAdjustments() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NH-DES200-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadServices.updateAgencyTaxpayerId(company, "NH-DES200-PAYMENT", "000135724");
        PayrollRun payrollRun1 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        PayrollRun payrollRun2 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-27"));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-03"));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-10"));

        DataLoadServices.setPSPDate(2012, 1, 18);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 1, 25);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 2, 1);
        voidAPaycheck(payrollRun1);
        voidAPaycheck(payrollRun2);

        DataLoadServices.setPSPDate(2012, 2, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-21"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company1Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NH-DES200-PAYMENT").setCompany(company).find());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.finalizePayment(company1Payment);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company1Payment);
        Application.refresh(payrollRun1);
        Application.refresh(payrollRun2);
        ArrayList<String> fts = new ArrayList<String>();
        //(arbitrarily) splitting the SUP and its void from the first payroll and the UI void from the 2nd payroll
        DomainEntitySet<FinancialTransaction> payroll1SUPTxns = company1Payment.getFinancialTransactionCollection().find(FinancialTransaction.PayrollRun().equalTo(payrollRun1).And(FinancialTransaction.Law().LawId().equalTo("185")));
        assertEquals(2, payroll1SUPTxns.size());
        for (FinancialTransaction financialTransaction : payroll1SUPTxns) {
            fts.add(financialTransaction.getId().toString());
        }
        FinancialTransaction payroll2SUIATD = assertOne(company1Payment.getFinancialTransactionCollection().find(FinancialTransaction.PayrollRun().equalTo(payrollRun2).And(FinancialTransaction.Law().LawId().equalTo("113").And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit)))));
        fts.add(payroll2SUIATD.getId().toString());

        String psid = company1Payment.getCompany().getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();

        ArrayList<SAPLawAmount> lawAmounts = new ArrayList<SAPLawAmount>();
        SAPLawAmount lawAmount = new SAPLawAmount("113", "113");
        lawAmount.setAmount(100);
        lawAmounts.add(lawAmount);
        lawAmount = new SAPLawAmount("185", "185");
        lawAmount.setAmount(-75);
        lawAmounts.add(lawAmount);

        new TaxAdapter().editPaymentAmount(company1Payment.getId().toString(), fts, lawAmounts, "This is my memo", false, true, psid );

        PayrollServices.beginUnitOfWork();
        Application.refresh(company1Payment);
        assertEquals(new SpcfMoney("4649.00"), company1Payment.getMoneyMovementTransactionAmount());

        MoneyMovementTransaction company1HoldPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NH-DES200-PAYMENT").setCompany(company).setOnHold().find());
        assertEquals(new SpcfMoney("144.00"), company1HoldPayment.getMoneyMovementTransactionAmount());
        assertEquals("This is my memo", company1HoldPayment.getActiveOnHoldReason(PaymentOnHoldReason.Agent).getNote());

        assertEquals(new SpcfMoney("-25.00"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERSUITaxDue));

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test100PercentRefundSUIAdjustment() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NH-DES200-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadServices.updateAgencyTaxpayerId(company, "NH-DES200-PAYMENT", "000135724");
        PayrollRun payrollRun1 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        PayrollRun payrollRun2 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-27"));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-03"));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-10"));

        DataLoadServices.setPSPDate(2012, 1, 18);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 1, 25);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 2, 1);
        voidAPaycheck(payrollRun1);
        voidAPaycheck(payrollRun2);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 2, 8);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 2, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-21"));

        DataLoadServices.setPSPDate(2012, 2, 16);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company1Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NH-DES200-PAYMENT").setCompany(company).find());
        String psid = company1Payment.getCompany().getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.finalizePayment(company1Payment);

        ArrayList<SAPLawAmount> lawAmounts = new ArrayList<SAPLawAmount>();
        SAPLawAmount lawAmount = new SAPLawAmount("113", "113");
        lawAmount.setAmount(-1808.00);
        lawAmounts.add(lawAmount);
        lawAmount = new SAPLawAmount("185", "185");
        lawAmount.setAmount(-2960.00);
        lawAmounts.add(lawAmount);

        new TaxAdapter().editPaymentAmount(company1Payment.getId().toString(), new ArrayList<String>(), lawAmounts, "This is my memo", false, true, psid);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company1Payment);
        assertEquals(SpcfMoney.ZERO, company1Payment.getMoneyMovementTransactionAmount());
        assertEquals(TaxPaymentStatus.ATFFinalized, company1Payment.getTaxPaymentStatus());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    //PSRV003406
    public void testNegativeSUIAdjustments() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NH-DES200-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));


        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updateAgencyTaxpayerId(company, "NH-DES200-PAYMENT", "000135724");
        DataLoadServices.setPSPDate(2012, 1, 10);
        PayrollRun payrollRun1 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-03"));

        DataLoadServices.setPSPDate(2012, 1, 18);
        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company1Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NH-DES200-PAYMENT").setCompany(company).find());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.finalizePayment(company1Payment);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company1Payment);
        Application.refresh(payrollRun1);
        ArrayList<String> fts = new ArrayList<String>();
        String psid = company1Payment.getCompany().getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();
        // SUI adjustment of $1000
        ArrayList<SAPLawAmount> lawAmounts = new ArrayList<SAPLawAmount>();
        SAPLawAmount lawAmount = new SAPLawAmount("113", "113");
        lawAmount.setAmount(1000);
        lawAmounts.add(lawAmount);
        new TaxAdapter().editPaymentAmount(company1Payment.getId().toString(), new ArrayList<String>(), lawAmounts, "This is my memo", false, true, psid);
        // SUI adjustment of -$1000
        ArrayList<SAPLawAmount> lawAmounts1 = new ArrayList<SAPLawAmount>();
        SAPLawAmount lawAmount1 = new SAPLawAmount("113", "113");
        lawAmount1.setAmount(-1000);
        lawAmounts1.add(lawAmount1);
        new TaxAdapter().editPaymentAmount(company1Payment.getId().toString(), new ArrayList<String>(), lawAmounts1, "This is my memo", false, true, psid);

        assertEquals(new SpcfMoney("2384.00"), company1Payment.getMoneyMovementTransactionAmount());

    }

    @Test
    public void testSUIAdjustmentsAfterSplit_AddsToCorrectMMT() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NH-DES200-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadServices.updateAgencyTaxpayerId(company, "NH-DES200-PAYMENT", "000135724");
        PayrollRun payrollRun1 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        PayrollRun payrollRun2 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-27"));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-03"));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-10"));

        DataLoadServices.setPSPDate(2012, 1, 18);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 1, 25);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 2, 1);
        voidAPaycheck(payrollRun1);
        voidAPaycheck(payrollRun2);

        DataLoadServices.setPSPDate(2012, 2, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-02-21"));

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company1Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NH-DES200-PAYMENT").setCompany(company).find());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.finalizePayment(company1Payment);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company1Payment);
        Application.refresh(payrollRun1);
        Application.refresh(payrollRun2);
        ArrayList<String> fts = new ArrayList<String>();
        //(arbitrarily) splitting the SUP and its void from the first payroll and the UI void from the 2nd payroll
        DomainEntitySet<FinancialTransaction> payroll1SUPTxns = company1Payment.getFinancialTransactionCollection().find(FinancialTransaction.PayrollRun().equalTo(payrollRun1).And(FinancialTransaction.Law().LawId().equalTo("185")));
        assertEquals(2, payroll1SUPTxns.size());
        for (FinancialTransaction financialTransaction : payroll1SUPTxns) {
            fts.add(financialTransaction.getId().toString());
        }
        FinancialTransaction payroll2SUIATD = assertOne(company1Payment.getFinancialTransactionCollection().find(FinancialTransaction.PayrollRun().equalTo(payrollRun2).And(FinancialTransaction.Law().LawId().equalTo("113").And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyTaxDebit)))));
        fts.add(payroll2SUIATD.getId().toString());
        String psid = company1Payment.getCompany().getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();

        ArrayList<SAPLawAmount> lawAmounts = new ArrayList<SAPLawAmount>();

        new TaxAdapter().editPaymentAmount(company1Payment.getId().toString(), fts, lawAmounts, "This is my memo", false, true, psid);

        SAPLawAmount lawAmount = new SAPLawAmount("113", "113");
        lawAmount.setAmount(100);
        lawAmounts.add(lawAmount);
        lawAmount = new SAPLawAmount("185", "185");
        lawAmount.setAmount(-75);
        lawAmounts.add(lawAmount);

        fts.removeAll(fts);

        new TaxAdapter().editPaymentAmount(company1Payment.getId().toString(), fts, lawAmounts, "This is my memo", false, true, company1Payment.getCompany().getSourceCompanyId());

        PayrollServices.beginUnitOfWork();
        Application.refresh(company1Payment);
        assertEquals(new SpcfMoney("4649.00"), company1Payment.getMoneyMovementTransactionAmount());

        MoneyMovementTransaction company1HoldPayment = assertOne(MoneyMovementTransaction.findTaxPayments().setPaymentTemplateCd("NH-DES200-PAYMENT").setCompany(company).setOnHold().find());
        assertEquals(new SpcfMoney("144.00"), company1HoldPayment.getMoneyMovementTransactionAmount());
        assertEquals("This is my memo", company1HoldPayment.getActiveOnHoldReason(PaymentOnHoldReason.Agent).getNote());

        assertEquals(new SpcfMoney("-25.00"), LedgerAccount.getLedgerAccountBalance(company, LedgerAccountCode.ERSUITaxDue));

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testInitiateRepaymentAllRecreate() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();
        Company company3 = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateRequiredIDs(company, null, false);
        DataLoadServices.updateRequiredIDs(company2, null, false);
        DataLoadServices.updateRequiredIDs(company3, null, false);

        DataLoadServices.invalidateDepositFrequencies(company, "MS-M89-PAYMENT");
        DataLoadServices.invalidateDepositFrequencies(company2, "MS-M89-PAYMENT");
        DataLoadServices.invalidateDepositFrequencies(company3, "MS-M89-PAYMENT");

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MS-M89-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company2.getSourceCompanyId(), "MS-M89-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company3.getSourceCompanyId(), "MS-M89-PAYMENT", DepositFrequencyCode.MONTHLY, SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO("2012-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company3, new DateDTO("2012-01-20"));

        DataLoadServices.setPSPDate(2012, 2, 13);
        BatchJobManager.runJob(BatchJobType.PrintedCheckBatch);

        DataLoadServices.setPSPDate(2012, 2, 14);
        DataLoadServices.returnAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("MS-M89-PAYMENT"));

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "MS-M89-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company2.getSourceCompanyId(), "MS-M89-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1));
        DataLoadServices.updateEffectiveDepositFreqEffDate(company3.getSourceCompanyId(), "MS-M89-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.updateRequiredIDs(company, null, true);
        DataLoadServices.updateRequiredIDs(company2, null, true);
        DataLoadServices.updateRequiredIDs(company3, null, true);

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company1Payment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MS-M89-PAYMENT").setRejectedOrReturned().find());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 2, 15);
        SAPTaxRepaymentOptions options = new SAPTaxRepaymentOptions();
        options.setUpdateAll(true);
        options.setRecreate(true);
        new TaxAdapter().initiateRepayment(company1Payment.getId().toString(), options, company.getSourceCompanyId());

        PayrollServices.beginUnitOfWork();
        MoneyMovementTransaction company1Repayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company).setPaymentTemplateCd("MS-M89-PAYMENT").setPending().find());
        MoneyMovementTransaction company2Repayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company2).setPaymentTemplateCd("MS-M89-PAYMENT").setPending().find());
        MoneyMovementTransaction company3Repayment = assertOne(MoneyMovementTransaction.findTaxPayments().setCompany(company3).setPaymentTemplateCd("MS-M89-PAYMENT").setPending().find());
        assertEquals(DepositFrequencyCode.QUARTERLY, company1Repayment.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals(DepositFrequencyCode.QUARTERLY, company2Repayment.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals(DepositFrequencyCode.QUARTERLY, company3Repayment.getPaymentFrequency().getPaymentFrequencyId());
        assertEquals("1224-5678", company1Repayment.getAgencyTaxpayerId());
        assertEquals("1224-5678", company2Repayment.getAgencyTaxpayerId());
        assertEquals("1224-5678", company3Repayment.getAgencyTaxpayerId());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRecordNegativeCustomerPaymentCreatesCorrectTransactions() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));

        DataLoadServices.setPSPDate(2012, 1, 18);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 1, 19);

        ArrayList<SAPManualLedgerTaxLine> manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        SAPManualLedgerTaxLine sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        SAPLawItem sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("62");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(-100);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        new TaxAdapter().createManualLedgerEntry(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                "Record Customer Payment",
                manualLedgerTaxLines,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2012, 3, 31)),
                "This is my memo",
                3,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2012, 1, 1)), true);


        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment)));
        FinancialTransaction atc = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit));
        assertEquals(new SpcfMoney("100.00"), atc.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, atc.getCurrentTransactionState().getTransactionStateCd());

        FinancialTransaction erd = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("100.00"), erd.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, erd.getCurrentTransactionState().getTransactionStateCd());

        assertEquals(PayrollStatus.Pending, payrollRun.getPayrollRunStatus());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRecordNetNegativeCustomerPaymentCreatesCorrectTransactions() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));

        DataLoadServices.setPSPDate(2012, 1, 18);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 1, 19);

        ArrayList<SAPManualLedgerTaxLine> manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        SAPManualLedgerTaxLine sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        SAPLawItem sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("62");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(-100);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        SAPManualLedgerTaxLine sapManualLedgerTaxLineFit = new SAPManualLedgerTaxLine();
        SAPLawItem sapLawItemFit = new SAPLawItem();
        sapLawItemFit.setLawId("1");
        sapLawItemFit.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFit.setLaw(sapLawItemFit);
        sapManualLedgerTaxLineFit.setAmount(30);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFit);

        new TaxAdapter().createManualLedgerEntry(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                "Record Customer Payment",
                manualLedgerTaxLines,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2012, 3, 31)),
                "This is my memo",
                3,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2012, 1, 1)), true);


        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment)));
        FinancialTransaction atc = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit));
        assertEquals(new SpcfMoney("100.00"), atc.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, atc.getCurrentTransactionState().getTransactionStateCd());

        FinancialTransaction atd = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxDebit));
        assertEquals(new SpcfMoney("30.00"), atd.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, atd.getCurrentTransactionState().getTransactionStateCd());

        FinancialTransaction erd = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("70.00"), erd.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, erd.getCurrentTransactionState().getTransactionStateCd());

        FinancialTransaction tca = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxCreditApplied));
        assertEquals(new SpcfMoney("30.00"), tca.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, tca.getCurrentTransactionState().getTransactionStateCd());

        assertEquals(PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testRecordNegativeCustomerPaymentWithOutstandingERPAndATRBalancesCreatesCorrectTransactions() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 1, 10);
        PayrollRun executedPayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));

        DataLoadServices.setPSPDate(2012, 1, 18);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 1, 23);
        voidAPaycheck(executedPayrollRun);

        DataLoadServices.setPSPDate(2012, 1, 24);

        ArrayList<SAPManualLedgerTaxLine> manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        SAPManualLedgerTaxLine sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        SAPLawItem sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("62");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(-100);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        new TaxAdapter().createManualLedgerEntry(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                "Record Customer Payment",
                manualLedgerTaxLines,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2012, 3, 31)),
                "This is my memo",
                3,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2012, 1, 1)), true);


        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment)));
        FinancialTransaction atc = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit));
        assertEquals(new SpcfMoney("100.00"), atc.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, atc.getCurrentTransactionState().getTransactionStateCd());

        FinancialTransaction erd = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit));
        assertEquals(new SpcfMoney("0.00"), erd.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, erd.getCurrentTransactionState().getTransactionStateCd());

        FinancialTransaction tca = assertOne(payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxCreditApplied));
        assertEquals(new SpcfMoney("100.00"), tca.getFinancialTransactionAmount());
        assertEquals(TransactionStateCode.Created, tca.getCurrentTransactionState().getTransactionStateCd());

        assertEquals(PayrollStatus.Pending, payrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAnnualSemiAnnualPaymentsAreIncluded() throws  Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-SDI-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-SDI-PAYMENT", DepositFrequencyCode.SEMIANNUAL, SpcfCalendar.createInstance(2012, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-20"));
        DataLoadServices.setPSPDate(2012, 4, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-04-20"));
        DataLoadServices.setPSPDate(2012, 7, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-07-20"));
        DataLoadServices.setPSPDate(2012, 10, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-10-20"));

        DataLoadServices.setPSPDate(2013, 1, 1);
        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "NY-SDI-PAYMENT", DepositFrequencyCode.ANNUAL, SpcfCalendar.createInstance(2013, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-01-20"));
        DataLoadServices.setPSPDate(2013, 4, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-04-20"));
        DataLoadServices.setPSPDate(2013, 7, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-07-20"));
        DataLoadServices.setPSPDate(2013, 10, 10);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-10-20"));

        SAPPaymentTemplateYearPayment summary2012 = new TaxAdapter().getTemplateYearPayment(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "2012", "NY-SDI-PAYMENT");
        assertEquals(0, summary2012.getTemplateQuarterPayments().get(0).getPendingPayments().size());
        assertEquals(1, summary2012.getTemplateQuarterPayments().get(1).getPendingPayments().size());
        assertEquals(0, summary2012.getTemplateQuarterPayments().get(2).getPendingPayments().size());
        assertEquals(1, summary2012.getTemplateQuarterPayments().get(3).getPendingPayments().size());

        SAPPaymentTemplateYearPayment summary2013 = new TaxAdapter().getTemplateYearPayment(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "2013", "NY-SDI-PAYMENT");
        assertEquals(0, summary2013.getTemplateQuarterPayments().get(0).getPendingPayments().size());
        assertEquals(0, summary2013.getTemplateQuarterPayments().get(1).getPendingPayments().size());
        assertEquals(0, summary2013.getTemplateQuarterPayments().get(2).getPendingPayments().size());
        assertEquals(1, summary2013.getTemplateQuarterPayments().get(3).getPendingPayments().size());

        //ledger
        assertLedgerTransactions(company, 6, new Date("01/01/2012"), new Date("12/31/2012"));
        assertLedgerTransactions(company, 1, new Date("01/01/2012"), new Date("3/31/2012"));
        assertLedgerTransactions(company, 2, new Date("04/01/2012"), new Date("06/30/2012"));
        assertLedgerTransactions(company, 1, new Date("07/01/2012"), new Date("09/30/2012"));
        assertLedgerTransactions(company, 2, new Date("10/01/2012"), new Date("12/31/2012"));

        assertLedgerTransactions(company, 5, new Date("01/01/2013"), new Date("12/31/2013"));
        assertLedgerTransactions(company, 1, new Date("01/01/2013"), new Date("3/31/2013"));
        assertLedgerTransactions(company, 1, new Date("04/01/2013"), new Date("06/30/2013"));
        assertLedgerTransactions(company, 1, new Date("07/01/2013"), new Date("09/30/2013"));
        assertLedgerTransactions(company, 2, new Date("10/01/2013"), new Date("12/31/2013"));

    }

    @Test
    public void testUpdateInitiationDates() throws Throwable{
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String companyPSID = "999061606";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);

        //******************3/7/11******************************
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("ofx/HPDE999061606.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        TaxAdapter taxAdapter = new TaxAdapter();
        SAPPaymentSearch sapPaymentSearch = new SAPPaymentSearch("Pending", null, "IRS", "IRS-941-PAYMENT", null, null, null, null, null, null, null, false);
        SpcfCalendar spcfCalendar = SpcfCalendar.createInstance();
        Date date = CalendarUtils.convertToDate(spcfCalendar);
        int count = taxAdapter.updateInitiationDates(sapPaymentSearch, date);
        assertEquals(2, count);

        spcfCalendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(spcfCalendar, 1);
        date = CalendarUtils.convertToDate(spcfCalendar);
        count = taxAdapter.updateInitiationDates(sapPaymentSearch, date);
        assertEquals(2, count);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT")).setCompany(company).find();
        for (MoneyMovementTransaction mmt : mmts) {
            assertEquals(SpcfCalendar.createInstance(spcfCalendar.getYear(), spcfCalendar.getMonth(), spcfCalendar.getDay(), SpcfTimeZone.getLocalTimeZone()), mmt.getInitiationDate().toLocal());
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testORUIMoneyMovementScreen() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("OR-OTCUI-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateACHAgentEnabledFlags(company, "OR-OTCUI-PAYMENT", true);
        DataLoadServices.updateAgencyTaxpayerId(company, "OR-OTCUI-PAYMENT","53259094-2");

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-01-10"));

        DataLoadServices.setPSPDate(2012, 4, 26);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("OR-OTCUI-PAYMENT"));

        SAPPaymentForVerification paymentForVerification = assertOne(new TaxAdapter().getMoneyMovementTransactionsForVerification(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null, null, null, null, null, null, null));

        List<SAPKeyValuePair> expectedDetails = new ArrayList<SAPKeyValuePair>();
        expectedDetails.add(new SAPKeyValuePair("Record Data", "6221232067070000015091       0000094000TEST_0001000000TEST_COMPANY_1          1"));
        expectedDetails.add(new SAPKeyValuePair("Txp Record Data", "TXP*000100000*01101*120331*S*24000*S*0*S*70000\\"));
        expectedDetails.add(new SAPKeyValuePair("Record Data", "6221232067070000015091       0000069000TEST_0001000000TEST_COMPANY_1          1"));
        expectedDetails.add(new SAPKeyValuePair("Txp Record Data", "TXP*000100000*01102*120331*L*34400*L*34600\\"));

        for (SAPKeyValuePair sapKeyValuePair : paymentForVerification.getDetails()) {
            assertNotNull(expectedDetails.remove(sapKeyValuePair));
        }

    }

    @Test
    public void testTORs() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 11, 1);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-11-05"));
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 11, 7);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"));

        DataLoadServices.setPSPDate(2012, 12, 21);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 1, 11);
        voidAPaycheck(payrollRun);

        List<SAPTemplateQuarterAmount> agencyTaxRefundBreakdown = new TaxAdapter().getAgencyTaxRefundBreakdown(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        assertEquals(2, agencyTaxRefundBreakdown.size());

        SAPTemplateQuarterAmount irsAmount;
        SAPTemplateQuarterAmount caAmount;

        if (agencyTaxRefundBreakdown.get(0).getPaymentTemplateCd().equals("IRS-941-PAYMENT")) {
            irsAmount = agencyTaxRefundBreakdown.get(0);
            caAmount = agencyTaxRefundBreakdown.get(1);
        } else {
            irsAmount = agencyTaxRefundBreakdown.get(1);
            caAmount = agencyTaxRefundBreakdown.get(0);
        }

        assertEquals(502.00, irsAmount.getAmount());
        assertEquals("IRS-941-PAYMENT", irsAmount.getPaymentTemplateCd());
        assertEquals(2012, irsAmount.getQuarter().getYear());
        assertEquals(4, irsAmount.getQuarter().getQuarter());

        assertEquals(146.00, caAmount.getAmount());
        assertEquals("CA-PITSDI-PAYMENT", caAmount.getPaymentTemplateCd());
        assertEquals(2012, caAmount.getQuarter().getYear());
        assertEquals(4, caAmount.getQuarter().getQuarter());

        new TaxAdapter().createTORTransactions(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "IRS-941-PAYMENT", new SAPQuarter(2012, 4));

        agencyTaxRefundBreakdown = new TaxAdapter().getAgencyTaxRefundBreakdown(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        assertEquals(1, agencyTaxRefundBreakdown.size());

        Application.beginUnitOfWork();
        PayrollRun adjustmentPR = assertOne(PayrollRun.findPayrollRuns(company).find(PayrollRun.PayrollRunType().equalTo(PayrollType.Adjustment)));
        assertEquals(PayrollStatus.Complete, adjustmentPR.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> financialTransactions = adjustmentPR.getFinancialTransactionCollection().sort(FinancialTransaction.Law().LawId());
        assertEquals(5, financialTransactions.size());
        FinancialTransaction fitFT = financialTransactions.get(0);
        assertEquals(TransactionTypeCode.AgencyRefundTOR, fitFT.getTransactionType().getTransactionTypeCd());
        assertEquals(new SpcfMoney("2.00"), fitFT.getFinancialTransactionAmount());
        assertEquals("1", fitFT.getLaw().getLawId());
        assertEquals(TransactionStateCode.Completed, fitFT.getCurrentTransactionState().getTransactionStateCd());
        assertEquals(SpcfCalendar.createInstance(2013, 1, 11, SpcfTimeZone.getLocalTimeZone()), fitFT.getSettlementDate().toLocal());
        assertEquals(SettlementType.ApplyForward, fitFT.getSettlementTypeCd());
        Application.rollbackUnitOfWork();

    }

    @Test
    public void testTORWithNegativeATRPriorQuarter() throws Throwable {
        //validates PSRV004251 + PSRV004252
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 8, 2);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-08-05"));
        PayrollRun payrollRun2 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-08-05"));
        PayrollRun payrollRun3 = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-08-05"));

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 10, 30);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2012, 11, 15);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());


        DataLoadServices.setPSPDate(2012, 12, 2);
        voidAPaycheck(payrollRun);
        voidAPaycheck(payrollRun2);
        voidAPaycheck(payrollRun3);

        PayrollRun newPayroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-12-08"));

        DataLoadServices.setPSPDate(2012, 12, 6);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 2);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 1, 30);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        //a bit contrived, but I can't find any other way to do it
        new PayrollRunAdapter().addFinancialLedgerAdjustmentTransaction(company.getSourceCompanyId(),
                                                                        company.getSourceSystemCd().toString(),
                                                                        newPayroll.getId().toString(),
                                                                        LedgerAccountCode.AgencyTaxRefund.toString(),
                                                                        LedgerAccountCode.ERPayable.toString(),
                                                                        1000,
                                                                        "1",
                                                                        "Note");



        //check the balances
        List<SAPTemplateQuarterAmount> agencyTaxRefundBreakdown = new TaxAdapter().getAgencyTaxRefundBreakdown(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        assertEquals(3, agencyTaxRefundBreakdown.size());
        SAPTemplateQuarterAmount f941Q3 = null;
        SAPTemplateQuarterAmount f941Q4 = null;
        SAPTemplateQuarterAmount f940 = null;

        for (SAPTemplateQuarterAmount sapTemplateQuarterAmount : agencyTaxRefundBreakdown) {
            if (sapTemplateQuarterAmount.getPaymentTemplateCd().equals(PaymentTemplate.IRS_941)) {
                if (sapTemplateQuarterAmount.getQuarter().getQuarter() == 3) {
                    f941Q3 = sapTemplateQuarterAmount;
                } else {
                    f941Q4  = sapTemplateQuarterAmount;
                }
            } else {
                f940 = sapTemplateQuarterAmount;
            }
        }

        assertNotNull(f941Q3);
        assertNotNull(f941Q4);
        assertNotNull(f940);

        assertEquals(1506., f941Q3.getAmount());
        assertEquals("IRS-941-PAYMENT", f941Q3.getPaymentTemplateCd());
        assertEquals(2012, f941Q3.getQuarter().getYear());
        assertEquals(3, f941Q3.getQuarter().getQuarter());
        assertFalse(f941Q3.getIsAnnual());

        assertEquals(-1000., f941Q4.getAmount());
        assertEquals("IRS-941-PAYMENT", f941Q4.getPaymentTemplateCd());
        assertEquals(2012, f941Q4.getQuarter().getYear());
        assertEquals(4, f941Q4.getQuarter().getQuarter());
        assertFalse(f941Q4.getIsAnnual());

        assertEquals(132., f940.getAmount());
        assertEquals("IRS-940-PAYMENT", f940.getPaymentTemplateCd());
        assertEquals(2012, f940.getQuarter().getYear());
        assertTrue(f940.getIsAnnual());

        //940 should apply across the entire year
        new TaxAdapter().createTORTransactions(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "IRS-940-PAYMENT", new SAPQuarter(2012, 4));

        agencyTaxRefundBreakdown = new TaxAdapter().getAgencyTaxRefundBreakdown(company.getSourceSystemCd().toString(), company.getSourceCompanyId());
        assertEquals(2, agencyTaxRefundBreakdown.size());

        //941 should give an error
        try {
            new TaxAdapter().createTORTransactions(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "IRS-941-PAYMENT", new SAPQuarter(2012, 4));
            fail("exception expected");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Details: ERROR (5000) Cannot create TOR because Company has negative ATR on template"));
        }

    }

    @Test
    public void test940WithNetZeroCannotTOR() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2012, 8, 2);
        PayrollRun payrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-08-05"));

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2012, 10, 30);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2012, 11, 15);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());

        DataLoadServices.setPSPDate(2012, 12, 2);
        voidAPaycheck(payrollRun);

        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2012-12-08"));

        DataLoadServices.setPSPDate(2012, 12, 6);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 2);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 1, 30);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        //only 941, i.e. balance for 940 is shown as 0
        SAPTemplateQuarterAmount agencyTaxRefundBreakdown = assertOne(new TaxAdapter().getAgencyTaxRefundBreakdown(company.getSourceSystemCd().toString(), company.getSourceCompanyId()));
        assertEquals("IRS-941-PAYMENT", agencyTaxRefundBreakdown.getPaymentTemplateCd());

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.financialTransactionManager.addTORTransactions(company.getSourceSystemCd(), company.getSourceCompanyId(), "IRS-940-PAYMENT", new SAPQuarter(2012, 4).getLastDayOfQuarter());
        assertEquals("There is no balance to TOR", assertOne(processResult.getWarningMessages()).getMessage());
        assertNull(processResult.getResult());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void lotsOfPaychecks() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        for (int i=1; i < 30; i++) {
            DataLoadServices.setPSPDate(2013, 1, i);
            DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(PSPDate.getPSPTime()));
        }


    }

    @Test
    public void updatingNYWHAgencyIDsUpdatesNYMetroAgencyIDs() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_WH, SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_METRO, SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        //Original IDs?
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertNyIDs(company, "258963147 1", "122456");
        PayrollServices.rollbackUnitOfWork();

        //Change ID
        SAPCompanyAgencyPaymentTemplateAgencyId mainID = new SAPCompanyAgencyPaymentTemplateAgencyId();
        mainID.setId("Bob");

        SAPCompanyAgencyPaymentTemplateAgencyId stateAccessCode = new SAPCompanyAgencyPaymentTemplateAgencyId();
        stateAccessCode.setName("State Access Code");
        stateAccessCode.setId("Apples");

        new TaxAdapter().updateAgencyIDs(company.getSourceSystemCd().name(), company.getSourceCompanyId(), PaymentTemplate.NY_WH, Arrays.asList(mainID, stateAccessCode));

        //New IDs?
        PayrollServices.beginUnitOfWork();
        assertNyIDs(company, "Bob", "Apples");
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void updatingNYWHAgencyIDsWithoutNYMetro() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_WH, SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.addCompanyLawsWithAgencyId("1234567890", company, "36", "54", "56", "57");
        DataLoadServices.addAdditionalFilingAmounts(company);

        PayrollServices.beginUnitOfWork();
        AgencyIdDTO agencyIdDTO = new AgencyIdDTO(PaymentTemplate.NY_WH, "State Access Code", "123456");
        assertSuccess(PayrollServices.companyManager.addOrUpdateAgencyId(company.getSourceSystemCd(), company.getSourceCompanyId(), agencyIdDTO));
        PayrollServices.commitUnitOfWork();

        //Original IDs?
        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertNyIDs(company, "1234567890", "123456", false);
        PayrollServices.rollbackUnitOfWork();

        //Change ID
        SAPCompanyAgencyPaymentTemplateAgencyId mainID = new SAPCompanyAgencyPaymentTemplateAgencyId();
        mainID.setId("Bob");

        SAPCompanyAgencyPaymentTemplateAgencyId stateAccessCode = new SAPCompanyAgencyPaymentTemplateAgencyId();
        stateAccessCode.setName("State Access Code");
        stateAccessCode.setId("Apples");

        new TaxAdapter().updateAgencyIDs(company.getSourceSystemCd().name(), company.getSourceCompanyId(), PaymentTemplate.NY_WH, Arrays.asList(mainID, stateAccessCode));

        //New IDs?
        PayrollServices.beginUnitOfWork();
        assertNyIDs(company, "Bob", "Apples", false);
        PayrollServices.rollbackUnitOfWork();

    }

    private void assertNyIDs(Company company, String mainId, String stateAccessCode) {
        assertNyIDs(company, mainId, stateAccessCode, true);
    }

    private void assertNyIDs(Company company, String mainId, String stateAccessCode, boolean isMetroSupported) {
        CompanyAgencyPaymentTemplate nyWH = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_WH));
        assertEquals(mainId, nyWH.getAgencyTaxpayerId());

        CompanyPaymentTemplateAgencyId whStateAccessCode = assertOne(nyWH.getCompanyPaymentTemplateAgencyIdCollection().find(CompanyPaymentTemplateAgencyId.Name().equalTo("State Access Code")));
        assertEquals(stateAccessCode, whStateAccessCode.getAgencyTaxpayerId());

        CompanyAgencyPaymentTemplate nyMetro = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));

        if(isMetroSupported) {
            assertEquals(mainId, nyMetro.getAgencyTaxpayerId());

            CompanyPaymentTemplateAgencyId metroStateAccessCode = assertOne(nyMetro.getCompanyPaymentTemplateAgencyIdCollection().find(CompanyPaymentTemplateAgencyId.Name().equalTo("State Access Code")));
            assertEquals(stateAccessCode, metroStateAccessCode.getAgencyTaxpayerId());
        } else {
            assertNull("Metro is found", nyMetro);
        }
    }

    @Test
    public void testUpdateNYDFUpdatesNYMetro() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_WH, SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_METRO, SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        assertNYDFs(company, DepositFrequencyCode.QUARTERLY);

        PayrollServices.rollbackUnitOfWork();

        SAPDepositFrequency sapDepositFrequency = new SAPDepositFrequency();
        sapDepositFrequency.setDepositFrequency(DepositFrequencyCode.FIVEBANKINGDAY.name());
        sapDepositFrequency.setEffectiveDate(SAPTranslator.createDate(2012, 1, 1));
        new TaxAdapter().updateDepositFrequencies(company.getSourceSystemCd().name(), company.getSourceCompanyId(), PaymentTemplate.NY_WH, Arrays.asList(sapDepositFrequency));

        PayrollServices.beginUnitOfWork();
        assertNYDFs(company, DepositFrequencyCode.FIVEBANKINGDAY);
        PayrollServices.rollbackUnitOfWork();

    }

    private void assertNYDFs(Company company, DepositFrequencyCode code) {
        EffectiveDepositFrequency effectiveDepositFrequency = assertOne(EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_WH), SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), true));
        assertEquals(code, effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());

        EffectiveDepositFrequency metroEffectiveDepositFrequency = assertOne(EffectiveDepositFrequency.findEffectiveDepositFrequencies(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO), SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), true));
        assertEquals(code, metroEffectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId());
    }

    @Test
    public void testUpdateNYACHRegisteredFlagUpdatesMetro() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_WH, SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_METRO, SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);

        assertNYRegisteredFlag(company, true);
        PayrollServices.rollbackUnitOfWork();

        new TaxAdapter().updateAgentEnabled(company.getSourceSystemCd().name(), company.getSourceCompanyId(), PaymentTemplate.NY_WH, false);

        PayrollServices.beginUnitOfWork();
        assertNYRegisteredFlag(company, false);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testLocalTaxes() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_WH, SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate(PaymentTemplate.NY_METRO, SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.addCompanyLaws_177(company, "63", "64");

        List<SAPCompanyLaw> companyLaws = new TaxAdapter().findCompanyLaws(company.getSourceSystemCd().name(), company.getSourceCompanyId());

        SAPCompanyLaw law177 = null;
        for (SAPCompanyLaw companyLaw : companyLaws) {
            if (companyLaw.getSourceId().equals("63")) {
                law177 = companyLaw;
            }
        }
        assertNotNull(law177);

        new TaxAdapter().editCompanyLawAgencyId(company.getSourceSystemCd().name(), company.getSourceCompanyId(), law177.getSourceId(), "$TEXAS");

        PayrollServices.beginUnitOfWork();
        CompanyLaw foundCompanyLaw = CompanyLaw.findCompanyLawBySourceId(company, "63");
        assertEquals("$TEXAS", foundCompanyLaw.getQbdtPayrollItemInfo().getAgencyId());
        PayrollServices.rollbackUnitOfWork();

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testUpdateGroupPaymentMethods() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        DataLoadServices.setPSPDate(2012, 1, 12);

        Company company1 = DataLoadPalette.setupTaxCompany();
        Company company2 = DataLoadPalette.setupTaxCompany();
        Company company3 = DataLoadPalette.setupTaxCompany();

        DataLoadServices.updateRequiredIDs(company3, "CA-PITSDI-PAYMENT", false);

        DataLoadServices.setPSPDate(2013, 1, 3);
        DataLoadPalette.runSimpleTaxPayroll(company1, new DateDTO("2013-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company2, new DateDTO("2013-01-20"));
        DataLoadPalette.runSimpleTaxPayroll(company3, new DateDTO("2013-01-20"));

        SAPPaymentSearch searchCriteria = new SAPPaymentSearch("Pending", null, "CAEDD", "CA-PITSDI-PAYMENT", null, null, null, null, null, null, new SAPQuarter(2013, 1), false);
        assertPaymentMethodCounts(searchCriteria, 2, 1);

        new TaxAdapter().updateGroupPaymentMethods(searchCriteria, PaymentMethod.CheckPayment.toString());
        assertPaymentMethodCounts(searchCriteria, 0, 3);

        new TaxAdapter().updateGroupPaymentMethods(searchCriteria, PaymentMethod.ACHCredit.toString());
        assertPaymentMethodCounts(searchCriteria, 2, 1); //can't set to ACHCredit if not enabled
    }

    /*This test will fail in non-prod env, nore details in PSP-3851, marking it as Ignore because this is expected
    behaviour in non-prod env but it still requires investigation.

    Created R&N for futher investigation - PSP-6383, till then this test will be marked as Ignore

    Comment from David on failure of this test:
    I've checked in TaxAdapterTests.testSettlementDateInPast. It will fail in non-prod. To get it to pass, comment out
    the block in GenerateLiabilityCheckCore. In prod, it will be a prod alert but it will not stop the submission.
    We still haven't figured out the root cause of the liability check problem, but it appears to be existing.
    */
    @Ignore
    @Test
    public void testSettlementDateInPast() throws Throwable {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 7 , 2);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-07-05"));
        DataLoadServices.runOffload();
        DataLoadServices.runACHTransactionProcessor();

        DataLoadServices.setPSPDate(2013, 7 , 9);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());

        DataLoadServices.setPSPDate(2013, 8 , 1);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2013-08-10"));

        DataLoadServices.setPSPDate(2013, 8 , 2);
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.AchRejectOther);

        DataLoadServices.setPSPDate(2013, 8 , 11);
        PayrollServices.beginUnitOfWork();
        new ProcessMissedPayrolls().process(PSPDate.getPSPTime().format("20130811"));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 8 , 20);
        DataLoadServices.removeCompanyOnHoldReasons(company);


        ArrayList<SAPManualLedgerTaxLine> manualLedgerLines = new TaxAdapter().getManualLedgerLines(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), PaymentTemplate.IRS_941, null, new Date("08/20/2013"));
        manualLedgerLines.get(1).setAmount(-10);
        new TaxAdapter().createManualLedgerEntry(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), "Liabilities/Wages", manualLedgerLines, new Date("08/20/2013"), "My memo", 0, null, true);

        PayrollServices.beginUnitOfWork();
        FinancialTransaction erTOA = assertOne(Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxOverpaymentApplied)));
        assertEquals(erTOA.getMoneyMovementTransaction().getFirstFinancialTransaction().getSettlementDate(), erTOA.getSettlementDate());
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * This test is to verify the fix for PSP-4142
     */
    @Test
    public void testCancelDeleteRAFEnrollment() {

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2012, 1, 2);
        Company company1 = DataLoadPalette.setupTaxCompany();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateRAFEnrollmentStatus(company1.getSourceSystemCd(), company1.getSourceCompanyId(), company1.getCurrentRAFEnrollment(), RAFEnrollmentStatus.PendingEnrollmentTape));
        assertSuccess(PayrollServices.companyManager.updateRAFEnrollmentStatus(company1.getSourceSystemCd(), company1.getSourceCompanyId(), company1.getCurrentRAFEnrollment(), RAFEnrollmentStatus.PendingEnrollmentResponse));
        assertSuccess(PayrollServices.companyManager.updateRAFEnrollmentStatus(company1.getSourceSystemCd(), company1.getSourceCompanyId(), company1.getCurrentRAFEnrollment(), RAFEnrollmentStatus.Enrolled));
        assertSuccess(PayrollServices.companyManager.updateRAFEnrollmentStatus(company1.getSourceSystemCd(), company1.getSourceCompanyId(), company1.getCurrentRAFEnrollment(), RAFEnrollmentStatus.PendingDeleteTape));
        PayrollServices.commitUnitOfWork();

        DomainEntitySet<RAFEnrollment> allRAFEnrollments = company1.getAllRAFEnrollments();
        String rafEnrollmentId = null;
        for(RAFEnrollment rafEnrollment : allRAFEnrollments){
            if(rafEnrollment.getStatus().equals(RAFEnrollmentStatus.PendingDeleteTape)){
                rafEnrollmentId = rafEnrollment.getId().toString();
                break;
            }
        }
        try {
            new TaxAdapter().cancelDeleteRAFEnrollment(company1.getSourceSystemCd().toString(), company1.getSourceCompanyId(), rafEnrollmentId);
        } catch (Throwable pThrowable) {
            logger.info(pThrowable);
        }
        PayrollServices.beginUnitOfWork();
        Application.refresh(company1);
        PayrollServices.rollbackUnitOfWork();
        allRAFEnrollments = company1.getAllRAFEnrollments();
        for(RAFEnrollment rafEnrollment : allRAFEnrollments){
            assert rafEnrollmentId != null;
            if(rafEnrollmentId.equals(rafEnrollment.getId().toString())) {
                assertEquals("Enrollment Status Change Successful", RAFEnrollmentStatus.Enrolled, rafEnrollment.getStatus());
            }
        }
    }

    @Test
    public void testManualLedgerKeyingFailureForNY_1MN() throws Exception {
        runBeforeEachTest() ;
        String psid = "336016279";
        String fein ="161563822";

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,4, 1,0,0,0,0, SpcfTimeZone.getLocalTimeZone()));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, fein, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        String stateEin = "161563822 6";
        DataLoadServices.addFederalAndNYStateTaxCompanyLaws(company);
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-45MN-PAYMENT", SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.updateAgencyTaxpayerId(company,"NY-1MN-PAYMENT",stateEin);
        DataLoadServices.updateAgencyTaxpayerId(company,"NY-MTA305-PAYMENT","161563822 6");
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-SDI-PAYMENT", null);
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.addEEs(company, 4,true,true);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "NY-1MN-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "NY-MTA305-PAYMENT");
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-1MN-PAYMENT", SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-MTA305-PAYMENT", SpcfCalendar.createInstance(2013, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("NY-45MN-PAYMENT", SpcfCalendar.createInstance(2013, 1, 1));

        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,4, 18,10,59,0,0, SpcfTimeZone.getLocalTimeZone()));
        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("143", "14.3");
        lawAmounts.put("1", "10");
        lawAmounts.put("36", "50");
        DataLoadServices.runPayrollForNY(psid,new ArrayList<Employee>(company.getCloudEmployees()),null,new DateDTO("2016-3-31"),lawAmounts,true) ;
        lawAmounts = new HashMap();
        lawAmounts.put("61", "6.11");
        lawAmounts.put("62", "6.21");
        lawAmounts.put("63", "6.31");
        lawAmounts.put("64", "6.41");
        lawAmounts.put("66", "6.61");
        lawAmounts.put("143", "14.31");
        lawAmounts.put("1", "11");
        lawAmounts.put("36", "51");
        lawAmounts.put("54", "26");
        DataLoadServices.runPayrollForNY(psid,new ArrayList<Employee>(company.getEmployees()),null,new DateDTO("2016-4-1"),lawAmounts,true) ;
        lawAmounts = new HashMap();
        lawAmounts.put("61", "6.12");
        lawAmounts.put("62", "6.22");
        lawAmounts.put("63", "6.32");
        lawAmounts.put("64", "6.42");
        lawAmounts.put("66", "6.62");
        lawAmounts.put("143", "14.32");
        lawAmounts.put("1", "12");
        lawAmounts.put("36", "52");
        lawAmounts.put("54", "22");
        DataLoadServices.runPayrollForNY(psid,new ArrayList<Employee>(company.getEmployees()),null,new DateDTO("2016-4-7"),lawAmounts,true) ;
        lawAmounts = new HashMap();
        lawAmounts.put("61", "6.13");
        lawAmounts.put("62", "6.23");
        lawAmounts.put("63", "6.33");
        lawAmounts.put("64", "6.43");
        lawAmounts.put("66", "6.63");
        lawAmounts.put("143", "14.33");
        lawAmounts.put("1", "103");
        lawAmounts.put("36", "503");
        lawAmounts.put("54", "253");
        DataLoadServices.runPayrollForNY(psid,new ArrayList<Employee>(company.getEmployees()),null,new DateDTO("2016-4-14"),lawAmounts,true) ;
        lawAmounts = new HashMap();
        lawAmounts.put("61", "6.14");
        lawAmounts.put("62", "6.24");
        lawAmounts.put("63", "6.34");
        lawAmounts.put("64", "6.44");
        lawAmounts.put("66", "6.64");
        lawAmounts.put("143", "14.34");
        lawAmounts.put("1", "104");
        lawAmounts.put("36", "504");
        lawAmounts.put("54", "254");
        DataLoadServices.runPayrollForNY(psid,new ArrayList<Employee>(company.getEmployees()),null,new DateDTO("2016-4-19"),lawAmounts,true) ;
        DataLoadServices.runOffload();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,4, 21,0,0,0,0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        new OffloadNYDTFPayments().createFiles(PSPDate.getPSPTime());
        new OffloadATFFinalizedPayments().process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,4, 25,0,0,0,0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.beginUnitOfWork();
        new OffloadNYDTFPayments().createFiles(PSPDate.getPSPTime());
        new OffloadATFFinalizedPayments().process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();
        lawAmounts = new HashMap();
        lawAmounts.put("61", "50");
        lawAmounts.put("62", "100");
        lawAmounts.put("63", "150");
        lawAmounts.put("64", "200");
        lawAmounts.put("66", "250");
        lawAmounts.put("143", "350");
        lawAmounts.put("1", "300");
        lawAmounts.put("36", "540");
        lawAmounts.put("54", "600");
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016,4, 26,0,0,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.runPayrollForNY(psid,new ArrayList<Employee>(company.getEmployees()),null,new DateDTO("2016-4-19"),lawAmounts,false) ;
        DataLoadServices.runOffload();
        PayrollServices.beginUnitOfWork();
        new OffloadNYDTFPayments().createFiles(PSPDate.getPSPTime());
        new OffloadATFFinalizedPayments().process(PSPDate.getPSPTime());
        PayrollServices.commitUnitOfWork();
            //Manula Ledger
            DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2016, 6, 20, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
            ArrayList<SAPManualLedgerTaxLine> manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
            SAPManualLedgerTaxLine sapManualLedgerTaxLineNYSIT = new SAPManualLedgerTaxLine();
            SAPLawItem sapLawItemNYSIT= new SAPLawItem();
            sapLawItemNYSIT.setLawId("36");
            sapLawItemNYSIT.setPaymentTemplateCd("NY-1MN-PAYMENT");
            sapManualLedgerTaxLineNYSIT.setLaw(sapLawItemNYSIT);
            // sapManualLedgerTaxLineNYSIT.setAmount(1071.98);
            sapManualLedgerTaxLineNYSIT.setAmount(3320);
            manualLedgerTaxLines.add(sapManualLedgerTaxLineNYSIT);
            boolean isKeyFailed = true;
        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PMT_TMPLT_SPLIT_ETD_NULL_BACKDATED_AND_TIMELY_TAXES, "1NY-1MN-PAYMENT");
        Application.commitUnitOfWork();
            try {
                DataLoadServices.setPrincipalToAgent(OperationId.values());
                new TaxAdapter().createManualLedgerEntry(
                        "QBDT",
                        "336016279",
                        "Record Customer Payment",
                        manualLedgerTaxLines,
                        SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2016, 4, 1,SpcfTimeZone.getLocalTimeZone())),
                        "This is my memo2",
                        3,
                        SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2016, 4, 19,SpcfTimeZone.getLocalTimeZone())), true);
            } catch (Throwable pThrowable) {
                isKeyFailed = false;
            }
        Assert.assertFalse("Keying must fail",isKeyFailed);
        Application.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PMT_TMPLT_SPLIT_ETD_NULL_BACKDATED_AND_TIMELY_TAXES, "NY-1MN-PAYMENT");
        Application.commitUnitOfWork();
        try {
            isKeyFailed = true;
            DataLoadServices.setPrincipalToAgent(OperationId.values());
            new TaxAdapter().createManualLedgerEntry(
                    "QBDT",
                    "336016279",
                    "Record Customer Payment",
                    manualLedgerTaxLines,
                    SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2016, 4, 1,SpcfTimeZone.getLocalTimeZone())),
                    "This is my memo2",
                    3,
                    SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2016, 4, 19,SpcfTimeZone.getLocalTimeZone())), true);
        } catch (Throwable pThrowable) {
            isKeyFailed = false;
        }
        Assert.assertTrue("Keying failed",isKeyFailed);

    }

    @Test
    public void test7911() throws Throwable {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 12, 1));
        DataLoadServices.setPSPDate(2015, 2, 17);
        String psid = "652012793";
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("ofx/balance_file_7911.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
       // DataLoadServices.runJobs(1);
        //DataLoadServices.setPSPDate(2015, 02, 18);
        ArrayList<SAPManualLedgerTaxLine> manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        SAPManualLedgerTaxLine sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        SAPLawItem sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("61");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(1720.05);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("62");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(1722.3);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("1");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(3092);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("63");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(402.23);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("64");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(402.82);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);
        new TaxAdapter().createManualLedgerEntry(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                "Record Customer Payment",
                manualLedgerTaxLines,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 1, 15,SpcfTimeZone.getLocalTimeZone())),
                "This is my memo",
                3,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 1, 15,SpcfTimeZone.getLocalTimeZone())), true);
      //  DataLoadServices.runJobs(10);
       // DataLoadServices.setPSPDate(2015, 02, 28);

        manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
         sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
         sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("61");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(333.43);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("62");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(331.09);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("1");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(255.79);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("63");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(77.98);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("64");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(77.43);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);
        new TaxAdapter().createManualLedgerEntry(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                "Record Customer Payment",
                manualLedgerTaxLines,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 1, 28,SpcfTimeZone.getLocalTimeZone())),
                "This is my memo2",
                3,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 1, 28,SpcfTimeZone.getLocalTimeZone())), true);

        manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("61");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(1498.18);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("62");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(1498.19);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);


        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("1");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(2968.35);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("63");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(350.38);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);



        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("64");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(350.37);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);
        new TaxAdapter().createManualLedgerEntry(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                "Record Customer Payment",
                manualLedgerTaxLines,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 2, 2,SpcfTimeZone.getLocalTimeZone())),
                "This is my memo3",
                3,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 2, 2,SpcfTimeZone.getLocalTimeZone())), true);

        manualLedgerTaxLines = new ArrayList<SAPManualLedgerTaxLine>();
        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("61");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(184.03);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("62");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(184.11);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);


        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("1");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(121.86);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);

        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("63");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(43.08);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);



        sapManualLedgerTaxLineFica = new SAPManualLedgerTaxLine();
        sapLawItemFica = new SAPLawItem();
        sapLawItemFica.setLawId("64");
        sapLawItemFica.setPaymentTemplateCd("IRS-941-PAYMENT");
        sapManualLedgerTaxLineFica.setLaw(sapLawItemFica);
        sapManualLedgerTaxLineFica.setAmount(43.05);
        manualLedgerTaxLines.add(sapManualLedgerTaxLineFica);
        new TaxAdapter().createManualLedgerEntry(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                "Record Customer Payment",
                manualLedgerTaxLines,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 2, 12,SpcfTimeZone.getLocalTimeZone())),
                "This is my memo4",
                3,
                SAPTranslator.getDateFromSpcfCalendar(SpcfCalendar.createInstance(2015, 2, 12,SpcfTimeZone.getLocalTimeZone())), true);
        PayrollServices.beginUnitOfWork();
        SpcfDecimal atrAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.AgencyTaxRefund);
        Assert.assertEquals("ATR amount",SpcfDecimal.createInstance(15656.63).setScale(2),atrAmount);
        SpcfDecimal erpAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.ERPayable);
        Assert.assertEquals("ERP amount",SpcfMoney.ZERO,erpAmount);
        SpcfDecimal erpLiabOffsetAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.ERLiabilityOffset);
        Assert.assertEquals("ERLiability offset amount",SpcfDecimal.createInstance(0.09).setScale(2),erpLiabOffsetAmount);
        DomainEntitySet<PayrollRun> payrollRuns=PayrollRun.findPayrollRuns(company);
        PayrollRun payrollRun=payrollRuns.find(PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2015, 1, 15,SpcfTimeZone.getLocalTimeZone()))).getFirst();
        SpcfDecimal atdAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxDebit)){
            atdAmount=atdAmount.add(ft.getFinancialTransactionAmount());
        }
        SpcfDecimal atcAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxCredit)){
            atcAmount=atcAmount.add(ft.getFinancialTransactionAmount());
        }

        SpcfDecimal etoaAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.EmployerTaxOverpaymentApplied)){
            etoaAmount = etoaAmount.add(ft.getFinancialTransactionAmount());
        }
        SpcfDecimal etdAmountAdjusted = SpcfMoney.ZERO;
        for(FinancialTransaction ft:payrollRun.getEmployerTaxDebitTransactions()){
            if(!payrollRun.getEmployerTaxDebitTransactions().find(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled))).isEmpty()){
                if(ft.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled)) {
                    etdAmountAdjusted = etdAmountAdjusted.add(ft.getFinancialTransactionAmount()) ;
                } else{
                    etdAmountAdjusted = etdAmountAdjusted.subtract(ft.getFinancialTransactionAmount()) ;
                }
            }
        }
        SpcfDecimal  etdAmount= payrollRun.getNonCancelledEmployerTaxDebit().getFinancialTransactionAmount();
        Assert.assertEquals("ETOA/ATO amount",etoaAmount,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmountAdjusted,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmount,atcAmount.subtract(atdAmount));

        payrollRun= payrollRuns.find(PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2015, 1, 28,SpcfTimeZone.getLocalTimeZone()))).getFirst();
        atcAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxCredit)){
            atcAmount=atcAmount.add(ft.getFinancialTransactionAmount());
        }
         atdAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxDebit)){
            atdAmount=atdAmount.add(ft.getFinancialTransactionAmount());
        }

         etoaAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.EmployerTaxOverpaymentApplied)){
            etoaAmount = etoaAmount.add(ft.getFinancialTransactionAmount());
        }
         etdAmountAdjusted = SpcfMoney.ZERO;
        for(FinancialTransaction ft:payrollRun.getEmployerTaxDebitTransactions()){
            if(!payrollRun.getEmployerTaxDebitTransactions().find(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled))).isEmpty()){
                if(ft.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled)) {
                    etdAmountAdjusted = etdAmountAdjusted.add(ft.getFinancialTransactionAmount()) ;
                } else{
                    etdAmountAdjusted = etdAmountAdjusted.subtract(ft.getFinancialTransactionAmount()) ;
                }
            }
        }
         etdAmount= payrollRun.getNonCancelledEmployerTaxDebit().getFinancialTransactionAmount();
        Assert.assertEquals("ETOA/ATO amount",etoaAmount,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmountAdjusted,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmount,atcAmount.subtract(atdAmount));
        payrollRun=payrollRuns.find(PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2015, 2, 2,SpcfTimeZone.getLocalTimeZone()))).getFirst();
        atdAmount = SpcfMoney.ZERO;
        atcAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxCredit)){
            atcAmount=atcAmount.add(ft.getFinancialTransactionAmount());
        }
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxDebit)){
            atdAmount=atdAmount.add(ft.getFinancialTransactionAmount());
        }

        etoaAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.EmployerTaxOverpaymentApplied)){
            etoaAmount = etoaAmount.add(ft.getFinancialTransactionAmount());
        }
        etdAmountAdjusted = SpcfMoney.ZERO;
        for(FinancialTransaction ft:payrollRun.getEmployerTaxDebitTransactions()){
            if(!payrollRun.getEmployerTaxDebitTransactions().find(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled))).isEmpty()){
                if(ft.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled)) {
                    etdAmountAdjusted = etdAmountAdjusted.add(ft.getFinancialTransactionAmount()) ;
                } else{
                    etdAmountAdjusted = etdAmountAdjusted.subtract(ft.getFinancialTransactionAmount()) ;
                }
            }
        }
        etdAmount= payrollRun.getNonCancelledEmployerTaxDebit().getFinancialTransactionAmount();
        Assert.assertEquals("ETOA/ATO amount",etoaAmount,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmountAdjusted,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmount,atcAmount.subtract(atdAmount));
        payrollRun= payrollRuns.find(PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2015, 2, 12,SpcfTimeZone.getLocalTimeZone()))).getFirst();
        atdAmount = SpcfMoney.ZERO;
        atcAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxCredit)){
            atcAmount=atcAmount.add(ft.getFinancialTransactionAmount());
        }
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.AgencyTaxDebit)){
            atdAmount=atdAmount.add(ft.getFinancialTransactionAmount());
        }

        etoaAmount = SpcfMoney.ZERO;
        for(FinancialTransaction ft: payrollRun.getFinancialTransactions(TransactionStateCode.Created,TransactionTypeCode.EmployerTaxOverpaymentApplied)){
            etoaAmount = etoaAmount.add(ft.getFinancialTransactionAmount());
        }
        etdAmountAdjusted = SpcfMoney.ZERO;
        for(FinancialTransaction ft:payrollRun.getEmployerTaxDebitTransactions()){
            if(!payrollRun.getEmployerTaxDebitTransactions().find(FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled))).isEmpty()){
                if(ft.getCurrentTransactionState().getTransactionStateCd().equals(TransactionStateCode.Cancelled)) {
                    etdAmountAdjusted = etdAmountAdjusted.add(ft.getFinancialTransactionAmount()) ;
                } else{
                    etdAmountAdjusted = etdAmountAdjusted.subtract(ft.getFinancialTransactionAmount()) ;
                }
            }
        }
        etdAmount= payrollRun.getNonCancelledEmployerTaxDebit().getFinancialTransactionAmount();
        Assert.assertEquals("ETOA/ATO amount",etoaAmount,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmountAdjusted,atdAmount);
        Assert.assertEquals("ETDAdjusted/ATD amount",etdAmount,atcAmount.subtract(atdAmount));
        PayrollServices.rollbackUnitOfWork();

    }

    private void assertPaymentMethodCounts(SAPPaymentSearch searchCriteria, int expectedACHCredit, int expectedCheck) throws Throwable {
        SAPSearchResults<SAPPayment> taxPayments = new TaxAdapter().findTaxPayments(searchCriteria, 0, 100, null, false);
        assertEquals(expectedACHCredit + expectedCheck, taxPayments.getTotalRecords());

        int achCreditPayments = 0;
        int checkPayments = 0;
        for (SAPPayment sapPayment : taxPayments.getReturnsList()) {
            if (sapPayment.getPaymentMethod().equals(PaymentMethod.ACHCredit.toString())) {
                achCreditPayments++;
            } else if (sapPayment.getPaymentMethod().equals(PaymentMethod.CheckPayment.toString())) {
                checkPayments++;
            }
        }
        assertEquals(expectedACHCredit, achCreditPayments);
        assertEquals(expectedCheck, checkPayments);
    }


    private void assertNYRegisteredFlag(Company company, boolean enabled) {
        CompanyAgencyPaymentTemplate nyWH = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_WH));
        assertEquals(enabled, nyWH.getCompanyPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit).getAgentEnabled());

        CompanyAgencyPaymentTemplate nyMetro = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(PaymentTemplate.NY_METRO));
        assertEquals(enabled, nyMetro.getCompanyPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit).getAgentEnabled());
    }


    private void assertLedgerTransactions(Company company, int expected, Date quarterBegin, Date quarterEnd) throws Throwable {
        ArrayList<SAPLawTransactions> transactions = new TaxAdapter().findTaxTransactions(company.getSourceSystemCd().toString(), company.getSourceCompanyId(), null, "NYSIF", "NY-SDI-PAYMENT", null, null, quarterBegin, quarterEnd, true);
        assertEquals(expected, transactions.get(0).getTaxTransactions().size());
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
    public void testNullPaymentTypeOnUpdateInitiationDates() {
        TaxAdapter taxAdapter = new TaxAdapter();
        SAPPaymentSearch sapPaymentSearch = new SAPPaymentSearch("Pending", null, null, null, null, null, null, null, null, null, null, false);
        SpcfCalendar spcfCalendar = SpcfCalendar.createInstance();
        Date date = CalendarUtils.convertToDate(spcfCalendar);
        try {
            int count = taxAdapter.updateInitiationDates(sapPaymentSearch, date);
        } catch (Throwable pThrowable) {
            assertEquals("Error returned does not match : ", "Error updating initiation dates : Payment Type missing. Please select Agency and Payment type again.", pThrowable.getMessage());
        }
    }
    
    @Test
    public void testMessageChangedOnUpdatePaymentMethodPopup() throws Throwable
    {
        TaxAdapter taxAdapter=new TaxAdapter();
        try {
            taxAdapter.getValidPaymentMethodsByTemplate(null);
        }
        catch(Exception e) {
            assertEquals(e.getMessage(), "Error updating payment methods : Payment Type missing. Please select Agency and Payment type again.");
        }
        try {
            taxAdapter.getValidPaymentMethodsByTemplate("FALSE-PAYMENT-CD");
        }
        catch(Exception e) {
            assertEquals(e.getMessage(),  "Error updating payment methods : Payment Type missing. Please select Agency and Payment type again.");
        }
        assertNotNull(taxAdapter.getValidPaymentMethodsByTemplate("IRS-941-PAYMENT"));
    }

}
