package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.Test;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 2, 2010
 * Time: 3:22:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class QBOFXTest {

    @Test
    public void testOFXStringToInt(){
        int result = QBOFX.mapOFXStringToInt("a2. 3");
        assertEquals(23,result);

        result = QBOFX.mapOFXStringToInt("*2%^. 5");
        assertEquals(25,result);
        result = QBOFX.mapOFXStringToInt("*%^");
        assertEquals(0,result);
        result = QBOFX.mapOFXStringToInt("55");
        assertEquals(55,result);
        result = QBOFX.mapOFXStringToInt("-55 ");
        assertEquals(-55,result);
        result = QBOFX.mapOFXStringToInt("5-5");
        assertEquals(0,result);
        result = QBOFX.mapOFXStringToInt("-*2%^. 5");
        assertEquals(-25,result);
    }

    @Test
    public void testOFXStringToMoney(){
        SpcfMoney res = QBOFX.mapOFXStringToMoney("^&a2. 3%^*&");
        assertEquals(new BigDecimal("2.30"), SpcfUtils.convertToBigDecimal(res));

        res = QBOFX.mapOFXStringToMoney("^&%^*&-");
        assertEquals(new BigDecimal("0.00"), SpcfUtils.convertToBigDecimal(res));

        res = QBOFX.mapOFXStringToMoney("$^&%2^*.5&");
        assertEquals(new BigDecimal("2.50"), SpcfUtils.convertToBigDecimal(res));
        res = QBOFX.mapOFXStringToMoney("$2.5");
        assertEquals(new BigDecimal("2.50"), SpcfUtils.convertToBigDecimal(res));
        res = QBOFX.mapOFXStringToMoney("2.5");
        assertEquals(new BigDecimal("2.50"), SpcfUtils.convertToBigDecimal(res));
        res = QBOFX.mapOFXStringToMoney("-$2.5");
        assertEquals(new BigDecimal("-2.50"), SpcfUtils.convertToBigDecimal(res));
        res = QBOFX.mapOFXStringToMoney("-2 .5");
        assertEquals(new BigDecimal("-2.50"), SpcfUtils.convertToBigDecimal(res));
        res = QBOFX.mapOFXStringToMoney("2-.5");
        assertEquals(new BigDecimal("0.00"), SpcfUtils.convertToBigDecimal(res));

    }

    @Test
    public void testOFXStringToDouble(){
        double res = QBOFX.mapOFXStringToDouble("$%$4.7iern&**%");
        assertEquals(4.7, res);
        res = QBOFX.mapOFXStringToDouble("$%ern&**%-");
        assertEquals(0.0, res);
        res = QBOFX.mapOFXStringToDouble("5.0 ");
        assertEquals(5.0, res);
        res = QBOFX.mapOFXStringToDouble("-5.0");
        assertEquals(-5.0, res);
        res = QBOFX.mapOFXStringToDouble("-$%$4.7iern&**%");
        assertEquals(-4.7, res);
    }
}
