package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;

/**
    @author Jeff Jones
 */

@XmlSeeAlso({
    EwsAssistedService.class,
    EwsDirectDepositService.class
})

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "promotionId"
})
public class EwsBaseService implements Cloneable {

    @XmlElement(name = "PromotionId", required = false)
    protected String promotionId;

    public EwsBaseService clone() throws CloneNotSupportedException {
        return (EwsBaseService) super.clone();
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.promotionId, true, "^(\\P{M}\\p{M}*){0,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PromotionId", "BaseService"));
        }
    }    
}
