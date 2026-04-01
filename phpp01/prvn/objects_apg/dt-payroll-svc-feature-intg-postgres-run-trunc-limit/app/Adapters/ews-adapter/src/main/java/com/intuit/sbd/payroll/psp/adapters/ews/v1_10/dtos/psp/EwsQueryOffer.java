package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;


import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.*;

/**
 * @author Marcela Villani
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "offerCode"
})
public class EwsQueryOffer extends EwsRequest implements Cloneable {

    @XmlElement(name = "OfferCode", required = true)
    protected String offerCode;

    public EwsQueryOffer() {
        super();
    }

    public EwsQueryOffer clone() throws CloneNotSupportedException {
        return (EwsQueryOffer) super.clone();
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.offerCode, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("OfferCode", "QueryOffer"));
        }

    }
}