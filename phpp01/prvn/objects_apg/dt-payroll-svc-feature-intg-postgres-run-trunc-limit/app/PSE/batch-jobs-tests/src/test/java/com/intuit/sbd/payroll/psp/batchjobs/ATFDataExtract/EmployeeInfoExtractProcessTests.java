package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXAssert;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMP;
import com.intuit.sbd.payroll.psp.common.ofx.request.ISETTING;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertEquals;

/**
 * User: ihannur
 * Date: 10/8/12
 * Time: 2:35 PM
 */
public class EmployeeInfoExtractProcessTests {

    static boolean extractEmployeeInfo = false;

    @BeforeClass
    public static void beforeClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        PayrollServices.beginUnitOfWork();
        extractEmployeeInfo = SystemParameter.findBooleanValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_INFO_EXTRACT);
        PayrollServices.commitUnitOfWork();
        if (!extractEmployeeInfo) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_INFO_EXTRACT, "true");
        }
    }

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
        if (!extractEmployeeInfo) {
            DataLoadServices.updateSystemParameter(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_INFO_EXTRACT, "false");
        }
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
        DataLoadServices.reinitialize();
        OFXRequestGenerator.reset();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_HappyPath"));
    }

    @Test
    public void testHappyPathWithDGDeletedCompany() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        company1.setIsDgDisassociated(true);
        Application.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedDGData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_HappyPath"));
    }

    @Test
    public void testCompanyTerminated() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
        taxCompanyServiceInfo.setLastQuarterToFile(20121);
        taxCompanyServiceInfo.setFinalAnnualReturns(true);
        taxCompanyServiceInfo.setFileAnnualReturns(true);
        taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2012, 1, 7, SpcfTimeZone.getLocalTimeZone()));
        taxCompanyServiceInfo.setStatusCd(ServiceSubStatusCode.Terminated);
        Application.save(taxCompanyServiceInfo);
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_CompanyTerminated"));
    }

    @Test
    public void testCompanyTerminatedNullLastFileQtr() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
        //taxCompanyServiceInfo.setLastQuarterToFile(1);
        taxCompanyServiceInfo.setFinalAnnualReturns(true);
        taxCompanyServiceInfo.setFileAnnualReturns(true);
        taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2012, 1, 7, SpcfTimeZone.getLocalTimeZone()));
        taxCompanyServiceInfo.setStatusCd(ServiceSubStatusCode.Terminated);
        Application.save(taxCompanyServiceInfo);
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_CompanyTerminatedLastFileQtrNull"));
    }

    @Test
    public void testActiveAndInactiveEmployees() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);

        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
            iemp.setIEMPGENDER("FEMALE");
            //iemp.setIINACTIVE("Y");
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        DomainEntitySet<Employee> employees = company.getEmployees().sort(Employee.SourceEmployeeId());
        employees.get(0).setStatusCd(EmployeeStatus.Inactive);
        Application.save(employees.get(0));
        employees.get(1).setStatusCd(EmployeeStatus.Inactive);
        Application.save(employees.get(1));

        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
        taxCompanyServiceInfo.setLastQuarterToFile(20121);
        taxCompanyServiceInfo.setFinalAnnualReturns(true);
        taxCompanyServiceInfo.setFileAnnualReturns(true);
        taxCompanyServiceInfo.setLastPayrollDate(SpcfCalendar.createInstance(2012, 1, 7, SpcfTimeZone.getLocalTimeZone()));
        Application.save(taxCompanyServiceInfo);
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_ActiveAndInactiveEmployees"));
    }

    @Test
    public void testUpdateEmployee() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_HappyPath"));

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        Employee employee = company.getEmployees().sort(Employee.SourceEmployeeId()).getFirst();
        employee.setLastName("Smith");
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_UpdateOneEmp"));
    }

    @Test
    public void testUpdateEmployeeWagePlan() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_HappyPath"));

        //If one employee wage plan is updated, all employee wage plan is extracted for that company
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        Employee employee = company.getEmployees().sort(Employee.SourceEmployeeId()).getFirst();
        EmployeeWagePlan employeeWagePlan = employee.getEmployeeWagePlanCollection().getFirst();
        employeeWagePlan.setDescription("blah blah 1");
        Application.save(employeeWagePlan);
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_UpdateOneEmpWagePlan"));
    }

    @Test
    public void testRunExtractTwice() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_HappyPath"));

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 2, SpcfTimeZone.getLocalTimeZone()));

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_RunExtractTwice"));
    }

    @Test
    public void testRemoveFitFromOneEmp() throws Exception {
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);

        int i = 6;
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIDTHIRE(PSPDate.getPSPTime().format("yyyyMMdd"));
            List<ISETTING> settings = iemp.getIEMPCOMPLIANCE().getISETTING();
            for (int count = i + 1; count % 4 != 0; count++) {
                ISETTING isetting = new ISETTING();
                isetting.setIDESCRIPTION("CA blah blah " + i);
                isetting.setIDOMAIN("WorkOrLiveState");
                isetting.setINAME("WPC");
                isetting.setIRULESVERSION("R" + i);
                isetting.setISTATE("CA");
                isetting.setIVALUE("" + i);
                settings.add(isetting);
                i++;
            }
            i++;
        }

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals(5, payrollRuns.size());
        OFXAssert.assertPayrolls(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Application.find(Employee.class).sort(Employee.SourceEmployeeId());

        //Delete first Employee FIT to test for extract
        Employee employee = employees.get(0);
        EmployeeTax empTax = assertOne(employee.getEmployeeTaxCollection().find(EmployeeTax.TaxType().equalTo(EmployeeTaxType.FIT)));
        employee.removeEmployeeTax(empTax);
        for (Iterator<TaxTableMiscData> taxTableMisc = empTax.getTaxTableMiscDataCollection().iterator(); taxTableMisc.hasNext(); ) {
            TaxTableMiscData taxTable = taxTableMisc.next();
            Application.delete(taxTable);
            taxTableMisc.remove();
        }
        Application.delete(empTax);
        Application.save(employee);
        employees.remove(employee);

        //Delete second Employee all EmployeeTax to test for extract
        employee = employees.get(0);
        for (Iterator<EmployeeTax> iterator = employee.getEmployeeTaxCollection().iterator(); iterator.hasNext(); ) {
            empTax = iterator.next();
            for (Iterator<TaxTableMiscData> taxTableMisc = empTax.getTaxTableMiscDataCollection().iterator(); taxTableMisc.hasNext(); ) {
                TaxTableMiscData taxTable = taxTableMisc.next();
                Application.delete(taxTable);
                taxTableMisc.remove();
            }
            iterator.remove();
            Application.delete(empTax);
        }
        for (Iterator<EmployeeWagePlan> iterator = employee.getEmployeeWagePlanCollection().iterator(); iterator.hasNext(); ) {
            EmployeeWagePlan wagePlan = iterator.next();
            Application.delete(wagePlan);
            iterator.remove();
        }
        Application.save(employee);
        employees.remove(employee);

        for (Employee emp : employees) {
            EmployeeTax employeeTax = assertOne(emp.getEmployeeTaxCollection().find(EmployeeTax.TaxType().equalTo(EmployeeTaxType.FIT)));
            employeeTax.setAllowances(Integer.valueOf(emp.getSourceEmployeeId()));
            Application.save(employeeTax);
        }
        PayrollServices.commitUnitOfWork();

        ATFDataExtractTestsUtil.runExtractAndValidateFilesForUpdatedData(new ATFDataExtractTestsUtil.ATFDataFile(ATFDataExtractFileType.EmployeeInfo, "atfextract/expected/test_EmployeeInfo_expected"));

    }

}
