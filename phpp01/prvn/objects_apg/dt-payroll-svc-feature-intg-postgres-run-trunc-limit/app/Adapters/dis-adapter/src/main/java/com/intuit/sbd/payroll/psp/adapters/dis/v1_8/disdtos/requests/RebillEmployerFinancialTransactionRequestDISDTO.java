package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement()
@XmlType()
public class RebillEmployerFinancialTransactionRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(nillable = false, required = true)
    private String financialTransactionId;

    @XmlElement(nillable = false, required = true)
    private BigDecimal rebillAmount;

    @XmlElement(nillable = false, required = false)
    private String token;

    @XmlElement(nillable = false, required = false)
    private String corpId;

    @XmlElement(nillable = false, required = false)
    private String noteToAttachToRebillEvent;

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

    public BigDecimal getRebillAmount() {
        return rebillAmount;
    }

    public void setRebillAmount(BigDecimal pRebillAmount) {
        rebillAmount = pRebillAmount;
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

    public String getNoteToAttachToRebillEvent() {
        return noteToAttachToRebillEvent;
    }

    public void setNoteToAttachToRebillEvent(String pNoteToAttachToRebillEvent) {
        noteToAttachToRebillEvent = pNoteToAttachToRebillEvent;
    }
}
