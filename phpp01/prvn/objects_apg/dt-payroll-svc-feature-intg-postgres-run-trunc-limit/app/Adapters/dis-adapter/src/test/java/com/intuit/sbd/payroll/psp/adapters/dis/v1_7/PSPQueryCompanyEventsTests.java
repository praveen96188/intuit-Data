package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyEventDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyEventsRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyEventsResponseDISDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import junit.framework.TestCase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryCompanyEventsTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryCompanyEventsTests {
    private String source_company_id = null;
    private Company company1 = null;
    private DomainEntitySet<CompanyEvent> coEvents;
    private DomainEntitySet<CompanyEvent> coSignupEvents;
    private CompanyEvent coSignupEvent;

    @Before
    public void loadDataHappyPath() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime("20070822000000");
        PayrollServices.commitUnitOfWork();

        company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "12312332", false, ServiceCode.Tax);
        source_company_id = company1.getSourceCompanyId();
        PayrollServices.beginUnitOfWork();
        coEvents = CompanyEvent.findCompanyEvents(company1);
        coSignupEvent = CompanyEvent.findCompanyEvents(company1, EventTypeCode.CustomerSignedUp).get(0);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        coEvents = CompanyEvent.findCompanyEvents(company1);
        coSignupEvents = CompanyEvent.findCompanyEvents(company1, EventTypeCode.CustomerSignedUp);
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testQueryCompanyEventsAll() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEventsRequestDISDTO queryCompanyEventsRequestDISDTO = new QueryCompanyEventsRequestDISDTO();
            queryCompanyEventsRequestDISDTO.setSourceCompanyId(company1.getSourceCompanyId());
            queryCompanyEventsRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));

            QueryCompanyEventsResponseDISDTO queryCompanyEventsResponseDISDTO = disAdapter.Query_CompanyEvent(queryCompanyEventsRequestDISDTO);
            Assert.assertEquals(coEvents.size(), queryCompanyEventsResponseDISDTO.getCompanyEvents().size());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testQueryCompanyEventTypeSpecified() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEventsRequestDISDTO queryCompanyEventsRequestDISDTO = new QueryCompanyEventsRequestDISDTO();
            queryCompanyEventsRequestDISDTO.setSourceCompanyId(company1.getSourceCompanyId());
            queryCompanyEventsRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));
            queryCompanyEventsRequestDISDTO.setEventTypeCode(EventTypeCode.CustomerSignedUp.toString());

            QueryCompanyEventsResponseDISDTO queryCompanyEventsResponseDISDTO = disAdapter.Query_CompanyEvent(queryCompanyEventsRequestDISDTO);
            Assert.assertEquals(coSignupEvents.size(), queryCompanyEventsResponseDISDTO.getCompanyEvents().size());

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testQueryCompanyEventsContents() {
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEventsRequestDISDTO queryCompanyEventsRequestDISDTO = new QueryCompanyEventsRequestDISDTO();
            queryCompanyEventsRequestDISDTO.setSourceCompanyId(company1.getSourceCompanyId());
            queryCompanyEventsRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));
            queryCompanyEventsRequestDISDTO.setEventTypeCode(EventTypeCode.CustomerSignedUp.toString());

            QueryCompanyEventsResponseDISDTO queryCompanyEventsResponseDISDTO = disAdapter.Query_CompanyEvent(queryCompanyEventsRequestDISDTO);
            Assert.assertEquals(coSignupEvents.size(), queryCompanyEventsResponseDISDTO.getCompanyEvents().size());
            CompanyEventDISDTO returnedSignupEventDISDTO = queryCompanyEventsResponseDISDTO.getCompanyEvents().get(0);
            Assert.assertEquals(coSignupEvent.getCreatorId(), returnedSignupEventDISDTO.getCreatorId());
            Assert.assertEquals(new Date(coSignupEvent.getEventTimeStamp().getTimeInMilliseconds()), returnedSignupEventDISDTO.getEventTimeStamp());
            Assert.assertEquals(coSignupEvent.getEventTypeCd().toString(), returnedSignupEventDISDTO.getEventTypeCode());
            Assert.assertEquals(coSignupEvent.getStatusCd(), returnedSignupEventDISDTO.getStatusCd());
            Assert.assertEquals(new Date(coSignupEvent.getStatusEffectiveDate().getTimeInMilliseconds()), returnedSignupEventDISDTO.getStatusEffectiveDate());
            EventType eventType = PayrollServices.entityFinder.findById(EventType.class, coSignupEvent.getEventTypeCd());
            Assert.assertEquals(eventType.getName(), returnedSignupEventDISDTO.getName());
            Assert.assertTrue(returnedSignupEventDISDTO.getEventDetails().size() > 0);

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testNoCompanyFound() {
        try {
            String sourceCoIdDNE = "companyDNE";
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEventsRequestDISDTO request = new QueryCompanyEventsRequestDISDTO();
            request.setSourceCompanyId(sourceCoIdDNE);
            request.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));

            QueryCompanyEventsResponseDISDTO response = disAdapter.Query_CompanyEvent(request);
            Assert.assertNull(response.getCompanyEvents());
            TestHelper.verifyDISResponse(DISMessages.companyDoesNotExist(sourceCoIdDNE), response.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testMigratedCompanyWithDDAndAssistedPayrolls() {

        String psid = DISCompanyDataloader.setupMigratedCompanyWithDDPayrollAndAssistedPayroll().getSourceCompanyId();
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEventsRequestDISDTO queryCompanyEventsRequestDISDTO = new QueryCompanyEventsRequestDISDTO();
            queryCompanyEventsRequestDISDTO.setSourceCompanyId(company1.getSourceCompanyId());
            queryCompanyEventsRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(company1.getSourceSystemCd()));
            queryCompanyEventsRequestDISDTO.setEventTypeCode(EventTypeCode.CustomerSignedUp.toString());

            QueryCompanyEventsResponseDISDTO queryCompanyEventsResponseDISDTO = disAdapter.Query_CompanyEvent(queryCompanyEventsRequestDISDTO);
            TestHelper.verifySuccess(queryCompanyEventsResponseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }
}