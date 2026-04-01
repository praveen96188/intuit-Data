package com.intuit.sbd.payroll.psp.adapters.mobile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.mobile.webservices.PayeeWS;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import org.junit.*;

import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * @author Jeff Jones
 */
public class PayeeTests {

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
    }

    @After
    public void shutdown() {
        DataLoadServices.setEmployeeCount(1);
        DataLoadServices.setPayrollCount(1);
        DataLoadServices.setLoadAdditionalSavingsAccount(false);
    }

    @Test
    public void getPayee401k() {

    }

    @Test
    public void getPayeeDD() {

    }

    @Test
    public void getPayeeDD4V() {

    }

    @Test
    public void getPayeeAssisted() {
        DataLoadServices.reinitialize();
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFXRequestGenerator.reset();
        OFX balanceFileOFX = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(balanceFileOFX);
        QBDTTestHelper.assertNoErrorRequests(company);

        DomainEntitySet<Employee> employees = Employee.findEmployees(company).sort(Employee.SourceEmployeeId());

        PayeeWS payeeWS = new PayeeWS();
        String jsonResponse = payeeWS.getPayee(company.getFedTaxId(), DataLoadServices.PIN, employees.get(0).getId().toString());

        assertNotNull(jsonResponse);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        RSPayee rsPayee = gson.fromJson(jsonResponse, RSPayee.class);

        assertNotNull(rsPayee);

        assertNotNull(rsPayee.getId());
        assertEquals("First1", rsPayee.getFirstName());
        assertEquals("M", rsPayee.getMiddleName());
        assertEquals("Last1", rsPayee.getLastName());
        assertEquals(RSPayeeType.Employee, rsPayee.getType());
        assertEquals("this@that.com", rsPayee.getEmail());
        assertEquals(RSGenderCode.Male, rsPayee.getGender());
        assertNotNull(rsPayee.getHireDate());
        assertEquals("201-123-1234", rsPayee.getPhone());
        assertEquals("0.0", rsPayee.getSick());
        assertEquals("0.0", rsPayee.getVacation());

        assertNull(rsPayee.getStatus());
        assertNull(rsPayee.getSuffix());
        assertNull(rsPayee.getTaxId());
        assertNull(rsPayee.getIs1099());
        assertNull(rsPayee.getMailingAddress());
        assertNull(rsPayee.getBirthDate());

        assertEquals("2", rsPayee.getFederalAllowances());
        assertEquals("10.10", rsPayee.getFederalAdditionalWithholding());
        assertEquals("Single", rsPayee.getFederalFilingStatus());

        assertFalse(rsPayee.getStateWithholdings().isEmpty());
        assertEquals(1, rsPayee.getStateWithholdings().size());

        RSStateWithholding rsStateWithholding = rsPayee.getStateWithholdings().get(0);
        assertEquals("SIT", rsStateWithholding.getType());
        assertEquals("CA", rsStateWithholding.getState());
        assertEquals("1", rsStateWithholding.getAllowances());
        assertEquals("5.05", rsStateWithholding.getAdditionalWithHolding());
        assertEquals("Married", rsStateWithholding.getFilingStatus());

        assertFalse(rsPayee.getBankAccounts().isEmpty());
        assertEquals(2, rsPayee.getBankAccounts().size());

        RSBankAccount rsBankAccount = rsPayee.getBankAccounts().get(0);
        assertNotNull(rsBankAccount.getId());
        assertEquals("bank 0", rsBankAccount.getBankName());
        assertEquals("123456780", rsBankAccount.getAccountNumber());
        assertEquals("111000025", rsBankAccount.getRoutingNumber());
        assertEquals(RSBankAccountTypeCode.Checking, rsBankAccount.getType());
        assertEquals(RSBankAccountStatusCode.Active, rsBankAccount.getStatus());

        rsBankAccount = rsPayee.getBankAccounts().get(1);
        assertNotNull(rsBankAccount.getId());
        assertEquals("bank 1", rsBankAccount.getBankName());
        assertEquals("123456781", rsBankAccount.getAccountNumber());
        assertEquals("111000025", rsBankAccount.getRoutingNumber());
        assertEquals(RSBankAccountTypeCode.Savings, rsBankAccount.getType());
        assertEquals(RSBankAccountStatusCode.Active, rsBankAccount.getStatus());
    }
}
