package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;


public class SalesTaxRequestToBillToPartyMapperTests {

    @Mock
    private SalesTaxRequestToGSTAddressMapper salesTaxRequestToGSTAddressMapper;

    private Address dummyAddress = new Address();

    private MapperRegistry mapperRegistry;
    private SalesTaxRequestToBillToPartyMapper mapperUnderTest = new SalesTaxRequestToBillToPartyMapper();



    @BeforeEach
    public void initializeMappers() {
        MockitoAnnotations.openMocks(this);
        List<BeanMapper> requiredMappers = new ArrayList<>();

        when(salesTaxRequestToGSTAddressMapper.getSourceType()).thenReturn(SalesTaxRequest.class);
        when(salesTaxRequestToGSTAddressMapper.getTargetType()).thenReturn(Address.class);
        when(salesTaxRequestToGSTAddressMapper.mapToTarget(any(),eq(Address.class))).thenReturn(dummyAddress);
        requiredMappers.add(salesTaxRequestToGSTAddressMapper);

        mapperRegistry = new MapperRegistry(requiredMappers);
        mapperUnderTest.setCustomMapperRegistry(mapperRegistry);
    }

    @Test
    public void testHappyPath() {
        initializeMappers();
        SalesTaxRequest pspSalesTaxRequest = new SalesTaxRequest();

        BillToParty resultBillToParty = mapperUnderTest.mapToTarget(pspSalesTaxRequest,BillToParty.class);
        assertEquals("Party",resultBillToParty.getPartyId().getId().getSchemeName());
        assertEquals("XXXNOPRSPARTYXXX",resultBillToParty.getPartyId().getId().getValue());
        assertEquals(dummyAddress,resultBillToParty.getAddress());
    }

}
