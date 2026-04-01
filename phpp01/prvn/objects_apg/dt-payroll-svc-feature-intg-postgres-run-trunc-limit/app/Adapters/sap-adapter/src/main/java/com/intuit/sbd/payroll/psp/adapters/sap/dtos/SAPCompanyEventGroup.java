package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.EventGroup;

import java.util.ArrayList;

/**
 * User: cyoder
 * Date: Sep 15, 2008
 * Time: 8:20:55 AM
 */
public class SAPCompanyEventGroup {
    private String name;
    private String eventGroupCode;

    private ArrayList<SAPCompanyEventGroupItem> children;

    public ArrayList<SAPCompanyEventGroupItem> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<SAPCompanyEventGroupItem> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEventGroupCode() {
        return eventGroupCode;
    }

    public void setEventGroupCode(String eventGroupCode) {
        this.eventGroupCode = eventGroupCode;
    }
}
