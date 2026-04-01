package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;
import com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates.*;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTransactionDatesResponse", propOrder = {
        "ewsDTO",
        "qbdtwsDTO",
        "qbdtDTO",
        "qboeDTO",
        "testwsDTO"
})
@XmlRootElement(name = "GetTransactionDatesResponse")
public class LtDateRsWSDTO {

    @XmlElement(name = "EWS_Dates", required = false)
    protected EWSDateDTO ewsDTO;

    @XmlElement(name = "QBDTWS_Dates", required = false)
    protected QBDT_WSDateDTO qbdtwsDTO;

    @XmlElement(name = "QBDT_Dates", required = false)
    protected QBDTDateDTO qbdtDTO;

    @XmlElement(name = "QBOE_Dates", required = false)
    protected QBOEDateDTO qboeDTO;

    @XmlElement(name = "TestWS_Dates", required = false)
    protected TestWSDateDTO testwsDTO;


    public EWSDateDTO getEwsDTO() {
        return ewsDTO;
    }

    public void setEwsDTO(EWSDateDTO ewsDTO) {
        this.ewsDTO = ewsDTO;
    }

    public QBDT_WSDateDTO getQbdtwsDTO() {
        return qbdtwsDTO;
    }

    public void setQbdtwsDTO(QBDT_WSDateDTO qbdtwsDTO) {
        this.qbdtwsDTO = qbdtwsDTO;
    }

    public QBDTDateDTO getQbdtDTO() {
        return qbdtDTO;
    }

    public void setQbdtDTO(QBDTDateDTO qbdtDTO) {
        this.qbdtDTO = qbdtDTO;
    }

    public QBOEDateDTO getQboeDTO() {
        return qboeDTO;
    }

    public void setQboeDTO(QBOEDateDTO qboeDTO) {
        this.qboeDTO = qboeDTO;
    }

    public TestWSDateDTO getTestwsDTO() {
        return testwsDTO;
    }

    public void setTestwsDTO(TestWSDateDTO testwsDTO) {
        this.testwsDTO = testwsDTO;
    }
}
