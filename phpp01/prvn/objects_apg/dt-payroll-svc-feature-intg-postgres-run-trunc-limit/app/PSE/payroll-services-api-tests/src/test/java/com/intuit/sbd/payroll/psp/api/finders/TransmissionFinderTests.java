package com.intuit.sbd.payroll.psp.api.finders;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddQBDTOFX;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Jun 9, 2008
 * Time: 11:09:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class TransmissionFinderTests {

    @Before
    public void runBeforeEachTest() {
        AddQBDTOFX.runBeforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFindCompanyTransmissions() {
        AddQBDTOFX.testFindCompanyTransmissions();
    }
}
