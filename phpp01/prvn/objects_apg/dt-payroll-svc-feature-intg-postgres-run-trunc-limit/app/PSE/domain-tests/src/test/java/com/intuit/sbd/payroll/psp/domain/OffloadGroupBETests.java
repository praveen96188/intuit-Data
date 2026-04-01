package com.intuit.sbd.payroll.psp.domain;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Aug 16, 2007
 * Time: 7:13:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class OffloadGroupBETests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void validateIsBeforeCutoffTime() {
        OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD);
        SpcfCalendar calendar = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(),
                PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay(), 17, 9, 59, 59, SpcfTimeZone.getLocalTimeZone());
        assertTrue(offloadGroup.isBeforeCutoffTime(calendar));
    }

    @Test
    public void validateIsNotBeforeCutoffTime() {
        OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD);
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfCalendar.getNow().getYear(),
                SpcfCalendar.getNow().getMonth(), SpcfCalendar.getNow().getDay(), 17, 10, 1, 0, SpcfTimeZone.getLocalTimeZone());
        assertFalse(offloadGroup.isBeforeCutoffTime(calendar));
    }

    @Test
    public void validateIsBeforeCutoffTimeGetNow() {
        OffloadGroup offloadGroup = OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD);
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfCalendar.getNow().getYear(),
                SpcfCalendar.getNow().getMonth(), SpcfCalendar.getNow().getDay(), 17, 9, 0, 0, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(calendar);
        PayrollServices.commitUnitOfWork();
        assertTrue(offloadGroup.isBeforeCutoffTime());
    }

    @Test
    public void queryAllOffloadGroups() {
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<OffloadGroup> offloadGropus = Application.find(OffloadGroup.class);
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue(offloadGropus.size() > 0);
    }
}
