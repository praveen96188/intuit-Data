package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static org.junit.Assert.assertEquals;

/**
 * User: dweinberg
 * Date: 1/8/13
 * Time: 3:06 PM
 * Tests for code gen feature of manually getting collections based on the partition key
 */
public class PartitionedLazyCollectionTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testMoneyMovementFTsAndEDRsAreLoadedByInitiationDate() {
        /* Relationships:
            FT.SettlementDate >= MMT.InitiationDate
            EDR.InitiationDate = MMT.InitiationDate
         */
        DataLoadServices.setPSPDate(2013, 1, 1);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        Application.beginUnitOfWork();
        MoneyMovementTransaction ddCredit = assertOne(Application.find(MoneyMovementTransaction.class,
                                                                       MoneyMovementTransaction.Company().equalTo(company)
                                                                                               .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(new SpcfMoney("1.00")))));
        assertEquals(1, ddCredit.getFinancialTransactionCollection().size());
        assertEquals(2, ddCredit.getEntryDetailRecordCollection().size());

        //switch-a-roo!
        ddCredit.getFinancialTransactionCollection().getFirst().setSettlementDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        ddCredit.getEntryDetailRecordCollection().getFirst().setInitiationDate(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        //un-initialize
        ddCredit = Application.refresh(ddCredit);

        assertEquals(0, ddCredit.getFinancialTransactionCollection().size());
        assertEquals(1, ddCredit.getEntryDetailRecordCollection().size());

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testNoUpdateIfNothingChanges() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud);

        Application.beginUnitOfWork();
        CompanyEvent companyEvent = CompanyEvent.createOfferingUpdatedEvent(company, "Old1", "New1");
        assertEquals(0L, companyEvent.getVersion());
        Application.commitUnitOfWork();
        /*
            In hibernate 5.x on commitUnitOfWork() i.e on flush() causes version to be incremented due to
            dirty collection(company event details) present in the companyEvent.
         */
        assertEquals(1L, companyEvent.getVersion());

        Application.beginUnitOfWork();
        companyEvent = Application.findById(CompanyEvent.class, companyEvent.getId());
        companyEvent.getCompanyEventDetailCollection();
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        Application.refresh(companyEvent);
        assertEquals(1L, companyEvent.getVersion());
        Application.rollbackUnitOfWork();
    }
}
