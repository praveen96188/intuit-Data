package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall;
import com.intuit.sbd.payroll.psp.util.FileUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class SAPMethodCallTest {


    @BeforeClass
    public static void runBeforeAllTests(){
        PayrollServicesTest.beforeEachTest();
        ApplicationSecondary.initialize();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        ApplicationSecondary.initialize();
    }

    @Before
    public void runBeforeEachTest() {
        ApplicationSecondary.beginUnitOfWork();
        ApplicationSecondary.truncateTables();
        ApplicationSecondary.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        ApplicationSecondary.beginUnitOfWork();
        ApplicationSecondary.truncateTables();
        ApplicationSecondary.commitUnitOfWork();
    }

    private void assertYearMonthDayEquals(SpcfCalendar pTimeToCompare, SpcfCalendar pPSPTime) {
        SpcfCalendar time1 = pTimeToCompare.toUtc();
        SpcfCalendar time2 = pPSPTime.toUtc();

        assertEquals(time1.getDay(), time2.getDay());
        assertEquals(time1.getMonth(), time2.getMonth());
        assertEquals(time1.getYear(), time2.getYear());
    }

    @Test
    public void setSapMethodCallInsertInSecondary() {
        ApplicationSecondary.beginUnitOfWork();
        SAPMethodCall sapMethodCall = getSAPMethodCall();
        ApplicationSecondary.save(sapMethodCall);
        ApplicationSecondary.commitUnitOfWork();
    }

    @Test
    public void addDaysToPSPDate() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar eightyDaysFromNow = SpcfCalendar.createInstance(now.getTimeInMilliseconds() + 80*86400L*1000, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(80);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(eightyDaysFromNow, PSPDate.getPSPTime());
        ApplicationSecondary.beginUnitOfWork();
        SAPMethodCall sapMethodCall2 = ApplicationSecondary.save(getSAPMethodCall());
        ApplicationSecondary.commitUnitOfWork();
    }

    @Test
    public void addDaysToPSPDate2() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar eightyDaysFromNow = SpcfCalendar.createInstance(now.getTimeInMilliseconds() + 80*86400L*1000, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(80);
        assertYearMonthDayEquals(eightyDaysFromNow, PSPDate.getPSPTime());
        ApplicationSecondary.beginUnitOfWork();
        SAPMethodCall sapMethodCall2 = ApplicationSecondary.save(getSAPMethodCall());
        ApplicationSecondary.commitUnitOfWork();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addDaysToPSPDate3() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar eightyDaysFromNow = SpcfCalendar.createInstance(now.getTimeInMilliseconds() + 80*86400L*1000, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(80);
        assertYearMonthDayEquals(eightyDaysFromNow, PSPDate.getPSPTime());
        ApplicationSecondary.beginUnitOfWork();
        SAPMethodCall sapMethodCall2 = ApplicationSecondary.save(getSAPMethodCall());
        PayrollServices.commitUnitOfWork();
        ApplicationSecondary.commitUnitOfWork();
    }

    @Test
    public void testRemoveNonAsciiCharactersExceptionTrace() {
        ApplicationSecondary.beginUnitOfWork();
        SAPMethodCall sapMethodCall = getSAPMethodCall();
        String exceptionTrace = FileUtils.readClasspathFileContent("ExceptionTraceWithNonAsciiCharacters.txt");
        sapMethodCall.setExceptionTrace(exceptionTrace);
        sapMethodCall  = ApplicationSecondary.save(sapMethodCall);
        ApplicationSecondary.commitUnitOfWork();
        sapMethodCall = ApplicationSecondary.findById(com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall.class, sapMethodCall.getId());
        String exceptionTraceWithoutNonAsciiCharacters = FileUtils.readClasspathFileContent("ExceptionTraceWithoutNonAsciiCharacters.txt");
        assertEquals(exceptionTraceWithoutNonAsciiCharacters, sapMethodCall.getExceptionTrace());
    }

    @Test
    public void testRemoveNonAsciiCharacters() {
        ApplicationSecondary.beginUnitOfWork();
        SAPMethodCall sapMethodCall = getSAPMethodCall();
        sapMethodCall.setScreenPath("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sapMethodCall.setParameters("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sapMethodCall.setExceptionTrace("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sapMethodCall.setHost("Characters: 日本人中國的~=[]()%+{}@;’#!$_&-éè;∞¥₤€");
        sapMethodCall  = ApplicationSecondary.save(sapMethodCall);
        ApplicationSecondary.commitUnitOfWork();
        SpcfUniqueId callId = sapMethodCall.getId();
        sapMethodCall = ApplicationSecondary.findById(com.intuit.sbd.payroll.psp.domainsecondary.SAPMethodCall.class, callId);
        assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sapMethodCall.getScreenPath());
        assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sapMethodCall.getParameters());
        assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sapMethodCall.getExceptionTrace());
        assertEquals("Characters: ~=[]()%+{}@;#!$_&-;", sapMethodCall.getHost());
    }

    private SAPMethodCall getSAPMethodCall() {
        Integer no = (new Double(Math.random()*100)).intValue();
        SAPMethodCall sapMethodCall = new SAPMethodCall();
        sapMethodCall.setElapsedMillis(-1);
        sapMethodCall.setHost("host"+no);
        sapMethodCall.setScreenPath("screenPath"+no);
        sapMethodCall.setServiceName("serviceName"+no);
        sapMethodCall.setMethodName("operation"+no);
        sapMethodCall.setParameters("parameters"+no);
        sapMethodCall.setResultSize(-1);
        sapMethodCall.setSecurityPrincipal("principal"+no);
        sapMethodCall.setSessionId("authToken"+no);
        return sapMethodCall;
    }
}