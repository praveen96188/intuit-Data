package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "legalName"
})
public class EwsLegalInfo extends EwsAddress implements Cloneable {

    @XmlElement(name = "LegalName", required = true)
    protected String legalName;

    public EwsLegalInfo() {
        super();
    }

    public EwsLegalInfo clone() throws CloneNotSupportedException {
        return (EwsLegalInfo) super.clone();
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.legalName, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LegalName", "LegalInfo"));
        }
    }
}
