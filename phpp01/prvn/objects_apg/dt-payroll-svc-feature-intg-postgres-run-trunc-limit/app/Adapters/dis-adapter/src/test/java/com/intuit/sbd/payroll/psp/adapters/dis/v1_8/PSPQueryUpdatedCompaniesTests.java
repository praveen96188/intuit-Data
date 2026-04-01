package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests.QueryUpdatedCompaniesRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses.QueryUpdatedCompaniesResponseDISDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryUpdatedCompaniesTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryUpdatedCompaniesTests {
    private String company1Psid = "123123123";
    private String company2Psid = "321321321";

    private SpcfCalendar assistedCompany1CreateDate;
    private SpcfCalendar assistedCompany2CreateDate;

    @Before
    public void loadDataHappyPath() {

        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20120305100000");
        PayrollServices.commitUnitOfWork();

        Company assistedCompany1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, company1Psid, false, ServiceCode.Tax);

        // Reload company as services are not in company object yet.
        {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceCompanyId().equalTo(assistedCompany1.getSourceCompanyId())));
            assistedCompany1 = companies.get(0);
            assistedCompany1CreateDate =  assistedCompany1.getService(ServiceCode.Tax).getCreatedDate();
            PayrollServices.rollbackUnitOfWork();
        }


        Company ddCompany1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "111222333", false, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20120305210000");
        PayrollServices.commitUnitOfWork();

        Company assistedCompany2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, company2Psid, false, ServiceCode.Tax);
        // Reload company as services are not in company object yet.
        {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class, new Query<Company>().Where(Company.SourceCompanyId().equalTo(assistedCompany2.getSourceCompanyId())));
            assistedCompany2 = companies.get(0);
            assistedCompany2CreateDate = assistedCompany2.getService(ServiceCode.Tax).getCreatedDate();
            PayrollServices.rollbackUnitOfWork();
        }

    }

    @Test
    public void testQueryUpdatedCompaniesStartDateOnlyNoCompanies() {
        SpcfCalendar spcfCalendarOneSecondAfterSecondCompany = SpcfCalendar.createInstance(assistedCompany2CreateDate.getTimeInMilliseconds()+1000);
        performQueryUpdateWebServiceCall(spcfCalendarOneSecondAfterSecondCompany,null,0);
    }

    @Test
    public void testQueryUpdatedCompaniesStartDateOnlyMultiple() {
        QueryUpdatedCompaniesResponseDISDTO responseDISDTO = performQueryUpdateWebServiceCall(assistedCompany1CreateDate,null,2);

        TestCase.assertEquals(company1Psid,responseDISDTO.getCompanies().get(0).getPsid());
        TestCase.assertEquals(company2Psid,responseDISDTO.getCompanies().get(1).getPsid());
    }

    @Test
    public void testQueryUpdatedCompaniesStartDateOnlyMultipleWhenDgDisassociated() {
        try {
            PayrollServices.beginUnitOfWork();
            Company dgDisassociatedCompany1 = Company.findCompany(company1Psid, SourceSystemCode.QBDT);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }

        QueryUpdatedCompaniesResponseDISDTO responseDISDTO = performQueryUpdateWebServiceCall(assistedCompany1CreateDate,null,1);
        TestCase.assertEquals(company2Psid,responseDISDTO.getCompanies().get(0).getPsid());
    }

    @Test
    public void testQueryUpdatedCompaniesStartAndEndDateMultipleCompaniesWhenDgDisassociated() {
        try {
            PayrollServices.beginUnitOfWork();
            Company dgDisassociatedCompany1 = Company.findCompany(company1Psid, SourceSystemCode.QBDT);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        QueryUpdatedCompaniesResponseDISDTO responseDISDTO = performQueryUpdateWebServiceCall(assistedCompany1CreateDate, assistedCompany2CreateDate,1);
        TestCase.assertEquals(company2Psid,responseDISDTO.getCompanies().get(0).getPsid());
    }

    @Test
    public void testQueryUpdatedCompaniesStartDateOnlySingle() {
        performQueryUpdateWebServiceCall(assistedCompany2CreateDate,null,1);
    }

    @Test
    public void testQueryUpdatedCompaniesStartAndEndDateNoCompaniesDateAfterBoth() {
        SpcfCalendar spcfCalendarOneSecondAfterSecondCompany = SpcfCalendar.createInstance(assistedCompany2CreateDate.getTimeInMilliseconds()+1000);
        performQueryUpdateWebServiceCall(spcfCalendarOneSecondAfterSecondCompany,spcfCalendarOneSecondAfterSecondCompany,0);
    }

    @Test
    public void testQueryUpdatedCompaniesStartAndEndDateNoCompaniesDateBeforeBoth() {
        SpcfCalendar spcfCalendarOneSecondBeforeSecondCompany = SpcfCalendar.createInstance(assistedCompany1CreateDate.getTimeInMilliseconds()-1000);
        performQueryUpdateWebServiceCall(spcfCalendarOneSecondBeforeSecondCompany,spcfCalendarOneSecondBeforeSecondCompany,0);
    }

    @Test
    public void testQueryUpdatedCompaniesStartAndEndDateNoCompaniesDateMiddleOfBoth() {
        SpcfCalendar spcfCalendarOneSecondAfterFirstCompany = SpcfCalendar.createInstance(assistedCompany1CreateDate.getTimeInMilliseconds()+1000);
        SpcfCalendar spcfCalendarOneSecondBeforeSecondCompany = SpcfCalendar.createInstance(assistedCompany1CreateDate.getTimeInMilliseconds()-1000);
        performQueryUpdateWebServiceCall(spcfCalendarOneSecondAfterFirstCompany,spcfCalendarOneSecondBeforeSecondCompany,0);
    }

    @Test
    public void testQueryUpdatedCompaniesStartAndEndDateMultipleCompanies() {
        QueryUpdatedCompaniesResponseDISDTO responseDISDTO = performQueryUpdateWebServiceCall(assistedCompany1CreateDate,assistedCompany2CreateDate,2);

        TestCase.assertEquals(company1Psid,responseDISDTO.getCompanies().get(0).getPsid());
        TestCase.assertEquals(company2Psid,responseDISDTO.getCompanies().get(1).getPsid());
    }

    @Test
    public void testQueryUpdatedCompaniesStartAndEndDateOneCompany() {
        performQueryUpdateWebServiceCall(assistedCompany1CreateDate,assistedCompany2CreateDate,2);
    }

    public QueryUpdatedCompaniesResponseDISDTO performQueryUpdateWebServiceCall(SpcfCalendar pStartSpcfCalendar,SpcfCalendar pEndSpcfCalendar,int pExpectedCount) {
        QueryUpdatedCompaniesResponseDISDTO responseDISDTO = null;
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryUpdatedCompaniesRequestDISDTO requestDISDTO = new QueryUpdatedCompaniesRequestDISDTO();

            Calendar begin = CalendarUtils.convertToCalendar(pStartSpcfCalendar);
            requestDISDTO.setStartDate(begin);
            requestDISDTO.setServiceCode(ServiceCode.Tax);
            if (pEndSpcfCalendar != null) {
                Calendar end = CalendarUtils.convertToCalendar(pEndSpcfCalendar);
                requestDISDTO.setEndDate(end);
            }

            responseDISDTO = disAdapter.Query_UpdatedCompanies(requestDISDTO);
            Assert.assertEquals(pExpectedCount, responseDISDTO.getCompanies().size());
            TestHelper.verifySuccess(responseDISDTO.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();
            TestCase.fail(e.getMessage());
        }
        return responseDISDTO;
    }

}
