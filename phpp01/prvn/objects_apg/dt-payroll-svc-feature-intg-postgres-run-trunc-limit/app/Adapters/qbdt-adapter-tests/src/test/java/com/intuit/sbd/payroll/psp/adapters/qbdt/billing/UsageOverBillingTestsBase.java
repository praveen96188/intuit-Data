package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.Bill;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyUsage;
import com.intuit.sbd.payroll.psp.domain.EditionType;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage;
import com.intuit.sbd.payroll.psp.domain.EmployeeUsage.EmployeeUsageFoundCode;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.PaycheckUsage;
import com.intuit.sbd.payroll.psp.domain.PaycheckUsageHist;
import com.intuit.sbd.payroll.psp.domain.ReasonForFreeChargeCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices.AssetItemNumber;
import com.intuit.sbd.payroll.psp.processes.DeactivateServiceCore;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UsageOverBillingTestsBase extends UsageBillingTestsBase {

    public OFX submitUsageDataFromOFX(Company company, SpcfCalendar paycheckDate, List<EmployeeDTO> employeeDTOs) throws Exception {
        return submitUsageDataFromOFX(company, Arrays.asList(paycheckDate), employeeDTOs, true, true);
    }

    public OFX submitUsageDataFromOFX(Company company, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs) throws Exception {
        return submitUsageDataFromOFX(company, paycheckDates, employeeDTOs, true, true);
    }

    public OFX submitUsageDataFromOFX(Company company, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        OFX request = createOFX(company, paycheckDates, employeeDTOs, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        return submitUsageDataFromOFX(request);
    }

    public OFX submitUsageDataFromOFX(Company company, Map<String, String> ofxValues, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        OFX request = createOFX(company, paycheckDates, employeeDTOs, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        updateOFX(request, ofxValues);
        return submitUsageDataFromOFX(request);
    }

    public OFX createOFX(Company company, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        Application.beginUnitOfWork();
        Application.refresh(company);
        String newSubscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getSubscriptionNumber();
        Application.rollbackUnitOfWork();

        String paycheckId = "1";
        OFX request = new UsageOFXDataloader().createOFX(company.getSourceCompanyId(), UsageOFXDataloader.OFX_NULL_STRING, company.getFedTaxId(), newSubscriptionNumber, paycheckDates, employeeDTOs, paycheckId, "N", false, 1, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        return request;
    }

    public OFX updateOFX(OFX requestOFX, Map<String, String> ofxValues){
        for (Map.Entry<String, String> entry : ofxValues.entrySet()) {
            switch (entry.getKey()){
                case "SourceCompanyId":
                    requestOFX.getSIGNONMSGSRQV1().getSONRQ().setUSERID(entry.getValue());
                    break;
                case "SubscriptionNum":
                    requestOFX.getSIGNONMSGSRQV1().getSONRQ().setISUBSCRIPTIONNUM(entry.getValue());
                    break;
                case "RequestToken":
                    requestOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(entry.getValue());
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("OFX update is not supported for %s", entry.getKey()));
            }
        }
        return requestOFX;
    }

    public void reSubmitUsageDataFromOFX(OFX requestOFX, Map<String, String> ofxValues) throws Exception {
        reSubmitUsageDataFromOFX(Arrays.asList(requestOFX), ofxValues);
    }

    public List<OFX> reSubmitUsageDataFromOFX(List<OFX> requestOFXs, Map<String, String> ofxValues) throws Exception {
        List<OFX> responseOFXs = new ArrayList<>();
        for (OFX requestOFX : requestOFXs){
            OFX request = updateOFX(requestOFX, ofxValues);
            responseOFXs.add(submitUsageDataFromOFX(request));

            // Increment the request token before submitting the next OFX
            String requestTokenString = requestOFX.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getTOKEN();
            int requestToken = NumberUtils.toInt(requestTokenString, 0);
            ofxValues.put("RequestToken", String.valueOf(++requestToken));
        }
        return responseOFXs;
    }

    public OFX submitUsageDataFromOFX(OFX request) throws Exception {
        String requestOfx = OFXManager.javaRequestToOFX(request);
        System.out.println(String.format("Request OFX\n -%s", requestOfx));
        String responseOfx = QBDTTestHelper.processOFXRequestSuccess(request);

        System.out.println(String.format("Response OFX\n -%s", responseOfx));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseOfx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        return request;
    }

    public List<EmployeeDTO> generateEmployeesAtIndex(int index) {
        List<EmployeeDTO> employeeDTOList = generateEmployees();
        if(index > (employeeDTOList.size()-1)){
            throw new RuntimeException("Doesn't support more than "+employeeDTOList.size());
        }
        return Arrays.asList(employeeDTOList.get(index));
    }

    public List<EmployeeDTO> generateEmployees(int numOfEmployees) {
        List<EmployeeDTO> employeeDTOList = generateEmployees();
        if(employeeDTOList.size() < numOfEmployees){
            throw new RuntimeException("Doesn't support more than "+employeeDTOList.size());
        }
        return employeeDTOList.subList(0, numOfEmployees);
    }

    public List<EmployeeDTO> generateEmployees() {
        List<EmployeeDTO> employeeDTOList = new ArrayList<>();
        employeeDTOList.add(createEmployeeDTO("1", "Susan", "Butchman"));
        employeeDTOList.add(createEmployeeDTO("2", "Alfred P", "Sloan"));
        employeeDTOList.add(createEmployeeDTO("3", "Archer Daniels", "Midland"));
        employeeDTOList.add(createEmployeeDTO("4", "Patrick U", "Gooch"));
        employeeDTOList.add(createEmployeeDTO("5", "Jack", "Glisson"));
        employeeDTOList.add(createEmployeeDTO("6", "Marguerite", "Gill"));
        employeeDTOList.add(createEmployeeDTO("7", "Clay", "Miller"));
        employeeDTOList.add(createEmployeeDTO("8", "Thelma", "Becker"));
        employeeDTOList.add(createEmployeeDTO("9", "Margaret J", "Pierce"));
        employeeDTOList.add(createEmployeeDTO("10", "Martha", "Williams"));
        return employeeDTOList;
    }

    public EmployeeDTO createEmployeeDTO(String empId, String firstName, String lastName){
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setEmployeeId(empId);
        employeeDTO.setFirstName(firstName);
        employeeDTO.setLastName(lastName);
        return employeeDTO;
    }

    /** Asserts **/
    public void assertFinalBillData(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate, boolean billClosed, int billUsageCount) {
        assertFinalBillData(company, entitlement, billPeriodStartDate, billClosed, billUsageCount, billUsageCount);
    }

    public void assertFinalBillData(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate, boolean billClosed, int billUsageCount, int synchedUsageCount) {
        assertBillCounts(company, entitlement, billPeriodStartDate, billUsageCount, synchedUsageCount);

        // Ideally all the bills should be closed but bills are not getting closed if the Synched count is 0.
        // This is a known bug, when the issue is fixed, assert statement should be changed
        if(billClosed){
            assertBillClosed(company, entitlement, billPeriodStartDate);
        } else {
            assertBillOpen(company, entitlement, billPeriodStartDate);
        }
    }

    public void assertCalculatedUsageData(Company company, SpcfCalendar billPeriodStartDate, int employeeUsageCount, int billUsageCount) {
        assertCalculatedUsageData(company, billPeriodStartDate, employeeUsageCount, billUsageCount, null);
    }

    public void assertCalculatedUsageData(Company company, SpcfCalendar billPeriodStartDate, int employeeUsageCount, int billUsageCount, EmployeeUsageFoundCode employeeUsageFoundCode) {
        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        Entitlement entitlement = entitlementUnit.getEntitlement();
        assertCalculatedUsageData(company, entitlement, billPeriodStartDate, employeeUsageCount, billUsageCount, employeeUsageFoundCode);
    }

    public void assertCalculatedUsageData(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate, int employeeUsageCount, int billUsageCount) {
        assertCalculatedUsageData(company, entitlement, billPeriodStartDate, employeeUsageCount, billUsageCount, null);
    }

    public void assertCalculatedUsageData(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate, int employeeUsageCount, int billUsageCount, EmployeeUsageFoundCode employeeUsageFoundCode) {
        assertPaycheckUsage(company, entitlement, employeeUsageFoundCode);
        assertEmployeeUsageCount(company, entitlement, employeeUsageCount);
        assertBillCounts(company, entitlement, billPeriodStartDate, billUsageCount, 0);
        assertBillOpen(company, entitlement, billPeriodStartDate);
    }

    public void assertNotNullCompanyUsage(Company company, Entitlement entitlement) {
        CompanyUsage companyUsage = findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        Assert.assertNotNull(companyUsage);
    }

    public void assertNullCompanyUsage(Company company, Entitlement entitlement) {
        CompanyUsage companyUsage = findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        Assert.assertNull(companyUsage);
    }

    public void assertEmployeeUsageCount(Company company, Entitlement entitlement, int usageCount) {
        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        MultiValuedMap<EmployeeUsageCode, EmployeeUsage> multiValuedMap = findEmployeeUsages(usage, PSPDate.getPSPTime());

        Collection<EmployeeUsage> employeeUsageCollection = multiValuedMap.get(EmployeeUsageCode.AlreadyBilled);
        for (EmployeeUsage employeeUsage: employeeUsageCollection) {
            assertEquals(0, employeeUsage.getUsageCount());
        }

        employeeUsageCollection = multiValuedMap.get(EmployeeUsageCode.NotPartOfUsageBilling);
        for (EmployeeUsage employeeUsage: employeeUsageCollection) {
            assertEquals(0, employeeUsage.getUsageCount());
        }

        employeeUsageCollection = multiValuedMap.get(EmployeeUsageCode.EligibleForBilling);
        for (EmployeeUsage employeeUsage: employeeUsageCollection) {
            assertEquals(usageCount, employeeUsage.getUsageCount());
        }
        Application.rollbackUnitOfWork();
    }

    public void assertPaycheckUsage(Company company, Entitlement entitlement) {
        assertPaycheckUsage(company, entitlement, null);
    }

    public void assertPaycheckUsage(Company company, Entitlement entitlement, EmployeeUsageFoundCode employeeUsageFoundCode) {
        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());

        Bill bill = findBill(usage, PSPDate.getPSPTime());

        DomainEntitySet<PaycheckUsage> paycheckUsageCollection = bill.getPaycheckUsageCollection();
        for (PaycheckUsage paycheckUsage: paycheckUsageCollection) {

            if(Objects.isNull(employeeUsageFoundCode)){
                assertEmptyPaycheckUsageHist(paycheckUsage);
                continue;
            }

            switch (employeeUsageFoundCode){
                case MATCHES_FED_TAX_ID_AND_LIST_ID:
                    assertPaycheckUsageHistForResend(paycheckUsage);
                    break;
                case MATCHES_FED_TAX_ID_AND_NAME:
                    assertPaycheckUsageHistForRecreate(paycheckUsage);
                    break;
            }
        }
        Application.rollbackUnitOfWork();
    }

    public void assertEmptyPaycheckUsageHist(PaycheckUsage paycheckUsage){
        assertTrue("Paycheck Usage History should be empty", paycheckUsage.getPaycheckUsageHistCollection().isEmpty());
    }


    public void assertPaycheckUsageHistForResend(PaycheckUsage paycheckUsage) {
        for(PaycheckUsageHist paycheckUsageHist: paycheckUsage.getPaycheckUsageHistCollection()){
            assertNotNull(paycheckUsageHist);
            EmployeeUsage historicalEmployeeUsage = paycheckUsageHist.getEmployeeUsage();
            PaycheckUsage historicalPaycheckUsage = historicalEmployeeUsage.getPaycheckUsageCollection().getFirst();

            ReasonForFreeChargeCode reasonForFreeChargeCode = ReasonForFreeChargeCode.AlreadyBilled;
            if(!historicalPaycheckUsage.getBill().getClosed()){
                reasonForFreeChargeCode = ReasonForFreeChargeCode.UsageTransfer;
            }

            assertEquals(paycheckUsage.getReasonForFreeCharge(), reasonForFreeChargeCode);
            assertEquals(paycheckUsage.getEmployeeUsage().getSourceEmployeeId(), historicalEmployeeUsage.getSourceEmployeeId());
            assertEquals(findCompany(paycheckUsage).getFedTaxId(), findCompany(historicalPaycheckUsage).getFedTaxId());
            assertEquals(EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_LIST_ID.getReason(), paycheckUsageHist.getNotes());
        }
    }

    public void assertPaycheckUsageHistForRecreate(PaycheckUsage paycheckUsage) {
        for(PaycheckUsageHist paycheckUsageHist: paycheckUsage.getPaycheckUsageHistCollection()){
            assertNotNull(paycheckUsageHist);
            EmployeeUsage historicalEmployeeUsage = paycheckUsageHist.getEmployeeUsage();
            PaycheckUsage historicalPaycheckUsage = historicalEmployeeUsage.getPaycheckUsageCollection().getFirst();

            assertEquals(findCompany(historicalPaycheckUsage).getFedTaxId(), findCompany(paycheckUsage).getFedTaxId());
            assertEquals(EmployeeUsageFoundCode.MATCHES_FED_TAX_ID_AND_NAME.getReason(), paycheckUsageHist.getNotes());
        }
    }

    public OFX submitNonSymphonyPayrollAndAssertNullCompanyUsage(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs) throws Exception {
        OFX requestOFX = submitUsageDataFromOFX(company, paycheckDates,  employeeDTOs);
        runPSPToEMSBSDataSyncProcessor();

        // Assert for Null Company Usage, as the above is a Non Symphony payroll
        assertNullCompanyUsage(company, entitlement);
        return requestOFX;
    }

    public OFX submitPayrollAndAssertOpenBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                              int expectedEmployeeUsageCount, int expectedBillUsageCount) throws Exception {
        return submitPayrollAndAssertBill(company, entitlement, paycheckDates, employeeDTOs, false, expectedEmployeeUsageCount, expectedBillUsageCount, null, null, true, true);
    }

    public OFX submitPayrollAndAssertOpenBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                              int expectedEmployeeUsageCount, int expectedBillUsageCount,  boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        return submitPayrollAndAssertBill(company, entitlement, paycheckDates, employeeDTOs, false, expectedEmployeeUsageCount, expectedBillUsageCount, null, null, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
    }

    public OFX submitPayrollAndAssertOpenBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                              SpcfCalendar billPeriodStartDate, int expectedEmployeeUsageCount, int expectedBillUsageCount, ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        return submitPayrollAndAssertBill(company, entitlement, paycheckDates, employeeDTOs, billPeriodStartDate, false, expectedEmployeeUsageCount, expectedBillUsageCount, reasonForFreeChargeCode, employeeUsageFoundCode, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
    }

    public OFX submitPayrollAndAssertClosedBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                                int expectedEmployeeUsageCount, int expectedBillUsageCount) throws Exception {
        return submitPayrollAndAssertClosedBill(company, entitlement, paycheckDates, employeeDTOs, null, expectedEmployeeUsageCount, expectedBillUsageCount, null, null, true, true);
    }

    public OFX submitPayrollAndAssertClosedBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                                int expectedEmployeeUsageCount, int expectedBillUsageCount, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        return submitPayrollAndAssertClosedBill(company, entitlement, paycheckDates, employeeDTOs, null, expectedEmployeeUsageCount, expectedBillUsageCount, null, null, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
    }

    public OFX submitPayrollAndAssertClosedBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                                SpcfCalendar billPeriodStartDate, int expectedEmployeeUsageCount, int expectedBillUsageCount, ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        if(Objects.isNull(billPeriodStartDate)){
            SpcfCalendar lastPaycheckDate = paycheckDates.get(paycheckDates.size() - 1);
            billPeriodStartDate = CalendarUtils.getFirstDayOfMonth(lastPaycheckDate);
        }
        return submitPayrollAndAssertBill(company, entitlement, paycheckDates, employeeDTOs, billPeriodStartDate, true, expectedEmployeeUsageCount, expectedBillUsageCount,reasonForFreeChargeCode,employeeUsageFoundCode, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
    }

    public OFX submitPayrollAndAssertBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs, boolean closeBill,
                                          int expectedEmployeeUsageCount, int expectedBillUsageCount, ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        SpcfCalendar lastPaycheckDate = paycheckDates.get(paycheckDates.size() - 1);
        SpcfCalendar billPeriodStartDate = CalendarUtils.getFirstDayOfMonth(lastPaycheckDate);
        return submitPayrollAndAssertBill(company, entitlement, paycheckDates, employeeDTOs, billPeriodStartDate, closeBill, expectedEmployeeUsageCount, expectedBillUsageCount,reasonForFreeChargeCode,employeeUsageFoundCode, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
    }

    public OFX submitPayrollAndAssertBill(Company company, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs, SpcfCalendar billPeriodStartDate, boolean closeBill,
                                           int expectedEmployeeUsageCount, int expectedBillUsageCount, ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        SpcfCalendar lastPaycheckDate = paycheckDates.get(paycheckDates.size() - 1);

        // Send multiple paychecks for the month of 07/2011
        OFX ofx = submitUsageDataFromOFX(company, paycheckDates,  employeeDTOs, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        runPSPToEMSBSDataSyncProcessor();

        if(Objects.isNull(reasonForFreeChargeCode)){
            assertCalculatedUsageData(company, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, employeeUsageFoundCode);
        } else {
            switch(reasonForFreeChargeCode){
                case NotPartOfUsageBilling:
                case AlreadyBilled:
                case UsageTransfer:
                default:
                    assertCalculatedUsageData(company, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, employeeUsageFoundCode);
                    break;
            }
        }

        if(closeBill){
            runEMSBSToBRMDataSyncProcessor(billPeriodStartDate);
            assertFinalBillData(company, entitlement, lastPaycheckDate, true, expectedBillUsageCount);
        }

        return ofx;
    }

    public OFX submitPayrollAndAssertBill(Company company, Map<String, String> ofxValues, Entitlement entitlement, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                          SpcfCalendar billPeriodStartDate, int expectedEmployeeUsageCount, int expectedBillUsageCount, ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode, boolean pGenerateStandardEmployeeListId, boolean pGenerateStandardPaycheckListId) throws Exception {
        OFX ofx = submitUsageDataFromOFX(company, ofxValues, paycheckDates,  employeeDTOs, pGenerateStandardEmployeeListId, pGenerateStandardPaycheckListId);
        runPSPToEMSBSDataSyncProcessor();

        assertCalculatedUsageData(company, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount);

        return ofx;
    }

    public void resendPayrollAndAssertOpenBill(OFX requestOFX, Company oldCompany, Entitlement oldEntitlement,
                                               Company newCompany, Entitlement newEntitlement,
                                               SpcfCalendar billPeriodStartDate, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                               ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode) throws Exception {
        resendPayrollAndAssertBill(Arrays.asList(requestOFX), oldCompany, oldEntitlement, newCompany, newEntitlement, billPeriodStartDate, paycheckDates, employeeDTOs, reasonForFreeChargeCode, employeeUsageFoundCode, false);
    }

    public void resendPayrollAndAssertOpenBill(List<OFX> requestOFXs, Company oldCompany, Entitlement oldEntitlement,
                                               Company newCompany, Entitlement newEntitlement,
                                               SpcfCalendar billPeriodStartDate, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                               ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode) throws Exception {
        resendPayrollAndAssertBill(requestOFXs, oldCompany, oldEntitlement, newCompany, newEntitlement, billPeriodStartDate, paycheckDates, employeeDTOs, reasonForFreeChargeCode, employeeUsageFoundCode, false);
    }

    public void resendPayrollAndAssertClosedBill(OFX requestOFX, Company oldCompany, Entitlement oldEntitlement,
                                                 Company newCompany, Entitlement newEntitlement,
                                                 SpcfCalendar billPeriodStartDate, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                                 ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode) throws Exception {
        resendPayrollAndAssertBill(Arrays.asList(requestOFX), oldCompany, oldEntitlement, newCompany, newEntitlement, billPeriodStartDate, paycheckDates, employeeDTOs, reasonForFreeChargeCode, employeeUsageFoundCode, true);
    }

    public void resendPayrollAndAssertClosedBill(List<OFX> requestOFXs, Company oldCompany, Entitlement oldEntitlement,
                                                 Company newCompany, Entitlement newEntitlement,
                                                 SpcfCalendar billPeriodStartDate, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                                 ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode) throws Exception {
        resendPayrollAndAssertBill(requestOFXs, oldCompany, oldEntitlement, newCompany, newEntitlement, billPeriodStartDate, paycheckDates, employeeDTOs, reasonForFreeChargeCode, employeeUsageFoundCode, true);
    }

    public void resendPayrollAndAssertBill(List<OFX> requestOFXs, Company oldCompany, Entitlement oldEntitlement,
                                           Company newCompany, Entitlement newEntitlement,
                                           SpcfCalendar billPeriodStartDate, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                           ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode, boolean generateBill) throws Exception {
        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        // Resend all the paychecks from the previous profile
        Map<String, String> ofxValues = new HashMap<>();
        ofxValues.put("SourceCompanyId", newCompany.getSourceCompanyId());
        // Initial token is always 4
        ofxValues.put("RequestToken", "4");
        reSubmitUsageDataFromOFX(requestOFXs, ofxValues);
        runPSPToEMSBSDataSyncProcessor();

        switch(reasonForFreeChargeCode){
            case NotPartOfUsageBilling:
            case AlreadyBilled:
                // All the paychecks on the new profile should be considered for billing
                assertCalculatedUsageData(newCompany, billPeriodStartDate, 0, 0, employeeUsageFoundCode);
                break;
            case UsageTransfer:
                // All the paychecks belonging to the old profile needs to be Usage Transferred to the new profile
                assertCalculatedUsageData(oldCompany, oldEntitlement, billPeriodStartDate, 0, 0, employeeUsageFoundCode);

                // All the paychecks on the new profile should be considered for billing
                assertCalculatedUsageData(newCompany, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, employeeUsageFoundCode);
                break;
            default:
                throw new UnsupportedOperationException(String.format("ReasonForFreeCharge code of type %s is not supported", reasonForFreeChargeCode));
        }

        if(generateBill){
            generateBillAndAssertClosedBill(oldCompany, oldEntitlement, newCompany, newEntitlement, billPeriodStartDate, expectedBillUsageCount, reasonForFreeChargeCode);
        }
    }

    public void resendPayrollAndAssertBill(List<OFX> requestOFXs, Company newCompany, Entitlement newEntitlement, SpcfCalendar billPeriodStartDate, int expectedEmployeeUsageCount, int expectedBillUsageCount, EmployeeUsageFoundCode employeeUsageFoundCode, boolean generateBill) throws Exception {
        // Resend all the paychecks from the previous profile
        Map<String, String> ofxValues = new HashMap<>();
        ofxValues.put("SourceCompanyId", newCompany.getSourceCompanyId());
        // Initial token is always 4
        ofxValues.put("RequestToken", "4");
        reSubmitUsageDataFromOFX(requestOFXs, ofxValues);
        runPSPToEMSBSDataSyncProcessor();

        assertCalculatedUsageData(newCompany, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, employeeUsageFoundCode);

        if(generateBill){
            runEMSBSToBRMDataSyncProcessor(billPeriodStartDate);
            assertFinalBillData(newCompany, newEntitlement, billPeriodStartDate, true, expectedBillUsageCount);
        }
    }

    public void recreatePayrollAndAssertBill(Company oldCompany, Entitlement oldEntitlement,
                                           Company newCompany, Entitlement newEntitlement,
                                           SpcfCalendar billPeriodStartDate, List<SpcfCalendar> paycheckDates, List<EmployeeDTO> employeeDTOs,
                                           ReasonForFreeChargeCode reasonForFreeChargeCode, EmployeeUsageFoundCode employeeUsageFoundCode, boolean generateBill) throws Exception {
        int expectedEmployeeUsageCount = paycheckDates.size();
        int expectedBillUsageCount = employeeDTOs.size();

        submitPayrollAndAssertOpenBill(newCompany, oldEntitlement, paycheckDates, employeeDTOs, expectedEmployeeUsageCount, expectedBillUsageCount, true, true);
        runPSPToEMSBSDataSyncProcessor();

        switch(reasonForFreeChargeCode){
            case NotPartOfUsageBilling:
            case AlreadyBilled:
                // All the paychecks on the new profile should be considered for billing
                assertCalculatedUsageData(newCompany, billPeriodStartDate, 0, 0, employeeUsageFoundCode);
                break;
            case UsageTransfer:
                // All the paychecks belonging to the old profile needs to be Usage Transferred to the new profile
                assertCalculatedUsageData(oldCompany, oldEntitlement, billPeriodStartDate, 0, 0, employeeUsageFoundCode);

                // All the paychecks on the new profile should be considered for billing
                assertCalculatedUsageData(newCompany, billPeriodStartDate, expectedEmployeeUsageCount, expectedBillUsageCount, employeeUsageFoundCode);
                break;
            default:
                throw new UnsupportedOperationException(String.format("ReasonForFreeCharge code of type %s is not supported", reasonForFreeChargeCode));
        }

        if(generateBill){
            generateBillAndAssertClosedBill(oldCompany, oldEntitlement, newCompany, newEntitlement, billPeriodStartDate, expectedBillUsageCount, reasonForFreeChargeCode);
        }
    }

    public void generateBillAndAssertClosedBill(Company oldCompany, Entitlement oldEntitlement,
                                                Company newCompany, Entitlement newEntitlement,
                                                SpcfCalendar billPeriodStartDate, int billUsageCount,
                                                ReasonForFreeChargeCode reasonForFreeChargeCode){
        runEMSBSToBRMDataSyncProcessor(billPeriodStartDate);

        switch(reasonForFreeChargeCode){
            case NotPartOfUsageBilling:
            case AlreadyBilled:
                // No Employee should be charged under the new profile
                assertFinalBillData(newCompany, newEntitlement, billPeriodStartDate, false, 0);
                break;
            case UsageTransfer:
                // No Employee should be charged under the old profile
                assertFinalBillData(oldCompany, oldEntitlement, billPeriodStartDate, false, 0);

                // All the employees should be charged under the new profile
                assertFinalBillData(newCompany, newEntitlement, billPeriodStartDate, true, billUsageCount);
                break;
            default:
                throw new UnsupportedOperationException(String.format("ReasonForFreeCharge code of type %s is not supported", reasonForFreeChargeCode));
        }
    }


    /**
     * Disable the old Entitlement Unit and create new Entitlement Unit under the given Entitlement
     *
     * @param entitlementUnit
     * @param newEntitlement
     * @return
     */
    public Company migrateProfile(EntitlementUnit entitlementUnit, Entitlement newEntitlement){
        deactivateServices(company, ServiceCode.DirectDeposit, ServiceCode.Cloud);

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        Company newCompany = setupDDCompany("8574537", company.getFedTaxId(),  newEntitlement.getLicenseNumber(), newEntitlement.getEntitlementOfferingCode());
        updateBDOMForCompany(newCompany, 15);

        Application.beginUnitOfWork();
        Application.refresh(newCompany);
        EntitlementUnit primaryEntitlementUnit = newCompany.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        Application.rollbackUnitOfWork();

        return newCompany;
    }

    public void migrateLicense(EntitlementUnit entitlementUnit, String licenseNumber, String entitlementOfferingCode) {
        migrateLicense(entitlementUnit, licenseNumber, entitlementOfferingCode, AssetItemNumber.DIY_YEARLY);
    }

    public void migrateLicense(EntitlementUnit entitlementUnit, String licenseNumber, String entitlementOfferingCode, AssetItemNumber assetItemNumber) {
        deactivateServices(company, ServiceCode.DirectDeposit);

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);
        switch (assetItemNumber){
            case DIY_YEARLY:
                DataLoadServices.addEntitlementUnit(company, licenseNumber, entitlementOfferingCode);
                break;
            case DIY_USAGE_BILLING_YEARLY:
            case DIY_USAGE_BILLING_LOWBASE:
            case DIY_USAGE_BILLING_MONTHLY:
                DataLoadServices.addEntitlementUnit(company, licenseNumber, entitlementOfferingCode, EditionType.Enhanced, null, assetItemNumber, null);
                break;
        }

        Application.beginUnitOfWork();
        Application.refresh(company);
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        Application.rollbackUnitOfWork();
    }

    public void deactivateServices(Company company, ServiceCode... serviceCodes){
        Application.beginUnitOfWork();
        for (ServiceCode serviceCode: serviceCodes) {
            DeactivateServiceCore cancelServiceCoreProcess = new DeactivateServiceCore(company.getSourceSystemCd(),
                    company.getSourceCompanyId(), serviceCode);
            ProcessResult cancelServiceProcessResult = cancelServiceCoreProcess.execute();
            assertTrue(cancelServiceProcessResult.isSuccess());
        }
        Application.commitUnitOfWork();
    }

    private MultiValuedMap<EmployeeUsageCode,EmployeeUsage> findEmployeeUsages(CompanyUsage companyUsage, SpcfCalendar paycheckDate){
        Bill bill = findBill(companyUsage, paycheckDate);

        DomainEntitySet<PaycheckUsage> paycheckUsageCollection = bill.getPaycheckUsageCollection();

        MultiValuedMap<EmployeeUsageCode, EmployeeUsage> multiValuedMap = new HashSetValuedHashMap<>();
        for (PaycheckUsage paycheckUsage: paycheckUsageCollection) {

            switch (paycheckUsage.getReasonForFreeCharge()){
                case AlreadyBilled:
                    multiValuedMap.put(EmployeeUsageCode.AlreadyBilled, paycheckUsage.getEmployeeUsage());
                    break;
                case NotPartOfUsageBilling:
                    multiValuedMap.put(EmployeeUsageCode.NotPartOfUsageBilling, paycheckUsage.getEmployeeUsage());
                    break;
                default:
                    multiValuedMap.put(EmployeeUsageCode.EligibleForBilling, paycheckUsage.getEmployeeUsage());break;

            }
        }

        return multiValuedMap;
    }

    private Company findCompany(PaycheckUsage paycheckUsage){
        CompanyUsage historicalCompanyUsage = paycheckUsage.getBill().getCompanyUsage();
        Company historicalCompany = Company.findCompanyNoEagerLoad(historicalCompanyUsage.getSourceCompanyId(), SourceSystemCode.QBDT);
        assertNotNull(historicalCompany);
        return historicalCompany;
    }

    private CompanyUsage findCompanyUsage(String sourceCompanyId, SourceSystemCode sourceSystemCd, String licenseNumber, String entitlementOfferingCode){
        Application.beginUnitOfWork();
        CompanyUsage companyUsage = CompanyUsage.findCompanyUsage(sourceCompanyId, sourceSystemCd, licenseNumber, entitlementOfferingCode);
        Application.rollbackUnitOfWork();
        return companyUsage;
    }

    public enum EmployeeUsageCode {
        AlreadyBilled,
        NotPartOfUsageBilling,
        EligibleForBilling
    }
}
