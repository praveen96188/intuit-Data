package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.Address;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.junit.Test;

import static org.junit.Assert.*;

public class SalesTaxRequestToGSTAddressMapperTests {

    private SalesTaxRequestToGSTAddressMapper mapperUnderTest = new SalesTaxRequestToGSTAddressMapper();

    @Test
    public void testHappyPath() {
        SalesTaxRequest pspSalesTaxRequest = new SalesTaxRequest();
        pspSalesTaxRequest.setAddressLine1("addr1");
        pspSalesTaxRequest.setAddressLine2("addr2");
        pspSalesTaxRequest.setAddressLine3("addr3");
        pspSalesTaxRequest.setZipCode("000000");
        pspSalesTaxRequest.setCity("DummyCity");
        pspSalesTaxRequest.setState("DummyState");
        pspSalesTaxRequest.setCountry("USA");

        Address resultAddress = mapperUnderTest.mapToTarget(pspSalesTaxRequest,Address.class);
        assertEquals(3,resultAddress.getAddressLine().size());
        assertEquals("addr1",resultAddress.getAddressLine().get(0));
        assertEquals("addr2",resultAddress.getAddressLine().get(1));
        assertEquals("addr3",resultAddress.getAddressLine().get(2));
        assertEquals("000000",resultAddress.getPostalCode());
        assertEquals("DummyCity",resultAddress.getCity());
        assertEquals("DummyState",resultAddress.getStateOrProvince());
        assertEquals("US",resultAddress.getCountry());
        assertTrue(resultAddress.getStandardizeAddress());
        assertFalse(resultAddress.getStandardizedAddressInd());


    }


}
