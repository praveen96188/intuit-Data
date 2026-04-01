package com.intuit.sbd.payroll.psp.gateways.salestax.mappers;

import com.intuit.gst.BillToParty;
import com.intuit.gst.Parties;
import com.intuit.gst.ShipToParty;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.SalesTaxRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SalesTaxRequestToGSTPartiesMapper extends BeanMapper<SalesTaxRequest, Parties> {
    @Override
    public Parties mapToTarget(SalesTaxRequest pspSalesTaxRequest, Class<Parties> partiesClass) {

        Parties gstParties = new Parties();

        //todo - check this mapper (2)
        BillToParty billToParty = getMapperRegistry().mapToTarget(pspSalesTaxRequest,BillToParty.class);

        //todo - check this mapper (2)
        ShipToParty shipToParty = getMapperRegistry().mapToTarget(pspSalesTaxRequest,ShipToParty.class);

        List<BillToParty> billToPartyList = new ArrayList<>();
        billToPartyList.add(billToParty);

        List<ShipToParty> shipToPartyList = new ArrayList<>();
        shipToPartyList.add(shipToParty);

        gstParties.setBillToParty(billToPartyList);
        gstParties.setShipToParty(shipToPartyList);

        return gstParties;
    }
}
