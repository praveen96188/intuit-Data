package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2008
 * Time: 2:21:20 PM
 */
public class SAPOffer {
    private String offerCd;
    private String name;
    private String description;
    private String offerEndEvent;
    private Date effectiveDate;
    private Date expirationDate;
    private boolean openToUser;

    public String getOfferEndEvent() {
        return offerEndEvent;
    }

    public void setOfferEndEvent(String offerExpirationEvent) {
        this.offerEndEvent = offerExpirationEvent;
    }

    public String getOfferCd() {
        return offerCd;
    }

    public void setOfferCd(String offerCd) {
        this.offerCd = offerCd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean getOpenToUser() {
        return openToUser;
    }

    public void setOpenToUser(boolean pOpenToUser) {
        openToUser = pOpenToUser;
    }

    public SAPOffer(String offerCd, String description) {
        this.offerCd = offerCd;
        this.description = description;
    }
    public int hashCode(){
        int hashcode = 0;
        hashcode = offerCd.length()*20;
        hashcode += description.hashCode();
        return hashcode;
    }

    public boolean equals(Object obj){
        if (obj instanceof SAPOffer) {
            SAPOffer sapOffer = (SAPOffer) obj;
            return (sapOffer.getOfferCd().equals(this.offerCd) );
        } else {
            return false;
        }
    }


    public SAPOffer() {
        super();
    }
}
