package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.constants.CommonConstants;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestContext;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyEventDetail;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class HibernateInterceptorTests {
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

    @Test
    public void testUpdateInterceptor() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent1 = Application.findById(CompanyEvent.class, companyEvent.getId());
        companyEvent1.setStatusCd(CompanyEventStatus.Inactive);
        Application.save(companyEvent1);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    @Ignore
    public void testDeleteInterceptor() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent1 = Application.findById(CompanyEvent.class, companyEvent.getId());
        Application.delete(companyEvent1);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testfindByIdInterceptor() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        CompanyEvent companyEvent = createCompanyEvent(company);
        PayrollServices.commitUnitOfWork();

        pspRequestContextManager.setRequestContext(company, RequestType.TEST, "Test" );

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent1 = Application.findById(CompanyEvent.class, companyEvent.getId());
        PayrollServices.commitUnitOfWork();
    }

    private Company createCompany() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Company company = result.getResult();
        return company;
    }
    private CompanyEvent createCompanyEvent(Company foundCompany){
        CompanyEvent companyEvent = CompanyEvent.createCompanyEvent(foundCompany, EventTypeCode.ACHReturn);

        //create CompanyEventDetail
        CompanyEventDetail companyEventDetail = new CompanyEventDetail();
        companyEventDetail.setEventDetailTypeCd(EventDetailTypeCode.BankAccountNumber);
        companyEventDetail.setValue("Test bank account");
        companyEventDetail.setEventDetailSubtype("Test bank account");
        companyEventDetail.setCompanyEvent(companyEvent);
        companyEventDetail.setCompany(foundCompany);

        //Update CompanyEvent
        companyEvent.setModifierId("Test");

        Application.save(companyEvent);
        Application.save(companyEventDetail);

        //Update CompanyEventDetail
        companyEventDetail.setValue("Test update bank account");
        Application.save(companyEventDetail);
        return companyEvent;
    }
}

