package com.intuit.sbd.payroll.psp.adapters.ivr.test;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ivr.dto.GetServiceKeyRequest;
import com.intuit.sbd.payroll.psp.adapters.ivr.dto.GetServiceKeyResponse;
import com.intuit.sbd.payroll.psp.adapters.ivr.dto.ServiceKeyInfo;
import com.intuit.sbd.payroll.psp.adapters.ivr.webservices.IVRWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * User: dweinberg
 * Date: Jun 14, 2010
 * Time: 2:44:00 PM
 */
public class IVRWebServicesTests {


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.reinitialize();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testServiceKeyFound() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        GetServiceKeyRequest request = new GetServiceKeyRequest();
        request.setEin("000000001");
        request.setLicenseNumber(licenseNumber);
        request.setEoc(eoc);

        GetServiceKeyResponse response = new IVRWebServices().getServiceKey(request);

        assertEquals(1, response.getServiceKeys().size());

        ServiceKeyInfo info = response.getServiceKeys().get(0);

        assertTrue(info.getServiceKey().matches("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$"));
        assertEquals(licenseNumber, info.getLicenseNumber());
        assertEquals(eoc, info.getEoc());
    }

    @Test
    public void testServiceKeyNotFoundNoLicense() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        GetServiceKeyRequest request = new GetServiceKeyRequest();
        request.setEin("000000001");
        request.setLicenseNumber("potato");
        request.setEoc(eoc);

        GetServiceKeyResponse response = new IVRWebServices().getServiceKey(request);

        assertEquals(0, response.getServiceKeys().size());
    }

    @Test
    public void testServiceKeyNotFoundNoEIN() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);

        GetServiceKeyRequest request = new GetServiceKeyRequest();
        request.setEin("987654321");
        request.setLicenseNumber(licenseNumber);
        request.setEoc(eoc);

        GetServiceKeyResponse response = new IVRWebServices().getServiceKey(request);

        assertEquals(0, response.getServiceKeys().size());
    }

    @Test
    public void testMultipleServiceKeysFound() {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";

        String licenseNumber2 = "1234567890123456789B";
        String eoc2 = "0987654321098765432B";


        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, "1", true, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addEntitlementUnit(company1, licenseNumber, eoc);

        DataLoadServices.newCompany(SourceSystemCode.QBDT, "2", true, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        
        PayrollServices.beginUnitOfWork();
        Company company2 = Company.findCompany("2", SourceSystemCode.QBDT);
        company2.setFedTaxId("000000001");
        Application.save(company2);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addEntitlementUnit(company2, licenseNumber2, eoc2);

        GetServiceKeyRequest request = new GetServiceKeyRequest();
        request.setEin("000000001");

        GetServiceKeyResponse response = new IVRWebServices().getServiceKey(request);

        assertEquals(3, response.getServiceKeys().size());

        ServiceKeyInfo info1 = response.getServiceKeys().get(0);
        ServiceKeyInfo info2 = response.getServiceKeys().get(1);

        assertTrue(info1.getServiceKey().matches("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$"));
        assertEquals(licenseNumber, info1.getLicenseNumber());
        assertEquals(eoc, info1.getEoc());

        assertTrue(info2.getServiceKey().matches("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$"));
        assertEquals(licenseNumber2, info2.getLicenseNumber());
        assertEquals(eoc2, info2.getEoc());
    }

}
