package com.intuit.sbd.payroll.psp.util.launchdarkly;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;

// Extend deadline for feature flags on IXP to avoid feature flag failing tests
public class FeatureFlagsTest {
    @Test
    public void testGetBoolean() throws Exception {
        Assert.assertTrue(FeatureFlags.get().booleanValue(FeatureFlags.Key.TEST_BOOLEAN_UNIT, false));
    }

    @Test
    public void testGetInt() throws Exception {
        Assert.assertEquals(30, FeatureFlags.get().intValue(FeatureFlags.Key.TEST_INT_UNIT, 0));
    }

    @Test
    public void testGetDouble() throws Exception {
        Assert.assertEquals(100, FeatureFlags.get().doubleValue(FeatureFlags.Key.TEST_DOUBLE_UNIT, 0d), 0d);
    }

    @Test
    public void testGetString() throws Exception {
        Assert.assertEquals("hello", FeatureFlags.get().stringValue(FeatureFlags.Key.TEST_STRING_UNIT, "a"));
    }

    @Test
    public void testGetJson() throws Exception {
        JsonNode jsonNode = FeatureFlags.get().jsonValue(FeatureFlags.Key.TEST_JSON_UNIT, (JsonNode) null);
        Assert.assertEquals("1001", jsonNode.get("id").textValue());
    }

    @Test
    public void testIsReady() throws Exception {
        Assert.assertTrue(FeatureFlags.get().isReady());
    }

}
