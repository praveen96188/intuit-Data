package com.intuit.sbd.payroll.psp.adapters.mobile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.webservices.AuthenticationWS;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
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
public class AuthenticationTests {

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

    @Ignore
    @Test
    public void happyPath() {
        try {
            DataLoadServices.setEmployeeCount(3);
            DataLoadServices.setPayrollCount(3);
            DataLoadServices.setLoadAdditionalSavingsAccount(true);

            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

            DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);

            DomainEntitySet<FinancialTransaction> eeTxs = null;
            DomainEntitySet<FinancialTransaction> erTxs = null;
            DomainEntitySet<FinancialTransaction> nocTxs = null;

            for (PayrollRun payrollRun : payrollRuns) {
                if (!payrollRun.getPayrollRunType().equals(PayrollType.Regular)) {
                    continue;
                }

                if (eeTxs == null) {
                    eeTxs = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployeeDdCredit);
                    while (eeTxs.size() > 1) {
                        eeTxs.remove(1);
                    }
                } else
                if (erTxs == null) {
                    erTxs = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerDdDebit);
                } else
                if (nocTxs == null) {
                    nocTxs = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployeeDdCredit);
                    while (nocTxs.size() > 1) {
                        nocTxs.remove(1);
                    }                    
                }
            }
            PayrollServices.commitUnitOfWork();

            DataLoadServices.returnTxns(eeTxs);
            DataLoadServices.returnTxns(erTxs);
            DataLoadServices.returnTxns(nocTxs, "C01", "123456789");

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            String response = new AuthenticationWS().authenticateUser(company.getFedTaxId(), "test1234!");
            RSResponse rsResponse = gson.fromJson(response, RSResponse.class);

            assertNotNull(rsResponse);
            assertEquals(3, rsResponse.getRecentTransmissionCount());
            assertEquals(3, rsResponse.getRecentEventCount());

            assertNull(rsResponse.getCompany());
            assertTrue(rsResponse.getEvents().isEmpty());
            assertTrue(rsResponse.getPayees().isEmpty());
            assertTrue(rsResponse.getTransmissions().isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
