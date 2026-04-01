package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.ErrorMessage;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequestLine;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

//import static org.junit.Assert.*;


public class PSPSalesTaxRequestLineToGSTSalesTaxLineMapperTests {

    @Mock
    SalesTaxRequestLineToGSTLineTotalMapper salesTaxRequestLineToGSTLineTotalMapper;

    @Mock
    SalesTaxRequestLineToGSTOrderItemMapper salesTaxRequestLineToGSTOrderItemMapper;

    private LineTotal dummyLineTotal = new LineTotal((float) 1.23,"USD");
    private OrderItem dummyOrderItem = new OrderItem();

    private MapperRegistry mapperRegistry;

    //object being tested
    private PSPSalesTaxRequestLineToGSTSalesTaxLineMapper mapperUnderTest;


    @BeforeEach
    public void initializeAllMappers() {
        MockitoAnnotations.openMocks(this);
        ErrorMessage dummyErrorMessage = new ErrorMessage();
        dummyErrorMessage.setErrorCode("504");
        dummyErrorMessage.setErrorDescription("summaryErrorDescription");

        when(salesTaxRequestLineToGSTLineTotalMapper.getSourceType()).thenReturn(SalesTaxRequestLine.class);
        when(salesTaxRequestLineToGSTLineTotalMapper.getTargetType()).thenReturn(LineTotal.class);
        when(salesTaxRequestLineToGSTLineTotalMapper.mapToTarget(any(),eq(LineTotal.class))).thenReturn(dummyLineTotal);

        when(salesTaxRequestLineToGSTOrderItemMapper.getSourceType()).thenReturn(SalesTaxRequestLine.class);
        when(salesTaxRequestLineToGSTOrderItemMapper.getTargetType()).thenReturn(OrderItem.class);
        dummyOrderItem.setDescription("dummyOrderItem");
        when(salesTaxRequestLineToGSTOrderItemMapper.mapToTarget(any(),eq(OrderItem.class))).thenReturn(dummyOrderItem);



        List<BeanMapper> requiredMappers = new ArrayList<>();
        requiredMappers.add(salesTaxRequestLineToGSTLineTotalMapper);
        requiredMappers.add(salesTaxRequestLineToGSTOrderItemMapper);
        mapperRegistry = new MapperRegistry(requiredMappers);

        mapperUnderTest = new PSPSalesTaxRequestLineToGSTSalesTaxLineMapper();
        mapperUnderTest.setCustomMapperRegistry(mapperRegistry);
        mapperRegistry.addMapperToRegistry(mapperUnderTest);
    }

    @Test
    public void testHappyPath() {
        initializeAllMappers();
        SalesTaxRequestLine pspSalesTaxRequestLine = new SalesTaxRequestLine();
        pspSalesTaxRequestLine.setQuantity(3);
        pspSalesTaxRequestLine.setAmount(BigDecimal.valueOf(1.23));
        pspSalesTaxRequestLine.setSKU("000");

        SalesTaxLine gstSalesTaxLine = mapperUnderTest.mapToTarget(pspSalesTaxRequestLine,SalesTaxLine.class);

        Assertions.assertEquals(dummyLineTotal,gstSalesTaxLine.getLineTotal());
        Assertions.assertEquals(dummyOrderItem,gstSalesTaxLine.getOrderItem());
        Assertions.assertEquals(3,gstSalesTaxLine.getOrderQuantity().getValue());
        Assertions.assertEquals("Each",gstSalesTaxLine.getOrderQuantity().getUom());

        UnitPrice unitPrice = gstSalesTaxLine.getUnitPrice();
        Assertions.assertEquals(1.23,unitPrice.getAmount().getValue());
        Assertions.assertEquals("USD",unitPrice.getAmount().getCurrency());
        Assertions.assertEquals("Each",unitPrice.getPerQuantity().getUom());
        Assertions.assertEquals(1L,unitPrice.getPerQuantity().getValue());

        Assertions.assertEquals("ADD",gstSalesTaxLine.getActionType());

    }

}
