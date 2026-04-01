package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Response DIS DTO class that all WS DIS DTO classes will extend.
 *
 */


@XmlSeeAlso({
        QueryCompanyAgenciesYearInfoResponseDISDTO.class,
        QueryCompanyEmployeesWihPaycheckCountResponseDISDTO.class,
        QueryCompanyEventsResponseDISDTO.class,
        QueryCompanyLatestPayrollDatesResponseDISDTO.class,
        QueryCompanyPaymentTemplatesResponseDISDTO.class,
        SearchSAPCompanyResponseDISDTO.class
})

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "disResponse"
})
public abstract class ResponseDISDTO {
    @XmlElement(name = "Response", nillable = false, required = true)
    private DISResponseDISDTO disResponse = new DISResponseDISDTO();

    public DISResponseDISDTO getDisResponse() {
        return disResponse;
    }

    public void setDISResponse(DISResponseDISDTO disResponse) {
        this.disResponse = disResponse;
    }

    /***
     * Clear the XML elements to be returned in the WS.
     */
    public abstract void clearElements();


}
