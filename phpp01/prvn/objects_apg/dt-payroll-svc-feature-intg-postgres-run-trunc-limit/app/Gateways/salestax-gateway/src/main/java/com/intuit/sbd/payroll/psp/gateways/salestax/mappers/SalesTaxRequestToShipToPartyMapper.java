package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.springframework.stereotype.Component;

@Component
public class SalesTaxRequestToShipToPartyMapper extends BeanMapper<SalesTaxRequest, ShipToParty> {

    @Override
    public ShipToParty mapToTarget(SalesTaxRequest pspSalesTaxRequest, Class<ShipToParty> shipToPartyClass) {
        ShipToParty shipToParty = new ShipToParty();

        Address shipToPartyAddress = getMapperRegistry().mapToTarget(pspSalesTaxRequest,Address.class);
        shipToParty.setAddress(shipToPartyAddress);
        //todo - check if it is ok to set default party Id for all requests
        PartyId defaultPartyId = getDefaultPartyId();
        shipToParty.setPartyId(defaultPartyId);

        return shipToParty;
    }


    private PartyId getDefaultPartyId() {
        PartyId partyId = new PartyId();
        Id idForDefaultPartyId  = getIdForDefaultPartyId();
        partyId.setId(idForDefaultPartyId);
        return partyId;
    }

    private Id getIdForDefaultPartyId() {
        Id idForDefaultPartyId = new Id();
        idForDefaultPartyId.setSchemeName("Party");
        idForDefaultPartyId.setValue("XXXNOPRSPARTYXXX");
        return idForDefaultPartyId;
    }
    
    
}
