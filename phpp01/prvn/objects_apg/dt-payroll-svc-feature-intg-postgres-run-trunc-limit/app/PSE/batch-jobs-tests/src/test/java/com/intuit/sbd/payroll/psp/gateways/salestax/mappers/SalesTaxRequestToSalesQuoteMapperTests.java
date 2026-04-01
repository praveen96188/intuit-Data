package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.SalesQuote;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

public class SalesTaxRequestToSalesQuoteMapperTests {

    private SalesTaxRequestToSalesQuoteMapper mapperUnderTest = new SalesTaxRequestToSalesQuoteMapper();

    @Test
    public void testHappyPath() {
        SalesTaxRequest salesTaxRequest = new SalesTaxRequest();

        SalesTaxRequestLine line1 = new SalesTaxRequestLine();
        SalesTaxRequestLine line2 = new SalesTaxRequestLine();
        line1.setAmount(BigDecimal.valueOf((float) 10));
        line2.setAmount(BigDecimal.valueOf((float) 20));
        salesTaxRequest.addLine(line1);
        salesTaxRequest.addLine(line2);

        SalesQuote resultSalesQuote = mapperUnderTest.mapToTarget(salesTaxRequest,SalesQuote.class);
        assertEquals(30,resultSalesQuote.getTotalTransaction().getValue(),0.01);
        assertEquals("USD",resultSalesQuote.getTotalTransaction().getCurrency());
        assertEquals(0,resultSalesQuote.getTotalFreight().getValue(),0.01);
        assertEquals("USD",resultSalesQuote.getTotalTransaction().getCurrency());
        assertEquals(0,resultSalesQuote.getTotalDiscount().getValue(),0.01);
        assertEquals("USD",resultSalesQuote.getTotalTransaction().getCurrency());



    }
}
