package com.intuit.sbd.payroll.psp.jss.processors.workerscomp;

import com.intuit.bp.wc.common.schema.Business;
import com.intuit.bp.wc.common.schema.Employee;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheckStateCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.mapper.DomainObjToWCObjConverter;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.mapper.DomainObjToWCObjConverterSplitLimit;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDTO;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDtoCompanyFileInfo;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service.WorkersCompServiceNext;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service.WorkersCompServiceSplitLimit;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.util.*;


public class WorkerCompDataGeneratorTest {

    WorkersCompServiceNext objWorkersCompServiceNext;
    WorkersCompServiceSplitLimit objWorkersCompServiceSplitLimit;

    @Before
    public void runBeforeEachTest() {
        objWorkersCompServiceNext = PayrollApplicationBeanFactory.getBean(WorkersCompServiceNext.class);
        objWorkersCompServiceSplitLimit = PayrollApplicationBeanFactory.getBean(WorkersCompServiceSplitLimit.class);
        DataLoadServices.reinitialize();
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testCreatePayrollDataForWCHappyPathForNext() {

        Company company = TestUtils.createCompanyEmployeesAndComplexPayroll(2, false);
        TestUtils.createWcCompanyEntryForNextCompany(company);
        DataLoadServices.addWorkersCompService(company);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());

        List<Set<SpcfUniqueId>> batches = WorkersCompPaycheckPendingState.getCompaniesWithPendingPaychecks("findDistinctNextCompaniesWithWCPendingPaychecks");
        System.out.println("Total batches: " + batches.size());
        for (Set<SpcfUniqueId> batch : batches) {
            long startBatch = System.currentTimeMillis();
            SpcfCalendar currentDate = PSPDate.getPSPTime();
            List<WorkersCompPaycheckPendingState> pendingPaychecks = WorkersCompPaycheckPendingState.getPendingPaychecks(new HashSet<SpcfUniqueId>(batch),currentDate,Arrays.asList(WorkersCompPaycheckStateCode.PendingNew,WorkersCompPaycheckStateCode.PendingDelete,
                    WorkersCompPaycheckStateCode.PendingEdit));
            PayrollDTO<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll> dto=objWorkersCompServiceNext.getDTOFromDomainObject(pendingPaychecks);
            long endBatch = System.currentTimeMillis();
            int totalCompanies = dto.getPayroll().getBusinesses().getBusiness().size();
            int totalPaychecks = 0;
            for (com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll.Businesses.Business b : dto.getPayroll().getBusinesses().getBusiness()) {
                for (com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll.Businesses.Business.Employees.Employee e : b.getEmployees().getEmployee()) {
                    totalPaychecks += e.getPaychecks().getPaycheck().size();
                }
            }
            System.out.println("------BATCH--- Companies - " + totalCompanies + ", Paychecks - " + totalPaychecks + "): -----");
            System.out.println("Time to get pending checks: " + (endBatch - startBatch) + " ms");

            startBatch = System.currentTimeMillis();

            List<PayrollDtoCompanyFileInfo> response = objWorkersCompServiceNext.createPayrollDataforWC();
            Assert.assertTrue(response.get(0).getFileName().startsWith(company.getSourceCompanyId().substring(2)));

            endBatch = System.currentTimeMillis();
            System.out.println("Time to upload to WC Service: " + (endBatch - startBatch) + " ms");

            startBatch = System.currentTimeMillis();

            endBatch = System.currentTimeMillis();
            System.out.println("Time to mark pending checks : " + (endBatch - startBatch) + " ms");
            System.out.println("------BATCH END-------");
        }
    }

    @Test
    public void testCreatePayrollDataForWCNoBatchForSplit() {

      /*  Company company = TestUtils.createCompanyEmployeesAndComplexPayroll(2, false);
        TestUtils.createWcCompanyEntryForSplitCompany(company);
        DataLoadServices.addWorkersCompService(company);
        WorkersCompFacade facade = new WorkersCompFacade();
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());

        List<Set<SpcfUniqueId>> batches = facade.getCompaniesWithPendingPaychecks("findDistinctSplitLimitCompaniesWithWCPendingPaychecks");
        System.out.println("Total batches: " + batches.size());
        for (Set<SpcfUniqueId> batch : batches) {
            long startBatch = System.currentTimeMillis();
            List<WorkersCompPaycheckPendingState> pendingPaychecks = facade.getPendingPaychecks(new HashSet<SpcfUniqueId>(batch));
            DomainObjToWCObjConverter.fillDomainObjectToEmployeePaycheckMap(pendingPaychecks);
            List<String> processedCompanyIds = new ArrayList<>();
            PayrollDTO<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll> dto= DomainObjToWCObjConverterSplitLimit.createDomainObjectToPayrollDto(processedCompanyIds);
            long endBatch = System.currentTimeMillis();
            int totalCompanies = dto.getPayroll().getBusinesses().getItem().size();
            int totalPaychecks = 0;
            for (Business b : dto.getPayroll().getBusinesses().getItem()) {
                for (Employee e : b.getEmployees().getItem()) {
                    totalPaychecks += e.getPaychecks().getItem().size();
                }
            }
            System.out.println("------BATCH--- Companies - " + totalCompanies + ", Paychecks - " + totalPaychecks + "): -----");
            System.out.println("Time to get pending checks: " + (endBatch - startBatch) + " ms");

            startBatch = System.currentTimeMillis();

            List<PayrollDtoCompanyFileInfo> response = objWorkersCompServiceSplitLimit.createPayrollDataforWC();
            Assert.assertTrue(response.size() == 0);

            endBatch = System.currentTimeMillis();
            System.out.println("Time to upload to WC Service: " + (endBatch - startBatch) + " ms");

            startBatch = System.currentTimeMillis();

            endBatch = System.currentTimeMillis();
            System.out.println("Time to mark pending checks : " + (endBatch - startBatch) + " ms");
            System.out.println("------BATCH END-------");
        }*/
    }


}