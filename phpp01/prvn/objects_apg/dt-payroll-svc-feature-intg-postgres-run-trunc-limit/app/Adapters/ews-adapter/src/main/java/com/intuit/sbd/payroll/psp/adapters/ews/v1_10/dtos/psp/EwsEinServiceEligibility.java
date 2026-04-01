package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "ein",
        "enableCurrentTaxYearValidation"
})
public class EwsEinServiceEligibility extends EwsRequest implements Cloneable{

    @XmlElement(name = "EIN", required = true)
    protected String ein;

    @XmlElement(name = "EnableCurrentTaxYearValidation", required = true)
    protected boolean enableCurrentTaxYearValidation;

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public boolean isEnableCurrentTaxYearValidation() {
        return enableCurrentTaxYearValidation;
    }

    public void setEnableCurrentTaxYearValidation(boolean enableCurrentTaxYearValidation) {
        this.enableCurrentTaxYearValidation = enableCurrentTaxYearValidation;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.ein, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("EIN", "Company"));
        }
    }

}
