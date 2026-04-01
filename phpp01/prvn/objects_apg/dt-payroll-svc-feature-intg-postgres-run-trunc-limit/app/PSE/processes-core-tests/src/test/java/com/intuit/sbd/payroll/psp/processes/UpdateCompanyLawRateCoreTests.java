package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * User: TimothyD698
 * Date: Mar 12, 2013
 */
public class UpdateCompanyLawRateCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(2013, 4, 10);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testInvalidParameters() {

        ProcessResult processResult;

        Company company = DataLoadServices.setupAssistedCompanyForCA("606001967", 1, true);
        Application.beginUnitOfWork();
        CompanyLaw caSuiLaw = CompanyLaw.findCompanyLaw(company, "87");
        Application.rollbackUnitOfWork();

        // Company not specified.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, null, caSuiLaw.getLaw(),
                                                                            PSPDate.getPSPTime(), 3.5, false);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "138", "Source Company ID is not specified.");

        // Law not specified.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", null,
                                                                            PSPDate.getPSPTime(), 3.5, false);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "10117", "Law is not specified.");

        // Quarter Start Date not specified.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967",
                                                                            caSuiLaw.getLaw(), null, 3.5, false);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: quarterStartDate");

        // Quarter Start Date is not at the beginning of the quarter.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", caSuiLaw.getLaw(),
                                                                            SpcfCalendar.createInstance(2013,5,1), 3.5, false);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: quarterStartDate");

        // Rate not specified.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", caSuiLaw.getLaw(),
                                                                            SpcfCalendar.createInstance(2013,4,1,7,0,0,0), null, false);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: rate");

        // Unable to find Company Law
        Application.beginUnitOfWork();
        Law ncSuiLaw = Application.find(Law.class, Law.LawId().equalTo("110")).getFirst();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", ncSuiLaw,
                                                                            SpcfCalendar.createInstance(2013,4,1,7,0,0,0),
                                                                            3.5, false);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "1502",
                                     "Company Law 110 for company QBDT:606001967 does not exist.");

    }

    private static void verifyInvalidParameterResult(ProcessResult processResult, String messageCode, String message) {
        Message errorMessage;

        assertFalse(processResult.isSuccess());
        assertEquals("Messages size", 1, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", messageCode, errorMessage.getMessageCode());
        assertEquals("Error message", message, errorMessage.getMessage());
    }

    @Test
    public void testSuiRateUpdate() {

        ProcessResult processResult;
        SpcfCalendar quarterStart = CalendarUtils.getFirstDayOfQuarter(2013,2);

        Company company = DataLoadServices.setupAssistedCompanyForCA("606001967", 1, true);

        // Create some old rates that should get invalidated as part of the process.
        Application.beginUnitOfWork();
        CompanyLaw caSuiLaw = CompanyLaw.findCompanyLaw(company, "87");
        addCompanyLawRate(caSuiLaw, CalendarUtils.getFirstDayOfQuarter(2012, 1), 0.015);
        addCompanyLawRate(caSuiLaw, CalendarUtils.getFirstDayOfQuarter(2012, 2), 0.016);
        addCompanyLawRate(caSuiLaw, CalendarUtils.getFirstDayOfQuarter(2012, 3), 0.017);
        addCompanyLawRate(caSuiLaw, CalendarUtils.getFirstDayOfQuarter(2012, 4), 0.018);
        Application.commitUnitOfWork();

        // Create an unexpired Company Law rate for the previous quarter.
        Application.beginUnitOfWork();
        PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", caSuiLaw.getLaw(),
                                                                            CalendarUtils.getFirstDayOfQuarter(2013,1), 0.005, false);
        // Verify events.
        CompanyEvent event = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyLawUpdated));
        CompanyEventDetail detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.Details));
        Assert.assertEquals("Detail Message", "The company's rate for CAEDD/SUI (2013 Q1) has changed from 1.8% to 0.5%.",
                            detail.getValue());
        detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.CompanyAgency));
        Assert.assertEquals("CompanyAgency", "CAEDD", detail.getValue());
        detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.Law));
        Assert.assertEquals("LawId", "87", detail.getValue());
        detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.OldStringValue));
        Assert.assertEquals("Old Value", "1.8%", detail.getValue());
        Application.commitUnitOfWork();

        // Update rate with no push.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", caSuiLaw.getLaw(),
                                                                            quarterStart, 0.0375, false);
        Assert.assertTrue(processResult.isSuccess());
        CompanyLawRate updatedRate = (CompanyLawRate)processResult.getResult();
        Assert.assertEquals("Rate Token", -1, updatedRate.getCompanyLaw().getQbdtPayrollItemInfo().getRatePushToken());
        Assert.assertEquals("Rate", 0.0375, updatedRate.getRate());
        //Since the default value of rate type is percentage we should verify that percentage type is saved
        Assert.assertEquals("Rate Type", QbdtNumericType.Percentage, updatedRate.getRateType());
        Application.commitUnitOfWork();

        // Add a future rate which should get marked invalid when we update the current quarter.
        Application.beginUnitOfWork();
        addCompanyLawRate(caSuiLaw, CalendarUtils.getFirstDayOfQuarter(2013, 3), 2.5);
        Application.commitUnitOfWork();

        // Update rate with push.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", caSuiLaw.getLaw(),
                                                                            quarterStart, 0.041, true);
        Assert.assertTrue(processResult.isSuccess());
        updatedRate = (CompanyLawRate)processResult.getResult();
        Application.refresh(company);
        Assert.assertTrue("Rate Token", updatedRate.getCompanyLaw().getQbdtPayrollItemInfo().getRatePushToken() > 0);
        Assert.assertEquals("Rate Token and Token are same", updatedRate.getCompanyLaw().getQbdtPayrollItemInfo().getRatePushToken(),
                                                             updatedRate.getCompanyLaw().getQbdtPayrollItemInfo().getToken());
        Assert.assertEquals("Rate", 0.041, updatedRate.getRate());
        //Since the default value of rate type is percentage we should verify that percentage type is saved
        Assert.assertEquals("Rate Type", QbdtNumericType.Percentage, updatedRate.getRateType());
        Application.commitUnitOfWork();

        // Verify remaining valid CompanyLawRate entries.
        Application.beginUnitOfWork();
        DomainEntitySet<CompanyLawRate> remainingRates = Application.find(CompanyLawRate.class, new Query<CompanyLawRate>()
                        .Where(CompanyLawRate.CompanyLaw().equalTo(caSuiLaw)
                            .And(CompanyLawRate.InvalidDate().isNull()))
                        .OrderBy(CompanyLawRate.EffectiveDate()));
        Assert.assertEquals("Remaining Set Size", 4, remainingRates.size());
        Assert.assertEquals("Effective Date", SpcfCalendar.createInstance(2012, 7, 1, 7, 0, 0, 0), remainingRates.get(0).getEffectiveDate());
        Assert.assertEquals("Expiration Date", SpcfCalendar.createInstance(2012, 9, 30, 7, 0, 0, 0), remainingRates.get(0).calculateExpirationDate());
        Assert.assertEquals("Rate", 0.018, remainingRates.get(0).getRate());
        Assert.assertEquals("Effective Date", SpcfCalendar.createInstance(2012, 10, 1, 7, 0, 0, 0), remainingRates.get(1).getEffectiveDate());
        Assert.assertEquals("Expiration Date", SpcfCalendar.createInstance(2012, 12, 31, 8, 0, 0, 0), remainingRates.get(1).calculateExpirationDate());
        Assert.assertEquals("Rate", 0.018, remainingRates.get(1).getRate());
        Assert.assertEquals("Effective Date", SpcfCalendar.createInstance(2013, 1, 1, 8, 0, 0, 0), remainingRates.get(2).getEffectiveDate());
        Assert.assertEquals("Expiration Date", SpcfCalendar.createInstance(2013, 3, 31, 7, 0, 0, 0), remainingRates.get(2).calculateExpirationDate());
        Assert.assertEquals("Rate", 0.0325, remainingRates.get(2).getRate());
        Assert.assertEquals("Effective Date", SpcfCalendar.createInstance(2013, 4, 1, 7, 0, 0, 0), remainingRates.get(3).getEffectiveDate());
        Assert.assertNull("Expiration Date", remainingRates.get(3).calculateExpirationDate());
        Assert.assertEquals("Rate", 0.041, remainingRates.get(3).getRate());
        Application.rollbackUnitOfWork();

    }

    @Test
    public void testSuiRateUpdate2() {

        ProcessResult processResult;
        SpcfCalendar quarterStart = CalendarUtils.getFirstDayOfQuarter(2013,2);

        Company company = DataLoadServices.setupAssistedCompanyForCA("606001967", 1, true);

        // Create some rates that we would try to invalid as part of the process.
        Application.beginUnitOfWork();
        CompanyLaw caSuiLaw = CompanyLaw.findCompanyLaw(company, "87");
        addCompanyLawRate(caSuiLaw, CalendarUtils.getFirstDayOfQuarter(2013, 1), 0.015);
        addCompanyLawRate(caSuiLaw, CalendarUtils.getFirstDayOfQuarter(2014, 1), 0.025); //Future calendar year
        Application.commitUnitOfWork();

        // Create an unexpired Company Law rate for the previous quarter.
        Application.beginUnitOfWork();
        PayrollServices.companyManager.updateCompanyLawRate(SourceSystemCode.QBDT, "606001967", caSuiLaw.getLaw(),
                                                            quarterStart, 0.031, false);
        // Verify events.
        CompanyEvent event = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.CompanyLawUpdated));
        CompanyEventDetail detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.Details));
        Assert.assertEquals("Detail Message", "The company's rate for CAEDD/SUI (2013 Q2) has changed from 1.5% to 3.1%.",
                            detail.getValue());
        detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.CompanyAgency));
        Assert.assertEquals("CompanyAgency", "CAEDD", detail.getValue());
        detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.Law));
        Assert.assertEquals("LawId", "87", detail.getValue());
        detail = assertOne(event.getCompanyEventDetailsFromDetailCollection(EventDetailTypeCode.OldStringValue));
        Assert.assertEquals("Old Value", "1.5%", detail.getValue());
        Application.commitUnitOfWork();

        // Verify that next year's law date is not invalidated
        Application.beginUnitOfWork();
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "87");
        CompanyLawRate rate = CompanyLawRate.findEffectiveLawRate(companyLaw, CalendarUtils.getFirstDayOfQuarter(2014, 1));
        Assert.assertEquals(rate.getInvalidDate(), null);
        Application.rollbackUnitOfWork();

    }

    private void addCompanyLawRate(CompanyLaw coLaw, SpcfCalendar effectiveDate, Double rate) {
        CompanyLawRate coLawRate = new CompanyLawRate();
        coLawRate.setCompanyLaw(coLaw);
        coLawRate.setEffectiveDate(effectiveDate);
        coLawRate.setRate(rate);
        Application.save(coLawRate);
    }
}
