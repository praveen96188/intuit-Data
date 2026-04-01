package com.intuit.sbd.payroll.psp.emailsender.model;

import com.intuit.sbd.payroll.psp.emailsender.NotificationDataType;

import java.util.ArrayList;
import java.util.List;

public class ExactTargetResults {

    private List<ExactTargetResult> result = new ArrayList<ExactTargetResult>();

    public ExactTargetResults(List<NotificationDataType.Destinations.Destination> destinations, boolean success) {
        for (NotificationDataType.Destinations.Destination destination : destinations) {
            ExactTargetResult result = new ExactTargetResult();
            result.setRecipientId(destination.getRecipientId());
            result.setSuccess(success);
            this.result.add(result);
        }
    }

    public List<ExactTargetResult> getResult() {
        return result;
    }

    public void setResult(List<ExactTargetResult> result) {
        this.result = result;
    }


}