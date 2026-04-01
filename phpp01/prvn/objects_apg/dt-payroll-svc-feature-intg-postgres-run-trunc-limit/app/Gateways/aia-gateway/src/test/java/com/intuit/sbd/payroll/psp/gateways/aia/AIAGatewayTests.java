package com.intuit.sbd.payroll.psp.gateways.aia;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Vidhya
 * Date: Apr 7, 2008
 * Time: 4:49:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class AIAGatewayTests {
    public static final String mLicenceId = "590285459983251";
    public static final String mEntitlementId = "389857";

    @Test
    public void testQueryInvoiceList() {
        try {
            AIAGateway gw = new AIAGateway();
            String customerId = "53510860";
            String billingProfileId = "SYS-57FR6MW";

            List<BillInfo> billInfoList = gw.queryInvoiceList(customerId, billingProfileId);
            assertNotNull(billInfoList);
            for (BillInfo billInfo : billInfoList) {
                System.out.println("Bill Date" + billInfo.getBillDate());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryBillingProfile(){
        try {
            AIAGateway gw = new AIAGateway();
            String customerId = "53510860";

            String billingProfileId = gw.queryBillingProfile(customerId);
            assertNotNull(billingProfileId);
            assertEquals("SYS-57FR6MW", billingProfileId);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryInvoiceListByDate(){
        try {
            AIAGateway gw = new AIAGateway();
            String customerId = "53510860";
            String billingProfileId = "SYS-57FR6MW";

            List<BillInfo> billInfoList = gw.queryInvoiceList(customerId, billingProfileId, CalendarUtils.convertToDate(SpcfCalendar.createInstance(2014, 3, 18, SpcfTimeZone.getLocalTimeZone())),CalendarUtils.convertToDate(SpcfCalendar.createInstance(2015, 3, 18, SpcfTimeZone.getLocalTimeZone())));
            assertNotNull(billInfoList);
            for (BillInfo billInfo : billInfoList) {
                System.out.println("Bill Date" + billInfo.getBillDate());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryDetails() {
        try {
            AIAGateway gw = new AIAGateway();
            String customerId = "261549178";
            String billingProfileId = "SYS-56UCNWU";
            String billPOID="0.0.0.1 /bill 43426828 0";
            List<ItemCharge> itemChargeList = gw.queryInvoiceDetails( customerId, billingProfileId,billPOID);
            assertNotNull(itemChargeList);
            for (ItemCharge itemCharge : itemChargeList) {
                System.out.println("item charge id :: " + itemCharge.getItemChargeId());
                System.out.println("item Name :: " + itemCharge.getItemName());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testEventDetails() {
        try {
            AIAGateway gw = new AIAGateway();
            String customerId = "261549178";
            String billingProfileId = "SYS-56UCNWU";
            String billPOID="0.0.0.1 /bill 43426828 0";
            String itemChargeId="0.0.0.1 /item/cycle_forward 47201781 1";
            Integer duration = gw.queryEventDetails( customerId, billingProfileId,billPOID,itemChargeId);
            System.out.println("duration :: " + duration);
            assertNotNull(duration);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
