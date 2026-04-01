package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class SalesTaxRequestLineToGSTOrderItemMapperTests {


    private SalesTaxRequestLineToGSTOrderItemMapper mapperUnderTest;

    @Before
    public void initializeAllMappers() {
        mapperUnderTest = new SalesTaxRequestLineToGSTOrderItemMapper();
    }

    @Test
    public void testHappyPath() {
//        initializeAllMappers();
        SalesTaxRequestLine pspSalesTaxRequestLine = new SalesTaxRequestLine();
        pspSalesTaxRequestLine.setSKU("000");
        pspSalesTaxRequestLine.setProductClassForSKU("PS");

        OrderItem resultOrderItem = mapperUnderTest.mapToTarget(pspSalesTaxRequestLine,OrderItem.class);
        assertEquals("",resultOrderItem.getDescription());
        assertEquals("PS",resultOrderItem.getItemAttributes().getProductClass());
        assertFalse(resultOrderItem.getItemAttributes().getShippableInd());
        assertEquals("OracleSKU",resultOrderItem.getItemIds().getItemId().getId().getSchemeName());
        assertEquals("000",resultOrderItem.getItemIds().getItemId().getId().getValue());

    }

}
