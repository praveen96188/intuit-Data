package com.intuit.sbd.payroll.psp.gateways.wc.dto;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schapparam on 4/4/14.
 */
public class WCChangeEventResponseDTO {
    private List<String> processedEvents = new ArrayList<String>();

    public List<String> getProcessedEvents() {
        return processedEvents;
    }

    public void setProcessedEvents(List<String> pProcessedEvents) {
        processedEvents = pProcessedEvents;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
