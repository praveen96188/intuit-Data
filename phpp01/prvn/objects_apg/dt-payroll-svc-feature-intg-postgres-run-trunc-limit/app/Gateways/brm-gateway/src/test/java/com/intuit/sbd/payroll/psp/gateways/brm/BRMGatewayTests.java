package com.intuit.sbd.payroll.psp.gateways.brm;

import com.intuit.ems.payroll.psp.gateway.brm.BRMGateway;
import com.intuit.ems.payroll.psp.gateway.brm.BRMGatewayFactory;
import com.intuit.ems.payroll.psp.gateway.brm.CreateServiceResponse;
import com.intuit.ems.payroll.psp.gateway.brm.QueryUsageBalanceResponse;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.Ignore;
import org.junit.Test;

import java.util.GregorianCalendar;

import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Vidhya
 * Date: Apr 7, 2008
 * Time: 4:49:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class BRMGatewayTests {
    public static final String mLicenceId = "590285459983251";
    public static final String mEntitlementId = "389857";

    @Test @Ignore
    public void testCreateServiceUsage() {
        try {
            BRMGateway brmGateway = BRMGatewayFactory.createInstance();
            assertNotNull(brmGateway);

            SpcfCalendar transactionDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
            transactionDate.setValues(2012, 9, 27, 0, 0, 0, 0);

            CreateServiceResponse response = brmGateway.createUsage(mLicenceId, mEntitlementId, transactionDate, 1);
            assertNotNull(response);
            assertTrue("Y".equals(response.getSuccess()) || "SIEBEL".equals(response.getErrorSource()));
        } catch (Exception e) {
            e.printStackTrace();
            fail("BRMGateway exception");
        }
    }

    @Test @Ignore
    public void testQueryUsageBalance() {
        try {
            BRMGateway brmGateway = BRMGatewayFactory.createInstance();
            assertNotNull(brmGateway);

            QueryUsageBalanceResponse response = brmGateway.queryUsageBalance(mLicenceId, mEntitlementId);
            assertNotNull(response);

            double initialBalance = 0;
            try {
                initialBalance = response.getBalanceCurrencyAmt();
            } catch (Exception e) {
            }

            CreateServiceResponse createUsageResponse = brmGateway.createUsage(mLicenceId, mEntitlementId, SpcfCalendar.createInstance(), 1);
            assertNotNull(response);

            if ("Y".equals(createUsageResponse.getSuccess())) {
                response = brmGateway.queryUsageBalance(mLicenceId, mEntitlementId);
                assertNotNull(response);
                assertTrue(response.getBalanceCurrencyAmt()-initialBalance != 0);
            } else if (!"SIEBEL".equals(createUsageResponse.getErrorSource())) {
                fail("create usage failed at BRM. Stop testing");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("BRMGateway exception");
        }
    }

    @Test @Ignore
    public void testQueryUsageBalanceNoSend() {
        try {
            BRMGateway brmGateway = BRMGatewayFactory.createInstance();
            assertNotNull(brmGateway);

            QueryUsageBalanceResponse response = brmGateway.queryUsageBalance(mLicenceId, mEntitlementId);
            assertNotNull(response);

            double initialBalance = response.getBalanceCurrencyAmt();

            System.out.println("$" + initialBalance);
        } catch (Exception e) {
            e.printStackTrace();
            fail("BRMGateway exception");
        }
    }
}
