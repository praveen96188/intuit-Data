package com.intuit.sbd.payroll.psp.api.other;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.EventDetailType;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * User: dweinberg
 * Date: Jun 23, 2009
 * Time: 8:55:15 AM
 * I don't know where to put this, so I put it here.
 * It tests that all the event detail codes have been inserted into the static data
 * or else it breaks the UI event log.
 */
public class EventDetailTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void compareEventDetailCodesToStaticDataTest() {
        PayrollServices.beginUnitOfWork();

        StringBuilder missing = new StringBuilder();

        for (EventDetailTypeCode code : EventDetailTypeCode.values()) {
            EventDetailType eventDetailType = PayrollServices.entityFinder.findById(EventDetailType.class, code);
            if (eventDetailType == null) {
                missing.append(code)
                        .append(" is in the EventDetailTypeCode enumeration, but not in populate_eventtype.sql")
                        .append("\n");
            }
        }

        if (missing.length() > 0) {
            fail(missing.toString());
        }

        PayrollServices.rollbackUnitOfWork();
    }
}
