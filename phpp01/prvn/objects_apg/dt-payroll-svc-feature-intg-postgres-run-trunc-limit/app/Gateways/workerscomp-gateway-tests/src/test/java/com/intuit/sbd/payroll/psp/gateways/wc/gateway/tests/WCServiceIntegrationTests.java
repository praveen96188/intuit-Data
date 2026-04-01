package com.intuit.sbd.payroll.psp.gateways.wc.gateway.tests;

import com.intuit.bp.wc.common.schema.Payroll;
import com.intuit.bp.wc.common.schema.WorkersCompSubscriptions;
import com.intuit.bp.wc.common.schema.WorkersCompSubscription;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.PayrollDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompGateway;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompGatewayImpl;
import com.intuit.sbd.payroll.psp.gateways.wc.gateway.WorkersCompServiceDelegate;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Author: Sriram Nutakki
 * Date created: 11/12/12
 */
public class WCServiceIntegrationTests {

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
    public void testDummy() {
        assertNotNull("Intuit");
    }

    public void testGetSubscriptions() throws Exception {
        WorkersCompSubscriptions subscriptions = WorkersCompServiceDelegate.getSubscriptionChanges();
        if (subscriptions != null) {
            for (WorkersCompSubscription subscription : subscriptions.getWorkersCompSubscription()) {
                assertNotNull(subscription.getPSID());
                assertNotNull(subscription.getStartDate());
            }
        }
    }

    public void testPostSubscriptionConfirmation() throws Exception {
        WorkersCompSubscriptions subscriptions = WorkersCompServiceDelegate.getSubscriptionChanges();
        int totalEntries = subscriptions != null && subscriptions.getWorkersCompSubscription() != null
                ? subscriptions.getWorkersCompSubscription().size()
                : 0;
        if (totalEntries > 0) {
            WorkersCompSubscription subscription = subscriptions.getWorkersCompSubscription().get(0);
            WorkersCompSubscriptions confirmSubscriptions = new WorkersCompSubscriptions();
            confirmSubscriptions.getWorkersCompSubscription().add(subscription);
            WorkersCompServiceDelegate.postSubscriptionConfirmation(confirmSubscriptions);

            WorkersCompSubscriptions subscriptionsAfterConf = WorkersCompServiceDelegate.getSubscriptionChanges();
            int totalEntriesAfterConf = subscriptionsAfterConf != null &&
                    subscriptionsAfterConf.getWorkersCompSubscription() != null
                        ? subscriptionsAfterConf.getWorkersCompSubscription().size()
                        : 0;

            assertEquals(totalEntries - totalEntriesAfterConf, 1);
        }
    }

    public void testPushPayroll() throws Exception {
        // Add subscription
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());

        WorkersCompGateway gateway = new WorkersCompGatewayImpl();
        gateway.pushPayrollDataToWC();
    }

    public void testGetSubscriptionStatus() throws Exception {
        // Add subscription
        String psid = String.valueOf(System.currentTimeMillis());
        Company company = TestUtil.createCompanyEmployeesAndPayroll(psid);
        WorkersCompSubscriptions subscriptions = new WorkersCompSubscriptions();
        WorkersCompSubscription subscription = new WorkersCompSubscription();
        subscription.setPSID(psid);
        subscription.setActive(true);
        subscription.setStartDate(WCUtil.createXMLGregorianCalendar(2005, 5, 20));
        subscriptions.getWorkersCompSubscription().add(subscription);
        WorkersCompGatewayManager manager = new WorkersCompGatewayManager();
        manager.saveSubscriptionChanges(subscriptions);
        company = DataLoadServices.refreshCompany(company);
        CompanyService workersCompService = company.getService(ServiceCode.WorkersComp);
        Assert.assertEquals(ServiceSubStatusCode.ActiveCurrent, workersCompService.getStatusCd());

        WorkersCompGateway gateway = new WorkersCompGatewayImpl();
        String xml = gateway.getDisplayDataForHelpDesk("QBDT", psid);
        System.out.println(xml);
    }
}
