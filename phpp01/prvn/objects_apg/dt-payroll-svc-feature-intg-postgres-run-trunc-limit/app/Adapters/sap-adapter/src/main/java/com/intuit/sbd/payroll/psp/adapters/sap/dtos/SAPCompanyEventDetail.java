package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;

/**
 * @author Wiktor Kozlik
 */
public class SAPCompanyEventDetail {
    private EventDetailTypeCode eventDetailTypeCd;
    private String name;
    private String value;
    private String valueClassName;

    public EventDetailTypeCode getEventDetailTypeCd() {
        return eventDetailTypeCd;
    }

    public void setEventDetailTypeCd(EventDetailTypeCode eventDetailTypeCd) {
        this.eventDetailTypeCd = eventDetailTypeCd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueClassName() {
        return valueClassName;
    }

    public void setValueClassName(String valueClassName) {
        this.valueClassName = valueClassName;
    }
}
