package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DeductionTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CompanyPayrollItemExtractTests {

    private static final ArrayList<String> supportedPaymentTemplates = new ArrayList<String>();
    static boolean extract = false;

    static {
        supportedPaymentTemplates.add("IRS-940-PAYMENT");
        supportedPaymentTemplates.add("IRS-941-PAYMENT");
        supportedPaymentTemplates.add("CA-PITSDI-PAYMENT");
        supportedPaymentTemplates.add("CA-UIETT-PAYMENT");
    }


    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        PayrollServices.commitUnitOfWork();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_PAYROLL_ITEMS_EXTRACT, "true");
        }
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_COMPANY_PAYROLL_ITEMS_EXTRACT, "false");
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.reinitialize();
        DataLoadServices.updateWageLimits("2012", "1");
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.


    @Test
    public void testCompanyPayrollItemExtractNPL() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt22 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-EE", "TTT22", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt23 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Family", "TTT23", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt24 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-FMLA", "TTT24", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt25 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Health", "TTT25", PayrollItemCode.OtherNonTaxableEmployerContribution);
        CompanyPayrollItem ttt28 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "National Paid Leave-Medicare", "TTT28", PayrollItemCode.OtherNonTaxableEmployerContribution);
        DataLoadServices.addCompanyLaws(company, "214");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.setPayPeriodEndDate(new DateDTO(2021,5,10));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt22.getSourcePayrollItemId(), null, new SpcfMoney("5.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt23.getSourcePayrollItemId(), null, new SpcfMoney("10.00")));
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt24.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));
        firstPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt28.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt25.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        DataLoadServices.setPSPDate(2020, 4, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 5, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("146.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "43.00", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "214", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();

        //Execute ATADataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFCompanyPayrollItemExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2020 2");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
    }

    @Test
    public void testCompanyPayrollItemExtractNPL_two() {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt26 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - EE", "TTT26", PayrollItemCode.Compensation);
        CompanyPayrollItem ttt27 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Retention - Health", "TTT27", PayrollItemCode.OtherNonTaxableEmployerContribution);
        DataLoadServices.addCompanyLaws(company, "215");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        firstPaycheck.getCompensationTransactions().add(DataLoadServices.createCompensationTransaction(ttt26.getSourcePayrollItemId(), null, new SpcfMoney("24.00")));

        PaycheckDTO secondPaycheck = paycheckDTOS.get(1);
        secondPaycheck.getEmployerContributionTransactions().add(DataLoadServices.createEmployerContributionTransaction(ttt27.getSourcePayrollItemId(), new BigDecimal(2), new BigDecimal(2), null, null));

        DataLoadServices.setPSPDate(2020, 2, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 3, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        assertEquals("Payroll Tax Amount", new SpcfMoney("176.00").toString(), payroll.getTaxDebit().getFinancialTransactionAmount().toString());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "13.00", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "215", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();

        //Execute ATADataExtract batch process for Quarterly Data
        ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                BatchJobType.ATFCompanyPayrollItemExtract, SpcfUniqueId.createInstance(true).toString(),
                ATFDataExtractRunType.QuarterlyData + " 2020 1");
        processor.executeJob();

        String extractBatchId = processor.getExtractBatchId();

        PayrollServices.beginUnitOfWork();
        ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));
        assertEquals("Extract Batch Status", ATFDataExtractBatchStatus.Completed, extractBatch.getBatchStatus());
    }

    @Test
    public void testCompanyPayrollItemExtractEEDeferral() throws Exception {
        String psid = "123456789";
        PayrollRunDTO payrollDTO = DataLoadServices.setupCompanyGetPayrollRunDTO(psid);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        // add national paid leave items
        CompanyPayrollItem ttt29 = DataLoadServices.persistPayrollItem(SourceSystemCode.QBDT, psid, "Employee Deferral", "TTT29", PayrollItemCode.Compensation);
        DataLoadServices.addCompanyLaws(company, "217");

        ArrayList<PaycheckDTO> paycheckDTOS = new ArrayList<>(payrollDTO.getPaychecks());
        PaycheckDTO firstPaycheck = paycheckDTOS.get(0);
        DeductionTransactionDTO deductionTransaction = DataLoadServices.createDeductionTransaction(ttt29.getSourcePayrollItemId());
        deductionTransaction.setDeductionAmount(new BigDecimal("-5.00"));
        deductionTransaction.setDeductionYTDAmount(new BigDecimal("-5.00"));
        firstPaycheck.getDeductionTransactions().add(deductionTransaction);

        DataLoadServices.setPSPDate(2020, 4, 28);
        payrollDTO.setTargetPayrollTXDate(new DateDTO(2020, 5, 1));

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        // persistence testing
        PayrollServices.beginUnitOfWork();
        PayrollRun payroll = PayrollRun.findPayrollRun(company, payrollDTO.getPayrollTXBatchId());
        assertNotNull(payroll);
        assertEquals("Payroll Status", PayrollStatus.Pending, payroll.getPayrollRunStatus());

        // Verify Agency Credits
        DomainEntitySet<FinancialTransaction> agencyTaxDebit = FinancialTransaction.getFinancialTransactions(payroll, TransactionTypeCode.AgencyTaxDebit, TransactionStateCode.Created);
        assertEquals("Number of Agency Debit Transactions", 1, agencyTaxDebit.size());

        assertEquals("FinancialTransaction Amount", "5.00", agencyTaxDebit.get(0).getFinancialTransactionAmount().toString());
        assertEquals("Law Id", "217", agencyTaxDebit.get(0).getLaw().getLawId());
        assertEquals("Payment Template", "IRS-941-PAYMENT", agencyTaxDebit.get(0).getLaw().getPaymentTemplate().getPaymentTemplateCd());
        assertEquals("Settlement Type for FinancialTransaction", SettlementType.ApplyForward, agencyTaxDebit.get(0).getSettlementTypeCd());
        PayrollServices.rollbackUnitOfWork();

        try {
            // todo this should use the new system parameter test util, but it's in a different pr
            DataLoadServices.updateSystemParameter(SystemParameter.Code.EE_TOTALS_CALC_PAYROLL_ITEMS, "true");
            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);
        } finally {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.EE_TOTALS_CALC_PAYROLL_ITEMS, "false");
        }

        ATFDataExtractTestsUtil.runExtractAndValidateFiles(ATFDataExtractRunType.QuarterlyData, 2020, 2,
                                                           new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_EETotals_EE_FICA_Deferral"),
                                                           new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.CompanyPayrollItemInfo, "atfextract/expected/test_PayrollItemExtract_EE_FICA_Deferral"));
    }
}
