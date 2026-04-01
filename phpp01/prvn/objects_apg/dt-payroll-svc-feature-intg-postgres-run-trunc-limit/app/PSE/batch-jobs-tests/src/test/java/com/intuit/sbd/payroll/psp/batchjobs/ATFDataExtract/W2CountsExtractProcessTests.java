package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.employeeTotals.CalculateEmployeeAnnualTotals;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.HashMap;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * User: ihannur
 * Date: 11/28/12
 * Time: 4:50 PM
 */
public class W2CountsExtractProcessTests {

    static boolean extract = false;

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        extract = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_W2_COUNT_INFO_EXTRACT);
        PayrollServices.commitUnitOfWork();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_W2_COUNT_INFO_EXTRACT, "true");
        }
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extract) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_W2_COUNT_INFO_EXTRACT, "false");
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2012, 1, 1);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    // resolved 08/05/2021
    @Test
    public void testHappyPath() throws Exception {
        String[] statesList = new String[]{"OR", "CA", "VT"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 2);  // 2 added in DLS.setupCompany, adding 2 EEs here. Making it to 4
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, paycheckDate, false, lawAmounts, PaymentTemplateCategory.Withholding);
        }

        DataLoadServices.setPSPDate(2012, 1, 1);

        statesList = new String[]{"AR"};
        DataLoadServices.setupCompany(158907L, 1, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.CheckPayment);
        paycheckDate = new DateDTO("2012-01-10");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("158907", SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, paycheckDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(2012, 1, 6);
        DataLoadServices.runOffload();

        //Run EE Quarterly Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();

        //Run EE Quarterly Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Run EE Annual Totals calculation batch job
        new CalculateEmployeeAnnualTotals().main(new String[]{"-year:2012"});

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForAnnualData(2012, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.W2CountInfo, "atfextract/expected/test_W2Counts_File"));
    }


    @Test
    public void testForDGDeletedCompany() throws Exception {
        String[] statesList = new String[]{"OR", "CA", "VT"};
        DataLoadServices.setupCompany(158905L, 2, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);
        SpcfCalendar supportedDate = SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone());
        DateDTO paycheckDate = new DateDTO("2012-01-07");

        HashMap<String, String> lawAmounts = new HashMap<String, String>();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).sort(Company.SourceCompanyId());
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            DataLoadServices.addEEs(company, 2);  // 2 added in DLS.setupCompany, adding 2 EEs here. Making it to 4
            DataLoadServices.runPayrollRun(company, statesList, supportedDate, paycheckDate, false, lawAmounts, PaymentTemplateCategory.Withholding);
        }

        DataLoadServices.setPSPDate(2012, 1, 1);

        statesList = new String[]{"AR"};
        DataLoadServices.setupCompany(158907L, 1, statesList, PaymentTemplateCategory.Withholding, PaymentMethod.CheckPayment);
        paycheckDate = new DateDTO("2012-01-10");

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("158907", SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runPayrollRun(company, statesList, supportedDate, paycheckDate, false, new HashMap<String, String>(), PaymentTemplateCategory.Withholding);

        DataLoadServices.setPSPDate(2012, 1, 6);

        DataLoadServices.runOffload();

        //Run EE Quarterly Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findFirstCompanyPayrollRun(company);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();

        //Run EE Quarterly Totals calculation batch job
        BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess);

        //Run EE Annual Totals calculation batch job
        new CalculateEmployeeAnnualTotals().main(new String[]{"-year:2012"});

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForAnnualData(2012, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.W2CountInfo, "atfextract/expected/test_W2Counts_File"));

        Application.beginUnitOfWork();
        company = Company.findCompany("158905", SourceSystemCode.QBDT);
        Application.save(company);
        Application.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForAnnualData(2012, new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.W2CountInfo, "atfextract/expected/test_W2Counts_File"));

    }
}