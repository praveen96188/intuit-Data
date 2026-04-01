package com.intuit.sbd.payroll.psp.adapters.brm.test;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.brm.dto.GetUsageBillingDetailRequest;
import com.intuit.sbd.payroll.psp.adapters.brm.dto.GetUsageBillingDetailResponse;
import com.intuit.sbd.payroll.psp.adapters.brm.webservices.BRMWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.ReasonForFreeChargeCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.DataLoadPalette;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.billing.CompanyDTO;
import com.intuit.sbd.payroll.psp.processes.billing.CreateBillingUsage;
import com.intuit.sbd.payroll.psp.processes.billing.EmployeeDTO;
import com.intuit.sbd.payroll.psp.processes.billing.PaycheckDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


public class BRMWebServicesTests {


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testGetUsageBillingEmployeeDetails() throws Exception{

        GetUsageBillingDetailRequest request = new GetUsageBillingDetailRequest();
        request.setEin("00000000");
        request.setBillDate("02/15/2011 07:00:00");
        GetUsageBillingDetailResponse response = new BRMWebServices().getUsageBillingEmployeeDetails(request);
        JAXBContext context = JAXBContext.newInstance(GetUsageBillingDetailResponse.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(response, sw);
        String xmlString = sw.toString();
        assertNotNull(response);
    }

    @Test
    public void testChangesForOneEINMultipleCompanies() throws Exception{
        DataLoadServices.setPSPDate(2016,01,01);

        //Create one company
        Company company=DataLoadPalette.setupTaxCompany("111111111", "1234", 1);
        Application.beginUnitOfWork();
        EntitlementUnit primaryEntitlementUnit = Application.refresh(company).getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();
        CompanyDTO companyDTO = new CompanyDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), primaryEntitlementUnit.getEntitlement().getEntitlementOfferingCode(), primaryEntitlementUnit.getEntitlement().getLicenseNumber(), primaryEntitlementUnit.getEntitlement().getBillingDayOfMonth(), 1);
        Employee employee=company.getEmployees().get(0);
        EmployeeDTO employeeDTO = new EmployeeDTO(employee.getFullName(), employee.getSourceEmployeeId(), "1");
        PaycheckDTO paycheckDTO=new PaycheckDTO("561237", SpcfCalendar.createInstance(2016, 1, 10, 0, 0, 0, 0), "8783667", "", true, ReasonForFreeChargeCode.Trial, false);
        Application.beginUnitOfWork();
        new CreateBillingUsage(companyDTO, employeeDTO, paycheckDTO).execute();
        Application.commitUnitOfWork();

