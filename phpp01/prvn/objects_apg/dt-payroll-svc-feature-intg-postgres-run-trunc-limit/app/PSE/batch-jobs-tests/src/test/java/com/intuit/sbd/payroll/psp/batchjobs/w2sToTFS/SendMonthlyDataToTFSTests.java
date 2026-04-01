package com.intuit.sbd.payroll.psp.batchjobs.w2sToTFS;

import com.intuit.ems.tfs.messages.v1.DepositFrequencyType;
import com.intuit.ems.tfs.messages.v1.FilingTypeType;
import com.intuit.ems.tfs.messages.v1.PayrollFormInfo;
import com.intuit.ems.tfs.messages.v1.SubmitFilingRequest;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * User: dhaddan
 * Date: May 22, 2013
 * Time: 4:34:09 PM
 */
public class SendMonthlyDataToTFSTests {


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

    @Test
    public void testMonthlyDataWithDepositFrequencies() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 1));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalILAndMEStateTaxCompanyLaws(company);

        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        String[] statesList = new String[]{"ME", "IL"};
        DataLoadServices.setupCompany(1L, 1, statesList, PaymentTemplateCategory.SUI);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IL-UI340-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 11,25));

        DataLoadServices.updateRequiredIDs(company, null, true);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndILStateTaxes(payrollRunDTO, company, new DateDTO("2010-11-02"), emps);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        DataLoadServices.setPSPDate(2011, 1, 27);

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2010, 11, 1);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2010, 11, 30);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
        SendMonthlyDataToTFS.setYear(2010);
        HashMap<SpcfUniqueId, List<Object[]>> companyMonthlyTotals = SendMonthlyDataToTFS.getEETotalsTaxAmounts(SendMonthlyDataToTFS.LAW_97, beginDate, endDate);
        final List<Object[]> companyTotals = companyMonthlyTotals.get(foundCompany.getId());

        SubmitFilingRequest sfr = SendMonthlyDataToTFS.createSubmitFilingRequest(foundCompany, companyTotals, beginDate, endDate);
        List<PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency> depFreq = sfr.getPayrollFormInfo().get(0).getCompanyInfo().getTaxItemInfo().get(0).getDepositFrequency();

        assertEquals("Number of deposit frequencies is 1", 1, depFreq.size());
        PayrollFormInfo.CompanyInfo.TaxItemInfo.DepositFrequency depositFrequency = depFreq.get(0);
        assertEquals("Deposit Frequency Date", SpcfCalendar.createInstance(2010, 7, 1).format("yyyy-MM-dd"), depositFrequency.getEffectiveDate());
        assertEquals("Deposit Frequency Type", DepositFrequencyType.QUARTERLY, depositFrequency.getDepositFrequency());

        FilingTypeType ft = sfr.getPayrollFormInfo().get(0).getDataSpace().getFilingType();
        assertEquals("Dataspace filing type", FilingTypeType.ModifiableDailyData, ft);
        assertEquals("Dataspace year", BigInteger.valueOf(2010), sfr.getPayrollFormInfo().get(0).getDataSpace().getYear());


        PayrollServices.commitUnitOfWork();
    }



    @Test
    public void testMonthlyDataWithZeroWagesCompanies() throws Exception {
        String psid = "123456789";

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 1));
        PayrollServices.commitUnitOfWork();

        //Step 1 : Create a company
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalILAndMEStateTaxCompanyLaws(company);

        //Step 2 : Create 2 + 2 employees for above company
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        List<Employee> otherEmps = DataLoadServices.addEEs(company, 2);

        //Step 3 : Create 2 companies for IL
        String[] statesList = new String[]{"ME", "IL"};
        DataLoadServices.setupCompany(1L, 2, statesList, PaymentTemplateCategory.SUI);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(psid, "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(psid, "IL-UI340-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 11,25));
        DataLoadServices.updateRequiredIDs(company, null, true);


        //Step 4 : Run the payroll for 1 company
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25));
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWith941AndILStateTaxes(payrollRunDTO, company, new DateDTO("2010-11-02"), emps);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);


        DataLoadServices.setPSPDate(2011, 1, 27);

        SpcfCalendar beginDate = SpcfCalendar.createInstance(2010, 11, 1);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2010, 11, 30);

        //Step 5 : Get a company for which we ran a payroll and assert the expected objects like EETotalsAmounts
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
        SendMonthlyDataToTFS.setYear(2010);
        HashMap<SpcfUniqueId, List<Object[]>> companyMonthlyTotals = SendMonthlyDataToTFS.getEETotalsTaxAmounts(SendMonthlyDataToTFS.LAW_97, beginDate, endDate);
        final List<Object[]> companyTotals = companyMonthlyTotals.get(foundCompany.getId());
        assertNotNull("CompanyMonthlyTotals is NULL", companyMonthlyTotals);
        assertTrue("Expected companyMonthlyTotals size is not matching", companyMonthlyTotals.size()==1); // we have payroll for only 1 company
        assertTrue("Expected companyTotals size is not matching", companyTotals .size()==2);// we ran payroll for 2 employees only so we will get 2 records only


        //Step 6 : Get the company seqs for IL and test the getTaxCompanySeqs method
        Set<SpcfUniqueId> taxCompanySeqs = SendMonthlyDataToTFS.getTaxCompanySeqs(SendMonthlyDataToTFS.LAW_97);
        assertNotNull("TaxCompanySeqs is NULL", taxCompanySeqs);
        assertTrue("Expected taxCompanySeqs size is not matching",taxCompanySeqs.size()==3); // we have 3 tax companies for IL


        //Step 7 : Get the above company with their companyTotals and create the SubmitFilingRequest and test it
        SubmitFilingRequest sfr = SendMonthlyDataToTFS.createSubmitFilingRequest(foundCompany, companyTotals, beginDate, endDate);
        assertNotNull("Company object is null", sfr.getPayrollFormInfo().get(0).getCompanyInfo());
        assertNotNull("Employee object is null", sfr.getPayrollFormInfo().get(0).getEmployeeInfo());// we do have 2 emps for this company
        assertTrue("Expected getPayrollFormInfo size is not matching", sfr.getPayrollFormInfo().get(0).getEmployeeInfo().size()==2);// we ran payroll for 2 employees only so we will get 2 records only


        //Step 8 : Get the zero wage company with their companyTotals (empty) and create the SubmitFilingRequest and test it
        Company zeroWageComp = Company.findCompany("1", SourceSystemCode.QBDT);
        sfr = SendMonthlyDataToTFS.createSubmitFilingRequest(zeroWageComp, new ArrayList<Object[]>(), beginDate, endDate);
        assertNotNull("Company object is null", sfr.getPayrollFormInfo().get(0).getCompanyInfo());
        assertTrue("Expected getPayrollFormInfo size is not matching", sfr.getPayrollFormInfo().get(0).getEmployeeInfo().size() == 0);// we did not have any employees so we will not get any records
        PayrollServices.commitUnitOfWork();
    }



    @Test
    public void doesNotAttemptSendForNonAssistedCustomers() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-501-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-UI340-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 15);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.CloudV2, ServiceCode.ViewMyPaycheck);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "IL");

        DataLoadServices.addEEs(company, 2);

        DataLoadServices.setPSPDate(2011, 10, 20);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2011-11-02"));

        DataLoadServices.setPSPDate(2011, 12, 10);
        SendMonthlyDataToTFS.process(new String[]{}); //will throw an error if reading non-Tax
    }

    @Ignore
    @Test
    public void testZerowagesFilingForInactiveCompanies(){

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 1, 15));
        PayrollServices.commitUnitOfWork();

        //Create 4 companies
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax, ServiceCode.Cloud);
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax, ServiceCode.Cloud);
        Company inactiveCompany1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax, ServiceCode.Cloud);
        Company inactiveCompany2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax, ServiceCode.Cloud);

        //Activate required services on the companies and payrollruns
        DataLoadServices.addCompanyPIN(company1, null);
        DataLoadServices.addCompanyBankAccount(company1);
        DataLoadServices.addCompanyLaws(company1, "61", "62", "63", "64", "65", "1", "97", "16");

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company1.getSourceCompanyId(), "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(company1.getSourceCompanyId(), "IL-UI340-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 11,25));
        DataLoadServices.updateRequiredIDs(company1, null, true);

        List<Employee> employees = DataLoadServices.addEEs(company1, 2);

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company1, payrollRunDTO);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 1, 20));
        PayrollRunDTO payrollDTO = createILStatePayrollRun(payrollRunDTO, company1, new DateDTO("2016-01-22"), employees);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company1.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //Company 2
        DataLoadServices.addCompanyPIN(company2, null);
        DataLoadServices.addCompanyBankAccount(company2);
        DataLoadServices.addCompanyLaws(company2, "61", "62", "63", "64", "65", "1", "97", "16");

        //InactiveCompany 1
        DataLoadServices.addCompanyPIN(inactiveCompany1, null);
        DataLoadServices.addCompanyBankAccount(inactiveCompany1);
        DataLoadServices.addCompanyLaws(inactiveCompany1, "61", "62", "63", "64", "65", "1", "97", "16");

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(inactiveCompany1.getSourceCompanyId(), "IRS-941-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDate(inactiveCompany1.getSourceCompanyId(), "IL-UI340-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 11,25));
        DataLoadServices.updateRequiredIDs(inactiveCompany1, null, true);


        List<Employee> inactiveEmployees = DataLoadServices.addEEs(inactiveCompany1, 2);

        PayrollRunDTO payrollRunDTO1 = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(inactiveCompany1, payrollRunDTO1);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 1, 20));
        PayrollRunDTO payrollDTO1 = createILStatePayrollRun(payrollRunDTO1, inactiveCompany1, new DateDTO("2016-01-22"), inactiveEmployees);
        ProcessResult<PayrollRun> processResult1 = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, inactiveCompany1.getSourceCompanyId(), payrollDTO1);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        //InactiveCompany 2
        DataLoadServices.addCompanyPIN(inactiveCompany2, null);
        DataLoadServices.addCompanyBankAccount(inactiveCompany2);
        DataLoadServices.addCompanyLaws(inactiveCompany2, "61", "62", "63", "64", "65", "1", "97", "16");

        PayrollServices.beginUnitOfWork();
        DataLoadServices.deactivateFilingStatusOfCompanyLaw(inactiveCompany1, SendMonthlyDataToTFS.LAW_97);
        DataLoadServices.deactivateFilingStatusOfCompanyLaw(inactiveCompany2, SendMonthlyDataToTFS.LAW_97);
        PayrollServices.commitUnitOfWork();

        //The 2 companies with active filingStatus should get picked up
        Set<SpcfUniqueId> taxCompanySeqs = SendMonthlyDataToTFS.getTaxCompanySeqs(SendMonthlyDataToTFS.LAW_97);
        assertNotNull("TaxCompanySeqs is NULL", taxCompanySeqs);
        assertTrue("Expected taxCompanySeqs size is not matching : "+taxCompanySeqs.size(),taxCompanySeqs.size()==2);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 02, 12));
        PayrollServices.commitUnitOfWork();

        //3 companies should get picked up by the job, 2 wage companies and 1 zero wage company. Verified from the logs
        BatchJobManager.runJob(BatchJobType.SendMonthlyDataToTFSProcessor);
    }

    @Test
    public void testForDGDeletedCompanies_NoArguments(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-501-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-UI340-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 15);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.CloudV2, ServiceCode.ViewMyPaycheck);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "IL");

        DataLoadServices.addEEs(company, 2);

        DataLoadServices.setPSPDate(2011, 10, 20);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2011-11-02"));

        DataLoadServices.setPSPDate(2011, 12, 10);

        // Company passed through SystemParameter
        Application.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);

        Application.commitUnitOfWork();

        SendMonthlyDataToTFS.process(new String[]{});


    }

    @Test(expected = RuntimeException.class)
    public void testForDGDeletedCompanies(){
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-501-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("IL-UI340-PAYMENT", SpcfCalendar.createInstance(1999, 1, 1));

        DataLoadServices.setPSPDate(2011, 10, 15);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.CloudV2, ServiceCode.ViewMyPaycheck);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "IL");

        DataLoadServices.addEEs(company, 2);

        DataLoadServices.setPSPDate(2011, 10, 20);
        DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO("2011-11-02"));

        DataLoadServices.setPSPDate(2011, 12, 10);

        // Company passed through SystemParameter
        Application.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);

        SystemParameter companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.TFS_MONTHLY_TRANSFER_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue(company.getSourceCompanyId());
        Application.save(companyListParameter);

        Application.commitUnitOfWork();

        SendMonthlyDataToTFS.process(new String[]{});

        // Company passed as an arguments

        Application.beginUnitOfWork();
        company = Application.findById(Company.class, company.getId());
        company.setIsDgDisassociated(Boolean.TRUE);
        Application.save(company);

        companyListParameter = SystemParameter.findSystemParameter(SystemParameter.Code.TFS_MONTHLY_TRANSFER_COMPANY_LIST);
        companyListParameter = Application.refresh(companyListParameter);

        companyListParameter.setSystemParameterValue(null);
        Application.save(companyListParameter);

        Application.commitUnitOfWork();

        SendMonthlyDataToTFS.process(new String[]{"-companyId:"+company.getSourceCompanyId()});


    }

    public PayrollRunDTO createILStatePayrollRun(PayrollRunDTO pPayrollRunDTO, Company pCompany, DateDTO payrollDate, List<Employee> pEmployees){
        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("97", "27");
        lawAmounts.put("16", "28");
        lawAmounts.put("62", "12");
        lawAmounts.put("63", "5.5");
        lawAmounts.put("64", "45");
        lawAmounts.put("1", "25");
        return DataLoadServices.createPayrollRun(pPayrollRunDTO, pCompany, payrollDate, pEmployees, lawAmounts);

    }
}
