package com.intuit.sbd.payroll.psp.entity.publisher;

import java.util.ArrayList;
import java.util.List;

public class EntityPublishMessage {
    private String batchId;
    private List<String> psIds = new ArrayList<>();

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public List<String> getPsIds() {
        return psIds;
    }

    public void setPsIds(List<String> psIds) {
        this.psIds = psIds;
    }

    public EntityPublishMessage(String batchId, List<String> psIds) {
        this.batchId = batchId;
        this.psIds = psIds;
    }

    public EntityPublishMessage() {
    }
}