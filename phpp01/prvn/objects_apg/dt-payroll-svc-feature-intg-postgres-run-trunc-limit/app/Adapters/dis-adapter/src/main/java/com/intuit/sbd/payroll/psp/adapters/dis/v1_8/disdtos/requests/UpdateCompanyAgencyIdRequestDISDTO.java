package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;

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
public class UpdateCompanyAgencyIdRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(nillable = false, required = true)
    private String paymentTemplateCd;

    @XmlElement(nillable = false, required = true)
    private String agencyId;

    @XmlElement(nillable = false, required = false)
    private String token;

    @XmlElement(nillable = false, required = false)
    private String corpId;

    @XmlElement(nillable = false, required = false)
    private String noteToAttachToEvent;

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

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String pPaymentTemplateCd) {
        paymentTemplateCd = pPaymentTemplateCd;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String pAgencyId) {
        agencyId = pAgencyId;
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

    public String getNoteToAttachToEvent() {
        return noteToAttachToEvent;
    }

    public void setNoteToAttachToEvent(String pNoteToAttachToEvent) {
        noteToAttachToEvent = pNoteToAttachToEvent;
    }
}
