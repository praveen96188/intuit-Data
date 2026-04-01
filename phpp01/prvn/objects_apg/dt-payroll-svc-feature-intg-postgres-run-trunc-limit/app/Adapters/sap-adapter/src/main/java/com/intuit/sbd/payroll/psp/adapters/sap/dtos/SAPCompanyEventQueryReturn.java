package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Sep 19, 2008
 * Time: 9:33:39 AM
 */
public class SAPCompanyEventQueryReturn {
    private ArrayList<SAPCompanyEvent> events;
    private boolean moreEventsExistForQuery;

    public ArrayList<SAPCompanyEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<SAPCompanyEvent> events) {
        this.events = events;
    }

    public boolean getMoreEventsExistForQuery() {
        return moreEventsExistForQuery;
    }

    public void setMoreEventsExistForQuery(boolean moreEventsExistForQuery) {
        this.moreEventsExistForQuery = moreEventsExistForQuery;
    }
}
