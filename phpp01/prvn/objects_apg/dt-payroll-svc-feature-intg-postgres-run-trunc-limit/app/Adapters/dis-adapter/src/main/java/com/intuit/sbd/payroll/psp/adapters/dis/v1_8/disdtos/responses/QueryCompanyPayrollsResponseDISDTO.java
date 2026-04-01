package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyPayrollDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Response WS DTO for the query company events request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class QueryCompanyPayrollsResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private List<CompanyPayrollDISDTO> companyPayrollDISDTOs;

    public List<CompanyPayrollDISDTO> getCompanyPayrollDISDTOs() {
        return companyPayrollDISDTOs;
    }

    public void setCompanyPayrollDISDTOs(List<CompanyPayrollDISDTO> pCompanyPayrollDISDTOs) {
        companyPayrollDISDTOs = pCompanyPayrollDISDTOs;
    }

    public void clearElements() {
        this.companyPayrollDISDTOs = null;
    }

}
