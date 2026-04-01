package com.intuit.sbd.payroll.psp.adapters.mobile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSPayee;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSPayeeType;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.webservices.PayeesWS;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.PayrollSubmitTest;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBEmployee;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPaycheck;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPaychecks;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.SubmitPayrollRequest;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.test.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices.QBPayrollWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import org.junit.*;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class PayeesTests {

    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

   @Before
    public void startUp() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();

        QBDTTestHelper.typicalRunBeforeEachTest();
    }

    @After
    public void shutdown() {
        DataLoadServices.setEmployeeCount(1);
        DataLoadServices.setPayrollCount(1);
        DataLoadServices.setLoadAdditionalSavingsAccount(false);

        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void getPayee401k() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        Application.commitUnitOfWork();

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.ThirdParty401k);

        PayrollServices.beginUnitOfWork();

        SubmitPayrollRequest request = new SubmitPayrollRequest();
        QBDTWSRequestCreator.initRequest(request, company);

        request.setUpdateCompanyRequest(QBDTWSRequestCreator.createUpdateCompanyRequest(company));
        request.setSubmitEmployeesRequest(QBDTWSRequestCreator.createSubmitEmployeesRequest(company));

        QBDTWSPayrollItemRepository payrollItemRepository = new QBDTWSPayrollItemRepository(true);
        WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
        QBPaychecks qbPaychecks = new QBPaychecks();

        for (QBEmployee qbEmployee : request.getSubmitEmployeesRequest().getEmployees().getEmployee()) {
            QBPaycheck paycheck = paycheckGenerator.newPaycheck(qbEmployee, "01/12/2011", 102)
                    .addEarningLine(12, 120.50, 10200.48)
                    .add401kEmployeeDeferralLine(-12, -100)
                    .getPaycheck();

            qbPaychecks.getPaycheck().add(paycheck);
        }

        request.setPaycheckList(qbPaychecks);
        request.setPayrollItemList(payrollItemRepository.getAllPayrollItems());

        Application.commitUnitOfWork();

        WS_Assert.assertSuccess("SubmitPayroll failed", new QBPayrollWebServices().SubmitPayroll(request));

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany("123456789", SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        PayeesWS payeesWS = new PayeesWS();
        String jsonResponse = payeesWS.getPayees(company.getFedTaxId(), DataLoadServices.PIN);

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        char lastKey = '"';
        assertEquals(27, rsResponse.getPayees().size());
        for (String key : rsResponse.getPayees().keySet()) {
            List<RSPayee> payeeList = rsResponse.getPayees().get(key);

            assertTrue(lastKey < key.charAt(0));
            lastKey = key.charAt(0);

            if ("L".equals(key)) {
                assertEquals(2, payeeList.size());

                for (RSPayee rsPayee : payeeList) {
                    assertNotNull(rsPayee.getId());
                    assertNotNull(rsPayee.getFirstName());
                    assertNotNull(rsPayee.getMiddleName());
                    assertNotNull(rsPayee.getLastName());
                    assertEquals(RSPayeeType.Employee, rsPayee.getType());

                    assertTrue(rsPayee.getBankAccounts().isEmpty());
                    assertNull(rsPayee.getBirthDate());
                    assertNull(rsPayee.getEmail());
                    assertNull(rsPayee.getGender());
                    assertNull(rsPayee.getHireDate());
                    assertNull(rsPayee.getIs1099());
                    assertNull(rsPayee.getMailingAddress());
                    assertNull(rsPayee.getPhone());
                    assertNull(rsPayee.getSick());
                    assertNull(rsPayee.getStatus());
                    assertNull(rsPayee.getSuffix());
                    assertNull(rsPayee.getTaxId());
                    assertNull(rsPayee.getVacation());
                    assertTrue(rsPayee.getStateWithholdings().isEmpty());
                }
            } else {
                assertEquals(0, payeeList.size());
            }
        }

        assertNull(rsResponse.getCompany());
        assertNull(rsResponse.getRecentTransmissionCount());
        assertNull(rsResponse.getRecentEventCount());
        assertTrue(rsResponse.getPaychecks().isEmpty());
        assertTrue(rsResponse.getTransmissions().isEmpty());
        assertTrue(rsResponse.getEvents().isEmpty());
    }

    @Test
    public void getPayeeDD() {
        PayrollSubmitTest payrollRunDTO = new PayrollSubmitTest();
        payrollRunDTO.testPayrollHappyPath();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(CompanyQB1DataLoader.COMPANY_PSID, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        PayeesWS payeesWS = new PayeesWS();
        String jsonResponse = payeesWS.getPayees(company.getFedTaxId(), "test1234");

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        char lastKey = '"';
        assertEquals(27, rsResponse.getPayees().size());
        for (String key : rsResponse.getPayees().keySet()) {
            List<RSPayee> payeeList = rsResponse.getPayees().get(key);

            assertTrue(lastKey < key.charAt(0));
            lastKey = key.charAt(0);

            if ("L".equals(key) || "M".equals(key)) {
                assertEquals(1, payeeList.size());

                for (RSPayee rsPayee : payeeList) {
                    assertNotNull(rsPayee.getId());
                    assertNotNull(rsPayee.getFirstName());
                    assertNull(rsPayee.getMiddleName());
                    assertNotNull(rsPayee.getLastName());
                    assertEquals(RSPayeeType.Employee, rsPayee.getType());

                    assertTrue(rsPayee.getBankAccounts().isEmpty());
                    assertNull(rsPayee.getBirthDate());
                    assertNull(rsPayee.getEmail());
                    assertNull(rsPayee.getGender());
                    assertNull(rsPayee.getHireDate());
                    assertNull(rsPayee.getIs1099());
                    assertNull(rsPayee.getMailingAddress());
                    assertNull(rsPayee.getPhone());
                    assertNull(rsPayee.getSick());
                    assertNull(rsPayee.getStatus());
                    assertNull(rsPayee.getSuffix());
                    assertNull(rsPayee.getTaxId());
                    assertNull(rsPayee.getVacation());
                    assertTrue(rsPayee.getStateWithholdings().isEmpty());
                }
            } else if ("T".equals(key)) {
                assertEquals(2, payeeList.size());

                for (RSPayee rsPayee : payeeList) {
                    assertNotNull(rsPayee.getId());
                    assertNotNull(rsPayee.getFirstName());
                    assertNotNull(rsPayee.getMiddleName());
                    assertNotNull(rsPayee.getLastName());
                    assertEquals(RSPayeeType.Employee, rsPayee.getType());

                    assertTrue(rsPayee.getBankAccounts().isEmpty());
                    assertNull(rsPayee.getBirthDate());
                    assertNull(rsPayee.getEmail());
                    assertNull(rsPayee.getGender());
                    assertNull(rsPayee.getHireDate());
                    assertNull(rsPayee.getIs1099());
                    assertNull(rsPayee.getMailingAddress());
                    assertNull(rsPayee.getPhone());
                    assertNull(rsPayee.getSick());
                    assertNull(rsPayee.getStatus());
                    assertNull(rsPayee.getSuffix());
                    assertNull(rsPayee.getTaxId());
                    assertNull(rsPayee.getVacation());
                    assertTrue(rsPayee.getStateWithholdings().isEmpty());
                }
            } else {
                assertEquals(0, payeeList.size());
            }
        }

        assertNull(rsResponse.getCompany());
        assertNull(rsResponse.getRecentTransmissionCount());
        assertNull(rsResponse.getRecentEventCount());
        assertTrue(rsResponse.getPaychecks().isEmpty());
        assertTrue(rsResponse.getTransmissions().isEmpty());
        assertTrue(rsResponse.getEvents().isEmpty());
    }

    @Test
	@Ignore
	// Ignoring this test as it failed in the build machine and mobile-adapter is not getting deployed
    public void getPayeeDD4V() {
        BillPaymentWebServicesTests billPaymentWebServicesTests = new BillPaymentWebServicesTests();
        billPaymentWebServicesTests.testBillPaymentVoid_HappyPath();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        PayeesWS payeesWS = new PayeesWS();
        String jsonResponse = payeesWS.getPayees(company.getFedTaxId(), "1234567a");

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);
    }

    @Test
    public void getPayeesAssisted() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayeesWS payeesWS = new PayeesWS();
        String jsonResponse = payeesWS.getPayees(company.getFedTaxId(), DataLoadServices.PIN);

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSResponse rsResponse = gson.fromJson(jsonResponse, RSResponse.class);

        assertNotNull(rsResponse);

        char lastKey = '"';
        assertEquals(27, rsResponse.getPayees().size());
        for (String key : rsResponse.getPayees().keySet()) {
            List<RSPayee> payeeList = rsResponse.getPayees().get(key);

            assertTrue(lastKey < key.charAt(0));
            lastKey = key.charAt(0);

            if ("L".equals(key)) {
                assertEquals(5, payeeList.size());

                for (RSPayee rsPayee : payeeList) {
                    assertNotNull(rsPayee.getId());
                    assertNotNull(rsPayee.getFirstName());
                    assertNotNull(rsPayee.getMiddleName());
                    assertNotNull(rsPayee.getLastName());
                    assertEquals(RSPayeeType.Employee, rsPayee.getType());

                    assertTrue(rsPayee.getBankAccounts().isEmpty());
                    assertNull(rsPayee.getBirthDate());
                    assertNull(rsPayee.getEmail());
                    assertNull(rsPayee.getGender());
                    assertNull(rsPayee.getHireDate());
                    assertNull(rsPayee.getIs1099());
                    assertNull(rsPayee.getMailingAddress());
                    assertNull(rsPayee.getPhone());
                    assertNull(rsPayee.getSick());
                    assertNull(rsPayee.getStatus());
                    assertNull(rsPayee.getSuffix());
                    assertNull(rsPayee.getTaxId());
                    assertNull(rsPayee.getVacation());
                    assertTrue(rsPayee.getStateWithholdings().isEmpty());
                }
            } else {
                assertEquals(0, payeeList.size());
            }
        }

        assertNull(rsResponse.getCompany());
        assertNull(rsResponse.getRecentTransmissionCount());
        assertNull(rsResponse.getRecentEventCount());
        assertTrue(rsResponse.getPaychecks().isEmpty());
        assertTrue(rsResponse.getTransmissions().isEmpty());
        assertTrue(rsResponse.getEvents().isEmpty());
    }

}
