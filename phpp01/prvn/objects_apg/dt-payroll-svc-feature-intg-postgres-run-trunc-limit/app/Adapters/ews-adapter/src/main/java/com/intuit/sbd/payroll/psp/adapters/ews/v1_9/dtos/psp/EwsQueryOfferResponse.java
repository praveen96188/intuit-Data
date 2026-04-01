package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author Marcela Villani
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "offerCode",
    "description"
})

public class EwsQueryOfferResponse extends EwsResponse implements Cloneable {

    @XmlElement(name = "OfferCode", required = false)
    protected String offerCode;

    @XmlElement(name = "Description", required = false)
    protected String description;

    public EwsQueryOfferResponse() {
        super();
    }

    public EwsQueryOfferResponse clone() throws CloneNotSupportedException {
        return (EwsQueryOfferResponse) super.clone();
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}