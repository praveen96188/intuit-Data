package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 21, 2010
 * Time: 11:06:54 AM
 */
public class AssistedEmployeeProcessingTests {

    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testEmptyEmployeeCompliance() throws Exception {
        String companyPSID = "999079113";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 1, 9, 10, 3, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/HPDE999079113.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2013, 3, 27, 13, 51, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Maint999079113.txt")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        company = DataLoadServices.refreshCompany(company);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        Employee employee = assertOne(Employee.findEmployees(company));
        EmployeeWagePlan wagePlan = assertOne(employee.getEmployeeWagePlanCollection());
        assertNull("Wage Plan is valid: ",wagePlan.getInvalidDate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesAddedWithBalanceFile() throws Exception {
        DataLoadServices.setPSPDate(2012, 7, 16);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company, true);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        DataLoadServices.runOffload(company, 2012, 7, 17);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        assertSuccess(PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(),
                verificationTransactions.get(0).getFinancialTransactionAmount(),
                verificationTransactions.get(1).getFinancialTransactionAmount(), false));
        PayrollServices.commitUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        // pending first payroll
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()));
        assertEquals("Tax mode Active", QBOFX.TAX_MODES.ACTIVE, syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE());
        assertEquals("DD mode Active", QBOFX.DD_MODES.ACTIVE, syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIDDSTATUS().getIDDMODE());
    }

    @Test
    public void testMultipleOfTheSameEmployeeUpdated() throws Exception {
        DataLoadServices.setPSPDate(2012, 7, 16);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        IEMP iemp = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0);

        OFX employeeUpdate = new OFX();
        employeeUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 Arrays.asList(iemp, iemp),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        employeeUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(employeeUpdate);

        // no chances were made, so they employees should still match the balf
        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesAddedAfterBalanceFile() throws Exception {
        String psid = "123456789";
        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 OFXRequestGenerator.generateNewEmployees(2,
                                                                                                                          new ArrayList<IPITEM>(),
                                                                                                                          new ArrayList<IPITEM>(),
                                                                                                                          new ArrayList<IPITEM>()),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);

        OFX newEmployeeOfx = new OFX();
        newEmployeeOfx.setSIGNONMSGSRQV1(ofx.getSIGNONMSGSRQV1());
        newEmployeeOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage((Integer.parseInt(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN()) + 1) + "", true, ipayrolltrnrq));
        String ofxString = OFXManager.javaToOFX(newEmployeeOfx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
        qbdtPegServlet.processesRequest(new TestHttpServletRequest(ofxString), testHttpServletResponse);
        assertFalse("response contains errors " + testHttpServletResponse.toString(), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals(7, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeeNamesProperlyParsed() throws Exception {
        String psid = "123456789";
        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 OFXRequestGenerator.generateNewEmployees(1,
                                                                                                                          new ArrayList<IPITEM>(),
                                                                                                                          new ArrayList<IPITEM>(),
                                                                                                                          new ArrayList<IPITEM>()),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);

        addEmployeeByOFX(company, ofx, ipayrolltrnrq);

        PayrollServicesTest.validateQbdtRequestInfo(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT,null, TransmissionType.PayrollSubmission,"Sent Maintenance"),PayrollServicesTest.getQbdtRequestInfo(1,0,0,0,0,0,0,0,0,0,0,0));
        PayrollServicesTest.validateQbdtRequestInfo(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT,null, TransmissionType.BalanceFile,"Sent Balance File"),PayrollServicesTest.getQbdtRequestInfo(5,0,0,0,0,0,26,0,0,0,0,0));
    }

    private IPAYROLLTRNRQ generateEmployeeAddRequest(String name){
       return null;
    }

    private void addEmployeeByOFX(Company company, OFX balanceFile, IPAYROLLTRNRQ ipayrolltrnrq){
        OFX newEmployeeOfx = new OFX();
        newEmployeeOfx.setSIGNONMSGSRQV1(balanceFile.getSIGNONMSGSRQV1());
        newEmployeeOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage((Integer.parseInt(balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN()) + 1) + "", true, ipayrolltrnrq));
        String ofxString = OFXManager.javaToOFX(newEmployeeOfx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        TestHttpServletResponse testHttpServletResponse = new TestHttpServletResponse();
        QBDTPegServlet qbdtPegServlet = new QBDTPegServlet();
        qbdtPegServlet.processesRequest(new TestHttpServletRequest(ofxString), testHttpServletResponse);
        assertFalse("response contains errors " + testHttpServletResponse.toString(), QBOFX.ofxStringContainsErrorSeverity(testHttpServletResponse.toString()));
        QBDTTestHelper.assertNoErrorRequests(company);



    }

    @Test
    public void testEmployeesAddedWithInvalidId() throws Exception {
        String psid = "123456789";
        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 OFXRequestGenerator.generateNewEmployees(1,
                                                                                                                          new ArrayList<IPITEM>(),
                                                                                                                          new ArrayList<IPITEM>(),
                                                                                                                          new ArrayList<IPITEM>()),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        for (IEMP iemp : ipayrolltrnrq.getIPAYROLLRQ().getIEMP()) {
            iemp.setIEMPID("0");
        }
        OFX newEmployeeOfx = new OFX();
        newEmployeeOfx.setSIGNONMSGSRQV1(ofx.getSIGNONMSGSRQV1());
        newEmployeeOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company.getCurrentToken() + "", true, ipayrolltrnrq));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(newEmployeeOfx, false);

        assertTrue("Generic 2107 message received", response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE().contains("[Message Code 2107]"));

        PayrollServices.beginUnitOfWork();
        CompanyEventDetail companyEventDetail = assertOne(CompanyEventDetail.findCompanyEventDetails(company).find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ErrorMessage)));
        assertTrue("Error should be code 10089", companyEventDetail.getValue().contains("10089"));
    }

    @Test
    public void testEmployeesAddedWithInvalidIdWithAPaycheck() throws Exception {
        DataLoadServices.setPSPDate(2012, 2, 1);
        String psid = "123456789";
        OFX ofx = QBDTTestHelper.setupCompanyAndSubmitBalanceFile(psid);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        IPAYROLLRUN ipayrollrun = OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                                         ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                                         new Date("02/15/2012"), new Date("02/15/2012"), new Date("02/15/2012"), false);
        for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
            ipaychk.setIEMPID("0");
            ipaychk.setIEMPNAME("First Last");
        }
        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 Arrays.asList(ipayrollrun));
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company.getCurrentToken() + "", true, ipayrolltrnrq));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx, false);

        assertTrue("Generic 2107 message received", response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE().contains("[Message Code 2107]"));

        PayrollServices.beginUnitOfWork();
        CompanyEventDetail companyEventDetail = assertOne(CompanyEventDetail.findCompanyEventDetails(company).find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ErrorMessage)));
        assertTrue("Error should be code 10089", companyEventDetail.getValue().contains("10089"));
    }

    @Test
    public void testEmployeesAccrualUpdates() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);

        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        OFX accrualUpdates = OFXRequestGenerator.generateEmployeeAccrualUpdates(balanceFileOFX);
        QBDTTestHelper.submitQBDTRequest(accrualUpdates);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEmployeeAccrualUpdates(employees, accrualUpdates.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesFullUpdates() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        OFX updateOFX = OFXRequestGenerator.generateEmployeeFullUpdates(balanceFileOFX, DataLoadServices.refreshCompany(company));
        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEmployeeAccrualUpdates(employees, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesFullUpdates_MaxLengths() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);

        StringBuffer buffer = new StringBuffer(255);
        for (int i = 0; i < 255; i++) {
            buffer.append("1");
        }

        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        OFX updateOFX = OFXRequestGenerator.generateEmployeeFullUpdates(balanceFileOFX, DataLoadServices.refreshCompany(company));
        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEmployeeAccrualUpdates(employees, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testInactiveEmployeeFullUpdates() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();


        OFX inactiveEmployeeOfx = new OFX();
        inactiveEmployeeOfx.setSIGNONMSGSRQV1(balanceFileOFX.getSIGNONMSGSRQV1());

        List<IEMP> employeeMods = new ArrayList<IEMP>();
        for (IEMP iemp : balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIINACTIVE("Y");
            employeeMods.add(iemp);
        }

        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 employeeMods,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);

        inactiveEmployeeOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage((Integer.parseInt(balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN()) + 1) + "", true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(inactiveEmployeeOfx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEmployeeAccrualUpdates(employees, inactiveEmployeeOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();

        OFX updateOFX = OFXRequestGenerator.generateEmployeeFullUpdates(balanceFileOFX, DataLoadServices.refreshCompany(company));
        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEmployeeAccrualUpdates(employees, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesDeletes() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        OFX updateOFX = OFXRequestGenerator.generateEmployeeDeletes(balanceFileOFX);
        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        for (Employee employee : employees) {
            assertTrue("Employee " + employee.getSourceEmployeeId() + " not deleted.", employee.getQbdtEmployeeInfo().getIsDeleted());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeeUpdatesBeforeAdd() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IEMP> iemps = new ArrayList<IEMP>();
        for (IEMP iemp : balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemps.add(iemp);
        }
        balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().removeAll(iemps);
        assertEquals("removed ees", 0, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().size());
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(0, employees.size());
        PayrollServices.rollbackUnitOfWork();

        balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().addAll(iemps);
        OFX updateOFX = OFXRequestGenerator.generateEmployeeFullUpdates(balanceFileOFX, DataLoadServices.refreshCompany(company));
        assertEquals("ee adds", 0, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().size());
        QBDTTestHelper.submitQBDTRequest(updateOFX);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();

        QBDTTestHelper.assertNoErrorRequests(company);
    }

    @Test
    public void testEmployeeAccrualUpdateBeforeAdd() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IEMP> iemps = new ArrayList<IEMP>();
        for (IEMP iemp : balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemps.add(iemp);
        }
        balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().removeAll(iemps);
        assertEquals("removed ees", 0, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().size());
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(0, employees.size());
        PayrollServices.rollbackUnitOfWork();

        balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().addAll(iemps);
        OFX updateOFX = OFXRequestGenerator.generateEmployeeAccrualUpdates(balanceFileOFX);
        assertEquals("ee adds", 0, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().size());
        assertEquals("ee mods", 5, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().size());
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponse = QBDTTestHelper.submitQBDTRequest(updateOFX, false);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEventDetail.findCompanyEventDetails(Company.findCompany(psid, SourceSystemCode.QBDT)).find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ErrorMessage));
        Assert.assertEquals(1, companyEventDetails.size());
        CompanyEventDetail companyEventDetail = companyEventDetails.get(0);
        assertTrue("5001 error recorded", companyEventDetail.toString().contains("Employee Name has invalid value"));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesAddTwice() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        OFX updateOFX = OFXRequestGenerator.generateEmployeeFullUpdates(balanceFileOFX, DataLoadServices.refreshCompany(company));
        updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().addAll(updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().removeAll(updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesAddedTwiceInBalanceFile() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IEMP> iemps = new ArrayList<IEMP>();
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemps.add(iemp);
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().addAll(iemps);
        assertEquals("employee adds", 10, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().size());
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesAddedAndModInBalanceFile() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IEMP> iemps = new ArrayList<IEMP>();
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemps.add(iemp);
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().addAll(iemps);
        assertEquals("employee adds", 5, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().size());
        assertEquals("employee mods", 5, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().size());
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesDeletesWithNoAddsBalanceFile() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(0, employees.size());
        PayrollServices.rollbackUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        List<String> iempIds = new ArrayList<String>();
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iempIds.add(iemp.getIEMPID());
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPDELID().addAll(iempIds);
        assertEquals("employee adds", 0, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().size());
        assertEquals("employee deletes", 5, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPDELID().size());
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals(0, employees.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployeesDeletes_EmptyNameSSN() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        for (IEMP iemp : balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            //iemp.setIEMPNAME("");
            iemp.setISSN("");
            if (iemp.getIADDRINFO() != null) {
                //iemp.getIADDRINFO().setIFIRST("");
                //iemp.getIADDRINFO().setILAST("");
            }
        }
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        OFX updateOFX = OFXRequestGenerator.generateEmployeeDeletes(balanceFileOFX);
        for (IEMP iemp : updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIEMPNAME("");
            iemp.setISSN("");
            if (iemp.getIADDRINFO() != null) {
                iemp.getIADDRINFO().setIFIRST("");
                iemp.getIADDRINFO().setILAST("");
            }
        }
        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        for (Employee employee : employees) {
            assertTrue("Employee " + employee.getSourceEmployeeId() + " not deleted.", employee.getQbdtEmployeeInfo().getIsDeleted());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployee_TokenUpdate() {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008,1,1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        assertEquals("beginning token", 1, company.getCurrentToken());
        long currentToken = company.getCurrentToken();
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        ++currentToken;
        DomainEntitySet<Employee> employees = Application.find(Employee.class);
        for (Employee employee : employees) {
            assertEquals("employee token", currentToken, employee.getQbdtEmployeeInfo().getToken());
        }
        PayrollServices.rollbackUnitOfWork();
        company = DataLoadServices.refreshCompany(company);
        assertEquals("company token", currentToken, company.getCurrentToken());

        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.setIEMPNAME(iemp.getIEMPNAME() + 1);
            OFX employeeUpdate = new OFX();
            employeeUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
            IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                     null,
                                                                                     null,
                                                                                     Arrays.asList(iemp),
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null);
            employeeUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
            QBDTTestHelper.submitQBDTRequest(employeeUpdate);
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            Employee employee = Employee.findEmployee(company, iemp.getIEMPID());
            assertEquals("employee token", ++currentToken, employee.getQbdtEmployeeInfo().getToken());
            PayrollServices.rollbackUnitOfWork();
            company = DataLoadServices.refreshCompany(company);
            assertEquals("company token", currentToken, company.getCurrentToken());
            company = DataLoadServices.refreshCompany(company);
        }
    }

    @Test
    public void testEmployee_Sync() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        long syncToken = company.getCurrentToken()-1;
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, syncToken);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        assertEmployeesMatch(ofx, syncResponse);

        // test duplicate payroll items submitted
        company = DataLoadServices.refreshCompany(company);
        long nextId = Long.parseLong(company.getNextPayrollItemId());
        List<IPITEM> balanceFileItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        List<String> newPayrollItemIdList = new ArrayList<String>();
        for (IPITEM balanceFileItem : balanceFileItems) {
            String newPayrollItemId = nextId++ + "";
            newPayrollItemIdList.add(newPayrollItemId);
            balanceFileItem.setIPITEMID(newPayrollItemId);
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD().setIDTFILEQTRSTART(null);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD().setITAXREADY(null);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, syncToken);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        assertEquals("Employee mods", 5, syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPMOD().size());
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IEMP iemp : syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPMOD()) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IADJ iadj : iemp.getIPAYROLL().getIADJ()) {
                assertTrue("Not new payroll item", newPayrollItemIdList.contains(iadj.getIPITEMID()));
            }
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IWAGE iwage : iemp.getIPAYROLL().getIWAGE()) {
                assertTrue("Not new payroll item", newPayrollItemIdList.contains(iwage.getIPITEMID()));
            }
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IEMPOTHERTAX iempothertax : iemp.getIEMPTAX().getIEMPOTHERTAX()) {
                assertTrue("Not new payroll item", newPayrollItemIdList.contains(iempothertax.getIPITEMID()));
            }
        }
    }

    private void assertEmployeesMatch(OFX pOfx, com.intuit.sbd.payroll.psp.common.ofx.response.OFX pSyncResponse) {
        List<IEMP> balanceFileEmployees = pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP();
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IEMP> syncResponseEmployees = pSyncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPMOD();
        assertEquals("Employee mods", balanceFileEmployees.size(), syncResponseEmployees.size());

        for (IEMP balanceFileEmp : balanceFileEmployees) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IEMP syncResponseEmp : syncResponseEmployees) {
                if(balanceFileEmp.getIEMPID().equals(syncResponseEmp.getIEMPID())) {
                    assertIEMPEquals(balanceFileEmp, syncResponseEmp);
                    break;
                }
            }
        }
    }

    @Test
    public void testEmployee_SyncDeleted() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        IEMP iemp = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0);

        OFX employeeDelete = new OFX();
        employeeDelete.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 Arrays.asList(iemp.getIEMPID()),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        employeeDelete.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(employeeDelete);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        List<String> delIds = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPDELID();
        assertEquals("deleted item ids", 1, delIds.size());
        assertEquals("deleted item id", iemp.getIEMPID(), delIds.get(0));
    }

    @Test
    public void testEmployee_SyncNoFit() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.getIEMPTAX().setIEMPFIT(null);
            iemp.getIEMPTAX().setISUBJTOFIT("N");
        }
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        assertEmployeesMatch(ofx, syncResponse);
    }

    @Test
    public void testEmployee_EmployeeNullDDAccount() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        IEMP iemp = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();

        // submit employee with null account fields
        iemp.getIEMPDD().setIUSEDD(QBOFX.Y_N(false));
        iemp.getIEMPDD().getIDDACCT().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().add(iemp);
        QBDTTestHelper.submitQBDTRequest(ofx);
        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals("employees", 1, employees.size());
        assertEquals("use dd", false, employees.get(0).getQbdtEmployeeInfo().getUseDD());
        assertEquals("employee bank accounts", 0, employees.get(0).getEmployeeBankAccountCollection().size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 2, 1);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = Application.find(CompanyPayrollItem.class, CompanyPayrollItem.Company().equalTo(company)
                                                                                                                               .And(CompanyPayrollItem.QbdtPayrollItemInfo().SpecialType().equalTo(QbdtSpecialType.DIRDEP)));
        Assert.assertEquals(1, companyPayrollItems.size());
        CompanyPayrollItem ddItem = companyPayrollItems.get(0);
        PayrollServices.rollbackUnitOfWork();

        // submit a payroll with null dd lines
        IPAYROLLRUN ipayrollrun = OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                                         ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                                         new Date("02/15/2012"), new Date("02/15/2012"), new Date("02/15/2012"), false);
        for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
            IDDLINE iddline = new IDDLINE();
            iddline.setIPITEMID(ddItem.getSourcePayrollItemId());
            iddline.setIAMT(ipaychk.getIAMT());
            IDDACCT iddacct = new IDDACCT();
            iddline.setIDDACCT(iddacct);
            BANKACCT bankacct = new BANKACCT();
            iddacct.setBANKACCTTO(bankacct);
            iddline.getIDDACCT().setIACCTNAME(QBOFX.NULL);
            iddline.getIDDACCT().setIAMT(QBOFX.NULL);
            iddline.getIDDACCT().getBANKACCTTO().setBANKID(QBOFX.NULL);
            iddline.getIDDACCT().getBANKACCTTO().setACCTID(QBOFX.NULL);
            iddline.getIDDACCT().getBANKACCTTO().setACCTTYPE(QBOFX.OFXBankAccountType.UNKNOWN.toString());
            ipaychk.getIDDLINE().add(iddline);
        }
        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Arrays.asList(ipayrollrun));
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollOfx, false);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEventDetail> companyEventDetails = CompanyEventDetail.findCompanyEventDetails(Company.findCompany(psid, SourceSystemCode.QBDT)).find(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ErrorMessage));
        Assert.assertEquals(1, companyEventDetails.size());
        CompanyEventDetail companyEventDetail = companyEventDetails.get(0);
        assertTrue("5001 error recorded", companyEventDetail.toString().contains("Invalid bank account found"));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testEmployee_EmployeeDDAccounts() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        IEMP iemp = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();

        // start with no accounts
        iemp.getIEMPDD().setIUSEDD(QBOFX.Y_N(false));
        iemp.getIEMPDD().getIDDACCT().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().add(iemp);
        QBDTTestHelper.submitQBDTRequest(ofx);
        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals("employees", 1, employees.size());
        assertEquals("use dd", false, employees.get(0).getQbdtEmployeeInfo().getUseDD());
        assertEquals("employee bank accounts", 0, employees.get(0).getEmployeeBankAccountCollection().size());
        PayrollServices.rollbackUnitOfWork();

        // add 1 account without setting the dd flag
        iemp.getIEMPDD().getIDDACCT().add(OFXRequestGenerator.generateDDAccount(1));
        OFX employeeUpdate = new OFX();
        employeeUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 Arrays.asList(iemp),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        employeeUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(employeeUpdate);
        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals("employees", 1, employees.size());
        assertEquals("use dd", false, employees.get(0).getQbdtEmployeeInfo().getUseDD());
        assertEquals("employee bank accounts", 1, employees.get(0).getEmployeeBankAccountCollection().size());
        EmployeeBankAccount employeeBankAccount = employees.get(0).getEmployeeBankAccountCollection().get(0);
        assetEmployeeBankAccount(employeeBankAccount, 1);
        assertEquals("status", BankAccountStatus.Active, employeeBankAccount.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        // remove the account
        iemp.getIEMPDD().getIDDACCT().clear();
        employeeUpdate = new OFX();
        employeeUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                   null,
                                                                   null,
                                                                   Arrays.asList(iemp),
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null);
        employeeUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(employeeUpdate);
        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals("employees", 1, employees.size());
        assertEquals("use dd", false, employees.get(0).getQbdtEmployeeInfo().getUseDD());
        assertEquals("employee bank accounts", 1, employees.get(0).getEmployeeBankAccountCollection().size());
        employeeBankAccount = employees.get(0).getEmployeeBankAccountCollection().get(0);
        assetEmployeeBankAccount(employeeBankAccount, 1);
        assertEquals("status", BankAccountStatus.Inactive, employeeBankAccount.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        // add same account and a new account, change the DD flag
        iemp.getIEMPDD().setIUSEDD(QBOFX.Y_N(true));
        iemp.getIEMPDD().getIDDACCT().add(OFXRequestGenerator.generateDDAccount(1));
        iemp.getIEMPDD().getIDDACCT().add(OFXRequestGenerator.generateDDAccount(2));
        employeeUpdate = new OFX();
        employeeUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                   null,
                                                                   null,
                                                                   Arrays.asList(iemp),
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null);
        employeeUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(employeeUpdate);
        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals("employees", 1, employees.size());
        assertEquals("use dd", true, employees.get(0).getQbdtEmployeeInfo().getUseDD());
        assertEquals("employee bank accounts", 3, employees.get(0).getEmployeeBankAccountCollection().size());
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class,
                                                                                     EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
        assertEquals("active employee bank accounts", 2, employeeBankAccounts.size());
        for (EmployeeBankAccount bankAccount : employeeBankAccounts) {
            assetEmployeeBankAccount(bankAccount, (int)bankAccount.getAmount());
            assertEquals("status", BankAccountStatus.Active, bankAccount.getStatusCd());
        }
        PayrollServices.rollbackUnitOfWork();

        // remove 1 account, update non account info for other
        IDDACCT iddacct = iemp.getIEMPDD().getIDDACCT().get(1);
        iemp.getIEMPDD().getIDDACCT().clear();
        iddacct.setIACCTNAME("changed");
        iddacct.setIAMT("100%");
        iemp.getIEMPDD().getIDDACCT().add(iddacct);
        employeeUpdate = new OFX();
        employeeUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                   null,
                                                                   null,
                                                                   Arrays.asList(iemp),
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null);
        employeeUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(employeeUpdate);
        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals("employees", 1, employees.size());
        assertEquals("use dd", true, employees.get(0).getQbdtEmployeeInfo().getUseDD());
        // the account changed order, so we treat it like a new account. We do this so we can allow the same account to be used for multiple lines.
        assertEquals("employee bank accounts", 4, employees.get(0).getEmployeeBankAccountCollection().size());
        employeeBankAccounts = Application.find(EmployeeBankAccount.class,
                                                EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
        assertEquals("active employee bank accounts", 1, employeeBankAccounts.size());
        assertEquals("account type", BankAccountType.Checking, employeeBankAccounts.get(0).getBankAccount().getAccountTypeCd());
        assertEquals("account type", QbdtNumericType.Percentage, employeeBankAccounts.get(0).getAmountType());
        assertEquals("account", 100, employeeBankAccounts.get(0).getAmount());
        assertEquals("account number", "123456782", employeeBankAccounts.get(0).getBankAccount().getAccountNumber());
        assertEquals("routing number", "111000025", employeeBankAccounts.get(0).getBankAccount().getRoutingNumber());
        assertEquals("bank name", "changed", employeeBankAccounts.get(0).getBankAccount().getBankName());
        PayrollServices.rollbackUnitOfWork();

        // add same account twice
        iemp.getIEMPDD().getIDDACCT().add(iemp.getIEMPDD().getIDDACCT().get(0));
        employeeUpdate = new OFX();
        employeeUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                   null,
                                                                   null,
                                                                   Arrays.asList(iemp),
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null);
        employeeUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(employeeUpdate);
        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals("employees", 1, employees.size());
        assertEquals("use dd", true, employees.get(0).getQbdtEmployeeInfo().getUseDD());
        assertEquals("employee bank accounts", 5, employees.get(0).getEmployeeBankAccountCollection().size());
        employeeBankAccounts = Application.find(EmployeeBankAccount.class,
                                                EmployeeBankAccount.StatusCd().equalTo(BankAccountStatus.Active));
        assertEquals("active employee bank accounts", 2, employeeBankAccounts.size());
        for (EmployeeBankAccount bankAccount : employeeBankAccounts) {
            assertEquals("account type", BankAccountType.Checking, bankAccount.getBankAccount().getAccountTypeCd());
            assertEquals("account type", QbdtNumericType.Percentage, bankAccount.getAmountType());
            assertEquals("account", 100, bankAccount.getAmount());
            assertEquals("account number", "123456782", bankAccount.getBankAccount().getAccountNumber());
            assertEquals("routing number", "111000025", bankAccount.getBankAccount().getRoutingNumber());
            assertEquals("bank name", "changed", bankAccount.getBankAccount().getBankName());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    //PSRV003419: Employee is not being updated properly in PSP
    @Test
    public void testEmployeesModsWithTerminationDate() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, balanceFileOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        PayrollServices.rollbackUnitOfWork();

        //Submit request with Emp MOD and terminate all employees
        OFX updateOFX = OFXRequestGenerator.generateEmployeeFullUpdates(balanceFileOFX, DataLoadServices.refreshCompany(company));
        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();

        //Submit request with Emp MOD with null terminate date - re-hired
        updateOFX = OFXRequestGenerator.generateEmployeeFullUpdates(balanceFileOFX, DataLoadServices.refreshCompany(company));
        for (IEMP iemp : updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD()) {
            iemp.setIDTRELEASE(null);
            iemp.setIDTHIRE(null);
        }

        QBDTTestHelper.submitQBDTRequest(updateOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        employees = Employee.findEmployees(company);
        assertEquals(5, employees.size());
        OFXAssert.assertEmployees(employees, updateOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBankAccountAddedDuringPayrollSubmission() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 8, 15);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFile = OFXRequestGenerator.generateBalanceFile(psid, false);

        IEMP iemp = balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0);
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().add(iemp);
        QBDTTestHelper.submitQBDTRequest(balanceFile);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        assertEmployeesMatch(balanceFile, syncResponse);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 8, 17), employees, new HashMap<String, String>());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            for (DDTransactionDTO ddTransactionDTO : paycheckDTO.getDdTransactions()) {
                // add a character to the bank accounts so that new accounts will be created
                ddTransactionDTO.getEmployeeBankAccount().getBankAccount().setAccountNumber(ddTransactionDTO.getEmployeeBankAccount().getBankAccount().getAccountNumber() + "1");
            }
        }
        PayrollServices.rollbackUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        // make sure that the new bank accounts will not be returned with the employee sync
        company = DataLoadServices.refreshCompany(company);
        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        assertEmployeesMatch(balanceFile, syncResponse);

        PayrollServices.beginUnitOfWork();
        assertEquals(4, Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().Company().equalTo(company)).size());
        PayrollServices.rollbackUnitOfWork();

        // submit a second payroll with the same bank accounts
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        // we should not add the accounts again
        PayrollServices.beginUnitOfWork();
        assertEquals(4, Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().Company().equalTo(company)).size());
        PayrollServices.rollbackUnitOfWork();

        // add the same account to the employee
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().setICOINFOMOD(null);
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().clear();
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().clear();
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().add(iemp);

        for (IDDACCT iddacct : iemp.getIEMPDD().getIDDACCT()) {
            iddacct.getBANKACCTTO().setACCTID(iddacct.getBANKACCTTO().getACCTID() + "1");
        }

        company = DataLoadServices.refreshCompany(company);
        balanceFile.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(balanceFile);

        PayrollServices.beginUnitOfWork();
        assertEquals(6, Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().Company().equalTo(company)).size());
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        assertEmployeesMatch(balanceFile, syncResponse);
    }

    private void assetEmployeeBankAccount(EmployeeBankAccount pEmployeeBankAccount, int generationValue) {
        if(generationValue % 2 == 0) {
            assertEquals("account type", QbdtNumericType.MoneyType, pEmployeeBankAccount.getAmountType());
            assertEquals("account type", BankAccountType.Checking, pEmployeeBankAccount.getBankAccount().getAccountTypeCd());
        } else {
            assertEquals("account type", QbdtNumericType.Percentage, pEmployeeBankAccount.getAmountType());
            assertEquals("account type", BankAccountType.Savings, pEmployeeBankAccount.getBankAccount().getAccountTypeCd());
        }
        assertEquals("account", generationValue, pEmployeeBankAccount.getAmount());
        assertEquals("account number", "12345678" + generationValue, pEmployeeBankAccount.getBankAccount().getAccountNumber());
        assertEquals("routing number", "111000025", pEmployeeBankAccount.getBankAccount().getRoutingNumber());
        assertEquals("bank name", "bank " + generationValue, pEmployeeBankAccount.getBankAccount().getBankName());
    }

    private void assertEmployeeAccrualUpdates(DomainEntitySet<Employee> pEmployees, Collection<IEMP> pOFXEmployees) {
        for (IEMP iemp : pOFXEmployees) {
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee ofxEmployee = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee(iemp);
            Employee employee = pEmployees.findEntity(Employee.SourceEmployeeId().equalTo(ofxEmployee.getSourceId()));
            assertNotNull("employee", employee);
            QbdtEmployeeInfo qbdtEmployeeInfo = employee.getQbdtEmployeeInfo();
            assertNotNull(qbdtEmployeeInfo);
            assertTrue(qbdtEmployeeInfo.getIsAssisted());
            int accrualsCount = (ofxEmployee.getSickAccrual() != null ? 1 : 0) + (ofxEmployee.getVacationAccrual() != null ? 1 : 0);
            assertEquals(accrualsCount, employee.getEmployeeAccrualCollection().size());
            for (EmployeeAccrual employeeAccrual : employee.getEmployeeAccrualCollection()) {
                assertNotNull(employeeAccrual.getAccrualType());
                com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Employee.EmployeeAccrual ofxEmployeeAccrual = null;
                if(employeeAccrual.getAccrualType().equals(AccrualType.Sick)) {
                    ofxEmployeeAccrual = ofxEmployee.getSickAccrual();
                } else if(employeeAccrual.getAccrualType().equals(AccrualType.Vacation)) {
                    ofxEmployeeAccrual = ofxEmployee.getVacationAccrual();
                }
                if(ofxEmployeeAccrual != null) {
                    assertEquals(ofxEmployeeAccrual.getHours(), employeeAccrual.getHours());
                }
            }
        }
    }

    private void assertIEMPEquals(IEMP pReqEmp, com.intuit.sbd.payroll.psp.common.ofx.response.IEMP pRespEmp) {
        assertEquals(pReqEmp.getIADDRINFO().getIADDR1(), pRespEmp.getIADDRINFO().getIADDR1());
        assertEquals(pReqEmp.getIADDRINFO().getIADDR2(), pRespEmp.getIADDRINFO().getIADDR2());
        assertEquals(pReqEmp.getIADDRINFO().getICITY(), pRespEmp.getIADDRINFO().getICITY());
        assertEquals(pReqEmp.getIADDRINFO().getISTATE(), pRespEmp.getIADDRINFO().getISTATE());
        assertEquals(pReqEmp.getIADDRINFO().getIPOSTALCODE(), pRespEmp.getIADDRINFO().getIPOSTALCODE());

        assertEquals(pReqEmp.getIADDRINFO().getIALTPHONE(), pRespEmp.getIADDRINFO().getIALTPHONE());
        assertEquals(pReqEmp.getIBILLPAYACCT(), pRespEmp.getIBILLPAYACCT());
        assertEquals(pReqEmp.getIPAYROLL().getICLASS(), pRespEmp.getIPAYROLL().getICLASS());
        assertEquals(pReqEmp.getIEMPTYPE(), pRespEmp.getIEMPTYPE());
        assertEquals(pReqEmp.getIEMPTAX().getIENFORCESUBJECTTO(), pRespEmp.getIEMPTAX().getIENFORCESUBJECTTO());
        assertEquals(pReqEmp.getIADDRINFO().getIINITIALS(), pRespEmp.getIADDRINFO().getIINITIALS());
        assertEquals(pReqEmp.getIADDRINFO().getIPRINTASNAME(), pRespEmp.getIADDRINFO().getIPRINTASNAME());
        assertEquals(pReqEmp.getIADDRINFO().getITITLE(), pRespEmp.getIADDRINFO().getITITLE());
        assertEquals(pReqEmp.getIEMPTAX().getIDECEASED(), pRespEmp.getIEMPTAX().getIDECEASED());
        assertEquals(pReqEmp.getIPAYROLL().getIUSETIME(), pRespEmp.getIPAYROLL().getIUSETIME());

        assertEquals(pReqEmp.getIADDRINFO().getIEMAIL(), pRespEmp.getIADDRINFO().getIEMAIL());
        if(pReqEmp.getIEMPTAX().getIEMPFIT() != null) {
            assertEquals(pReqEmp.getIEMPTAX().getIEMPFIT().getIALLOWANCES(), pRespEmp.getIEMPTAX().getIEMPFIT().getIALLOWANCES());
            assertEquals(pReqEmp.getIEMPTAX().getIEMPFIT().getIEXTRAWITHHOLD(), pRespEmp.getIEMPTAX().getIEMPFIT().getIEXTRAWITHHOLD());
            assertEquals(pReqEmp.getIEMPTAX().getIEMPFIT().getIFEDFILESTATUS(), pRespEmp.getIEMPTAX().getIEMPFIT().getIFEDFILESTATUS());
            assertEquals(pReqEmp.getIEMPTAX().getIEMPFIT().getITAXTBLMISCDATA().size(), pRespEmp.getIEMPTAX().getIEMPFIT().getITAXTBLMISCDATA().size());
        } else {
            assertNull(pRespEmp.getIEMPTAX().getIEMPFIT());
        }
        assertEquals(pReqEmp.getIADDRINFO().getIFIRST(), pRespEmp.getIADDRINFO().getIFIRST());
        assertEquals(pReqEmp.getIEMPGENDER(), pRespEmp.getIEMPGENDER());
        assertEquals(pReqEmp.getIPAYROLL().getIPPLANOVERRIDE(), pRespEmp.getIPAYROLL().getIPPLANOVERRIDE());
        assertEquals(pReqEmp.getIADDRINFO().getILAST(), pRespEmp.getIADDRINFO().getILAST());
        assertEquals(pReqEmp.getIADDRINFO().getISTATELIVED(), pRespEmp.getIADDRINFO().getISTATELIVED());
        assertEquals(pReqEmp.getIADDRINFO().getIMI(), pRespEmp.getIADDRINFO().getIMI());
        assertEquals(pReqEmp.getIPAYROLL().getIPAYPD(), pRespEmp.getIPAYROLL().getIPAYPD());
        assertEquals(pReqEmp.getIADDRINFO().getIPHONE(), pRespEmp.getIADDRINFO().getIPHONE());
        assertEquals(pReqEmp.getIEMPTAX().getIQUALFORAEIC(), pRespEmp.getIEMPTAX().getIQUALFORAEIC());
        assertEquals(pReqEmp.getIINACTIVE(), pRespEmp.getIINACTIVE());
        assertEquals(pReqEmp.getISSN(), pRespEmp.getISSN());
        assertEquals(pReqEmp.getIADDRINFO().getISTATEWORKED(), pRespEmp.getIADDRINFO().getISTATEWORKED());
        assertEquals(pReqEmp.getIDTHIRE(), pRespEmp.getIDTHIRE());
        if(pRespEmp.getIDTRELEASE() != null) {
            assertEquals(pReqEmp.getIDTRELEASE(), pRespEmp.getIDTRELEASE());
        }

        assertEquals(pReqEmp.getIEMPCOMPLIANCE().getISETTING().size(), pRespEmp.getIEMPCOMPLIANCE().getISETTING().size());
        // there is no significant order that can be relied on unless there is only one
        if(pRespEmp.getIEMPCOMPLIANCE().getISETTING().size() == 1) {
            ISETTING reqSetting = pReqEmp.getIEMPCOMPLIANCE().getISETTING().get(0);
            com.intuit.sbd.payroll.psp.common.ofx.response.ISETTING respSetting = pRespEmp.getIEMPCOMPLIANCE().getISETTING().get(0);
            assertEquals(reqSetting.getIDESCRIPTION(), respSetting.getIDESCRIPTION());
            assertEquals(reqSetting.getISTATE(), respSetting.getISTATE());
            assertEquals(reqSetting.getIDOMAIN(), respSetting.getIDOMAIN());
            assertEquals(reqSetting.getINAME(), respSetting.getINAME());
            assertEquals(reqSetting.getIRULESVERSION(), respSetting.getIRULESVERSION());
            assertEquals(reqSetting.getIVALUE(), respSetting.getIVALUE());
        }

        assertEquals(pReqEmp.getIPAYROLL().getIWAGE().size(), pRespEmp.getIPAYROLL().getIWAGE().size());
        // order matters, so they should be in the same order
        for (int i = 0; i < pReqEmp.getIPAYROLL().getIWAGE().size(); i++) {
            IWAGE reqWage = pReqEmp.getIPAYROLL().getIWAGE().get(i);
            com.intuit.sbd.payroll.psp.common.ofx.response.IWAGE respWage = pRespEmp.getIPAYROLL().getIWAGE().get(i);
            assertEquals(reqWage.getIPITEMID(), respWage.getIPITEMID());
            assertEquals(reqWage.getIRATE(), respWage.getIRATE());
        }

        assertEquals(pReqEmp.getIPAYROLL().getIADJ().size(), pRespEmp.getIPAYROLL().getIADJ().size());
        // order matters, so they should be in the same order
        for (int i = 0; i < pReqEmp.getIPAYROLL().getIADJ().size(); i++) {
            IADJ reqAdj = pReqEmp.getIPAYROLL().getIADJ().get(i);
            com.intuit.sbd.payroll.psp.common.ofx.response.IADJ respAdj = pRespEmp.getIPAYROLL().getIADJ().get(i);
            assertEquals(reqAdj.getIPITEMID(), respAdj.getIPITEMID());
            assertEquals(reqAdj.getIAMT(), respAdj.getIAMT());
            assertEquals(reqAdj.getILIMIT(), respAdj.getILIMIT());
        }

        assertEquals(pReqEmp.getIHASCUSTOMFLD(), pRespEmp.getIHASCUSTOMFLD());
        assertEquals(pReqEmp.getICUSTOMFLD().size(), pRespEmp.getICUSTOMFLD().size());
        // order matters, so they should be in the same order
        for (int i = 0; i < pReqEmp.getICUSTOMFLD().size(); i++) {
            ICUSTOMFLD reqCustomfld = pReqEmp.getICUSTOMFLD().get(i);
            com.intuit.sbd.payroll.psp.common.ofx.response.ICUSTOMFLD respCustomfld = pRespEmp.getICUSTOMFLD().get(i);
            assertEquals(reqCustomfld.getIFLDNAME(), respCustomfld.getIFLDNAME());
            assertEquals(reqCustomfld.getIFLDVALUE(), respCustomfld.getIFLDVALUE());
        }

        assertEquals(pReqEmp.getISICK().getIACCRUALPD(), pReqEmp.getISICK().getIACCRUALPD());
        assertEquals(pReqEmp.getISICK().getIHRS(), pReqEmp.getISICK().getIHRS());
        assertEquals(pReqEmp.getISICK().getIHRSPERPD(), pReqEmp.getISICK().getIHRSPERPD());
        assertEquals(pReqEmp.getISICK().getIMAXHRS(), pReqEmp.getISICK().getIMAXHRS());
        assertEquals(pReqEmp.getISICK().getINEWYRRESET(), pReqEmp.getISICK().getINEWYRRESET());

        assertEquals(pReqEmp.getIVAC().getIACCRUALPD(), pReqEmp.getIVAC().getIACCRUALPD());
        assertEquals(pReqEmp.getIVAC().getIHRS(), pReqEmp.getIVAC().getIHRS());
        assertEquals(pReqEmp.getIVAC().getIHRSPERPD(), pReqEmp.getIVAC().getIHRSPERPD());
        assertEquals(pReqEmp.getIVAC().getIMAXHRS(), pReqEmp.getIVAC().getIMAXHRS());
        assertEquals(pReqEmp.getIVAC().getINEWYRRESET(), pReqEmp.getIVAC().getINEWYRRESET());

        assertEquals(pReqEmp.getIEMPTAX().getIEMPOTHERTAX().size(), pRespEmp.getIEMPTAX().getIEMPOTHERTAX().size());
        // order matters, so they should be in the same order
        for (int i = 0; i < pReqEmp.getIEMPTAX().getIEMPOTHERTAX().size(); i++) {
            IEMPOTHERTAX reqEmpothertax = pReqEmp.getIEMPTAX().getIEMPOTHERTAX().get(i);
            com.intuit.sbd.payroll.psp.common.ofx.response.IEMPOTHERTAX respEmpothertax = pRespEmp.getIEMPTAX().getIEMPOTHERTAX().get(i);
            assertEquals(respEmpothertax.getIPITEMID(), respEmpothertax.getIPITEMID());
            assertEquals(reqEmpothertax.getITAXLAWVER(), respEmpothertax.getITAXLAWVER());
            assertEquals(reqEmpothertax.getIW2NAME(), respEmpothertax.getIW2NAME());
            assertEquals(reqEmpothertax.getITAXTBLMISCDATA().size(), respEmpothertax.getITAXTBLMISCDATA().size());
        }

        assertEquals(pReqEmp.getIEMPTAX().getIEMPSDI().getISTATE(), pRespEmp.getIEMPTAX().getIEMPSDI().getISTATE());

        assertEquals(pReqEmp.getIEMPTAX().getIEMPSIT().getISTATE(), pRespEmp.getIEMPTAX().getIEMPSIT().getISTATE());
        assertEquals(pReqEmp.getIEMPTAX().getIEMPSIT().getIALLOWANCES(), pRespEmp.getIEMPTAX().getIEMPSIT().getIALLOWANCES());
        assertEquals(pReqEmp.getIEMPTAX().getIEMPSIT().getIEXTRAWITHHOLD(), pRespEmp.getIEMPTAX().getIEMPSIT().getIEXTRAWITHHOLD());
        assertEquals(pReqEmp.getIEMPTAX().getIEMPSIT().getISTATEFILESTATUS(), pRespEmp.getIEMPTAX().getIEMPSIT().getISTATEFILESTATUS());
        assertEquals(pReqEmp.getIEMPTAX().getIEMPSIT().getITAXLAWVER(), pRespEmp.getIEMPTAX().getIEMPSIT().getITAXLAWVER());
        assertEquals(pReqEmp.getIEMPTAX().getIEMPSIT().getITAXTBLMISCDATA().size(), pRespEmp.getIEMPTAX().getIEMPSIT().getITAXTBLMISCDATA().size());

        assertEquals(pReqEmp.getIEMPTAX().getIEMPSUI().getISTATE(), pRespEmp.getIEMPTAX().getIEMPSUI().getISTATE());

        assertEquals(pReqEmp.getIEMPTAX().getISUBJTOFIT(), pRespEmp.getIEMPTAX().getISUBJTOFIT());
        assertEquals(pReqEmp.getIEMPTAX().getISUBJTOFUTA(), pRespEmp.getIEMPTAX().getISUBJTOFUTA());
        assertEquals(pReqEmp.getIEMPTAX().getISUBJTOMCARE(), pRespEmp.getIEMPTAX().getISUBJTOMCARE());
        assertEquals(pReqEmp.getIEMPTAX().getISUBJTOSS(), pRespEmp.getIEMPTAX().getISUBJTOSS());

        assertEquals(pReqEmp.getIEMPDD().getIUSEDD(), pRespEmp.getIEMPDD().getIUSEDD());
        assertEquals(pReqEmp.getIEMPDD().getIDDACCT().size(), pRespEmp.getIEMPDD().getIDDACCT().size());
        // order matters, so the accounts should be in the same order
        for (int i = 0; i < pReqEmp.getIEMPDD().getIDDACCT().size(); i++) {
            IDDACCT reqDdacct = pReqEmp.getIEMPDD().getIDDACCT().get(i);
            com.intuit.sbd.payroll.psp.common.ofx.response.IDDACCT respDdacct = pRespEmp.getIEMPDD().getIDDACCT().get(i);
            assertEquals(reqDdacct.getBANKACCTTO().getACCTID(), respDdacct.getBANKACCTTO().getACCTID());
            assertEquals(reqDdacct.getBANKACCTTO().getACCTTYPE(), respDdacct.getBANKACCTTO().getACCTTYPE());
            assertEquals(reqDdacct.getBANKACCTTO().getBANKID(), respDdacct.getBANKACCTTO().getBANKID());
            assertEquals(reqDdacct.getIACCTNAME(), respDdacct.getIACCTNAME());
            assertEquals(QBOFX.mapOFXStringToDouble(reqDdacct.getIAMT()), QBOFX.mapOFXStringToDouble(respDdacct.getIAMT()));
        }
    }

    @Test
    public void testSameEmployeeAddedAfterPaycheckSent() {
        sameEmployeeAddedAfterPaycheckSent(true, false);
    }

    @Test
    public void testDifferentEmployeeAddedAfterPaycheckSent() {
        sameEmployeeAddedAfterPaycheckSent(false, false);
    }

    @Test
    public void testSameEmployeeAddedAfterPaycheckSent_QB2013() {
        sameEmployeeAddedAfterPaycheckSent(true, true);
    }

    private void sameEmployeeAddedAfterPaycheckSent(boolean pSameEmployee, boolean pIncludeUniqueId) {
        DataLoadServices.setPSPDate(2012, 10, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false, false);
        int count = 0;
        List<IEMP> newEmployeeList = new ArrayList<IEMP>();
        List<String> employeeIds = new LinkedList<String>();
        for (Iterator<IEMP> iterator = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().iterator(); iterator.hasNext(); ) {
            IEMP iemp = iterator.next();
            if(count < 2) {
                iemp.getIADDRINFO().setIFIRST("First");
                iemp.getIADDRINFO().setILAST("Last");
                if(pSameEmployee) {
                    iemp.setISSN("000000123");
                }
                if(pIncludeUniqueId) {
                    iemp.setIQBUNIQUEID("abc");
                }
                employeeIds.add(iemp.getIEMPID());
                if(count == 1) {
                    newEmployeeList.add(iemp);
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
            count++;
        }
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        List<Employee> employees = new ArrayList<Employee>();
        employees.add(Employee.findEmployee(company, employeeIds.get(0)));
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawMap = new HashMap<String, String>();
        lawMap.put("1", "10.00");
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 10, 5), employees, lawMap);
        PayrollServices.rollbackUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
        assertEquals("Paycheck date", SpcfCalendar.createInstance(2012, 10, 5, SpcfTimeZone.getLocalTimeZone()), payrollRun.getPaycheckDate().toLocal());
        assertEquals("Employee 1", employeeIds.get(0), assertOne(payrollRun.getPaycheckCollection()).getSourceEmployee().getSourceEmployeeId());
        PayrollServices.rollbackUnitOfWork();

        // change the employee to the second employee that is the same employee
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setEmployeeId(employeeIds.get(1));
        }
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(company, payrollRunDTO, false, true));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 newEmployeeList,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        if(pSameEmployee) {
            QBDTTestHelper.submitQBDTRequest(payrollOfx);

            // verify employee updated
            PayrollServices.beginUnitOfWork();
            payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
            assertEquals("Employee 2", employeeIds.get(1), assertOne(payrollRun.getPaycheckCollection()).getSourceEmployee().getSourceEmployeeId());
            if(pIncludeUniqueId) {
                assertEquals("Number of Employees", 1, Employee.findEmployees(company).size());
            } else {
                assertEquals("Number of Employees", 2, Employee.findEmployees(company).size());
            }
            PayrollServices.rollbackUnitOfWork();
        } else {
            QBDTTestHelper.submitQBDTRequest(payrollOfx, false);
        }
    }

    @Test
    public void testOFXLeadingSpace() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2013, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        // Put a special leading space in front of the first name.
        IEMP updatedEmp = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0);
        updatedEmp.getIADDRINFO().setIFIRST(QBOFX.LEADING_SPACE + "Mike");
        updatedEmp.getIADDRINFO().setIPRINTASNAME(QBOFX.LEADING_SPACE + "MikeMiddleLast");

        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        long syncToken = company.getCurrentToken()-1;
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, syncToken);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        // Find the employee we updated and verify the leading space was removed.
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IEMP> emps = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPMOD();
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IEMP emp : emps) {
            if (emp.getIEMPID().equals(updatedEmp.getIEMPID())) {
                assertEquals("Mike", emp.getIADDRINFO().getIFIRST());
            }
        }

        ofx = OFXRequestGenerator.generateEmployeeFullUpdates(ofx, company);

        // Update one of the mods.
        updatedEmp = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().get(0);
        updatedEmp.getIADDRINFO().setIFIRST(QBOFX.LEADING_SPACE + "Brad");
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        syncToken = company.getCurrentToken()-1;
        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, syncToken);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        emps = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPMOD();
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IEMP emp : emps) {
            if (emp.getIEMPID().equals(updatedEmp.getIEMPID())) {
                assertEquals("Brad", emp.getIADDRINFO().getIFIRST());
            }
        }

    }

    private void assertEmployeesW4Match(OFX pOfx, com.intuit.sbd.payroll.psp.common.ofx.response.OFX pSyncResponse) {
        List<IEMP> balanceFileEmployees = pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP();
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IEMP> syncResponseEmployees = pSyncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIEMPMOD();
        assertEquals("Employee mods", balanceFileEmployees.size(), syncResponseEmployees.size());

        for (IEMP balanceFileEmp : balanceFileEmployees) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IEMP syncResponseEmp : syncResponseEmployees) {
                if(balanceFileEmp.getIEMPID().equals(syncResponseEmp.getIEMPID())) {
                    assertEquals(balanceFileEmp.getIEMPTAX().getIEMPFIT().getIFEDFILESTATUS(), syncResponseEmp.getIEMPTAX().getIEMPFIT().getIFEDFILESTATUS());
                    assertEquals(balanceFileEmp.getIEMPTAX().getIEMPFIT().getICLAIMDEPENDENTS(),syncResponseEmp.getIEMPTAX().getIEMPFIT().getICLAIMDEPENDENTS());
                    assertEquals(balanceFileEmp.getIEMPTAX().getIEMPFIT().getIOTHERINCOME(),syncResponseEmp.getIEMPTAX().getIEMPFIT().getIOTHERINCOME());
                    assertEquals(balanceFileEmp.getIEMPTAX().getIEMPFIT().getIDEDUCTIONS(),syncResponseEmp.getIEMPTAX().getIEMPFIT().getIDEDUCTIONS());
                    assertEquals(balanceFileEmp.getIEMPTAX().getIEMPFIT().getIMULTIPLEJOBS(),syncResponseEmp.getIEMPTAX().getIEMPFIT().getIMULTIPLEJOBS());
                    assertEquals(balanceFileEmp.getIEMPTAX().getIEMPFIT().getIFEDW4EMPLOYEEPREF(),syncResponseEmp.getIEMPTAX().getIEMPFIT().getIFEDW4EMPLOYEEPREF());
                    break;
                }
            }
        }
    }

    @Test
    public void testEmployeeW42020_SyncTest() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.getIEMPTAX().getIEMPFIT().setIFEDFILESTATUS("Single or Married filing separately");
            iemp.getIEMPTAX().getIEMPFIT().setICLAIMDEPENDENTS("$500.00");
            iemp.getIEMPTAX().getIEMPFIT().setIOTHERINCOME("$224.55");
            iemp.getIEMPTAX().getIEMPFIT().setIDEDUCTIONS("$98.25");
            iemp.getIEMPTAX().getIEMPFIT().setIMULTIPLEJOBS("Y");
            iemp.getIEMPTAX().getIEMPFIT().setIFEDW4EMPLOYEEPREF("2020ANDLATER");
        }
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        assertEmployeesW4Match(ofx, syncResponse);
    }

    @Test
    public void testEmployeeW42019_SyncTest() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            iemp.getIEMPTAX().getIEMPFIT().setIFEDFILESTATUS("Married Using Single Rate");
            iemp.getIEMPTAX().getIEMPFIT().setICLAIMDEPENDENTS("$0.00");
            iemp.getIEMPTAX().getIEMPFIT().setIOTHERINCOME("$0.00");
            iemp.getIEMPTAX().getIEMPFIT().setIDEDUCTIONS("$0.00");
            iemp.getIEMPTAX().getIEMPFIT().setIMULTIPLEJOBS("N");
            iemp.getIEMPTAX().getIEMPFIT().setIFEDW4EMPLOYEEPREF("2019ORBEFORE");
        }
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        assertEmployeesW4Match(ofx, syncResponse);
    }

    @Test
    public void testEmployeesAddedWithBirthDate() throws Exception {
        DataLoadServices.setPSPDate(2012, 2, 1);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company, true);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        DataLoadServices.runOffload(company, 2012, 7, 17);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyBankAccount companyBankAccount = company.getCompanyBankAccountCollection().get(0);
        DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
        assertSuccess(PayrollServices.companyManager.verifyCompanyBankAccount(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(),
                companyBankAccount.getSourceBankAccountId(),
                verificationTransactions.get(0).getFinancialTransactionAmount(),
                verificationTransactions.get(1).getFinancialTransactionAmount(), false));
        PayrollServices.commitUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        // pending first payroll
        OFX ofx1 = OFXRequestGenerator.generateEmployeeFullUpdates(ofx, DataLoadServices.refreshCompany(company));
        String sourceEmployeeId = ofx1.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().get(0).getIEMPID();
        ofx1.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().get(0).setIBIRTHDATE("20001111");
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(ofx1);
        PayrollServices.beginUnitOfWork();
        Employee employee = company.getEmployees()
                .stream().filter(emp -> emp.getSourceEmployeeId().equals(sourceEmployeeId))
                .collect(Collectors.toList()).get(0);
        PayrollServices.commitUnitOfWork();

        int year = employee.getBirthDate().getYear();
        int month = employee.getBirthDate().getMonth();
        int day = employee.getBirthDate().getDay();
        StringBuilder dob = new StringBuilder();
        dob.append(year).append(month).append(day);

        assertEquals(ofx1.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD().get(0).getIBIRTHDATE(), dob.toString());
    }

}
