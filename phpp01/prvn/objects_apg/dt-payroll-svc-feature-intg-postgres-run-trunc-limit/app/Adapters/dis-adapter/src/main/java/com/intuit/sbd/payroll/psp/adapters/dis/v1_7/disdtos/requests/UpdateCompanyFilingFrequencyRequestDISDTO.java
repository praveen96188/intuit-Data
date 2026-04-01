package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;
import com.intuit.sbd.payroll.psp.domain.SettlementType;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

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
public class UpdateCompanyFilingFrequencyRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(nillable = false, required = true)
    private Boolean fileType944;

    @XmlElement(nillable = false, required = true)
    private String token;

    @XmlElement(nillable = false, required = true)
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
