package com.intuit.ems.payroll.psp.gateways.tfs;

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 12/12/12
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 */


public class TFSGatewayTests {

    @Test
    @Ignore
    public void integrationTest() {
        ITFSGateway tfsGateway = TFSGatewayFactory.createInstance();
        Map<String, Integer> results = tfsGateway.getW2PageCountsByCompany(2012);

        assertTrue(results.isEmpty());
    }
}
