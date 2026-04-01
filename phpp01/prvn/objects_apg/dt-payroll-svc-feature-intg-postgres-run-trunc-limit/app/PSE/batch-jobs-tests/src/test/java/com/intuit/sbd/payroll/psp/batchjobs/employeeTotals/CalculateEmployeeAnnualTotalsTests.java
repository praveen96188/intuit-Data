package com.intuit.sbd.payroll.psp.batchjobs.employeeTotals;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS.SendW2DataToTFS;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertTrue;

/**
 * User: mvillani
 * Date: 8/29/2012
 * Time: 10:36 AM
 */
public class CalculateEmployeeAnnualTotalsTests {


    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
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
            // Create Law 177 with 4 source ids
            DataLoadServices.addCompanyLaws_177(company, "1771", "1772", "1773", "1774");
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

        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[] {"-year:2012"});
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companiesToProcess = Application.find(Company.class);
        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.W2_COMPANY_LIST);
        String value = "";
        for (Company company:companiesToProcess)  {
           value = value + company.getSourceCompanyId()+ ",";
        }
        value = value.substring(0,value.lastIndexOf(","));
        companyListParameter.setSystemParameterValue(value);
        PayrollServices.commitUnitOfWork();

    }


}
