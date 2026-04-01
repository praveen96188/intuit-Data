package com.intuit.sbd.payroll.psp.adapters.mobile.dtos;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.*;

/**
 @author Jeff Jones
 */

@XmlRootElement
public class RSResponse {

    private RSCompany company;
    private Map<String, List<RSPayee>> payees;
    private Integer recentTransmissionCount;
    private List<RSPaycheck> paychecks;
    private List<RSTransmission> transmissions;
    private Integer recentEventCount;
    private List<RSEvent> events;

    public RSResponse() {
    }

    public RSCompany getCompany() {
        return company;
    }

    public void setCompany(RSCompany company) {
        this.company = company;
    }

    public Map<String, List<RSPayee>> getPayees() {
        if (payees == null) {
            payees =  new TreeMap<String, List<RSPayee>>();
            payees.put("A", new ArrayList<RSPayee>());
            payees.put("B", new ArrayList<RSPayee>());
            payees.put("C", new ArrayList<RSPayee>());
            payees.put("D", new ArrayList<RSPayee>());
            payees.put("E", new ArrayList<RSPayee>());
            payees.put("F", new ArrayList<RSPayee>());
            payees.put("G", new ArrayList<RSPayee>());
            payees.put("H", new ArrayList<RSPayee>());
            payees.put("I", new ArrayList<RSPayee>());
            payees.put("J", new ArrayList<RSPayee>());
            payees.put("K", new ArrayList<RSPayee>());
            payees.put("L", new ArrayList<RSPayee>());
            payees.put("M", new ArrayList<RSPayee>());
            payees.put("N", new ArrayList<RSPayee>());
            payees.put("O", new ArrayList<RSPayee>());
            payees.put("P", new ArrayList<RSPayee>());
            payees.put("Q", new ArrayList<RSPayee>());
            payees.put("R", new ArrayList<RSPayee>());
            payees.put("S", new ArrayList<RSPayee>());
            payees.put("T", new ArrayList<RSPayee>());
            payees.put("U", new ArrayList<RSPayee>());
            payees.put("V", new ArrayList<RSPayee>());
            payees.put("W", new ArrayList<RSPayee>());
            payees.put("X", new ArrayList<RSPayee>());
            payees.put("Y", new ArrayList<RSPayee>());
            payees.put("Z", new ArrayList<RSPayee>());
            payees.put("#", new ArrayList<RSPayee>());
        }

        return payees;
    }

    public Integer getRecentTransmissionCount() {
        return recentTransmissionCount;
    }

    public void setRecentTransmissionCount(Integer recentTransmissionCount) {
        this.recentTransmissionCount = recentTransmissionCount;
    }

    public List<RSTransmission> getTransmissions() {
        if (transmissions == null)
            transmissions = new ArrayList<RSTransmission>();

        return transmissions;
    }

    public Integer getRecentEventCount() {
        return recentEventCount;
    }

    public void setRecentEventCount(Integer recentEventCount) {
        this.recentEventCount = recentEventCount;
    }    

    public List<RSEvent> getEvents() {
        if (events == null)
            events = new ArrayList<RSEvent>();

        return events;
    }

    public List<RSPaycheck> getPaychecks() {
        if (paychecks == null)
            paychecks = new ArrayList<RSPaycheck>();

        return paychecks;
    }

    public void setPaychecks(List<RSPaycheck> paychecks) {
        this.paychecks = paychecks;
    }
}
