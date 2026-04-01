package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.GSTRequest;

import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import com.intuit.sbg.psp.salestaxservices.EOSProductClassInfoServiceViaRest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class SalesTaxRequestToGSTRequestMapperTests {



    @Mock
    EOSProductClassInfoServiceViaRest eosProductClassInfoServiceViaRest;

    private String itemSKU1 = "12345";
    private String itemSKU2 = "11111";

    private SalesTaxRequestToGSTRequestMapper mapperUnderTest = new SalesTaxRequestToGSTRequestMapper();



    private MapperRegistry mapperRegistry;

    @BeforeAll
    public void initializeAllOriginalMappers() {
        MockitoAnnotations.openMocks(this);
        when(eosProductClassInfoServiceViaRest.getProductClassForSKU("12345")).thenReturn("PS");
        when(eosProductClassInfoServiceViaRest.getProductClassForSKU("11111")).thenReturn("Dummy");

        List<BeanMapper> requiredMappers = new ArrayList<>();
        requiredMappers.add(new GSTErrorToErrorMessageMapper());
        requiredMappers.add(new GSTResponseToPSPSalesTaxResponseMapper());
        requiredMappers.add(new GSTSalesTaxLineToPSPSalesTaxResponseLineMapper());
        requiredMappers.add(new PSPSalesTaxRequestLineToGSTSalesTaxLineMapper());
        requiredMappers.add(new SalesTaxRequestLineToGSTLineTotalMapper());
        requiredMappers.add(new SalesTaxRequestLineToGSTOrderItemMapper());
        requiredMappers.add(new SalesTaxRequestToBillToPartyMapper());
        requiredMappers.add(new SalesTaxRequestToGSTAddressMapper());
        requiredMappers.add(new SalesTaxRequestToGSTPartiesMapper());
        requiredMappers.add(new SalesTaxRequestToSalesContextMapper());
        requiredMappers.add(new SalesTaxRequestToSalesQuoteMapper());
        requiredMappers.add(new SalesTaxRequestToSalesTaxHeaderMapper());
        requiredMappers.add(new SalesTaxRequestToSalesTaxHeaderMapper());
        requiredMappers.add(new SalesTaxRequestToShipToPartyMapper());
        mapperRegistry = new MapperRegistry(requiredMappers);

        mapperUnderTest.setCustomMapperRegistry(mapperRegistry);
        for(BeanMapper mapper: requiredMappers) {
            mapper.setCustomMapperRegistry(mapperRegistry);
        }
    }


    @Test
    public void testSalesTaxRequestToGSTRequestMapperWithOriginalMappers() {
        initializeAllOriginalMappers();


        SalesTaxRequest pspSalesTaxRequest = build_SalesTaxRequest();
        GSTRequest gstRequest = mapperUnderTest.mapToTarget(pspSalesTaxRequest, GSTRequest.class);

        assertEquals("Batch001",gstRequest.getSalesTaxHeader().getDocumentId());
        assertEquals(new GregorianCalendar(2023,1,31).getTimeInMillis(),gstRequest.getSalesTaxHeader().getDocumentDateTime().longValue());
        assertEquals("13433 Wyoming Valley",gstRequest.getSalesTaxHeader().getParties().getBillToParty().get(0).getAddress().getAddressLine().get(0));

        assertEquals(itemSKU1,gstRequest.getSalesTaxLine().get(0).getOrderItem().getItemIds().getItemId().getId().getValue());
        assertEquals("PS",gstRequest.getSalesTaxLine().get(0).getOrderItem().getItemAttributes().getProductClass());
        assertEquals(itemSKU2,gstRequest.getSalesTaxLine().get(1).getOrderItem().getItemIds().getItemId().getId().getValue());
        assertEquals("Dummy",gstRequest.getSalesTaxLine().get(1).getOrderItem().getItemAttributes().getProductClass());
        assertEquals("OracleSKU",gstRequest.getSalesTaxLine().get(0).getOrderItem().getItemIds().getItemId().getId().getSchemeName());
        assertEquals(1L,gstRequest.getSalesTaxLine().get(1).getLineNumber().longValue());

        assertEquals(374.95,gstRequest.getSalesTaxHeader().getSalesQuote().getTotalTransaction().getValue().floatValue(),0.01);
        assertEquals("PSP",gstRequest.getSalesTaxHeader().getSalesContext().getSalesOrganization());
    }




    private SalesTaxRequest build_SalesTaxRequest(){
        SalesTaxRequest taxRequest = new SalesTaxRequest();
        taxRequest.setDocumentId("Batch001");
        taxRequest.setDocumentDateTime(new GregorianCalendar(2023,1,31));
        taxRequest.setCompanyName("12345");
        taxRequest.setAddressLine1("13433 Wyoming Valley");
        taxRequest.setAddressLine2("");
        taxRequest.setAddressLine3("");
        taxRequest.setCity("Austin");
        taxRequest.setCountry("US");
        taxRequest.setZipCode("78727");
        taxRequest.setState("TX");
        taxRequest.setFirstName("");
        taxRequest.setLastName("");
        taxRequest.setEmail("");
        taxRequest.setPhoneNumber("");

        SalesTaxRequestLine line1 = new SalesTaxRequestLine();
        line1.setSKU(itemSKU1);
        line1.setQuantity(1);
        line1.setAmount(new BigDecimal("74.95"));
        line1.setProductClassForSKU("PS");

        taxRequest.addLine(line1);

        SalesTaxRequestLine line2 = new SalesTaxRequestLine();
        line2.setSKU(itemSKU2);
        line2.setQuantity(1);
        line2.setAmount(new BigDecimal("300.00"));
        line2.setProductClassForSKU("Dummy");

        taxRequest.addLine(line2);

        return taxRequest;
    }

}
