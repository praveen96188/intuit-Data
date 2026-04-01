package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsDeactivateService;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsDeactivateServiceResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.webservices.EWSAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EditionType;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.Arrays;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/19/13
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeactivateServiceProcessTests {

    @BeforeClass
    public static void beforeClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar newOfferEndDate = SpcfCalendar.createInstance();
        newOfferEndDate.addDays(30);
        offer.setEndDate(newOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @AfterClass
    public static void afterClass() {
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByPromotionId("1099426");
        SpcfCalendar oldOfferEndDate = SpcfCalendar.createInstance(2013, 7, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        offer.setEndDate(oldOfferEndDate);
        Application.save(offer);
        PayrollServices.commitUnitOfWork();
    }

    @Before
    public void startUp() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void cancelVMP() {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
        DataLoadServices.addEntitlementUnit(company, "12345", "12345", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_LOWBASE, null);

        EwsDeactivateService ewsDeactivateVMPService = TestDataFactory.createEwsDeactivateVMPService(company);

        EWSAdapter webservice = new EWSAdapter();
        EwsDeactivateServiceResponse ewsDeactivateServiceResponse = webservice.Deactivate_Service(ewsDeactivateVMPService);
        Assert.assertNotNull("response status", ewsDeactivateServiceResponse.getEwsResponseStatus());
        assertEquals("error code", 0, ewsDeactivateServiceResponse.getEwsResponseStatus().getCode());

        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertFalse(company.getService(ServiceCode.CloudV2).isActive());
        assertFalse(company.getService(ServiceCode.ViewMyPaycheck).isActive());
        Application.rollbackUnitOfWork();

        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.EWS, SourceSystemCode.PSP,
                        TransmissionType.DeactivateService), "v1_10/test_cancelVMP.xml",
                Arrays.asList("DateTimeStamp"));
    }
}
