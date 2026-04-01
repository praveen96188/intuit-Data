package com.intuit.sbd.payroll.psp.hibernate;

import com.google.gson.JsonObject;

public interface EntityChangeListener {

    public JsonObject getChangedAttribute();
    public String getuniqueId();
    public String getEntitiesName();
    public Long getEntityVersion();
    public void isDuplicate(boolean duplicate);
    public boolean getDuplicate();
}