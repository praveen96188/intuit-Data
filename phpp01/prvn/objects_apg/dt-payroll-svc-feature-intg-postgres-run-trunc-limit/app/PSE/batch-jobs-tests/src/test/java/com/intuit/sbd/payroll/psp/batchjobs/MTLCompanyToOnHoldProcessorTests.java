package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.OnHoldReason;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import junit.framework.Assert;
import org.junit.*;

import java.util.Collection;
import java.util.UUID;

public class MTLCompanyToOnHoldProcessorTests {
    @AfterClass
    public static void afterClass() {

    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    @Ignore
    public void testMTLCompanyToOnHoldProcessor_1_PSID() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","MTLCompanyToOnHoldProcessor","-psIds", psid};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        Collection<OnHoldReason> onHoldReason = company1.getCurrentOnHoldReasons();
        Assert.assertEquals(onHoldReason.size(), 1);
        Assert.assertEquals(onHoldReason.stream().findFirst().toString(), ServiceSubStatusCode.MTLHold.toString());
    }

}
