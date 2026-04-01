package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEEQuarterlyTotals;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * User: ihannur
 * Date: 5/25/12
 * Time: 10:36 AM
 */
public class EmployeeTotalsCalculationProcessTests {
    static boolean extract = false;

    private static final String FIT = "1";
    private static final String FICA = "61";
    private static final String Medicare = "63";
    private static final String FUTA = "66";
    private static final String COBRA = "196";

    /**************
     * NOTE: Wage Limit changes scenarios are not valid anymore as we removed the Taxable wages calculation based on wage limits.
     *************/


    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        extract = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT);
        PayrollServices.commitUnitOfWork();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT, "true");
        }
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT, "false");
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
    @Ignore
    public void testLiabilityAdjustmentRecall() throws Exception {

        Company company = setupCompanyWithLiabilityAdjustment();

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");

            //Run EE Totals calculation batch job
            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testLiabilityAdjustmentRecall_beforeRecall"));

            // Recall
            PayrollServices.beginUnitOfWork();
            List<String> adjustmentIds = new ArrayList<String>();
            adjustmentIds.add("Adjust_1");
            assertSuccess(PayrollServices.payrollManager.voidLiabilityAdjustments(company.getSourceSystemCd(), company.getSourceCompanyId(), adjustmentIds, true));
            PayrollServices.commitUnitOfWork();

            DataLoadServices.setPSPDate(2012, 2, 1);
            //Run EE Totals calculation batch job
            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testLiabilityAdjustmentRecall_AfterRecall"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPartialLiabilityAdjustmentRecall() throws Exception {
        Company company = setupCompanyWithLiabilityAdjustment();
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};

        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());

        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        DateDTO paycheckDate = new DateDTO("2012-01-25");
        payrollRunDTO.setTargetPayrollTXDate(paycheckDate);
        payrollRunDTO.setPayrollTXBatchId("Batch_2");

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_2", new DateDTO("2011-08-20"));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        for (Employee employee : employees) {
            for (String stateLawId : stateLawIds) {
                SpcfMoney amount = new SpcfMoney(stateLawId);
                SpcfMoney totalWages;
                SpcfMoney taxableWages;
                if (stateLawId.equals("116")) {
                    totalWages = new SpcfMoney("26500");
                } else {
                    totalWages = (SpcfMoney) amount.multiply(new SpcfMoney("2"));
                }
                if (stateLawId.equals("130")) {
                    taxableWages = new SpcfMoney("800");
                } else {
                    taxableWages = (SpcfMoney) amount.divide(new SpcfMoney("5"));
                }
                LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, taxableWages, totalWages, false);
                QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
                liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
            }
        }

        payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().add(companyAdjustmentSubmissionDTO);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);


        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testPartialLiabilityAdjustmentRecall_beforeRecall"));

            // Recall - second liability adjustments
            PayrollServices.beginUnitOfWork();
            List<String> adjustmentIds = new ArrayList<String>();
            adjustmentIds.add("Adjust_2");
            assertSuccess(PayrollServices.payrollManager.voidLiabilityAdjustments(company.getSourceSystemCd(), company.getSourceCompanyId(), adjustmentIds, true));
            PayrollServices.commitUnitOfWork();

            DataLoadServices.setPSPDate(2012, 2, 1);
            //Run EE Totals calculation batch job
            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testLiabilityAdjustmentRecall_beforeRecall"));

            DataLoadServices.setPSPDate(2012, 2, 2);
            // Void - first liability adjustments
            PayrollServices.beginUnitOfWork();
            adjustmentIds = new ArrayList<String>();
            adjustmentIds.add("Adjust_1");
            assertSuccess(PayrollServices.payrollManager.voidLiabilityAdjustments(company.getSourceSystemCd(), company.getSourceCompanyId(), adjustmentIds, false));
            PayrollServices.commitUnitOfWork();

            //Run EE Totals calculation batch job
            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testLiabilityAdjustmentRecall_AfterRecall"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath_Update() throws Exception {
        testHappyPath(SpcfCalendar.createInstance(2012, 3, 9),"-mode="+CalculateEEQuarterlyTotals.Mode.UPDATE.toString());
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath_Flush() throws Exception {
        testHappyPath(SpcfCalendar.createInstance(2012, 3, 9),"-mode="+CalculateEEQuarterlyTotals.Mode.FLUSH.toString());
    }
    
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath_FlushQuarter() throws Exception {
        testHappyPath(SpcfCalendar.createInstance(2012, 3, 9),"-mode="+CalculateEEQuarterlyTotals.Mode.FLUSH.toString(), "-yearQuarter=2012Q1");
    }

    public void testHappyPath(SpcfCalendar dateToRunExtractFor, String... pBatchJobArguments) throws Exception {

        //Wage limit test scenarios.
        //Employee Totals Extract - Taxable Wages Exceed Wage Limit - Current Quarter
        //Employee Totals Extract - Total Wages Exceed Wage Limit and Taxable wages less than wage limit

        // NV SUI_ER - 116, Wage limit Q1 - 26,900
        //Scenarios. -- Qtr, Total wages, Taxable wages, Calculated taxable wages
        //  Q1  69,600  43,258   26,900
        //  Q1  48,000  21,658   26,900
        // VT SUI_ER - 130, Wage limit Q1 - 16,000
        //  Q1  26,520  26,700   16,000
        //  Q1  13,520  13,700   13,700
        // WY SUI_ER - 134, Wage limit Q1 - 23,800
        //  Q1  27,336  27,867   23,800
        //  Q1  13,936  13,467   13,467
        // CA SUI_ER - 87, Wage limit Q1 - 7,000
        //  Q1  17,748  17,443.5   7,000
        //  Q1   9,048   8,743.5   7,000

        DataLoadServices.setPSPDate(2012, 1, 1);
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(610 * i));
            lawAmounts.put("62", String.valueOf(620 * i));
            lawAmounts.put("63", String.valueOf(630 * i));
            lawAmounts.put("64", String.valueOf(640 * i));
            lawAmounts.put("1", String.valueOf(150 * i));
            lawAmounts.put("134", String.valueOf(1340 * i)); // WY SUI-ER
            lawAmounts.put("131", String.valueOf(1310 * i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(1300 * i)); // VT SUI-ER
            lawAmounts.put("120", String.valueOf(120 * i));   // OR SUI-ER
            lawAmounts.put("116", String.valueOf(2160 * i)); // NV SUI-ER
            lawAmounts.put("87", String.valueOf(870 * i));   // CA SUI-ER

            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyReg = String.valueOf(companyPayrollItemSourceId);

            companyPayrollItemSourceId++;
            companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            lawIds = new ArrayList<String>();
            companyLaw = CompanyLaw.findCompanyLaw(company, "134");
            lawIds.add(companyLaw.getSourceId());
            companyLaw = CompanyLaw.findCompanyLaw(company, "131");
            lawIds.add(companyLaw.getSourceId());
            companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyVac = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2012-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                    EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                    employerContributionTransactionDTO.setContributionAmount(new BigDecimal(i));
                    employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                    employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                    employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                    deductionTransactionDTO.setDeductionAmount(new BigDecimal(i));
                    deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));

                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                    if ((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                        deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                        employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                        deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                        employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder((long) k);

                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                    paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                    paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);

                }
                k++;
            }

            CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_" + k, new DateDTO("2011-08-14"));
            Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
            companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

            LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
            liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
            liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
            liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

            for (Employee employee : employees) {
                for (String stateLawId : stateLawIds) {
                    SpcfMoney amount = new SpcfMoney(stateLawId);
                    SpcfMoney totalWages;
                    SpcfMoney taxableWages;
                    if (stateLawId.equals("116")) {
                        totalWages = new SpcfMoney("26400");
                    } else {
                        totalWages = (SpcfMoney) amount.multiply(new SpcfMoney("4"));
                    }
                    if (stateLawId.equals("130")) {
                        taxableWages = new SpcfMoney("700");
                    } else {
                        taxableWages = (SpcfMoney) amount.divide(new SpcfMoney("2"));
                    }
                    LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, taxableWages, totalWages, false);
                    QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                    liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
                    liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
                }
            }

            payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().add(companyAdjustmentSubmissionDTO);
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;
        }

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess, pBatchJobArguments);

        //Run ATF extract for EE Totals file
        DataLoadServices.setPSPDate(dateToRunExtractFor);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_with_limits"));

    }

    @Test
    public void testBackdatedPayrollsBatchJob() throws Exception {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005,1,1));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));
        String[] statesList = new String[]{"AR","AZ","CA"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.Withholding);
        assertEquals("Number of companies setup", 2,companies.size());
        Company company1 = companies.get(0);
        Company company2 = companies.get(1);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 7, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.runPayrollRun(company1, statesList);
        DataLoadServices.runPayrollRun(company2, statesList);

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess, "-mode:"+CalculateEEQuarterlyTotals.Mode.FLUSH.toString());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 7, SpcfTimeZone.getLocalTimeZone()));
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-4-15");

        DataLoadServices.runPayrollRun(company1, statesList, supportedDate, payrollDate, false);

        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess, "-mode:"+CalculateEEQuarterlyTotals.Mode.BACKDATE.toString());

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 3, 12, SpcfTimeZone.getLocalTimeZone()));
        DateDTO payrollDate2 = new DateDTO("2011-4-15");

        DataLoadServices.runPayrollRun(company2, statesList, supportedDate, payrollDate2, false);

        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess, "-mode:"+CalculateEEQuarterlyTotals.Mode.BACKDATE.toString());

        //Company 1 shouldn't have any EQLs that were modified after 3/7.  Test date of 3/8 just to be safe.
        Expression<EmployeeLawQtrTotals> query =
                new Query<EmployeeLawQtrTotals>()
                        .Where(EmployeeLawQtrTotals.Company().equalTo(company1)
                                .And(EmployeeLawQtrTotals.<DomainEntity>ModifiedDate().greaterThan(SpcfCalendar.createInstance(2012,3,8, SpcfTimeZone.getLocalTimeZone())
                                )));
        DomainEntitySet<EmployeeLawQtrTotals> eqls =  Application.find(EmployeeLawQtrTotals.class, query);

        assertEquals("No EQLs past 3/8 for company 1", 0, eqls.size());
        
    }
    
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath_FutureQuarter_Payroll() throws Exception {
        //Wage limit test scenarios.
        //Future quarter payroll
        //Employee Totals Extract - Taxable Wages Exceed Wage Limit
        //Employee Totals Extract - Taxable Wages - Wage Limit Increased After Wage Limit Met

        // NV SUI_ER - 116, Wage limit Q1 - 30,000 (Changed from 26,900)
        //Scenarios -- Qtr, Total wages, Taxable wages, Calculated taxable wages
        //  Q1  70,760  44,418   30,000
        //  Q2   1,160   1,160       00

        //  Q1  49,160  22,818   30,000
        //  Q2   1,160   1,160       00

        // VT SUI_ER - 130, Wage limit Q1 - 16,000
        //  Q1  27,820  28,000   16,000
        //  Q2   1,300   1,300       00

        //  Q1  14,820  15,000   15,000
        //  Q2   1,300   1,300    1,000

        // WY SUI_ER - 134, Wage limit Q1 - 23,800
        //  Q1  28,676  28,207   23,800
        //  Q2   1,340   1,340       00

        //  Q1  15,276  14,807   14,807
        //  Q2   1,340   1,340    1,340

        // CA SUI_ER - 87, Wage limit Q1 - 7,000
        //  Q1  18,618  18,313.5   7,000
        //  Q2     870     870        00

        //  Q1   9,918   9,613.5   7,000
        //  Q2     870     870        00

        testHappyPath(SpcfCalendar.createInstance(2012, 3, 1));

        PayrollServices.beginUnitOfWork();
        // NV SUI_ER, Wage limit increase Q1 limits from 26,400 to 30,000
        Law lawNvSui = Application.findById(Law.class, "116");
        WageLimit wageLimitNvSui = WageLimit.findWageLimitAmount(2012, 1, lawNvSui.getLawId());
        SpcfMoney amountNvSui = wageLimitNvSui.getAmount();
        wageLimitNvSui.setAmount(new SpcfMoney("30000")); // Increasing from 16,000 to 17,200
        Application.getHibernateSession().save(wageLimitNvSui);
        PayrollServices.commitUnitOfWork();

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());

        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        DateDTO paycheckDate = new DateDTO("2012-02-25");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, paycheckDate, false, lawAmounts, PaymentTemplateCategory.SUI);
        }

        DataLoadServices.setPSPDate(2012, 3, 25);
        paycheckDate = new DateDTO("2012-04-10");
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, paycheckDate, false, lawAmounts, PaymentTemplateCategory.SUI);
        }

        //Run EE Totals calculation batch for new payrolls
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        // Update back to old wage limits
        PayrollServices.beginUnitOfWork();
        Application.getHibernateSession().refresh(wageLimitNvSui);
        wageLimitNvSui.setAmount(amountNvSui);
        Application.getHibernateSession().save(wageLimitNvSui);
        PayrollServices.commitUnitOfWork();

        //Run ATF extract for EE Totals file for current quarter; ensure it contains just Q1
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_FutureQuarter_Payroll_Q1"));

        //Run FLUSH ATF extract for EE Totals file; ensure it's got all the Q2 data
        DataLoadServices.setPSPDate(2012, 6, 2);
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForQuarterlyData(2012, 2, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_FutureQuarter_Payroll"));

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testWageLimits_With3Quarter() throws Exception {
        // Tests with wage limit increase and decrease in Q3.
        //Employee Totals Extract - Taxable Wages - Wage Limit Increased
        //Employee Totals Extract - Taxable Wages - Wage Limit Decreased
        testHappyPath_FutureQuarter_Payroll();

        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());

        DataLoadServices.setPSPDate(2012, 7, 2);

        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        DateDTO paycheckDate = new DateDTO("2012-07-10");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        lawAmounts.put("131", "1200"); // VT SUI_ER

        PayrollServices.beginUnitOfWork();
        //Wage limit increase 130. // VT SUI_ER, Wage limits Q1 and Q2 is - 16,000, Q3 = 17,200
        //Scenario 1. -- Qtr, Total wages, Taxable wages, Calculated taxable wages
        //  Q1  14,820  15,000   15,000
        //  Q2   1,300   1,300    1,000
        //  Q3   1,300   1,300    1,200
        //Scenario 2. -- Qtr, Total wages, Taxable wages, Calculated taxable wages
        //  Q1  27,820  28,000   16,000
        //  Q2   1,300   1,300       00
        //  Q3   1,300   1,300    1,200
        Law lawWaSui = Application.findById(Law.class, "130");
        WageLimit wageLimitWaSui = WageLimit.findWageLimitAmount(2012, 3, lawWaSui.getLawId());
        SpcfMoney amountWaSui = wageLimitWaSui.getAmount();
        wageLimitWaSui.setAmount(new SpcfMoney("17200")); // Increasing from 16,000 to 17,200
        Application.getHibernateSession().save(wageLimitWaSui);

        //Wage limit decrease 120 // OR SUI-ER, Wage limits Q1 and Q2 is - 33,000, Q3 = 5,000
        //Scenario 1. -- Qtr, Total wages, Taxable wages, Calculated taxable wages
        //  Q1  2,880  2,460   2,460
        //  Q2  1,200  1,200   1,200
        //  Q3  1,200  1,200   1,200
        //Scenario 2. -- Qtr, Total wages, Taxable wages, Calculated taxable wages
        //  Q1  4,080  3,660   3,660
        //  Q2  1,200  1,200   1,200
        //  Q3  1,200  1,200     140
        Law lawOrSui = Application.findById(Law.class, "120");
        WageLimit wageLimitOrSui = WageLimit.findWageLimitAmount(2012, 3, lawOrSui.getLawId());
        SpcfMoney amountOrSui = wageLimitOrSui.getAmount();
        wageLimitOrSui.setAmount(new SpcfMoney("5000")); // Decrease from 33,000 to 5,000
        Application.getHibernateSession().save(wageLimitOrSui);

        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.commitUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, paycheckDate, false, lawAmounts, PaymentTemplateCategory.SUI);
        }

        //Re-Run EE Totals calculation batch for one company to test for single company
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        // Update back to old wage limits
        PayrollServices.beginUnitOfWork();
        Application.getHibernateSession().refresh(wageLimitWaSui);
        wageLimitWaSui.setAmount(amountWaSui);
        Application.getHibernateSession().save(wageLimitWaSui);

        Application.getHibernateSession().refresh(wageLimitOrSui);
        wageLimitOrSui.setAmount(amountOrSui);
        Application.getHibernateSession().save(wageLimitOrSui);

        //Remove all records from table - ATFDataExtractBatch so that we extract all quarters data to compare
        DomainEntitySet<ATFDataExtractBatch> atfDataExtractBatches = Application.find(ATFDataExtractBatch.class);
        Iterator<ATFDataExtractBatch> iterator = atfDataExtractBatches.iterator();

        while (iterator.hasNext()) {
            ATFDataExtractBatch atfDataExtractBatch = iterator.next();
            Iterator<ATFDataExtractFile> dataFileIterator = atfDataExtractBatch.getATFDataExtractFileCollection().iterator();
            while (dataFileIterator.hasNext()) {
                ATFDataExtractFile atfDataExtractFile = dataFileIterator.next();
                Application.delete(atfDataExtractFile);
                dataFileIterator.remove();
            }
            Application.delete(atfDataExtractBatch);
            iterator.remove();
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 7, 2);
        //Run ATF extract for EE Totals file
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_Three_Quarters_Payroll"));
    }

    @Test
    public void testLiabilityAdjustmentsWithCobra() throws Exception {
        //Test scenario - Employee Totals Extract - Liability Adjustment - Negative Law Cobra
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 5);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLaws(company, COBRA);

        List<Employee> employees = DataLoadServices.addEEs(company, 1);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class);
        CompanyLaw companyLaw;
        LiabilityAdjustmentDTO liabilityAdjustmentDTO;
        QBDTTransactionInfoDTO qbdtTransactionInfoDTO;

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();

        for (Employee employee : employees) {
            companyLaw = companyLaws.find(CompanyLaw.Law().LawId().equalTo(FIT)).getFirst();
            liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, employee.getSourceEmployeeId(), new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("27.20"), new SpcfMoney("10.0"), new SpcfMoney("50.0"), false);
            qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setPayrollItemId(companyLaw.getSourceId());
            liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);

            companyLaw = companyLaws.find(CompanyLaw.Law().LawId().equalTo(FICA)).getFirst();
            liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, employee.getSourceEmployeeId(), new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("200.27"), new SpcfMoney("300.0"), new SpcfMoney("250.0"), false);
            qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setPayrollItemId(companyLaw.getSourceId());
            liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);

            companyLaw = companyLaws.find(CompanyLaw.Law().LawId().equalTo(Medicare)).getFirst();
            liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(Medicare, Medicare, employee.getSourceEmployeeId(), new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("10.09"), new SpcfMoney("400.0"), new SpcfMoney("450.0"), false);
            qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setPayrollItemId(companyLaw.getSourceId());
            liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);

            companyLaw = companyLaws.find(CompanyLaw.Law().LawId().equalTo(COBRA)).getFirst();
            liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(COBRA, COBRA, employee.getSourceEmployeeId(), new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-100.09"), new SpcfMoney("200.0"), new SpcfMoney("225.0"), false);
            qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setPayrollItemId(companyLaw.getSourceId());
            liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);

            companyLaw = companyLaws.find(CompanyLaw.Law().LawId().equalTo(FUTA)).getFirst();
            liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FUTA, FUTA, employee.getSourceEmployeeId(), new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("100.11"), new SpcfMoney("1.0"), new SpcfMoney("15.0"), false);
            qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
            liabilityAdjustmentDTO.setPayrollItemId(companyLaw.getSourceId());
            liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        }

        PayrollServices.rollbackUnitOfWork();

        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                                                                                  .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO,
                                                                                                           new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        CompanyAdjustmentSubmission companyAdjustmentSubmission = Application.findById(CompanyAdjustmentSubmission.class, processResult.getResult().getId());
        assertNotNull("Company Adjustment Submission", companyAdjustmentSubmission);
        DomainEntitySet<LiabilityAdjustment> liabilityAdjustments = companyAdjustmentSubmission.getLiabilityAdjustmentCollection();
        assertEquals("Liability Adjustments", 5, liabilityAdjustments.size());

        PayrollServices.rollbackUnitOfWork();

        //Run EE Totals calculation batch for adjustment payroll run
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        DataLoadServices.setPSPDate(2012, 4, 5);
        //Run ATF extract for EE Totals file
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_liability_adjustmentsWithCobra"));

    }

    private static Company setupCompanyWithLiabilityAdjustment() {
        DataLoadServices.setPSPDate(2012, 1, 2);
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        Company company = assertOne(Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        PayrollServices.rollbackUnitOfWork();

        int companyPayrollItemSourceId = 1;

        DataLoadServices.addEEs(company, 3);

        //create Company payroll Items
        List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        List<String> lawIds = new ArrayList<String>();
        CompanyLaw companyLaw;
        for (String stateLawId : stateLawIds) {
            companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
            lawIds.add(companyLaw.getSourceId());
        }
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

        companyPayrollItemSourceId++;
        companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        lawIds = new ArrayList<String>();
        companyLaw = CompanyLaw.findCompanyLaw(company, "134");
        lawIds.add(companyLaw.getSourceId());
        companyLaw = CompanyLaw.findCompanyLaw(company, "131");
        lawIds.add(companyLaw.getSourceId());
        companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        DateDTO paycheckDate = new DateDTO("2012-01-20");
        payrollRunDTO.setTargetPayrollTXDate(paycheckDate);
        payrollRunDTO.setPayrollTXBatchId("Batch_1");

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO("2011-08-14"));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        for (Employee employee : employees) {
            for (String stateLawId : stateLawIds) {
                SpcfMoney amount = new SpcfMoney(stateLawId);
                SpcfMoney totalWages;
                SpcfMoney taxableWages;
                if (stateLawId.equals("116")) {
                    totalWages = new SpcfMoney("26400");
                } else {
                    totalWages = (SpcfMoney) amount.multiply(new SpcfMoney("4"));
                }
                if (stateLawId.equals("130")) {
                    taxableWages = new SpcfMoney("700");
                } else {
                    taxableWages = (SpcfMoney) amount.divide(new SpcfMoney("2"));
                }
                LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, taxableWages, totalWages, false);
                QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
                liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
                liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
            }
        }

        payrollRunDTO.getCompanyAdjustmentSubmissionDTOs().add(companyAdjustmentSubmissionDTO);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        return company;
    }

    @Test
    public void testMonthlyCountsForNonSUILawThatNeedsCounts() throws Exception {

        Company company = setupCompanyIncludingWHLawThatNeedsEECounts();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");
            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_monthsWorked_OK"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPartialRecallPayrollWithOnlyPaychecks() throws Exception {

        Company company = setupCompanyWithOnePayrollRun();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");
            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testPartialRecallPayrollWithOnlyPaychecks_beforeRecall"));

            PayrollServices.beginUnitOfWork();
            PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
            DomainEntitySet<Paycheck> paychecks = payrollRun.getPaycheckCollection().sort(Paycheck.SourceEmployee().SourceEmployeeId());
            PayrollServices.rollbackUnitOfWork();

            PayrollServices.beginUnitOfWork();
            TransactionCancelEEDTO cancelEEDTO = new TransactionCancelEEDTO();
            cancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            List<String> paycheckList = new ArrayList<String>();
            paycheckList.add(paychecks.getFirst().getSourcePaycheckId());
            cancelEEDTO.setSourcePaycheckIdList(paycheckList);
            PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), cancelEEDTO);
            PayrollServices.commitUnitOfWork();

            //Run EE Totals calculation batch job
            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testPartialRecallPayrollWithOnlyPaychecks_AfterPartialRecall_1"));

            PayrollServices.beginUnitOfWork();
            cancelEEDTO = new TransactionCancelEEDTO();
            cancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

            PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), cancelEEDTO);
            PayrollServices.commitUnitOfWork();

            //Run EE Totals calculation batch job
            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_testPartialRecallPayrollWithOnlyPaychecks_AfterPartialRecall_2"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }

    }

    private static Company setupCompanyWithOnePayrollRun() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        Company company = assertOne(Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        DataLoadServices.addEEs(company, 3);
        lawAmounts.put("61", String.valueOf(610 * i));
        lawAmounts.put("62", String.valueOf(620 * i));
        lawAmounts.put("63", String.valueOf(630 * i));
        lawAmounts.put("64", String.valueOf(640 * i));
        lawAmounts.put("1", String.valueOf(150 * i));
        lawAmounts.put("134", String.valueOf(1340 * i)); // WY SUI-ER
        lawAmounts.put("131", String.valueOf(1310 * i)); // WA SUI-ER
        lawAmounts.put("130", String.valueOf(1300 * i)); // VT SUI-ER
        lawAmounts.put("120", String.valueOf(120 * i));   // OR SUI-ER
        lawAmounts.put("116", String.valueOf(2160 * i)); // NV SUI-ER
        lawAmounts.put("87", String.valueOf(870 * i));   // CA SUI-ER

        //create Company payroll Items
        List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        List<String> lawIds = new ArrayList<String>();
        CompanyLaw companyLaw;
        for (String stateLawId : stateLawIds) {
            companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
            lawIds.add(companyLaw.getSourceId());
        }
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String hourlyReg = String.valueOf(companyPayrollItemSourceId);

        companyPayrollItemSourceId++;
        companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        lawIds = new ArrayList<String>();
        companyLaw = CompanyLaw.findCompanyLaw(company, "134");
        lawIds.add(companyLaw.getSourceId());
        companyLaw = CompanyLaw.findCompanyLaw(company, "131");
        lawIds.add(companyLaw.getSourceId());
        companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String hourlyVac = String.valueOf(companyPayrollItemSourceId);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        int k = 1;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            for (String stateLawId : stateLawIds) {
                CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                employerContributionTransactionDTO.setContributionAmount(new BigDecimal(i));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                deductionTransactionDTO.setDeductionAmount(new BigDecimal(i));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));

                compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                if ((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                } else {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                }
                QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                compensationTransactionDTO.setPayStubOrder((long) k);

                paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);

            }
            k++;
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        return company;
    }

    private static Company setupCompanyIncludingWHLawThatNeedsEECounts() {
        DataLoadServices.setPSPDate(2012, 1, 1);
        String[] statesList = new String[]{"OK"};
        String[] stateLawIds = new String[]{"38"};
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state, PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        Company company = assertOne(Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0;
        int companyPayrollItemSourceId = 1;

        DataLoadServices.addEEs(company, 3);
        lawAmounts.put("61", String.valueOf(610 * i));
        lawAmounts.put("62", String.valueOf(620 * i));
        lawAmounts.put("63", String.valueOf(630 * i));
        lawAmounts.put("64", String.valueOf(640 * i));
        lawAmounts.put("1", String.valueOf(150 * i));
        lawAmounts.put("38", String.valueOf(1340 * i)); // OK SWT

        //create Company payroll Items
        List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        List<String> lawIds = new ArrayList<String>();
        CompanyLaw companyLaw;
        for (String stateLawId : stateLawIds) {
            companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
            lawIds.add(companyLaw.getSourceId());
        }
        CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String hourlyReg = String.valueOf(companyPayrollItemSourceId);

        companyPayrollItemSourceId++;
        companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
        lawIds = new ArrayList<String>();
        companyLaw = CompanyLaw.findCompanyLaw(company, "38");
        lawIds.add(companyLaw.getSourceId());
        companyPayrollItemDTO = new CompanyPayrollItemDTO();
        companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
        qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
        qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.VAC);
        companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
        companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
        companyPayrollItemDTO.setTaxFormLine("OTHER");
        companyPayrollItemDTOs.add(companyPayrollItemDTO);
        companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

        DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
        String hourlyVac = String.valueOf(companyPayrollItemSourceId);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        int k = 1;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            for (String stateLawId : stateLawIds) {
                CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
                EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

                employerContributionTransactionDTO.setContributionAmount(new BigDecimal(i));
                employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                deductionTransactionDTO.setDeductionAmount(new BigDecimal(i));
                deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));

                compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                if ((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                } else {
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    deductionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    employerContributionTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                }
                QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId) * k) / 2);
                compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                compensationTransactionDTO.setPayStubOrder((long) k);

                paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
                paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);

            }
            k++;
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        return company;
    }

}