        //Call the web service v1 api
        GetUsageBillingDetailRequest request = new GetUsageBillingDetailRequest();
        request.setEin("111111111");
        request.setBillDate("02/01/2016 00:00:00");
        GetUsageBillingDetailResponse response = new BRMWebServices().getUsageBillingEmployeeDetails(request);
        JAXBContext context = JAXBContext.newInstance(GetUsageBillingDetailResponse.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.marshal(response, sw);
        String xmlString = sw.toString();
        assertNotNull(response);
        String expectedString="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><getUsageBillingDetailResponse><EmployeeDetails><EmployeeDetailInfo><CheckNumber>8783667</CheckNumber><CompanyName>TEST_COMPANY_1</CompanyName><EIN>lic_TEST_1234</EIN><EmployeeName>Last_1, First_1 M_1</EmployeeName><PaycheckDate>2016-01-09T16:00:00-08:00</PaycheckDate></EmployeeDetailInfo></EmployeeDetails><IsMultiEin>false</IsMultiEin><NumCompaniesBilled>1</NumCompaniesBilled><NumEmployeesBilled>1</NumEmployeesBilled><Status><Success>Y</Success></Status><UsagePeriodEndDate>2016-01-31T23:59:59.999-08:00</UsagePeriodEndDate><UsagePeriodStartDate>2016-01-01T00:00:00-08:00</UsagePeriodStartDate></getUsageBillingDetailResponse>";
        assertEquals("Response was unexpected", expectedString, xmlString);

        //Create another company
        company= DataLoadPalette.setupTaxCompany("111111112", "5678", 1);
        Application.beginUnitOfWork();
        primaryEntitlementUnit = Application.refresh(company).getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();
        companyDTO = new CompanyDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), primaryEntitlementUnit.getEntitlement().getEntitlementOfferingCode(), primaryEntitlementUnit.getEntitlement().getLicenseNumber(), primaryEntitlementUnit.getEntitlement().getBillingDayOfMonth(), 1);
        employee=company.getEmployees().get(0);
        employeeDTO = new EmployeeDTO(employee.getFullName(), employee.getSourceEmployeeId(), "1");
        paycheckDTO=new PaycheckDTO("561238", SpcfCalendar.createInstance(2016, 1, 10, 0, 0, 0, 0), "8783668", "", true, ReasonForFreeChargeCode.Trial, false);
        Application.beginUnitOfWork();
        new CreateBillingUsage(companyDTO, employeeDTO, paycheckDTO).execute();
        Application.commitUnitOfWork();

        //Set new company's EIN to the same as the old one
        Application.beginUnitOfWork();
        Application.refresh(company).setFedTaxId("111111111");
        Application.commitUnitOfWork();

        //Call the web service v2 api
        request = new GetUsageBillingDetailRequest();
        request.setEin("111111111");
        request.setBillDate("02/01/2016 00:00:00");
        request.setCompanyId("TEST_1234");
        response = new BRMWebServices().getUsageBillingEmployeeDetails(request);
        context = JAXBContext.newInstance(GetUsageBillingDetailResponse.class);
        marshaller = context.createMarshaller();
        sw = new StringWriter();
        marshaller.marshal(response, sw);
        xmlString = sw.toString();
        assertNotNull(response);
        expectedString="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><getUsageBillingDetailResponse><EmployeeDetails><EmployeeDetailInfo><CheckNumber>8783667</CheckNumber><CompanyName>TEST_COMPANY_1</CompanyName><EIN>lic_TEST_1234</EIN><EmployeeName>Last_1, First_1 M_1</EmployeeName><PaycheckDate>2016-01-09T16:00:00-08:00</PaycheckDate></EmployeeDetailInfo></EmployeeDetails><IsMultiEin>false</IsMultiEin><NumCompaniesBilled>1</NumCompaniesBilled><NumEmployeesBilled>1</NumEmployeesBilled><Status><Success>Y</Success></Status><UsagePeriodEndDate>2016-01-31T23:59:59.999-08:00</UsagePeriodEndDate><UsagePeriodStartDate>2016-01-01T00:00:00-08:00</UsagePeriodStartDate></getUsageBillingDetailResponse>";
        assertEquals("Response was unexpected", expectedString, xmlString);

        //Call the web service v2 api for a null EIN
        request = new GetUsageBillingDetailRequest();
        //request.setEin(null);
        request.setBillDate("02/01/2016 00:00:00");
        request.setCompanyId("TEST_1234");
        response = new BRMWebServices().getUsageBillingEmployeeDetails(request);
        context = JAXBContext.newInstance(GetUsageBillingDetailResponse.class);
        marshaller = context.createMarshaller();
        sw = new StringWriter();
        marshaller.marshal(response, sw);
        xmlString = sw.toString();
        assertNotNull(response);
        expectedString="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><getUsageBillingDetailResponse><NumCompaniesBilled>0</NumCompaniesBilled><NumEmployeesBilled>0</NumEmployeesBilled><Status><Success>N</Success><Code>30001</Code><Message>Missing Required Inputs</Message></Status></getUsageBillingDetailResponse>";
        assertEquals("Response was unexpected", expectedString, xmlString);
    }



}
