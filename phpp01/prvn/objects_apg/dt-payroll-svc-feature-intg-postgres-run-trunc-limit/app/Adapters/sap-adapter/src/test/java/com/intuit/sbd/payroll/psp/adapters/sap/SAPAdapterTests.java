/*
 * $Id: //psp/dev/Adapters/SAP/test/tests/com/intuit/sbd/payroll/psp/adapters/sap/SAPAdapterTests.java#5 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.FakeSalesTaxGateway;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.*;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.ObjectFactory;
import com.intuit.sbd.payroll.psp.common.ofx.request.SIGNONMSGSRQV1;
import com.intuit.sbd.payroll.psp.common.ofx.request.SONRQ;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.LoadFraudEvents;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.TransactionReverseCoreDataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfTimeZoneImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * JUnit tests for SAP Adapter classes.
 *
 * @author Joe Warmelink
 */
@SuppressWarnings({"deprecation"})
public class SAPAdapterTests {


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void adapterExceptionTest() {
        AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(PayrollServices.getLogger(SAPAdapterTests.class));
        try {
            PayrollServices.beginUnitOfWork();
            SAPCompany sapComp = new SAPCompany();
            sapComp.setLegalName("Fake Company");

            // try to generate exception by calling method on a company that cannot exist.
            ProcessResult<CompanyService> pr = PayrollServices.companyManager.deactivateService(SourceSystemCode.QBOE, "Invalid", ServiceCode.DirectDeposit);
            assertFalse(pr.isSuccess());

            PayrollServices.commitUnitOfWork();

            try {
                ArrayList<ProcessResult> prList = new ArrayList<ProcessResult>();
                prList.add(pr);
                aeFactory.throwGenericException("Error", prList);

                TestCase.fail("Company Update Exception was expected.");
            } catch (Exception ex) {
                // just make sure there was an exception the message could change
                assertTrue(true);
            }


        } catch (Exception ex) {
            TestCase.fail("Error in test case : " + ex.toString());
        }
    }

    private boolean safeEquals(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        }

        if (o2 == null)
            return false;

