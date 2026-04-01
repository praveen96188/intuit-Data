package com.intuit.sbd.payroll.psp.gateways.wc.gateway.tests;

import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import org.junit.Test;

import java.util.*;

import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompPropEnum.PUSH_PAYROLL_COMPANIES_BATCH_SIZE;
import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompPropEnum.PUSH_PAYROLL_PAYCHECKS_BATCH_SIZE;
import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompServiceEnum.WC_SERVICE;
import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompURLEnum.*;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Author: Sriram Nutakki
 * Date created: 11/8/12
 */
public class WCGeneralTests {

    @Test
    public void testProps() throws Exception {
        assertNotNull(WC_SERVICE.getAWSHost());
        assertNotNull(WC_SERVICE_GET_SUBSCRIPTIONS_URL.getAWSUrl());
        assertNotNull(WC_SERVICE_POST_PAYROLL_URL.getAWSUrl());
        assertNotNull(WC_SERVICE_POST_SUBSCRIPTIONS_CONFIRMATION_URL.getAWSUrl());
        assertNotNull(WC_SERVICE_GET_DISPLAY_DATA_FOR_HELPDESK_URL.getAWSUrl());
        assertNotNull(WC_SERVICE_POST_CHANGE_EVENTS_URL.getAWSUrl());
        assertNotNull(PUSH_PAYROLL_COMPANIES_BATCH_SIZE.getValue());
        assertTrue(PUSH_PAYROLL_PAYCHECKS_BATCH_SIZE.getValueAsInt() > 0);
    }

    @Test
    public void testCollectionSplit() {
        // Test 1
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(new String[]{"1", "2", "3" ,"4", "5"}));
        List<List<String>> result = WCUtil.split(list, 6);
        assertTrue(result.size() == 1);
        assertTrue(result.get(0).size() == 5);

        // Test 2
        list = new ArrayList<String>();
        list.addAll(Arrays.asList(new String[]{"1", "2", "3" ,"4", "5"}));
        result = WCUtil.split(list, 2);
        assertTrue(result.size() == 3);
        assertTrue(result.get(0).size() == 2);
        assertTrue(result.get(2).size() == 1);

        // Test 3
        list = new ArrayList<String>();
        for (int i = 1; i <= 51; i++) { // Add 51 elements
            list.add(String.valueOf(i));
        }
        result = WCUtil.split(list, 10);
        assertTrue(result.size() == 6);
        assertTrue(result.get(0).size() == 10);
        assertTrue(result.get(5).size() == 1 && result.get(5).get(0).equals(String.valueOf("51")));
    }

    @Test
    public void testBatchSplitByCount() {

        // Put data
        Map<String, Number> map = new LinkedHashMap<String, Number>();
        for (int i = 1; i <= 10; i++) { // Add 10 elements
            map.put(String.valueOf(i), i);
        }
        assertTrue(map.size() == 10);

        // Test 1
        List<Set<String>> result = WCUtil.split(map, 1, 1);
        assertTrue(result.size() == 10);
        assertTrue(result.get(0).size() == 1);
        assertTrue(result.get(9).size() == 1);

        // Test 2
        result = WCUtil.split(map, 2, 1);
        assertTrue(result.size() == 10);
        assertTrue(result.get(0).size() == 1);
        assertTrue(result.get(9).size() == 1);

        // Test 2
        result = WCUtil.split(map, 10, 20);
        assertTrue(result.size() == 4);
        assertTrue(result.get(0).size() == 5);
        assertTrue(result.get(1).size() == 2);
        assertTrue(result.get(2).size() == 2);
        assertTrue(result.get(3).size() == 1);

        // Test 3
        map = new LinkedHashMap<String, Number>();
        map.put("T1", 10);
        map.put("T2", 5);
        map.put("T3", 6);
        result = WCUtil.split(map, 10, 5);
        assertTrue(result.size() == 3);
        assertTrue(result.get(0).size() == 1);
        assertTrue(result.get(1).size() == 1);
        assertTrue(result.get(2).size() == 1);

        // Test 5
        map = new LinkedHashMap<String, Number>();
        map.put("T1", 4);
        map.put("T2", 1);
        map.put("T3", 7);
        result = WCUtil.split(map, 10, 5);
        assertTrue(result.size() == 2);
        assertTrue(result.get(0).size() == 2);
        assertTrue(result.get(1).size() == 1);
    }
}
