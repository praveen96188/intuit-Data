package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 18, 2010
 * Time: 9:42:05 PM
 */
public class AssistedUnprocessedRequestTests {

    @BeforeClass
    public static void beforeClass() {
        SocketManagerFactory.setInstanceClass(MockSocketManager.class);
    }

    @AfterClass
    public static void afterClass() {
        SocketManagerFactory.setInstanceClass(null);
    }

    @Test
    public void runPayments() {
        BatchJobManager.runJob(BatchJobType.EftpsPayment);
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }    

    @Test
    public void testCanceledAssistedCompany() {
        String psid = "606104305"; 
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        company = DataLoadServices.refreshCompany(company);

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken());
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(ofx);

        assertEquals(QBOFX.TAX_MODES.TERMINATED, response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getITAXSERVSTATUS().getITAXSERVMODE());
        assertEquals(QBOFX.DD_MODES.TERMINATED, response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIDDSTATUS().getIDDMODE());
    }

    @Test
    public void testQBDTRequestProcessingFlag_incrementsNextIds() {
        OFXRequestGenerator.reset();
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.updateProcessRequestFlag(company, false);

        company = DataLoadServices.refreshCompany(company);
        long token = company.getCurrentToken();
        int empNextId = Integer.parseInt(company.getNextEmployeeId());
        int pitemNextId = Integer.parseInt(company.getNextPayrollItemId());
        int payrollTxNextId = Integer.parseInt(company.getNextPayrollItemId());
        int paycheckNextId = Integer.parseInt(company.getNextPaycheckId());

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true, true, false, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        assertEquals("Token incremented by 1", token + 1, company.getCurrentToken());
        assertEquals("Emp Next Id", empNextId + 5, Integer.parseInt(company.getNextEmployeeId()));
        assertEquals("Pitem Next Id", pitemNextId + 26, Integer.parseInt(company.getNextPayrollItemId()));
        assertEquals("Payroll tx Next Id", payrollTxNextId + 5, Integer.parseInt(company.getNextPayrollTransactionId()));
        assertEquals("Paycheck Next Id", paycheckNextId + 25, Integer.parseInt(company.getNextPaycheckId()));
    }

    @Test
    public void testQBDTRequestProcessingFlag_closeToOffload() {
        DataLoadServices.setPSPDate(2012, 8, 8);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Tax);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        DataLoadServices.updateProcessRequestFlag(company, false);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        assertFalse(company.getQuickbooksInfo().getProcessTransmissions());

        List<Employee> employees = new ArrayList<Employee>(Application.find(Employee.class, Employee.Company().equalTo(company)));
        PayrollRunDTO payrollRunDTO = DataLoadServices.createPayrollRunWith941AndPAStateTaxes(new PayrollRunDTO(), company, new DateDTO(2012, 8, 20), employees);
        PayrollServices.rollbackUnitOfWork();

        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        PayrollServices.beginUnitOfWork();
        assertEquals(1, Application.find(QbdtUnprocessedRequest.class, QbdtUnprocessedRequest.Status().equalTo(QbdtRequestStatus.Error)).size());
        PayrollServices.rollbackUnitOfWork();

        // set psp date after 4:15pm
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 8, 8, 16, 11, 0, 0, SpcfTimeZone.getLocalTimeZone()));

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyDTO companyDTO = PayrollServices.dtoFactory.create(company);
        companyDTO.getQuickBooksInfo().setProcessTransmissions(true);
        try {
            PayrollServices.companyManager.updateQBCompanyInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), companyDTO);
            fail("Expected runtime exception");
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Cannot update processing flag anytime after 1hr before the company's offload time (i.e. 4:15pm if the offload is 5:15pm).");
        }
        PayrollServices.commitUnitOfWork();
    }

    public static void main(String[] argc) {
        try {
            String OFX = readFile("C:\\Users\\yifengs302\\Desktop\\badofx.txt");
            QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // used to manually test production OFX requests
    @Test
    @Ignore
    public void testAssistedReadFile() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.Cloud);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addCompanyBankAccount(company);
        }

        String OFX = readFile("C:\\Users\\yifengs302\\Desktop\\assisted.xml");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        /*
        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        OFX = readFile("C:\\Users\\znorcross\\Desktop\\Temp\\request3.txt");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        */

        /*OFX = readFile("C:\\Users\\znorcross\\Desktop\\request6.txt");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true, true);*/

        /*DataLoadServices.setPSPDate(2012, 8, 9);
        OFX = readFile("C:\\Users\\znorcross\\Desktop\\request4.txt");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true, true);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2012, 8, 18, SpcfTimeZone.getLocalTimeZone()));
        OFX = readFile("C:\\Users\\znorcross\\Desktop\\request5.txt");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true, true);*/

        /*OFX = readFile("C:\\Users\\znorcross\\Desktop\\request5.txt");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true, true);

        OFX = readFile("C:\\Users\\znorcross\\Desktop\\request6.txt");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true, true);

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2011, 12, 27, SpcfTimeZone.getLocalTimeZone()));
        OFX = readFile("C:\\Users\\znorcross\\Desktop\\request7.txt");
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true, true);*/


    }

    public static String readFile(String pFileName) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        FileReader fileReader = new FileReader(new File(pFileName));
        BufferedReader input =  new BufferedReader(fileReader);
        try {
            String line;
            while (( line = input.readLine()) != null){
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
            }
        } finally {
            input.close();
        }

        return stringBuilder.toString();
    }

    @Test
    public void testHPDE999061599OFX() throws Throwable {
        String companyPSID = "999061606";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 30, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        String OFX = readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061606.txt"));

        QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
        String response = qbdtRequestProcessor.processRequest(OFX, companyPSID,"Test IP");
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 18, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFX = readFile(Application.findFileOnClassPath("resources/ofx/PayrollFor999061606.txt"));

        qbdtRequestProcessor = new QBDTRequestProcessor();
        response = qbdtRequestProcessor.processRequest(OFX, companyPSID,"Test IP");
        QBDTTestHelper.assertNoErrorRequests(company);
        
//        TaxAdapter taxAdapter = new TaxAdapter();
//        SpcfCalendar firstDayOfQtr = CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime());
//        SpcfCalendar lastDayOfQtr = CalendarUtils.getLastDayOfQuarter(PSPDate.getPSPTime());
//        Date firstDayDt = CalendarUtils.convertToDate(firstDayOfQtr);
//        Date lastDayDt = CalendarUtils.convertToDate(lastDayOfQtr);
//
//
//        ArrayList<SAPLawTransactions> txns = taxAdapter.findTaxTransactions(SourceSystemCode.QBDT.toString(), companyPSID, null, Agency.IRS, "IRS-941-Payment", null, null,
//                firstDayDt, lastDayDt, null, null, null, null, true);
//
//        for (SAPLawTransactions currTxn : txns) {
//            double currSum = currTxn.getCurrentTaxesSum();
//            System.out.println("curr sum for law: "+currTxn.getLaw().getLawId()+" is: "+currSum);
//        }

    }

    @Test
    public void testStandAloneOFX() throws Throwable {
        String companyPSID = "999061349";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 30, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        String OFX = readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061606.txt"));

        QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
        String response = qbdtRequestProcessor.processRequest(OFX, companyPSID,"Test IP");
        QBDTTestHelper.assertNoErrorRequests(company);
    }

    @Test
    public void testHPDE652011971OFX() throws Throwable {
        String companyPSID = "999061349";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 30, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        String OFX = readFile(Application.findFileOnClassPath("resources/ofx/HPDE999061349.txt"));

        QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
        String response = qbdtRequestProcessor.processRequest(OFX, companyPSID,"Test IP");
        QBDTTestHelper.assertNoErrorRequests(company);
    }

    @Test
    public void test_PSRV002371() throws Throwable {
        String companyPSID = "999069798";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.claimNoFeesOffer(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 4, 20, 13, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String OFX = readFile(Application.findFileOnClassPath("resources/ofx/PSRV002371TestBALF.txt"));

        QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
        String response = qbdtRequestProcessor.processRequest(OFX, companyPSID,"Test IP");
        QBDTTestHelper.assertNoErrorRequests(company);

        OFX = readFile(Application.findFileOnClassPath("resources/ofx/PSRV002371Test.txt"));

        qbdtRequestProcessor = new QBDTRequestProcessor();
        response = qbdtRequestProcessor.processRequest(OFX, companyPSID,"Test IP");
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals("Number of payroll runs", 1, payrollRuns.size());
        assertEquals("PayrollRun Status", PayrollStatus.Complete, payrollRuns.get(0).getPayrollRunStatus());
        assertEquals("FTs created for payroll", 0, payrollRuns.get(0).getFinancialTransactionCollection().find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)).size());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void test_PSRV002387() throws Throwable {
        String companyPSID = "999062227";
        
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 4, 1, SpcfTimeZone.getLocalTimeZone()));
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, companyPSID, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 5, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        String OFX = readFile(Application.findFileOnClassPath("resources/ofx/HPDE_PSRV002387.txt"));

        QBDTRequestProcessor qbdtRequestProcessor = new QBDTRequestProcessor();
        String response = qbdtRequestProcessor.processRequest(OFX, companyPSID,"Test IP");
        QBDTTestHelper.assertNoErrorRequests(company);

    }    
}
