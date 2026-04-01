package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.FsetFilingProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 17, 2011
 * Time: 11:14:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class EdiPaymentTests {
	
	private int achTaxOffloadOffset;

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        EftpsDataLoader.deleteAllFsetTestDirFiles();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        SystemParameter achTaxOffloadOffsetParam = SystemParameter.findSystemParameter(SystemParameter.Code.ACH_TAX_PAYMENT_FILE_SETTLEMENT_DATE_OFFSET);
        achTaxOffloadOffsetParam = Application.refresh(achTaxOffloadOffsetParam);
        achTaxOffloadOffset = Integer.valueOf(achTaxOffloadOffsetParam.getSystemParameterValue());
        PayrollServices.commitUnitOfWork();
        DataLoadServices.reinitialize();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    public Company setupCompanyForMS(String pPsid) {

        SpcfCalendar supportStartDate = SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(supportStartDate);
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPsid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "65", "143", "1", "27");
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        //Update Agency Id
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", true);

        //Enable EDI payment method
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.paymentManager.updatePaymentAgentEnabledCore(company.getSourceSystemCd(), company.getSourceCompanyId(), "MS-M89-PAYMENT", PaymentMethod.ACHCredit, true));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(pPsid, "IRS-940-PAYMENT");
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(pPsid, "IRS-941-PAYMENT");

        DataLoadServices.updateIRSPaymentTemplateSupportDate(PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("MS-M89-PAYMENT", PSPDate.getPSPTime());

        return company;
    }

    public void createPayrollWithMSStatePayments(Company pCompany, SpcfCalendar pPayrollRunDate, DateDTO pPaycheckDate) {

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pPayrollRunDate);
        PayrollServices.commitUnitOfWork();

        HashMap<String, String> lawAmounts = new HashMap();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("143", "14.3");
        lawAmounts.put("1", "10");
        lawAmounts.put("27", "2.7");

        DomainEntitySet<Employee> employees = Employee.findEmployees(pCompany);
        List<Employee> emps = new ArrayList<Employee>(employees);

        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(pCompany, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRun(payrollRunDTO, pCompany, pPaycheckDate, emps, lawAmounts);

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(pCompany, payrollRunDTO.getPayrollTXBatchId());
        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPaymentMethodSwitches_FsetFiling() throws Exception {
        Company company = setupCompanyForMS("1234567");
        createPayrollWithMSStatePayments(company, SpcfCalendar.createInstance(2011, 10, 13, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-10-14"));

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        //Updating the legal name, addressLine1 and addressLine2  with invalid pattern.
        company.setLegalName("  Testing for {}. invalid's #company      Legal   name * , Schema & (has)  weired prn validation.  ");
        company.getLegalAddress().setAddressLine1(" -/-Invalid's -  #St.,   /     Too \\ big     st Name,\\ Invalid  dr - A line 1 ");
        company.getLegalAddress().setAddressLine2(" /-/-Invalid's - second.     St, / Too   big   street  second Name, - A line 2 ");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("MS-M89-PAYMENT");
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("MS State WH payments", 1, moneyMovementTransactions.size());
        assertEquals("MS state WH payment method", PaymentMethod.ACHCredit, moneyMovementTransactions.get(0).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        //Disable EDI payment method
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", false);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        assertEquals("MS State WH payments", 1, moneyMovementTransactions.size());
        assertEquals("MS state WH payment method", PaymentMethod.CheckPayment, assertOne(moneyMovementTransactions).getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        //Enable EDI payment method
        DataLoadServices.updateRequiredIDs(company, "MS-M89-PAYMENT", true);

        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = MoneyMovementTransaction.findTaxPayments().setPaymentTemplate(paymentTemplate).find();
        MoneyMovementTransaction moneyMovementTransaction = assertOne(moneyMovementTransactions);
        assertEquals("MS state WH payment method", PaymentMethod.ACHCredit, moneyMovementTransaction.getMoneyMovementPaymentMethod());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 11, 10);

        DataLoadServices.offloadAgencyTaxCredits(paymentTemplate);

        FsetFilingProcessor fsetFilingProcessor = new FsetFilingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.FsetFilingProcessor, "Test-Id", null);
        fsetFilingProcessor.setInitiationDate(SpcfCalendar.createInstance(2011, 11, 10, SpcfTimeZone.getLocalTimeZone()));
        fsetFilingProcessor.executeJobStep("GenerateFsetReturnFileStep");

        PayrollServices.beginUnitOfWork();
        FsetFile fsetFile = assertOne(Application.find(FsetFile.class));
        FsetFilingDetail fsetFilingDetail = assertOne(Application.find(FsetFilingDetail.class));
        assertNotNull(fsetFilingDetail.getCompany());
        assertEquals(fsetFilingDetail.getMoneyMovementTransaction().getCompany(),fsetFilingDetail.getCompany());
        assertEquals("Fset file", fsetFile, fsetFilingDetail.getParentFile());
        assertEquals("Fset MMT", moneyMovementTransaction, fsetFilingDetail.getMoneyMovementTransaction());
        assertEquals("MMT Amount", moneyMovementTransaction.getMoneyMovementTransactionAmount(), fsetFilingDetail.getFilingAmount());
        assertEquals("MMT Due date", moneyMovementTransaction.getDueDate(), fsetFilingDetail.getFilingDueDate());
        assertEquals("MMT Period End date", moneyMovementTransaction.getPaymentPeriodEnd(), fsetFilingDetail.getPeriodEndDate());
        assertEquals("Business Name after removing special chars", "Testing for invalid's #company Legal name Schema & (has) weired prn validat", fsetFilingDetail.getBusinessName());
        assertEquals("Address line1 after removing special chars", "Invalids - St / Too big st Name Inv", fsetFilingDetail.getAddressLine1());
        assertEquals("Address line2 after removing special chars", "Invalids - second St / Too big stre", fsetFilingDetail.getAddressLine2());
        PayrollServices.rollbackUnitOfWork();

    }
	
    @Test
    public void testFilingWithMultipleCompaniesAndZeroPayment() throws Exception {
        Company company1 = setupCompanyForMS("12356");
        createPayrollWithMSStatePayments(company1, SpcfCalendar.createInstance(2011, 10, 13, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-10-14"));

        Company company2 = setupCompanyForMS("32344354");
        createPayrollWithMSStatePayments(company2, SpcfCalendar.createInstance(2011, 10, 15, SpcfTimeZone.getLocalTimeZone()), new DateDTO("2011-10-15"));

        setupCompanyForMS("45454562");

        DataLoadServices.setPSPDate(2011, 10, 4);

        BatchJobManager.runJob(BatchJobType.AchZeroPayments);

        SpcfCalendar initDate = SpcfCalendar.createInstance(2011, 11, 15, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.addBusinessDays(initDate, -achTaxOffloadOffset);
        DataLoadServices.setPSPDate(initDate);

        PayrollServices.beginUnitOfWork();
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate("MS-M89-PAYMENT");
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.offloadAgencyTaxCredits(paymentTemplate);

        //Running again same step to make sure do not submit same payments again, submit one payment only
        FsetFilingProcessor fsetFilingProcessor = new FsetFilingProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.FsetFilingProcessor, "Test-Id-1", null);
        fsetFilingProcessor.setInitiationDate(initDate);
        fsetFilingProcessor.executeJobStep("GenerateFsetReturnFileStep");

        PayrollServices.beginUnitOfWork();
        FsetFile fsetFile = assertOne(Application.find(FsetFile.class));
        DomainEntitySet<FsetFilingDetail> fsetFilingDetails = Application.find(FsetFilingDetail.class);
        assertEquals("Fset Filing details", 3, fsetFilingDetails.size());
        assertNotNull(fsetFilingDetails.getFirst().getCompany());

        PayrollServices.rollbackUnitOfWork();

    }

}
