package com.intuit.sbd.payroll.psp.processes;

import com.intuit.client.ius.GrantType;
import com.intuit.client.ius.IUSGrantClient;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.platform.integration.ius.common.types.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.accountservices.AccountServicesDataGenerator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.iam.realm.RealmManager;
import com.intuit.sbd.payroll.psp.ius.IUSDataGenerator;
import com.intuit.sbd.payroll.psp.processes.util.WorkersCompTestUtil;
import com.intuit.sbd.payroll.psp.workflows.WorkflowState;
import com.intuit.sbd.payroll.psp.workflows.Workflows;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * User: michaelp696
 */
public class DeactivateServiceCoreTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /*
     * Test that ensure when deactivating tax service from a company workers comp service is not cancelled.
     * Use case: Assisted customer turns to DIY
     */
    @Test
    public void testDeactivateTaxNotImpactsWorkersComp() {
        String psid = "123456789";
        Company taxCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(psid);
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, psid, ServiceCode.Tax);
        PayrollServices.commitUnitOfWork();
        taxCompany = DataLoadServices.refreshCompany(taxCompany);
        CompanyService taxService = taxCompany.getService(ServiceCode.Tax);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, taxService.getStatusCd());
        CompanyService workersCompService = taxCompany.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());
    }
    /*
     * Prevent cancel emails from going out for workers comp service -
     */
    @Test
    public void testDeactivateWorkersComp_PSRV004137() {
        String psid = "123456789";
        Company taxCompany = WorkersCompTestUtil.createAssistedCompanyWithWorkersCompService(psid);
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT,psid,ServiceCode.WorkersComp);
        PayrollServices.commitUnitOfWork();
        taxCompany = DataLoadServices.refreshCompany(taxCompany);
        CompanyService workersCompService = taxCompany.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.Cancelled, workersCompService.getStatusCd());
        DomainEntitySet<CompanyEvent> ce = CompanyEvent.findCompanyEvents(taxCompany, EventTypeCode.ServiceStatusChange) ;
        //Cancellation fires a ServiceStatusChange Event - assert the event is fired
        CompanyEvent workersCompCancelledEvent = null;
        for(CompanyEvent event: ce) {
            DomainEntitySet<CompanyEventDetail> ced = Application.find(CompanyEventDetail.class,CompanyEventDetail.CompanyEvent().equalTo(event).And(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.ServiceCode)).And(CompanyEventDetail.Value().equalTo("Workers Compensation Service"))) ;
            DomainEntitySet<CompanyEventDetail> ced2 = Application.find(CompanyEventDetail.class,CompanyEventDetail.CompanyEvent().equalTo(event).And(CompanyEventDetail.EventDetailTypeCd().equalTo(EventDetailTypeCode.NewServiceStatus)).And(CompanyEventDetail.Value().equalTo("Cancelled"))) ;
            if(ced!=null && ced.size()>0 && ced2!=null && ced2.size()>0)  {
                workersCompCancelledEvent = event;
                break;
            }
        }
        Assert.assertNotNull(workersCompCancelledEvent);
        // Assert that the workersCompCancelledEvent did not queue an email
        DomainEntitySet<CompanyEventEmail> cee = Application.find(CompanyEventEmail.class, CompanyEventEmail.CompanyEvent().equalTo(workersCompCancelledEvent));
        Assert.assertEquals(cee.size(),0);
    }

    @Test
    public void deactivateServiceCoreWithPayrollGrantUpdateSuccess() {
        DataLoader dataloader = new DataLoader();
        RealmManager realmManager = new RealmManager();
        PaymentsAccount paymentsAccount = createPaymentsAccount();

        PayrollServices.beginUnitOfWork();
        dataloader.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Company company = dataloader.persistTestIntuitCompany();
        company.setIAMRealmId(paymentsAccount.getRealmId());
        company.setWorkflowState(Workflows.OII, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.ACTIVATE_DIRECT_DEPOSIT, WorkflowState.ENABLED);
        company.setWorkflowState(Workflows.MONEY_MOVEMENT_ONBOARDING, WorkflowState.ENABLED);
        ServiceInfoDTO ddCompanyService = dataloader.getTestCompanyService();

        // Add DD Services
        PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), ddCompanyService);
        PayrollServices.commitUnitOfWork();
        checkServices(company, ServiceSubStatusCode.PendingBankVerification, ServiceCode.DirectDeposit);

        // Check whether grant has FeatureSetObj with DIRECT_DEPOSIT optional feature with active status
        Grant grant = realmManager.findPayrollGrant(company.getIAMRealmId());
        assetPayrollGrant(grant);
        assertPayrollGrantEntitlementInfo(grant);
        assertPayrollGrantTRONFeatureSet(grant, "DIY", "1099581", "ACTIVE");

        // Deactivate DD Service
        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.deactivateService(SourceSystemCode.QBDT, company.getSourceCompanyId(), ServiceCode.DirectDeposit);
        PayrollServices.commitUnitOfWork();

        // Check whether grant has FeatureSetObj with DIRECT_DEPOSIT optional feature with Inactive status
        grant = realmManager.findPayrollGrant(company.getIAMRealmId());
        assetPayrollGrant(grant);
        assertPayrollGrantEntitlementInfo(grant);
        assertPayrollGrantTRONFeatureSet(grant, "DIY", "1099581", "INACTIVE");
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

    private PaymentsAccount createPaymentsAccount() {
        AccountServicesDataGenerator accountServicesHelper = new AccountServicesDataGenerator();
        PaymentsAccount paymentsAccount = accountServicesHelper.createPaymentsAccount("Approved");
        return paymentsAccount;
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
