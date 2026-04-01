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
public class UpdateCompanyFilingFrequencyRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(nillable = false, required = true)
    private Boolean fileType944;

    @XmlElement(nillable = false, required = false)
    private String token;

    @XmlElement(nillable = false, required = false)
    private String corpId;

    @XmlElement(nillable = false, required = false)
    private String noteToAttachTEvent;

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

    public Boolean getFileType944() {
        return fileType944;
    }

    public void setFileType944(Boolean pFileType944) {
        fileType944 = pFileType944;
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

    public String getNoteToAttachTEvent() {
        return noteToAttachTEvent;
    }

    public void setNoteToAttachTEvent(String pNoteToAttachTEvent) {
        noteToAttachTEvent = pNoteToAttachTEvent;
    }
}