        return (o1.equals(o2));
    }

    private void validateContactDTOEqualsSAPContact(ContactDTO contact, SAPContact sapContact) {
        assertTrue(safeEquals(contact.getAccountSignatory(), sapContact.getAccountSignatory()));
        validateAddressDTOEqualsSAPAddress(contact.getAddress(), sapContact.getAddress());
        assertTrue(safeEquals(contact.getCommunicationTypeCd(), sapContact.getCommunicationTypeCd()));
        assertTrue(safeEquals(contact.getContactRoleCd(), sapContact.getContactRoleCd()));
        assertTrue(safeEquals(contact.getEmail(), sapContact.getEmail()));
        assertTrue(safeEquals(contact.getFirstName(), sapContact.getFirstName()));
        assertTrue(safeEquals(contact.getLastName(), sapContact.getLastName()));
        assertTrue(safeEquals(contact.getMiddleName(), sapContact.getMiddleName()));
        assertTrue(safeEquals(contact.getPhoneNumber(), SAPTranslator.sanitizePhoneNumber(sapContact.getPhoneNumber())));
    }

    private void validateAddressDTOEqualsSAPAddress(AddressDTO address, SAPAddress sapAddress) {
        assertTrue(safeEquals(address.getAddressLine1(), sapAddress.getAddressLine1()));
        assertTrue(safeEquals(address.getAddressLine2(), sapAddress.getAddressLine2()));
        assertTrue(safeEquals(address.getAddressLine3(), sapAddress.getAddressLine3()));
        assertTrue(safeEquals(address.getCity(), sapAddress.getCity()));
        assertTrue(safeEquals(address.getCountry(), sapAddress.getCountry()));
        assertTrue(safeEquals(address.getState(), sapAddress.getState()));
        assertTrue(safeEquals(address.getZipCode(), sapAddress.getZipCode()));
        assertTrue(safeEquals(address.getZipCodeExtension(), sapAddress.getZipCodeExtension()));
    }

    private void validateCompanyDTOEqualsSAPCompany(CompanyDTO company, SAPCompany sapCompany) {
        assertTrue(safeEquals(company.getFein(), sapCompany.getFein()));
        assertTrue(safeEquals(company.getLegalName(), sapCompany.getLegalName()));
        assertTrue(safeEquals(company.getDBA(), sapCompany.getDBA()));
        assertTrue(safeEquals(company.getPayrollFrequencyCd(), sapCompany.getPayrollFrequencyCd()));
        assertTrue(safeEquals(company.getFein(), sapCompany.getFein()));
        assertTrue(safeEquals(company.getNotificationEmail(), sapCompany.getNotificationEmail()));
        assertTrue(safeEquals(company.getCompanyId(), sapCompany.getCompanyId()));
        assertTrue(safeEquals(company.getSourceSystemCd().toString(), sapCompany.getSourceSystemCd().toString()));

    }

    @Test
    public void modifyDDInfoTest() {
        PayrollServicesTest.truncateTables();

        CompanyAdapter companyAdapter = new CompanyAdapter();
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            Company1Dataloader c1dl = new Company1Dataloader();
            c1dl.persistCompany1();
            PayrollServices.commitUnitOfWork();

            SAPCompany mockCompany = null;
            try {
                mockCompany = companyAdapter.findCompany("QBOE", "1234567");
            } catch (Throwable t) {
                fail(t.getMessage());
            }


            SAPCompanyDdLimits sapCompanyDdLimits = new SAPCompanyDdLimits();
            sapCompanyDdLimits.setPerPayrollLimit(50000.00);
            sapCompanyDdLimits.setPerEmployeeLimit(40000.00);
            /*newCompany.getDirectDepositService().setAveragePayRunAmount(100000.00);
            newCompany.getDirectDepositService().setHighAnnualPayAmount(250000.00);*/

            companyAdapter.saveCompanyService(mockCompany.getSourceSystemCd(), mockCompany.getCompanyId(), ServiceCode.DirectDeposit.toString(), null, sapCompanyDdLimits, null);

            PayrollServices.beginUnitOfWork();
            SourceSystemCode sourceSystemCd = SourceSystemCode.valueOf(mockCompany.getSourceSystemCd());

            DomainEntitySet<CompanyService> serviceList = CompanyService.findCompanyServicesBySourceCompanyId(
                    sourceSystemCd,
                    ServiceCode.DirectDeposit,
                    mockCompany.getCompanyId());

            DDCompanyServiceInfo service = (DDCompanyServiceInfo) serviceList.get(0);

            /*assertEquals(
                SAPTranslator.getDoubleFromSpcfMoney(service.getAveragePayRunAmount()),
                newCompany.getDirectDepositService().getAveragePayRunAmount(), 0.001);
            assertEquals(
                SAPTranslator.getDoubleFromSpcfMoney(service.getHighAnnualPayAmount()),
                newCompany.getDirectDepositService().getHighAnnualPayAmount(), 0.001);*/


            PayrollServices.rollbackUnitOfWork();

        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Error: " + t.toString());
        }
    }

    @Test
    public void translateCompanyStrikesTest() throws Throwable {
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();


        CompanyAdapter companyAdapter = new CompanyAdapter();

        // add a strike
        PayrollServices.beginUnitOfWork();
        ProcessResult addStrikeResult =
                PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE,
                                                              "1234567", "Add a strike, because it's fun!",
                                                              SpcfCalendar.createInstance(2007, 8, 29, SpcfTimeZone.getLocalTimeZone()));
        assertTrue(addStrikeResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        Company company = c1dl.getCompany();
        CompanyEvent cancelStrike = assertOne(company.getCurrentStrikeEvents());


        companyAdapter.cancelCompanyStrike(company.getSourceSystemCd().toString(),
                                           company.getSourceCompanyId(),
                                           cancelStrike.getId().toString());


        // verify the strike is now cancelled
        PayrollServices.beginUnitOfWork();
        Application.refresh(cancelStrike);
        assertEquals(CompanyEventStatus.Inactive, cancelStrike.getStatusCd());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPayollRunFinder() {
        PayrollServicesTest.truncateTables();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));

        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        Company1Dataloader.persistPayrollRun(c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO(PSPDate.getPSPTime())));
        PayrollServices.commitUnitOfWork();

        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
        ArrayList<String> payrollTypes = new ArrayList<String>();
        payrollTypes.add(PayrollType.Regular.toString());
        ArrayList<SAPPayrollRun> payrollRuns = null;
        try {
            payrollRuns = payrollRunAdapter.findPayrollRunsByDate("1234567", "QBOE", payrollTypes, null, null);
        } catch (Throwable t) {
            fail(t.getMessage());
        }

        assertEquals("Number of runs", payrollRuns.size(), 1);
        SAPPayrollRun sapPayrollRun = payrollRuns.get(0);
        assertNotNull("Payroll returned is null", sapPayrollRun);

        SAPPayrollRun payrollRun = null;
        try {
            payrollRun = payrollRunAdapter.findPayrollRun("QBOE", "1234567", sapPayrollRun.getSourcePayRunId());
        } catch (Throwable t) {
            fail(t.getMessage());
        }
        assertEquals("Payrolls unique id is not the same", payrollRun.getId(), sapPayrollRun.getId());
    }

    @Test
    public void testCompanyDDLimitHistory() {
        PayrollServicesTest.truncateTables();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));

        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        Company1Dataloader.persistPayrollRun(c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO(PSPDate.getPSPTime())));
        PayrollServices.commitUnitOfWork();

        PropertyAuditAdapter propertyAuditAdapter = new PropertyAuditAdapter();
        CompanyAdapter companyAdapter = new CompanyAdapter();
        try {
            ArrayList<SAPPropertyAudit> propertyAuditList = null;
            try {
                propertyAuditList = propertyAuditAdapter.getCompanyDDLimitHistory("1234567", "QBOE", null);
            } catch (Throwable t) {
                fail(t.getMessage());
            }

            assertTrue("Verify no property audit returned.", (propertyAuditList.size() == 0));

            SAPCompany company = companyAdapter.findCompany("QBOE", "1234567");

            SAPCompanyDdLimits sapCompanyDdLimits = new SAPCompanyDdLimits();
            sapCompanyDdLimits.setPerPayrollLimit(1000.00);

            companyAdapter.saveCompanyService(company.getSourceSystemCd(), company.getCompanyId(), ServiceCode.DirectDeposit.toString(), null, sapCompanyDdLimits, null);

            try {
                propertyAuditList = propertyAuditAdapter.getCompanyDDLimitHistory("1234567", "QBOE", null);
            } catch (Throwable t) {
                fail(t.getMessage());
            }
            assertTrue("Verify one property audit returned.", propertyAuditList.size() == 1);
            assertEquals("Verify audit is on OverrideCompanyLimitAmount",
                         propertyAuditList.get(0).getPropertyName(), "OverrideCompanyLimitAmount");

            int changeLimitAgainIndex = 0;
            if (propertyAuditList.get(0).getNewPropertyValue() == null)
                changeLimitAgainIndex = 1;

            assertEquals("Verify audit new value is correct",
                    Float.parseFloat(propertyAuditList.get(changeLimitAgainIndex).getNewPropertyValue()), Float.parseFloat("1000"),0 );
    

        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Error in testNotificationEmailHistory.  Details: " + t.toString());
        }
    }

    @Test
    public void testCompanyDDLimitViolationHistory() {
        PayrollServicesTest.truncateTables();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));

        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollRunDTO payrollRunDTO = c1dl.getCompany1PR_ExceedsLimits(new DateDTO("2007-09-07"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<PayrollRun> submitPayrollResult =
                PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBOE, "1234567", payrollRunDTO);
        PayrollServices.commitUnitOfWork();

        CompanyAdapter companyAdapter = new CompanyAdapter();
        ArrayList<SAPCompanyEvent> sapLimitViolationList = new ArrayList<SAPCompanyEvent>();

        try {
            sapLimitViolationList = companyAdapter.getLimitViolationEvents("1234567", "QBOE", null, null);
        } catch (Throwable t) {
            fail("Unexpected exception" + t.getMessage());
        }

        assertEquals("Violation Limit number", 2, sapLimitViolationList.size());

        int withEmployee = 0;
        for (SAPCompanyEvent event : sapLimitViolationList) {
            if (event.getEventDetail(EventDetailTypeCode.EmployeeId) != null) {
                assertEquals("Violation Amount", "300000.00", event.getEventDetail(EventDetailTypeCode.ViolationAmount).getValue());
                assertEquals("Limit Amount", "15000.00", event.getEventDetail(EventDetailTypeCode.LimitAmount).getValue());
                withEmployee++;
            }
        }
        assertEquals("One event with employee", 1, withEmployee);
    }

    @Ignore
    @Test
    public void testFailedAuthentication() {
        try {
            AuthAdapter authAdapter = new AuthAdapter();
            SAPUser sapUser = authAdapter.login("jwarmelink", "thisIsNotMyPassword", true);
            assertNull("SAPUser should be null", sapUser);
        } catch (Throwable ex) {
            ex.printStackTrace();
            TestCase.fail("Errir in testFailedAuthentication.  Details: " + ex.toString());
        }
    }

    @Test
    public void testPayrollRunAction() {
        try {
            PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
            ArrayList<SAPPayrollRunAction> actions = payrollRunAdapter.getAllPayrollRunActions();
            assertNotNull(actions);
        } catch (Throwable ex) {
            ex.printStackTrace();
            TestCase.fail("Error in testPayrollRunAction.  Details: " + ex.toString());
        }
    }

    @Test
    public void testDstErrorWhileConversionGMTToPST(){
        PayrollServicesTest.truncateTables();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Date date = CalendarUtils.convertToDate(PSPDate.getPSPTime());
        SpcfCalendar spcfCalendar = CalendarUtils.convertToSpcfCalendar(SAPTranslator.getGMTFormatDateWithDSTHandled(date));
        CalendarUtils.clearTime(spcfCalendar);

        assertEquals(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()),spcfCalendar);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(1977, 3, 23, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        date = CalendarUtils.convertToDate(PSPDate.getPSPTime());
        spcfCalendar = CalendarUtils.convertToSpcfCalendar(SAPTranslator.getGMTFormatDateWithDSTHandled(date));
        CalendarUtils.clearTime(spcfCalendar);

        assertEquals(SpcfCalendar.createInstance(1977, 3, 23, SpcfTimeZone.getLocalTimeZone()),spcfCalendar);

        //Check for null date
        assertNull(SAPTranslator.getGMTFormatDateWithDSTHandled(null));

    }

    @Test
    public void testChaseReport() throws Throwable {
        PayrollServicesTest.truncateTables();
        TransactionReverseCoreDataLoader.loadPayrollRunForTransactionReverseTest();

        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
        ArrayList<SAPChaseReport> chaseReports = null;
        chaseReports = payrollRunAdapter.findChaseReportForDateRange("QBOE", "123272727", new Date("8/31/2007"), new Date("8/31/2007"));

        assertEquals("Number of reports", 1, chaseReports.size());
        SAPChaseReport sapChaseReport = chaseReports.get(0);
        assertNotNull("Report returned is null", sapChaseReport);

        SAPChaseReportTransaction sapChaseReportTransaction = sapChaseReport.getTransactions().get(0);
        assertNotNull("Report returned is null", sapChaseReportTransaction);

    }

    @Test
    public void testChaseReportForBillPayment() throws Throwable {
        PayrollServicesTest.truncateTables();
        TransactionReverseCoreDataLoader.loadPayrollRunForBPTransactionReverseTest();

        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();
        ArrayList<SAPChaseReport> chaseReports = null;
        chaseReports = payrollRunAdapter.findChaseReportForDateRange("QBDT", "123272727", new Date("8/31/2007"), new Date("9/12/2007"));

        assertEquals("Number of reports", 1, chaseReports.size());
        SAPChaseReport sapChaseReport = chaseReports.get(0);
        assertNotNull("Report returned is null", sapChaseReport);

        assertNotNull("Report returned is null", sapChaseReport.getTransactions().get(0));

        for (SAPChaseReportTransaction sapChaseReportTransaction : sapChaseReport.getTransactions()) {
            // consider the reports from intuit to payee
            if (sapChaseReportTransaction.getDebitAccountName() == "Intuit") {
                assertTrue(sapChaseReportTransaction.getCreditAccountName().contains("Payee"));
                break;
            }

        }


    }

    @Test
    public void testFindCompanyFraudEvents() {
        try {
            LoadFraudEvents.loadFraudCompanyAndPayroll();

            CompanyAdapter companyAdapter = new CompanyAdapter();

            // find all fraud events
            ArrayList<SAPFraudEvent> events = companyAdapter.findCompanyFraudEvents(null, null, -1, null, null, null);
            assertEquals("Number of events", events.size(), 2);
            SAPFraudEvent event = events.get(0);
            assertNotNull("Event returned is null", event);

            // find all sign up fraud events
            events = companyAdapter.findCompanyFraudEvents(null, FraudEventCategory.SignUp.toString(), -1, null, null, null);
            assertEquals("Number of events", events.size(), 1);
            event = events.get(0);
            assertNotNull("Event returned is null", event);

            // find all payroll fraud events
            events = companyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), -1, null, null, null);
            assertEquals("Number of events", events.size(), 1);
            event = events.get(0);
            assertNotNull("Event returned is null", event);

            // test ein filter
            events = companyAdapter.findCompanyFraudEvents("123456789", FraudEventCategory.Payroll.toString(), -1, null, null, null);
            assertEquals("Number of events", events.size(), 1);
            event = events.get(0);
            assertNotNull("Event returned is null", event);

            // test date filter
            events = companyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), -1, new Date("09/03/2007"), new Date("09/03/2007"), null);
            assertEquals("Number of events", events.size(), 1);
            event = events.get(0);
            assertNotNull("Event returned is null", event);

            // test date filter with all types
            events = companyAdapter.findCompanyFraudEvents(null, null, -1, new Date("09/03/2007"), new Date("09/03/2007"), null);
            assertEquals("Number of events", events.size(), 2);
            event = events.get(0);
            assertNotNull("Event returned is null", event);

            // payroll amount filter
            events = companyAdapter.findCompanyFraudEvents(null, FraudEventCategory.Payroll.toString(), 180.00, null, null, null);
            assertEquals("Number of events", events.size(), 1);
            event = events.get(0);
            assertNotNull("Event returned is null", event);
        } catch (Throwable t) {
            t.printStackTrace();
            TestCase.fail("Error in testFindCompanyFraudEvents.  Details: " + t.toString());
        }
    }

    @Test
    public void testFindFeeDetail() throws Throwable {

        SAPPayrollDataLoader.loadQBDTCompanyRequests1TxnReversed();

        BillingAdapter billingAdapter = new BillingAdapter();

        PayrollServices.beginUnitOfWork();

        DomainEntitySet<FinancialTransaction> reversalFeeTransactions = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed)
                                                                                            .find(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney("75.00")));

        String transactionId = assertOne(reversalFeeTransactions).getId().toString();
        String psid = reversalFeeTransactions.get(0).getCompany().getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();

        SAPFeeDetail feeDetail = billingAdapter.findFeeDetail(transactionId,psid);
        assertEquals("Reversal Fee", feeDetail.getFeeName());
        assertEquals(75., feeDetail.getTotalPrice(),0);
        assertEquals(1., feeDetail.getUnits(), 0);
        assertEquals(0., feeDetail.getUnitPrice(), 0);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> ddFeeTransactions = FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536", TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Executed)
                                                                                      .find(FinancialTransaction.FinancialTransactionAmount().equalTo(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2)));
        transactionId = assertOne(ddFeeTransactions).getId().toString();
        String sourceCompanyId = ddFeeTransactions.get(0).getCompany().getSourceCompanyId();
        PayrollServices.rollbackUnitOfWork();

        feeDetail = billingAdapter.findFeeDetail(transactionId,sourceCompanyId);
        assertEquals("Direct Deposit Fee", feeDetail.getFeeName());
        assertEquals(SAPTranslator.getDoubleFromSpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16(2)), feeDetail.getTotalPrice(), 0);
        assertEquals(2., feeDetail.getUnits(),0);
        assertEquals(SAPTranslator.getDoubleFromSpcfMoney(ServiceChargePrices.getNormalPerPayrollServiceChargeFY16()), feeDetail.getUnitPrice(), 0);
    }

    /**
     * Simulate the scenario:
     * 1. agent opens and views a company while talking to customer
     * 2. customer submits a payroll against the company (updates/sync nextpayrollid, nexttransactionid, token)
     * 3. agent saves customer address info (should *not* update/overwrite nextpayrollid, nextransactionid, token values that were just synced back to QBDT)
     * 4. customer re-sync or submits a payroll
     */
    @Test
    public void testQBDTPayrollIDConcurrency() throws Throwable {
        PayrollServicesTest.truncateTables();

        // create the companyToCreate
        PayrollServices.beginUnitOfWork();
        CompanyQB1DataLoader companyQB1DataLoader = new CompanyQB1DataLoader();

        PSPDate.setPSPTime("20070822000000");
        Company companyToCreate = companyQB1DataLoader.persistQBCompany1();

        ObjectFactory objFact = new ObjectFactory();
        SIGNONMSGSRQV1 signOnMsg = null;
        signOnMsg = objFact.createSIGNONMSGSRQV1();
        SONRQ signOnRequest = objFact.createSONRQ();
        signOnRequest.setUSERID(companyToCreate.getSourceCompanyId());
        signOnMsg.setSONRQ(signOnRequest);

        PayrollServices.commitUnitOfWork();

        // fetch companyToCreate from SAPAdapter
        CompanyAdapter companyAdapter = new CompanyAdapter();
        SAPCompanyLegalInfo companyLegalInfo = companyAdapter.getCompanyLegalInfo(companyToCreate.getSourceSystemCd().toString(), companyToCreate.getSourceCompanyId());

        // submit payroll
        String ofxResponse = QBDTTestHelper.processOFXPayrollRequestHappyPath();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(ofxResponse, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String expectedNextPaycheckId = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID();
        String expectedNextPayrollTxId = ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID();

        // save companyToCreate DTO (after gratuitous change to force save)
        companyLegalInfo.setLegalName(companyLegalInfo.getLegalName() + " - updated");
        companyAdapter.updateCompanyLegalInfo(companyToCreate.getSourceSystemCd().toString(), companyToCreate.getSourceCompanyId(), companyLegalInfo,"CaseId:12345");

        // perform sync -- test IDs are as expected
        String syncOfxResponse = QBDTTestHelper.processOFXSyncRequestHappyPath();
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxSyncResponseObj = OFXManager.ofxResponseToJava(syncOfxResponse, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertEquals(expectedNextPaycheckId, ofxSyncResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYCHKNEXTID());
        assertEquals(expectedNextPayrollTxId, ofxSyncResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTXNEXTID());

    }


    @Test
    public void testNonNSFRejectReturnForUsageBillingCompany() {

        PayrollServicesTest.truncateTables();
        String psid="330010282";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 5, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, Boolean.TRUE, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit activePrimaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.deactivateEntitlementUnit(activePrimaryEntitlementUnit);
        DataLoadServices.addEntitlementUnit(company, "lic_0231", "eoc_9298", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        DataLoadServices.runJobs(3);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollRunDTO ddPayrollRun = DataLoadServices.createDDPayrollRun(company, new DateDTO(SpcfCalendar.createInstance(2016, 05, 15)));
        ProcessResult<PayrollRun> payrollRunProcessResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), ddPayrollRun);
        assertNotNull(payrollRunProcessResult);
        PayrollRun resultPayrollRun = payrollRunProcessResult.getResult();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.runOffload();

        List<TransactionTypeCode> transactionTypeCodes = new ArrayList<TransactionTypeCode>();
        List<TransactionStateCode> transactionStateCodes = new ArrayList<TransactionStateCode>();
        transactionTypeCodes.add(TransactionTypeCode.EmployerDdDebit);
        transactionStateCodes.add(TransactionStateCode.Executed);
        PayrollServices.beginUnitOfWork();
        Application.refresh(resultPayrollRun);
        DomainEntitySet<FinancialTransaction> financialTransactionsForPayrollByTypeAndState = FinancialTransaction.findFinancialTransactionsForPayrollByTypeAndState(resultPayrollRun, transactionTypeCodes, transactionStateCodes);

        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2016, 05, 21, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.returnTxns(financialTransactionsForPayrollByTypeAndState, "R04", "some Non-NSF return");
        //Go to SAP UI and create a payment transaction for the payroll that is in DebitReturned state and verify
        // If the handling fee box shows up
        // On Save, if the Hold is removed
        // EmployerDDRedebit transaction gets created for the DD Debit that was returned
        // EmployerFeeDebit transaction of $100 gets created as handling fee
        // Check the Ledger account Fee Income, it should have the $100 we created as handling fee

        //The same is being achieved by method calls below
        ArrayList<SAPPayrollBillingTransactions> sapPayrollBillingTransactions = null;
        try {
            sapPayrollBillingTransactions = new PayrollRunAdapter().findPayrollUncollectedBalances(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), resultPayrollRun.getSourcePayRunId());
            assertEquals("Count of UncollectedBalances not matching ", sapPayrollBillingTransactions.size(), 2);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        SAPPayrollBillingTransactions billingTransactions = null;
        for (SAPPayrollBillingTransactions sapBillingTransaction : sapPayrollBillingTransactions) {
            if(sapBillingTransaction.getDdTransactions().get(0).getFinancialAmount() > 0.0) {
                billingTransactions = sapBillingTransaction;
                break;
            }
        }

        SAPBillingTransaction ddTransaction = assertOne(billingTransactions.getDdTransactions());
        assertEquals(1.0, ddTransaction.getFinancialAmount(), 0.00001);

        SAPBillingTransaction handlingFeeTransaction = billingTransactions.getHandlingFeeTransaction();
        assertNotNull(handlingFeeTransaction);

        billingTransactions.getDdTransactions().get(0).setFinancialReturnAmount(1.0);
        billingTransactions.getHandlingFeeTransaction().setFinancialTxnId(SpcfUniqueId.EmptyGuid);
        billingTransactions.getHandlingFeeTransaction().setFinancialReturnAmount(billingTransactions.getHandlingFeeTransaction().getFinancialAmount());

        List<SAPPayrollBillingTransactions> sapBillingTransactions = new ArrayList<SAPPayrollBillingTransactions>();
        sapBillingTransactions.add(billingTransactions);

        try {
            new PayrollRunAdapter().redebitPayrollTransactions(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(),
                    SettlementType.Wire.toString(),
                    SAPTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime()),
                    (ArrayList<SAPPayrollBillingTransactions>) sapBillingTransactions);
        } catch (Throwable pThrowable) {
            pThrowable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Application.beginUnitOfWork();
        Application.refresh(resultPayrollRun);

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.NonAchPaymentReceived));
        CompanyEventEmail companyEventEmail = assertOne(companyEvent.getCompanyEventEmailCollection());
        assertEquals(EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1, companyEventEmail.getEmailTemplateTypeCd());

        assertEquals("Number of ER DD Redebits ", resultPayrollRun.getFinancialTransactions(TransactionStateCode.Completed, TransactionTypeCode.EmployerDdRedebit).size(), 1);
        assertEquals("Number of ER Fee Debits ", resultPayrollRun.getFinancialTransactions(TransactionStateCode.Completed, TransactionTypeCode.EmployerFeeDebit).size(), 1);

        Application.rollbackUnitOfWork();
    }

}
