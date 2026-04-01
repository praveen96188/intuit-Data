package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.common.utils.ServiceKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 14, 2010
 * Time: 10:41:47 AM
 */
public class UpdateEntitlementCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testUpdateEntitlement_Validation() {
        // null dto
        PayrollServices.beginUnitOfWork();
        ProcessResult<Entitlement> entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlement(null);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();

        EntitlementDTO entitlementDTO = new EntitlementDTO();
        entitlementDTO.setLicenseNumber("123");
        entitlementDTO.setEntitlementOfferingCode("1234");

        // entitlement does not exist
        PayrollServices.beginUnitOfWork();
        entitlementMessageProcessResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        PSP_PRAssert.assertContains("validation error", 317, MessageInfo.MessageLevel.ERROR, entitlementMessageProcessResult);
        PayrollServices.rollbackUnitOfWork();
        
    }

    @Test
    public void testUpdateEntitlement_HappyPath() {
        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance(2010, 10, 10);
        String email = "Zack@intuit.com";
        String ccExp = "04/2010";
        String ccNum = "1234";
        String ccType = "VISA";
        String custID = "1234567";
        String orderNum = "1245";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        // add license number and EOC
        DataLoadServices.addEntitlementUnit(company, licenseNumber, entitlementOfferingCode);

        PayrollServices.beginUnitOfWork();
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode));
        entitlementDTO.setContactEmail(email);
        entitlementDTO.setCreditCardExpiration(ccExp);
        entitlementDTO.setCreditCardNumber(ccNum);
        entitlementDTO.setCreditCardType(ccType);
        entitlementDTO.setCustomerId(custID);
        entitlementDTO.setOrderNumber(orderNum);
        entitlementDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
        entitlementDTO.setNextChargeDate(nextChargeDate);
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<Entitlement> entitlementProcessResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        PSP_PRAssert.assertSuccess("update entitlement", entitlementProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        assertEquals("email", email, entitlement.getContactEmail());
        assertEquals("cc exp", ccExp, entitlement.getCreditCardExpiration());
        assertEquals("cc num", ccNum, entitlement.getCreditCardNumber());
        assertEquals("cc type", ccType, entitlement.getCreditCardType());
        assertEquals("cust id", custID, entitlement.getCustomerId());
        assertEquals("order num", orderNum, entitlement.getOrderNumber());
        assertEquals("payment type", EntitlementPaymentMethodType.CC, entitlement.getPaymentMethodType());
        assertEquals("next charge date", nextChargeDate, entitlement.getNextChargeDate());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testUpdateEntitlement_DiskDeliveryKeyUpdate() {
        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        SpcfCalendar nextChargeDate = SpcfCalendar.createInstance(2010, 10, 10);
        String email = "Zack@intuit.com";
        String ccExp = "04/2010";
        String ccNum = "1234";
        String ccType = "VISA";
        String custID = "1234567";
        String orderNum = "1245";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        // add license number and EOC
        DataLoadServices.addDiskDeliveryEntitlementUnit(company, licenseNumber, entitlementOfferingCode);

        PayrollServices.beginUnitOfWork();
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode));
        entitlementDTO.setContactEmail(email);
        entitlementDTO.setCreditCardExpiration(ccExp);
        entitlementDTO.setCreditCardNumber(ccNum);
        entitlementDTO.setCreditCardType(ccType);
        entitlementDTO.setCustomerId(custID);
        entitlementDTO.setOrderNumber(orderNum);
        entitlementDTO.setPaymentMethodType(EntitlementPaymentMethodType.CC);
        entitlementDTO.setNextChargeDate(nextChargeDate);
        PayrollServices.rollbackUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<Entitlement> entitlementProcessResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        PSP_PRAssert.assertSuccess("update entitlement", entitlementProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        assertEquals("email", email, entitlement.getContactEmail());
        assertEquals("cc exp", ccExp, entitlement.getCreditCardExpiration());
        assertEquals("cc num", ccNum, entitlement.getCreditCardNumber());
        assertEquals("cc type", ccType, entitlement.getCreditCardType());
        assertEquals("cust id", custID, entitlement.getCustomerId());
        assertEquals("order num", orderNum, entitlement.getOrderNumber());
        assertEquals("payment type", EntitlementPaymentMethodType.CC, entitlement.getPaymentMethodType());
        assertEquals("next charge date", nextChargeDate, entitlement.getNextChargeDate());

        assertEquals("service key event", 3, CompanyEvent.getEventCountByType(company, EventTypeCode.ServiceKeyUpdated));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testUpdateEntitlement_UpdateFromDummy() {
        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        // add license number and EOC
        EntitlementUnit entitlementUnit = DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, null, null);

        PayrollServices.beginUnitOfWork();
        assertNotNull("Service Key", entitlementUnit.getServiceKey());
        assertEquals("service key event", 2, CompanyEvent.getEventCountByType(company, EventTypeCode.ServiceKeyUpdated));
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode));
        entitlementDTO.setNumberOfEmployeesType(NumberOfEmployeesType.UPTO3);
        entitlementDTO.setEditionType(EditionType.Basic);
        entitlementDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.updateEntitlement(entitlementDTO);

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        assertEquals("number of employees", NumberOfEmployeesType.UPTO3, entitlement.getEntitlementCode().getNumberOfEmployeesType());
        assertEquals("Edition", EditionType.Basic, entitlement.getEntitlementCode().getEditionType());
        for (EntitlementUnit unit : entitlement.getEntitlementUnitCollection()) {
            assertNotNull("Service Key is null", unit.getServiceKey());
        }
        assertEquals("service key event", 2, CompanyEvent.getEventCountByType(company, EventTypeCode.ServiceKeyUpdated));
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPendingDeactivationToDeactivationHold() {
        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        EntitlementUnit entitlementUnit = entitlement.getEntitlementUnitCollection().get(0);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
        Application.save(entitlementUnit);

        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlement);
        entitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Entitlement> entitlementProcessResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        PSP_PRAssert.assertSuccess("update entitlement", entitlementProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        entitlementUnit = entitlement.getEntitlementUnitCollection().get(0);

        assertEquals("entitlement status", EntitlementStateCode.Disabled, entitlement.getEntitlementState());
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.DeactivationHold, entitlementUnit.getEntitlementUnitStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDeactivationHoldToPendingDeactivation() {
        String licenseNumber = "12345678901234567890";
        String entitlementOfferingCode = "09876543210987654321";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        EntitlementUnit entitlementUnit = entitlement.getEntitlementUnitCollection().get(0);
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        Application.save(entitlementUnit);

        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlement);
        entitlementDTO.setEntitlementState(EntitlementStateCode.Enabled);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<Entitlement> entitlementProcessResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        PSP_PRAssert.assertSuccess("update entitlement", entitlementProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        entitlementUnit = entitlement.getEntitlementUnitCollection().get(0);

        assertEquals("entitlement status", EntitlementStateCode.Enabled, entitlement.getEntitlementState());
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingDeactivation, entitlementUnit.getEntitlementUnitStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUpdateEntitlement_Disabled() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        EntitlementUnit entitlementUnit = assertOne(company.getEntitlementUnitCollection());
        Entitlement entitlement = entitlementUnit.getEntitlement();
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        Application.save(entitlementUnit);

        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlement);
        entitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        ProcessResult<Entitlement> entitlementProcessResult = PayrollServices.entitlementManager.updateEntitlement(entitlementDTO);
        PSP_PRAssert.assertSuccess("update entitlement", entitlementProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        entitlementUnit = assertOne(company.getEntitlementUnitCollection());
        entitlement = entitlementUnit.getEntitlement();

        assertEquals("entitlement status", EntitlementStateCode.Disabled, entitlement.getEntitlementState());
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());

        CompanyService companyService = entitlementUnit.getCompany().getCompanyService(ServiceCode.Tax);
        assertEquals("tax service status", ServiceSubStatusCode.Cancelled, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test7000ServiceKeyNotChangedTo4000ServiceKeyIfInputsUnchanged() {
        //verifies PSRV003573

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        //PSP cannot generate 7000 SK so must update directly to simulate DM
        Application.beginUnitOfWork();
        Application.refresh(company);
        EntitlementUnit entitlementUnit = assertOne(company.getEntitlementUnitCollection());
        Application.refresh(entitlementUnit);
        String sevenThousandSK = new ServiceKey(company.getFedTaxId(), entitlementUnit.getEntitlement().getSubscriptionNumber(), ServiceKey.ServiceType.SVC_PREMIUM_DD).getApplicationNumber();
        assertTrue(sevenThousandSK.startsWith("7"));
        entitlementUnit.setServiceKey(sevenThousandSK);
        Application.save(entitlementUnit);
        Application.commitUnitOfWork();

        //represent an entitlement update with nothing changing (like AMO message we don't care about)
        PayrollServices.beginUnitOfWork();
        Application.refresh(entitlementUnit);
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlementUnit.getEntitlement());
        Application.rollbackUnitOfWork();

        Entitlement entitlement = DataLoadServices.updateEntitlement(entitlementDTO);


        PayrollServices.beginUnitOfWork();
        Application.refresh(entitlement);
        entitlementUnit = assertOne(entitlement.getEntitlementUnitCollection());

        assertEquals(sevenThousandSK, entitlementUnit.getServiceKey());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAssistedServiceCancelledWhenEntitlementDisabled() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertTrue(company.isCompanyOnService(ServiceCode.Tax));

        EntitlementUnit entitlementUnit = assertOne(company.getEntitlementUnitCollection());
        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlementUnit.getEntitlement());
        entitlementDTO.setEntitlementState(EntitlementStateCode.Disabled);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.updateEntitlement(entitlementDTO);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertFalse(company.isCompanyOnService(ServiceCode.Tax));
        PayrollServices.rollbackUnitOfWork();


    }
}
