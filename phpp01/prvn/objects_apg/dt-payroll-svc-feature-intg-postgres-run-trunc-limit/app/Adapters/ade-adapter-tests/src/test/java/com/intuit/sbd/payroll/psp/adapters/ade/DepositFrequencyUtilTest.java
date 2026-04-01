package com.intuit.sbd.payroll.psp.adapters.ade;

import com.intuit.sbd.payroll.psp.adapters.ade.mapping.DepositFrequencyUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.DepositFrequencyCode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 1/28/14
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class DepositFrequencyUtilTest {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testDFCodeCount() throws Throwable {
        //Whenever you do changes , please add the same to DepositFrequencyUtil.getOrderOfDepositFrequencyCode() and com.intuit.sbd.payroll.psp.adapters.ade.mapping.FrequencyMapper
        DepositFrequencyCode[] values = DepositFrequencyCode.values();
        assertEquals("DepositFrequencyCode changed.Please add the same in DepositFrequencyUtil.getOrderOfDepositFrequencyCode() and com.intuit.sbd.payroll.psp.adapters.ade.mapping.FrequencyMapper", 19, values.length);
    }

    @Test
    public void testCompareDF() throws Throwable {
        assertTrue("Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(DepositFrequencyCode.SEMIMONTHLY, DepositFrequencyCode.SEMIWEEKLY) <= 0);
        assertTrue("Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(DepositFrequencyCode.SEMIWEEKLY, DepositFrequencyCode.SEMIWEEKLY) <= 0);
        assertTrue("Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(DepositFrequencyCode.MONTHLY, DepositFrequencyCode.SEMIWEEKLY) <= 0);
        assertTrue("Don't Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(DepositFrequencyCode.SEMIWEEKLY, DepositFrequencyCode.MONTHLY) > 0);
        assertTrue("Don't Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(DepositFrequencyCode.MONTHLY, DepositFrequencyCode.QUARTERLY) > 0);
        assertTrue("Don't Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(DepositFrequencyCode.MONTHLY, DepositFrequencyCode.ANNUAL) > 0);
        assertTrue("Don't Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(null, DepositFrequencyCode.ANNUAL) > 0);
        assertTrue("Don't Check Threshold", DepositFrequencyUtil.compareDepositFrequencyCodes(DepositFrequencyCode.SEMIMONTHLY, null) > 0);
    }

    @Test
    public void testAllDFWeights() throws Throwable {

        assertEquals("NEXTBANKINGDAY", 0, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.NEXTBANKINGDAY));
        assertEquals("THREEBANKINGDAY", 1, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.THREEBANKINGDAY));
        assertEquals("FIVEBANKINGDAY", 2, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.FIVEBANKINGDAY));
        assertEquals("SEMIWEEKLY", 3, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.SEMIWEEKLY));
        assertEquals("EIGHTHMONTHLY", 4, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.EIGHTHMONTHLY));
        assertEquals("WEEKLY", 5, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.WEEKLY));
        assertEquals("QUARTERMONTHLY", 6, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.QUARTERMONTHLY));
        assertEquals("QUADMONTHLY", 7, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.QUADMONTHLY));
        assertEquals("SEMIMONTHLY", 8, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.SEMIMONTHLY));
        assertEquals("TWICEMONTHLY", 9, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.TWICEMONTHLY));
        assertEquals("SPLITMONTHLY", 10, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.SPLITMONTHLY));
        assertEquals("MONTHLYACCELERATED", 11, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.MONTHLYACCELERATED));
        assertEquals("MONTHLY", 12, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.MONTHLY));
        assertEquals("QUARTERLY", 13, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.QUARTERLY));
        assertEquals("SEMIANNUAL", 14, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.SEMIANNUAL));
        assertEquals("EARLYFILER", 15, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.EARLYFILER));
        assertEquals("ACCELERATED", 16, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.ANNUAL));
        assertEquals("ACCELERATED", 17, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.ACCELERATED));
        assertEquals("DEFAULT ", 18, DepositFrequencyUtil.getOrderOfDepositFrequencyCode(DepositFrequencyCode.NOCALC));

    }
}
