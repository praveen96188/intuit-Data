package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.Address;
import com.intuit.gst.BillToParty;
import com.intuit.gst.Id;
import com.intuit.gst.PartyId;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.springframework.stereotype.Component;

@Component
public class SalesTaxRequestToBillToPartyMapper extends BeanMapper<SalesTaxRequest, BillToParty> {

    @Override
    public BillToParty mapToTarget(SalesTaxRequest pspSalesTaxRequest, Class<BillToParty> billToPartyClass) {
        BillToParty billToParty = new BillToParty();

        Address billToPartyAddress = getMapperRegistry().mapToTarget(pspSalesTaxRequest,Address.class);
        billToParty.setAddress(billToPartyAddress);
        //todo - check if it is ok to set default party Id for all requests
        PartyId defaultPartyId = getDefaultPartyId();
        billToParty.setPartyId(defaultPartyId);

        return billToParty;
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
