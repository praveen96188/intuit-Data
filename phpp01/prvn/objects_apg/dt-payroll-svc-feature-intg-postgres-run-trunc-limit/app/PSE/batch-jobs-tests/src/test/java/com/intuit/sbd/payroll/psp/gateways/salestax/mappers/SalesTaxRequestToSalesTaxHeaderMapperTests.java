package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;


import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class SalesTaxRequestToSalesTaxHeaderMapperTests {

    @Mock
    private SalesTaxRequestToGSTPartiesMapper salesTaxRequestToGSTPartiesMapper;
    private Parties dummyGSTParties = new Parties();

    @Mock
    private SalesTaxRequestToSalesContextMapper salesTaxRequestToSalesContextMapper;
    private SalesContext dummySalesContext = new SalesContext();

    @Mock
    private SalesTaxRequestToSalesQuoteMapper salesTaxRequestToSalesQuoteMapper;
    private SalesQuote dummySalesQuote = new SalesQuote();

    private MapperRegistry mapperRegistry;
    private SalesTaxRequestToSalesTaxHeaderMapper mapperUnderTest = new SalesTaxRequestToSalesTaxHeaderMapper();

    @BeforeEach
    public void initializeMappers() {
        MockitoAnnotations.openMocks(this);
        List<BeanMapper> requiredMappers = new ArrayList<>();

        when(salesTaxRequestToGSTPartiesMapper.getSourceType()).thenReturn(SalesTaxRequest.class);
        when(salesTaxRequestToGSTPartiesMapper.getTargetType()).thenReturn(Parties.class);
        when(salesTaxRequestToGSTPartiesMapper.mapToTarget(any(),eq(Parties.class))).thenReturn(dummyGSTParties);
        requiredMappers.add(salesTaxRequestToGSTPartiesMapper);

        when(salesTaxRequestToSalesContextMapper.getSourceType()).thenReturn(SalesTaxRequest.class);
        when(salesTaxRequestToSalesContextMapper.getTargetType()).thenReturn(SalesContext.class);
        when(salesTaxRequestToSalesContextMapper.mapToTarget(any(),eq(SalesContext.class))).thenReturn(dummySalesContext);
        requiredMappers.add(salesTaxRequestToSalesContextMapper);

        when(salesTaxRequestToSalesContextMapper.getSourceType()).thenReturn(SalesTaxRequest.class);
        when(salesTaxRequestToSalesContextMapper.getTargetType()).thenReturn(SalesContext.class);
        when(salesTaxRequestToSalesContextMapper.mapToTarget(any(),eq(SalesContext.class))).thenReturn(dummySalesContext);
        requiredMappers.add(salesTaxRequestToSalesContextMapper);

        when(salesTaxRequestToSalesQuoteMapper.getSourceType()).thenReturn(SalesTaxRequest.class);
        when(salesTaxRequestToSalesQuoteMapper.getTargetType()).thenReturn(SalesQuote.class);
        when(salesTaxRequestToSalesQuoteMapper.mapToTarget(any(),eq(SalesQuote.class))).thenReturn(dummySalesQuote);
        requiredMappers.add(salesTaxRequestToSalesQuoteMapper);

        mapperRegistry = new MapperRegistry(requiredMappers);
        mapperUnderTest.setCustomMapperRegistry(mapperRegistry);
    }


    @Test
    public void testHappyPath() {
        initializeMappers();
        SalesTaxRequest salesTaxRequest = new SalesTaxRequest();
        salesTaxRequest.setDocumentId("dummyDocId");
        Calendar dummyDate = new GregorianCalendar(2023,1,31);
        salesTaxRequest.setDocumentDateTime(dummyDate);

        SalesTaxHeader resultSalesTaxHeader = mapperUnderTest.mapToTarget(salesTaxRequest,SalesTaxHeader.class);
        assertEquals("dummyDocId",resultSalesTaxHeader.getDocumentId());
        assertEquals(dummyDate.getTimeInMillis(),resultSalesTaxHeader.getDocumentDateTime().longValue());
        assertEquals(dummyGSTParties,resultSalesTaxHeader.getParties());
        assertEquals(dummySalesContext,resultSalesTaxHeader.getSalesContext());
        assertEquals(dummySalesQuote,resultSalesTaxHeader.getSalesQuote());
    }

}
