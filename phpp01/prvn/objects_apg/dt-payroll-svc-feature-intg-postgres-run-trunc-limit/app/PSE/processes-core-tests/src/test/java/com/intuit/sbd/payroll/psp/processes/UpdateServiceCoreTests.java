/*
 * $Id: $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company401kDataloader;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Contains the unit tests for the <CODE>UpdateServiceCoreTests</CODE> class.
 *
 * @author: dmartens
 * @version: Aug 31, 2007
 */
public class UpdateServiceCoreTests {
    private DataLoader dataloader = new DataLoader();

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updServiceCoreNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.updateService(SourceSystemCode.QBOE, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, ddServiceAddProcessResult.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updServiceCoreNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.updateService(null, "1234567", null);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, ddServiceAddProcessResult.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void update401kServiceCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company401kDataloader c1401kDL = new Company401kDataloader();
        c1401kDL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany("1234567", SourceSystemCode.QBDT);
                ThirdParty401kServiceInfoDTO tp401kCompanyService = new ThirdParty401kServiceInfoDTO();

        tp401kCompanyService.setCustodialId("5555557777");

        ProcessResult<CompanyService> tp401kServiceUpdProcessResult = PayrollServices.companyManager.updateService(
                foundCompany.getSourceSystemCd(), foundCompany.getSourceCompanyId(), tp401kCompanyService);
        assertSuccess(tp401kServiceUpdProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        foundCompany = Company.findCompany("1234567", SourceSystemCode.QBDT);
        ThirdParty401kCompanyServiceInfo domain401kCompanyService = (ThirdParty401kCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.ThirdParty401k);

        assertNotNull(tp401kCompanyService);

        assertEquals("Custodial Id", "5555557777", domain401kCompanyService.getCustodialId());
        assertEquals("Company", foundCompany, domain401kCompanyService.getCompany());
        assertEquals("Service code", ServiceCode.ThirdParty401k, domain401kCompanyService.getService().getServiceCd());

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void updateServiceCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        DDServiceInfoDTO updatedDDService = dataloader.getTestCompanyService();

        updatedDDService.setAveragePayrollAmount(new BigDecimal("1550.00"));

        ProcessResult<CompanyService> ddServiceUpdProcessResult = PayrollServices.companyManager.updateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedDDService);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(0, ddServiceUpdProcessResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);

        assertNotNull(ddCompServiceInfo);
        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("1550.00");
        SpcfMoney expectedHighPayRunAmt = new SpcfMoney("250.00");

        assertEquals("Average payroll run amount", expectedAvgPayRunAmt, ddCompServiceInfo.getAveragePayRunAmount());
        assertEquals("High annual run amount", expectedHighPayRunAmt, ddCompServiceInfo.getHighAnnualPayAmount());
        assertEquals("Offload group", OffloadGroup.Codes.STANDARD,
                foundCompany.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Consecutive limit violations", 0L, ddCompServiceInfo.getConsecutiveLimitViolationCount());
        assertEquals("Company", foundCompany, ddCompServiceInfo.getCompany());
        assertNull("Comp Override", ddCompServiceInfo.getOverrideCompanyLimitAmount());
        assertNull("Emp Override", ddCompServiceInfo.getOverrideEmployeeLimitAmount());
        assertEquals("Service code", ServiceCode.DirectDeposit, ddCompServiceInfo.getService().getServiceCd());

        PayrollServices.commitUnitOfWork();        
    }

    @Test
    public void updateService_CompanyNotActive() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);

        DDServiceInfoDTO updatedDDService = dataloader.getTestCompanyService();
        updatedDDService.setAveragePayrollAmount(new BigDecimal("1550.00"));

        PayrollServices.companyManager.addOnHoldReason(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceSubStatusCode.Fraud);
        company1 = PayrollServicesTest.save(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceUpdProcessResult = PayrollServices.companyManager.updateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedDDService);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(1, ddServiceUpdProcessResult.getMessages().size());
        Message errorMessage = ddServiceUpdProcessResult.getMessages().get(0);
        assertEquals("Error message code", "1101", errorMessage.getMessageCode());
        assertEquals("Error message", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456 in its current state.",
                errorMessage.getMessage());
    }

    @Test
    public void updateService_ServiceNotActive() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);

        DDServiceInfoDTO updatedDDService = dataloader.getTestCompanyService();
        updatedDDService.setAveragePayrollAmount(new BigDecimal("1550.00"));
        // Deactivate company
        PayrollServices.companyManager.deactivateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ServiceCode.DirectDeposit);
        company1 = PayrollServicesTest.save(company1);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPrincipalToQBDT();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceUpdProcessResult = PayrollServices.companyManager.updateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedDDService);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(1, ddServiceUpdProcessResult.getMessages().size());
        Message errorMessage = ddServiceUpdProcessResult.getMessages().get(0);
        assertEquals("Error message code", "1101", errorMessage.getMessageCode());
        assertEquals("Error message", "The operation ChangeCompanyInfo is not allowed for company QBOE:123456 in its current state.",
                errorMessage.getMessage());
    }

    @Test
    public void updateService_CompanyDoesNotHaveService() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();

        DDServiceInfoDTO updatedDDService = dataloader.getTestCompanyService();
        updatedDDService.setAveragePayrollAmount(new BigDecimal("1550.00"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceUpdProcessResult = PayrollServices.companyManager.updateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), updatedDDService);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(1, ddServiceUpdProcessResult.getMessages().size());
        Message errorMessage = ddServiceUpdProcessResult.getMessages().get(0);
        assertEquals("Error message code", "117", errorMessage.getMessageCode());
        assertEquals("Error message", "Company Service Info is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updateServiceCoreNullFromService() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);

        DDServiceInfoDTO updatedDDService = dataloader.getTestCompanyService();

        updatedDDService.setAveragePayrollAmount(new BigDecimal("1550.00"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceUpdProcessResult = PayrollServices.companyManager.updateService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), null);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals(1, ddServiceUpdProcessResult.getMessages().size());
        Assert.assertEquals("117", ddServiceUpdProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("Company Service Info is not specified.",
                ddServiceUpdProcessResult.getMessages().get(0).getMessage());
    }

    @Test
    public void testServiceStartDateUpdate(){
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addFederalAndPAStateTaxCompanyLaws(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        SpcfCalendar newServiceStartDate = SpcfCalendar.createInstance(2011,02,05,SpcfTimeZone.getLocalTimeZone());
        ServiceInfoDTO serviceInfoDto = new DataLoader().getTaxCompanyService();
        serviceInfoDto.setServiceStartDate(newServiceStartDate);
        ProcessResult result = PayrollServices.companyManager.updateService(SourceSystemCode.QBDT, psid, serviceInfoDto);
        // Ensure processing was succsessful
        assertSuccess("UpdateServiceTaxResult", result);
        assertEquals("Messages size", 0, result.getMessages().size());
        for (CompanyAgency companyAgency : company.getCompanyAgencyCollection()) {
            assertEquals("Start date:", newServiceStartDate, companyAgency.getIntuitResponsibilityStartDate());
        }
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testReactivateAssisted() {
        //todo_rhn: add reactivate assisted test
    }

    @Test
    public void testReactivateDirectDeposit() {
        //todo_rhn: add reactivate DD test
    }

    
}
