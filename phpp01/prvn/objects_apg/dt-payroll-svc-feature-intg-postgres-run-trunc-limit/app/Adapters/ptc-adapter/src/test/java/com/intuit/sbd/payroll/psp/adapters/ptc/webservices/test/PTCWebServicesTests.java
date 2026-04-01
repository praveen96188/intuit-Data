package com.intuit.sbd.payroll.psp.adapters.ptc.webservices.test;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ptc.dto.*;
import com.intuit.sbd.payroll.psp.adapters.ptc.exception.PTCAdapterException;
import com.intuit.sbd.payroll.psp.adapters.ptc.webservices.PTCWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Assert;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * User: dweinberg
 * Date: 8/16/12
 * Time: 10:34 AM
 */
public class PTCWebServicesTests {

    private Company company;


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(2012, 10, 1);

        company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax, ServiceCode.DirectDeposit);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testValidatePINReturnsMissingInput() {
        PINValidationRequest request;

        request = createPINValidationRequest();
        request.setEin(null);
        assertPINValidationResponse(request, PTCWebServices.ERR_MISSING_INPUT);

        request = createPINValidationRequest();
        request.setSourceSystemCode("");
        assertPINValidationResponse(request, PTCWebServices.ERR_MISSING_INPUT);

        request = createPINValidationRequest();
        request.setPin(null);
        assertPINValidationResponse(request, PTCWebServices.ERR_MISSING_INPUT);

        request = createPINValidationRequest();
        request.setPsid("");
        assertPINValidationResponse(request, PTCWebServices.ERR_MISSING_INPUT);
    }

    @Test
    public void testValidatePINReturnsInvalidInput() {
        PINValidationRequest request;

        request = createPINValidationRequest();
        request.setPsid("NotARealPSID");
        assertPINValidationResponse(request, PTCWebServices.ERR_INVALID_INPUT);

        request = createPINValidationRequest();
        request.setEin("NotARealEIN");
        assertPINValidationResponse(request, PTCWebServices.ERR_INVALID_INPUT);
    }

    @Test
    public void testValidatePINPerformsPINValidation() {
        PINValidationRequest request;

        request = createPINValidationRequest();
        request.setPin("NotMyRealPIN");
        assertPINValidationResponse(request, PTCWebServices.ERR_INVALID_PIN);
        assertPINValidationResponse(request, PTCWebServices.ERR_INVALID_PIN);
        assertPINValidationResponse(request, PTCWebServices.ERR_PIN_LOCKOUT);

        request = createPINValidationRequest();
        assertPINValidationResponse(request, PTCWebServices.ERR_PIN_LOCKOUT);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar newTime = PSPDate.getPSPTime().copy();
        newTime.addMinutes(20);
        PSPDate.setPSPTime(newTime);
        PayrollServices.commitUnitOfWork();

        assertPINValidationResponse(request, PTCWebServices.VALID);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertNull(company.getAccountLockedUntil());
        assertEquals(0, company.getNumberOfFailedLoginAttempts());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testValidatePINReturnsUnexpectedError() {
        PINValidationRequest request;
        request = createPINValidationRequest();
        request.setSourceSystemCode("NotARealSourceSystem");
        assertPINValidationResponse(request, PTCWebServices.EX_VALIDATION_FAILED);
    }

    private PINValidationRequest createPINValidationRequest() {
        PINValidationRequest request = new PINValidationRequest();
        request.setEin(company.getFedTaxId());
        request.setPin(DataLoadServices.PIN);
        request.setPsid(company.getSourceCompanyId());
        request.setSourceSystemCode(company.getSourceSystemCd().toString());
        return request;
    }

    private void assertPINValidationResponse(PINValidationRequest request, String expectedResponseStatus) {
        PINValidationResponse pinValidationResponse = new PTCWebServices().validatePIN(request);
        assertEquals(expectedResponseStatus, pinValidationResponse.getStatus());
    }

    @Test
    public void testGetW2ServiceChargePrice() {
        ServiceChargePrice w2ServiceChargePrice = new PTCWebServices().getW2ServiceChargePrice(createPSPCompanyRequest());
        assertEquals("$40.00", w2ServiceChargePrice.getBasePrice());
        assertEquals("$4.25", w2ServiceChargePrice.getUnitPrice());
    }

    @Test(expected = PTCAdapterException.class)
    public void testGetW2ServiceChargePriceWhenDgDisassociated() {
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            Company dgDisassociatedCompany1 = companies.get(0);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }

        new PTCWebServices().getW2ServiceChargePrice(createPSPCompanyRequest());
    }

    @Test
    //were this test to fail, the PTC Adapter would not give the correct fees
    public void testW2FeesAreNotTiered() {
        PayrollServices.beginUnitOfWork();
        HqlBuilder builder = new HqlBuilder("from com.intuit.sbd.payroll.psp.domain.OfferingServiceChargePrice price " +
                                                    "join price.OfferingServiceCharge charge " +
                                                    "join charge.OfferingServiceChargeGroup grp " +
                                                    "where grp.AppliesTo in (:appliesTo) " +
                                                    "and charge.TierNumber > 0");
        builder.setParameterList("appliesTo", OfferingServiceChargeType.W2BaseFee, OfferingServiceChargeType.W2Fee);

        assertEquals(0, builder.list().size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testActiveOnService() {
        assertTrue(new PTCWebServices().isActiveOnService(createPSPCompanyRequest()));

        DataLoadServices.cancelService(company, ServiceCode.Tax);

        assertTrue(!new PTCWebServices().isActiveOnService(createPSPCompanyRequest()));
    }

    @Test(expected = PTCAdapterException.class)
    public void testActiveOnServiceWhenDgDisassociated() {
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            Company dgDisassociatedCompany1 = companies.get(0);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        new PTCWebServices().isActiveOnService(createPSPCompanyRequest());
    }

    @Test
    public void testGetPSIDForEIN() throws  Throwable {
        assertEquals(company.getSourceCompanyId(), new PTCWebServices().getPSIDForEIN("QBDT", company.getFedTaxId()));

        String ein = "991234567";

        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, DataLoadServices.nextCompanyId(), ein, true, ServiceCode.Cloud, ServiceCode.Tax, ServiceCode.DirectDeposit);
        assertEquals(company1.getSourceCompanyId(), new PTCWebServices().getPSIDForEIN("QBDT", ein));
        DataLoadServices.cancelService(company1, ServiceCode.Tax);
        DataLoadServices.cancelService(company1, ServiceCode.DirectDeposit);
        assertEquals(company1.getSourceCompanyId(), new PTCWebServices().getPSIDForEIN("QBDT", ein));

        try { Thread.sleep(10); } catch (InterruptedException e) {}
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, DataLoadServices.nextCompanyId(), ein, false, ServiceCode.Cloud, ServiceCode.Tax, ServiceCode.DirectDeposit);
        assertEquals(company2.getSourceCompanyId(), new PTCWebServices().getPSIDForEIN("QBDT", ein));
        DataLoadServices.activateServices(company2, ServiceCode.Cloud, ServiceCode.Tax, ServiceCode.DirectDeposit);
        assertEquals(company2.getSourceCompanyId(), new PTCWebServices().getPSIDForEIN("QBDT", ein));
        DataLoadServices.cancelService(company1, ServiceCode.Tax);
        DataLoadServices.cancelService(company1, ServiceCode.DirectDeposit);
        assertEquals(company2.getSourceCompanyId(), new PTCWebServices().getPSIDForEIN("QBDT", ein));
    }

    @Test
    public void testGetPSIDForEINWhenDgDisassociated() throws  Throwable {
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            Company dgDisassociatedCompany1 = companies.get(0);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        assertNull(new PTCWebServices().getPSIDForEIN("QBDT", company.getFedTaxId()));
    }

    @Test
    public void testIsEligibleForCancelForms() {
        DataLoadServices.setPSPDate(2012, 10, 1);

        assertTrue(new PTCWebServices().isEligibleForCancelForms(createPSPCompanyRequest()));

        DataLoadServices.cancelService(company, ServiceCode.Tax);
        assertTrue(new PTCWebServices().isEligibleForCancelForms(createPSPCompanyRequest()));

        DataLoadServices.setPSPDate(2013, 6, 15);
        assertTrue(new PTCWebServices().isEligibleForCancelForms(createPSPCompanyRequest()));

        DataLoadServices.setPSPDate(2013, 7, 1);
        assertTrue(!new PTCWebServices().isEligibleForCancelForms(createPSPCompanyRequest()));

    }

    @Test(expected = PTCAdapterException.class)
    public void testIsEligibleForCancelFormsWhenDgDisassociated() {
        DataLoadServices.setPSPDate(2012, 10, 1);
        assertTrue(new PTCWebServices().isEligibleForCancelForms(createPSPCompanyRequest()));

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            Company dgDisassociatedCompany1 = companies.get(0);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        new PTCWebServices().isEligibleForCancelForms(createPSPCompanyRequest());
    }

    @Test
    public void testPrintingPreferences() {
        new PTCWebServices().updateW2PrintingPreference(createPSPCompanyRequest(), "Electronic");
        assertEquals("Electronic", new PTCWebServices().getW2PrintingPreference(createPSPCompanyRequest()));
        new PTCWebServices().updateW2PrintingPreference(createPSPCompanyRequest(), "Mail");
        assertEquals("Mail", new PTCWebServices().getW2PrintingPreference(createPSPCompanyRequest()));
    }

    @Test(expected = PTCAdapterException.class)
    public void testPrintingPreferencesWhenDgDisassociated() {
        new PTCWebServices().updateW2PrintingPreference(createPSPCompanyRequest(), "Electronic");
        assertEquals("Electronic", new PTCWebServices().getW2PrintingPreference(createPSPCompanyRequest()));


        new PTCWebServices().updateW2PrintingPreference(createPSPCompanyRequest(), "Mail");
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            Company dgDisassociatedCompany1 = companies.get(0);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        new PTCWebServices().getW2PrintingPreference(createPSPCompanyRequest());
    }

    @Test
    public void testLegalInfo() {
        LegalInfo legalInfo = new PTCWebServices().getLegalInfo(createPSPCompanyRequest());
        assertEquals(company.getLegalName(), legalInfo.getLegalName());
        assertEquals(company.getLegalAddress().getAddressLine1(), legalInfo.getLegalAddress().getAddress1());
    }

    @Test(expected = PTCAdapterException.class)
    public void testLegalInfoWhenDgDisassociated() {
        LegalInfo legalInfo = new PTCWebServices().getLegalInfo(createPSPCompanyRequest());
        assertEquals(company.getLegalName(), legalInfo.getLegalName());
        assertEquals(company.getLegalAddress().getAddressLine1(), legalInfo.getLegalAddress().getAddress1());

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            Company dgDisassociatedCompany1 = companies.get(0);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        new PTCWebServices().getLegalInfo(createPSPCompanyRequest());
    }

    @Test
    public void testGetEmployeeInfo() {
        Employee employee = DataLoadServices.getLastCreatedEmployee(company);
        assertNull(new PTCWebServices().getEmployeeInfo(createEmployeeInfoRequest("65654", "2")));
        EmployeeInfo employeeInfo = new PTCWebServices().getEmployeeInfo(createEmployeeInfoRequest(employee.getTaxId(), employee.getFirstMiddleLastName()));
        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals(employee.getFirstName(), employeeInfo.getFirstName());
        assertEquals(employee.getMailingAddress().getAddressLine1(), employeeInfo.getAddress().getAddress1());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test(expected = PTCAdapterException.class)
    public void testGetEmployeeInfoWhenDgDisassociated() {
        Employee employee = DataLoadServices.getLastCreatedEmployee(company);
        assertNull(new PTCWebServices().getEmployeeInfo(createEmployeeInfoRequest("65654", "2")));
        EmployeeInfo employeeInfo = new PTCWebServices().getEmployeeInfo(createEmployeeInfoRequest(employee.getTaxId(), employee.getFirstMiddleLastName()));

        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        assertEquals(employee.getFirstName(), employeeInfo.getFirstName());
        assertEquals(employee.getMailingAddress().getAddressLine1(), employeeInfo.getAddress().getAddress1());
        PayrollServices.rollbackUnitOfWork();

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Application.find(Company.class);
            Company dgDisassociatedCompany1 = companies.get(0);
            dgDisassociatedCompany1.setIsDgDisassociated(Boolean.TRUE);
        } finally {
            PayrollServices.commitUnitOfWork();
        }
        new PTCWebServices().getEmployeeInfo(createEmployeeInfoRequest(employee.getTaxId(), employee.getFirstMiddleLastName()));
    }

    @Test
    /*
    scenario: same employee exists from DIY.  SSN changes on
    assisted EE and so it might find the DIY one which doesn't have all the necessary data.
    */
    public void testGetEmployeeInfoWithMatchingDIY() {
        Employee employee = DataLoadServices.getLastCreatedEmployee(company);
        String oldTaxId = employee.getTaxId();

        PayrollServices.beginUnitOfWork();
        Application.refresh(employee);
        EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.setEmployeeId("SomeStupidDIYDDGuid");
        employeeDTO.setLiveAddress(null); //old DIY doesn't have an address (among other things)
        employeeDTO.setQBDTEmployeeInfoDTO(null);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.employeeManager.addEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), employeeDTO));
        PayrollServices.commitUnitOfWork();

        //update new SSN
        employeeDTO = PayrollServices.dtoFactory.create(employee);
        employeeDTO.setSocialSecurityNumber("991111111");
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.employeeManager.updateEmployee(company.getSourceSystemCd(), company.getSourceCompanyId(), employeeDTO));
        PayrollServices.commitUnitOfWork();

        EmployeeInfo employeeInfo = new PTCWebServices().getEmployeeInfo(createEmployeeInfoRequest(oldTaxId, employee.getFirstMiddleLastName()));
        assertNotNull(employeeInfo.getAddress().getAddress1());
    }

    private EmployeeInfoRequest createEmployeeInfoRequest(String ssn, String employeeName) {
        EmployeeInfoRequest request = new EmployeeInfoRequest();
        request.setPsid(company.getSourceCompanyId());
        request.setSourceSystemCode(company.getSourceSystemCd().toString());
        request.setSsn(ssn);
        request.setEmployeeName(employeeName);
        return request;
    }

    private PSPCompanyRequest createPSPCompanyRequest() {
        PSPCompanyRequest request = new PSPCompanyRequest();
        request.setPsid(company.getSourceCompanyId());
        request.setSourceSystemCode(company.getSourceSystemCd().toString());
        return request;
    }
}
