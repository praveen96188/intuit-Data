package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;


import com.intuit.gst.SalesContext;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SalesTaxRequestToSalesContextMapperTests {

    private SalesTaxRequestToSalesContextMapper mapperUnderTest = new SalesTaxRequestToSalesContextMapper();

    @Test
    public void testHappyPath() {
        SalesTaxRequest pspSalesTaxRequest = new SalesTaxRequest();
        SalesContext resultSalesContext = mapperUnderTest.mapToTarget(pspSalesTaxRequest,SalesContext.class);

        assertEquals("Quotation",resultSalesContext.getBizProcessType());
        assertEquals("Web",resultSalesContext.getChannel());
        assertEquals("PSP",resultSalesContext.getSalesOrganization());
        assertEquals("eBiz",resultSalesContext.getSalesParty().getSalesBusiness().getName());
        assertEquals("503",resultSalesContext.getSalesParty().getSalesBusiness().getCompanyId());
        assertEquals("Sale",resultSalesContext.getTransactionType());
        assertEquals("",resultSalesContext.getTransactionSubtype());
    }

}
