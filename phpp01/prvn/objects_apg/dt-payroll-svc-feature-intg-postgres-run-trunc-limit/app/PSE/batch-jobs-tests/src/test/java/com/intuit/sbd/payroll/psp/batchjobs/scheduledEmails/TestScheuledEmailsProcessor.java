package com.intuit.sbd.payroll.psp.batchjobs.scheduledEmails;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.billing.UsageBillingTestsBase;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *   Tests for the scheduled emails processor batch jobs
 */
public class TestScheuledEmailsProcessor extends UsageBillingTestsBase {
    @Test
    @Ignore
    public void testUsageBilling15DaysIntoSubscription() {

        DataLoadServices.setPSPDate(2011, 7, 20);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        Entitlement entitlement = primaryEntitlementUnit.getEntitlement();
        SpcfCalendar subscriptionStartDate = PSPDate.getPSPTime().copy();
        subscriptionStartDate.addDays(-15);
        entitlement.setSubscriptionStartDate(subscriptionStartDate);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();
        BatchJobManager.runJob(BatchJobType.ScheduledEmails);
        DomainEntitySet<CompanyEvent> eventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.UsageBilling15DaysIntoSubscription);
        boolean foundEmail = false;
        if (eventList != null) {
            for (CompanyEvent event : eventList) {
                DomainEntitySet<CompanyEventEmail> emailList = event.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.UsageBillingMidTrial));
                foundEmail = emailList != null && emailList.size() > 0;
                if (foundEmail) {
                    for(CompanyEventEmail email : emailList) {
                        assertNotNull("CompanyEventEmail company null",email.getCompany());
                        assertEquals(email.getCompanyEvent().getCompany().getSourceCompanyId(),
                                email.getCompany().getSourceCompanyId());
                    }
                    break;
                }
            }
        }
        assertTrue(foundEmail);
    }

    @Test
    public void testUsageBilling25DaysIntoSubscription() {

        DataLoadServices.setPSPDate(2011, 7, 20);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        Entitlement entitlement = primaryEntitlementUnit.getEntitlement();
        SpcfCalendar subscriptionStartDate = PSPDate.getPSPTime().copy();
        subscriptionStartDate.addDays(-25);
        entitlement.setSubscriptionStartDate(subscriptionStartDate);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();
        BatchJobManager.runJob(BatchJobType.ScheduledEmails);

        Application.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> eventList = CompanyEvent.findCompanyEvents(company, EventTypeCode.UsageBilling25DaysIntoSubscription);
        boolean foundEmail = false;
        for (CompanyEvent event : eventList) {
            DomainEntitySet<CompanyEventEmail> emailList = event.getCompanyEventEmailCollection().find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(EventEmailTemplateTypeCode.SymphonyBillingDetailsMonthly));
            foundEmail = emailList != null && emailList.size() > 0;
            if (foundEmail) {
                for(CompanyEventEmail email : emailList) {
                    assertNotNull("CompanyEventEmail company null",email.getCompany());
                    assertEquals(email.getCompanyEvent().getCompany().getSourceCompanyId(),
                            email.getCompany().getSourceCompanyId());
                }
                break;
            }
        }

        assertTrue(foundEmail);
        Application.rollbackUnitOfWork();
    }
}
