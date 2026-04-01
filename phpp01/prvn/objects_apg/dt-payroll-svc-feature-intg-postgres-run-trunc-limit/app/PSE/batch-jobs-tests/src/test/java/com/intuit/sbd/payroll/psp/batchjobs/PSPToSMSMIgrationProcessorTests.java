package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SMSMigration;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import org.junit.*;

import java.util.UUID;

import static com.intuit.sbd.payroll.psp.domain.SMSMigration.getSmsMigrationByCompany;

public class PSPToSMSMIgrationProcessorTests {

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
    public void testPSPtoSMSMigrationProcessor_1_PSID() throws Exception {
        String psid = UUID.randomUUID().toString();

        //create company with VMP and Assisted service
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        String[] args = {"run", "PSPToSMSMigrationProcessor", "-smsMigartionCompanyIds", psid};

        BatchJobManager.executeCommand(args);

        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        DomainEntitySet<SMSMigration> migrationRows = getSmsMigrationByCompany(company1);
        junit.framework.Assert.assertEquals(migrationRows.size(), 1);
    }
}
