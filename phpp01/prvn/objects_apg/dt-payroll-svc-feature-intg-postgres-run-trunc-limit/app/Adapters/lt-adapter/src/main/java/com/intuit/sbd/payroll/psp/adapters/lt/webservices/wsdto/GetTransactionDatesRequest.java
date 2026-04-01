package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;

import javax.xml.bind.annotation.*;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetTransactionDatesRequest", propOrder = {
        "sourceSystemId",
        "dateSpec",
        "date"
})
public class GetTransactionDatesRequest {

    @XmlElement(name = "System", required = true)
    protected DateUtilities.LtSourceSystemCode sourceSystemId;

    @XmlElement(name = "DateSpecification", required = true)
    protected DateUtilities.DateSpecification dateSpec;

    @XmlElement(name = "Date", required = false)
    protected Date date;


    public DateUtilities.LtSourceSystemCode getSourceSystemId() {
        return sourceSystemId;
    }

    public void setSourceSystemId(DateUtilities.LtSourceSystemCode sourceSystemId) {
        this.sourceSystemId = sourceSystemId;
    }

    public DateUtilities.DateSpecification getDateSpec() {
        return dateSpec;
    }

    public void setDateSpec(DateUtilities.DateSpecification dateSpec) {
        this.dateSpec = dateSpec;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}


