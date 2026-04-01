package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.Offer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SAPPriceTypeOffer {


    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    String priceType;


    public ArrayList<SAPOffer> getOfferList() {
        return offerList;
    }

    public void setOfferList(ArrayList<SAPOffer> offerList) {
        this.offerList = offerList;
    }

    ArrayList<SAPOffer> offerList;


}
