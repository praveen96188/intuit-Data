package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.LedgerTransactionDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryCompanyEventsResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Response WS DTO for the query company events request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType()
public class QueryCompanyCourtesyFeeRefundsResponseDISDTO extends ResponseDISDTO {

    @XmlElement
    private List<LedgerTransactionDISDTO> ledgerTransactions;

    public List<LedgerTransactionDISDTO> getLedgerTransactions() {
        return ledgerTransactions;
    }

    public void setLedgerTransactions(List<LedgerTransactionDISDTO> companyEvents) {
        this.ledgerTransactions = companyEvents;
    }

    public void clearElements() {
        this.ledgerTransactions = null;
    }


}
