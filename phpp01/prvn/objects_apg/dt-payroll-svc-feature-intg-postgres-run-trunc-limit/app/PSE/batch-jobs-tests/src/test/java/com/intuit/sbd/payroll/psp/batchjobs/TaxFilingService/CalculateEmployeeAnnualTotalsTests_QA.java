package com.intuit.sbd.payroll.psp.batchjobs.TaxFilingService;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEmployeeAnnualTotals;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertTrue;


/**
 * User: vbindage
 * Date: 9/06/2012
 * Time: 08:13 AM
 */
public class CalculateEmployeeAnnualTotalsTests_QA {


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
    public void testAnnualTotals() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(6.1 * i));
            lawAmounts.put("62", String.valueOf(6.2 * i));
            lawAmounts.put("63", String.valueOf(6.3 * i));
            lawAmounts.put("64", String.valueOf(6.4*i));
            lawAmounts.put("1", String.valueOf(1.5*i));
            lawAmounts.put("134", String.valueOf(13.4*i)); // WY SUI-ER
            lawAmounts.put("131", String.valueOf(13.1*i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(13.0*i)); // VT SUI-ER
            lawAmounts.put("120", String.valueOf(12*i));   // OR SUI-ER
            lawAmounts.put("116", String.valueOf(11.6*i)); // NV SUI-ER
            lawAmounts.put("87", String.valueOf(8.7*i));   // CA SUI-ER

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
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyVac = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);

            paycheckDate = new DateDTO("2012-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }

            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            DataLoadServices.setPSPDate(2012, 4, 2);
            PayrollServices.beginUnitOfWork();

            paycheckDate = new DateDTO("2012-04-20");

            k = 1;
            payrollRunDTO.setPayrollTXBatchId("Batch_2_Quarter_2");
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                paycheckDTO.setPaycheckId(SpcfUniqueId.generateRandomUniqueId().toString());
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
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

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[] {"-year:2012"});

          //get the list of all totals calculated by the annual  process
          DomainEntitySet<EmployeeW2Totals> totals =  Application.find(EmployeeW2Totals.class);
          System.out.println("Total number of Annual totals calculated for given data set up  are " + totals.size());
              // based on the data set up above i.e. total 10 employees and number of calculation for each, we can expect the total number of calculation entries
              assertTrue(totals.size()==110);

          //Iterate through each employee for specific values
          for (Company company : companies) {
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            for(Employee employee : employees ){
              //get all the annual calculation records fr this employee
              DomainEntitySet<EmployeeW2Totals> totalsForEE =  getEmployeeW2TotalsForEE(employee);
              //based on data setup, we can expect the total number of calculations for this employee to be 11
               assertTrue(totalsForEE.size()==11);

              for(EmployeeW2Totals total: totalsForEE){
                //TO do
                  //when the LawID FK will be set on W2 total table, verify the amounts for each Law ID
                  //Currently since it is not being set, there is no way to verify this

                  //Since the data setup has no deductions, taxable wages should be same as total wages
                  assertTrue(total.getTaxableWages().equals(total.getTotalWages()));

                  //employee Id for each record
                  System.out.println("Total records fot this employee are  " + totalsForEE.size());
                 }
              }
              }
         }

    /*
        Negative tests: set all wage values to zero
     */
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testAnnualTotals_zeroValues() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", String.valueOf(0.0 * i));
            lawAmounts.put("62", String.valueOf(0.0 * i));
            lawAmounts.put("63", String.valueOf(0.0 * i));
            lawAmounts.put("64", String.valueOf(0.0 *i));
            lawAmounts.put("1", String.valueOf(0.0 *i));
            lawAmounts.put("134", String.valueOf(0.0 *i)); // WY SUI-ER
            lawAmounts.put("131", String.valueOf(0.0 *i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(0.0*i)); // VT SUI-ER
            lawAmounts.put("120", String.valueOf(0.0 *i));   // OR SUI-ER
            lawAmounts.put("116", String.valueOf(0.0 *i)); // NV SUI-ER
            lawAmounts.put("87", String.valueOf(0.0 *i));   // CA SUI-ER

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
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyVac = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);

            paycheckDate = new DateDTO("2012-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }

            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            DataLoadServices.setPSPDate(2012, 4, 2);
            PayrollServices.beginUnitOfWork();

            paycheckDate = new DateDTO("2012-04-20");

            k = 1;
            payrollRunDTO.setPayrollTXBatchId("Batch_2_Quarter_2");
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                paycheckDTO.setPaycheckId(SpcfUniqueId.generateRandomUniqueId().toString());
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
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

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[] {"-year:2012"});

          //get the list of all totals calculated by the annual  process
          DomainEntitySet<EmployeeW2Totals> totals =  Application.find(EmployeeW2Totals.class);
          System.out.println("Total number of Annual totals calculated for given data set up  are " + totals.size());
              // based on the data set up above i.e. total 10 employees and number of calculation for each, we can expect the total number of calculation entries
              assertTrue(totals.size()==110);

          //Iterate through each employee for specific values
          for (Company company : companies) {
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            for(Employee employee : employees ){
              //get all the annual calculation records fr this employee
              DomainEntitySet<EmployeeW2Totals> totalsForEE =  getEmployeeW2TotalsForEE(employee);
              //based on data setup, we can expect the total number of calculations for this employee to be 11
               assertTrue(totalsForEE.size()==11);

              for(EmployeeW2Totals total: totalsForEE){
                //TO do
                  //when the LawID FK will be set on W2 total table, verify the amounts for each Law ID
                  //Currently since it is not being set, there is no way to verify this

                  //Since the data setup has no deductions, taxable wages should be same as total wages
                  assertTrue(total.getTaxableWages().equals(total.getTotalWages()));

                  //employee Id for each record
                  System.out.println("Total records fot this employee are  " + totalsForEE.size());
                 }
              }
              }
         }


    @Ignore
    @Test
    public void testAnnualTotals_nullValues() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            lawAmounts.put("61", "");
            lawAmounts.put("62", "");
            lawAmounts.put("63", "");
            lawAmounts.put("64","");
            lawAmounts.put("1", "");
            lawAmounts.put("134", ""); // WY SUI-ER
            lawAmounts.put("131", ""); // WA SUI-ER
            lawAmounts.put("130", ""); // VT SUI-ER
            lawAmounts.put("120", "");   // OR SUI-ER
            lawAmounts.put("116", ""); // NV SUI-ER
            lawAmounts.put("87", "");   // CA SUI-ER

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
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyVac = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);

            paycheckDate = new DateDTO("2012-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }

            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            DataLoadServices.setPSPDate(2012, 4, 2);
            PayrollServices.beginUnitOfWork();

            paycheckDate = new DateDTO("2012-04-20");

            k = 1;
            payrollRunDTO.setPayrollTXBatchId("Batch_2_Quarter_2");
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                paycheckDTO.setPaycheckId(SpcfUniqueId.generateRandomUniqueId().toString());
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
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

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[] {"-year:2012"});

          //get the list of all totals calculated by the annual  process
          DomainEntitySet<EmployeeW2Totals> totals =  Application.find(EmployeeW2Totals.class);
          System.out.println("Total number of Annual totals calculated for given data set up  are " + totals.size());
              // based on the data set up above i.e. total 10 employees and number of calculation for each, we can expect the total number of calculation entries
              assertTrue(totals.size()==110);

          //Iterate through each employee for specific values
          for (Company company : companies) {
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            for(Employee employee : employees ){
              //get all the annual calculation records fr this employee
              DomainEntitySet<EmployeeW2Totals> totalsForEE =  getEmployeeW2TotalsForEE(employee);
              //based on data setup, we can expect the total number of calculations for this employee to be 11
               assertTrue(totalsForEE.size()==11);

              for(EmployeeW2Totals total: totalsForEE){
                //TO do
                  //when the LawID FK will be set on W2 total table, verify the amounts for each Law ID
                  //Currently since it is not being set, there is no way to verify this

                  //Since the data setup has no deductions, taxable wages should be same as total wages
                  assertTrue(total.getTaxableWages().equals(total.getTotalWages()));

                  //employee Id for each record
                  System.out.println("Total records fot this employee are  " + totalsForEE.size());
                 }
              }
              }
         }
    
    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testAnnualTotals_No_Lawamounts() throws Exception {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));
        String[] statesList = new String[]{"OR", "CA", "NV", "WA", "WY", "VT"};
        String[] stateLawIds = new String[]{"87", "116", "120", "130", "131", "134"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.SUI, PaymentMethod.CheckPayment);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");
        SpcfCalendar firstPaycheckPeriodBeginDate = SpcfCalendar.createInstance(2011, 12, 1, SpcfTimeZone.getLocalTimeZone());

        HashMap<String, String> lawAmounts = new HashMap();

        for (String state : statesList) {
            PaymentTemplate paymentTemplate = DataLoadServices.getStatePaymentTemplate(state,PaymentTemplateCategory.SUI);
            DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplate.getPaymentTemplateCd(), supportedDate);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        double i = 1.0 ;
        int companyPayrollItemSourceId = 1;

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 3);
            lawAmounts.clear();
            /*
            lawAmounts.put("61", String.valueOf(0.0 * i));
            lawAmounts.put("62", String.valueOf(0.0 * i));
            lawAmounts.put("63", String.valueOf(0.0 * i));
            lawAmounts.put("64", String.valueOf(0.0 *i));
            lawAmounts.put("1", String.valueOf(0.0 *i));
            lawAmounts.put("134", String.valueOf(0.0 *i)); // WY SUI-ER
            lawAmounts.put("131", String.valueOf(0.0 *i)); // WA SUI-ER
            lawAmounts.put("130", String.valueOf(0.0*i)); // VT SUI-ER
            lawAmounts.put("120", String.valueOf(0.0 *i));   // OR SUI-ER
            lawAmounts.put("116", String.valueOf(0.0 *i)); // NV SUI-ER
            lawAmounts.put("87", String.valueOf(0.0 *i));   // CA SUI-ER
            */
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
            companyPayrollItemDTOs.add(companyPayrollItemDTO);
            companyPayrollItemDTO.setSourcePayrollItemId(String.valueOf(companyPayrollItemSourceId));

            DataLoadServices.persistPayrollItems(company.getSourceSystemCd(), company.getSourceCompanyId(), companyPayrollItemDTOs);
            String hourlyVac = String.valueOf(companyPayrollItemSourceId);

            PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, paycheckDate, Arrays.asList(employees.toArray(new Employee[]{})), lawAmounts);

            paycheckDate = new DateDTO("2012-01-20");

            int k = 1;
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
                    paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
                }
                k++;
            }

            ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), payrollRunDTO);
            assertSuccess(processResult);
            PayrollServices.commitUnitOfWork();
            DataLoadServices.setPSPDate(2012, 4, 2);
            PayrollServices.beginUnitOfWork();

            paycheckDate = new DateDTO("2012-04-20");

            k = 1;
            payrollRunDTO.setPayrollTXBatchId("Batch_2_Quarter_2");
            for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
                paycheckDTO.setPayPeriodBeginDate(new DateDTO(firstPaycheckPeriodBeginDate));
                paycheckDTO.setPaycheckId(SpcfUniqueId.generateRandomUniqueId().toString());
                firstPaycheckPeriodBeginDate.addDays(15);
                paycheckDTO.setPayPeriodEndDate(new DateDTO(firstPaycheckPeriodBeginDate));

                for (String stateLawId : stateLawIds) {
                    companyLaw = CompanyLaw.findCompanyLaw(company, stateLawId);
                    CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
                    compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance((Integer.parseInt(stateLawId) + k)/3));
                    if((stateLawId.equals("131") || stateLawId.equals("134")) && (k % 2 == 0)) {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyVac));
                    } else {
                        compensationTransactionDTO.setSourcePayrollItemId(String.valueOf(hourlyReg));
                    }
                    QBDTPaylineInfoDTO qbdtPaylineInfoDTO = new QBDTPaylineInfoDTO();
                    qbdtPaylineInfoDTO.setRate((Integer.parseInt(stateLawId)*k)/2);
                    compensationTransactionDTO.setQBDTPaylineInfoDTO(qbdtPaylineInfoDTO);
                    compensationTransactionDTO.setCompensationAmount(new SpcfMoney(String.valueOf(i)));
                    compensationTransactionDTO.setPayStubOrder(Long.valueOf(k));
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

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1));

        //Run EE Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Move PSP date to second quarter, so that extract with UpdatedData will extract previous quarter data
        DataLoadServices.setPSPDate(2012, 4, 2);

        new CalculateEmployeeAnnualTotals().main(new String[] {"-year:2012"});

          //get the list of all totals calculated by the annual  process
          DomainEntitySet<EmployeeW2Totals> totals =  Application.find(EmployeeW2Totals.class);
          System.out.println("Total number of Annual totals calculated for given data set up  are " + totals.size());
              // based on the data set up above i.e. total 10 employees and number of calculation for each, we can expect the total number of calculation entries
              assertTrue(totals.size()==0);

          //Iterate through each employee for specific values
          for (Company company : companies) {
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);
            for(Employee employee : employees ){
              //get all the annual calculation records fr this employee
              DomainEntitySet<EmployeeW2Totals> totalsForEE =  getEmployeeW2TotalsForEE(employee);
              //based on data setup, we can expect the total number of calculations for this employee to be 11
               assertTrue(totalsForEE.size()==0);

              for(EmployeeW2Totals total: totalsForEE){
                //TO do
                  //when the LawID FK will be set on W2 total table, verify the amounts for each Law ID
                  //Currently since it is not being set, there is no way to verify this

                  //Since the data setup has no deductions, taxable wages should be same as total wages
                  assertTrue(total.getTaxableWages().equals(total.getTotalWages()));

                  //employee Id for each record
                  System.out.println("Total records fot this employee are  " + totalsForEE.size());
                 }
              }
              }
         }

      private  DomainEntitySet<EmployeeW2Totals> getEmployeeW2TotalsForEE(Employee pEmployee) {
        Criterion<EmployeeW2Totals> where = EmployeeW2Totals.Employee().equalTo(pEmployee);


        DomainEntitySet<EmployeeW2Totals> eeTotals = Application.find(EmployeeW2Totals.class, where);

        if (eeTotals != null && eeTotals.size() > 0) {
            return eeTotals;
        } else {
            return new DomainEntitySet<EmployeeW2Totals>();
        }
      }

      private EmployeeW2Totals getEmployeeW2Totals(Employee pEmployee, Law pLaw, CompanyPayrollItem pCompanyPayrollItem, int pYear) {
        Criterion<EmployeeW2Totals> where = EmployeeW2Totals.Employee().equalTo(pEmployee)
                                                            .And(EmployeeW2Totals.Year().equalTo(pYear));
        if (pLaw != null) {
            where = where.And(EmployeeW2Totals.Law().equalTo(pLaw));
        }
        if (pCompanyPayrollItem != null) {
            where = where.And(EmployeeW2Totals.CompanyPayrollItem().equalTo(pCompanyPayrollItem));
        }

        DomainEntitySet<EmployeeW2Totals> eeTotals = Application.find(EmployeeW2Totals.class, where);

        if (eeTotals != null && eeTotals.size() > 0) {
            return eeTotals.get(0);
        } else {
            return new EmployeeW2Totals();
        }
    }
}
