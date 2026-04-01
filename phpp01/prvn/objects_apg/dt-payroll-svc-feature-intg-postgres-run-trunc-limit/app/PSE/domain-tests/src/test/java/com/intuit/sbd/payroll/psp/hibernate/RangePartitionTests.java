package com.intuit.sbd.payroll.psp.hibernate;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventEnumType;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.AddCompanyDataLoader;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class RangePartitionTests {

    PSPRequestContextManager pspRequestContextManager;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
        pspRequestContextManager.clearRequestContext();
        pspRequestContextManager.clearCreatedDate();
    }

    @After
    public void runAfterEachTest() {
        pspRequestContextManager.clearRequestContext();
        pspRequestContextManager.clearCreatedDate();
    }

    @Test
    public void testFindById() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        EntityUpdate entityUpdate = createEntityUpdate(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pspRequestContextManager.setCreatedDate(entityUpdate.getCreatedDate());
        EntityUpdate entityUpdate1 = Application.findById(EntityUpdate.class, entityUpdate.getId());
        Assert.assertNotNull(entityUpdate1);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Setting Wrong Date
        SpcfCalendar wrongDate = entityUpdate.getCreatedDate();
        wrongDate.addDays(4);
        pspRequestContextManager.setCreatedDate(wrongDate);
        EntityUpdate entityUpdate2 = Application.findById(EntityUpdate.class, entityUpdate.getId());
        Assert.assertNull(entityUpdate2);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testUpdate() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        EntityUpdate entityUpdate = createEntityUpdate(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pspRequestContextManager.setCreatedDate(entityUpdate.getCreatedDate());
        EntityUpdate entityUpdate1 = Application.findById(EntityUpdate.class, entityUpdate.getId());
        pspRequestContextManager.clearCreatedDate();
        entityUpdate1.setEntityName("New Entity Name");
        Application.save(entityUpdate1);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testDelete() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        EntityUpdate entityUpdate = createEntityUpdate(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pspRequestContextManager.setCreatedDate(entityUpdate.getCreatedDate());
        EntityUpdate entityUpdate1 = Application.findById(EntityUpdate.class, entityUpdate.getId());
        pspRequestContextManager.clearCreatedDate();
        Application.delete(EntityUpdate.class, entityUpdate1.getId());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCriteria() {
        PayrollServices.beginUnitOfWork();
        Company company = createCompany();
        EntityUpdate entityUpdate = createEntityUpdate(company);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pspRequestContextManager.setCreatedDate(entityUpdate.getCreatedDate());
        Criterion<EntityUpdate> criterion = EntityUpdate.Status().in(Status.Created);
        Expression<EntityUpdate> expression = new Query<EntityUpdate>()
                .Where(criterion);
        List<EntityUpdate> entityUpdateList = Application.executeQuery(EntityUpdate.class, expression);
        Assert.assertEquals(1, entityUpdateList.size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Setting Wrong Date
        SpcfCalendar wrongDate = entityUpdate.getCreatedDate();
        wrongDate.addDays(4);
        pspRequestContextManager.setCreatedDate(wrongDate);
        List<EntityUpdate> entityUpdateList1 = Application.executeQuery(EntityUpdate.class, expression);
        Assert.assertEquals(0, entityUpdateList1.size());
        PayrollServices.rollbackUnitOfWork();
    }

    private EntityUpdate createEntityUpdate(Company company) {
        EntityUpdate entityUpdate = new EntityUpdate();
        entityUpdate.setCompany(company);
        entityUpdate.setEventType(EventEnumType.EntityCreate);
        entityUpdate.setEntityId("123");
        entityUpdate.setEntityName("abc");
        entityUpdate.setChangedAttributes("changedAttr");
        entityUpdate.setRetryCount(0);
        entityUpdate.setStatus(Status.Created);
        Application.save(entityUpdate);
        return entityUpdate;
    }

    private Company createCompany() {
        CompanyDTO company1 = AddCompanyDataLoader.dataloader.getTestIntuitCompany();
        ProcessResult<Company> result = PayrollServices.companyManager.addCompany(company1);
        Company company = result.getResult();
        return company;
    }
}
