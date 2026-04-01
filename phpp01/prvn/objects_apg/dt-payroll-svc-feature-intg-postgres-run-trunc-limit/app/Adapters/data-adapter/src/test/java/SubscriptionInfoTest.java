import com.intuit.ems.psp.adapters.dataadapter.dto.PayrollStatus;
import com.intuit.ems.psp.adapters.dataadapter.dto.RequestHeader;
import com.intuit.ems.psp.adapters.dataadapter.service.SubscriptionInfoResource;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Calendar;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: sshetty
 * Date: 8/30/15
 * Time: 9:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionInfoTest {
    @Before
    public void startup(){
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.beforeEachTest();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
    }


    @Test
    public void testHappyPath() throws Exception{
        Company company =setupCompany("123456789", "734780", "9245-8601-6933-296");
        SubscriptionInfoResource subscriptionInfoResource = new SubscriptionInfoResource();

        RequestHeader requestHeader = new RequestHeader();
       // requestHeader.setUUID("9898-98u8-98-989");
        requestHeader.setMinorVersion("8");
        requestHeader.setMajorVersion("25");
        requestHeader.setQBLicense("9245-8601-6933-296");

        String intuit_tid = "98789-8889-989";
        Response response =  subscriptionInfoResource.getQbLicenseInfo(intuit_tid,requestHeader);
        assertEquals(200, response.getStatus());
        com.intuit.ems.psp.adapters.dataadapter.dto.PayrollStatus payrollStatus = (PayrollStatus) response.getEntity();
        assertNotNull(payrollStatus);
        assertNotNull(response.getMetadata());


        assertEquals("response tid should be same as the request tid", intuit_tid, response.getMetadata().getFirst("intuit_tid"));
        assertEquals("License Number should be same as the request", requestHeader.getQBLicense(), payrollStatus.getQBLicense());
        assertEquals("Status should be Enabled", "Enabled", payrollStatus.getStatus());
        assertNull("SubScription ENd date should be null", payrollStatus.getEndDate());

        requestHeader.setMinorVersion("8");
        requestHeader.setMajorVersion("25");
        requestHeader.setQBLicense("1111-1111-1111-111");

         response =  subscriptionInfoResource.getQbLicenseInfo(intuit_tid, requestHeader);

        assertEquals(200, response.getStatus());
        payrollStatus = (PayrollStatus) response.getEntity();
        assertNotNull(payrollStatus);

        assertEquals("response tid should be same as the request tid", intuit_tid, response.getMetadata().getFirst("intuit_tid"));
        assertEquals("License Number should be same as the request", requestHeader.getQBLicense(), payrollStatus.getQBLicense());
        assertEquals("Status should be Disabled", "Disabled", payrollStatus.getStatus());
        assertNull("SubScription ENd date should be null", payrollStatus.getEndDate());

    }

//
//    @Test
//    public void testInvalidLicenseVersion() throws Exception {
//
//        SubscriptionInfoResource subscriptionInfoResource = new SubscriptionInfoResource();
//        String intuit_tid = "7887-9889-989-889";
//
//        Company company =setupCompany("123456789", "734780", "9245-8601-6933-296");
//
//        RequestHeader requestHeader = new RequestHeader();
//        // requestHeader.setUUID("9898-98u8-98-989");
//        requestHeader.setMinorVersion("8");
//        requestHeader.setMajorVersion("25");
//
//        Response response =  subscriptionInfoResource.getQbLicenseInfo(intuit_tid, requestHeader);
//
//        assertEquals(400, response.getStatus());
//        assertTrue("Constaint failure should be displayed.", response.getEntity().toString().contains("Constraint Violations: "));
//
//        requestHeader.setMinorVersion("8");
//        requestHeader.setMajorVersion("25");
//        requestHeader.setQBLicense("");
//
//        response =  subscriptionInfoResource.getQbLicenseInfo(intuit_tid, requestHeader);
//
//        assertEquals(400, response.getStatus());
//        assertTrue("Constaint failure should be displayed.", response.getEntity().toString().contains("Constraint Violations: "));
//
//        requestHeader.setMinorVersion("8");
//        requestHeader.setMajorVersion("2");
//        requestHeader.setQBLicense("XXXX-XXXX-XXXX-XXX");
//
//        response =  subscriptionInfoResource.getQbLicenseInfo(intuit_tid, requestHeader);
//        PayrollStatus status = (PayrollStatus) response.getEntity();
//
//        assertEquals(200, response.getStatus());
//        assertEquals("Status should be Disabled", "Disabled", status.getStatus());
//    }
    @Test
    public void testMultipleEntitlementUnits() throws Exception{
        Company company = setupCompany("123456789", "734780", "9245-8601-6933-296");

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);
        assertEquals("Entitlement Unit status should be deactivated", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());

        PayrollServices.rollbackUnitOfWork();

        SubscriptionInfoResource subscriptionInfoResource = new SubscriptionInfoResource();
        RequestHeader requestHeader = new RequestHeader();
        requestHeader.setMinorVersion("8");
        requestHeader.setMajorVersion("25");
        requestHeader.setQBLicense("9245-8601-6933-296");

        Response response = subscriptionInfoResource.getQbLicenseInfo("XXX", requestHeader);

        // The QB license number has only one company that is disabled, Hence the ratable service should return Disabled status
        assertEquals("Status should be Disabled", "Disabled", ((PayrollStatus) response.getEntity()).getStatus());

        Company company1 = setupCompany("123456787", "734780", "9242-8601-6933-296");
        Application.beginUnitOfWork();
        company1 = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        EntitlementUnit entitlementUnit1 = company1.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();

        SpcfUniqueId entitlementId1 = entitlementUnit.getEntitlement().getId();
        SpcfUniqueId entitlementId2 = entitlementUnit1.getEntitlement().getId();
        assertTrue("Both the companies should be in same entitlement", entitlementId1.toString().equals(entitlementId2.toString()));
        assertEquals("Entitlement Unit status should be activated", EntitlementUnitStatusCode.PendingActivation, entitlementUnit1.getEntitlementUnitStatus());

        response = subscriptionInfoResource.getQbLicenseInfo("XXX", requestHeader);
        //The status should still be disabled
        assertEquals("Status should be Disabled", "Disabled", ((PayrollStatus)response.getEntity()).getStatus());

        subscriptionInfoResource = new SubscriptionInfoResource();
        requestHeader = new RequestHeader();
        requestHeader.setMinorVersion("8");
        requestHeader.setMajorVersion("25");
        requestHeader.setQBLicense("9242-8601-6933-296");

        response = subscriptionInfoResource.getQbLicenseInfo("XXX", requestHeader);
        assertEquals("Status should be Enabled", "Enabled", ((PayrollStatus)response.getEntity()).getStatus());


        Company company2 = setupCompany("123456788", "734780", "9245-8601-6933-296");
        Application.beginUnitOfWork();
        company2 = Company.findCompany(company2.getSourceCompanyId(), company2.getSourceSystemCd());
        entitlementUnit = company2.getActivePrimaryEntitlementUnit();
        Application.commitUnitOfWork();
        assertEquals("Entitlement Unit status should be activated", EntitlementUnitStatusCode.PendingActivation, entitlementUnit.getEntitlementUnitStatus());

        response = subscriptionInfoResource.getQbLicenseInfo("XXX", requestHeader);
        assertEquals("Status should be Enabled", "Enabled", ((PayrollStatus)response.getEntity()).getStatus());

    }



    private Company setupCompany(String pPsid, String pAppVersion, String pLicenseNum ) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPsid, true, ServiceCode.Cloud.Cloud);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, "13456879"  , "123456", EditionType.Basic,
                                                                              NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());

        Application.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);

        company.getQuickbooksInfo().setApplicationVersion(pAppVersion);
        company.getQuickbooksInfo().setLicenseNumber(pLicenseNum);
        Application.save(company.getQuickbooksInfo());
        Application.commitUnitOfWork();
        return company;
    }
}
