package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyPayrollDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.FinancialTransactionDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryCompanyPayrollsResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
