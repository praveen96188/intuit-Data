package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

/**
 * Unit Tests for CompanyEventEmail
 */

public class CompanyEventEmailTests {

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
    public void isEventMtlCompliantAllTrueTest() {
        PayrollServices.beginUnitOfWork();
        CompanyEventEmail dummyCompanyEventEmail = new CompanyEventEmail();
        dummyCompanyEventEmail.setStatusCd(EventEmailStatus.Sent);
        SpcfCalendar effectiveDate = SpcfCalendar.parse("MM/dd/yyyy", "12/07/2020");
        dummyCompanyEventEmail.setStatusEffectiveDate(effectiveDate);
        dummyCompanyEventEmail.setEmailTemplateTypeCd(EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL);
        Assert.assertTrue(CompanyEventEmail.isEventMtlCompliant(dummyCompanyEventEmail));
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void isEventMtlCompliantBeforeDate() {
        PayrollServices.beginUnitOfWork();
        CompanyEventEmail dummyCompanyEventEmail = new CompanyEventEmail();
        dummyCompanyEventEmail.setStatusCd(EventEmailStatus.Sent);
        SpcfCalendar effectiveDate = SpcfCalendar.parse("MM/dd/yyyy", "01/01/2020");
        effectiveDate = SpcfCalendar.createInstance(effectiveDate.getYear(), effectiveDate.getMonth(), effectiveDate.getDay(), SpcfTimeZone.getLocalTimeZone());
        dummyCompanyEventEmail.setStatusEffectiveDate(effectiveDate);
        dummyCompanyEventEmail.setEmailTemplateTypeCd(EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL);
        Assert.assertFalse(CompanyEventEmail.isEventMtlCompliant(dummyCompanyEventEmail));
        PayrollServices.commitUnitOfWork();
    }
}
