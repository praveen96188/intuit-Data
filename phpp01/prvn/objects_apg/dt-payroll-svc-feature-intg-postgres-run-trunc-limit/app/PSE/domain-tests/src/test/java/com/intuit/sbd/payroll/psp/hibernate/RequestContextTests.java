package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertNull;

public class RequestContextTests {

    PSPRequestContextManager pspRequestContextManager;
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
        pspRequestContextManager.clearRequestContext();
    }

    @After
    public void runAfterEachTest() {
        pspRequestContextManager.clearRequestContext();
    }

    private Company createCompany() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        company1.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Company company = result.getResult();
        return company;
    }

    private Company createCompany1() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany2();
        company1.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Company company = result.getResult();
        return company;
    }


    @Test
    public void contextManagerTest_setRequestContext(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        Company company1 = createCompany1();
        PayrollServices.commitUnitOfWork();
        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "test");
        RequestContext requestContext = pspRequestContextManager.getRequestContext();
        //check request context is set correctly
        assertEquals(company.getId(), requestContext.getCompanyInfo().getCompanySequence());
        pspRequestContextManager.clearRequestContext();
        //check request context is set cleared
        assertNull(pspRequestContextManager.getRequestContext());
        pspRequestContextManager.setRequestContext(requestContext);
        RequestContext requestContext1 = pspRequestContextManager.getRequestContext();
        //check request context is set correctly
        assertEquals(requestContext.getCompanyInfo().getCompanySequence(), requestContext1.getCompanyInfo().getCompanySequence());
        pspRequestContextManager.clearRequestContext();
        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "test");
        pspRequestContextManager.setRequestContext(company1, RequestType.TEST, "test");
        requestContext = pspRequestContextManager.getRequestContext();
        //check request context is set correctly if set twice
        assertEquals(company1.getId(), requestContext.getCompanyInfo().getCompanySequence());
        pspRequestContextManager.clearRequestContext();
        //check request context is cleared
        assertNull(pspRequestContextManager.getRequestContext());

    }

    @Test
    public void contextManagerTest_setRequestContext_ActiveSession(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        Company company1 = createCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "test");
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        RequestContext requestContext = pspRequestContextManager.getRequestContext();
        //check request context is set correctly
        assertEquals(company.getId(), requestContext.getCompanyInfo().getCompanySequence());
        pspRequestContextManager.clearRequestContext();
        assertNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is set cleared
        assertNull(pspRequestContextManager.getRequestContext());
        pspRequestContextManager.setRequestContext(requestContext);
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        RequestContext requestContext1 = pspRequestContextManager.getRequestContext();
        //check request context is set correctly
        assertEquals(requestContext.getCompanyInfo().getCompanySequence(), requestContext1.getCompanyInfo().getCompanySequence());
        pspRequestContextManager.clearRequestContext();
        assertNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "test");
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        pspRequestContextManager.setRequestContext(company1, RequestType.TEST, "test");
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        requestContext = pspRequestContextManager.getRequestContext();
        //check request context is set correctly if set twice
        assertEquals(company1.getId(), requestContext.getCompanyInfo().getCompanySequence());
        pspRequestContextManager.clearRequestContext();
        assertNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is cleared
        assertNull(pspRequestContextManager.getRequestContext());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void contextManagerTest_setRequestContextCompany_ActiveSession_stack(){
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        Company company1 = createCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pspRequestContextManager.setRequestContext(null, RequestType.TEST, "test");

        pspRequestContextManager.setRequestContextCompany(company);
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());

        pspRequestContextManager.clearRequestContextCompany();
        assertNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is set cleared
        assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());

        pspRequestContextManager.setRequestContextCompany(company);
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is set correctly
        assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());

        pspRequestContextManager.setRequestContextCompany(company);
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is set correctly
        assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());

        pspRequestContextManager.clearRequestContextCompany();
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is set correctly
        assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());

        pspRequestContextManager.clearRequestContextCompany();
        assertNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is set correctly
        assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());


        pspRequestContextManager.setRequestContextCompany(company);
        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
        //check request context is set correctly
        assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());

        pspRequestContextManager.setRequestContextCompany(company1);
