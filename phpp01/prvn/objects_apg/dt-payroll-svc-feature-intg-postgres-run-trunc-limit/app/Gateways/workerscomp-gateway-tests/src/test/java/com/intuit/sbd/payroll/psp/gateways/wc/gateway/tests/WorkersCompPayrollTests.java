package com.intuit.sbd.payroll.psp.gateways.wc.gateway.tests;

import com.intuit.bp.wc.common.schema.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.AssistedUnprocessedRequestTests;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.PayrollDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.hibernate.FlushMode;
import org.junit.*;

import javax.xml.bind.JAXBContext;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Author: Sriram Nutakki
 * Date created: 6/6/13
 */
public class WorkersCompPayrollTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testPayrollUpdate() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.WorkersComp);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        // Process payroll
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        // Get pending paychecks
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);


        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        List<WorkersCompPaycheck> workersCompPaychecks= dto.getIncludedPaychecks(psid)    ;
        for (WorkersCompPaycheck wc : workersCompPaychecks){
            assertEquals(1, wc.getPaycheckVersion());
            assertNotNull("WorkerCompPaycheck Company null",wc.getCompany());
            assertEquals(company,wc.getCompany());
        }


        for (String companyId : dto.getIncludedPaychecksByCompany().keySet()) {
            List<WorkersCompPaycheck> paychecks = dto.getIncludedPaychecks(companyId);
            if (paychecks != null && paychecks.size() > 0) {
                manager.markAsSent(paychecks);
                //asserting that companyFk was properly set
                for(WorkersCompPaycheck paycheck: paychecks) {
                    assertNotNull("WorkerCompPaycheck Company null",paycheck.getCompany());
                    assertEquals(company.getSourceCompanyId(),paycheck.getCompany().getSourceCompanyId());
                }
            }
        }

        // Update payroll
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_2.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        // Get pending paychecks
        manager = new WorkersCompGatewayManager();
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        workersCompPaychecks= dto.getIncludedPaychecks(psid)    ;
        for (WorkersCompPaycheck wc : workersCompPaychecks){
            assertEquals(2, wc.getPaycheckVersion());
        }
    }

    @Test
    public void testPayrollWithNonMatchingQBDTListId() throws Exception {

        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.WorkersComp);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        // Process payroll
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        // Set list id to null
        PayrollServices.beginUnitOfWork();
        Expression<QbdtPayrollItemInfo> query =
                new Query<QbdtPayrollItemInfo>().Where(QbdtPayrollItemInfo.Company().equalTo(company));
        Set<QbdtPayrollItemInfo> results = Application.find(QbdtPayrollItemInfo.class, query);
        for (QbdtPayrollItemInfo qbdtPayrollItemInfo : results) {
            qbdtPayrollItemInfo.setListId(null);
        }
        PayrollServices.commitUnitOfWork();

        // Get pending paychecks
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        List<Set<SpcfUniqueId>> batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        PayrollDTO dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        for (Business business : dto.getPayroll().getBusinesses().getItem()) {
            for (com.intuit.bp.wc.common.schema.Employee employee : business.getEmployees().getItem()) {
                for (com.intuit.bp.wc.common.schema.Paycheck paycheck : employee.getPaychecks().getItem()) {
                    for (PaycheckItem item : paycheck.getPaycheckItems().getItem()) {
                        if (item.getType() == PaycheckItemType.PAY) {
                            assertNotNull(item.getPayCode());
                            assertNotNull(item.getPayCustomItemName());
                            assertNotNull(item.getPayAmount());
                        } else {
                            assertNotNull(item.getDeductionCode());
                            assertNotNull(item.getDeductionCustomItemName());
                            assertNotNull(item.getDeductionAmount());
                        }
                    }
                }
            }
        }

        // Change company payroll item source description such that it deosn't match
        // and we retrieve QBDTPayItemInfo through CompanyLaw
        PayrollServices.beginUnitOfWork();
        Expression<CompanyPayrollItem> companyPayrollItemQuery =
                new Query<CompanyPayrollItem>().Where(CompanyPayrollItem.Company().equalTo(company));
        Set<CompanyPayrollItem> items = Application.find(CompanyPayrollItem.class, companyPayrollItemQuery);
        for (CompanyPayrollItem item : items) {
            item.setSourceDescription(item.getSourceDescription() + "-MOD");
        }
        PayrollServices.commitUnitOfWork();

        // Get pending paychecks
        batches = manager.getCompaniesWithPendingPaychecks();
        assertTrue(batches.size() > 0);

        dto = manager.getPendingPaychecks(batches.get(0));
        assertNotNull(dto);
        for (Business business : dto.getPayroll().getBusinesses().getItem()) {
            for (com.intuit.bp.wc.common.schema.Employee employee : business.getEmployees().getItem()) {
                for (com.intuit.bp.wc.common.schema.Paycheck paycheck : employee.getPaychecks().getItem()) {
                    for (PaycheckItem item : paycheck.getPaycheckItems().getItem()) {
                        if (item.getType() == PaycheckItemType.PAY) {
                            assertNotNull(item.getPayCode());
                            assertNotNull(item.getPayCustomItemName());
                            assertNotNull(item.getPayAmount());
                        } else {
                            assertNotNull(item.getDeductionCode());
                            assertNotNull(item.getDeductionCustomItemName());
                            assertNotNull(item.getDeductionAmount());
                        }
                    }
                }
            }
        }
    }
}