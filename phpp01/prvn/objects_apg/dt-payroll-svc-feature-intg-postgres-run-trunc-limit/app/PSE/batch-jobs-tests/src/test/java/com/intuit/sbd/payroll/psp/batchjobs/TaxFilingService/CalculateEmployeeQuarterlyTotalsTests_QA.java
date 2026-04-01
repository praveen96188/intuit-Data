package com.intuit.sbd.payroll.psp.batchjobs.TaxFilingService;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: vbindage
 * Date: 8/8/12
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class CalculateEmployeeQuarterlyTotalsTests_QA {


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

    /**
     * Verify that if there were no new payroll runs the employee calculation token does not get updated
     */
    @Test
    public void testTokenUpdate_NoChange() {

        //save old token
        long oldToken = getCurrentToken();

        //run the calculation process and Batch Job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //verify that the employee calculation token is unchanged
         assertTrue(getCurrentToken()==oldToken);
    }

    /**
     * Verify that if there were new payroll runs to be processed then the batch job updates the token after processing
     */
    @Ignore
    @Test
    public void testTokenUpdate() {

        //set up system date
        DataLoadServices.setPSPDate(2012, 1, 1);
        //create companies
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        //create date objects
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2012-01-07");
        //run payroll
        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class);
        Paycheck paycheck = paychecks.get(0);
        paycheck.setPayPeriodBeginDate(SpcfCalendar.createInstance(2012, 1, 8, SpcfTimeZone.getLocalTimeZone()));
        paycheck.setPayPeriodEndDate(SpcfCalendar.createInstance(2012, 1, 14, SpcfTimeZone.getLocalTimeZone()));
        paycheck = paychecks.get(1);
        paycheck.setPayPeriodBeginDate(SpcfCalendar.createInstance(2012, 2, 8, SpcfTimeZone.getLocalTimeZone()));
        paycheck.setPayPeriodEndDate(SpcfCalendar.createInstance(2012, 2, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        //save old token
        long oldToken = getCurrentToken();

        //run the calculation process and Batch Job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //verify that the employee calculation token is unchanged
         assertFalse(getCurrentToken()==oldToken);
    }

    /**
     * Add a new payrollRun for a new employee i.e first payrollRun for an employee
     * The expected tax totals before the batch job run should be zero
     * Now when the batch job is run and total for employee is queried, it should be same as the amount from payrollRun
     */
    @Test
    public void testTaxTotalForEmployee() {

        //set up todays date
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        //create and set up company
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        HashMap<String, String> lawAmounts = new HashMap();
            lawAmounts.put("61", "6.1");
            lawAmounts.put("62", "6.2");
            lawAmounts.put("63", "6.3");
            lawAmounts.put("64", "6.4");
            lawAmounts.put("1", "25");
            lawAmounts.put("65", "6.5");


        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));


        //Invoke the Employee calculation process
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);



         for (Company company : companies) {
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);

            for(Employee employee : employees ){

              DomainEntitySet<EmployeeLawQtrTotals> totals =  getEELawQuarterlyTotals(company, employee, 1, 2011);
              assertTrue(totals.size()==8);
              for(EmployeeLawQtrTotals total: totals){
                 assertTrue(total.getTotalWages().equals(total.getTaxableWages()));
                 if(total.getLaw().getLawId().equalsIgnoreCase("63")){
                     assertTrue(total.getTaxAmount().toString().equalsIgnoreCase("6.30"));
                 }

              }


              }
         }
    }


    /**
     * If the offload runs in earlier month but payroll if for the beginning of quarter
     */
    @Test
    public void testTaxTotalForEmployee_twoQuarters() {

        //set up todays date
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1));
        //create and set up company
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate1 = new DateDTO("2011-04-02");
       // DateDTO payrollDate2 = new DateDTO("2011-04-7");

        HashMap<String, String> lawAmounts = new HashMap();
            lawAmounts.put("61", "6.1");
            lawAmounts.put("62", "6.2");
            lawAmounts.put("63", "6.3");
            lawAmounts.put("64", "6.4");
            lawAmounts.put("1", "25");
            lawAmounts.put("65", "6.5");


        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate1, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()));

        //Invoke the Employee calculation process
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);



         for (Company company : companies) {
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);

            for(Employee employee : employees ){

              DomainEntitySet<EmployeeLawQtrTotals> totals =  getEELawQuarterlyTotals(company, employee, 2, 2011);
              assertTrue(totals.size()==8);
              for(EmployeeLawQtrTotals total: totals){
                 assertTrue(total.getTotalWages().equals(total.getTaxableWages()));
                 if(total.getLaw().getLawId().equals("63")){
                     assertTrue(total.getTaxAmount().toString().equals("6.30"));
                 }

              }


              }
         }
    }

     /**
     * If the offload runs in earlier month but payroll if for the beginning of quarter
     */
    @Test
    public void testTaxTotalForEmployee_twoPayrolls() {

        //set up todays date
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 4, 1));
        //create and set up company
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate1 = new DateDTO("2011-04-02");
        DateDTO payrollDate2 = new DateDTO("2011-04-09");

        HashMap<String, String> lawAmounts = new HashMap();
            lawAmounts.put("61", "6.1");
            lawAmounts.put("62", "6.2");
            lawAmounts.put("63", "6.3");
            lawAmounts.put("64", "6.4");
            lawAmounts.put("1", "25");
            lawAmounts.put("65", "6.5");


        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate1, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 3, 31, SpcfTimeZone.getLocalTimeZone()));

         for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate2, false, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 4, 7, SpcfTimeZone.getLocalTimeZone()));

        //Invoke the Employee calculation process
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);



         for (Company company : companies) {
            DomainEntitySet<Employee> employees = Employee.findEmployees(company);

            for(Employee employee : employees ){

              DomainEntitySet<EmployeeLawQtrTotals> totals =  getEELawQuarterlyTotals(company, employee, 2, 2011);
              assertTrue(totals.size()==8);
              for(EmployeeLawQtrTotals total: totals){
                 assertTrue(total.getTotalWages().equals(total.getTaxableWages()));
                 if(total.getLaw().getLawId().equalsIgnoreCase("63")){
                     assertTrue(total.getTaxAmount().toString().equalsIgnoreCase("12.60"));

                 }

              }


              }
         }
    }

    @Test
    public void testHappyPath() {

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 1, 1));
        String[] statesList = new String[]{"AR"};
        List<Company> companies = DataLoadServices.setupCompany(158905L, 1, statesList, PaymentTemplateCategory.SUI, PaymentMethod.ACHDebit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO payrollDate = new DateDTO("2011-01-07");

        for (Company company : companies) {
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, payrollDate, true, new HashMap<String, String>(), PaymentTemplateCategory.SUI);
        }

        DataLoadServices.runOffload(SpcfCalendar.createInstance(2011, 1, 5, SpcfTimeZone.getLocalTimeZone()));

        //Run totals calculation process
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        // Assert for calcualted data - PSP_EMPLOYEE_LAW_QTR_TOTALS

    }

    private long getCurrentToken(){
       return SystemParameter.findIntValue(SystemParameter.Code.EMPLOYEE_CALCULATION_TOKEN);
    }

    private DomainEntitySet<EmployeeLawQtrTotals> getEELawQuarterlyTotals(Company pCompany, Employee pEmployee, int pQuarter, int pYear) {
        Criterion<EmployeeLawQtrTotals> where = EmployeeLawQtrTotals.Company().equalTo(pCompany)
                                                                    .And(EmployeeLawQtrTotals.Employee().equalTo(pEmployee)
                                                                            .And(EmployeeLawQtrTotals.Quarter().equalTo(pQuarter))
                                                                            .And(EmployeeLawQtrTotals.Year().equalTo(pYear)));


        DomainEntitySet<EmployeeLawQtrTotals> eeTotals = Application.find(EmployeeLawQtrTotals.class, where);


        if (eeTotals != null && eeTotals.size() > 0) {
            return eeTotals;
        } else {
            return new DomainEntitySet<EmployeeLawQtrTotals>();
        }

    }
}