//        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
//        //check request context is set correctly
//        assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//
//        pspRequestContextManager.clearRequestContextCompany();
//        assertNotNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
//        //check request context is set correctly
//        assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//
//        pspRequestContextManager.clearRequestContextCompany();
//        assertNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
//        //check request context is set correctly
//        assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
//
//        pspRequestContextManager.clearRequestContextCompany();
//
//        assertNull(Application.getHibernateSession().getEnabledFilter("COMPANY_FILTER"));
//        //check request context is set correctly
        assertNull(pspRequestContextManager.getRequestContext());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void contextManagerTest_setRequestContextCompany_stack(){
        boolean enableStackBasedRequestContext = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_STACK_BASED_CONTEXT_MANAGER, false);
        if(enableStackBasedRequestContext) {

            PayrollServices.beginUnitOfWork();
            Company company = createCompany();
            Company company1 = createCompany1();
            PayrollServices.commitUnitOfWork();

            pspRequestContextManager.setRequestContext(null, RequestType.TEST, "test");
            pspRequestContextManager.setRequestContextCompany(company);
            //check request context is set correctly
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            //check request context is set cleared
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompany(company);
            pspRequestContextManager.setRequestContextCompany(company);
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.setRequestContextCompany(company1);
//            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            pspRequestContextManager.clearRequestContextCompany();
//            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            pspRequestContextManager.clearRequestContextCompany();
//            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
//            pspRequestContextManager.clearRequestContextCompany();
//            //check request context is cleared
            assertNull(pspRequestContextManager.getRequestContext());
        }

    }

    @Test
    public void contextManagerTest_setRequestContextCompanyByPSID_stack(){
        boolean enableStackBasedRequestContext = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_STACK_BASED_CONTEXT_MANAGER, false);
        if(enableStackBasedRequestContext) {
            PayrollServices.beginUnitOfWork();
            Company company = createCompany();
            Company company1 = createCompany1();
            PayrollServices.commitUnitOfWork();
            pspRequestContextManager.setRequestContext(null, RequestType.TEST, "test");
            pspRequestContextManager.setRequestContextCompanyFromPSID(company.getSourceCompanyId());
            //check request context is set correctly
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            //check request context is set cleared
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompanyFromPSID(company.getSourceCompanyId());
            pspRequestContextManager.setRequestContextCompanyFromPSID(company.getSourceCompanyId());
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.setRequestContextCompanyFromPSID(company1.getSourceCompanyId());
//            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            pspRequestContextManager.clearRequestContextCompany();
//            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            pspRequestContextManager.clearRequestContextCompany();
//            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
//            pspRequestContextManager.clearRequestContextCompany();
            //check request context is cleared
            assertNull(pspRequestContextManager.getRequestContext());
        }

    }

    @Test
    public void contextManagerTest_setRequestContextCompanyBySeq_stack(){
        boolean enableStackBasedRequestContext = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_STACK_BASED_CONTEXT_MANAGER, false);
        if(enableStackBasedRequestContext) {
            PayrollServices.beginUnitOfWork();
            Company company = createCompany();
            Company company1 = createCompany1();
            PayrollServices.commitUnitOfWork();
            pspRequestContextManager.setRequestContext(null, RequestType.TEST, "test");
            pspRequestContextManager.setRequestContextCompanyFromSeq(company.getId().toString());
            //check request context is set correctly
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            //check request context is set cleared
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompanyFromSeq(company.getId().toString());
            pspRequestContextManager.setRequestContextCompanyFromSeq(company.getId().toString());
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.setRequestContextCompanyFromSeq(company1.getId().toString());
//            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            pspRequestContextManager.clearRequestContextCompany();
//            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
//            pspRequestContextManager.clearRequestContextCompany();
//            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
//            pspRequestContextManager.clearRequestContextCompany();
//            //check request context is cleared
            assertNull(pspRequestContextManager.getRequestContext());
        }

    }


    @Test
    public void contextManagerTest_setRequestContextCompany(){
        boolean enableStackBasedRequestContext = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_STACK_BASED_CONTEXT_MANAGER, false);
        if(!enableStackBasedRequestContext) {

            PayrollServices.beginUnitOfWork();
            Company company = createCompany();
            Company company1 = createCompany1();
            PayrollServices.commitUnitOfWork();

            pspRequestContextManager.setRequestContext(null, RequestType.TEST, "test");
            pspRequestContextManager.setRequestContextCompany(company);
            //check request context is set correctly
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            //check request context is set cleared
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompany(company);
            pspRequestContextManager.setRequestContextCompany(company);
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompany(company1);
            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContext();
            //check request context is cleared
            assertNull(pspRequestContextManager.getRequestContext());
        }

    }

    @Test
    public void contextManagerTest_setRequestContextCompanyByPSID(){
        boolean enableStackBasedRequestContext = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_STACK_BASED_CONTEXT_MANAGER, false);
        if(!enableStackBasedRequestContext) {
            PayrollServices.beginUnitOfWork();
            Company company = createCompany();
            Company company1 = createCompany1();
            PayrollServices.commitUnitOfWork();
            pspRequestContextManager.setRequestContext(null, RequestType.TEST, "test");
            pspRequestContextManager.setRequestContextCompanyFromPSID(company.getSourceCompanyId());
            //check request context is set correctly
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            //check request context is set cleared
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompanyFromPSID(company.getSourceCompanyId());
            pspRequestContextManager.setRequestContextCompanyFromPSID(company.getSourceCompanyId());
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompanyFromPSID(company1.getSourceCompanyId());
            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContext();
            //check request context is cleared
            assertNull(pspRequestContextManager.getRequestContext());
        }

    }

    @Test
    public void contextManagerTest_setRequestContextCompanyBySeq(){
        boolean enableStackBasedRequestContext = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_STACK_BASED_CONTEXT_MANAGER, false);
        if(!enableStackBasedRequestContext) {
            PayrollServices.beginUnitOfWork();
            Company company = createCompany();
            Company company1 = createCompany1();
            PayrollServices.commitUnitOfWork();
            pspRequestContextManager.setRequestContext(null, RequestType.TEST, "test");
            pspRequestContextManager.setRequestContextCompanyFromSeq(company.getId().toString());
            //check request context is set correctly
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            //check request context is set cleared
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompanyFromSeq(company.getId().toString());
            pspRequestContextManager.setRequestContextCompanyFromSeq(company.getId().toString());
            assertEquals(company.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContextCompany();
            assertNull(pspRequestContextManager.getRequestContext().getCompanyInfo());
            pspRequestContextManager.setRequestContextCompanyFromSeq(company1.getId().toString());
            assertEquals(company1.getId(), pspRequestContextManager.getRequestContext().getCompanyInfo().getCompanySequence());
            pspRequestContextManager.clearRequestContext();
            //check request context is cleared
            assertNull(pspRequestContextManager.getRequestContext());
        }

    }
}
