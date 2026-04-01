package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.ResponseStatusEnum;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
