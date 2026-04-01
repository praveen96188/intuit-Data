package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyServiceUpdatedDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.FinancialTransactionDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryEmployerFinancialTransactionsResponseDISDTO.java $
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
public class QueryEmployerFinancialTransactionsResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private List<FinancialTransactionDISDTO> financialTransactionDISDTOs;

    public List<FinancialTransactionDISDTO> getFinancialTransactionDISDTOs() {
        return financialTransactionDISDTOs;
    }

    public void setFinancialTransactionDISDTOs(List<FinancialTransactionDISDTO> pFinancialTransactionDISDTOs) {
        financialTransactionDISDTOs = pFinancialTransactionDISDTOs;
    }

    public void clearElements() {
        this.financialTransactionDISDTOs = null;
    }

}
