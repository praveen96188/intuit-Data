package com.intuit.sbd.payroll.psp.emailsender.model;

public class ExactTargetResult {
    private String recipientId;
    private Boolean success;


    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public Boolean isSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public boolean isError() {
        return !success;
    }



}
