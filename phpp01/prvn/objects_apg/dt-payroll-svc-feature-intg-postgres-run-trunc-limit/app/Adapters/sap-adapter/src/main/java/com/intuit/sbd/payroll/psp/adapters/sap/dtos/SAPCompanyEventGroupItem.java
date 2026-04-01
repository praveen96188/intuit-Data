package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.EventTypeCode;

/**
 * User: cyoder
 * Date: Sep 15, 2008
 * Time: 8:23:55 AM
 */
public class SAPCompanyEventGroupItem {
    private String eventTypeCd;
    private String eventTypeName;

    public String getEventTypeCd() {
        return eventTypeCd;
    }

    public void setEventTypeCd(String eventTypeCd) {
        this.eventTypeCd = eventTypeCd;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }
}
