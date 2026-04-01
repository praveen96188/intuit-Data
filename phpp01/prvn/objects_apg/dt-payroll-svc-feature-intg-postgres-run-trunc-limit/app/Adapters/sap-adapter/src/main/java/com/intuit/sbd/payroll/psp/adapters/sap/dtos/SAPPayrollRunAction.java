package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.PayrollStatus;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 7, 2008
 * Time: 10:34:04 AM
 */
public class SAPPayrollRunAction {
    /**
     * Payroll Status
     */
    private PayrollStatus status;
    /**
     * List of Actions for a status
     */
    private ArrayList<SAPActionEvent> actionEvents;

    public PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollStatus status) {
        this.status = status;
    }

    public ArrayList<SAPActionEvent> getActionEvents() {
        return actionEvents;
    }

    public void setActionEvents(ArrayList<SAPActionEvent> actionEvents) {
        this.actionEvents = actionEvents;
    }
}
