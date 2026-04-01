package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.SimpleDateFormat;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QBOE_Dates", propOrder = {
        "systemEventSyncDate",
        "targetTxnDate"
})
public class QBOEDateDTO implements LtDateDTO{
    

    @XmlElement(name = "QBOE_SystemEventSyncDate")
    public String systemEventSyncDate;

    @XmlElement(name = "QBOE_TargetTxnDate")
    public String targetTxnDate;

    public QBOEDateDTO() {
    }

    public QBOEDateDTO(Date date) {
        this.setDate(date);
    }

    public String getSystemEventSyncDate() {
        return systemEventSyncDate;
    }

    public void setSystemEventSyncDate(String systemEventSyncDate) {
        this.systemEventSyncDate = systemEventSyncDate;
    }

    public String getTargetTxnDate() {
        return targetTxnDate;
    }

    public void setTargetTxnDate(String targetTxnDate) {
        this.targetTxnDate = targetTxnDate;
    }

    public void setDate(Date date){

        SimpleDateFormat sdf = new SimpleDateFormat();

        sdf.applyPattern(DateUtilities.QBOE_EVENTSYNC_FORMAT);
        this.systemEventSyncDate = sdf.format(date);

        sdf.applyPattern(DateUtilities.QBOE_TARGET_TXN_FORMAT);
        this.targetTxnDate = sdf.format(date);

    }
}
