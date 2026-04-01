package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTransmissionRequest", propOrder = {
        "sourceCompanyId",
        "sourceSystemCd",
        "transmissionType"
})
@XmlRootElement(name = "GetTransmissionRequest")
public class LtTransmissionRqWSDTO {

    @XmlElement(name = "Company", required = true)
    protected String sourceCompanyId;

    @XmlElement(name = "System", required = true)
    protected SourceSystemCode sourceSystemCd;

    @XmlElement(name = "TransType", required = true)
    protected TransmissionType transmissionType;

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    public SourceSystemCode getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(SourceSystemCode sourceSystemId) {
        this.sourceSystemCd = sourceSystemId;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }
}
