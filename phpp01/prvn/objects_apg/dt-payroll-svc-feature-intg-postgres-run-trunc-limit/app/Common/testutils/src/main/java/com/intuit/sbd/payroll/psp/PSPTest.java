package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 20, 2010
 * Time: 1:24:02 PM
 */
public abstract class PSPTest {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(getInitialDate());
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    public SpcfCalendar getInitialDate() {
        return SpcfCalendar.createInstance(2010, 12, 22, SpcfTimeZone.getLocalTimeZone());
    }
}
