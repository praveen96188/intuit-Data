package com.sbd.payroll.psp.adapters.keynote.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.StringWriter;

/**
 * User: ihannur
 * Date: 11/28/12
 * Time: 10:37 AM
 */
@SuppressWarnings("unused")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessingResponse")
public class KeynoteResponse extends QBProcessingMessages {
    @XmlElement(name = "Status", required = true)
    protected int status;

    @XmlElement(name = "Value", required = true)
    protected String value;

    public int getStatus() {
        return status;
    }

    public void setStatus(int pStatus) {
        status = pStatus;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String pValue) {
        value = pValue;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            JAXBContext ctx = JAXBContext.newInstance(getClass());
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(this, sw);
        } catch (Throwable t) {
            sw = new StringWriter();
            sw.write(t.getMessage());
        }
        return sw.toString();
    }

}