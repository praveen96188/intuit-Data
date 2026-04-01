package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertContains;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 28, 2010
 * Time: 3:44:33 PM
 */
public class AddOrUpdateEntitlementUnitCoreTests {

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
    public void testValidationErrors() {
        // null dto
        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementUnit> entitlementUnitProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                null, null, null);
        PSP_PRAssert.assertContains("validation error", 5001, MessageInfo.MessageLevel.ERROR, entitlementUnitProcessResult);
        PayrollServices.rollbackUnitOfWork();

        // company does not exist
        PayrollServices.beginUnitOfWork();
        entitlementUnitProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                SourceSystemCode.QBDT, "123", new EntitlementUnitDTO());
        PSP_PRAssert.assertContains("validation error", 169, MessageInfo.MessageLevel.ERROR, entitlementUnitProcessResult);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAddUpdateLicenseNumberEOC() {
        AssetItemCode assetItemCode = AssetItemCode.DIY;

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnit entitlementunit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode), company.getFedTaxId());
        EntitlementCode entitlementCode = entitlementunit.getEntitlement().getEntitlementCode();

        SpcfCalendar nextChargeDate = PSPDate.getPSPTime().copy();
        nextChargeDate.addDays(AddEntitlementCore.DEFAULT_NEXT_CHARGE_DATE_BUFFER_DAYS);
        CalendarUtils.clearTime(nextChargeDate);

        assertEquals("license number", licenseNumber, entitlementunit.getEntitlement().getLicenseNumber());
        assertEquals("EOC", entitlementOfferingCode, entitlementunit.getEntitlement().getEntitlementOfferingCode());
        assertEquals("assetItemCode", assetItemCode, entitlementunit.getEntitlement().getEntitlementCode().getAssetItemCd());
        SpcfCalendar spcfCalendar = entitlementunit.getEntitlement().getNextChargeDate().copy();
        CalendarUtils.clearTime(spcfCalendar);
        assertEquals("Next Charge date", nextChargeDate.toString(), spcfCalendar.toString());
        assertEquals("FEIN", company.getFedTaxId(), entitlementunit.getFedTaxId());
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.PendingActivation, entitlementunit.getEntitlementUnitStatus());
        assertEquals("entitlement state", EntitlementStateCode.Enabled, entitlementunit.getEntitlement().getEntitlementState());
        assertNotNull("subscription Number", entitlementunit.getEntitlement().getSubscriptionNumber());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode), company.getFedTaxId()));

        // update entitlement unit status
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);

        ProcessResult<EntitlementUnit> EntitlementUnitProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertSuccess("update entitlement", EntitlementUnitProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        entitlementunit = company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode), company.getFedTaxId());
        assertEquals("entitlement status", EntitlementUnitStatusCode.Activated, entitlementunit.getEntitlementUnitStatus());
        assertEquals("entitlement status changed event", 2, CompanyEvent.getEventCountByType(company, EventTypeCode.EntitlementUnitStatusChanged));
        assertEquals("service key event", 1, CompanyEvent.getEventCountByType(company, EventTypeCode.ServiceKeyUpdated));
        assertTrue("Default BDOM value", entitlementunit.getEntitlement().getBillingDayOfMonth() == 4);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testGenerateServiceKey_EO_ER_No_Emails() {
        String licenseNumber = "12345678901234567890";
        String eoEntitlementOfferingCode = "09876543210987654321";
        String erEntitlementOfferingCode = "09876543210987654322";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        // add license number and EOC
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYEE_ORGANIZER, null);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, erEntitlementOfferingCode, null, null, DataLoadServices.AssetItemNumber.EMPLOYMENT_REGULATION, null);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, eoEntitlementOfferingCode), company.getFedTaxId()));
        // update entitlement unit status
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        assertSuccess(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO));

        entitlementUnitDTO = PayrollServices.dtoFactory.create(company.getEntitlementUnit(Entitlement.findEntitlement(licenseNumber, erEntitlementOfferingCode), company.getFedTaxId()));
        // update entitlement unit status
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        assertSuccess(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<EntitlementUnit> primaryEntitlements = company.getPrimaryEntitlementUnits();
        assertEquals("entitlement status changed event", 5, CompanyEvent.getEventCountByType(company, EventTypeCode.EntitlementUnitStatusChanged));
        assertEquals("service key event", 3, CompanyEvent.getEventCountByType(company, EventTypeCode.ServiceKeyUpdated));
        assertEquals("service key emails", primaryEntitlements.size(), Application.find(CompanyEventEmail.class, CompanyEventEmail.CompanyEvent().Company().equalTo(company)
                                                                                                                                  .And(CompanyEventEmail.CompanyEvent().EventTypeCd().equalTo(EventTypeCode.ServiceKeyUpdated))).size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testAddUpdateLicenseNumberEOC_Validation() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        for (EntitlementUnit entitlementUnit : company.getPrimaryEntitlementUnits()) {
            entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
            Application.save(entitlementUnit);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();

        // add licence number, EOC, and entitlement status
        entitlementUnitDTO.setLicenseNumber("123456789012345678901");
        entitlementUnitDTO.setEntitlementOfferingCode("09876543210987654321");
        entitlementUnitDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());
        entitlementUnitDTO.setEditionType(EditionType.Basic);
        entitlementUnitDTO.setNumberOfEmployeesType(NumberOfEmployeesType.UPTO3);
        entitlementUnitDTO.setFedTaxId("999999999");

        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);

        ProcessResult<EntitlementUnit> addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("license number length validation " + addEntitlementProcessResult.toString(), 5001, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        entitlementUnitDTO.setLicenseNumber("12345678901234567890");
        entitlementUnitDTO.setFedTaxId("9999999991");

        addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("FEIN length validation", 5001, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        entitlementUnitDTO.setFedTaxId("999999999");
        entitlementUnitDTO.setEntitlementOfferingCode(null);

        addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("EOC validation", 5001, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        entitlementUnitDTO.setLicenseNumber("12345678901234567890");
        entitlementUnitDTO.setEntitlementOfferingCode("098765432109876543210");

        addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("EOC validation", 5001, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        entitlementUnitDTO.setEntitlementOfferingCode("09876543210987654321");
        entitlementUnitDTO.setEntitlementUnitStatus(null);

        addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("entitlement status missing", 5001, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
        entitlementUnitDTO.setEditionType(EditionType.EnhancedAccountant);

        addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("entitlement does not exist", 316, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        entitlementUnitDTO.setEditionType(EditionType.Basic);
        entitlementUnitDTO.setAssetItemNumber("123");

        addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("invalid asset item number", 316, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addEntitlementUnit(company, entitlementUnitDTO.getLicenseNumber(), entitlementUnitDTO.getEntitlementOfferingCode());

        PayrollServices.beginUnitOfWork();
        entitlementUnitDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.ASSISTED.toString());
        entitlementUnitDTO.setEditionType(null);
        entitlementUnitDTO.setNumberOfEmployeesType(null);
        entitlementUnitDTO.setFedTaxId(company.getFedTaxId());
        addEntitlementProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(
                company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);
        PSP_PRAssert.assertContains("cannot change entitlement asset type for an existing company entitlement", 5001, MessageInfo.MessageLevel.ERROR, addEntitlementProcessResult);

        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testPendingDeactivationToDeactivationHold1Step() {

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();
        EntitlementUnit entitlementUnit = DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementUnit);

        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        PayrollServices.commitUnitOfWork();

        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),
                                                                                                                     company.getSourceCompanyId(),
                                                                                                                     entitlementUnitDTO);
        assertTrue(processResult.isSuccess());
        Assert.assertEquals(EntitlementStateCode.Disabled, processResult.getResult().getEntitlement().getEntitlementState());
        Assert.assertEquals(EntitlementUnitStatusCode.DeactivationHold, processResult.getResult().getEntitlementUnitStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testPendingDeactivationToDeactivationHold2Step() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();
        EntitlementUnit entitlementUnit = DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        Application.save(entitlementUnit);

        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        PayrollServices.commitUnitOfWork();

        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),
                                                                                                                     company.getSourceCompanyId(),
                                                                                                                     entitlementUnitDTO);
        assertSuccess(processResult);
        Assert.assertEquals(EntitlementStateCode.Disabled, processResult.getResult().getEntitlement().getEntitlementState());
        PayrollServices.commitUnitOfWork();

        entitlementUnit = processResult.getResult();
        entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),
                                                                                      company.getSourceCompanyId(),
                                                                                      entitlementUnitDTO);
        assertTrue(processResult.isSuccess());
        Assert.assertEquals(EntitlementUnitStatusCode.DeactivationHold, processResult.getResult().getEntitlementUnitStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDeactivationHoldToPendingDeactivation() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();
        EntitlementUnit entitlementUnit = DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        PayrollServices.beginUnitOfWork();
        entitlementUnit = Application.findById(EntitlementUnit.class, entitlementUnit.getId());
        entitlementUnit.setEntitlementUnitStatus(EntitlementUnitStatusCode.DeactivationHold);
        Application.save(entitlementUnit);

        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        PayrollServices.commitUnitOfWork();

        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Enabled);

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),
                                                                                                                     company.getSourceCompanyId(),
                                                                                                                     entitlementUnitDTO);
        assertTrue(processResult.isSuccess());
        Assert.assertEquals(EntitlementStateCode.Enabled, processResult.getResult().getEntitlement().getEntitlementState());
        Assert.assertEquals(EntitlementUnitStatusCode.PendingDeactivation, processResult.getResult().getEntitlementUnitStatus());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddMultiplePrimaryEntitlements() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        // add license number and EOC
        DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);
        EntitlementUnitDTO entitlementUnitDTO = new EntitlementUnitDTO();
        entitlementUnitDTO.setLicenseNumber("123456");
        entitlementUnitDTO.setEntitlementOfferingCode("78594567");
        entitlementUnitDTO.setEditionType(null);
        entitlementUnitDTO.setNumberOfEmployeesType(null);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingActivation);
        entitlementUnitDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.ASSISTED.toString());
        entitlementUnitDTO.setFedTaxId(company.getFedTaxId());

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),
                                                                                                                     company.getSourceCompanyId(),
                                                                                                                     entitlementUnitDTO);
        PSP_PRAssert.assertContains("add entitlement ProcessResult", 321, MessageInfo.MessageLevel.ERROR, processResult);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testUpdateEntitlementCode() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        // add license number and EOC
        DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Basic, NumberOfEmployeesType.UNLIMITED);

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = EntitlementUnit.findEntitlementUnits(company.getFedTaxId(), licenseNumber, entitlementOfferingCode).get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setNumberOfEmployeesType(NumberOfEmployeesType.UPTO3);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),
                                                                                                                     company.getSourceCompanyId(),
                                                                                                                     entitlementUnitDTO);
        PSP_PRAssert.assertSuccess("add entitlement ProcessResult", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        assertEquals("entitlement number of employees", NumberOfEmployeesType.UPTO3, entitlement.getEntitlementCode().getNumberOfEmployeesType());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCancelTermedAssisted() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        // add license number and EOC
        DataLoadServices.addAssistedEntitlementUnit(company, licenseNumber, entitlementOfferingCode, true);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), SourceSystemCode.QBDT);
        CompanyService cs = company.getCompanyService(ServiceCode.Tax);
        cs.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        EntitlementUnit entitlementUnit = EntitlementUnit.findEntitlementUnits(company.getFedTaxId(), licenseNumber, entitlementOfferingCode).get(0);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<EntitlementUnit> processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(),
                                                                                                                     company.getSourceCompanyId(),
                                                                                                                     entitlementUnitDTO);

        PSP_PRAssert.assertSuccess("add entitlement ProcessResult", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Entitlement entitlement = Entitlement.findEntitlement(licenseNumber, entitlementOfferingCode);
        entitlementUnit = entitlement.getEntitlementUnitCollection().get(0);

        assertEquals("entitlement status", EntitlementStateCode.Disabled, entitlement.getEntitlementState());
        assertEquals("entitlement unit status", EntitlementUnitStatusCode.Deactivated, entitlementUnit.getEntitlementUnitStatus());

        CompanyService companyService = entitlementUnit.getCompany().getCompanyService(ServiceCode.Tax);
        assertEquals("tax service status", ServiceSubStatusCode.Terminated, companyService.getStatusCd());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testErrorCreatingOrReactivatingEntitlementUnitsOnDisabledEntitlements() {
        //verifies PSRV003535
        String licenseNumber = "lic1";
        String entitlementOfferingCode = "eoc1";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);

        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, licenseNumber, entitlementOfferingCode);
        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.disableEntitlement(entitlementUnit.getEntitlement());

        //reactivate
        PayrollServices.beginUnitOfWork();
        Application.refresh(entitlementUnit);
        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        ProcessResult<EntitlementUnit> entitlementUnitProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(),
                                                                                                                                    entitlementUnit.getCompany().getSourceCompanyId(),
                                                                                                                                    entitlementUnitDTO);
        assertContains(325, MessageInfo.MessageLevel.ERROR, entitlementUnitProcessResult);
        PayrollServices.rollbackUnitOfWork();

        //add new
        PayrollServices.beginUnitOfWork();
        Application.refresh(entitlementUnit);
        entitlementUnitDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Activated);
        entitlementUnitDTO.setFedTaxId("991234567");
        entitlementUnitProcessResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(),
                                                                                                     entitlementUnit.getCompany().getSourceCompanyId(),
                                                                                                     entitlementUnitDTO);
        assertContains(325, MessageInfo.MessageLevel.ERROR, entitlementUnitProcessResult);
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testServiceUpgradeFromDIYDDToAssisted() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        CompanyOffering companyOffering = company.getDirectDepositCompanyOffering();
        Assert.assertEquals("Offering", OfferingCode.DIYDDFY16, companyOffering.getOffering().getOfferingCode());
        org.junit.Assert.assertTrue("DirectDeposit Service", company.isCompanyOnService(ServiceCode.DirectDeposit));
        Assert.assertEquals("DirectDeposit service status", ServiceSubStatusCode.ActiveCurrent, company.getService(ServiceCode.DirectDeposit).getStatusCd());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addTaxService(company);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        companyOffering = company.getDirectDepositCompanyOffering();
        org.junit.Assert.assertTrue("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        Assert.assertEquals("Tax service status", ServiceSubStatusCode.PendingSetup, company.getService(ServiceCode.Tax).getStatusCd());
        Assert.assertEquals("Offering", OfferingCode.DIYDDFY16, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        companyOffering = company.getDirectDepositCompanyOffering();
        org.junit.Assert.assertTrue("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        Assert.assertEquals("Tax service status", ServiceSubStatusCode.PendingBalanceFile, company.getService(ServiceCode.Tax).getStatusCd());
        Assert.assertEquals("Offering", OfferingCode.DIYDDFY16, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.activateTaxService(company);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        companyOffering = company.getDirectDepositCompanyOffering();
        org.junit.Assert.assertTrue("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        Assert.assertEquals("Tax service status", ServiceSubStatusCode.ActiveCurrent, company.getService(ServiceCode.Tax).getStatusCd());
        Assert.assertEquals("Offering", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testServiceDowngradeFromAssistedToDIYDD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId() + "1";
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId() + "1";

        DataLoadServices.activateDDService(company);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        CompanyOffering companyOffering = company.getDirectDepositCompanyOffering();
        Assert.assertEquals("Offering", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
            EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(entitlementUnit);
            dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.PendingDeactivation);
            assertSuccess(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(), entitlementUnit.getCompany().getSourceCompanyId(), dto));
        }
        PayrollServices.commitUnitOfWork();
        DataLoadServices.cancelService(company, ServiceCode.Tax);
        //Leaving Cloud active

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        companyOffering = company.getDirectDepositCompanyOffering();
        org.junit.Assert.assertFalse("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        org.junit.Assert.assertFalse("Direct Deposit Service", company.isCompanyOnService(ServiceCode.DirectDeposit));
        Assert.assertEquals("Offering", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

        EntitlementUnit entitlementUnit = DataLoadServices.addDIYEntitlementUnit(company, licenseNumber, entitlementOfferingCode, null, null);

        DataLoadServices.addDDService(company);
        DataLoadServices.activateEntitlementUnit(entitlementUnit);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        companyOffering = company.getDirectDepositCompanyOffering();
        org.junit.Assert.assertFalse("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        org.junit.Assert.assertTrue("Direct Deposit Service", company.isCompanyOnService(ServiceCode.DirectDeposit));
        Assert.assertEquals("Offering", OfferingCode.DIYDDFY16, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

        EntitlementDTO entitlementDTO = PayrollServices.dtoFactory.create(entitlementUnit);
        entitlementDTO.setNumberOfEmployeesType(NumberOfEmployeesType.UPTO3);
        entitlementDTO.setEditionType(EditionType.Basic);
        entitlementDTO.setAssetItemNumber(DataLoadServices.AssetItemNumber.DIY_YEARLY.toString());

        DataLoadServices.updateEntitlement(entitlementDTO);

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        companyOffering = company.getDirectDepositCompanyOffering();
        org.junit.Assert.assertFalse("Tax Service", company.isCompanyOnService(ServiceCode.Tax));
        org.junit.Assert.assertTrue("DirectDeposit Service", company.isCompanyOnService(ServiceCode.DirectDeposit));
        Assert.assertEquals("Offering", OfferingCode.DIYDDFY16, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testst() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud);
        String licenseNumber = DataLoadServices.LIC_PREFIX + company.getSourceCompanyId();
        String entitlementOfferingCode = DataLoadServices.EOC_PREFIX + company.getSourceCompanyId();

        // add license number and EOC
        DataLoadServices.addAssistedEntitlementUnit(company, licenseNumber, entitlementOfferingCode, true);
        DataLoadServices.addTaxService(company);
    }
}
