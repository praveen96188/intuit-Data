package com.intuit.sbd.payroll.psp.gateways.wc.dto;

import com.google.gson.Gson;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;

/**
 * Created by schapparam on 4/4/14.
 */
public class WCChangeEventDTO {
    private String id;
    private String object;
    private String attribute;
    private String oldValue;
    private String newValue;
    private String eventDateTime;

    public String getObject() {
        return object;
    }

    public void setObject(String pObject) {
        object = pObject;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String pAttribute) {
        attribute = pAttribute;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String pOldValue) {
        oldValue = pOldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String pNewValue) {
        newValue = pNewValue;
    }

    public String getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(String pEventDateTime) {
        eventDateTime = pEventDateTime;
    }

    public String getId() {

        return id;
    }

    public void setId(String pId) {
        id = pId;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
