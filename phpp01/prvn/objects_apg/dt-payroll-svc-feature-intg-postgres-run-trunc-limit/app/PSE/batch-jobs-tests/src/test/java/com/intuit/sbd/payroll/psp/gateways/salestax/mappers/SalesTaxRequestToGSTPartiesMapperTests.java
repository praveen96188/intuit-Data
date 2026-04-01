package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.BillToParty;
import com.intuit.gst.Parties;
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

public class SalesTaxRequestToGSTPartiesMapperTests {

    @Mock
    private SalesTaxRequestToBillToPartyMapper salesTaxRequestToBillToPartyMapper;
    
    @Mock
    private SalesTaxRequestToShipToPartyMapper salesTaxRequestToShipToPartyMapper;

    private BillToParty dummyBillToParty = new BillToParty();
    private ShipToParty dummyShipToParty = new ShipToParty();

    private MapperRegistry mapperRegistry;
    private SalesTaxRequestToGSTPartiesMapper mapperUnderTest = new SalesTaxRequestToGSTPartiesMapper();



    @BeforeEach
    public void initializeMappers() {
        MockitoAnnotations.openMocks(this);
        List<BeanMapper> requiredMappers = new ArrayList<>();

        when(salesTaxRequestToBillToPartyMapper.getSourceType()).thenReturn(SalesTaxRequest.class);
        when(salesTaxRequestToBillToPartyMapper.getTargetType()).thenReturn(BillToParty.class);
        when(salesTaxRequestToBillToPartyMapper.mapToTarget(any(),eq(BillToParty.class))).thenReturn(dummyBillToParty);
        requiredMappers.add(salesTaxRequestToBillToPartyMapper);

        when(salesTaxRequestToShipToPartyMapper.getSourceType()).thenReturn(SalesTaxRequest.class);
        when(salesTaxRequestToShipToPartyMapper.getTargetType()).thenReturn(ShipToParty.class);
        when(salesTaxRequestToShipToPartyMapper.mapToTarget(any(),eq(ShipToParty.class))).thenReturn(dummyShipToParty);
        requiredMappers.add(salesTaxRequestToShipToPartyMapper);

        mapperRegistry = new MapperRegistry(requiredMappers);
        mapperUnderTest.setCustomMapperRegistry(mapperRegistry);

    }

    @Test
    public void testHappyPath() {
        initializeMappers();
        SalesTaxRequest pspSalesTaxRequest = new SalesTaxRequest();

        Parties resultGSTParties = mapperUnderTest.mapToTarget(pspSalesTaxRequest, Parties.class);
        assertEquals(1,resultGSTParties.getBillToParty().size());
        assertEquals(dummyBillToParty,resultGSTParties.getBillToParty().get(0));
        assertEquals(1,resultGSTParties.getShipToParty().size());
        assertEquals(dummyShipToParty,resultGSTParties.getShipToParty().get(0));
    }
    
    
}
