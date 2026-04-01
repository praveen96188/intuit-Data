package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * User: dweinberg
 * Date: Dec 21, 2009
 * Time: 4:24:20 PM
 */
public class UnlockCompanyPINCoreTests {

    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testUnlockPINAllowsSuccessfulTry() {
        CompanyDTO company1 = dataloader.getTestIntuitCompany();

        //get the company locked
        VerifyCompanyPINCoreTests verifyCompanyPINCoreTests = new VerifyCompanyPINCoreTests();
        verifyCompanyPINCoreTests.runBeforeEachTest();
        verifyCompanyPINCoreTests.VerifyCompanyPIN_AccountLocked();

        //and try another time just for good measure
        PayrollServices.beginUnitOfWork();
        PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111");
        PayrollServices.commitUnitOfWork();

        //unlock
        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.subscriptionManager.unlockPINOnce( company1.getSourceSystemCd(), company1.getCompanyId());
        assertTrue("Unlock successful", pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        //success
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "PIN123456");

        assertTrue(pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        assertEquals("Number Of Failed Login Attempts", 0, company.getNumberOfFailedLoginAttempts());
        assertEquals("Account Locked Until", null, company.getAccountLockedUntil());
    }

    @Test
    public void testUnlockPINNextFailureLocksOut() {
        CompanyDTO company1 = dataloader.getTestIntuitCompany();

        //get the company locked
        VerifyCompanyPINCoreTests verifyCompanyPINCoreTests = new VerifyCompanyPINCoreTests();
        verifyCompanyPINCoreTests.runBeforeEachTest();
        verifyCompanyPINCoreTests.VerifyCompanyPIN_AccountLocked();

        //unlock
        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.subscriptionManager.unlockPINOnce( company1.getSourceSystemCd(), company1.getCompanyId());
        assertTrue("Unlock successful", pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        //success
        PayrollServices.beginUnitOfWork();
        pr = PayrollServices.subscriptionManager.verifyCompanyPIN(
                SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), "111");

        assertFalse(pr.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(company1.getCompanyId(), company1.getSourceSystemCd());

        SourcePayrollParameter sourceParameter =
                SourcePayrollParameter.findSourcePayrollParameter(company.getSourceSystemCd(),
                        SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts);
        int maxNumberOfAttempts = Integer.parseInt(sourceParameter.getParameterValue());
        assertEquals("Number Of Failed Login Attempts", maxNumberOfAttempts, company.getNumberOfFailedLoginAttempts());
        assertNotNull(company.getAccountLockedUntil());
    }

    @Test
    public void testUnlockPINAllowsSuccessfulTryAssisted() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Tax);

        int maxTries = SourcePayrollParameter.findIntValue(company.getSourceSystemCd(), SourcePayrollParameterCode.MaxNumberOfFailedLoginAttempts);
        for (int i = 0; i < maxTries; i++) {
            PayrollServices.beginUnitOfWork();
            PayrollServices.subscriptionManager.verifyCompanyPIN(
                    company.getSourceSystemCd(),
                    company.getSourceCompanyId(), "111");
            PayrollServices.commitUnitOfWork();
        }

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertNotNull(company.getAccountLockedUntil());
        assertEquals(maxTries, company.getNumberOfFailedLoginAttempts());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.subscriptionManager.unlockPINOnce(company.getSourceSystemCd(), company.getSourceCompanyId()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertNull(company.getAccountLockedUntil());
        assertEquals(maxTries - 1, company.getNumberOfFailedLoginAttempts());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertSuccess(PayrollServices.subscriptionManager.verifyCompanyPIN(
                company.getSourceSystemCd(),
                company.getSourceCompanyId(), "test1234!"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertNull(company.getAccountLockedUntil());
        assertEquals(0, company.getNumberOfFailedLoginAttempts());
        PayrollServices.commitUnitOfWork();
    }

}
