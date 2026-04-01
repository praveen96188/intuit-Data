/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;

import javax.xml.bind.annotation.*;

/**
    @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetPayrollInfo")
public class GetPayrollInfoWSDTO implements Cloneable {

    private GetPayrollStatusWSDTO mGetPayrollStatusWSDTO;

    public GetPayrollInfoWSDTO clone() throws CloneNotSupportedException {
        GetPayrollInfoWSDTO clone = (GetPayrollInfoWSDTO) super.clone();

        if (mGetPayrollStatusWSDTO != null) {
            clone.setPayrollStatusWSDTO(mGetPayrollStatusWSDTO.clone());
        }

        return clone;
    }

    @XmlElement(name = "GetPayrollStatus", required = true, nillable = false)
    public GetPayrollStatusWSDTO getPayrollStatusWSDTO() {
        return mGetPayrollStatusWSDTO;
    }

    public void setPayrollStatusWSDTO(GetPayrollStatusWSDTO getPayrollStatusWSDTO) {
        this.mGetPayrollStatusWSDTO = getPayrollStatusWSDTO;
    }

    public void validateGetPayrollStatus() throws Exception {
        if (this.mGetPayrollStatusWSDTO == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("GetPayrollStatus"));
        }
    }
}
