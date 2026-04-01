package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EditionType;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage.EmployeeUsageFoundCode;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.ReasonForFreeChargeCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertNotNull;

/**
 * @author kmuthurangam
 *
 * Covers tests for all the customer scenarios which could lead to potential over billing.
 * All the over billing scenarios has been grouped under two categories
 *
 * <ul>
 * <li>Resend</li>
 * <li>Recreate</li>
 * </ul>
 */
public class UsageOverBillingTests extends UsageOverBillingTestsBase {


    @Test
    public void testProfileMigrationUnderSameLicenseWithOpenBill() throws Exception {

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15),
                SpcfCalendar.createInstance(2011, 7, 20));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX requestOFX = submitPayrollAndAssertOpenBill(company, entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount);

        // Disable the old Entitlement Unit and create new Entitlement Unit under the same license
        DataLoadServices.setPSPDate(2011, 7, 25);
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        // Submit all paychecks for the month of 07/2011 as part of the New Profile - Data Recovery event
        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 1);
        resendPayrollAndAssertClosedBill(requestOFX, company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(),
                billPeriodStartDate, paycheckDates, employeeDTOs, ReasonForFreeChargeCode.UsageTransfer, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID);

    }

    @Test
    public void testProfileMigrationUnderSameLicenseWithClosedBill() throws Exception {

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15),
                SpcfCalendar.createInstance(2011, 7, 20));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        DataLoadServices.setPSPDate(2011, 7, 20);

        // Submit multiple paychecks for the month of 07/2011
        OFX requestOFX = submitPayrollAndAssertClosedBill(company, entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount);

        DataLoadServices.setPSPDate(2011, 8, 5);

        // Disable the old Entitlement Unit and create new Entitlement Unit under the same license
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        // Submit all paychecks for the month of 07/2011 as part of the New Profile - Data Recovery event
        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 8, 1);
        resendPayrollAndAssertClosedBill(requestOFX, company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(),
                billPeriodStartDate, paycheckDates, employeeDTOs, ReasonForFreeChargeCode.AlreadyBilled, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID);
    }

    @Test
    public void testProfileMigrationUnderSameLicenseWithOpenAndClosedBill() throws Exception {

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> firstMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        List<OFX> requestOFXs = new ArrayList<>();

        int expectedEmployeeUsageCount = firstMonthPaycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 15);
        OFX requestOFX = submitPayrollAndAssertClosedBill(company, entitlementUnit.getEntitlement(), firstMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, false, true);

        requestOFXs.add(requestOFX);

        List<SpcfCalendar> secondMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 8, 5),
                SpcfCalendar.createInstance(2011, 8, 19));

        // Submit multiple paychecks for the month of 08/2011
        DataLoadServices.setPSPDate(2011, 8, 20);
        requestOFX = submitPayrollAndAssertOpenBill(company, entitlementUnit.getEntitlement(), secondMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, false, true);

        requestOFXs.add(requestOFX);

        // Create new Entitlement Unit under same license
        DataLoadServices.setPSPDate(2011, 8, 25);
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());
        EntitlementUnit newEntitlementUnit = assertOne(newCompany.getActiveEntitlementUnits());

        // Submit all paychecks for the month of 07/2011 & 08/2011 as part of the New Profile - Data Recovery event
        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 8, 25);

        // Resend all the paychecks from the previous profile, assert all the closed bills are marked as 'AlreadyBilled' and open bills are 'Usage Transferred'
        resendPayrollAndAssertBill(requestOFXs, newCompany, newEntitlementUnit.getEntitlement(),  billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID, true);
    }

    @Ignore
    @Test
    public void testProfileMigrationUnderSameLicenseWithNewPaychecks() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> firstMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        List<OFX> requestOFXs = new ArrayList<>();

        int expectedEmployeeUsageCount = firstMonthPaycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        OFX requestOFX = submitPayrollAndAssertClosedBill(company, entitlementUnit.getEntitlement(), firstMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, false, true);

        requestOFXs.add(requestOFX);

        DataLoadServices.setPSPDate(2011, 8, 5);
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 8, 5);
        resendPayrollAndAssertClosedBill(requestOFX, company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(),
                billPeriodStartDate, firstMonthPaycheckDates, employeeDTOs, ReasonForFreeChargeCode.AlreadyBilled, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID);


        List<SpcfCalendar> secondMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 8, 10),
                SpcfCalendar.createInstance(2011, 8, 15));

        DataLoadServices.setPSPDate(2011, 8, 20);

        // Submit multiple paychecks for the month of 08/2011 and both employees are charged for the month of 08/2011
        submitPayrollAndAssertClosedBill(newCompany, entitlementUnit.getEntitlement(), secondMonthPaycheckDates, employeeDTOs, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, ReasonForFreeChargeCode.AlreadyBilled, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID, false, true);
    }

    @Ignore
    @Test
    public void testProfileMigrationUnderSameLicenseWithBackdatedPaychecksOnClosedBill() throws Exception {

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        List<SpcfCalendar> firstMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        List<OFX> requestOFXs = new ArrayList<>();

        int expectedEmployeeUsageCount = firstMonthPaycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        OFX requestOFX = submitPayrollAndAssertClosedBill(company, entitlementUnit.getEntitlement(), firstMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, false, true);

        requestOFXs.add(requestOFX);

        DataLoadServices.setPSPDate(2011, 8, 5);
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 8, 5);
        resendPayrollAndAssertOpenBill(requestOFX, company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(),
                billPeriodStartDate, firstMonthPaycheckDates, employeeDTOs, ReasonForFreeChargeCode.AlreadyBilled, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID);

        List<SpcfCalendar> secondMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 21));

        DataLoadServices.setPSPDate(2011, 8, 20);

        // Submit backdated paycheck for the month of 07/2011 and assert employees are excluded from billing
        expectedEmployeeUsageCount = 0;
        expectedBillUsageCount =0;
        submitPayrollAndAssertOpenBill(newCompany, entitlementUnit.getEntitlement(), secondMonthPaycheckDates, employeeDTOs, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, ReasonForFreeChargeCode.AlreadyBilled, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID, false, true);

    }

    @Ignore
    @Test
    public void testProfileMigrationUnderSameLicenseWithBackdatedPaychecksForDifferentEmployee() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        List<SpcfCalendar> firstMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        List<OFX> requestOFXs = new ArrayList<>();

        int expectedEmployeeUsageCount = firstMonthPaycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        OFX requestOFX = submitPayrollAndAssertClosedBill(company, entitlementUnit.getEntitlement(), firstMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, false, true);

        requestOFXs.add(requestOFX);

        DataLoadServices.setPSPDate(2011, 8, 5);
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 8, 5);
        resendPayrollAndAssertOpenBill(requestOFX, company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(),
                billPeriodStartDate, firstMonthPaycheckDates, employeeDTOs, ReasonForFreeChargeCode.AlreadyBilled, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID);

        List<SpcfCalendar> secondMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 21));

        employeeDTOs = generateEmployeesAtIndex(6);

        DataLoadServices.setPSPDate(2011, 8, 20);

        // Submit backdated paycheck for the month of 07/2011 for Employee who has not been paid earlier and assert employees are correctly billed
        submitPayrollAndAssertOpenBill(newCompany, entitlementUnit.getEntitlement(), secondMonthPaycheckDates, employeeDTOs, billPeriodStartDate, 1, 1, ReasonForFreeChargeCode.AlreadyBilled, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID, false, true);

    }

    @Test
    public void testProfileMigrationUnderSameLicenseWithPaychecksAcrossMonths() throws Exception {

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> firstMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        List<OFX> requestOFXs = new ArrayList<>();

        int expectedEmployeeUsageCount = firstMonthPaycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 15);
        OFX requestOFX = submitPayrollAndAssertClosedBill(company, entitlementUnit.getEntitlement(), firstMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount);

        requestOFXs.add(requestOFX);

        List<SpcfCalendar> secondMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 8, 5),
                SpcfCalendar.createInstance(2011, 8, 19));

        // Submit multiple paychecks for the month of 08/2011
        DataLoadServices.setPSPDate(2011, 8, 20);
        requestOFX = submitPayrollAndAssertClosedBill(company, entitlementUnit.getEntitlement(), secondMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount);

        requestOFXs.add(requestOFX);

        // Create new Entitlement Unit under same license
        DataLoadServices.setPSPDate(2011, 8, 25);
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());
        EntitlementUnit newEntitlementUnit = assertOne(newCompany.getActiveEntitlementUnits());

        // Submit all paychecks for the month of 07/2011 & 08/2011 as part of the New Profile - Data Recovery event
        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 8, 25);
        // Resend all the paychecks from the previous profile
        resendPayrollAndAssertBill(requestOFXs, newCompany, newEntitlementUnit.getEntitlement(),  billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID, true);
    }

    @Test
    public void testProfileMigrationRecreatePaychecksUnderSameLicenseWithOpenBill() throws Exception {
         EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15),
                SpcfCalendar.createInstance(2011, 7, 20));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 20);
        submitPayrollAndAssertOpenBill(company, entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount);

        DataLoadServices.setPSPDate(2011, 7, 23);

        // Migrate the Company to a new Symphony Entitlement under a new profile
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        // Recreate and submit again the same paychecks for the month of 07/2011
        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 24);
        recreatePayrollAndAssertBill(company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(), billPeriodStartDate, paycheckDates,
                employeeDTOs, ReasonForFreeChargeCode.UsageTransfer, EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_NAME, false);
    }

    @Test
    public void testNonSymphonyToNewSymphonyLicenseMigrationUnderSameProfile() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        migrateLicense(entitlementUnit, "123456", "654321");

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 7),
                SpcfCalendar.createInstance(2011, 7, 14));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 15);

        submitNonSymphonyPayrollAndAssertNullCompanyUsage(company, entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs);

        DataLoadServices.setPSPDate(2011, 7, 15);

        migrateLicense(entitlementUnit, "690285559983251", "389857", AssetItemNumber.DIY_USAGE_BILLING_YEARLY);

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 21));

        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 08/2011
        DataLoadServices.setPSPDate(2011, 7, 25);

        Map<String, String> ofxValues = new HashMap<>();
        ofxValues.put("RequestToken", "6");

        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 25);
        submitPayrollAndAssertBill(company, ofxValues,  entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount,
                null, null, false, true);
    }

    @Test
    public void testNonSymphonyToNewSymphonyLicenseMigrationUnderDifferentProfile() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        migrateLicense(entitlementUnit, "123456", "654321");

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15),
                SpcfCalendar.createInstance(2011, 7, 20));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 20);

        OFX requestOFX = submitUsageDataFromOFX(company, paycheckDates,  employeeDTOs);
        runPSPToEMSBSDataSyncProcessor();

        // Assert for Null Company Usage, as the above is a Non Symphony payroll
        assertNullCompanyUsage(company, entitlementUnit.getEntitlement());

        // Migrate the Company to a new Symphony Entitlement under a new profile
        Company newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        // Resend the Non Symphony Payroll OFX to the newly created profile
        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 20);
        resendPayrollAndAssertClosedBill(requestOFX, company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(),
                        billPeriodStartDate, paycheckDates,  employeeDTOs, ReasonForFreeChargeCode.NotPartOfUsageBilling, null);
    }

    @Test
    public void testNonSymphonyToOldSymphonyLicenseMigrationUnderSameProfile() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        migrateLicense(entitlementUnit, "123456", "654321");

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 7),
                SpcfCalendar.createInstance(2011, 7, 14));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 15);

        submitNonSymphonyPayrollAndAssertNullCompanyUsage(company, entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs);

        DataLoadServices.setPSPDate(2011, 6, 15);

        Company newCompany = setupDDCompany("8574537", "000000002", "590285459980000", "389857");
        updateBDOMForCompany(newCompany, 15);


        DataLoadServices.setPSPDate(2011, 7, 15);

        migrateLicense(entitlementUnit, "590285459980000", "389857", AssetItemNumber.DIY_USAGE_BILLING_MONTHLY);

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 21));

        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 08/2011
        DataLoadServices.setPSPDate(2011, 7, 25);

        Map<String, String> ofxValues = new HashMap<>();
        ofxValues.put("RequestToken", "6");

        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 25);
        submitPayrollAndAssertBill(company, ofxValues,  entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount,
                null, null, false, true);
    }

    @Test
    public void testNonSymphonyToOldSymphonyLicenseMigrationUnderDifferentProfile() throws Exception {

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        migrateLicense(entitlementUnit, "123456", "654321");

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15),
                SpcfCalendar.createInstance(2011, 7, 20));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 20);

        OFX requestOFX = submitUsageDataFromOFX(company, paycheckDates,  employeeDTOs);
        runPSPToEMSBSDataSyncProcessor();

        // Assert for Null Company Usage, as the above is a Non Symphony payroll
        assertNullCompanyUsage(company, entitlementUnit.getEntitlement());

        DataLoadServices.setPSPDate(2011, 6, 15);

        Company newCompany = setupDDCompany("8574538", "000000002", "590285459980000", "389857");
        updateBDOMForCompany(newCompany, 15);

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        DataLoadServices.setPSPDate(2011, 7, 24);

        // Migrate the Company to a new Symphony Entitlement under a new profile
        newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        // Resend the Non Symphony Payroll OFX to the newly created profile
        DataLoadServices.setPSPDate(2011, 7, 26);
        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 26);
        resendPayrollAndAssertClosedBill(requestOFX, company, entitlementUnit.getEntitlement(), newCompany, entitlementUnit.getEntitlement(),
                billPeriodStartDate, paycheckDates,  employeeDTOs, ReasonForFreeChargeCode.NotPartOfUsageBilling, null);

    }

    @Test
    public void testNonSymphonyToOldReactivatedSymphonyLicenseMigrationUnderSameProfile() throws Exception {

    }

    @Test
    public void testNonSymphonyToSymphonyToAgainOldSymphonyLicenseMigrationUnderSameProfile() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<OFX> requestOFXs = new ArrayList<>();

        migrateLicense(entitlementUnit, "123456", "654321");

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 5),
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        // Submit multiple paychecks for the month of 07/2011
        DataLoadServices.setPSPDate(2011, 7, 15);

        OFX requestOFX = submitUsageDataFromOFX(company, paycheckDates,  employeeDTOs);
        runPSPToEMSBSDataSyncProcessor();

        requestOFXs.add(requestOFX);

        // Assert for Null Company Usage, as the above is a Non Symphony payroll
        assertNullCompanyUsage(company, entitlementUnit.getEntitlement());

        DataLoadServices.setPSPDate(2011, 7, 16);

        migrateLicense(entitlementUnit, "590285459980000", "389857", AssetItemNumber.DIY_USAGE_BILLING_MONTHLY);

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 21));

        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 08/2011
        DataLoadServices.setPSPDate(2011, 7, 17);

        Map<String, String> ofxValues = new HashMap<>();
        ofxValues.put("RequestToken", "6");

        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 17);
        requestOFX = submitPayrollAndAssertBill(company, ofxValues,  entitlementUnit.getEntitlement(), paycheckDates, employeeDTOs, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount,
                null, null, false, true);

        requestOFXs.add(requestOFX);

        DataLoadServices.setPSPDate(2011, 6, 15);

        Company newCompany = setupDDCompany("8574538", "000000002", "590285459980000", "389857");
        updateBDOMForCompany(newCompany, 15);

        entitlementUnit = assertOne(company.getActiveEntitlementUnits());

        DataLoadServices.setPSPDate(2011, 7, 24);

        // Migrate the Company to a new Symphony Entitlement under a new profile
        newCompany = migrateProfile(entitlementUnit, entitlementUnit.getEntitlement());

        entitlementUnit = assertOne(newCompany.getActiveEntitlementUnits());

        // Resend the Non Symphony Payroll OFX to the newly created profile
        DataLoadServices.setPSPDate(2011, 7, 26);

        billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 26);

        // Resend all the paychecks from the previous profile
        ofxValues = new HashMap<>();
        ofxValues.put("SourceCompanyId", newCompany.getSourceCompanyId());
        // Initial token is always 4
        ofxValues.put("RequestToken", "4");
        ofxValues.put("SubscriptionNum", entitlementUnit.getEntitlement().getSubscriptionNumber());
        reSubmitUsageDataFromOFX(requestOFXs, ofxValues);
        runPSPToEMSBSDataSyncProcessor();

        assertCalculatedUsageData(newCompany, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount);

    }

    @Ignore
    @Test
    public void testCorrectBillingDuringLicenseMigrationBillConsolidation() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        List<SpcfCalendar> firstMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        List<OFX> requestOFXs = new ArrayList<>();

        int expectedEmployeeUsageCount = firstMonthPaycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Submit multiple paychecks for the month of 07/2011
        OFX requestOFX = submitPayrollAndAssertOpenBill(company, entitlementUnit.getEntitlement(), firstMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, false, true);

        requestOFXs.add(requestOFX);

        DataLoadServices.setPSPDate(2011, 7, 17);

        // Migrate the license under same profile
        EntitlementUnit newEU = DataLoadServices.addEntitlementUnit(company, "1", "2", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        updateBDOMForCompany(company, 15);

        SpcfCalendar billPeriodStartDate = SpcfCalendar.createInstance(2011, 7, 21);

        List<SpcfCalendar> secondMonthPaycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 21));

        employeeDTOs = generateEmployees(2);

        // Submit one more paycheck for the month of 07/2011 for few same employees who has been paid earlier after license migration and assert all paychecks usages are transferred
        expectedEmployeeUsageCount = 1;
        expectedBillUsageCount = 2;

        submitPayrollAndAssertOpenBill(company, newEU.getEntitlement(), secondMonthPaycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, false, true);

        // Run the Monthly job and assert bills across multiple licenses are consolidated and charged under the new license
        expectedBillUsageCount = 2;
        int expectedBillSyncedCount = 5;

        runEMSBSToBRMDataSyncProcessor(billPeriodStartDate);
        assertFinalBillData(company, newEU.getEntitlement(), secondMonthPaycheckDates.get(0), true, expectedBillUsageCount, expectedBillSyncedCount);

    }


    @Test
    public void testFreePaycheckUsageNotBilledDuringLicenseMigrationBillConsolidation() throws Exception {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        List<SpcfCalendar> paycheckDates = Arrays.asList(
                SpcfCalendar.createInstance(2011, 7, 10),
                SpcfCalendar.createInstance(2011, 7, 15));

        List<EmployeeDTO> employeeDTOs = generateEmployees(5);

        List<OFX> requestOFXs = new ArrayList<>();

        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();


        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        Entitlement entitlement = primaryEntitlementUnit.getEntitlement();
        entitlement.setSubscriptionStartDate(SpcfCalendar.createInstance(2012, 10, 1));
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        // Submit multiple paychecks for the month of 07/2011
        OFX requestOFX = submitUsageDataFromOFX(company, paycheckDates,  employeeDTOs);
        runPSPToEMSBSDataSyncProcessor();
    }

    @Test
    public void testCancelledPaycheckUsageNotBilledDuringLicenseMigrationBillConsolidation() throws Exception {

    }

    @Test
    public void testAlreadyBilledPaycheckUsageNotBilledDuringLicenseMigrationBillConsolidation() throws Exception {

    }

}
