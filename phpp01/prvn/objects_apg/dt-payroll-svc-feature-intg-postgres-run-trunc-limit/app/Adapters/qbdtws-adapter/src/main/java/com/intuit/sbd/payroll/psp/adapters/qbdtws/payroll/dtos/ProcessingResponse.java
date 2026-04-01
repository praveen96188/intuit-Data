package com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessages;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessingResponse")
public class ProcessingResponse extends QBProcessingMessages {
    @XmlElement(name = "TransmissionId", required = true)
    protected String transmissionId;

    @XmlElement(name = "NextToken", required = true)
    protected String nextToken;

    @XmlElement(name = "ClearPaycheckQueue", required = true)
    protected boolean clearPaycheckQueue = false;

    @XmlElementWrapper(name="CompanyServices")
    @XmlElement(name = "CompanyService")
    protected List<QBCompanyService> companyServices;

    public String getTransmissionId() {
        return transmissionId;
    }

    public void setTransmissionId(String value) {
        this.transmissionId = value;
    }

    public String getNextToken() {
        return nextToken;
    }

    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }

    public boolean getClearPaycheckQueue() {
        return clearPaycheckQueue;
    }

    public void setClearPaycheckQueue(boolean clearPaycheckQueue) {
        this.clearPaycheckQueue = clearPaycheckQueue;
    }

    public List<QBCompanyService> getCompanyServices() {
        if (companyServices == null) {
            companyServices = new ArrayList<QBCompanyService>(3);
        }
        return companyServices;
    }

    public void setCompanyServices(List<QBCompanyService> companyServices) {
        this.companyServices = companyServices;
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            JAXBContext ctx = JAXBContext.newInstance(getClass());
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshaller.marshal(this, sw);           
        } catch (Throwable t) {
            sw = new StringWriter();
            sw.write(t.getMessage());
        }
        return sw.toString();
    }
}
