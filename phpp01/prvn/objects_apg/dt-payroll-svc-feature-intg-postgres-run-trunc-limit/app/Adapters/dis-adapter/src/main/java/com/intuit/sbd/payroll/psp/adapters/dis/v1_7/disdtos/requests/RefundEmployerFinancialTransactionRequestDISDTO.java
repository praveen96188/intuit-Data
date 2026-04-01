package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.domain.SettlementType;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/requests/RefundEmployerFinancialTransactionRequestDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
@XmlType()
public class RefundEmployerFinancialTransactionRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(nillable = false, required = true)
    private String financialTransactionId;

    @XmlElement(nillable = false, required = true)
    private BigDecimal refundAmount;

    @XmlElement(nillable = false, required = true)
    private SettlementType settlementType;

    @XmlElement(nillable = false, required = true)
    private String token;

    @XmlElement(nillable = false, required = true)
    private String corpId;

    @XmlElement(nillable = false, required = false)
    private String noteToAttachToRefundEvent;

    public SourceSystemEnum getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystemEnum pSourceSystem) {
        sourceSystem = pSourceSystem;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String pSourceCompanyId) {
        sourceCompanyId = pSourceCompanyId;
    }

    public String getFinancialTransactionId() {
        return financialTransactionId;
    }

    public void setFinancialTransactionId(String pFinancialTransactionId) {
        financialTransactionId = pFinancialTransactionId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal pRefundAmount) {
        refundAmount = pRefundAmount;
    }

    public SettlementType getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(SettlementType pSettlementType) {
        settlementType = pSettlementType;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String pToken) {
        token = pToken;
    }

    public String getCorpId() {
        return corpId;
    }

    public void setCorpId(String pCorpId) {
        corpId = pCorpId;
    }

    public String getNoteToAttachToRefundEvent() {
        return noteToAttachToRefundEvent;
    }

    public void setNoteToAttachToRefundEvent(String pNoteToAttachToRefundEvent) {
        noteToAttachToRefundEvent = pNoteToAttachToRefundEvent;
    }

}
