package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.SimpleDateFormat;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EWS_Dates", propOrder = {
        "dateTimeStamp"
})

public class EWSDateDTO implements LtDateDTO{

    @XmlElement(name = "EWS_DateTimeStamp")
    public String dateTimeStamp;

    public String getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(String dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public EWSDateDTO() {
    }

    public EWSDateDTO(Date date) {
        this.setDate(date);
    }

    public void setDate(Date date){

        SimpleDateFormat sdf = new SimpleDateFormat();

        sdf.applyPattern(DateUtilities.EWS_TIMESTAMP_FORMAT);
        this.dateTimeStamp = sdf.format(date);

    }    
}
