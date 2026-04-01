package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

/**
 * @author Jeff Jones
 */

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)

public class EwsQueryIndustryList extends EwsRequest implements Cloneable {

    public void validate() throws Exception {
        super.validate();
    }

}
