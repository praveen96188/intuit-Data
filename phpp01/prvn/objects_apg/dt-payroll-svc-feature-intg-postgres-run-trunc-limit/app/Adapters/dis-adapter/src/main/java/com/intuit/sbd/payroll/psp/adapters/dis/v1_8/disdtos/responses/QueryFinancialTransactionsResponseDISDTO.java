package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.FinancialTransactionDISDTO;

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
public class QueryFinancialTransactionsResponseDISDTO extends ResponseDISDTO {

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
