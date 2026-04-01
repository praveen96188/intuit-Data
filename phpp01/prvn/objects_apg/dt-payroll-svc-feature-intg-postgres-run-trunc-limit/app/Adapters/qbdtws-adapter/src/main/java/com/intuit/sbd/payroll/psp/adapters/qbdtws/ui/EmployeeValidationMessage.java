package com.intuit.sbd.payroll.psp.adapters.qbdtws.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rnorian
 * Date: Mar 16, 2010
 * Time: 10:42:48 PM
 */
public class EmployeeValidationMessage {
    private String employeeName;
    private String status;
    private List<String> messages = new ArrayList<String>();
    private List<String> relatedPaycheckMessages = new ArrayList<String>();

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessage(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getRelatedPaycheckMessages() {
        return relatedPaycheckMessages;
    }

    public void setRelatedPaycheckMessages(List<String> relatedPaycheckMessages) {
        this.relatedPaycheckMessages = relatedPaycheckMessages;
    }

    public int getRelatedPaycheckMessageCount() {
        return relatedPaycheckMessages.size();
    }
}
