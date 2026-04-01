package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class CdmHelperTests {

    @Test
    public void testFormatBigDecimal() {
        //Should remain negative
        String negative = "-1000.00";
        BigDecimal negativeDecimal = CdmHelper.convertToFormattedBigDecimal(SpcfDecimal.createInstance(negative));
        Assert.assertEquals(new BigDecimal("-1000.00"), negativeDecimal);
        //Should round up
        String wrongPrecisionHigh = "125.246";
        BigDecimal wrongPrecisionHighDecimal = CdmHelper.convertToFormattedBigDecimal(SpcfDecimal.createInstance(wrongPrecisionHigh));
        Assert.assertEquals(new BigDecimal("125.25"), wrongPrecisionHighDecimal);
        //Should not round up
        String wrongPrecisionLow = "125.254";
        BigDecimal wrongPrecisionLowDecimal = CdmHelper.convertToFormattedBigDecimal(SpcfDecimal.createInstance(wrongPrecisionLow));
        Assert.assertEquals(new BigDecimal("125.25"), wrongPrecisionLowDecimal);
    }

    @Test
    public void testFormatBigDecimalNull() {
        BigDecimal defaultValue = CdmHelper.convertToFormattedBigDecimal(null, SpcfDecimal.createInstance(0.0));
        Assert.assertEquals(new BigDecimal("0.00"), defaultValue);
    }

    @Test
    public void testFormatAndMask() {
        String ssn = "999-09-1234";
        Assert.assertEquals("....1234", CdmHelper.formatAndMask(ssn));
        ssn = "***-**-1234";
        Assert.assertEquals("....1234", CdmHelper.formatAndMask(ssn));
        ssn = "123";
        Assert.assertEquals("....123", CdmHelper.formatAndMask(ssn));
        ssn = "12345";
        Assert.assertEquals("....2345", CdmHelper.formatAndMask(ssn));
    }

    @Test
    public void testAddHoursMinutes() {
        String simpleTotal = CdmHelper.addHoursMinutes("12:30", "1:25");
        Assert.assertEquals("13:55", simpleTotal);
        String lessThanTenMinutes = CdmHelper.addHoursMinutes("1:50", "1:12");
        Assert.assertEquals("3:02", lessThanTenMinutes);
        String invalidAdditional = CdmHelper.addHoursMinutes("5:30", "abc");
        Assert.assertEquals("5:30", invalidAdditional);
        String minutesOverSixty = CdmHelper.addHoursMinutes("123:59", "27:53");
        Assert.assertEquals("151:52", minutesOverSixty);
        String zeroFormat = CdmHelper.addHoursMinutes(null, "44:23");
        Assert.assertEquals("44:23", zeroFormat);
        String doubleValue = CdmHelper.addHoursMinutes("123.0", "22.0");
        Assert.assertEquals("145.00", doubleValue);
        String zeroDecimal = CdmHelper.addHoursMinutes(null, "140.0");
        Assert.assertEquals("140.00", zeroDecimal);
    }

    @Test
    public void testNotBlank() {
        Assert.assertFalse(CdmHelper.notBlank(null));
        Assert.assertFalse(CdmHelper.notBlank("0"));
        Assert.assertFalse(CdmHelper.notBlank("0.0"));
        Assert.assertFalse(CdmHelper.notBlank("0.00"));
        Assert.assertFalse(CdmHelper.notBlank("0.000"));
        Assert.assertFalse(CdmHelper.notBlank("0:00"));

        Assert.assertTrue(CdmHelper.notBlank("1.23"));
        Assert.assertTrue(CdmHelper.notBlank("-10.83"));
        Assert.assertTrue(CdmHelper.notBlank("1:10"));
        Assert.assertTrue(CdmHelper.notBlank("-4:08"));
    }

    @Test
    public void testParseDecimal() {
        Assert.assertEquals(new BigDecimal("2.00"), CdmHelper.parseDecimal("2.0"));
        Assert.assertEquals(new BigDecimal("3.12"), CdmHelper.parseDecimal("3.12"));
        Assert.assertEquals(new BigDecimal("10.00"), CdmHelper.parseDecimal("10.0%"));
    }
}
