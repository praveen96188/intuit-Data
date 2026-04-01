package com.intuit.sbd.payroll.psp.jss.processors.workerscomp;

import com.intuit.bp.wc.common.schema.WorkersCompSubscription;
import com.intuit.bp.wc.common.schema.WorkersCompSubscriptions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.wc.manager.WorkersCompGatewayManager;
import com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertTrue;

public class WorkersCompManagerTest {

    @Test
    public void testBatchSplitByCount() {

        // Put data
        Map<String, Number> map = new LinkedHashMap<String, Number>();
        for (int i = 1; i <= 10; i++) { // Add 10 elements
            map.put(String.valueOf(i), i);
        }
        assertTrue(map.size() == 10);

        // Test 1
        List<Set<String>> result = WorkersCompPaycheckPendingState.split(map, 1, 1);
        assertTrue(result.size() == 10);
        assertTrue(result.get(0).size() == 1);
        assertTrue(result.get(9).size() == 1);

        // Test 2
        result = WorkersCompPaycheckPendingState.split(map, 2, 1);
        assertTrue(result.size() == 10);
        assertTrue(result.get(0).size() == 1);
        assertTrue(result.get(9).size() == 1);

        // Test 2
        result = WorkersCompPaycheckPendingState.split(map, 10, 20);
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
        result = WorkersCompPaycheckPendingState.split(map, 10, 5);
        assertTrue(result.size() == 3);
        assertTrue(result.get(0).size() == 1);
        assertTrue(result.get(1).size() == 1);
        assertTrue(result.get(2).size() == 1);

        // Test 5
        map = new LinkedHashMap<String, Number>();
        map.put("T1", 4);
        map.put("T2", 1);
        map.put("T3", 7);
        result = WorkersCompPaycheckPendingState.split(map, 10, 5);
        assertTrue(result.size() == 2);
        assertTrue(result.get(0).size() == 2);
        assertTrue(result.get(1).size() == 1);
    }
}
