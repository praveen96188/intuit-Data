package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;


import com.intuit.gst.*;

import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import com.intuit.sbg.psp.salestaxservices.EOSProductClassInfoServiceViaRest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

public class GSTSalesTaxLineToPSPSalesTaxResponseLineMapperTests {

    private GSTSalesTaxLineToPSPSalesTaxResponseLineMapper mapper = new GSTSalesTaxLineToPSPSalesTaxResponseLineMapper();

    @Test
    public void testHappyPath() {
        String dummySKU = "000";
        SalesTaxLine gstSalesTaxResponseLine = new SalesTaxLine();
        gstSalesTaxResponseLine.setOrderItem(getDummyOrderItemForSKU(dummySKU));

        ExtendedTaxAmount extendedTaxAmount = new ExtendedTaxAmount((float)2.23,"USD");
        Tax tax = new Tax();
        tax.setExtendedTaxAmount(extendedTaxAmount);
        tax.setTaxRate(new TaxRate(new BigDecimal("1.23")));
        gstSalesTaxResponseLine.setTax(tax);

        SalesTaxResponseLine pspSalesTaxResponseLine = mapper.mapToTarget(gstSalesTaxResponseLine, SalesTaxResponseLine.class);

        assertEquals("000",pspSalesTaxResponseLine.getSKU());
        assertEquals(new BigDecimal("2.23"), pspSalesTaxResponseLine.getTaxAmount());
        assertEquals(new BigDecimal("1.23"),pspSalesTaxResponseLine.getTaxRate());
    }


    private OrderItem getDummyOrderItemForSKU(String SKU) {
        Id id = new Id();
        id.setValue(SKU);
        ItemId itemId = new ItemId();
        itemId.setId(id);
        ItemIds itemIds = new ItemIds();
        itemIds.setItemId(itemId);
        OrderItem dummyOrderItem = new OrderItem();
        dummyOrderItem.setItemIds(itemIds);
        return dummyOrderItem;
    }

}
