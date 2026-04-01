package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.Address;
import com.intuit.gst.ShipToParty;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class SalesTaxRequestToShipToPartyMapperTests {

    @Mock
    private SalesTaxRequestToGSTAddressMapper salesTaxRequestToGSTAddressMapper;

    private Address dummyAddress = new Address();

    private MapperRegistry mapperRegistry;
    private SalesTaxRequestToShipToPartyMapper mapperUnderTest = new SalesTaxRequestToShipToPartyMapper();



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

        ShipToParty resultShipToParty = mapperUnderTest.mapToTarget(pspSalesTaxRequest, ShipToParty.class);
        assertEquals("Party",resultShipToParty.getPartyId().getId().getSchemeName());
        assertEquals("XXXNOPRSPARTYXXX",resultShipToParty.getPartyId().getId().getValue());
        assertEquals(dummyAddress,resultShipToParty.getAddress());
    }

}
