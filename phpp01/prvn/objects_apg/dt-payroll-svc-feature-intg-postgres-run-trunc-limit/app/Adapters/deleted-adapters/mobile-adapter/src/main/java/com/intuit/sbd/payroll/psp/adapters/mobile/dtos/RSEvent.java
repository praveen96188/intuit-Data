package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import java.util.ArrayList;

/**
 @author Jeff Jones
 */

public class RSEvent {
    private String id;
    private String eventType;
    private String createdDate;
    private String status;
    private String description;
    private RSLinkType linkType;
    private ArrayList<String> linkIdList;
    private String kbURL;
    private ArrayList<RSEventDetail> eventDetails;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RSLinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(RSLinkType linkType) {
        this.linkType = linkType;
    }

    public ArrayList<String> getLinkIdList() {
        if (linkIdList == null)
            linkIdList = new ArrayList<String>();

        return linkIdList;
    }

    public String getKbURL() {
        return kbURL;
    }

    public void setKbURL(String kbURL) {
        this.kbURL = kbURL;
    }

    public ArrayList<RSEventDetail> getEventDetails() {
        if (eventDetails == null)
            eventDetails = new ArrayList<RSEventDetail>();

        return eventDetails;
    }
}
