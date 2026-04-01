package com.intuit.sbd.payroll.psp.adapters.ews.v1_10;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.*;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.processes.CreateAccountProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 4/10/13
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomerCare {

    //*******************************************************************************************************
    //Run these tests against QA1, QA4, and QA6 to reload accounts used by Compliance and Customer Care Teams
    //*******************************************************************************************************

    private EwsCreateAccount mRequest;
    private EwsCreateAccountResponse mResponse;

    @Before
    public void beforeEachTest() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EWSAdapter));
    }

    @Ignore
    @Test
    public void createDiyBasic3() throws Exception {
        deleteCompany("9999990", "9999990");

        mRequest = createEwsCreateAccount();

        mRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099581");
        mRequest.getEwsEntitlements().get(0).setEdition(EwsEditionType.Basic);
        mRequest.getEwsEntitlements().get(0).setTier(EwsTierType.UpTo3);
        mRequest.getEwsEntitlements().get(0).setLicenseNumber("9999990");
        mRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("9999990");
        mRequest.getEwsCompany().setDba("Diy Basic 3");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        assertEquals(0, mResponse.getEwsResponseStatus().getCode());
        assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(company.getActivePrimaryEntitlementUnit().getEntitlement());
        entitlementDTO.setSubscriptionNumber("9999990");
        ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        assertTrue(processResult.isSuccess());
        EntitlementUnit entitlementUnit = processResult.getResult().getActiveEntitlementUnitCollection().getFirst();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        entitlementUnit = Application.save(entitlementUnit);

        assertEquals("9999990", entitlementUnit.getEntitlement().getSubscriptionNumber());
        assertEquals("4010-8881-0007-0751", entitlementUnit.getServiceKey());
        assertNull(entitlementUnit.getExtensionKey());
        PayrollServices.commitUnitOfWork();
    }

    @Ignore
    @Test
    public void createDiyEnhanced3AccountantEoAndEr() {
        deleteCompany("9999991", "9999991");

        mRequest = createEwsCreateAccount();

        mRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099581");
        mRequest.getEwsEntitlements().get(0).setEdition(EwsEditionType.EnhancedAccountant);
        mRequest.getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        mRequest.getEwsEntitlements().get(0).setLicenseNumber("9999991");
        mRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("9999991");
        mRequest.getEwsCompany().setDba("Diy Enhanced 3 Accountant EO & ER");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        assertEquals(0, mResponse.getEwsResponseStatus().getCode());
        assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(company.getActivePrimaryEntitlementUnit().getEntitlement());
        entitlementDTO.setSubscriptionNumber("9999991");
        ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        assertTrue(processResult.isSuccess());
        EntitlementUnit entitlementUnit = processResult.getResult().getActiveEntitlementUnitCollection().getFirst();

        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();
        entitlementUnitDTO.setAssetItemNumber("1099598");
        entitlementUnitDTO.setFedTaxId("991111111");
        entitlementUnitDTO.setEntitlementOfferingCode("99999910");
        entitlementUnitDTO.setLicenseNumber("99999910");
        entitlementUnitDTO.setSubscriptionNumber("9999994");
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Enabled);
        entitlementUnitDTO.setNextChargeDate(SpcfCalendar.createInstance(2020, 12, 31, SpcfTimeZone.getLocalTimeZone()));
        entitlementUnitDTO.setCustomerId("1234567890");
        ProcessResult<EntitlementUnit> processResult1 = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(processResult1.isSuccess());
        EntitlementUnit entitlementUnitEO = processResult1.getResult();

        entitlementUnitDTO = new EntitlementUnitDTO();
        entitlementUnitDTO.setAssetItemNumber("1099597");
        entitlementUnitDTO.setFedTaxId("991111111");
        entitlementUnitDTO.setEntitlementOfferingCode("99999911");
        entitlementUnitDTO.setLicenseNumber("99999911");
        entitlementUnitDTO.setSubscriptionNumber("9999995");
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Enabled);
        entitlementUnitDTO.setNextChargeDate(SpcfCalendar.createInstance(2020, 12, 31, SpcfTimeZone.getLocalTimeZone()));
        entitlementUnitDTO.setCustomerId("1234567890");
        processResult1 = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        assertTrue(processResult1.isSuccess());
        EntitlementUnit entitlementUnitER = processResult1.getResult();

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        entitlementUnit = Application.save(entitlementUnit);

        assertEquals("9999991", entitlementUnit.getEntitlement().getSubscriptionNumber());
        assertEquals("4010-8880-0007-9751", entitlementUnit.getServiceKey());
        assertNull(entitlementUnit.getExtensionKey());

        assertEquals("9999994", entitlementUnitEO.getEntitlement().getSubscriptionNumber());
        assertEquals("D010-8893-0007-2751", entitlementUnitEO.getServiceKey());
        assertNull(entitlementUnitEO.getExtensionKey());

        assertEquals("9999995", entitlementUnitER.getEntitlement().getSubscriptionNumber());
        assertEquals("E010-8892-0007-2751", entitlementUnitER.getServiceKey());
        assertNull(entitlementUnitER.getExtensionKey());

        PayrollServices.commitUnitOfWork();
    }

    @Ignore
    @Test
    public void createDiyStandard() {
        deleteCompany("9999992", "9999992");

        mRequest = createEwsCreateAccount();

        mRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099581");
        mRequest.getEwsEntitlements().get(0).setEdition(EwsEditionType.Standard);
        mRequest.getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        mRequest.getEwsEntitlements().get(0).setLicenseNumber("9999992");
        mRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("9999992");
        mRequest.getEwsCompany().setDba("Diy Standard");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        assertEquals(0, mResponse.getEwsResponseStatus().getCode());
        assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(company.getActivePrimaryEntitlementUnit().getEntitlement());
        entitlementDTO.setSubscriptionNumber("9999992");
        ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        assertTrue(processResult.isSuccess());
        EntitlementUnit entitlementUnit = processResult.getResult().getActiveEntitlementUnitCollection().getFirst();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        entitlementUnit = Application.save(entitlementUnit);

        assertEquals("9999992", entitlementUnit.getEntitlement().getSubscriptionNumber());
        assertEquals("4010-8895-0007-5751", entitlementUnit.getServiceKey());
        assertNull(entitlementUnit.getExtensionKey());
        PayrollServices.commitUnitOfWork();
    }

    @Ignore
    @Test
    public void createDiyDiskDeliveryEnhanced () {
        deleteCompany("9999993", "9999993");

        mRequest = createEwsCreateAccount();

        mRequest.getEwsEntitlements().get(0).setAssetItemNumber("1099574");
        mRequest.getEwsEntitlements().get(0).setEdition(EwsEditionType.Enhanced);
        mRequest.getEwsEntitlements().get(0).setTier(EwsTierType.Unlimited);
        mRequest.getEwsEntitlements().get(0).setLicenseNumber("9999993");
        mRequest.getEwsEntitlements().get(0).setEntitlementOfferingCode("9999993");
        mRequest.getEwsCompany().setDba("Diy Disk Delivery Enhanced");

        CreateAccountProcess process = new CreateAccountProcess(mRequest, false);
        mResponse = process.execute();

        assertNotNull(mResponse);

        assertNotNull(mResponse.getEwsResponseStatus());
        assertEquals(0, mResponse.getEwsResponseStatus().getCode());
        assertEquals("Success", mResponse.getEwsResponseStatus().getMessage());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mResponse.getPsid(), SourceSystemCode.QBDT);
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(company.getActivePrimaryEntitlementUnit().getEntitlement());
        entitlementDTO.setSubscriptionNumber("9999993");
        ProcessResult<Entitlement> processResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        assertTrue(processResult.isSuccess());
        EntitlementUnit entitlementUnit = processResult.getResult().getActiveEntitlementUnitCollection().getFirst();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.refresh(entitlementUnit);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        entitlementUnit = Application.save(entitlementUnit);

        assertEquals("9999993", entitlementUnit.getEntitlement().getSubscriptionNumber());
        assertEquals("8010-8894-0007-8751", entitlementUnit.getServiceKey());
        //assertEquals("1823-6045-5462", entitlementUnit.getExtensionKey());
        PayrollServices.commitUnitOfWork();
    }

    private void deleteCompany(String pLicenseNumber, String pEOC) {
        try {
            PayrollServices.beginUnitOfWork();
            Entitlement entitlement = Entitlement.findEntitlement(pLicenseNumber, pEOC);
            for (EntitlementUnit entitlementUnit : entitlement.getEntitlementUnitCollection()) {
                Company company = entitlementUnit.getCompany();
                if (company != null) {
                    Application.deleteCompany(company.getId().toString());
                }
            }
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            //Do nothing
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private static EwsCreateAccount createEwsCreateAccount() {
        EwsCreateAccount ewsCreateAccount = new EwsCreateAccount();

        EwsCompany ewsCompany = new EwsCompany();
        ewsCreateAccount.setEwsCompany(ewsCompany);

        ewsCompany.setEin("991111111");
        ewsCompany.setRealmId("1234567890");
        ewsCompany.setClientPacketDeliveryPreference(EwsDeliveryType.mail);
        ewsCompany.setW2DeliveryPreference(EwsDeliveryType.mail);

        EwsContact payrollAdmin = new EwsContact();
        ewsCompany.setPayrollAdmin(payrollAdmin);

        payrollAdmin.setFirstName("Payroll");
        payrollAdmin.setLastName("Admin");
        payrollAdmin.seteMail("PayrollAdmin@intuit.com");
        payrollAdmin.setWorkPhone("775-332-8800");
        payrollAdmin.setAuthenticationId("12345");

        EwsContact primaryPrincipal = new EwsContact();
        ewsCompany.setPrimaryPrincipal(primaryPrincipal);

        primaryPrincipal.setFirstName("Primary");
        primaryPrincipal.setLastName("Principal");
        primaryPrincipal.seteMail("PrimaryPrincipal@intuit.com");
        primaryPrincipal.setWorkPhone("775-332-8800");
        primaryPrincipal.setAuthenticationId("67890");

        EwsQuickBooks quickBooks = new EwsQuickBooks();
        ewsCompany.setQuickBooks(quickBooks);

        quickBooks.setAppVersion("22.00.R.0/20716#professional");
        quickBooks.setLicenseNumber("1234-56789-0123-456");

        ewsCompany.setDba("Acme Software");

        EwsAddress ewsAddress = new EwsAddress();
        ewsCompany.setMailingAddress(ewsAddress);

        ewsAddress.setAddressLine1("6884 Sierra Center Pkwy");
        ewsAddress.setCity("Reno");
        ewsAddress.setState("NV");
        ewsAddress.setZip("89511");

        ArrayList<EwsEntitlement> ewsEntitlements = new ArrayList<EwsEntitlement>();
        ewsCreateAccount.setEwsEntitlements(ewsEntitlements);

        EwsEntitlement ewsEntitlement = new EwsEntitlement();
        ewsEntitlements.add(ewsEntitlement);

        ewsEntitlement.setBuyerEmailAddress("Buyer@intuit.com");
        ewsEntitlement.setBillingAccountId("1234567890");
        ewsEntitlement.setAddEin(false);

        EwsBillingDetails ewsBillingDetails = new EwsBillingDetails();
        ewsEntitlement.setEwsBillingDetails(ewsBillingDetails);

        Calendar nextChargeDate = CalendarUtils.convertToCalendar(SpcfCalendar.createInstance(2020, 12, 31, SpcfTimeZone.getLocalTimeZone()));
        ewsBillingDetails.setSubscriptionNextBillDate(nextChargeDate);
        ewsBillingDetails.setPaymentMethod(EwsPaymentMethod.CC);
        ewsBillingDetails.setCreditCardNumber("1111");
        ewsBillingDetails.setCreditCardType(EwsCreditCardType.VISA);
        ewsBillingDetails.setCreditCardExp("12/2020");

        EwsServices ewsServices = new EwsServices();
        ewsCreateAccount.setEwsServices(ewsServices);

        ewsServices.setCloudService(new EwsBaseService());

        ewsCreateAccount.setIpAddress("127.0.0.1");
        ewsCreateAccount.setForceRandomDollar(true);

        ewsCreateAccount.setDateTimeStamp(Calendar.getInstance());

        return ewsCreateAccount;
    }
}
