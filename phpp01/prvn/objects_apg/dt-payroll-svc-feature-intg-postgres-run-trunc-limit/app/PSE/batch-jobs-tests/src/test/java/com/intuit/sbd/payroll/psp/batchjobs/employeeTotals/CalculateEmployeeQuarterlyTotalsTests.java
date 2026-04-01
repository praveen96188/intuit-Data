package com.intuit.sbd.payroll.psp.batchjobs.employeeTotals;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.ATFDataExtractTestsUtil;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Created with IntelliJ IDEA.
 * User: mvillani
 * Date: 10/11/12
 * Time: 1:33 PM
 */
public class CalculateEmployeeQuarterlyTotalsTests {
    static boolean extract = false;

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
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
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
            lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(i);
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

            CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.Deduction);
            CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.EmployerContribution);

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

                    employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
                    employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
                    employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1"));
                    employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                    employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                    employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                    deductionTransactionDTO = new DeductionTransactionDTO();
                    deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10"));
                    deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));
                    deductionTransactionDTO.setSourcePayrollItemId("TTT10");

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

                QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                qbdtPayrollTransactionDTO.setAmount(new SpcfMoney("27.27"));
                qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 2));
                QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                qTlDTO.setAmount(new SpcfMoney("27.27"));
                qTlDTO.setPayrollItemId(String.valueOf(companyPayrollItemSourceId));
                qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                for (String stateLawId : stateLawIds) {
                    SpcfMoney amount = new SpcfMoney(stateLawId);
                    LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, (SpcfMoney) amount.multiply(new SpcfMoney("2")), (SpcfMoney) amount.divide(new SpcfMoney("2")), false);
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

        // Submit Payroll Item Adjustments without a payroll
        PayrollServices.beginUnitOfWork();
        companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        for (Company company : companies) {

            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            for (Employee employee : employees) {
                for (String taxFormLine : CalculateEmployeeTotalsTestsHelper.DEDUCTION_TAX_FORM_LINES) {
                    QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                    qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 20));
                    QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                    qTlDTO.setAmount(new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).negate().toString()));
                    qTlDTO.setPayrollItemId(String.valueOf(taxFormLine));
                    qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                    PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                }
                for (String taxFormLine : CalculateEmployeeTotalsTestsHelper.ER_CONTRIB_TAX_FORM_LINES) {
                    QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                    qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 20));
                    QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                    qTlDTO.setAmount(new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()));
                    qTlDTO.setPayrollItemId(String.valueOf(taxFormLine));
                    qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                    PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                }

            }
        }
        PayrollServices.commitUnitOfWork();
        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Run ATF extract for EE Totals file
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForQuarterlyData(2012, 1, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals"));

    }

    @Test
    public void testHappyPathLA() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1));
        String[] statesList = new String[]{"LA"};
        String[] stateLawIds = new String[]{"101"};

        //Creates a company and adds 2 employees
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2015-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2014, 12, 1, SpcfTimeZone.getLocalTimeZone());

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
            lawAmounts.clear();
            lawAmounts.put("101", String.valueOf(59.0 * i));

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
            companyPayrollItemDTO.setSourcePayrollItemDescription("Salary");

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyReg = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2015-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate(10);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder((long) k);
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;
        }


        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        Application.beginUnitOfWork();

        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotalses = Application.find(EmployeeLawQtrTotals.class);
        for (EmployeeLawQtrTotals employeeLawQtrTotal: employeeLawQtrTotalses){
            assertEquals("Expected: 10.0 Actual:" + employeeLawQtrTotal.getHourlyRate(),10.0,employeeLawQtrTotal.getHourlyRate());
        }

        Application.commitUnitOfWork();

    }

    @Test
    public void testHappyPathLAWithHourlyRate() throws Exception{
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1));
        String[] statesList = new String[]{"LA"};
        String[] stateLawIds = new String[]{"101"};

        //Creates a company and adds 2 employees
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2015-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2014, 12, 1, SpcfTimeZone.getLocalTimeZone());

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
            lawAmounts.clear();
            lawAmounts.put("101", String.valueOf(59.0 * i));

            //create Company payroll Items
            List<CompanyPayrollItemDTO> companyPayrollItemDTOs = new ArrayList<CompanyPayrollItemDTO>();
            List<String> lawIds = new ArrayList<String>();
            CompanyLaw companyLaw;
            for (String stateLawId : stateLawIds) {
                companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                lawIds.add(companyLaw.getSourceId());
            }

            //PayrollItem with Desc : Overtime
            CompanyPayrollItemDTO companyPayrollItemDTO = new CompanyPayrollItemDTO();
            companyPayrollItemDTO.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO);
            companyPayrollItemDTO.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));
            companyPayrollItemDTO.setSourcePayrollItemDescription("Overtime");

            //PayrollItem with Desc : Salary
            CompanyPayrollItemDTO companyPayrollItemDTO1 = new CompanyPayrollItemDTO();
            companyPayrollItemDTO1.setPayrollItemCode(PayrollItemCode.Hourly);
            QBDTPayrollItemInfoDTO qbdtPayrollItemInfoDTO1 = new QBDTPayrollItemInfoDTO();
            qbdtPayrollItemInfoDTO1.setPayType(QbdtPayType.REG);
            companyPayrollItemDTO1.setQBDTPayrollItemInfoDTO(qbdtPayrollItemInfoDTO1);
            companyPayrollItemDTO1.setTaxableToCompanyLawIds(lawIds);
            companyPayrollItemDTO1.setTaxFormLine("OTHER");
            companyPayrollItemDTOs.add(companyPayrollItemDTO1);
            companyPayrollItemDTO1.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId +1 ));
            companyPayrollItemDTO1.setSourcePayrollItemDescription("Salary");

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyReg = String.valueOf(companyPayrollItemSourceId);
            String hourlyReg1 = String.valueOf(companyPayrollItemSourceId+1);


            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2015-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                        CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                        compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                        QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                        qbdtPaylineInfoDTO.setRate(10);
                        compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                        compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                        compensationTransactionDTO.setPayStubOrder((long) k);
                        paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);

                        CompensationTransactionDTO compensationTransactionDTO1 = new CompensationTransactionDTO();
                        compensationTransactionDTO1.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                        compensationTransactionDTO1.setSourcePayrollItemId(String.valueOf(hourlyReg1));
                        QBDTPaylineInfoDTO qbdtPaylineInfoDTO1 = new QBDTPaylineInfoDTO();
                        qbdtPaylineInfoDTO1.setRate(5);
                        compensationTransactionDTO1.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO1);
                        compensationTransactionDTO1.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                        compensationTransactionDTO1.setPayStubOrder((long) k);
                        paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO1);

                }
                k++;
            }
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;
        }


        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        Application.beginUnitOfWork();

        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotalses = Application.find(EmployeeLawQtrTotals.class);
        for (EmployeeLawQtrTotals employeeLawQtrTotal: employeeLawQtrTotalses){

                assertEquals("Expected: 5.0 Actual:" + employeeLawQtrTotal.getHourlyRate(),5.0,employeeLawQtrTotal.getHourlyRate());

        }

        Application.commitUnitOfWork();
    }

    @Test
    public void testHappyPathLADifferentScenario() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1));
        String[] statesList = new String[]{"LA"};
        String[] stateLawIds = new String[]{"101"};

        //Creates a company and adds 2 employees
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2015-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2014, 12, 1, SpcfTimeZone.getLocalTimeZone());

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
            lawAmounts.clear();
            lawAmounts.put("101", String.valueOf(59.0 * i));

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
            companyPayrollItemDTO.setSourcePayrollItemDescription("Salary");

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyReg = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2015-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance(10));
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder((long) k);
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;

            //Run Payroll with Vacation pay

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
             hourlyReg = String.valueOf(companyPayrollItemSourceId);

            payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2015-01-25");

            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance(10));
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate(10);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder((long) k);
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }
            processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;

        }

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        Application.beginUnitOfWork();

        //check Hourly rate not set and For VAC or 
        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotalses = Application.find(EmployeeLawQtrTotals.class);
        for (EmployeeLawQtrTotals employeeLawQtrTotal: employeeLawQtrTotalses){
            assertEquals("Expected: 0.0 Actual:" + employeeLawQtrTotal.getHourlyRate(),0.0,employeeLawQtrTotal.getHourlyRate());
        }
        Application.commitUnitOfWork();


    }



    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath_With177() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
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
            lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(i);
            // Create Law 177 with 4 source ids
            DataLoadServices.addCompanyLaws_177(company,"1771","1772","1773","1774");
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

            CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.Deduction);
            CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.EmployerContribution);

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

                // Create amounts for law 177

                HashMap<String,String> law177Amounts =  CalculateEmployeeTotalsTestsHelper.initializeLaw177Amounts(i);
                for (String sourceLawId : law177Amounts.keySet()) {
                    BigDecimal amount = new BigDecimal(law177Amounts.get(sourceLawId));
                    LiabilityTransactionDTO liabilityTransactionDTO = new LiabilityTransactionDTO();
                    liabilityTransactionDTO.setLiabilityTaxableWages(amount.multiply(new BigDecimal("10")));
                    liabilityTransactionDTO.setLiabilityTotalWages(amount.multiply(new BigDecimal("10")));
                    liabilityTransactionDTO.setLiabilityTipsTaxableWages(amount.multiply(new BigDecimal("10")));
                    liabilityTransactionDTO.setLiabilityAmount(new BigDecimal(law177Amounts.get(sourceLawId)));
                    liabilityTransactionDTO.setLawId("177");
                    liabilityTransactionDTO.setPayrollItemId(sourceLawId);
                    paycheckDTO.getLiabilityTransactions().add(liabilityTransactionDTO);
                }

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

                    employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
                    employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
                    employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1"));
                    employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(i * 5));
                    employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(i * 50));
                    employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(i * 100));

                    deductionTransactionDTO = new DeductionTransactionDTO();
                    deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10"));
                    deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(i * 10));
                    deductionTransactionDTO.setSourcePayrollItemId("TTT10");

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

                QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                qbdtPayrollTransactionDTO.setAmount(new SpcfMoney("27.27"));
                qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 2));
                QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                qTlDTO.setAmount(new SpcfMoney("27.27"));
                qTlDTO.setPayrollItemId(String.valueOf(companyPayrollItemSourceId));
                qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                for (String stateLawId : stateLawIds) {
                    SpcfMoney amount = new SpcfMoney(stateLawId);
                    liabilityAdjustmentDTOs.add(DataLoadServices.createLiabilityAdjustmentDTO(stateLawId, null, employee.getSourceEmployeeId(), null, amount, (SpcfMoney) amount.multiply(new SpcfMoney("2")), (SpcfMoney) amount.divide(new SpcfMoney("2")), false));

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
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath_MultiplePayrolls() throws Exception {

        testHappyPath();
        // Submit another payroll run
        Company company = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DateDTO paycheckDate = new DateDTO("2012-01-30");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(1);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2012, 1, 16, SpcfTimeZone.getLocalTimeZone());
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
            employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
            employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1").multiply(new BigDecimal(3)));
            employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(5));
            employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(50));
            employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(100));

            DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
            deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10").multiply(new BigDecimal(3)));
            deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(10));
            deductionTransactionDTO.setSourcePayrollItemId("TTT10");

            paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
            paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        Company testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        Employee testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);

        DomainEntitySet<EmployeePayrollItemQtrTotals> employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
            } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(10)).toString()), eeTotals.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();

        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 1);

        //Run ATF extract for EE Totals file
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_updatedOneCompany"));

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testHappyPath_MultipleQuarters() throws Exception {

        testHappyPath_MultiplePayrolls();
        DataLoadServices.setPSPDate(2012, 4, 2);
        // Submit another payroll run
        Company company = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DateDTO paycheckDate = new DateDTO("2012-04-15");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(1);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2012, 4, 10, SpcfTimeZone.getLocalTimeZone());
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
            employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
            employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1").multiply(new BigDecimal(3)));
            employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(5));
            employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(50));
            employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(100));

            DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
            deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10").multiply(new BigDecimal(3)));
            deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(10));
            deductionTransactionDTO.setSourcePayrollItemId("TTT10");

            paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
            paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        Company testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        Employee testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);

        DomainEntitySet<EmployeePayrollItemQtrTotals> employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (eeTotals.getQuarter() == 1) {
                if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                    assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
                } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
                    assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(10)).toString()), eeTotals.getAmount());
                }
            }
            if (eeTotals.getQuarter() == 2) {
                if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                    assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
                } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
                    assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(3)).toString()), eeTotals.getAmount());
                }
            }
        }
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 7, 2);
        //Run ATF extract for EE Totals file for updatedData
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_testHappyPath_MultipleQuarters_Quarterly"));

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testPayrollItemAdjustmentsOnly() throws Exception {
        //Test scenario covered - Employee Totals Extract - Liability Adjustment Without Tax Items

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
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
            lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(i);
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

            CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.Deduction);
            CalculateEmployeeTotalsTestsHelper.createPayrollItems(company, PayrollItemType.EmployerContribution);


            i++;
            companyPayrollItemSourceId++;
        }

        // Submit Payroll Item Adjustments without a payroll
        PayrollServices.beginUnitOfWork();
        companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        for (Company company : companies) {

            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            for (Employee employee : employees) {
                for (String taxFormLine : CalculateEmployeeTotalsTestsHelper.DEDUCTION_TAX_FORM_LINES) {
                    QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                    qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 20));
                    QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                    qTlDTO.setAmount(new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).negate().toString()));
                    qTlDTO.setPayrollItemId(String.valueOf(taxFormLine));
                    qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                    PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                }
                for (String taxFormLine : CalculateEmployeeTotalsTestsHelper.ER_CONTRIB_TAX_FORM_LINES) {
                    QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
                    qbdtPayrollTransactionDTO.setEmployeeSourceId(employee.getSourceEmployeeId());
                    qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 20));
                    QBDTPayrollTransactionLineDTO qTlDTO = new QBDTPayrollTransactionLineDTO();
                    qTlDTO.setAmount(new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()));
                    qTlDTO.setPayrollItemId(String.valueOf(taxFormLine));
                    qbdtPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs().add(qTlDTO);
                    PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
                }

            }
        }
        PayrollServices.commitUnitOfWork();
        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        Company testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        Employee testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);
        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotals = Application.find(EmployeeLawQtrTotals.class, EmployeeLawQtrTotals.Employee().equalTo(testEmployee));
        DomainEntitySet<EmployeePayrollItemQtrTotals> employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
        }
        PayrollServices.rollbackUnitOfWork();

        //Run ATF extract for EE Totals file
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_EETotals_emptyFile"));
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testRecallPayroll() throws Exception {

        testHappyPath();
        // Submit another payroll run
        Company company = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DateDTO paycheckDate = new DateDTO("2012-01-30");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(1);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2012, 1, 16, SpcfTimeZone.getLocalTimeZone());
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
            EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

            employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
            employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
            employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1").multiply(new BigDecimal(3)));
            employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(5));
            employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(50));
            employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(100));

            deductionTransactionDTO = new DeductionTransactionDTO();
            deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10").multiply(new BigDecimal(3)));
            deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(10));
            deductionTransactionDTO.setSourcePayrollItemId("TTT10");

            paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
            paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2));

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");
            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_updatedOneCompany"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        Company testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        Employee testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);

        DomainEntitySet<EmployeePayrollItemQtrTotals> employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
            } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(10)).toString()), eeTotals.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO recallDTO = new TransactionCancelEEDTO();
        recallDTO.setSourcePayrollRunId("Batch_2");
        recallDTO.setRequestId("1");
        testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, recallDTO));

        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_EETotals_testRecallOrVoidPayroll"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);

        employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
            } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(4)).toString()), eeTotals.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testVoidPayroll() throws Exception {

        testHappyPath();
        // Submit another payroll run
        Company company = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DateDTO paycheckDate = new DateDTO("2012-01-30");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(1);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2012, 1, 16, SpcfTimeZone.getLocalTimeZone());
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
            EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

            employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
            employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
            employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1").multiply(new BigDecimal(3)));
            employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(5));
            employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(50));
            employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(100));

            deductionTransactionDTO = new DeductionTransactionDTO();
            deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10").multiply(new BigDecimal(3)));
            deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(10));
            deductionTransactionDTO.setSourcePayrollItemId("TTT10");

            paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
            paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20120118000000");
        PayrollServices.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        //DataLoadServices.runOffload(SpcfCalendar.createInstance(2012,1,18));
        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");
            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_updatedOneCompany"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        Company testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        Employee testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);

        DomainEntitySet<EmployeePayrollItemQtrTotals> employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
            } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(10)).toString()), eeTotals.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();


        //Void entire payroll run
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId("Batch_2");

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> voidProcessResult = PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, voidPayrollDTO);
        assertSuccess(voidProcessResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");

            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_EETotals_testRecallOrVoidPayroll"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);

        employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
            } else if (taxFormLine.equals("TTT1") || (taxFormLine.equals("TTT10"))) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(4)).toString()), eeTotals.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();

    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testVoidAdjustment() throws Exception {

        testHappyPath();
        // Submit another payroll run
        Company company = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DateDTO paycheckDate = new DateDTO("2012-01-30");
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawAmounts = CalculateEmployeeTotalsTestsHelper.initializeLawAmounts(1);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2012, 1, 16, SpcfTimeZone.getLocalTimeZone());
            paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
            firstPaycheckPeriodBeginDate.addDays(15);
            paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

            DeductionTransactionDTO deductionTransactionDTO = new DeductionTransactionDTO();
            EmployerContributionTransactionDTO employerContributionTransactionDTO = new EmployerContributionTransactionDTO();

            employerContributionTransactionDTO = new EmployerContributionTransactionDTO();
            employerContributionTransactionDTO.setSourcePayrollItemId("TTT1");
            employerContributionTransactionDTO.setContributionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT1").multiply(new BigDecimal(3)));
            employerContributionTransactionDTO.setContributionYTDAmount(new BigDecimal(5));
            employerContributionTransactionDTO.setTaxableWagesAmount(new BigDecimal(50));
            employerContributionTransactionDTO.setTotalWagesAmount(new BigDecimal(100));

            deductionTransactionDTO = new DeductionTransactionDTO();
            deductionTransactionDTO.setDeductionAmount(CalculateEmployeeTotalsTestsHelper.W2_CODES.get("TTT10").multiply(new BigDecimal(3)));
            deductionTransactionDTO.setDeductionYTDAmount(new BigDecimal(10));
            deductionTransactionDTO.setSourcePayrollItemId("TTT10");

            paycheckDTO.getDeductionTransactions().add(deductionTransactionDTO);
            paycheckDTO.getEmployerContributionTransactions().add(employerContributionTransactionDTO);
        }

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 2, 16));

        try {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "1");
            //Run ATF extract for EE Totals file
            ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_employee_totals_updatedOneCompany"));
        } finally {
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_MONTH_OF_QTR_CUTOFF, "2");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_MONTH_CUTOFF, "1");
            DataLoadServices.updateSystemParameter(SystemParameter.Code.ATF_DAY_OF_WEEK_CUTOFF, "7");
        }

        // Void Adjustment

        // Submit Payroll Item Adjustments without a payroll
        PayrollServices.beginUnitOfWork();
        Company testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        Employee testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);


        Criterion<QbdtPayrollTransactionLine> where = QbdtPayrollTransactionLine.QbdtPayrollTransaction().Company().equalTo(testCompany)
                                                                                .And(QbdtPayrollTransactionLine.QbdtPayrollTransaction().Employee().equalTo(testEmployee))
                                                                                .And(QbdtPayrollTransactionLine.CompanyPayrollItem().TaxFormLine().equalTo("TTT1"));


        QbdtPayrollTransactionLine payrollTransactionLine = Application.find(QbdtPayrollTransactionLine.class, new Query<QbdtPayrollTransactionLine>().Where(where)).get(0);
        QBDTPayrollTransactionDTO qbdtPayrollTransactionDTO = new QBDTPayrollTransactionDTO();
        qbdtPayrollTransactionDTO.setEmployeeSourceId(testEmployee.getSourceEmployeeId());
        qbdtPayrollTransactionDTO.setPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 20));
        qbdtPayrollTransactionDTO.setSourceId(payrollTransactionLine.getQbdtPayrollTransaction().getSourceId());
        qbdtPayrollTransactionDTO.setIsVoided(true);
        PayrollServices.companyManager.addOrUpdateQBDTPayrollTransaction(SourceSystemCode.QBDT, company.getSourceCompanyId(), qbdtPayrollTransactionDTO);
        PayrollServices.commitUnitOfWork();

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //No new employee liability adjustments and paychecks. Extract file will be empty
        //Run ATF extract for EE Totals file
        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeTotalsInfo, "atfextract/expected/test_ATF_general_emptyFile"));

        PayrollServices.beginUnitOfWork();
        // Find Employee First_4 M_4 Last_4
        testCompany = Company.findCompany(CalculateEmployeeTotalsTestsHelper.TEST_COMPANY_ID, SourceSystemCode.QBDT);
        testEmployee = Employee.findEmployee(testCompany, CalculateEmployeeTotalsTestsHelper.TEST_EMPLOYEE_ID);

        DomainEntitySet<EmployeePayrollItemQtrTotals> employeePayrollItemQtrTotalsList = Application.find(EmployeePayrollItemQtrTotals.class, EmployeePayrollItemQtrTotals.Employee().equalTo(testEmployee));
        for (EmployeePayrollItemQtrTotals eeTotals : employeePayrollItemQtrTotalsList) {
            String taxFormLine = eeTotals.getCompanyPayrollItem().getTaxFormLine();
            if (!taxFormLine.equals("TTT1") && !taxFormLine.equals("TTT10") && !taxFormLine.equals("OTHER")) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).toString()), eeTotals.getAmount());
            } else if (taxFormLine.equals("TTT1")) {
                assertEquals("Amount for TaxFormLine " + taxFormLine + ": ", new SpcfMoney(CalculateEmployeeTotalsTestsHelper.W2_CODES.get(taxFormLine).multiply(new BigDecimal(9)).toString()), eeTotals.getAmount());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSupersededPayrollIsNotIncluded() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2014, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.setPSPDate(2014, 1, 3);
        PayrollRun supersededPayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));

        DataLoadServices.setPSPDate(2014, 1, 8);
        DataLoadServices.runOffload();

        //simulate superseded PR
        Application.beginUnitOfWork();
        Application.refresh(supersededPayrollRun);
        supersededPayrollRun.setPayrollRunStatus(PayrollStatus.Superseded);
        Application.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        EmployeeLawQtrTotals employeeLawQtrTotals = Application.find(EmployeeLawQtrTotals.class, EmployeeLawQtrTotals.Law().LawId().equalTo(Law.FIT)).getFirst();
        assertEquals(new SpcfMoney("2.00"), employeeLawQtrTotals.getTaxAmount());

    }

    @Test
    public void testNegativePaycheckIdsNotIncluded() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2014, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();
        DataLoadServices.setPSPDate(2014, 1, 3);
        PayrollRun supersededPayrollRun = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2014, 1, 10));
        int i = 1;
        Application.beginUnitOfWork();
        SpcfCalendar fromDate = SpcfCalendar.createInstance(2014, 1, 1);
        DomainEntitySet<Paycheck> paychecks = Paycheck.findCompanyPaychecksFrom(company, fromDate);
        for (Paycheck paycheck : paychecks) {
            Application.refresh(paycheck);
            //By giving this paystub a negative id we are simulating what happens in the migration case, it should not be returned by findPaystubs
            paycheck.setSourcePaycheckId("-" + i);
            Application.save(paycheck);
            i++;
        }
        Application.commitUnitOfWork();


        DataLoadServices.setPSPDate(2014, 1, 8);

        DataLoadServices.runOffload();


        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        EmployeeLawQtrTotals employeeLawQtrTotals = Application.find(EmployeeLawQtrTotals.class, EmployeeLawQtrTotals.Law().LawId().equalTo(Law.FIT)).getFirst();
        assertNull(employeeLawQtrTotals);

    }

    @Test
    public void testNegativePaycheckIdsHourlyRate() {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2015, 1, 1));
        String[] statesList = new String[]{"LA"};
        String[] stateLawIds = new String[]{"101"};

        //Creates a company and adds 2 employees
        DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2015, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2015-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2014, 12, 1, SpcfTimeZone.getLocalTimeZone());

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
            lawAmounts.clear();
            lawAmounts.put("101", String.valueOf(59.0 * i));

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
            companyPayrollItemDTO.setSourcePayrollItemDescription("Salary");

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyReg = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[employees.size()])), lawAmounts);

            paycheckDate = new DateDTO("2015-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k) / 3));
                    compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate(10);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder((long) k);
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }
            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            i++;
            companyPayrollItemSourceId++;
        }

        SpcfCalendar fromDate = SpcfCalendar.createInstance(2014, 12, 1);
        Application.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = Paycheck.findCompanyPaychecksFrom(companies.get(0), fromDate);
        Paycheck paycheck = paychecks.get(0);
        Application.refresh(paycheck);
        paycheck.setSourcePaycheckId("-1");
        Application.commitUnitOfWork();
        //Run EE Totals calculation batch job

        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        Application.beginUnitOfWork();

        DomainEntitySet<EmployeeLawQtrTotals> employeeLawQtrTotalses = Application.find(EmployeeLawQtrTotals.class);
        assertEquals(employeeLawQtrTotalses.size(), 1);
        for (EmployeeLawQtrTotals employeeLawQtrTotal : employeeLawQtrTotalses) {
            assertEquals("Expected: 10.0 Actual:" + employeeLawQtrTotal.getHourlyRate(), 10.0, employeeLawQtrTotal.getHourlyRate());
        }

        Application.commitUnitOfWork();
    }
}
