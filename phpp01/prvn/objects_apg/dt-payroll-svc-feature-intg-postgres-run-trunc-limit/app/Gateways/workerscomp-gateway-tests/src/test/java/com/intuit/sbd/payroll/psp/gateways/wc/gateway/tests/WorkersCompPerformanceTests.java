package com.intuit.sbd.payroll.psp.gateways.wc.gateway.tests;

import com.intuit.bp.wc.common.schema.Business;
import com.intuit.bp.wc.common.schema.Employee;
import com.intuit.bp.wc.common.schema.WorkersCompPayrollUploadResponse;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.WorkersCompPaycheck;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.PayrollDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompServiceDelegate;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Author: Sriram Nutakki
 * Date created: 12/6/12
 */
public class WorkersCompPerformanceTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testDummy() {
        assertNotNull("Intuit");
    }

    public void testSeedData() {

        System.out.println("Truncating tables");
        PayrollServicesTest.truncateTables();
        System.out.println("Done Truncating tables");

        final int totalCompaniesToCreate = 100;
        final int employeesPerCompanyToCreate = 300;

        long start = System.currentTimeMillis();

        List<String> companies = new ArrayList<String>(totalCompaniesToCreate);
        for (int i = 0; i < totalCompaniesToCreate; i++) {
            System.out.print("Creating company: " + (i + 1) + " .....");
            Company company = TestUtil.createCompanyEmployeesAndComplexPayroll(employeesPerCompanyToCreate, false);
            DataLoadServices.addWorkersCompService(company);
            companies.add(company.getSourceCompanyId());
            System.out.println("DONE");
        }
        long end = System.currentTimeMillis();
        System.out.println("Time to seed data: " + (end - start) + " ms");
    }

    public void testGetPendingPaycheckOnly() {
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        System.out.println("Total batches: " + batches.size());
        for (Set<SpcfUniqueId> batch : batches) {
            long startBatch = System.currentTimeMillis();
            PayrollDTO dto = manager.getPendingPaychecks(new HashSet<SpcfUniqueId>(batch));
            long endBatch = System.currentTimeMillis();
            int totalCompanies = dto.getPayroll().getBusinesses().getItem().size();
            int totalPaychecks = 0;
            for (Business b : dto.getPayroll().getBusinesses().getItem()) {
                for (Employee e : b.getEmployees().getItem()) {
                    totalPaychecks += e.getPaychecks().getItem().size();
                }
            }
            System.out.println(
                    "Time to get pending checks for a batch " +
                            "(Companies - " + totalCompanies + ", Paychecks - " + totalPaychecks + "): " +
                            + (endBatch - startBatch) + " ms");
        }
    }

    public void testPushPayroll() {
        long start = System.currentTimeMillis();
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        System.out.println("Total batches: " + batches.size());
        for (Set<SpcfUniqueId> batch : batches) {
            long startBatch = System.currentTimeMillis();
            PayrollDTO dto = manager.getPendingPaychecks(new HashSet<SpcfUniqueId>(batch));
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
            WorkersCompPayrollUploadResponse response = WorkersCompServiceDelegate.uploadPayroll(dto.getPayroll());
            endBatch = System.currentTimeMillis();
            System.out.println("Time to upload to WC Service: " + (endBatch - startBatch) + " ms");

            startBatch = System.currentTimeMillis();
            if (response != null && response.getProcessedCompanyIds() != null) {
                for (String companyId : response.getProcessedCompanyIds()) {
                    List<WorkersCompPaycheck> paychecks = dto.getIncludedPaychecks(companyId);
                    if (paychecks != null && paychecks.size() > 0) {
                        manager.markAsSent(paychecks);
                    }
                }
            }
            endBatch = System.currentTimeMillis();
            System.out.println("Time to mark pending checks : " + (endBatch - startBatch) + " ms");
            System.out.println("------BATCH END-------");
        }
        long end = System.currentTimeMillis();
        System.out.println("Total time: " + (end - start) + " ms");
    }
}
