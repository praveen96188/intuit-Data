package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.ResponseStatusEnum;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/DISResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Base response wrapper DIS DTO that will be returned by the WS
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"status", "disResponseMessageDISDTO"})
public class DISResponseDISDTO {

    public DISResponseDISDTO() {
    }

    @XmlElement(name = "status", nillable = false, required = true)
    private ResponseStatusEnum status = ResponseStatusEnum.Success;

    public ResponseStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ResponseStatusEnum status) {
        this.status = status;
    }

    @XmlElementWrapper(name="ResponseMessages")
    @XmlElements(
            {
                @XmlElement(name = "ResponseMessage")
            }
    )
    private List<DISResponseMessageDISDTO> disResponseMessageDISDTO;

    public List<DISResponseMessageDISDTO> getDisResponseMessageDISDTO() {
        return disResponseMessageDISDTO;
    }

    public void setDisResponseMessageDISDTO(List<DISResponseMessageDISDTO> disResponseMessageDISDTO) {
        this.disResponseMessageDISDTO = disResponseMessageDISDTO;
    }
}
