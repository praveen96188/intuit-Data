package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.hibernate.FlushMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EntitlementUnitTests {
     @Before
    public void runBeforeEachTest() {
         PayrollServicesTest.beforeEachTest();
         PayrollServices.beginUnitOfWork();
         Application.truncateTables();
         ApplicationSecondary.truncateTables();
         PayrollServices.commitUnitOfWork();
     }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updateEntitlementLastMessageTimestampTwoThreadsSameProperties() {
        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addDIYEntitlementUnit(company1, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        company1 = Application.refresh(company1);
        final SpcfUniqueId entitlementUnitId = company1.getActivePrimaryEntitlementUnit().getId();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        EntitlementUnit ent = Application.findById(EntitlementUnit.class, entitlementUnitId);
        ent.setLastValidationDate(SpcfCalendar.getNow());

        // Force an update in another thread
        PayrollServices.executeTransactionThread(new TransactionThread() {
            public ProcessResult transaction() {
                EntitlementUnit localEntitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnitId);
                localEntitlementUnit.setErrorCount(10);
                Application.save(localEntitlementUnit);
                return new ProcessResult();
            }
        });

        PayrollServices.commitUnitOfWork();
    }
}
