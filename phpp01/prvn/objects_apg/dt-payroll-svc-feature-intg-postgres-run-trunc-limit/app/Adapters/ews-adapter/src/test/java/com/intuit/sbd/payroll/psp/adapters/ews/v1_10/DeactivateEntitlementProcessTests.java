package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsCreateAccount;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsCreateAccountResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsDeactivateEntitlement;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.EwsResponse;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEditionType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsTierType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.CreateAccountProcess;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.DeactivateEntitlementProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Offer;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * User: praveenkumarh635
 */
public class DeactivateEntitlementProcessTests {

    EwsDeactivateEntitlement mRequest;
    EwsResponse mResponse;
    String psid = null;
    EwsCreateAccount createAccRequest;
    EwsCreateAccountResponse createAccResponse;


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
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
    }

    /**
     *  Test with 3 EINs on a single Subscription Number.
     *  And also the case where an invalid EIN is present in the EINList
     *  The DeactivateEntitlement service deactivates all the EINs in the list or none.
     *  In case, any of the EINs fails a validation/ is invalid, None of the EINs will be deactivated.
     */
    @Test
    public void testDeactivateEntitlement(){
        ArrayList<String> einList = new ArrayList<String>();
        String subscriptionNumber;
        createAccRequest = TestDataFactory.createEwsCreateAccount();

        createAccRequest.getEwsEntitlements().get(0).setEdition(EwsEditionType.Standard);
        createAccRequest.getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        createAccRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099574");

        CreateAccountProcess process = new CreateAccountProcess(createAccRequest, false);
        createAccResponse = process.execute();

        Assert.assertNotNull(createAccResponse);
        einList.add(createAccResponse.getCompanyResponse().getEin());

        createAccRequest .getEwsCompany().setEin("876543211");


        createAccRequest .getEwsEntitlements().get(0).setEdition(EwsEditionType.Standard);
        createAccRequest .getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        createAccRequest .getEwsEntitlements().get(0).setAssetItemNumber("1099574");
        createAccRequest .getEwsEntitlements().get(0).setEwsBillingDetails(null);
        createAccRequest .getEwsEntitlements().get(0).setAddEin(true);

        CreateAccountProcess process2 = new CreateAccountProcess(createAccRequest , false);
        createAccResponse = process2.execute();

        Assert.assertNotNull(createAccResponse);
        einList.add(createAccResponse.getCompanyResponse().getEin());

        createAccRequest .getEwsCompany().setEin("876543210");

        createAccRequest .getEwsEntitlements().get(0).setEdition(EwsEditionType.Standard);
        createAccRequest .getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        createAccRequest .getEwsEntitlements().get(0).setAssetItemNumber("1099574");
        createAccRequest .getEwsEntitlements().get(0).setEwsBillingDetails(null);
        createAccRequest .getEwsEntitlements().get(0).setAddEin(true);

        CreateAccountProcess process3 = new CreateAccountProcess(createAccRequest , false);
        createAccResponse = process3.execute();

        Assert.assertNotNull(createAccResponse);

        einList.add(createAccResponse.getCompanyResponse().getEin());



        createAccRequest .getEwsCompany().setEin("876543212");


        createAccRequest .getEwsEntitlements().get(0).setEdition(EwsEditionType.Standard);
        createAccRequest .getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        createAccRequest .getEwsEntitlements().get(0).setAssetItemNumber("1099574");
        createAccRequest .getEwsEntitlements().get(0).setEwsBillingDetails(null);
        createAccRequest .getEwsEntitlements().get(0).setAddEin(false);

        CreateAccountProcess process4 = new CreateAccountProcess(createAccRequest , false);
        createAccResponse = process4.execute();

        Assert.assertNotNull(createAccResponse);


        einList.add(createAccResponse.getCompanyResponse().getEin());

        subscriptionNumber = createAccResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber();
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList,subscriptionNumber);
        Application.commitUnitOfWork();

        DeactivateEntitlementProcess  deactivateEntitlementProcess = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());

        //Deactivated Ein
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList, subscriptionNumber);
        Application.commitUnitOfWork();
        DeactivateEntitlementProcess  deactivateEntitlementProcess2 = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess2.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals(30166, mResponse.getEwsResponseStatus().getCode());

        // One of the Eins in the einList is null
        einList.add(0, null);
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList, subscriptionNumber);
        Application.commitUnitOfWork();
        DeactivateEntitlementProcess  deactivateEntitlementProcess6 = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess6.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals(30006, mResponse.getEwsResponseStatus().getCode());


        // No Eins in the einList
        einList.clear();
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList, subscriptionNumber);
        Application.commitUnitOfWork();
        DeactivateEntitlementProcess  deactivateEntitlementProcess3 = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess3.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals(30004, mResponse.getEwsResponseStatus().getCode());


        //Invalid Ein in the einList
        einList.clear();
        einList.add("9980716");
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList, subscriptionNumber);
        Application.commitUnitOfWork();
        DeactivateEntitlementProcess  deactivateEntitlementProcess4 = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess4.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals(30005, mResponse.getEwsResponseStatus().getCode());

        //Ein does not belong to this Subscription Number
        einList.clear();
        einList.add("966348318");
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList, subscriptionNumber);
        Application.commitUnitOfWork();
        DeactivateEntitlementProcess  deactivateEntitlementProcess5 = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess5.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals(30165, mResponse.getEwsResponseStatus().getCode());

    }

    @Test
    public void testDeactivationOfAssistedCompany(){
        //Ein is an Assisted plan

        ArrayList<String> einList = new ArrayList<String>();
        String subscriptionNumber;
        createAccRequest = TestDataFactory.createEwsCreateAccountAssisted();

        createAccRequest.getEwsEntitlements().get(0).setEdition(null);
        createAccRequest.getEwsEntitlements().get(0).setTier(null);
        createAccRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099750");             //Assisted
        createAccRequest .getEwsEntitlements().get(0).setEwsBillingDetails(null);
        createAccRequest .getEwsEntitlements().get(0).setAddEin(true);

        CreateAccountProcess process = new CreateAccountProcess(createAccRequest, false);
        createAccResponse = process.execute();

        Assert.assertNotNull(createAccResponse);
        einList.add(createAccResponse.getCompanyResponse().getEin());
        subscriptionNumber = createAccResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber();
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList, subscriptionNumber);
        Application.commitUnitOfWork();
        DeactivateEntitlementProcess  deactivateEntitlementProcess4 = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess4.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals(30164, mResponse.getEwsResponseStatus().getCode());
    }

    @Test
    public void testDeactivationOfAssistedAdvantageCompany(){
        //Ein is a AssistedAdvantage plan

        ArrayList<String> einList = new ArrayList<String>();
        String subscriptionNumber;
        createAccRequest = TestDataFactory.createEwsCreateAccountAssisted();

        createAccRequest.getEwsEntitlements().get(0).setEdition(null);
        createAccRequest.getEwsEntitlements().get(0).setTier(null);
        createAccRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099753");             //Assisted Advantage
        createAccRequest .getEwsEntitlements().get(0).setEwsBillingDetails(null);
        createAccRequest .getEwsEntitlements().get(0).setAddEin(true);

        CreateAccountProcess process = new CreateAccountProcess(createAccRequest, false);
        createAccResponse = process.execute();

        Assert.assertNotNull(createAccResponse);
        einList.add(createAccResponse.getCompanyResponse().getEin());
        subscriptionNumber = createAccResponse.getEwsEntitlementUnitResponses().get(0).getEwsEntitlementResponse().getSubscriptionNumber();
        Application.beginUnitOfWork();

        mRequest = TestDataFactory.createEwsDeactivateEntitlementRequest(einList, subscriptionNumber);
        Application.commitUnitOfWork();
        DeactivateEntitlementProcess  deactivateEntitlementProcess4 = new DeactivateEntitlementProcess(mRequest);
        mResponse = deactivateEntitlementProcess4.execute();

        Assert.assertNotNull(mResponse);
        Assert.assertNotNull(mResponse.getEwsResponseStatus());
        Assert.assertEquals(30164, mResponse.getEwsResponseStatus().getCode());
    }
}
