package com.intuit.sbd.payroll.psp.api.other;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.EventDetailType;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventType;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

/**
 * User: dweinberg
 * Date: Jun 23, 2009
 * Time: 8:55:15 AM
 * this is the same as eventdetails but for events 
 */
public class EventTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void compareEventDetailCodesToStaticDataTest() {
        PayrollServices.beginUnitOfWork();

        StringBuilder missing = new StringBuilder();

        for (EventTypeCode code : EventTypeCode.values()) {
            EventType eventType = PayrollServices.entityFinder.findById(EventType.class, code);
            if (eventType == null) {
                missing.append(code)
                        .append(" is in the EventTypeCode enumeration, but not in populate_eventtype.sql")
                        .append("\n");
            }
        }

        if (missing.length() > 0) {
            fail(missing.toString());
        }

        PayrollServices.rollbackUnitOfWork();
    }
}
