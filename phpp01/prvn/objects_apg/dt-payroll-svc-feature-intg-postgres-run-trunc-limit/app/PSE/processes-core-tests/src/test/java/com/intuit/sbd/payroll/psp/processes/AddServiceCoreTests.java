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

import com.intuit.client.ius.GrantType;
import com.intuit.client.ius.IUSGrantClient;
import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DDServiceInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.ius.IUSDataGenerator;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.iam.AddOrUpdateTRONGrantProcessor;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;


public class AddServiceCoreTests {

    private static final String PAYROLL_PLUGIN_ASSET_ALIAS = "Intuit.payroll.dirctdeposit.qbdtpayrolltronexp";
    private IUSDataGenerator iusDataGenerator = new IUSDataGenerator();
    private RealmManager realmManager = new RealmManager();

    private DataLoader dataloader;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        dataloader = new DataLoader();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void addServiceCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();

        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ddCompanyService);
        PayrollServices.commitUnitOfWork();

        assertEquals(0, ddServiceAddProcessResult.getMessages().size());
        assertNotNull(company1);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        DDCompanyServiceInfo ddCompServiceInfo = (DDCompanyServiceInfo) CompanyService
                .findCompanyService(foundCompany, ServiceCode.DirectDeposit);

        assertNotNull(ddCompServiceInfo);
        SpcfMoney expectedAvgPayRunAmt = new SpcfMoney("150.00");
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
    public void addServiceCoreWithPayrollGrantAddSuccess() {
        try {
            setPayrollPluginContext();
            PayrollServices.beginUnitOfWork();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
            Company company = dataloader.persistTestIntuitCompany();
            company.setIAMRealmId(getNewRealmId());
            ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();

            PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), ddCompanyService);
            PayrollServices.commitUnitOfWork();
            checkServices(company, ServiceSubStatusCode.PendingBankVerification, ServiceCode.DirectDeposit);

            // Check whether the Grant has FeatureSetObj with DIRECT_DEPOSIT optional feature with ACTIVE status
            Grant grant = realmManager.findPayrollGrant(company.getIAMRealmId());
            assetPayrollGrant(grant);
            assertPayrollGrantEntitlementInfo(grant);
            assertPayrollGrantTRONFeatureSet(grant, "DIY", "1099581", "ACTIVE");

        } finally {
            removePayrollPluginContext();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
        }
    }

    @Test
    public void addServiceCoreWithPayrollGrantAddFailure() {
        try {
            setPayrollPluginContext();
            PayrollServices.beginUnitOfWork();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
            Company company = dataloader.persistTestIntuitCompany();
            company.setIAMRealmId("1234567890");
            ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();

            ProcessResult<CompanyService> companyServiceProcessResult = PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), ddCompanyService);
            // Check Add Direct Deposit Service fails due to "Add Grant" failure
            assertFalse(companyServiceProcessResult.isSuccess());

            Message message = companyServiceProcessResult.getMessages().get(0);
            assertEquals("10126", message.getMessageCode());
            assertEquals("IUS Grant generic error for realm 1234567890", message.getMessage());

            PayrollServices.rollbackUnitOfWork();
        } finally {
            removePayrollPluginContext();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
        }
    }

    @Test
    public void addServiceCoreWithPayrollGrantUpdateSuccess() {
        try {
            if(!StringUtils.equalsIgnoreCase(FeatureFlags.get().stringValue(FeatureFlags.Key.REALMID_GUID_SYNC_FLAG,"DISABLE"), "WRITE")) {
                return;
            }

            setPayrollPluginContext();
            PayrollServices.beginUnitOfWork();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
            Company company = dataloader.persistTestIntuitCompany();
            company.setIAMRealmId(getNewRealmId());
            // Add the grant before adding the Direct Deposit Service
            AddOrUpdateGrantProcessor addOrUpdateGrantProcessor = new AddOrUpdateGrantProcessor(company, company.getIAMRealmId());
            ProcessResult<Grant> grantProcessResult = addOrUpdateGrantProcessor.execute();

            // Check the grant doesn't have FeatureSetObj
            Grant grant = grantProcessResult.getResult();
            assetPayrollGrant(grant);
            assertPayrollGrantEntitlementInfo(grant);
            assertNull(grant.getFeatureSetObj());
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();
            PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), ddCompanyService);
            PayrollServices.commitUnitOfWork();

            checkServices(company, ServiceSubStatusCode.PendingBankVerification, ServiceCode.DirectDeposit);

            // Check whether the Grant has FeatureSetObj with DIRECT_DEPOSIT optional feature with ACTIVE status
            grant = realmManager.findPayrollGrant(company.getIAMRealmId());
            assetPayrollGrant(grant);
            assertPayrollGrantEntitlementInfo(grant);
            assertPayrollGrantTRONFeatureSet(grant, "DIY", "1099581", "ACTIVE");
        } finally {
            removePayrollPluginContext();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
        }
    }

    @Test
    public void addServiceCoreWithNoGrantChangeSuccess() {
        try {
            setPayrollPluginContext();
            PayrollServices.beginUnitOfWork();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
            Company company = dataloader.persistTestIntuitCompany();
            company.setIAMRealmId(getNewRealmId());
            ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();

            PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), ddCompanyService);
            PayrollServices.commitUnitOfWork();
            checkServices(company, ServiceSubStatusCode.PendingBankVerification, ServiceCode.DirectDeposit);

            Grant grant = realmManager.findPayrollGrant(company.getIAMRealmId());
            assetPayrollGrant(grant);
            assertPayrollGrantEntitlementInfo(grant);
            assertPayrollGrantTRONFeatureSet(grant, "DIY", "1099581", "ACTIVE");

            // Trying to update the grant again will skip the payroll grant update
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            AddOrUpdateTRONGrantProcessor addOrUpdateTRONGrantProcessor = new AddOrUpdateTRONGrantProcessor(company);
            addOrUpdateTRONGrantProcessor.execute();
            PayrollServices.commitUnitOfWork();

            // Check the Grant is same as the first time it got created
            grant = realmManager.findPayrollGrant(company.getIAMRealmId());
            assetPayrollGrant(grant);
            assertPayrollGrantEntitlementInfo(grant);
            assertPayrollGrantTRONFeatureSet(grant, "DIY", "1099581", "ACTIVE");

        } finally {
            removePayrollPluginContext();
            dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBOE);
        }
    }

    @Test
    public void addServiceCoreNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(SourceSystemCode.QBOE, null, null);
        PayrollServices.commitUnitOfWork();

        assertEquals("Messages size", 1, ddServiceAddProcessResult.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addServiceCoreNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(null, "1234567", null);
        PayrollServices.commitUnitOfWork();
        assertEquals("Messages size", 1, ddServiceAddProcessResult.getMessages().size());
        Message errorMessage = ddServiceAddProcessResult.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addServiceCoreNullService() {
        PayrollServices.beginUnitOfWork();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();

        ProcessResult<Company> result = DataLoader.addCompany(company1);
        assertEquals(0, result.getMessages().size());

        Company domainCompany = result.getResult();

        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(domainCompany.getSourceSystemCd(), domainCompany.getSourceCompanyId(), null);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(1, ddServiceAddProcessResult.getMessages().size());
        Assert.assertEquals("117", ddServiceAddProcessResult.getMessages().get(0).getMessageCode());
        Assert.assertEquals("Company Service Info is not specified.",
                ddServiceAddProcessResult.getMessages().get(0).getMessage());
    }

    @Test
    public void addServiceCoreServiceTermed() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);

        ddCompanyService.setStatusCd(ServiceSubStatusCode.Terminated);
        PayrollServicesTest.save(company1);

        DDServiceInfoDTO service2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), service2);
        System.out.println(ddServiceAddProcessResult2);
        assertFalse(ddServiceAddProcessResult2.isSuccess());
        assertEquals(2, ddServiceAddProcessResult2.getMessages().size());
        Assert.assertEquals("1101", ddServiceAddProcessResult2.getMessages().get(0).getMessageCode());
        Assert.assertEquals("The operation AddService is not allowed for company QBOE:123456 in its current state.",
                ddServiceAddProcessResult2.getMessages().get(0).getMessage());
        Assert.assertEquals("1012", ddServiceAddProcessResult2.getMessages().get(1).getMessageCode());
        Assert.assertEquals("Company QBOE:123456 was previously terminated and cannot be added.",
                ddServiceAddProcessResult2.getMessages().get(1).getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreServiceExists() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        CompanyService ddCompanyService = dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DDServiceInfoDTO dtoService = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(
                company1.getSourceSystemCd(), company1.getSourceCompanyId(), dtoService);
        PayrollServices.commitUnitOfWork();
        System.out.println(ddServiceAddProcessResult2);
        assertFalse(ddServiceAddProcessResult2.isSuccess());
        assertEquals(1, ddServiceAddProcessResult2.getMessages().size());
        Assert.assertEquals("1013", ddServiceAddProcessResult2.getMessages().get(0).getMessageCode());
        Assert.assertEquals("Company QBOE:123456 already exists on the DirectDeposit service.",
                ddServiceAddProcessResult2.getMessages().get(0).getMessage().trim());
    }

    @Test
    public void testCompanyDNE() {
        DDServiceInfoDTO dtoService = dataloader.getTestCompanyService();
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> submitDDPayroll = PayrollServices.companyManager.addService(SourceSystemCode.QBOE, "1234567", dtoService);
        PayrollServices.commitUnitOfWork();
        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1234567 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void testInvalidDTO() {
        DDServiceInfoDTO dtoService = dataloader.getTestCompanyService();
        dtoService.setAveragePayrollAmount(new BigDecimal("1234.444"));
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> submitDDPayroll = PayrollServices.companyManager.addService(SourceSystemCode.QBOE, "1234567", dtoService);
        PayrollServices.commitUnitOfWork();
        assertFalse(submitDDPayroll.isSuccess());

        assertEquals("Messages size", 1, submitDDPayroll.getMessages().size());
        Message errorMessage = submitDDPayroll.getMessages().get(0);
        assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        assertEquals("Error message", "AveragePayrollAmount has invalid value", errorMessage.getMessage());
    }

    @Test
    public void testServiceNotOnOffering() {

        PayrollServices.beginUnitOfWork();
        CompanyDTO dtoCompany = dataloader.getTestIntuitCompany();
        ProcessResult<Company> resultCompany = PayrollServices.companyManager.addCompany(dtoCompany);
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue(resultCompany.isSuccess());

        // try to add the DD service - should work
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO dtoDDCompanyService = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> resultService = PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()), dtoCompany.getCompanyId(), dtoDDCompanyService);
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue(resultService.isSuccess());
        
        // try to add the DD service again - should fail because the company already has an offering with the DD service
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO dtoDDCompanyService2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> resultService2 = PayrollServices.companyManager.addService(
                SourceSystemCode.valueOf(dtoCompany.getSourceSystemCd().toString()), dtoCompany.getCompanyId(), dtoDDCompanyService2);
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue(!resultService2.isSuccess());
        Assert.assertTrue(resultService2.getMessages().size() == 1);
        assertEquals("Message code", "1013", resultService2.getMessages().get(0).getMessageCode()); // invalid value
        assertEquals("Message", "Company QBOE:123456 already exists on the DirectDeposit service.", resultService2.getMessages().get(0).getMessage()); // invalid value
    }


    @Test
    public void addServiceCoreSuccess_BillPayment() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();

        ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();

        ProcessResult<CompanyService> ddServiceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), ddCompanyService);
        CompanyService ddService = ddServiceAddProcessResult.getResult();
        ddService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        ddService = Application.save(ddService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO companyService = dataloader.getTestCompanyBillPaymentService();

        ProcessResult<CompanyService> serviceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), companyService);


        PayrollServices.commitUnitOfWork();

        assertEquals(0, serviceAddProcessResult.getMessages().size());
        assertNotNull(company1);

        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.BillPayment);

        assertNotNull(serviceInfo);
        FundingModel fundingModel = PayrollServices.entityFinder.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);
        assertEquals("Funding Model", fundingModel.getFundingModelCd(), serviceInfo.getEffectiveFundingModel().getFundingModelCd());

        assertEquals("Service code", ServiceCode.BillPayment, serviceInfo.getService().getServiceCd());
        PayrollServices.commitUnitOfWork();
    }
    
    @Test
    public void addServiceCoreSuccess_Cloud() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud);
        DataLoadServices.activateCloudService(company);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.Cloud);
    }

    @Test
    public void addServiceCoreSuccess_Tax() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.Tax);
        RAFEnrollment rafEnrollment = company.getCurrentRAFEnrollment();
        assertNotNull(rafEnrollment);
        assertEquals("RAF Status", RAFEnrollmentStatus.PendingEnrollment, rafEnrollment.getStatus());
        assertEquals("Company's IRS agency", CompanyAgency.findCompanyAgency(company, Agency.IRS),rafEnrollment.getCompanyAgency());
    }

    @Test
    public void addServiceCoreSuccess_DD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.DirectDeposit);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.DirectDeposit);
    }

    @Test
    public void addServiceCoreSuccess_CloudThenDD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.Cloud, ServiceCode.DirectDeposit);
    }

    @Test
    public void addServiceCoreSuccess_CloudThenTax() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.Cloud, ServiceCode.Tax);
    }

    @Test
    public void addServiceCoreSuccess_CloudDD401k() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.ThirdParty401k);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.ThirdParty401k);
    }

    @Test
    public void addServiceCoreSuccess_CloudDDBP401k() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.ThirdParty401k, ServiceCode.BillPayment);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.Cloud, ServiceCode.DirectDeposit, ServiceCode.ThirdParty401k, ServiceCode.BillPayment);
    }

    @Test
    public void addServiceCoreSuccess_TaxWorkersComp() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax, ServiceCode.WorkersComp);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.WorkersComp);
    }

    @Test
    public void addServiceCoreHold_TaxWorkersComp() {
        Company company = DataLoadServices.setupAssistedCompanyForCA("123456789", 2, true);
        DataLoadServices.addCompanyOnHoldReason(company, ServiceSubStatusCode.Fraud);
        DataLoadServices.addWorkersCompService(company);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.WorkersComp);
    }

    @Test
    public void addServiceCoreSuccess_CloudTaxDD() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax, ServiceCode.DirectDeposit);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.Cloud, ServiceCode.Tax);
        checkServices(company, ServiceSubStatusCode.PendingFirstPayroll, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        checkServices(company, ServiceSubStatusCode.ActiveCurrent, ServiceCode.DirectDeposit);
    }

    @Test
    public void addServiceCore_EINInUse_Terminated_Company_SameSrcSys() {
        //moved from AddCompanyCoreTests.addCompanyCore_EINInUse_Terminated_Company_SameSrcSys
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        PayrollServices.companyManager.addService(SourceSystemCode.valueOf(company1.getSourceSystemCd().toString()),
                company1.getCompanyId(), AddCompanyDataLoader.dataloader.getTestCompanyService());
        PayrollServices.commitUnitOfWork();
        junit.framework.Assert.assertEquals(0, result.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyService> servicePR = PayrollServices.companyManager.terminateService(SourceSystemCode.QBOE, "123456", ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();
        assertSuccess(servicePR);

        CompanyDTO company2 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        company2.setDBA("Turbo Tax");
        company2.setCompanyId("YXYYX");
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result2 = PayrollServices.companyManager.addCompany(company2);
        PayrollServices.commitUnitOfWork();
        junit.framework.Assert.assertEquals(0, result2.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.companyManager.addService(SourceSystemCode.QBOE, "YXYYX", DataLoadServices.createDDServiceInfo());
        assertSuccess(pr);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(result2.getResult());
        assertNotNull(result2.getResult().getCurrentOnHoldReason(ServiceSubStatusCode.FraudReview));
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void addServiceCoreCloudActiveOnDifferentPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();        
        PayrollServices.commitUnitOfWork();
        dataloader.addCloudService(company1);

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        DDServiceInfoDTO service2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreCloudActiveOnDifferentTermedPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();
        dataloader.addCloudService(company1);

        PayrollServices.beginUnitOfWork();
        company1 = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        company1.getService(ServiceCode.Cloud).setStatusCd(ServiceSubStatusCode.Terminated);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        DDServiceInfoDTO service2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreDDActiveCloudOnDifferentPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();
        dataloader.addCloudService(company1);

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        DDServiceInfoDTO service2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreDDActiveOnDifferentPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        dataloader.persistTestCompanyService(company1);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        DDServiceInfoDTO service2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertFalse(ddServiceAddProcessResult2.isSuccess());
        assertEquals(1, ddServiceAddProcessResult2.getMessages().size());
        Assert.assertEquals("1511", ddServiceAddProcessResult2.getMessages().get(0).getMessageCode());
        Assert.assertEquals("DirectDeposit could not be added because another company with EIN 123456789 has DirectDeposit active.",
                ddServiceAddProcessResult2.getMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreCloudActiveDDCloudOnDifferentPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();
        dataloader.addDDService(company1);

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        ServiceInfoDTO service2 = dataloader.getCloudCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreTaxActiveCloudOnDifferentPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();
        dataloader.addCloudService(company1);

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        DDServiceInfoDTO service2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreTaxActiveOnDifferentPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();
        dataloader.addTaxService(company1);

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        DDServiceInfoDTO service2 = dataloader.getTestCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertFalse(ddServiceAddProcessResult2.isSuccess());
        assertEquals(1, ddServiceAddProcessResult2.getMessages().size());
        Assert.assertEquals("1511", ddServiceAddProcessResult2.getMessages().get(0).getMessageCode());
        Assert.assertEquals("DirectDeposit could not be added because another company with EIN 123456789 has Tax active.",
                ddServiceAddProcessResult2.getMessages().get(0).getMessage());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void addServiceCoreCloudActiveTaxOnDifferentPSID() {
        PayrollServices.beginUnitOfWork();
        Company company1 = dataloader.persistTestIntuitCompany();
        PayrollServices.commitUnitOfWork();
        dataloader.addTaxService(company1);

        PayrollServices.beginUnitOfWork();
        Company company2 = dataloader.persistTestActiveCompany(company1.getFedTaxId(), "999999992");

        ServiceInfoDTO service2 = dataloader.getCloudCompanyService();
        ProcessResult<CompanyService> ddServiceAddProcessResult2 = PayrollServices.companyManager.addService(company2.getSourceSystemCd(), company2.getSourceCompanyId(), service2);

        System.out.println(ddServiceAddProcessResult2);
        assertTrue(ddServiceAddProcessResult2.isSuccess());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testAddServiceMatchingTermedEINIsAllowedButPlacedOnFraudReview() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, "123456789", "112345678", true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.terminateService(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit));
        PayrollServices.commitUnitOfWork();

        Company matchingCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, "987654321", "112345678", true, ServiceCode.Cloud);

        DataLoadServices.addDDService(matchingCompany);

        PayrollServices.beginUnitOfWork();
        Application.refresh(matchingCompany);
        assertNotNull(matchingCompany.getCurrentOnHoldReason(ServiceSubStatusCode.FraudReview));
        PayrollServices.rollbackUnitOfWork();
    }

    private void checkServices(Company pCompany, ServiceSubStatusCode pServiceSubStatusCode, ServiceCode... pServiceCodes) {
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company
                .findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        for (ServiceCode serviceCode : pServiceCodes) {
            CompanyService companyService = CompanyService.findCompanyService(foundCompany, serviceCode);

            assertNotNull(companyService);
            assertEquals(companyService.getService().getServiceCd().toString(), pServiceSubStatusCode, companyService.getStatusCd());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    private String getNewRealmId() {
        return iusDataGenerator.createRealmUsingSystemTicket("1", "TRON", "qbdttrontest+231020001@gmail.com");
    }

    private void setPayrollPluginContext() {
        IntuitContext intuitContext = new IntuitContext();
        intuitContext.setAssetAlias(PAYROLL_PLUGIN_ASSET_ALIAS);
        RequestAttributesUtils.setAttribute(ContextConstants.INTUIT_CONTEXT, intuitContext);
    }

    private void removePayrollPluginContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.INTUIT_CONTEXT);
    }

    private void assetPayrollGrant(Grant grant) {
        assertNotNull("Grant not found for the Realm", grant);
        assertEquals(GrantType.OFFERING_APP_GRANT.name(), grant.getGrantType());
        assertEquals(IUSGrantClient.EWS_GRANT_OFFERING_ID, grant.getOfferingId());
        assertEquals("ACTIVE", grant.getStatus());
    }

    private void assertPayrollGrantEntitlementInfo(Grant grant) {
        List<NameValuePair> nameValuePairList = grant.getEntitlementInfo();
        for(NameValuePair nameValuePair: nameValuePairList) {
            switch (nameValuePair.getName()) {
                case "SOURCE":
                    assertEquals("Payroll", nameValuePair.getValue());
                    break;
                case "SUBSCRIPTION_NUMBER":
                    //assertEquals("8000205", nameValuePair.getValue());
                    break;
            }
        }
    }

    private void assertPayrollGrantTRONFeatureSet(Grant grant, String featureSetCode, String grantFeatureCode, String optionalFeatureStatus) {
        FeatureSetObject featureSetObject = grant.getFeatureSetObj();
        assertNotNull(featureSetObject);

        assertEquals(featureSetCode, featureSetObject.getFeatureSetCode());

        List<GrantFeature> grantFeatures = featureSetObject.getFeatures();

        GrantFeature grantFeature = grantFeatures.get(0);

        assertEquals(grantFeatureCode, grantFeature.getCode());
        assertEquals("FEATURE", grantFeature.getType());

        List<OptionalFeature> optionalFeatures = featureSetObject.getOptionalFeatures();

        OptionalFeature optionalFeature = optionalFeatures.get(0);

        assertEquals("DIRECT_DEPOSIT", optionalFeature.getCode());
        assertEquals(optionalFeatureStatus, optionalFeature.getStatus());
        assertEquals("PAID", optionalFeature.getServiceStatus());
    }
}
