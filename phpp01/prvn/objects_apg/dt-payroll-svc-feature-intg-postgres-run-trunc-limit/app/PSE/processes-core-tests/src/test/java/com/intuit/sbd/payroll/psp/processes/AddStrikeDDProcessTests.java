/*
 * $Id: //psp/dev/PSE/Processes-Core/Test/com/intuit/sbd/payroll/psp/processes/AddStrikeDDProcessTests.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

/**
 *
 * User: rkrishna
 * Date: Dec 19, 2007
 * Time: 11:50:12 AM

 */
public class AddStrikeDDProcessTests {
    private DataLoader dataLoader = new DataLoader();


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * ************************************Null tests/incoming data verification***********************************
     */

    /**
     * Test message 137 - Source System Code not specified
     */
    @Test
    public void testNullSourceSystemId() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(null, "123272727", null,null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 138 - Source CompanyId not specified
     */
    @Test
    public void testNullCompany() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, null, null,null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    /**
     * Test message 169 - Company Does Not Exist
     */
    @Test
    public void testInvalidCompany() {
        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "1232727", null,null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1232727 does not exist.",
                errorMessage.getMessage());
    }

    /**
     * Test message 5001 - Strike Reason not specified
     */
    @Test
    public void testNullStrikeReason(){

        Application.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany();
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "123272727", null,null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "Strike Reason has invalid value",
                errorMessage.getMessage());
    }

    /**
     * Test message 5001 - Strike Reason Invalid Length
     */
    @Test
    public void testInvalidStrikeReasonLength(){

        Application.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany();
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "123272727",
                "AN OCCURANCE OF WHEN INTUIT ATTEMPTED AND FAILED TO COLLECT MONEY FROM A COMPANY.  " +
                        "TYPICIALLY REFERRED TO AS A NSF.AN OCCURANCE OF WHEN INTUIT ATTEMPTED AND FAILED TO COLLECT " +
                        "MONEY FROM A COMPANY.  TYPICIALLY REFERRED TO AS A NSF.", null);

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());

        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "Strike Reason has invalid value",
                errorMessage.getMessage());
    }

    /**
     * Test message 11 - Invalid Argument - Future Strike Date
     */
    @Test
    public void testFutureStrikeDate() {

        Application.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany();
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();

        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "123272727", "Strike Reason",
                SpcfCalendar.createInstance(2008, 12, 25, SpcfTimeZone.getLocalTimeZone()));

        Application.commitUnitOfWork();

        assertFalse(processResult.isSuccess());
        assertTrue("Messages size", processResult.getMessages().size() > 0);
        Message errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", "11", errorMessage.getMessageCode());
        assertEquals("Error message", "Invalid argument: Strike Date can't be in the future",
                errorMessage.getMessage());
    }

    /**
     * Test - adding the strike event
     */
    @Test
    public void addStrikeSuccess() {
        Application.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany();
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 12, 22, SpcfTimeZone.getLocalTimeZone()));
        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "123272727", "Strike Reason",
                SpcfCalendar.createInstance(2007, 12, 18, SpcfTimeZone.getLocalTimeZone()));

        CompanyEvent strikeEvent = (CompanyEvent) processResult.getResult() ;

        Application.commitUnitOfWork();

        assertSuccess("addStrikeEvent", processResult);

        assertEquals("Company Id ", "123272727", strikeEvent.getCompany().getSourceCompanyId());
        assertEquals("Event Type ", EventTypeCode.Strike,strikeEvent.getEventTypeCd());
        assertEquals("Event Strike Reason ", EnumUtils.getReadableName(StrikeReason.Manual), strikeEvent.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        assertEquals("Status ", CompanyEventStatus.Active,strikeEvent.getStatusCd());

        Application.beginUnitOfWork();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addMonths(-12);
        int strikeCount = CompanyEvent.getCompanyStrikeCount(company, fromDate, null);
        Application.commitUnitOfWork();
        
        assertEquals("12Month Strike Count ", 1,strikeCount);        
    }

    /**
     * Test - adding the strike event
     */
    @Test
    public void addStrikeSuccess_ForDated2YrsAgo() {
        Application.beginUnitOfWork();
        Company company = dataLoader.persistTestActiveCompany();
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        SpcfCalendar strikeDate = PSPDate.getPSPTime();
        strikeDate.addMonths(-24);

        ProcessResult processResult = PayrollServices.companyManager.addStrikeEvent(SourceSystemCode.QBOE, "123272727",
                "Strike Reason", strikeDate);

        CompanyEvent strikeEvent = (CompanyEvent)processResult.getResult() ;

        Application.commitUnitOfWork();

        assertSuccess("addStrikeEvent", processResult);

        assertEquals("Company Id ", "123272727", strikeEvent.getCompany().getSourceCompanyId());
        assertEquals("Event Type ", EventTypeCode.Strike,strikeEvent.getEventTypeCd());
        assertEquals("Event Strike Reason ", EnumUtils.getReadableName(StrikeReason.Manual), strikeEvent.getCompanyEventDetailValue(EventDetailTypeCode.StrikeReason));
        assertEquals("Status ", CompanyEventStatus.Active,strikeEvent.getStatusCd());

        Application.beginUnitOfWork();
        SpcfCalendar fromDate = PSPDate.getPSPTime();
        fromDate.addMonths(-12);
        int strikeCount = CompanyEvent.getCompanyStrikeCount(company, fromDate, null);
        Application.commitUnitOfWork();

        System.out.println("Strike Count " + strikeCount);
        assertEquals("12Month Strike Count ", 0,strikeCount);
    }
}
