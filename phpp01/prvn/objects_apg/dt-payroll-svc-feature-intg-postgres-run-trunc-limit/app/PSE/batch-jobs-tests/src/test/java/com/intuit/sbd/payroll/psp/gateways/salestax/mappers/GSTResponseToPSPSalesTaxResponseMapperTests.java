package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponse;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxResponseLine;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

public class GSTResponseToPSPSalesTaxResponseMapperTests {

    private MapperRegistry mapperRegistry;

    @Mock
    GSTErrorToErrorMessageMapper gstErrorToErrorMessageMapper;

    @Mock
    GSTSalesTaxLineToPSPSalesTaxResponseLineMapper gstSalesTaxLineToPSPSalesTaxResponseLineMapper;

    SalesTaxResponseLine dummySalesTaxResponseLine = new SalesTaxResponseLine();


    @BeforeEach
    public void initializeAllMappers() {
        MockitoAnnotations.openMocks(this);
        ErrorMessage dummyErrorMessage = new ErrorMessage();
        dummyErrorMessage.setErrorCode("504");
        dummyErrorMessage.setErrorDescription("summaryErrorDescription");
        when(gstErrorToErrorMessageMapper.mapToTarget(any(), eq(ErrorMessage.class))).thenReturn(dummyErrorMessage);
        when(gstErrorToErrorMessageMapper.getSourceType()).thenReturn(GSTError.class);
        when(gstErrorToErrorMessageMapper.getTargetType()).thenReturn(ErrorMessage.class);

        dummySalesTaxResponseLine.setSKU("12345");
        dummySalesTaxResponseLine.setTaxAmount(new BigDecimal("12.34"));
        dummySalesTaxResponseLine.setTaxRate(new BigDecimal("6.25"));

        when(gstSalesTaxLineToPSPSalesTaxResponseLineMapper.getSourceType()).thenReturn(SalesTaxLine.class);
        when(gstSalesTaxLineToPSPSalesTaxResponseLineMapper.getTargetType()).thenReturn(SalesTaxResponseLine.class);
        when(gstSalesTaxLineToPSPSalesTaxResponseLineMapper.mapToTarget(any(),any())).thenReturn(dummySalesTaxResponseLine);

        List<BeanMapper> requiredMappers = new ArrayList<>();
        requiredMappers.add(gstErrorToErrorMessageMapper);
        requiredMappers.add(gstSalesTaxLineToPSPSalesTaxResponseLineMapper);
        mapperRegistry = new MapperRegistry(requiredMappers);

        GSTResponseToPSPSalesTaxResponseMapper gstResponseToPSPSalesTaxResponseMapper = new GSTResponseToPSPSalesTaxResponseMapper();
        gstResponseToPSPSalesTaxResponseMapper.setCustomMapperRegistry(mapperRegistry);
        mapperRegistry.addMapperToRegistry(gstResponseToPSPSalesTaxResponseMapper);
    }


    @Test
    public void verifyFailureGSTResponse() {
        initializeAllMappers();
        GSTResponse gstResponse = new GSTResponse();

        Result result = new Result();
        result.setStatus("Failure");
        result.setError(new GSTError());
        gstResponse.setResult(result);

        List<GSTError> errorList = new ArrayList<>();
        errorList.add(new GSTError());
        gstResponse.setError(errorList);

        SalesTaxResponse pspSalesTaxResponse = mapperRegistry.mapToTarget(gstResponse,SalesTaxResponse.class);
        assertFalse(pspSalesTaxResponse.isSuccess());
        assertEquals("504",pspSalesTaxResponse.getSummaryErrorMessage().getErrorCode());
        assertEquals("summaryErrorDescription",pspSalesTaxResponse.getSummaryErrorMessage().getErrorDescription());

        assertEquals(1,pspSalesTaxResponse.getDetailErrorMessageList().size());
    }

    @Test
    public void verifySuccessGSTResponse() {
        initializeAllMappers();

        GSTResponse gstResponse = new GSTResponse();

        Result result = new Result();
        result.setStatus("Success");
        gstResponse.setResult(result);

        TotalTax totalTax = new TotalTax();
        totalTax.setValue((float) 12.34);
        totalTax.setCurrency("USD");

        SalesQuote salesQuote = new SalesQuote();
        salesQuote.setTotalTax(totalTax);

        SalesTaxHeader salesTaxHeader = new SalesTaxHeader();
        salesTaxHeader.setSalesQuote(salesQuote);
        gstResponse.setSalesTaxHeader(salesTaxHeader);

        List<SalesTaxLine> salesTaxLineList = new ArrayList<>();
        SalesTaxLine salesTaxLine = new SalesTaxLine();
        salesTaxLineList.add(salesTaxLine);
        gstResponse.setSalesTaxLine(salesTaxLineList);


        SalesTaxResponse salesTaxResponse = mapperRegistry.mapToTarget(gstResponse,SalesTaxResponse.class);
        assertTrue(salesTaxResponse.isSuccess());
        assertEquals(new BigDecimal("12.34"),salesTaxResponse.getTotalTaxAmount());
        assertEquals(dummySalesTaxResponseLine,salesTaxResponse.getSalesTaxResponseLineList().get(0));

    }


}
